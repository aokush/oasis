/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kush.oasis.segmented;

import org.kush.oasis.OasisList;
import org.kush.oasis.util.Utilities;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

/**
 * An instance of SegmentedOasisList provides an implementation of
 * {@link java.util.List List} interface with the ability to overflow to disk
 * after a pre-determined number of items have been stored in memory.
 *
 * The disk overflow is divided into segments. The number of items in each
 * segment is configurable using a constructor argument; otherwise, a default
 * value is used. The default location for the disk overflow store is the
 * {java.io.tmdir}.oasis-collection
 *
 * An instance of this class is not thread-safe
 *
 * @author AKuseju
 * @param <E>
 */
public class SegmentedOasisList<E extends Serializable>
        implements OasisList<E>, SegmentedOasisCollection, Serializable {

    /*
     * A shared store for all instances in teh same process
     */
    private static final File STORE;

    /*
     * Holds a Queue of directories to remove when the jvm exists
     */
    private static final ConcurrentLinkedQueue<String> directoriesToClean = new ConcurrentLinkedQueue<>();

    public static final String PERSISTED_SEGMENT_STORE_PREFIX = "oasis-lists-";

    /*
     * The number of items stored in this instance
     */
    private int size;

    /*
     * The path to store for this instance
     */
    private String storagePath;

    /*
     * Max number of item to be stored in primary memory store
     */
    private int itemsStoredInMemory;

    /*
     * Max number of item to be stored in each segment
     */
    private int itemsStoredIneachSegment;

    /*
     * primary memory store
     */
    private List<E> memoryStore = new ArrayList<>();

    /*
     * Segment store
     */
    private SegmentContext<List<E>> newSegment;

    /*
     * A list for tracking the file names of the persisted segments
     */
    private List<String> persistedMapTracker = new ArrayList<>();

    /*
     * Is the cache enabled
     */
    private boolean isCached;

    /*
     * Has this instance been marked for destruction
     */
    private boolean isDestroyed;

    /*
     * A simple cache for tracking persisted segments loaded into memory
     */
    private Map<String, SegmentContext> cache;

    static {
        DateFormat fmt = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.FULL);
        STORE = new File(BASE_STORE_NAME + PERSISTED_SEGMENT_STORE_PREFIX
                + Utilities.replaceSpecialChars(fmt.format(new Date())));
        STORE.getParentFile().deleteOnExit();
        STORE.deleteOnExit();
    }

    // A shutdown hook to remove all created directories and files
    // for all instances of SegmentedOasisList when the jvm is exiting
    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                for (String dir : directoriesToClean) {
                    Utilities.rmdir(new File(dir));
                }
            }
        });
    }

    /**
     * Creates an instance with
     * {@link org.kush.oasis.SegmentedOasisCollection#OJBECT_COUNT_IN_MEMORY 200000}
     * items in memory and
     * {@link org.kush.oasis.SegmentedOasisCollection#SEGMENT_SIZE 50000} segment
     * size
     */
    public SegmentedOasisList() {
        this(OJBECT_COUNT_IN_MEMORY, SEGMENT_SIZE);
    }

    /**
     * Creates an instance with a custom number for items in memory and
     * {@link org.kush.oasis.SegmentedOasisCollection#SEGMENT_SIZE 50000} segment
     * size
     *
     * @param itemsStoredInMemory
     */
    public SegmentedOasisList(int itemsStoredInMemory) {
        this(itemsStoredInMemory, SEGMENT_SIZE);
    }

    /**
     * Creates an instance with a custom number for items in memory and segment size
     *
     * @param itemsStoredInMemory
     * @param segmentSize
     */
    public SegmentedOasisList(int itemsStoredInMemory, int segmentSize) {
        this.itemsStoredInMemory = itemsStoredInMemory;
        this.itemsStoredIneachSegment = segmentSize;

        storagePath = prepareStorage(STORE.getAbsolutePath());
    }

    /**
     * Creates an instance with a default number for items in memory and segment
     * size. Segments are stored in the location provided as arguments
     *
     * @param diskStorePath
     */
    public SegmentedOasisList(String diskStorePath) {
        this(OJBECT_COUNT_IN_MEMORY, SEGMENT_SIZE, diskStorePath);
    }

    /**
     * Creates an instance with a default number for items in memory and segment
     * size. Segments are stored in the location provided as arguments
     *
     * @param diskStorePath
     */
    public SegmentedOasisList(File diskStorePath) {
        this(OJBECT_COUNT_IN_MEMORY, SEGMENT_SIZE, diskStorePath.getAbsolutePath());
    }

    /**
     * Creates an instance with a custom number for items in memory, segment size
     * and stored ocation provided as arguments
     *
     * @param itemsStoredInMemory
     * @param segmentSize
     * @param diskStorePath
     */
    public SegmentedOasisList(int itemsStoredInMemory, int segmentSize, File diskStorePath) {
        this(itemsStoredInMemory, segmentSize, diskStorePath.getAbsolutePath());
    }

    /**
     * Creates an instance with a custom number for items in memory, segment size
     * and stored ocation provided as arguments
     *
     * @param itemsStoredInMemory
     * @param segmentSize
     * @param diskStorePath
     */
    public SegmentedOasisList(int itemsStoredInMemory, int segmentSize, String diskStorePath) {
        this.itemsStoredInMemory = itemsStoredInMemory;
        this.itemsStoredIneachSegment = segmentSize;
        memoryStore = new ArrayList<>();
        storagePath = prepareStorage(diskStorePath);
        ;
    }

    private String prepareStorage(String customPath) {
        File instanceStore = null;

        String uniqueDir = File.separator + UUID.randomUUID().toString();
        // Using defauly persisted segment store
        if (customPath.equals(STORE.getAbsolutePath())) {
            instanceStore = new File(customPath + uniqueDir);
        } else {
            // construct directory structure similar to teh default execpt
            // that the base location is diffrent

            // extract the oasis collection direcotory in odrer to create a similar
            // structure in the custom
            // location provided
            String dirStruc = STORE.getAbsolutePath().substring(System.getProperty("java.io.tmpdir").length());
            ;
            instanceStore = new File(customPath + dirStruc + uniqueDir);

            instanceStore.getParentFile().deleteOnExit();
        }

        instanceStore.mkdirs();
        instanceStore.deleteOnExit();
        directoriesToClean.add(instanceStore.getAbsolutePath());

        return instanceStore.getAbsolutePath();
    }

    /**
     * {@link java.util.List#size() List.size()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public int size() {
        checkIfDestroyed();
        return size;
    }

    /**
     * {@link java.util.List#isEmpty() List.isEmpty()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public boolean isEmpty() {
        checkIfDestroyed();
        return size == 0;
    }

    /**
     * {@link java.util.List#contains() List.contains()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public boolean contains(Object o) {
        checkIfDestroyed();
        boolean found = false;
        try {

            E castElement = (E) o;
            if (!memoryStore.isEmpty()) {
                found = memoryStore.contains(o);
            }

            if (!found && !memoryStore.isEmpty()) {

                if (newSegment != null) {
                    found = newSegment.getData().contains(o);
                }
                if (!found) {

                    for (String fileName : persistedMapTracker) {

                        SegmentContext<List<E>> read = getSegment(fileName);
                        if (!read.getData().isEmpty() && (found = read.getData().contains(o))) {
                            break;
                        }
                    }
                }
            }

        } catch (ClassCastException cce) {

        }

        return found;
    }

    /**
     * {@link java.util.List#toArray() List.toArray()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public Iterator<E> iterator() {
        checkIfDestroyed();
        return new OasisSegmentedListIterator(this);
    }

    /**
     * {@link java.util.List#toArray() List.toArray()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public Object[] toArray() {

        checkIfDestroyed();
        List<E> elems = new ArrayList<>(size);

        if (!isEmpty()) {
            elems.addAll(memoryStore);

            for (String fileName : persistedMapTracker) {
                elems.addAll(getSegment(fileName).getData());
            }

            if (newSegment != null) {
                elems.addAll(newSegment.getData());
            }

        }

        return elems.toArray();
    }

    /**
     * {@link java.util.List#toArray(T[] a) List.toArray(T[] a)}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public <T> T[] toArray(T[] a) {
        checkIfDestroyed();
        List<E> elems = null;

        try {
            T t = null;
            E e = (E) t;

            if (!isEmpty()) {
                elems = new ArrayList<>(size);
                elems.addAll(memoryStore);

                for (String fileName : persistedMapTracker) {
                    elems.addAll(getSegment(fileName).getData());
                }

                if (newSegment != null) {
                    elems.addAll(newSegment.getData());
                }

            }

        } catch (ClassCastException e) {
            throw new ArrayStoreException("Argument not compatible");
        }

        return elems != null ? elems.toArray(a) : a;
    }

    /**
     * {@link java.util.List#add(E e) List.add(E e)}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public boolean add(E e) {
        checkIfDestroyed();
        boolean isAdded = false;

        int originalSize = memoryStore.size();
        if (originalSize < this.itemsStoredInMemory && originalSize == size) {
            isAdded = memoryStore.add(e);
        } else {
            if (newSegment == null) {
                newSegment = new SegmentContext(new ArrayList<>());
            }

            if (newSegment.getData().size() == itemsStoredIneachSegment) {
                String fileName = getNextSegmentName();
                newSegment.markDirty();
                writeSegment(newSegment, fileName);
                persistedMapTracker.add(fileName);
                newSegment = new SegmentContext(new ArrayList<>());
            }

            isAdded = newSegment.getData().add(e);

        }

        if (isAdded) {
            ++size;
        }

        return isAdded;
    }

    /**
     * {@link java.util.List#remove(Object o) List.remove(Object o)}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public boolean remove(Object o) {
        checkIfDestroyed();
        boolean isRemoved = false;

        if (!memoryStore.isEmpty()) {
            isRemoved = memoryStore.remove(o);
        }

        if (!isRemoved && !persistedMapTracker.isEmpty()) {

            String fileName;
            for (int i = 0; i < persistedMapTracker.size(); i++) {

                fileName = persistedMapTracker.get(i);

                SegmentContext<List<E>> read = getSegment(fileName);
                if (!read.getData().isEmpty() && (isRemoved = read.getData().remove(o))) {
                    read.markDirty();
                    writeSegment(read, fileName);
                    break;
                }
            }

            if (!isRemoved && newSegment != null) {
                isRemoved = newSegment.getData().remove(o);
            }

        }

        if (isRemoved) {
            --size;
        }

        return isRemoved;
    }

    /**
     * {@link java.util.List#containsAll(Collection<?> c)
     * List.containsAll(Collection<?> c)}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        checkIfDestroyed();
        boolean containsAll = true;

        for (Object item : c) {
            if (!(containsAll = contains(item))) {
                break;
            }
        }

        return containsAll;
    }

    /**
     * {@link java.util.List#addAll(Collection<? extends E> c)
     * ListaddAll(Collection<? extends E> c)}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        checkIfDestroyed();
        boolean allAdded = true;

        for (E item : c) {
            if (!(allAdded = add(item))) {
                break;
            }
        }

        return allAdded;
    }

    /**
     * {@link java.util.List#addAll(int index, Collection<? extends E> c)
     * ListaddAll(int index, Collection<? extends E> c)}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {

        checkIfDestroyed();
        for (E item : c) {
            add(index++, item);
        }

        return true;
    }

    /**
     * {@link java.util.List#removeAll(Collection<?> c) List.removeAll(Collection<?>
     * c)}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public boolean removeAll(Collection<?> c) {

        checkIfDestroyed();
        int totalStartSize = size;

        int startSize;
        if (!memoryStore.isEmpty()) {
            startSize = memoryStore.size();
            memoryStore.removeAll(c);

            size += memoryStore.size() - startSize;
        }

        if (!persistedMapTracker.isEmpty()) {

            String fileName;
            SegmentContext<List<E>> read;
            for (int i = 0; i < persistedMapTracker.size(); i++) {
                fileName = persistedMapTracker.get(i);
                read = getSegment(fileName);

                startSize = read.getData().size();
                read.getData().removeAll(c);

                if (startSize != read.getData().size()) {
                    read.markDirty();
                    writeSegment(read, fileName);
                    size += read.getData().size() - startSize;
                }

            }

        }

        if (newSegment != null) {
            startSize = newSegment.getData().size();
            newSegment.getData().removeAll(c);
            size += newSegment.getData().size() - startSize;
        }

        return totalStartSize > size;
    }

    /**
     * {@link java.util.List#retainAll(Collection<?> c) List.retainAll(Collection<?>
     * c)}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        checkIfDestroyed();
        int totalStartSize = size;

        int startSize;
        if (!memoryStore.isEmpty()) {
            startSize = memoryStore.size();
            memoryStore.retainAll(c);

            size += memoryStore.size() - startSize;
        }

        if (!persistedMapTracker.isEmpty()) {

            String fileName;
            SegmentContext<List<E>> read;
            for (int i = 0; i < persistedMapTracker.size(); i++) {
                fileName = persistedMapTracker.get(i);
                read = getSegment(fileName);

                startSize = read.getData().size();
                read.getData().retainAll(c);
                if (startSize != read.getData().size()) {
                    read.markDirty();
                    writeSegment(read, fileName);
                    size += read.getData().size() - startSize;
                }

            }

        }

        if (newSegment != null) {
            startSize = newSegment.getData().size();
            newSegment.getData().retainAll(c);
            size += newSegment.getData().size() - startSize;
        }

        return totalStartSize > size;
    }

    /**
     * {@link java.util.List#clear() List.clear()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public void clear() {

        checkIfDestroyed();
        memoryStore.clear();
        if (size > itemsStoredInMemory) {
            if (newSegment != null) {
                newSegment.getData().clear();
            }

            for (String f : persistedMapTracker) {
                File file = new File(f);
                try {
                    Files.deleteIfExists(file.toPath());
                } catch (IOException ex) {
                    Logger.getLogger(SegmentedOasisList.class.getName()).log(Level.WARNING,
                            String.format("Unable to delete %s. Will try to delete on prograsm termination ",
                                    file.getAbsolutePath()));
                }
            }

        }

        if (cache != null) {
            cache.clear();
        }

        persistedMapTracker.clear();
        size = 0;
    }

    /**
     * {@link java.util.List#get(int index) List.get(int index)}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public E get(int index) {
        checkIfDestroyed();
        E e = null;

        int itemCount;
        if (index >= 0 && index < size) {

            itemCount = memoryStore.size();

            if (index < itemCount) {
                e = memoryStore.get(index);
            } else {

                for (String f : persistedMapTracker) {
                    SegmentContext<List<E>> segCtx = getSegment(f);

                    if (index < (itemCount + segCtx.getData().size())) {
                        e = segCtx.getData().get(index - itemCount);
                        break;
                    }

                    itemCount += segCtx.getData().size();
                }
            }

            if (e == null && newSegment != null && index < itemCount + newSegment.getData().size()) {
                e = newSegment.getData().get(index - itemCount);
            }

        } else {
            throw new IndexOutOfBoundsException(String.format("Index %s is invalid", index));
        }

        return e;
    }

    /**
     * {@link java.util.List#set(int index, E element) List.set(int index, E
     * element)}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public E set(int index, E element) {
        checkIfDestroyed();
        E previous = null;

        int itemCount;
        if (index >= 0 && index < size) {

            itemCount = memoryStore.size();

            if (index < itemCount) {
                previous = memoryStore.set(index, element);
            } else {

                for (String f : persistedMapTracker) {
                    SegmentContext<List<E>> segCtx = getSegment(f);

                    if (index < (itemCount + segCtx.getData().size())) {
                        previous = segCtx.getData().set(index - itemCount, element);
                        segCtx.markDirty();
                        writeSegment(segCtx, f);
                        break;
                    }

                    itemCount += segCtx.getData().size();
                }
            }

            if (previous == null && newSegment != null && index < itemCount + newSegment.getData().size()) {
                previous = newSegment.getData().set(index - itemCount, element);
            }

        } else {
            throw new IndexOutOfBoundsException(String.format("Index %s is invalid", index));
        }

        return previous;
    }

    /**
     * {@link java.util.List#add(int index, E element) List.add(int index, E
     * element)}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public void add(int index, E element) {
        checkIfDestroyed();
        boolean isAdded = false;
        int itemCount;
        if (index >= 0 && index < size) {

            itemCount = memoryStore.size();

            if (index < itemCount) {
                memoryStore.add(index, element);
                isAdded = true;
            } else {

                for (String f : persistedMapTracker) {
                    SegmentContext<List<E>> segCtx = getSegment(f);

                    if (index < (itemCount + segCtx.getData().size())) {
                        segCtx.getData().add(index - itemCount, element);
                        segCtx.markDirty();
                        writeSegment(segCtx, f);
                        isAdded = true;
                        break;
                    }

                    itemCount += segCtx.getData().size();
                }
            }

            if (!isAdded && newSegment != null && index < itemCount + newSegment.getData().size()) {
                newSegment.getData().add(index - itemCount, element);

                if (newSegment.getData().size() == itemsStoredIneachSegment) {
                    String fileName = getNextSegmentName();
                    newSegment.markDirty();
                    persistedMapTracker.add(fileName);
                    writeSegment(newSegment, fileName);
                    newSegment = null;
                }
            }

            ++size;

        } else {
            throw new IndexOutOfBoundsException(String.format("Index %s is invalid", index));
        }
    }

    /**
     * {@link java.util.List#remove(int index) List.remove(int index)}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public E remove(int index) {
        checkIfDestroyed();
        E previous = null;

        int itemCount;
        if (index >= 0 && index < size) {

            itemCount = memoryStore.size();

            if (index < itemCount) {
                previous = memoryStore.remove(index);
            } else {

                for (String f : persistedMapTracker) {
                    SegmentContext<List<E>> segCtx = getSegment(f);

                    if (index < (itemCount + segCtx.getData().size())) {
                        previous = segCtx.getData().remove(index - itemCount);
                        segCtx.markDirty();
                        writeSegment(segCtx, f);

                        break;
                    }

                    itemCount += segCtx.getData().size();
                }
            }

            if (previous == null && newSegment != null && index < itemCount + newSegment.getData().size()) {
                previous = newSegment.getData().remove(index - itemCount);
            }

            if (previous != null) {
                --size;
            }

        } else {
            throw new IndexOutOfBoundsException(String.format("Index %s is invalid", index));
        }

        return previous;
    }

    /**
     * {@link java.util.List#indexOf(Object o) List.indexOf(Object o)}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public int indexOf(Object o) {
        checkIfDestroyed();
        int index = -1;

        int itemCount = 0;
        if (size > 0) {

            index = memoryStore.indexOf(o);
            if (index < 0) {

                itemCount = memoryStore.size();
                SegmentContext<List<E>> segCtx;

                for (String f : persistedMapTracker) {
                    segCtx = getSegment(f);

                    index = segCtx.getData().indexOf(o);
                    if (index >= 0) {
                        index = itemCount + index;
                        break;
                    }

                    itemCount += segCtx.getData().size();
                }

            }

            if (index < 0 && newSegment != null) {
                index = newSegment.getData().indexOf(o);
                if (index >= 0) {
                    index = itemCount + index;

                }
            }

        }

        return index;
    }

    /**
     * {@link java.util.List#lastIndexOf(Object o) List.lastIndexOf(Object o)}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public int lastIndexOf(Object o) {
        checkIfDestroyed();
        int index = -1;

        int itemCount = 0;
        if (size > 0) {

            if (newSegment != null) {
                index = newSegment.getData().lastIndexOf(o);
                itemCount = newSegment.getData().size();

                if (index >= 0) {
                    index = (size - itemCount) + index;
                }

            }

            if (index < 0) {
                SegmentContext<List<E>> segCtx;

                for (int i = persistedMapTracker.size() - 1; i >= 0; i--) {
                    segCtx = getSegment(persistedMapTracker.get(i));
                    itemCount += segCtx.getData().size();
                    index = segCtx.getData().lastIndexOf(o);

                    if (index >= 0) {
                        index = (size - itemCount) + index;
                        break;
                    }
                }
            }

            if (index < 0) {
                index = memoryStore.lastIndexOf(o);
            }

        }

        return index;
    }

    /**
     * {@link java.util.List#listIterator() List.listIterator()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public ListIterator<E> listIterator() {
        checkIfDestroyed();
        return new OasisSegmentedListIterator(this);
    }

    /**
     * {@link java.util.List#listIterator(int index) List.listIterator(int index)}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public ListIterator<E> listIterator(int index) {
        checkIfDestroyed();
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of range");
        }

        return new OasisSegmentedListIterator(this, index);
    }

    /**
     * {@link java.util.List#subList(int fromIndex, int toIndex) List.subList(int
     * fromIndex, int toIndex)}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        checkIfDestroyed();
        List<E> previous;

        int itemCount;
        if (toIndex >= fromIndex && fromIndex >= 0 && toIndex <= size) {

            previous = new SegmentedOasisList<>(toIndex - fromIndex);
            itemCount = memoryStore.size();

            if (fromIndex < itemCount) {

                if (toIndex <= itemCount) {
                    previous.addAll(memoryStore.subList(fromIndex, toIndex));
                } else {
                    previous.addAll(memoryStore.subList(fromIndex, itemCount));

                    SegmentContext<List<E>> segCtx;

                    for (String f : persistedMapTracker) {
                        segCtx = getSegment(f);

                        if (toIndex <= (itemCount + segCtx.getData().size())) {
                            previous.addAll(segCtx.getData().subList(0, toIndex - itemCount));
                            break;
                        }

                        itemCount += segCtx.getData().size();
                    }
                }

            } else {

                SegmentContext<List<E>> segCtx;
                for (String f : persistedMapTracker) {
                    segCtx = getSegment(f);

                    if (fromIndex < (itemCount + segCtx.getData().size())) {

                        if (toIndex <= (itemCount + segCtx.getData().size())) {
                            previous.addAll(segCtx.getData().subList(fromIndex - itemCount, toIndex - itemCount));
                            break;
                        } else {
                            previous.addAll(segCtx.getData().subList(fromIndex - itemCount, segCtx.getData().size()));
                        }

                    }

                    itemCount += segCtx.getData().size();
                }
            }

            if (newSegment != null && previous.size() < toIndex - fromIndex) {

                if (!previous.isEmpty()) {
                    previous.addAll(newSegment.getData().subList(0, toIndex - itemCount));
                } else {
                    previous.addAll(newSegment.getData().subList(fromIndex - itemCount, toIndex - itemCount));
                }
            }

        } else {
            throw new IndexOutOfBoundsException(String.format("Index range % to %s is invalid", fromIndex, toIndex));
        }

        return previous;
    }

    @Override
    protected void finalize() throws Throwable {
        cleanup();
        super.finalize();

    }

    /**
     * {@link org.kush.oasis.SegmentedOasisCollection#enableCache()
     * SegmentedOasisCollection.enableCache()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public void enableCache() {

        checkIfDestroyed();
        if (!isCached) {
            isCached = true;
            cache = new HashMap<>();
        }
    }

    /**
     * {@link org.kush.oasis.SegmentedOasisCollection#disableCache()
     * SegmentedOasisCollection.disableCache()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public void disableCache() {
        checkIfDestroyed();
        if (isCached && !isEmpty()) {
            compact();
            isCached = false;
            cache.forEach((k, v) -> {
                writeSegment(v, k);
            });
        }

        isCached = false;
        cache = null;

    }

    /**
     * {@link org.kush.oasis.SegmentedOasisCollection#isCacheEnabled()
     * SegmentedOasisCollection.isCacheEnabled()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public boolean isCacheEnabled() {
        checkIfDestroyed();
        return isCached;
    }

    /**
     * {@link org.kush.oasis.SegmentedOasisCollection#destroy()
     * SegmentedOasisCollection.destroy()}
     */
    @Override
    public void destroy() {

        // don't do anything destroy has been called on this instance already
        if (!isDestroyed) {
            clear();
            try {
                finalize();
            } catch (Throwable ex) {
                Logger.getLogger(SegmentedOasisList.class.getName()).log(Level.SEVERE, null, ex);
            }
            isDestroyed = true;
        }
    }

    private void checkIfDestroyed() {
        if (isDestroyed) {
            throw new IllegalStateException("Destroy has already been called on this instance");
        }
    }

    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {

        stream.writeInt(itemsStoredInMemory);
        stream.writeInt(itemsStoredIneachSegment);
        stream.writeInt(size);
        stream.writeUTF(storagePath);
        stream.writeObject(memoryStore);
        stream.writeObject(newSegment);
        stream.writeObject(persistedMapTracker);

        for (String fileName : persistedMapTracker) {
            stream.writeObject(getSegment(fileName).getData());
        }

    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {

        itemsStoredInMemory = stream.readInt();
        itemsStoredIneachSegment = stream.readInt();
        size = stream.readInt();
        storagePath = stream.readUTF();
        memoryStore = (List<E>) stream.readObject();
        newSegment = (SegmentContext) stream.readObject();

        persistedMapTracker = (List<String>) stream.readObject();

        // Ensure directory structure is recreated in case
        // it had been removed
        File file = new File(storagePath);
        file.mkdirs();

        if (!persistedMapTracker.isEmpty()) {

            SegmentContext<List<E>> segCtx = new SegmentContext<>();
            for (String fileName : persistedMapTracker) {
                segCtx.setData((List<E>) stream.readObject());
                segCtx.markDirty();
                writeSegment(segCtx, fileName);
            }

        }
    }

    private SegmentContext<List<E>> getSegment(String fileName) {

        SegmentContext<List<E>> read = null;

        if (isCached) {
            read = getSegmentFromCache(fileName);
        } else {
            read = getSegmentFromStore(fileName);
        }

        return read;
    }

    private SegmentContext getSegmentFromCache(String fileName) {

        SegmentContext read = null;

        if (isCached) {
            read = cache.get(fileName);

            if (read == null) {
                read = getSegmentFromStore(fileName);
                cache.put(fileName, read);
            }
        }

        return read;
    }

    private SegmentContext getSegmentFromStore(String fileName) {

        SegmentContext read = null;

        try (FileInputStream fileStrm = new FileInputStream(new File(fileName));
                FSTObjectInput in = new FSTObjectInput(fileStrm)) {
            read = new SegmentContext((List<E>) in.readObject(List.class));

        } catch (Exception ex) {
            Logger.getLogger(SegmentedHashOasisMap.class.getName()).log(Level.SEVERE, null, ex);
        }

        return read;

    }

    private void writeSegment(SegmentContext sgtCtx, String fileName) {

        if (sgtCtx.isDirty()) {
            if (isCached) {
                cache.put(fileName, sgtCtx);
            } else {

                try (FileOutputStream fileStrm = new FileOutputStream(new File(fileName));
                        FSTObjectOutput out = new FSTObjectOutput(fileStrm)) {
                    out.writeObject(sgtCtx.getData(), List.class);

                } catch (Exception ex) {
                    Logger.getLogger(SegmentedHashOasisMap.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

        }
    }

    private String getNextSegmentName() {
        return storagePath + File.separator + "segment-" + (persistedMapTracker.size() + 1);
    }

    /**
     * {@link org.kush.oasis.SegmentedOasisCollection#instanceStore()
     * SegmentedOasisCollection.instanceStore()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public String instanceStore() {
        checkIfDestroyed();
        return storagePath;
    }

    /**
     * {@link org.kush.oasis.SegmentedOasisCollection#persistedSegmentCount()
     * SegmentedOasisCollection.persistedSegmentCount()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public int persistedSegmentCount() {
        checkIfDestroyed();
        return persistedMapTracker.size();
    }

    private void cleanup() {
        Utilities.rmdir(new File(storagePath));
    }

    /**
     * {@link org.kush.oasis.SegmentedOasisCollection#compact()
     * SegmentedOasisCollection.compact()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public void compact() {

        checkIfDestroyed();
        if (!isCached) {
            initCache();
            persistedMapTracker.forEach(it -> {
                cache.put(it, getSegment(it));
            });
        }

        try {
            // In mots cases persistedMapTracker and cache should have the same size,
            // however,
            // if the cache is partly loaded as it it loaded onh demand, then their sizes
            // will be different; therefore, skip compacting to avoid expensive I/O
            if (!persistedMapTracker.isEmpty() && persistedMapTracker.size() == cache.size()) {
                compact(new SegmentContext(memoryStore), cache.get(persistedMapTracker.get(0)), itemsStoredInMemory,
                        itemsStoredIneachSegment);

                for (int i = 1; i < persistedMapTracker.size(); i++) {
                    compact(cache.get(persistedMapTracker.get(i - 1)), cache.get(persistedMapTracker.get(i)),
                            itemsStoredIneachSegment, itemsStoredIneachSegment);
                }

                if (newSegment != null && !newSegment.getData().isEmpty()) {
                    compact(cache.get(persistedMapTracker.get(persistedMapTracker.size() - 1)), newSegment,
                            itemsStoredIneachSegment, itemsStoredIneachSegment);
                }

                removeEmptySegments();

                if (!isCached) {
                    persistedMapTracker.forEach(it -> {
                        writeSegment(cache.get(it), it);
                    });
                }
            }

        } finally {

            if (!isCached) {
                cache = null;
            }
        }
    }

    /**
     * 
     * Compact this list toby moving items on disk to memory as long as max item
     * stored in memory is not exceeded.
     * 
     * Fast compaction is only applicable when the cache is not in use.
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    // @Override
    public void compactFast() {

        checkIfDestroyed();
        if (!isCached) {

            int segTracker = 0;
            while (memoryStore.size() < this.itemsStoredInMemory && !persistedMapTracker.isEmpty()) {

                SegmentContext<List<E>> sgtCtx = getSegment(persistedMapTracker.get(segTracker));
                if (!sgtCtx.getData().isEmpty()) {

                    int itemsToMove = this.itemsStoredInMemory - memoryStore.size();

                    // if the memory store has enough room for the entire data of at
                    // least one segment
                    if (itemsToMove >= sgtCtx.getData().size()) {
                        memoryStore.addAll(sgtCtx.getData().subList(0, sgtCtx.getData().size()));
                        sgtCtx.getData().clear();
                        String filename = persistedMapTracker.remove(segTracker);

                        File toDelete = new File(filename);
                        try {
                            Files.delete(toDelete.toPath());
                        } catch (IOException e) {
                            toDelete.deleteOnExit();
                            Logger.getLogger(SegmentedHashOasisMap.class.getName()).log(Level.WARNING,
                                    "Unable to delete empty segment file {0}. Will try to delete on prograsm termination ",
                                    toDelete.getAbsolutePath());
                        }

                    } else {
                        memoryStore.addAll(sgtCtx.getData().subList(0, itemsToMove));
                        sgtCtx.setData(new ArrayList<>(sgtCtx.getData().subList(itemsToMove, sgtCtx.getData().size())));
                        sgtCtx.markDirty();

                        writeSegment(sgtCtx, persistedMapTracker.get(segTracker));
                    }

                }

            }

        }

    }

    @Override
    public int hashCode() {
        checkIfDestroyed();
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.size);
        hash = 83 * hash + Objects.hashCode(this.memoryStore);

        if (newSegment != null) {
            hash = 83 * hash + Objects.hashCode(this.newSegment);
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        checkIfDestroyed();
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        try {
            final SegmentedOasisList<? extends E> other = (SegmentedOasisList<? extends E>) obj;

            if (this.size != other.size) {
                return false;
            }
            if (!Objects.equals(this.memoryStore, other.memoryStore)) {
                return false;
            }
            if (!Objects.equals(this.newSegment, other.newSegment)) {
                return false;
            }

        } catch (ClassCastException cce) {

            try {
                final List<? extends E> other = (List<? extends E>) obj;

                for (int i = 0; i < other.size(); i++) {
                    if (!Objects.equals(other.get(i), get(i))) {
                        return false;
                    }
                }

            } catch (ClassCastException cce2) {
                return false;
            }
        }
        return true;
    }

    private void initCache() {
        cache = new HashMap<>();
    }

    private void compact(SegmentContext<List<E>> dest, SegmentContext<List<E>> src, int destMaxSize,
            int sourceMaxSize) {

        if (dest.getData().size() < destMaxSize && !src.getData().isEmpty()) {

            if (destMaxSize - dest.getData().size() >= src.getData().size()) {
                dest.getData().addAll(src.getData());
                dest.markDirty();
                src.getData().clear();
            } else {
                int toExtract = destMaxSize - dest.getData().size();
                dest.getData().addAll(src.getData().subList(0, toExtract));
                src.setData(new ArrayList(src.getData().subList(toExtract, src.getData().size())));

                dest.markDirty();
                src.markDirty();
            }
        } else if (dest.getData().size() > destMaxSize && src.getData().size() < sourceMaxSize) {

            int toExtract = sourceMaxSize - src.getData().size();
            src.getData().addAll(0, dest.getData().subList(toExtract, dest.getData().size()));
            dest.setData(new ArrayList(dest.getData().subList(0, dest.getData().size() - toExtract)));

            dest.markDirty();
            src.markDirty();
        }

    }

    private void removeEmptySegments() {

        Iterator<String> itr = persistedMapTracker.iterator();
        String item;
        while (itr.hasNext()) {
            item = itr.next();
            SegmentContext<List<E>> cacheItem = cache.get(item);

            if (cacheItem.getData().isEmpty()) {
                cache.remove(item);
                itr.remove();
                File file = new File(item);
                try {
                    Files.deleteIfExists(file.toPath());
                } catch (IOException ex) {
                    file.deleteOnExit();
                    Logger.getLogger(SegmentedHashOasisMap.class.getName()).log(Level.WARNING,
                            String.format("Unable to delete %s. Will try to delete on prograsm termination ",
                                    file.getAbsolutePath()));
                }
            }

        }

        // List filesToRemove = new ArrayList<>();
        // persistedMapTracker.forEach(it -> {
        // List<E> cacheItem = cache.get(it);
        //
        // if (cacheItem.isEmpty()) {
        // cache.remove(it);
        // filesToRemove.add(it);
        // File file = new File(it);
        // try {
        // Files.deleteIfExists(file.toPath());
        // } catch (IOException ex) {
        // file.deleteOnExit();
        // Logger
        // .getLogger(SegmentedOasisMap.class
        // .getName()).log(Level.WARNING, String.format("Unable to delete %s. Will try
        // to delete on prograsm termination ", file.getAbsolutePath()));
        // }
        // }
        // });
        //
        // persistedMapTracker.removeAll(filesToRemove);
    }

    static class OasisSegmentedListIterator<E extends Serializable> implements ListIterator<E> {

        public enum LastCall {
            NEXT, PREVIOUS, REMOVE, SET, ADD
        }

        private final OasisList<E> backingList;
        private int currentIndex;
        private LastCall lastCall;

        public OasisSegmentedListIterator(OasisList<E> backingList) {
            this.backingList = backingList;
        }

        public OasisSegmentedListIterator(OasisList<E> backingList, int startIndex) {

            if (startIndex < 0 || startIndex >= backingList.size()) {
                throw new IndexOutOfBoundsException("Index out of range");
            }
            this.backingList = backingList;
            currentIndex = startIndex;
        }

        @Override
        public boolean hasNext() {
            return currentIndex < backingList.size();
        }

        @Override
        public E next() {
            if (currentIndex == backingList.size()) {
                throw new NoSuchElementException("No more element to retrieve");
            }

            lastCall = LastCall.NEXT;
            return backingList.get(currentIndex++);
        }

        @Override
        public boolean hasPrevious() {
            return currentIndex > 0;
        }

        @Override
        public E previous() {
            if (!hasPrevious()) {
                throw new NoSuchElementException("Elemnt does not exist");
            }

            lastCall = LastCall.PREVIOUS;
            return backingList.get(--currentIndex);
        }

        @Override
        public int nextIndex() {
            return currentIndex;
        }

        @Override
        public int previousIndex() {
            return currentIndex - 1;
        }

        @Override
        public void remove() {

            if (lastCall != null && lastCall.equals(LastCall.NEXT)) {
                backingList.remove(--currentIndex);
                lastCall = LastCall.REMOVE;
            } else if (lastCall != null && lastCall.equals(LastCall.PREVIOUS)) {
                lastCall = LastCall.REMOVE;
                backingList.remove(currentIndex);
            } else {
                throw new IllegalStateException(
                        "remove method can only be called once after previous or next method has been called");
            }
        }

        @Override
        public void set(E e) {

            try {
                if (lastCall != null && lastCall.equals(LastCall.NEXT)) {
                    backingList.set(currentIndex - 1, e);
                    lastCall = LastCall.SET;
                } else if (lastCall != null && lastCall.equals(LastCall.PREVIOUS)) {
                    backingList.set(currentIndex, e);
                    lastCall = LastCall.SET;
                } else {
                    throw new IllegalStateException(
                            "set method can only be called once after previous or next method has been called");
                }
            } catch (IllegalStateException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        @Override
        public void add(E e) {

            try {
                if (currentIndex == backingList.size()) {
                    backingList.add(e);
                } else {
                    backingList.add(currentIndex, e);
                }
                currentIndex++;
                lastCall = LastCall.ADD;

            } catch (Exception ex) {
                throw new IllegalArgumentException(ex);
            }
        }

    }
}
