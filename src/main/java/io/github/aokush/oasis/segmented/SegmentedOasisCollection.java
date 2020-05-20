/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.aokush.oasis.segmented;

import io.github.aokush.oasis.OasisCollection;
import java.io.File;
import java.lang.management.ManagementFactory;

/**
 *
 * @author AKuseju
 */
public interface SegmentedOasisCollection extends OasisCollection {

    /**
     * The default number of objects to be stored in memory
     */
    public static final int OJBECT_COUNT_IN_MEMORY = 200000;

    /**
     * The default number of objects to be stored in each segment
     */
    public static final int SEGMENT_SIZE = 50000;

    /**
     * The default location where all the persisted segments with the associated
     * java process are stored.
     * 
     * The store is created every time a new process is created and deleted when the
     * process exits
     */
    public static final String BASE_DIRECTORY_NAME = "oasis-collection";

    public static final String PROCESS_ID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

    public static final String BASE_STORE_NAME = System.getProperty("java.io.tmpdir") + File.separator
            + BASE_DIRECTORY_NAME + File.separator + PROCESS_ID + File.separator;

    /**
     * Enables the cache associated with this map
     */
    void enableCache();

    /**
     * Disables the cache associated with this map
     */
    void disableCache();

    /**
     * 
     * 
     * @return A boolean that provides the status of the associated cache
     */
    boolean isCacheEnabled();

    /**
     * Compact this underlying data structure to reduce number of persisted segments
     * 
     */
    void compact();


     /**
     * Compact this underlying data structure to reduce number of persisted segments
     * moving items on disk to memory as long as max item stored in memory is not exceeded.
     * 
     */
    void compactFast();

    /**
     * The store location
     * 
     * @return The path to the location where the segments of this map are stored
     */
    String instanceStore();

    /**
     * The number of segments persisted.
     * 
     * If this map is cached, it just the value returned is expected number of
     * segments to be persisted when cache is disabled
     * 
     * @return the number of segments persisted
     */
    int persistedSegmentCount();

    /**
     * Removes the all the internal state associated with an Oasis collection.
     */
    void destroy();

}
