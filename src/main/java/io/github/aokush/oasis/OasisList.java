/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.aokush.oasis;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author AKuseju
 * @param <E>
 */
public interface OasisList<E extends Serializable> extends List<E>, OasisCollection {    
    @Override
    Iterator<E> iterator();
    
    @Override
    ListIterator<E> listIterator();
}
