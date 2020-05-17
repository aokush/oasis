/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kush.oasis.segmented;

import java.io.Serializable;
import java.util.Objects;

/**
 * An instance of this class wraps an instance of OasisCollection to provide
 * additional management features when needed
 *
 * @author AKuseju
 * @param <K>
 */
class SegmentContext<K> implements Serializable {

    private K data;
    private boolean isDirty;

    SegmentContext() {
    }

    SegmentContext(K data) {
        this.data = data;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void markDirty() {
        isDirty = true;
    }

    public K getData() {
        return data;
    }

    public void setData(K data) {
        this.data = data;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SegmentContext<K> other = (SegmentContext) obj;
        if (!Objects.equals(this.data, other.data)) {
            return false;
        }
        return true;
    }
}
