package org.kush.oasis.segmented;

/**
 * Generates a value that determines the segment where an entry should be stored
 *
 * @author AKuseju
 */
public interface SegmentParitioner {

    String getSegmentHash(Object item);

}
