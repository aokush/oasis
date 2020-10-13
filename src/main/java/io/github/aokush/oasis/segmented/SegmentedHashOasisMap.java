package io.github.aokush.oasis.segmented;

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

import io.github.aokush.oasis.OasisMap;
import io.github.aokush.oasis.util.Utilities;
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
public class SegmentedHashOasisMap<K extends Serializable, V extends Serializable>
        implements Serializable, OasisMap<K, V>, SegmentedOasisCollection {

    private static final Logger LOGGER = Logger.getLogger(SegmentedHashOasisMap.class.getName());

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
    private List<String> persistedSegTracker = new ArrayList<>();

    /*
     * A numerical id appended to sgement file name to create a unique file
     */
    private int numberId;

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
    // for all instances of SegmentedOasisMap when the jvm is exiting
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
     * Default constructor
     */
    public SegmentedHashOasisMap() {
        this(OJBECT_COUNT_IN_MEMORY, SEGMENT_SIZE);
    }

    /**
     * Creates an instance with a custom number of items to be store in memory
     *
     * @param itemsStoredInMemory Max number of items to store in memory
     */
    public SegmentedHashOasisMap(int itemsStoredInMemory) {
        this(itemsStoredInMemory, SEGMENT_SIZE);
    }

    /**
     * Creates an instance with a custom number of items to be store in memory and
     * each segment
     *
     * @param itemsStoredInMemory Max number of items to store in memory
     * @param segmentSize         Max number of items to store in a segment
     */
    public SegmentedHashOasisMap(int itemsStoredInMemory, int segmentSize) {
        this.itemsStoredInMemory = itemsStoredInMemory;
        this.itemsStoredInEachSegment = segmentSize;

        storagePath = prepareStorage(STORE.getAbsolutePath());
    }

    /**
     * Creates an instance with a custom location for storing the persisted segment.
     *
     * @param diskStorePath The location to store the persisted segments of this map
     */
    public SegmentedHashOasisMap(String diskStorePath) {
        this(OJBECT_COUNT_IN_MEMORY, SEGMENT_SIZE, diskStorePath);
    }

    /**
     * Creates an instance with a custom location for storing the persisted segment.
     *
     * @param diskStorePath The location to store the persisted segments of this map
     */
    public SegmentedHashOasisMap(File diskStorePath) {
        this(OJBECT_COUNT_IN_MEMORY, SEGMENT_SIZE, diskStorePath.getAbsolutePath());
    }

    /**
     * Creates an instance with a custom number of items to be store in memory and
     * each segment, and also a custom location to store the persisted segments
     *
     * @param itemsStoredInMemory Max number of items to store in memory
     * @param segmentSize         Max number of items to store in a segment
     * @param diskStorePath       The location to store the persisted segments of
     *                            this map
     */
    public SegmentedHashOasisMap(int itemsStoredInMemory, int segmentSize, File diskStorePath) {
        this(itemsStoredInMemory, segmentSize, diskStorePath.getAbsolutePath());
    }

    /**
     * Creates an instance with a custom number of items to be store in memory and
     * each segment, and also a custom location to store the persisted segments
     *
     * @param itemsStoredInMemory Max number of items to store in memory
     * @param segmentSize         Max number of items to store in a segment
     * @param diskStorePath       The location to store the persisted segments of
     *                            this map
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
     * {@link java.util.Map#size() Map.size()}
     * 
     * @return The number of items in this map
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
     * {@link java.util.Map#isEmpty() Map.isEmpty()}
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
     * {@link java.util.Map#containsKey(Object) Map.containsKey(Object)}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
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

                        for (String fileName : persistedSegTracker) {

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
     * {@link java.util.Map#containsValue(Object) Map.containsValue(Object)}
     * 
     * @param value item to find in the values
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
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

                        for (String fileName : persistedSegTracker) {

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
     * {@link java.util.Map#get(Object) Map.get(Object)}
     * 
     * @param key find the value the key points to
     * 
     * @return V the value the key param points to or null if key does not exist
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
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
                    for (String fileName : persistedSegTracker) {

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
     * {@link java.util.Map#put(Object, Object) Map.put(K, V))}
     * 
     * @param key   the key for the item to be added
     * @param value the value for the item to be added
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
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

        // if there is no overflow to disk yet, but memoryStore is full, only newSegment
        // needs to be updated
        if (size == memoryStore.size()
                || (newSegment != null && size == memoryStore.size() + newSegment.getData().size())) {

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
                    persistedSegTracker.add(fileName);
                    newSegment = null;
                }
            }
            return previous;

        }

        boolean enableCacheInternal = false;
        boolean isAdded = false;

        // at this point, there are items stored on disk or in cache; therefore
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
        } else if (!persistedSegTracker.isEmpty()) {

            // enable cache if it is not already enabled
            if (!isCached) {
                enableCache();
                enableCacheInternal = true;
            }

            String fileName;
            SegmentContext<Map<K, V>> segCtx;
            for (int i = 0; i < persistedSegTracker.size(); i++) {
                fileName = persistedSegTracker.get(i);
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
                    persistedSegTracker.add(fileName);
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
        return storagePath + File.separator + "segment-" + numberId++;
    }

    /**
     * {@link java.util.Map#remove(Object) Map.remove(Object)}
     * 
     * @param key the key for the entry to remove
     * 
     * @return V The value the key points to or null if key does not exist in the
     *         map
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
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

                    for (String fileName : persistedSegTracker) {

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
     * {@link java.util.Map#putAll(Map) Map.putAll(Map)}
     * 
     * @param inputMap the map to add
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
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
     *                               instance
     */
    @Override
    public void clear() {
        checkIfDestroyed();
        if (!isEmpty()) {
            memoryStore.clear();

            if (newSegment != null) {
                newSegment.getData().clear();
            }

            for (String f : persistedSegTracker) {
                File file = new File(f);
                try {
                    Files.deleteIfExists(file.toPath());
                } catch (IOException ex) {
                    Logger.getLogger(SegmentedOasisList.class.getName()).log(Level.WARNING,
                            String.format("Unable to delete %s. Will try to delete on prograsm termination ",
                                    file.getAbsolutePath()));
                }
            }

            if (cache != null) {
                cache.clear();
            }

            persistedSegTracker.clear();
            size = 0;
        }
    }

    /**
     * {@link java.util.Map#keySet() Map.keySet()}
     * 
     * @return A set of all the keys in this map
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
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

            persistedSegTracker.stream().forEach((fileName) -> {
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
     * @return a collection of all the values in this map.
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
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

            persistedSegTracker.stream().forEach((fileName) -> {
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
     * @return A set of of the the entries in this map
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
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

            persistedSegTracker.stream().forEach((fileName) -> {
                SegmentContext<Map<K, V>> segCtx = getSegment(fileName);
                if (!segCtx.getData().isEmpty()) {
                    entries.addAll(segCtx.getData().entrySet());
                }
            });

        }

        return entries;
    }

    /**
     * {@link io.github.aokush.oasis.segmented.SegmentedOasisCollection#destroy()
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
                LOGGER.log(Level.SEVERE, null, ex);
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
        stream.writeObject(persistedSegTracker);

        for (String fileName : persistedSegTracker) {
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
        persistedSegTracker = (List<String>) stream.readObject();

        // Ensure directory structure is recreated in case
        // it had been removed
        File file = new File(storagePath);
        file.mkdirs();

        SegmentContext segCtx;
        for (String fileName : persistedSegTracker) {
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

        try (FileInputStream fileStrm = new FileInputStream(new File(fileName));
                FSTObjectInput in = new FSTObjectInput(fileStrm)) {
            read = (Map<K, V>) in.readObject(Map.class);

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
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

                try (FileOutputStream fileStrm = new FileOutputStream(new File(fileName));
                        FSTObjectOutput out = new FSTObjectOutput(fileStrm)) {
                    out.writeObject(segCtx.getData(), Map.class);

                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    /**
     * {@link io.github.aokush.oasis.segmented.SegmentedOasisCollection#enableCache()
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
            initCache();
        }

    }

    private void initCache() {
        cache = new HashMap<>();
    }

    /**
     * {@link io.github.aokush.oasis.segmented.SegmentedOasisCollection#disableCache()
     * SegmentedOasisCollection.disableCache()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
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
     * {@link io.github.aokush.oasis.segmented.SegmentedOasisCollection#compact()
     * SegmentedOasisCollection.compact()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public void compact() {

        checkIfDestroyed();
        if (!isEmpty()) {
            if (!isCached) {
                initCache();
                persistedSegTracker.forEach(it -> {
                    cache.put(it, getSegment(it));
                });
            }

            try {
                // In most cases persistedMapTracker and cache should have the same size,
                // however,
                // if the cache is partly loaded as it is loaded on demand, then their sizes
                // will be different; therefore, skip compacting to avoid expensive I/O
                if (!persistedSegTracker.isEmpty() && persistedSegTracker.size() == cache.size()) {

                    compact(new SegmentContext(memoryStore), cache.get(persistedSegTracker.get(0)),
                            itemsStoredInMemory);

                    for (int i = 1; i < persistedSegTracker.size(); i++) {
                        compact(cache.get(persistedSegTracker.get(i - 1)), cache.get(persistedSegTracker.get(i)),
                                itemsStoredInEachSegment);
                    }

                    if (newSegment != null && !newSegment.getData().isEmpty()) {
                        compact(cache.get(persistedSegTracker.get(persistedSegTracker.size() - 1)), newSegment,
                                itemsStoredInEachSegment);
                    }

                    removeEmptySegments();

                    if (!isCached) {
                        persistedSegTracker.forEach(it -> {
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
     * {@link io.github.aokush.oasis.segmented.SegmentedOasisCollection#compactFast()
     * SegmentedOasisCollection.compactFast()}
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public void compactFast() {

        checkIfDestroyed();
        if (!isEmpty() && !isCached) {

            List<String> segmentToDel = new ArrayList<>();
            int segTracker = 0;
            while (memoryStore.size() < this.itemsStoredInMemory && segTracker < persistedSegTracker.size()) {

                SegmentContext<Map<K, V>> sgtCtx = getSegment(persistedSegTracker.get(segTracker));
                if (!sgtCtx.getData().isEmpty()) {

                    int itemsToMove = this.itemsStoredInMemory - memoryStore.size();

                    // if the memory store has enough room for the entire data of at
                    // least one segment
                    if (itemsToMove >= sgtCtx.getData().size()) {
                        memoryStore.putAll(sgtCtx.getData());
                        sgtCtx.getData().clear();
                        segmentToDel.add(persistedSegTracker.get(segTracker));

                    } else {

                        Iterator<Entry<K, V>> itr = sgtCtx.getData().entrySet().iterator();
                        Entry<K, V> temp;

                        for (int i = 0; itr.hasNext() && i < itemsToMove; i++) {
                            temp = itr.next();
                            memoryStore.put(temp.getKey(), temp.getValue());
                            // remove from src
                            itr.remove();
                        }

                        sgtCtx.markDirty();
                        writeSegment(sgtCtx, persistedSegTracker.get(segTracker));
                    }

                }

                segTracker++;

            }

            persistedSegTracker.removeAll(segmentToDel);
            Utilities.deleteSegFiles(segmentToDel);

             // new segmenyt may contain data that may need to be compacted as well
             if (memoryStore.size() < this.itemsStoredInMemory && persistedSegTracker.isEmpty() && newSegment != null) {
    
                if (!newSegment.getData().isEmpty()) {
                    int itemsToMove = this.itemsStoredInMemory - memoryStore.size();

                    if (itemsToMove >= newSegment.getData().size()) {
                        memoryStore.putAll(newSegment.getData());
                        newSegment.getData().clear();
                    } else {

                        Iterator<Entry<K, V>> itr = newSegment.getData().entrySet().iterator();
                        Entry<K, V> temp;

                        for (int i = 0; itr.hasNext() && i < itemsToMove; i++) {
                            temp = itr.next();
                            memoryStore.put(temp.getKey(), temp.getValue());
                            // remove from src
                            itr.remove();
                        }
                    }
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

        Iterator<String> itr = persistedSegTracker.iterator();
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
                    LOGGER.log(Level.WARNING,
                            String.format("Unable to delete %s. Will try to delete on prograsm termination ",
                                    file.getAbsolutePath()));
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
     * {@link io.github.aokush.oasis.segmented.SegmentedOasisCollection#isCacheEnabled()
     * SegmentedOasisCollection.isCacheEnabled()}
     * 
     * @return boolean
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
     * {@link io.github.aokush.oasis.segmented.SegmentedOasisCollection#instanceStore()
     * SegmentedOasisCollection.instanceStore()}
     * 
     * @return A string representation of the backing disk store
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
     * {@link io.github.aokush.oasis.segmented.SegmentedOasisCollection#persistedSegmentCount()
     * SegmentedOasisCollection.persistedSegmentCount()}
     * 
     * @return A count of the number of segments saved to disk store.
     *
     * @throws IllegalStateException If destroy has already been called on this
     *                               instance
     */
    @Override
    public int persistedSegmentCount() {
        checkIfDestroyed();
        return persistedSegTracker.size();
    }
}
