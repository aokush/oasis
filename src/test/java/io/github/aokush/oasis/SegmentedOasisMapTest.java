/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.aokush.oasis;

import io.github.aokush.oasis.segmented.SegmentedHashOasisMap;
import io.github.aokush.oasis.util.Utilities;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author AKuseju
 */
public class SegmentedOasisMapTest {
    
    private static List<File> directoriesToDelete = new ArrayList<>();
    
    @AfterClass
    public static void testClassCleanup() {
        
        directoriesToDelete.stream().forEach(f -> {
            if (f.exists()) {
                Utilities.rmdir(f);
            }
        });
        
        
    }

    /**
     * Test of isCacheEnabled method, of class SegmentedOasisMap.
     */
    @Test
    public void testIsCacheEnabled_default() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();

        instance.put(1, 2);
        instance.put(2, 3);
        instance.put(3, 2);
        instance.put(3, 2);

        assertFalse(instance.isCacheEnabled());
    }

    /**
     * Test of isCacheEnabled method, of class SegmentedOasisMap.
     */
    @Test
    public void testIsCacheEnabled_enabled() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();

        instance.enableCache();
        instance.put(1, 2);
        instance.put(2, 3);
        instance.put(3, 2);
        instance.put(3, 2);

        assertTrue(instance.isCacheEnabled());
    }

    /**
     * Test of isCacheEnabled method, of class SegmentedOasisMap.
     */
    @Test
    public void testIsCacheEnabled_toggled() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();

        instance.enableCache();
        instance.put(1, 2);
        instance.put(2, 3);
        instance.put(3, 2);
        instance.put(3, 2);
        instance.disableCache();

        assertFalse(instance.isCacheEnabled());
    }

    /**
     * Test of size method, of class SegmentedOasisMap.
     */
    @Test
    public void testSize() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        long expResult = 3L;

        instance.put(1, 2);
        instance.put(2, 3);
        instance.put(3, 2);
        instance.put(3, 2);

        long result = instance.size();
        assertEquals(expResult, result);
    }

    /**
     * Test of size method, of class SegmentedOasisMap.
     */
    @Test
    public void testSize_Overflow() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(3, 2);
        long expResult = 3L;

        instance.put(1, 2);
        instance.put(2, 3);
        instance.put(3, 2);
        instance.put(3, 2);

        long result = instance.size();
        assertEquals(expResult, result);

    }

    /**
     * Test of isEmpty method, of class SegmentedOasisMap.
     */
    @Test
    public void testIsEmpty_Empty() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 2);
        instance.put(2, 3);
        instance.clear();

        boolean expResult = true;
        boolean result = instance.isEmpty();
        assertEquals(expResult, result);
    }

    /**
     * Test of isEmpty method, of class SegmentedOasisMap.
     */
    @Test
    public void testIsEmpty_Empty_Overflow() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 2);
        instance.put(2, 3);
        instance.put(3, 3);
        instance.put(4, 3);
        instance.put(5, 3);
        instance.clear();

        boolean expResult = true;
        boolean result = instance.isEmpty();
        assertEquals(expResult, result);
    }

    /**
     * Test of isEmpty method, of class SegmentedOasisMap.
     */
    @Test
    public void testIsEmpty_Not_Empty() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 2);
        instance.put(2, 3);

        boolean expResult = false;
        boolean result = instance.isEmpty();
        assertEquals(expResult, result);
    }

    /**
     * Test of isEmpty method, of class SegmentedOasisMap.
     */
    @Test
    public void testIsEmpty_Not_Empty_Overflow() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 2);
        instance.put(2, 3);
        instance.put(3, 3);
        instance.put(4, 3);
        instance.put(5, 3);

        boolean expResult = false;
        boolean result = instance.isEmpty();
        assertEquals(expResult, result);
    }

    /**
     * Test of containsKey method, of class SegmentedOasisMap.
     */
    @Test
    public void testContainsKey_Contain() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 2);
        instance.put(2, 3);

        boolean expResult = true;
        boolean result = instance.containsKey(1);
        assertEquals(expResult, result);
    }

    /**
     * Test of containsKey method, of class SegmentedOasisMap.
     */
    @Test
    public void testContainsKey_Contain_Overflow() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 2);
        instance.put(2, 3);
        instance.put(3, 3);
        instance.put(4, 3);
        instance.put(5, 3);

        boolean expResult = true;
        boolean result = instance.containsKey(4);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of containsKey method, of class SegmentedOasisMap.
     */
    @Test
    public void testContainsKey_Contain_Overflow_Boundary() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 2);
        instance.put(2, 3);
        instance.put(3, 3);
        instance.put(4, 3);
        instance.put(5, 3);

        boolean expResult = true;
        boolean result = instance.containsKey(5);
        assertEquals(expResult, result);
    }

    /**
     * Test of containsKey method, of class SegmentedOasisMap.
     */
    @Test
    public void testContainsKey_Not_Contain() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 2);
        instance.put(2, 3);

        boolean expResult = false;
        boolean result = instance.containsKey(3);
        assertEquals(expResult, result);
    }

    /**
     * Test of containsKey method, of class SegmentedOasisMap.
     */
    @Test
    public void testContainsKey_Not_Contain_Overflow() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 2);
        instance.put(2, 3);
        instance.put(3, 3);
        instance.put(4, 3);
        instance.put(5, 3);

        boolean expResult = false;
        boolean result = instance.containsKey(6);
        assertEquals(expResult, result);
    }

    /**
     * Test of get method, of class SegmentedOasisMap.
     */
    @Test
    public void testGet_Contains() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 2);
        instance.put(2, 3);

        Integer expResult = 3;
        Integer result = instance.get(2);
        assertEquals(expResult, result);
    }

    /**
     * Test of get method, of class SegmentedOasisMap.
     */
    @Test
    public void testGet_Null() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 2);
        instance.put(2, 3);

        assertNull(instance.get(3));
    }

    /**
     * Test of get method, of class SegmentedOasisMap.
     */
    @Test
    public void testGet_Contains_Overflow() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 2);
        instance.put(2, 4);
        instance.put(3, 5);
        instance.put(4, 3);
        instance.put(5, 7);

        Integer expResult = 3;
        Integer result = instance.get(4);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of get method, of class SegmentedOasisMap.
     */
    @Test
    public void testGet_Contains_Overflow_Boundary() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 2);
        instance.put(2, 4);
        instance.put(3, 5);
        instance.put(4, 3);
        instance.put(5, 7);

        Integer expResult = 7;
        Integer result = instance.get(5);
        assertEquals(expResult, result);
    }

    /**
     * Test of put method, of class SegmentedOasisMap.
     */
    @Test
    public void testPut() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 2);

        Integer expResult = 2;
        Integer result = instance.get(1);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of put method, of class SegmentedOasisMap.
     */
    @Test
    public void testPut_Duplicate() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 2);
        instance.put(2, 3);
        instance.put(3, 3);
        instance.put(4, 3);
        instance.put(5, 3);
        
        instance.put(4, 6);

        Integer expResult = 6;
        Integer result = instance.get(4);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of put method, of class SegmentedOasisMap.
     */
    @Test
    public void testPut_Duplicate_Size() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 2);
        instance.put(2, 3);
        instance.put(3, 3);
        instance.put(4, 3);
        instance.put(5, 3);
        
        instance.put(4, 6);

        Integer expResult = 5;
        Integer result = instance.size();
        assertEquals(expResult, result);
    }
    
    
    /**
     * Test of put method, of class SegmentedOasisMap.
     */
    @Test
    public void testPut_Null_Key() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 2);
        instance.put(2, 3);
        instance.put(3, 3);
        instance.put(null, 4);
        instance.put(5, 3);
        

        Integer expResult = 4;
        Integer result = instance.get(null);
        assertEquals(expResult, result);
    }
    
    
    /**
     * Test of put method, of class SegmentedOasisMap.
     */
    @Test
    public void testPut_Null_Key_Duplicate() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 2);
        instance.put(2, 3);
        instance.put(3, 3);
        instance.put(null, 4);
        instance.put(5, 3);
        
        instance.put(null, 6);

        Integer expResult = 6;
        Integer result = instance.get(null);
        assertEquals(expResult, result);
    }
    
    
    /**
     * Test of put method, of class SegmentedOasisMap.
     */
    @Test
    public void testPut_Null_Key_Duplicate_Size() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 2);
        instance.put(2, 3);
        instance.put(3, 3);
        instance.put(null, 4);
        instance.put(5, 3);
        
        instance.put(null, 6);

        Integer expResult = 5;
        Integer result = instance.size();
        assertEquals(expResult, result);
    }

    /**
     * Test of put method, of class SegmentedOasisMap.
     */
    @Test
    public void testPut_Overflow() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 2);
        instance.put(2, 3);
        instance.put(3, 3);
        instance.put(4, 3);
        instance.put(5, 3);

        Integer expResult = 3;
        Integer result = instance.get(5);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of put method, of class SegmentedOasisMap.
     */
    @Test
    public void testPut_Overflow_Duplicate() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 2);
        instance.put(2, 3);
        instance.put(3, 3);
        instance.put(4, 3);
        instance.put(5, 3);
        
        instance.put(4, 6);

        Integer expResult = 6;
        Integer result = instance.get(4);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of put method, of class SegmentedOasisMap.
     */
    @Test
    public void testPut_Size_Overflow_Duplicate() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 2);
        instance.put(2, 3);
        instance.put(3, 3);
        instance.put(4, 3);
        instance.put(5, 3);
        
        instance.put(4, 6);

        Integer expResult = 5;
        Integer result = instance.size();
        assertEquals(expResult, result);
    }

    /**
     * Test of remove method, of class SegmentedOasisMap.
     */
    @Test
    public void testRemove_Contains() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 2);
        instance.put(2, 3);

        Integer expResult = 3;
        Integer result = instance.remove(2);
        assertEquals(expResult, result);
    }

    /**
     * Test of remove method, of class SegmentedOasisMap.
     */
    @Test
    public void testRemove_Contains_Overflow() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 2);
        instance.put(2, 3);
        instance.put(3, 3);
        instance.put(4, 3);
        instance.put(5, 3);

        Integer expResult = 3;
        Integer result = instance.get(5);
        assertEquals(expResult, result);
        assertEquals(expResult, result);
    }

    /**
     * Test of remove method, of class SegmentedOasisMap.
     */
    @Test
    public void testRemove_Not_Contain() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 2);
        instance.put(2, 3);

        assertNull(instance.remove(3));
    }

    /**
     * Test of remove method, of class SegmentedOasisMap.
     */
    @Test
    public void testRemove_Not_Contain_Overflow() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 2);
        instance.put(2, 3);
        instance.put(3, 3);
        instance.put(4, 3);
        instance.put(5, 3);

        assertNull(instance.remove(7));
    }

    /**
     * Test of remove method, of class SegmentedOasisMap.
     */
    @Test
    public void testRemove_Contains_Size() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 2);
        instance.put(2, 3);

        long expResult = 1;
        instance.remove(2);
        long result = instance.size();
        assertEquals(expResult, result);
    }

    /**
     * Test of remove method, of class SegmentedOasisMap.
     */
    @Test
    public void testRemove_Contains_Size_Overflow() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 2);
        instance.put(2, 3);
        instance.put(3, 3);
        instance.put(4, 3);
        instance.put(5, 3);;

        long expResult = 4;
        instance.remove(2);
        long result = instance.size();
        assertEquals(expResult, result);
    }

    /**
     * Test of remove method, of class SegmentedOasisMap.
     */
    @Test
    public void testRemove_Not_Contain_Size() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 2);
        instance.put(2, 3);

        long expResult = 2;
        instance.remove(3);
        long result = instance.size();
        assertEquals(expResult, result);
    }

    /**
     * Test of remove method, of class SegmentedOasisMap.
     */
    @Test
    public void testRemove_Not_Contain_Size_overflow() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 2);
        instance.put(2, 3);
        instance.put(3, 3);
        instance.put(4, 3);
        instance.put(5, 3);

        long expResult = 5;
        instance.remove(6);
        long result = instance.size();
        assertEquals(expResult, result);
    }

    /**
     * Test of putAll method, of class SegmentedOasisMap.
     */
    @Test
    public void testPutAll_No_Replace() {

        Map<Integer, Integer> inputMap = new HashMap<>();
        inputMap.put(1, 2);
        inputMap.put(2, 3);

        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.putAll(inputMap);

        assertEquals(new Integer(2), instance.get(1));
        assertEquals(new Integer(3), instance.get(2));

    }

    /**
     * Test of putAll method, of class SegmentedOasisMap.
     */
    @Test
    public void testPutAll_No_Replace_Overflow() {

        Map<Integer, Integer> inputMap = new HashMap<>();
        inputMap.put(1, 2);
        inputMap.put(2, 3);
        inputMap.put(3, 3);
        inputMap.put(4, 3);
        inputMap.put(5, 3);

        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.putAll(inputMap);

        assertEquals(new Integer(3), instance.get(3));
        assertEquals(new Integer(3), instance.get(4));

    }

    /**
     * Test of putAll method, of class SegmentedOasisMap.
     */
    @Test
    public void testPutAll_No_Replace_Size() {

        Map<Integer, Integer> inputMap = new HashMap<>();
        inputMap.put(1, 2);
        inputMap.put(2, 3);

        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.putAll(inputMap);

        assertEquals(inputMap.size(), instance.size());

    }

    /**
     * Test of putAll method, of class SegmentedOasisMap.
     */
    @Test
    public void testPutAll_No_Replace_Size_Overflow() {

        Map<Integer, Integer> inputMap = new HashMap<>();
        inputMap.put(1, 2);
        inputMap.put(2, 3);
        inputMap.put(3, 3);
        inputMap.put(4, 3);
        inputMap.put(5, 3);

        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.putAll(inputMap);

        assertEquals(inputMap.size(), instance.size());

    }

    /**
     * Test of putAll method, of class SegmentedOasisMap.
     */
    @Test
    public void testPutAll_Replace() {

        Map<Integer, Integer> inputMap = new HashMap<>();
        inputMap.put(1, 2);
        inputMap.put(2, 3);

        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(2, 4);
        instance.putAll(inputMap);

        assertEquals(new Integer(2), instance.get(1));
        assertEquals(new Integer(3), instance.get(2));

    }

    /**
     * Test of putAll method, of class SegmentedOasisMap.
     */
    @Test
    public void testPutAll_Replace_Overflow() {

        Map<Integer, Integer> inputMap = new HashMap<>();
        inputMap.put(1, 2);
        inputMap.put(2, 3);
        inputMap.put(3, 3);
        inputMap.put(4, 3);
        inputMap.put(5, 3);

        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(4, 4);
        instance.putAll(inputMap);

        assertEquals(new Integer(3), instance.get(4));

    }
    
    /**
     * Test of putAll method, of class SegmentedOasisMap.
     */
    @Test
    public void testPutAll_Replace_Overflow_Boundary() {

        Map<Integer, Integer> inputMap = new HashMap<>();
        inputMap.put(1, 2);
        inputMap.put(2, 3);
        inputMap.put(3, 3);
        inputMap.put(4, 3);
        inputMap.put(5, 3);

        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(5, 4);
        instance.putAll(inputMap);

        assertEquals(new Integer(3), instance.get(5));

    }

    /**
     * Test of putAll method, of class SegmentedOasisMap.
     */
    @Test
    public void testPutAll_Replace_Size() {

        Map<Integer, Integer> inputMap = new HashMap<>();
        inputMap.put(1, 2);
        inputMap.put(2, 3);

        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(2, 4);
        instance.put(3, 5);
        instance.putAll(inputMap);

        long expected = 3;
        assertEquals(expected, instance.size());

    }

    /**
     * Test of putAll method, of class SegmentedOasisMap.
     */
    @Test
    public void testPutAll_Replace_Size_Overflow() {

        Map<Integer, Integer> inputMap = new HashMap<>();
        inputMap.put(1, 2);
        inputMap.put(2, 3);
        inputMap.put(3, 3);
        inputMap.put(4, 3);
        inputMap.put(5, 3);

        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(2, 4);
        instance.put(6, 5);
        instance.putAll(inputMap);

        long expected = 6;
        assertEquals(expected, instance.size());

    }
    
   
    /**
     * Test of clear method, of class SegmentedOasisMap.
     */
    @Test
    public void testClear() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(2, 4);
        instance.put(3, 5);

        instance.clear();
        assertEquals(0, instance.size());
    }

    /**
     * Test of clear method, of class SegmentedOasisMap.
     */
    @Test
    public void testClear_Overflow() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 2);
        instance.put(2, 3);
        instance.put(3, 3);
        instance.put(4, 3);
        instance.put(5, 3);

        instance.clear();
        assertEquals(0, instance.size());

    }

    /**
     * Test of keySet method, of class SegmentedOasisMap.
     */
    @Test
    public void testKeySet() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(2, 4);
        instance.put(3, 5);

        Set<Integer> expResult = new HashSet<>();
        expResult.add(2);
        expResult.add(3);

        Set<Integer> result = instance.keySet();
        assertEquals(expResult, result);

    }

    /**
     * Test of keySet method, of class SegmentedOasisMap.
     */
    @Test
    public void testKeySet_Overflow() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 2);
        instance.put(2, 3);
        instance.put(3, 3);
        instance.put(4, 3);
        instance.put(5, 3);

        Set<Integer> expResult = new HashSet<>();
        expResult.add(1);
        expResult.add(2);
        expResult.add(3);
        expResult.add(4);
        expResult.add(5);

        Set<Integer> result = instance.keySet();
        assertEquals(expResult, result);

    }

    /**
     * Test of values method, of class SegmentedOasisMap.
     */
    @Test
    public void testValues() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(2, 4);
        instance.put(3, 5);

        Collection<Integer> expResult = new HashSet<>();
        expResult.add(4);
        expResult.add(5);

        Collection<Integer> result = instance.values();
        Collection<Integer> resultSet = new HashSet<>(result);

        assertEquals(expResult, resultSet);

    }

    /**
     * Test of values method, of class SegmentedOasisMap.
     */
    @Test
    public void testValues_Overflow() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 2);
        instance.put(2, 7);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 3);

        Collection<Integer> expResult = new HashSet<>();
        expResult.add(2);
        expResult.add(3);
        expResult.add(4);
        expResult.add(7);

        Collection<Integer> result = instance.values();
        Collection<Integer> resultSet = new HashSet<>(result);

        assertEquals(expResult, resultSet);

    }

    /**
     * Test of values method, of class SegmentedOasisMap.
     */
    @Test
    public void testValues_Size() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(2, 4);
        instance.put(3, 5);

        Collection<Integer> result = instance.values();

        assertEquals(2, result.size());

    }

    /**
     * Test of values method, of class SegmentedOasisMap.
     */
    @Test
    public void testValues_Size_Overflow() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 2);
        instance.put(2, 7);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 3);

        Collection<Integer> result = instance.values();

        assertEquals(5, result.size());

    }

    /**
     * Test of entrySet method, of class SegmentedOasisMap.
     */
    @Test
    public void testEntrySet() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(2, 4);
        instance.put(3, 5);

        Set<Map.Entry<Integer, Integer>> expResult = new HashSet<>();
        expResult.add(new SimpleEntry(2, 4));
        expResult.add(new SimpleEntry(3, 5));

        Set<Map.Entry<Integer, Integer>> result = instance.entrySet();
        assertEquals(expResult, result);

    }

    /**
     * Test of entrySet method, of class SegmentedOasisMap.
     */
    @Test
    public void testEntrySet_Overflow() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 2);
        instance.put(2, 7);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 3);

        Set<Map.Entry<Integer, Integer>> expResult = new HashSet<>();
        expResult.add(new SimpleEntry(1, 2));
        expResult.add(new SimpleEntry(2, 7));
        expResult.add(new SimpleEntry(3, 3));
        expResult.add(new SimpleEntry(4, 4));
        expResult.add(new SimpleEntry(5, 3));

        Set<Map.Entry<Integer, Integer>> result = instance.entrySet();
        assertEquals(expResult, result);

    }

    /**
     * Test of entrySet method, of class SegmentedOasisMap.
     */
    @Test
    public void testEntrySet_Size() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(2, 4);
        instance.put(3, 5);

        Set<Map.Entry<Integer, Integer>> result = instance.entrySet();
        assertEquals(2, result.size());

    }

    /**
     * Test of entrySet method, of class SegmentedOasisMap.
     */
    @Test
    public void testEntrySet_Size_Overflow() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 2);
        instance.put(2, 7);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 3);

        Set<Map.Entry<Integer, Integer>> result = instance.entrySet();
        assertEquals(5, result.size());

    }

    /**
     * Test of containsValue method, of class SegmentedOasisMap.
     */
    public void testContainsValue_Contains() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(2, 4);
        instance.put(3, 5);

        assertTrue(instance.containsValue(4));

    }

    /**
     * Test of containsValue method, of class SegmentedOasisMap.
     */
    public void testContainsValue_Contains_Overflow() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 2);
        instance.put(2, 7);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 3);

        assertTrue(instance.containsValue(4));

    }
    
    
    /**
     * Test of containsValue method, of class SegmentedOasisMap.
     */
    @Test
    public void testContainsValue_Contains_Overflow_Boundary() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 2);
        instance.put(2, 7);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 10);

        assertTrue(instance.containsValue(10));

    }

    /**
     * Test of containsValue method, of class SegmentedOasisMap.
     */
    public void testContainsValue_No_Value() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(2, 4);
        instance.put(3, 5);

        assertFalse(instance.containsValue(3));

    }

    /**
     * Test of containsValue method, of class SegmentedOasisMap.
     */
    public void testContainsValue_No_Value_Overflow() {
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 2);
        instance.put(2, 7);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 3);

        assertFalse(instance.containsValue(6));

    }

    /**
     * Test of Constructor, of class SegmentedOasisMap.
     */
    @Test
    public void testConstructor_Custom_Store_File() {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "oasis-collection-test-1");
        file.mkdirs();
        file.deleteOnExit();

        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(file);
        instance.put(2, 4);
        instance.put(3, 5);

        File[] subDirs = file.listFiles();

        assertNotNull(subDirs);
        
        directoriesToDelete.add(file);
    }

    /**
     * Test of Constructor, of class SegmentedOasisMap.
     */
    @Test
    public void testConstructor_Custom_Store_File_Overflow() {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "oasis-collection-test-1");
        file.mkdirs();
        file.deleteOnExit();

        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2, file);
        instance.put(1, 2);
        instance.put(2, 7);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 3);

        File[] subDirs = file.listFiles();

        assertNotNull(subDirs);
        
        directoriesToDelete.add(file);
    }

    /**
     * Test of Constructor, of class SegmentedOasisMap.
     */
    @Test
    public void testConstructor_Custom_Store_String() {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "oasis-collection-test-2");
        file.mkdirs();
        file.deleteOnExit();

        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(file.getAbsolutePath());
        instance.put(2, 4);
        instance.put(3, 5);

        File[] subDirs = file.listFiles();

        assertNotNull(subDirs);

        directoriesToDelete.add(file);
    }

    /**
     * Test of Constructor, of class SegmentedOasisMap.
     */
    @Test
    public void testConstructor_Custom_Store_String_Overflow() {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "oasis-collection-test-2");
        file.mkdirs();
        file.deleteOnExit();

        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2, file.getAbsolutePath());
        instance.put(1, 2);
        instance.put(2, 7);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 3);

        File[] subDirs = file.listFiles();

        assertNotNull(subDirs);
        
        directoriesToDelete.add(file);

    }

    /**
     * Test of Persistence, of class SegmentedOasisMap.
     */
    @Test
    public void testPersistence_size() throws Exception {

        File file = File.createTempFile("oasis-collection-test-persistence", "");
        file.deleteOnExit();

        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(2, 4);
        instance.put(3, 5);

        SegmentedHashOasisMap<Integer, Integer> readInstance;

        try (ObjectOutputStream outObjStream = new ObjectOutputStream(new FileOutputStream(file))) {
            outObjStream.writeObject(instance);
        }

        try (ObjectInputStream inObjStream = new ObjectInputStream(new FileInputStream(file))) {
            readInstance = (SegmentedHashOasisMap<Integer, Integer>) inObjStream.readObject();
        }

        assertEquals(instance.size(), readInstance.size());

    }

    /**
     * Test of Persistence, of class SegmentedOasisMap.
     */
    @Test
    public void testPersistence_size_Overflow() throws Exception {

        File file = File.createTempFile("oasis-collection-test-persistence", "");
        file.deleteOnExit();

        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 2);
        instance.put(2, 7);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 3);

        SegmentedHashOasisMap<Integer, Integer> readInstance;

        try (ObjectOutputStream outObjStream = new ObjectOutputStream(new FileOutputStream(file))) {
            outObjStream.writeObject(instance);
        }

        try (ObjectInputStream inObjStream = new ObjectInputStream(new FileInputStream(file))) {
            readInstance = (SegmentedHashOasisMap<Integer, Integer>) inObjStream.readObject();
        }

        assertEquals(instance.size(), readInstance.size());

    }

    /**
     * Test of Persistence, of class SegmentedOasisMap.
     */
    @Test
    public void testPersistence_Elements() throws Exception {

        File file = File.createTempFile("oasis-collection-test-persistence", "");
        file.deleteOnExit();

        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(2, 4);
        instance.put(3, 5);

        SegmentedHashOasisMap<Integer, Integer> readInstance;

        try (ObjectOutputStream outObjStream = new ObjectOutputStream(new FileOutputStream(file))) {
            outObjStream.writeObject(instance);
        }

        try (ObjectInputStream inObjStream = new ObjectInputStream(new FileInputStream(file))) {
            readInstance = (SegmentedHashOasisMap<Integer, Integer>) inObjStream.readObject();
        }

        assertEquals(instance.entrySet(), readInstance.entrySet());

    }

    /**
     * Test of Persistence, of class SegmentedOasisMap.
     */
    @Test
    public void testPersistence_Elements_Overflow() throws Exception {

        File file = File.createTempFile("oasis-collection-test-persistence", "");
        file.deleteOnExit();

        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 2);
        instance.put(2, 7);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 3);

        SegmentedHashOasisMap<Integer, Integer> readInstance;

        try (ObjectOutputStream outObjStream = new ObjectOutputStream(new FileOutputStream(file))) {
            outObjStream.writeObject(instance);
        }

        try (ObjectInputStream inObjStream = new ObjectInputStream(new FileInputStream(file))) {
            readInstance = (SegmentedHashOasisMap<Integer, Integer>) inObjStream.readObject();
        }

        assertEquals(instance.entrySet(), readInstance.entrySet());

    }

    /**
     * Test of Persistence, of class SegmentedOasisMap.
     */
    @Test
    public void testPersistence_Custom_Store() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "oasis-collection-testPersistence_Custom_Store");
        file.mkdirs();
        file.deleteOnExit();

        File saveToFile = File.createTempFile("oasis-collection-test-persistence", "");
        file.deleteOnExit();

        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(file);
        instance.put(2, 4);
        instance.put(3, 5);

        SegmentedHashOasisMap<Integer, Integer> readInstance;

        try (ObjectOutputStream outObjStream = new ObjectOutputStream(new FileOutputStream(saveToFile))) {
            outObjStream.writeObject(instance);
        }

        try (ObjectInputStream inObjStream = new ObjectInputStream(new FileInputStream(saveToFile))) {
            readInstance = (SegmentedHashOasisMap<Integer, Integer>) inObjStream.readObject();
        }

        Entry<Integer, Integer> temp;
        for (Iterator<Entry<Integer, Integer>> itr = readInstance.entrySet().iterator(); itr.hasNext();) {
            temp = itr.next();
            assertEquals(instance.get(temp.getKey()), temp.getValue());
        }

        directoriesToDelete.add(file);
    }

    /**
     * Test of Persistence, of class SegmentedOasisMap.
     */
    @Test
    public void testPersistence_Custom_Store_Overflow() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "oasis-collection-testPersistence_Custom_Store_Overflow");
        file.mkdirs();
        file.deleteOnExit();

        File saveToFile = File.createTempFile("oasis-collection-test-persistence", "");
        file.deleteOnExit();

        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2, file);
        instance.put(1, 2);
        instance.put(2, 7);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 3);

        SegmentedHashOasisMap<Integer, Integer> readInstance;

        try (ObjectOutputStream outObjStream = new ObjectOutputStream(new FileOutputStream(saveToFile))) {
            outObjStream.writeObject(instance);
        }

        try (ObjectInputStream inObjStream = new ObjectInputStream(new FileInputStream(saveToFile))) {
            readInstance = (SegmentedHashOasisMap<Integer, Integer>) inObjStream.readObject();
        }

        Entry<Integer, Integer> temp;
        for (Iterator<Entry<Integer, Integer>> itr = readInstance.entrySet().iterator(); itr.hasNext();) {
            temp = itr.next();
            assertEquals(instance.get(temp.getKey()), temp.getValue());
        }

        directoriesToDelete.add(file);
    }
    
    /**
     * Test of compact, of class SegmentedOasisMap.
     */
    @Test
    public void testCompact_Overflow_Caching_Enabled() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "oasis-collection-testCompact_Overflow_Caching_Enabled");
        file.mkdirs();
        file.deleteOnExit();
        
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2, file);
        instance.enableCache();
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.put(7, 7);
        instance.put(8, 8);
        
        instance.remove(2);  
        instance.compact();
        instance.disableCache();
        
        assertNumberFilesInDirectory(instance.instanceStore(), 2);
        
        directoriesToDelete.add(file);

        
    }
    
    /**
     * Test of compact, of class SegmentedOasisMap.
     */
    @Test
    public void testCompact_Overflow_Caching_Disabled() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "oasis-collection-testCompact_Overflow_Caching_Disabled");
        file.mkdirs();
        file.deleteOnExit();
        
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2, file);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.put(7, 7);
        instance.put(8, 8);
        
        instance.remove(2); 
        instance.compact();
        
        assertNumberFilesInDirectory(instance.instanceStore(), 2);   
        
        directoriesToDelete.add(file);
    }
    
    /**
     * Test of compact, of class SegmentedOasisMap.
     */
    @Test
    public void testCompact_Memstore_Emptied_Caching_Enabled() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "oasis-collection-testCompact_Memstore_Emptied_Caching_Enabled");
        file.mkdirs();
        file.deleteOnExit();
        
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2, file);
        instance.enableCache();
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.put(7, 7);
        instance.put(8, 8);
        
        instance.remove(1);
        instance.remove(2);  
        instance.compact();
        instance.disableCache();
        
        assertNumberFilesInDirectory(instance.instanceStore(), 2);
        
        directoriesToDelete.add(file);
        
    }
    
    /**
     * Test of compact, of class SegmentedOasisMap.
     */
    @Test
    public void testCompact_Memstore_Emptied_Caching_Disabled() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "oasis-collection-testCompact_Memstore_Emptied_Caching_Disabled");
        file.mkdirs();
        file.deleteOnExit();
        
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2, file);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.put(7, 7);
        instance.put(8, 8);
        
        instance.remove(1);
        instance.remove(2); 
        instance.compact();
        
        assertEquals(2, instance.persistedSegmentCount());
        
        directoriesToDelete.add(file);
        
    }
    
     /**
     * Test of compact, of class SegmentedOasisMap.
     */
    @Test
    public void testCompact_Last_Segment_Caching_Enabled() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "oasis-collection-testCompact_Last_Segment_Caching_Enabled");
        file.mkdirs();
        file.deleteOnExit();
        
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2, file);
        instance.enableCache();
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.put(7, 7);
        instance.put(8, 8);
        
        instance.remove(8);     
        instance.compact();
        instance.disableCache();
        
        assertNumberFilesInDirectory(instance.instanceStore(), 2);
        
        directoriesToDelete.add(file);

        
    }
    
    /**
     * Test of compact, of class SegmentedOasisMap.
     */
    @Test
    public void testCompact_Last_Segment_Caching_Disabled() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "oasis-collection-testCompact_Last_Segment_Caching_Disabled");
        file.mkdirs();
        file.deleteOnExit();
        
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2, file);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.put(7, 7);
        instance.put(8, 8);
        
        instance.remove(8); 
        instance.compact();
        
        assertNumberFilesInDirectory(instance.instanceStore(), 2);   
        
        directoriesToDelete.add(file);
    }
    
    /**
     * Test of compact, of class SegmentedOasisMap.
     */
    @Test
    public void testCompact_Last_Segment_Emptied_Caching_Enabled() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "oasis-collection-testCompact_Last_Segment_Emptied_Caching_Enabled");
        file.mkdirs();
        file.deleteOnExit();
        
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2, file);
        instance.enableCache();
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.put(7, 7);
        instance.put(8, 8);
        
        instance.remove(7);
        instance.remove(8);
        instance.compact();
        instance.disableCache();
        
        assertNumberFilesInDirectory(instance.instanceStore(), 2);
        
        directoriesToDelete.add(file);
        
    }
    
    /**
     * Test of compact, of class SegmentedOasisMap.
     */
    @Test
  //  @Ignore
    public void testCompact_Last_Segment_Emptied_Caching_Disabled() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "oasis-collection-testCompact_Last_Segment_Emptied_Caching_Disabled");
        file.mkdirs();
        file.deleteOnExit();
        
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2, file);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.put(7, 7);
        instance.put(8, 8);
        
        instance.remove(7);
        instance.remove(8); 
        instance.compact();
        
        assertEquals(2, instance.persistedSegmentCount());
        
        directoriesToDelete.add(file);
        
    }
    
     /**
     * Test of compact, of class SegmentedOasisMap.
     */
    @Test
    public void testCompact_Mid_Segment_Caching_Enabled() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "oasis-collection-testCompact_Last_Segment_Caching_Enabled");
        file.mkdirs();
        file.deleteOnExit();
        
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2, file);
        instance.enableCache();
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.put(7, 7);
        instance.put(8, 8);
        
        instance.remove(6);     
        instance.compact();
        instance.disableCache();
        
        assertNumberFilesInDirectory(instance.instanceStore(), 2);
        
        directoriesToDelete.add(file);

        
    }
    
    /**
     * Test of compact, of class SegmentedOasisMap.
     */
    @Test
    public void testCompact_Mid_Segment_Caching_Disabled() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "oasis-collection-testCompact_Last_Segment_Caching_Disabled");
        file.mkdirs();
        file.deleteOnExit();
        
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2, file);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.put(7, 7);
        instance.put(8, 8);
        
        instance.remove(6); 
        instance.compact();
        
        assertNumberFilesInDirectory(instance.instanceStore(), 2);    
        
        directoriesToDelete.add(file);
    }
    
    /**
     * Test of compact, of class SegmentedOasisMap.
     */
    @Test
    public void testCompact_Mid_Segment_Emptied_Caching_Enabled() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "oasis-collection-testCompact_Last_Segment_Emptied_Caching_Enabled");
        file.mkdirs();
        file.deleteOnExit();
        
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2, file);
        instance.enableCache();
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.put(7, 7);
        instance.put(8, 8);
        
        instance.remove(5);
        instance.remove(6);  
        instance.compact();
        instance.disableCache();
        
        assertNumberFilesInDirectory(instance.instanceStore(), 2);
        
        directoriesToDelete.add(file);
        
    }
    
    /**
     * Test of compact, of class SegmentedOasisMap.
     */
    @Test
    public void testCompact_Mid_Segment_Emptied_Caching_Disabled() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "oasis-collection-testCompact_Last_Segment_Emptied_Caching_Disabled");
        file.mkdirs();
        file.deleteOnExit();
        
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2, file);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.put(7, 7);
        instance.put(8, 8);
        
        instance.remove(5);
        instance.remove(6); 
        instance.compact();
        
        assertEquals(2, instance.persistedSegmentCount());
        
        directoriesToDelete.add(file);
        
    }
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_Overflow() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.destroy();       
        
        assertNumberFilesInDirectory(instance.instanceStore(), 0);
        
    }
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_Size() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.destroy();       
        
        instance.size();
        
    }
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_Size_Overflow() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.destroy();       
        
        instance.size();
        
    }
    
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_IsEmpty() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.destroy();       
        
        instance.isEmpty();
        
    }
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_IsEmpty_Overflow() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.destroy();       
        
        instance.isEmpty();
        
    }
    
    
     /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_ContainsKey() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.destroy();       
        
        instance.containsKey(3);
        
    }
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_ContainsKey_Overflow() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.destroy();       
        
        instance.containsKey(3);
        
    }
    
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_ContainsValue() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.destroy();       
        
        instance.containsValue(3);
        
    }
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_ContainsValue_Overflow() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.destroy();       
        
        instance.containsValue(3);
        
    }
    
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_Get() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.destroy();       
        
        instance.get(3);
        
    }
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_Get_Overflow() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.destroy();       
        
        instance.get(3);
        
    }
    
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_Put() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.destroy();       
        
        instance.put(3, 3);
        
    }
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_Put_Overflow() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.destroy();       
        
        instance.put(3, 3);
        
    }
    
    

    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_Remove() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.destroy();       
        
        instance.remove(3);
        
    }
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_Remove_Overflow() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.destroy();       
        
        instance.remove(3);
        
    }
    
     /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_Clear() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.destroy();       
        
        instance.clear();
        
    }
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_Clear_Overflow() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.destroy();       
        
        instance.clear();
        
    }
    
    
        /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_PutAll() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.destroy();       
        
        instance.putAll(new HashMap<>());
        
    }
    
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_PutAll_Overflow() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.destroy();       
        
        instance.putAll(new HashMap<>());
        
    }
    
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_Keyset() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.destroy();       
        
        instance.keySet();
        
    }
    
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_Keyset_Overflow() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.destroy();       
        
        instance.keySet();
        
    }
    
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_Values() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.destroy();       
        
        instance.values();
        
    }
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_Values_Overflow() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.destroy();       
        
        instance.values();
        
    }
    
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_EntrySet() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.destroy();       
        
        instance.entrySet();
        
    }
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_EntrySet_Overflow() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.destroy();       
        
        instance.entrySet();
        
    }
    
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_EnableCache() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.destroy();       
        
        instance.enableCache();
        
    }
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_DisableCache_Overflow() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.destroy();       
        
        instance.disableCache();
        
    }
    
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_DisableCache() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.destroy();       
        
        instance.disableCache();
        
    }
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_EnableCache_Overflow() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.destroy();       
        
        instance.enableCache();
        
    }


    /**
     * Test of compact, of class SegmentedOasisList.
     */
    @Test
    public void testCompactFast_One_Sgement_Removed() throws Exception {

        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(3, 2);

        // 2 persisted segment counts
        IntStream.rangeClosed(1, 8).forEach(it ->  instance.put(it, it));

        // remove 2 items form memory before compacting fast
        instance.remove(1);
        instance.remove(3);

        instance.compactFast();

        assertEquals(1, instance.persistedSegmentCount());

    }

    /**
     * Test of compact, of class SegmentedOasisList.
     */
    @Test
    public void testCompactFast_Muliple_Sgements_Removed() throws Exception {

        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(3, 1);

        // 1 persisted segment counts
        IntStream.rangeClosed(1, 6).forEach(it ->  instance.put(it, it));

        // remove 2 items from memory before compacting fast
        instance.remove(1);
        instance.remove(2);

        instance.compactFast();

        assertEquals(0, instance.persistedSegmentCount());

    }

    /**
     * Test of compact, of class SegmentedOasisList.
     */
    @Test
    public void testCompactFast_Sgement_Not_Removed() throws Exception {

        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(3, 2);

        // 2 persisted segment counts
        IntStream.rangeClosed(1, 8).forEach(it ->  instance.put(it, it));

        // remove 2 items form memory before compacting fast
        instance.remove(1);

        instance.compactFast();

        assertEquals(2, instance.persistedSegmentCount());

    }
    
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_Compact() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.destroy();       
        
        instance.compact();
        
    }
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_Compact_Overflow() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.destroy();       
        
        instance.compact();
        
    }
    
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_IsCacheEnabled() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.destroy();       
        
        instance.isCacheEnabled();
        
    }
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_IsCacheEnabled_Overflow() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.destroy();       
        
        instance.isCacheEnabled();
        
    }
    
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_InstanceStore() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.destroy();       
        
        instance.instanceStore();
        
    }
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_InstanceStore_Overflow() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.destroy();       
        
        instance.instanceStore();
        
    }
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_PersistedSegmentCount() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>();
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.destroy();       
        
        instance.persistedSegmentCount();
        
    }
    
    /**
     * Test of destroy, of class SegmentedOasisMap.
     */
    @Test(expected=IllegalStateException.class)
    public void testDestroy_PersistedSegmentCount_Overflow() throws Exception {
       
        SegmentedHashOasisMap<Integer, Integer> instance = new SegmentedHashOasisMap<>(2, 2);
        instance.put(1, 1);
        instance.put(2, 2);
        instance.put(3, 3);
        instance.put(4, 4);
        instance.put(5, 5);
        instance.put(6, 6);
        instance.destroy();       
        
        instance.persistedSegmentCount();
        
    }
    
    private void assertNumberFilesInDirectory(String dir , int fileCount) {
        assertEquals(fileCount, new File(dir).listFiles().length);
    }

}
