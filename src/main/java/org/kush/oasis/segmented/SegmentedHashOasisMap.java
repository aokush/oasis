package org.kush.oasis.segmented;

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kush.oasis.OasisMap;
import org.kush.oasis.util.Utilities;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

/**
 * An instance of SegmentedOasisMap is an implementation of Java Map interface
 * that start overflowing to disk (file system) after a certain number of items
 * have been stored in memory
 *
 * @author AKuseju
 * @param <K> The key type for this Map
 * @param <V> The value type for this Map
 */
public class SegmentedHashOasisMap<K extends Serializable, V extends Serializable> implements Serializable, OasisMap<K, V>, SegmentedOasisCollection {

    /*
     * A shared store for all instances in teh same process
     */
    private static final File STORE;
    
    /*
     * Holds a Queue of directories to remove when the jvm exists
     */
    private static final ConcurrentLinkedQueue<String> directoriesToClean = new ConcurrentLinkedQueue<>();
    
    
    public static final String PERSISTED_SEGMENT_STORE_PREFIX = "segmented-maps-";

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
    private int itemsStoredInEachSegment;

    /*
     * primary memory store
     */
    private Map<K, V> memoryStore = new LinkedHashMap<>();

    /*
     * Segment store
     */
    private SegmentContext<Map<K, V>> newSegment;

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
        STORE = new File(BASE_STORE_NAME + PERSISTED_SEGMENT_STORE_PREFIX + Utilities.replaceSpecialChars(fmt.format(new Date())));
        STORE.getParentFile().deleteOnExit();
        STORE.deleteOnExit();
    }

    // A shutdown hook to remove all created directories and files
    // for all instances of SegmentedOasisMap when the jvm is exiting
    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                for (String dir : directoriesToClean) {
                    Utilities.rmdir(new File(dir));
                }
            }

        }
        );
    }

    /**
     * Default constructor
     */
    public SegmentedHashOasisMap() {
        this(OJBECT_COUNT_IN_MEMORY, SEGMENT_SIZE);
    }

    /**
     * Creates an instance with a custom number of items to be store in memory
     *
     * @param itemsStoredInMemory
     */
    public SegmentedHashOasisMap(int itemsStoredInMemory) {
        this(itemsStoredInMemory, SEGMENT_SIZE);
    }

    /**
     * Creates an instance with a custom number of items to be store in memory
     * and each segment
     *
     * @param itemsStoredInMemory
     * @param segmentSize
     */
    public SegmentedHashOasisMap(int itemsStoredInMemory, int segmentSize) {
        this.itemsStoredInMemory = itemsStoredInMemory;
        this.itemsStoredInEachSegment = segmentSize;

        storagePath = prepareStorage(STORE.getAbsolutePath());
    }

    /**
     * Creates an instance with a custom location for storing the persisted
     * segment.
     *
     * @param diskStorePath The location to store the persisted segments of this
     * map
     */
    public SegmentedHashOasisMap(String diskStorePath) {
        this(OJBECT_COUNT_IN_MEMORY, SEGMENT_SIZE, diskStorePath);
    }

    /**
     * Creates an instance with a custom location for storing the persisted
     * segment.
     *
     * @param diskStorePath The location to store the persisted segments of this
     * map
     */
    public SegmentedHashOasisMap(File diskStorePath) {
        this(OJBECT_COUNT_IN_MEMORY, SEGMENT_SIZE, diskStorePath.getAbsolutePath());
    }

    /**
     * Creates an instance with a custom number of items to be store in memory
     * and each segment, and also a custom location to store the persisted
     * segments
     *
     * @param itemsStoredInMemory
     * @param segmentSize
     * @param diskStorePath
     */
    public SegmentedHashOasisMap(int itemsStoredInMemory, int segmentSize, File diskStorePath) {
        this(itemsStoredInMemory, segmentSize, diskStorePath.getAbsolutePath());
    }

    /**
     * Creates an instance with a custom number of items to be store in memory
     * and each segment, and also a custom location to store the persisted
     * segments
     *
     * @param itemsStoredInMemory
     * @param segmentSize
     * @param diskStorePath
     */
    public SegmentedHashOasisMap(int itemsStoredInMemory, int segmentSize, String diskStorePath) {
        this.itemsStoredInMemory = itemsStoredInMemory;
        this.itemsStoredInEachSegment = segmentSize;
        memoryStore = new LinkedHashMap<>();
        storagePath = prepareStorage(diskStorePath);
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

            // extract the oasis collection direcotory in odrer to create a similar structure in the custom
            // location provided
            String dirStruc = STORE.getAbsolutePath().substring(System.getProperty("java.io.tmpdir").length());;
            instanceStore = new File(customPath + dirStruc + uniqueDir);

            instanceStore.getParentFile().deleteOnExit();
        }

        instanceStore.mkdirs();
        instanceStore.deleteOnExit();
        directoriesToClean.add(instanceStore.getAbsolutePath());

        return instanceStore.getAbsolutePath();
    }

    /**
     * {@link java.util.Map#size() Map.size()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     * instance
     */
    @Override
    public int size() {
        checkIfDestroyed();
        return size;
    }

    /**
     * {@link java.util.Map#isEmpty() Map.isEmpty()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     * instance
     */
    @Override
    public boolean isEmpty() {
        checkIfDestroyed();
        return size == 0;
    }

    /**
     * {@link java.util.Map#containsKey() Map.containsKey()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     * instance
     */
    @Override
    public boolean containsKey(Object key) {
        checkIfDestroyed();
        boolean found = false;
        try {

            if (!isEmpty()) {

                // Is this Key an instance of the key stored in this map?
                K castKey = (K) key;

                // Only check memory store if it is not empty
                // Checking is faster if map is empty because
                // there is no need to calculate the hashcode 
                // of the key first
                if (!memoryStore.isEmpty()) {
                    found = memoryStore.containsKey(key);
                }

                // If not found
                if (!found) {

                    if (newSegment != null && !newSegment.getData().isEmpty()) {
                        found = newSegment.getData().containsKey(key);
                    }

                    // If not found
                    if (!found) {

                        for (String fileName : persistedMapTracker) {

                            SegmentContext<Map<K, V>> segCtx = getSegment(fileName);

                            // Only attemp to search if segmentCtx is not empty, and 
                            // If found stop searching
                            if (!segCtx.getData().isEmpty() && (found = segCtx.getData().containsKey(key))) {
                                break;
                            }
                        }
                    }
                }

            }
        } catch (ClassCastException cce) {

        }

        return found;
    }

    /**
     * {@link java.util.Map#containsValue() Map.containsValue()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     * instance
     */
    @Override
    public boolean containsValue(Object value) {
        checkIfDestroyed();
        boolean found = false;
        try {

            if (!isEmpty()) {

                // Is this value an instance of the value stored in this map?
                V castValue = (V) value;

                if (!memoryStore.isEmpty()) {
                    found = memoryStore.containsValue(value);
                }

                if (!found) {

                    if (newSegment != null && !newSegment.getData().isEmpty()) {
                        found = newSegment.getData().containsValue(value);
                    }

                    if (!found) {

                        for (String fileName : persistedMapTracker) {

                            SegmentContext<Map<K, V>> segCtx = getSegment(fileName);
                            if (!segCtx.getData().isEmpty() && (found = segCtx.getData().containsValue(value))) {
                                break;
                            }
                        }
                    }
                }

            }
        } catch (ClassCastException cce) {

        }

        return found;
    }

    /**
     * {@link java.util.Map#get() Map.get()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     * instance
     */
    @Override
    public V get(Object key) {
        checkIfDestroyed();
        V value = null;

        if (!isEmpty()) {

            if (!memoryStore.isEmpty()) {
                value = memoryStore.get(key);
            }

            if (value == null) {

                if (newSegment != null && !newSegment.getData().isEmpty()) {
                    value = newSegment.getData().get(key);
                }

                if (value == null) {
                    for (String fileName : persistedMapTracker) {

                        SegmentContext<Map<K, V>> segCtx = getSegment(fileName);
                        if (!segCtx.getData().isEmpty() && (value = segCtx.getData().get(key)) != null) {
                            break;
                        }
                    }
                }

            }

        }
        return value;
    }

    /**
     * {@link java.util.Map#put() Map.put()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     * instance
     */
    @Override
    public V put(K key, V value) {
        checkIfDestroyed();
        V previous = null;

        // If all the items added so far are held in
        // memoryStore or this map is empty
        if (size == memoryStore.size()) {

            int originalSize = memoryStore.size();
            // Is the memory store at maximum capacity?
            if (originalSize < itemsStoredInMemory) {
                previous = memoryStore.put(key, value);

                // Increment size if this a new entry
                if (originalSize < memoryStore.size()) {
                    size++;
                }

                return previous;
            }
        }

        // if there is no overflow to disk yet, but memoryStore is full, only newSegment needs to be updated
        if (size == memoryStore.size() || (newSegment != null && size == memoryStore.size() + newSegment.getData().size())) {

            // Although memoryStore is full, it may already
            // contain the key for this entry
            if (memoryStore.containsKey(key)) {
                previous = memoryStore.put(key, value);

            } else {

                // Instantiate if necessary
                if (newSegment == null) {
                    newSegment = new SegmentContext(new LinkedHashMap<>());
                }

                int originalSize = newSegment.getData().size();
                previous = newSegment.getData().put(key, value);

                // Increment size if this a new entry
                if (originalSize < newSegment.getData().size()) {
                    size++;
                }

                // if nesSgement is at max capacity, then write to disk
                if (newSegment.getData().size() == itemsStoredInEachSegment) {
                    String fileName = getNextSegmentName();
                    newSegment.markDirty();
                    writeSegment(newSegment, fileName);
                    persistedMapTracker.add(fileName);
                    newSegment = null;
                }
            }
            return previous;

        }

        boolean enableCacheInternal = false;
        boolean isAdded = false;

        //at this point, there are items stored on disk or in cache; therefore
        // attempt to find and overwrite this entry if it is already existing
        // in any of memoryStore, newSegment or segments stored on disk or in 
        // the cache; otherwise, insert the the new entry the first available
        // slot checking in the order memoryStore, newSegment, cache, disk
        if (memoryStore.containsKey(key)) {
            previous = memoryStore.put(key, value);
            isAdded = true;
        } else if (newSegment != null && newSegment.getData().containsKey(key)) {
            previous = newSegment.getData().put(key, value);
            isAdded = true;
        } else if (!persistedMapTracker.isEmpty()) {

            // enable cache if it is not already enabled
            if (!isCached) {
                enableCache();
                enableCacheInternal = true;
            }

            String fileName;
            SegmentContext<Map<K, V>> segCtx;
            for (int i = 0; i < persistedMapTracker.size(); i++) {
                fileName = persistedMapTracker.get(i);
                segCtx = getSegment(fileName);

                if (!segCtx.getData().isEmpty() && segCtx.getData().containsKey(key)) {
                    previous = segCtx.getData().put(key, value);
                    segCtx.markDirty();
                    writeSegment(segCtx, fileName);
                    isAdded = true;
                    break;
                }
            }

            // disable cache only if it was enabled
            // during this method call
            if (enableCacheInternal) {
                disableCache();
            }
        }

        // If this item has not been added to this map,
        // then add to memoryStore if not already full;
        // otherwise, add to newSegment
        if (!isAdded) {
            if (memoryStore.size() < this.itemsStoredInMemory) {
                memoryStore.put(key, value);
                isAdded = true;
            }

            if (!isAdded) {

                if (newSegment == null) {
                    newSegment = new SegmentContext(new LinkedHashMap<>());
                }

                // if nesSgement is at max capacity, then write to disk
                if (newSegment.getData().size() == itemsStoredInEachSegment) {
                    String fileName = getNextSegmentName();
                    // Mark dirty to write segment
                    newSegment.markDirty();
                    writeSegment(newSegment, fileName);
                    persistedMapTracker.add(fileName);
                    newSegment = new SegmentContext(new LinkedHashMap<>());
                }
                newSegment.getData().put(key, value);

            }

            // Incerement size
            ++size;

        }

        return previous;
    }

    /*
    * creates a unique name for the next segment to persisted on disk
     */
    private String getNextSegmentName() {
        return storagePath + File.separator + "segment-" + (persistedMapTracker.size() + 1);
    }

    /**
     * {@link java.util.Map#remove() Map.remove()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     * instance
     */
    @Override
    public V remove(Object key) {
        checkIfDestroyed();
        V value = null;

        try {

            K castKey = (K) key;

            if (!isEmpty()) {
                boolean isRemoved = false;
                int originalSize = 0;

                if (!memoryStore.isEmpty()) {
                    originalSize = memoryStore.size();
                    value = memoryStore.remove(key);
                }

                if (originalSize > memoryStore.size()) {
                    isRemoved = true;
                } else if (newSegment != null) {

                    originalSize = newSegment.getData().size();
                    value = newSegment.getData().remove(key);

                    if (originalSize > newSegment.getData().size()) {
                        isRemoved = true;
                    }
                }

                if (!isRemoved) {

                    for (String fileName : persistedMapTracker) {

                        SegmentContext<Map<K, V>> segCtx = getSegment(fileName);

                        if (!segCtx.getData().isEmpty()) {
                            originalSize = segCtx.getData().size();
                            value = segCtx.getData().remove(key);

                            if (originalSize > segCtx.getData().size()) {
                                isRemoved = true;
                                segCtx.markDirty();
                                writeSegment(segCtx, fileName);
                                break;
                            }

                        }
                    }

                }

                // If removed successfully, decerement size
                if (isRemoved) {
                    --size;
                }
            }

        } catch (ClassCastException cce) {

        }

        return value;
    }

    /**
     * {@link java.util.Map#putAll() Map.putAll()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     * instance
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> inputMap) {

        checkIfDestroyed();
        boolean enableCacheInternal = false;

        // enable cache internally if not enabled
        // to possible performnace hit if disk I/O
        // needs to be made several times
        if (!isCached) {
            enableCacheInternal = true;
            enableCache();
        }

        inputMap.forEach((k, v) -> {
            put(k, v);
        });

        // If cache was just enabled for this operation, disable it
        if (enableCacheInternal) {
            disableCache();
        }

    }

    /**
     * {@link java.util.Map#clear() Map.clear()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     * instance
     */
    @Override
    public void clear() {
        checkIfDestroyed();
        if (!isEmpty()) {
            memoryStore.clear();

            if (newSegment != null) {
                newSegment.getData().clear();
            }

            for (String f : persistedMapTracker) {
                File file = new File(f);
                try {
                    Files.deleteIfExists(file.toPath());
                } catch (IOException ex) {
                    Logger.getLogger(SegmentedOasisList.class.getName()).log(Level.WARNING, String.format("Unable to delete %s. Will try to delete on prograsm termination ", file.getAbsolutePath()));
                }
            }

            if (cache != null) {
                cache.clear();
            }

            persistedMapTracker.clear();
            size = 0;
        }
    }

    /**
     * {@link java.util.Map#keySet() Map.keySet()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     * instance
     */
    @Override
    public Set<K> keySet() {

        checkIfDestroyed();
        Set<K> keys = new HashSet<>();

        if (!isEmpty()) {
            if (!memoryStore.isEmpty()) {
                keys.addAll(memoryStore.keySet());
            }

            if (newSegment != null && !newSegment.getData().isEmpty()) {
                keys.addAll(newSegment.getData().keySet());
            }

            persistedMapTracker.stream().forEach((fileName) -> {
                SegmentContext<Map<K, V>> segCtx = getSegment(fileName);
                if (!segCtx.getData().isEmpty()) {
                    keys.addAll(segCtx.getData().keySet());
                }
            });
        }
        return keys;
    }

    /**
     * {@link java.util.Map#values() Map.values()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     * instance
     */
    @Override
    public Collection<V> values() {

        checkIfDestroyed();
        Collection<V> values = new ArrayList<>();

        if (!isEmpty()) {
            if (!memoryStore.isEmpty()) {
                values.addAll(memoryStore.values());
            }

            if (newSegment != null && !newSegment.getData().isEmpty()) {
                values.addAll(newSegment.getData().values());
            }

            persistedMapTracker.stream().forEach((fileName) -> {
                SegmentContext<Map<K, V>> segCtx = getSegment(fileName);
                if (!segCtx.getData().isEmpty()) {
                    values.addAll(segCtx.getData().values());
                }
            });

        }
        return values;
    }

    /**
     * {@link java.util.Map#entrySet() Map.entrySet()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     * instance
     */
    @Override
    public Set<Entry<K, V>> entrySet() {

        checkIfDestroyed();
        Set<Entry<K, V>> entries = new HashSet<>();

        if (!isEmpty()) {
            entries.addAll(memoryStore.entrySet());

            if (newSegment != null && !newSegment.getData().isEmpty()) {
                entries.addAll(newSegment.getData().entrySet());
            }

            persistedMapTracker.stream().forEach((fileName) -> {
                SegmentContext<Map<K, V>> segCtx = getSegment(fileName);
                if (!segCtx.getData().isEmpty()) {
                    entries.addAll(segCtx.getData().entrySet());
                }
            });

        }

        return entries;
    }

    /**
     * {@link org.kush.oasis.SegmentedOasisCollection#destroy() SegmentedOasisCollection.destroy()}
     */
    @Override
    public void destroy() {

        // don't do anything destroy has been called on this instance already
        if (!isDestroyed) {
            clear();
            try {
                finalize();
            } catch (Throwable ex) {
                Logger.getLogger(SegmentedHashOasisMap.class.getName()).log(Level.SEVERE, null, ex);
            }
            isDestroyed = true;

        }
    }

    private void checkIfDestroyed() {
        if (isDestroyed) {
            throw new IllegalStateException("Destroy has already been called on this instance");
        }
    }

    /*
     * Custom serialization method used when writing an instance of this map
     */
    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {

        stream.writeInt(itemsStoredInMemory);
        stream.writeInt(itemsStoredInEachSegment);
        stream.writeInt(size);
        stream.writeUTF(storagePath);
        stream.writeObject(memoryStore);
        stream.writeObject(newSegment);
        stream.writeObject(persistedMapTracker);

        for (String fileName : persistedMapTracker) {
            stream.writeObject(getSegment(fileName).getData());
        }

    }

    /*
     * Custom serialization method used when reading an instance of this map
     */
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {

        itemsStoredInMemory = stream.readInt();
        itemsStoredInEachSegment = stream.readInt();
        size = stream.readInt();
        storagePath = stream.readUTF();
        memoryStore = (Map<K, V>) stream.readObject();
        newSegment = (SegmentContext) stream.readObject();
        persistedMapTracker = (List<String>) stream.readObject();

        // Ensure directory structure is recreated in case 
        // it had been removed
        File file = new File(storagePath);
        file.mkdirs();

        SegmentContext segCtx;
        for (String fileName : persistedMapTracker) {
            segCtx = new SegmentContext((Map<K, V>) stream.readObject());
            // mark segment dirty; otherwise, it won't be written to disk
            segCtx.markDirty();
            writeSegment(segCtx, fileName);
        }

    }

    /*
     * Retrieve the segment assoiated with the fileName provided
     */
    private SegmentContext getSegment(String fileName) {

        SegmentContext segmentCtx = null;
        if (isCached) {
            segmentCtx = getSegmentFromCache(fileName);
        } else {
            segmentCtx = getSegmentFromStore(fileName);
        }

        return segmentCtx;
    }

    /*
     * Retrieve the segment assoiated with the fileName provided from the cache.
     */
    private SegmentContext getSegmentFromCache(String fileName) {

        SegmentContext segCtx = null;

        if (isCached) {
            segCtx = cache.get(fileName);

            if (segCtx == null) {
                // If not found, then load from the disk as segments
                // are loaded on demand 
                segCtx = getSegmentFromStore(fileName);
                cache.put(fileName, segCtx);
            }

        }

        return segCtx;
    }

    /*
     * Retrieve the segment assoiated with the fileName provided from the disk.
     */
    private SegmentContext getSegmentFromStore(String fileName) {

        Map<K, V> read = null;

        try (FileInputStream fileStrm = new FileInputStream(new File(fileName));FSTObjectInput in = new FSTObjectInput(fileStrm)) {
            read = (Map<K, V>) in.readObject(Map.class);

        } catch (Exception ex) {
            Logger.getLogger(SegmentedHashOasisMap.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new SegmentContext(read);
    }

    /*
     * Writes the segment to disk usingw the fileName provided
     */
    private void writeSegment(SegmentContext segCtx, String fileName) {

        if (segCtx.isDirty()) {
            if (isCached) {
                cache.put(fileName, segCtx);
            } else {

                try (FileOutputStream fileStrm = new FileOutputStream(new File(fileName)); FSTObjectOutput out = new FSTObjectOutput(fileStrm)) {
                    out.writeObject(segCtx.getData(), Map.class);

                } catch (Exception ex) {
                    Logger.getLogger(SegmentedHashOasisMap.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    /**
     * {@link org.kush.oasis.SegmentedOasisCollection#enableCache() SegmentedOasisCollection.enableCache()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     * instance
     */
    @Override
    public void enableCache() {
        checkIfDestroyed();
        if (!isCached) {
            isCached = true;
            initCache();
        }

    }

    private void initCache() {
        cache = new HashMap<>();
    }

    /**
     * {@link org.kush.oasis.SegmentedOasisCollection#disableCache() SegmentedOasisCollection.disableCache()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     * instance
     */
    @Override
    public void disableCache() {
        checkIfDestroyed();
        if (isCached && !isEmpty()) {
            // attemp compacting to make this map more efficient
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
     * {@link org.kush.oasis.SegmentedOasisCollection#compact() SegmentedOasisCollection.compact()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     * instance
     */
    @Override
    public void compact() {

        checkIfDestroyed();
        if (!isEmpty()) {
            if (!isCached) {
                initCache();
                persistedMapTracker.forEach(it -> {
                    cache.put(it, getSegment(it));
                });
            }

            try {
                // In most cases persistedMapTracker and cache should have the same size, however,
                // if the cache is partly loaded as it is loaded on demand, then their sizes
                // will be different; therefore, skip compacting to avoid expensive I/O
                if (!persistedMapTracker.isEmpty() && persistedMapTracker.size() == cache.size()) {

                    compact(new SegmentContext(memoryStore), cache.get(persistedMapTracker.get(0)), itemsStoredInMemory);

                    for (int i = 1; i < persistedMapTracker.size(); i++) {
                        compact(cache.get(persistedMapTracker.get(i - 1)), cache.get(persistedMapTracker.get(i)), itemsStoredInEachSegment);
                    }

                    if (newSegment != null && !newSegment.getData().isEmpty()) {
                        compact(cache.get(persistedMapTracker.get(persistedMapTracker.size() - 1)), newSegment, itemsStoredInEachSegment);
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

    }

    /**
     * Moves item from src map to dest map.
     *
     * @param dest
     * @param src
     * @param destMaxSize
     */
    private void compact(SegmentContext<Map<K, V>> dest, SegmentContext<Map<K, V>> src, int destMaxSize) {

        if (dest.getData().size() < destMaxSize && !src.getData().isEmpty()) {

            if (destMaxSize - dest.getData().size() >= src.getData().size()) {
                dest.getData().putAll(src.getData());
                dest.markDirty();
                src.getData().clear();
            } else {
                int toExtract = destMaxSize - dest.getData().size();
                Iterator<Entry<K, V>> itr = src.getData().entrySet().iterator();
                Entry<K, V> temp;

                for (int i = 0; itr.hasNext() && i < toExtract; i++) {
                    temp = itr.next();
                    dest.getData().put(temp.getKey(), temp.getValue());
                    // remove from src
                    itr.remove();
                }

                dest.markDirty();
                src.markDirty();
            }
        }

    }

    private void removeEmptySegments() {

        Iterator<String> itr = persistedMapTracker.iterator();
        String item;
        while (itr.hasNext()) {
            item = itr.next();

            SegmentContext<Map<K, V>> cacheItem = cache.get(item);

            if (cacheItem.getData().isEmpty()) {
                cache.remove(item);
                itr.remove();
                File file = new File(item);
                try {
                    Files.deleteIfExists(file.toPath());
                } catch (IOException ex) {
                    file.deleteOnExit();
                    Logger.getLogger(SegmentedHashOasisMap.class.getName()).log(Level.WARNING, String.format("Unable to delete %s. Will try to delete on prograsm termination ", file.getAbsolutePath()));
                }
            }
        }

    }

    @Override
    protected void finalize() throws Throwable {
        cleanup();
        super.finalize();

    }

    private void cleanup() {
        Utilities.rmdir(new File(storagePath));
    }

    /**
     * {@link org.kush.oasis.SegmentedOasisCollection#isCacheEnabled() SegmentedOasisCollection.isCacheEnabled()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     * instance
     */
    @Override
    public boolean isCacheEnabled() {
        checkIfDestroyed();
        return isCached;
    }

    /**
     * {@link org.kush.oasis.SegmentedOasisCollection#instanceStore() SegmentedOasisCollection.instanceStore()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     * instance
     */
    @Override
    public String instanceStore() {
        checkIfDestroyed();
        return storagePath;
    }

    /**
     * {@link org.kush.oasis.SegmentedOasisCollection#persistedSegmentCount() SegmentedOasisCollection.persistedSegmentCount()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     * instance
     */
    @Override
    public int persistedSegmentCount() {
        checkIfDestroyed();
        return persistedMapTracker.size();
    }
}
