/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kush.oasis;

import java.io.Serializable;
import java.util.Map;

/**
 * OasisMap interface
 * 
 * @author AKuseju
 * @param <K>
 * @param <V>
 */
public interface OasisMap<K extends Serializable, V extends Serializable> extends Map<K, V>, OasisCollection {
            
}
