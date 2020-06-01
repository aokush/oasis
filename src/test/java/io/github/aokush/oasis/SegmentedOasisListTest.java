/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.aokush.oasis;

import io.github.aokush.oasis.OasisList;
import io.github.aokush.oasis.segmented.SegmentedOasisList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author AKuseju
 */
public class SegmentedOasisListTest {

    public SegmentedOasisListTest() {
    }

    @Test
    public void testCustomStore() throws IOException {
        Path path = Files.createTempDirectory("dir");

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(path.toAbsolutePath().toFile());
        instance.add(1);

        assertTrue(instance.instanceStore().startsWith(path.toAbsolutePath().toFile().getAbsolutePath()));
    }

    /**
     * Test of size method, of class SegmentedOasisList.
     */
    @Test
    public void testSize() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        int expResult = 4;

        instance.add(1);
        instance.add(2);
        instance.add(3);
        instance.add(4);

        long result = instance.size();
        assertEquals(expResult, result);
    }

    /**
     * Test of size method, of class SegmentedOasisList.
     */
    @Test
    public void testSize_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        int expResult = 6;

        instance.add(1);
        instance.add(2);
        instance.add(3);
        instance.add(4);
        instance.add(5);
        instance.add(6);

        long result = instance.size();
        assertEquals(expResult, result);

    }

    /**
     * Test of item orering.
     */
    @Test
    public void testItem_Ordering() {
        OasisList<Integer> baseList = new SegmentedOasisList<>(2, 2);

        List<Integer> expResult = Arrays.asList(1, 2, 6, 8);
        baseList.addAll(expResult);

        for (int i = 0; i < expResult.size(); i++) {
            assertEquals(expResult.get(i), baseList.get(i));
        }
    }

    /**
     * Test of item orering.
     */
    @Test
    public void testItem_Ordering_With_Holes() {
        OasisList<Integer> instance = new SegmentedOasisList<>(2, 2);

        List<Integer> expResult = Arrays.asList(1, 6, 8, 19);
        instance.addAll(Arrays.asList(1, 2, 6, 8));

        instance.remove(1);
        instance.add(19);

        for (int i = 0; i < expResult.size(); i++) {
            assertEquals(expResult.get(i), instance.get(i));
        }
    }

    /**
     * Test of isEmpty method, of class SegmentedOasisList.
     */
    @Test
    public void testIsEmpty() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        instance.add(1);
        instance.add(2);
        instance.add(3);
        instance.add(4);
        instance.clear();

        boolean expResult = true;
        boolean result = instance.isEmpty();
        assertEquals(expResult, result);
    }

    /**
     * Test of isEmpty method, of class SegmentedOasisList.
     */
    @Test
    public void testIsEmpty_Empty_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(3, 2);
        instance.add(1);
        instance.add(2);
        instance.add(3);
        instance.add(4);
        instance.add(5);
        instance.add(6);
        instance.clear();

        boolean expResult = true;
        boolean result = instance.isEmpty();
        assertEquals(expResult, result);
    }

    /**
     * Test of isEmpty method, of class SegmentedOasisList.
     */
    @Test
    public void testIsEmpty_Not_Empty() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        instance.add(1);
        instance.add(2);
        instance.add(3);

        boolean expResult = false;
        boolean result = instance.isEmpty();
        assertEquals(expResult, result);
    }

    /**
     * Test of isEmpty method, of class SegmentedOasisList.
     */
    @Test
    public void testIsEmpty_Not_Empty_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(3, 2);
        instance.add(1);
        instance.add(2);
        instance.add(3);
        instance.add(4);
        instance.add(5);
        instance.add(6);

        boolean expResult = false;
        boolean result = instance.isEmpty();
        assertEquals(expResult, result);
    }

    /**
     * Test of contains method, of class SegmentedOasisList.
     */
    @Test
    public void testContains_Contain() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        instance.add(1);
        instance.add(2);
        instance.add(3);

        boolean expResult = true;
        boolean result = instance.contains(1);
        assertEquals(expResult, result);
    }

    /**
     * Test of contains method, of class SegmentedOasisList.
     */
    @Test
    public void testContains_Contain_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(3, 2);
        instance.add(1);
        instance.add(2);
        instance.add(3);
        instance.add(4);
        instance.add(5);
        instance.add(6);

        boolean expResult = true;
        boolean result = instance.contains(4);
        assertEquals(expResult, result);
    }

    /**
     * Test of contains method, of class SegmentedOasisList.
     */
    @Test
    public void testContains_Contain_Overflow_Boundary() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        instance.add(1);
        instance.add(2);
        instance.add(3);
        instance.add(4);
        instance.add(5);
        instance.add(6);

        boolean expResult = true;
        boolean result = instance.contains(6);
        assertEquals(expResult, result);
    }

    /**
     * Test of contains method, of class SegmentedOasisList.
     */
    @Test
    public void testContains_Not_Contain() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        instance.add(1);
        instance.add(2);

        boolean expResult = false;
        boolean result = instance.contains(3);
        assertEquals(expResult, result);
    }

    /**
     * Test of contains method, of class SegmentedOasisList.
     */
    @Test
    public void testContains_Not_Contain_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        instance.add(1);
        instance.add(2);
        instance.add(3);
        instance.add(4);
        instance.add(5);
        instance.add(6);

        boolean expResult = false;
        boolean result = instance.contains(7);
        assertEquals(expResult, result);
    }

    /**
     * Test of iterator method, of class SegmentedOasisList.
     */
    @Test
    public void testIterator() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 6, 8));

        Iterator<Integer> instance = baseList.iterator();
        assertTrue(isEqual(instance, baseList));

    }

    /**
     * Test of iterator method, of class SegmentedOasisList.
     */
    @Test
    public void testIterator_Overflow() {
        OasisList<Integer> baseList = new SegmentedOasisList<>(2, 2);
        baseList.addAll(Arrays.asList(1, 2, 6, 8, 7, 8, 90));

        Iterator<Integer> instance = baseList.iterator();
        assertTrue(isEqual(instance, baseList));

    }

    /**
     * Test of toArray method, of class SegmentedOasisList.
     */
    @Test
    public void testToArray_0args() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        instance.add(1);
        instance.add(2);
        instance.add(3);
        instance.add(4);
        instance.add(5);
        instance.add(6);

        Integer[] expResult = { 1, 2, 3, 4, 5, 6 };
        Object[] result = instance.toArray();
        assertArrayEquals(expResult, result);

    }

    /**
     * Test of toArray method, of class SegmentedOasisList.
     */
    @Test
    public void testToArray_GenericType() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        instance.add(1);
        instance.add(2);
        instance.add(3);
        instance.add(4);
        instance.add(5);
        instance.add(6);

        Integer[] expResult = { 1, 2, 3, 4, 5, 6 };
        Integer[] result = instance.toArray(new Integer[] {});
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of add method, of class SegmentedOasisList.
     */
    @Test
    public void testAdd_GenericType() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        boolean expResult = true;
        boolean result = instance.add(1);
        assertEquals(expResult, result);
    }

    /**
     * Test of add method, of class SegmentedOasisList.
     */
    @Test
    public void testAdd_GenericType_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        instance.add(1);
        instance.add(2);

        boolean expResult = true;
        boolean result = instance.add(3);
        assertEquals(expResult, result);
    }

    /**
     * Test of add method, of class SegmentedOasisList.
     */
    @Test
    public void testAdd_GenericType_Overflow_Boundary() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        instance.add(1);
        instance.add(2);
        instance.add(3);
        instance.add(4);
        instance.add(5);
        instance.add(6);

        boolean expResult = true;
        boolean result = instance.add(7);
        assertEquals(expResult, result);
    }

    /**
     * Test of remove method, of class SegmentedOasisList.
     */
    @Test
    public void testRemove_Contains() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        instance.add(1);

        Integer item = 1;
        boolean expResult = true;
        boolean result = instance.remove(item);
        assertEquals(expResult, result);
    }

    /**
     * Test of remove method, of class SegmentedOasisList.
     */
    @Test
    public void testRemove_Not_Contains() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        instance.add(1);

        Integer item = 2;
        boolean expResult = false;
        boolean result = instance.remove(item);
        assertEquals(expResult, result);
    }

    /**
     * Test of remove method, of class SegmentedOasisList.
     */
    @Test
    public void testRemove_Contains_Size() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        instance.add(1);
        instance.add(1);
        instance.add(2);
        instance.add(1);

        instance.remove(new Integer(1));

        int expResult = 3;
        int result = instance.size();
        assertEquals(expResult, result);
    }

    /**
     * Test of remove method, of class SegmentedOasisList.
     */
    @Test
    public void testRemove_Contains_First_Ocurrence() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        instance.add(1);
        instance.add(2);
        instance.add(1);
        instance.add(1);

        instance.remove(new Integer(1));

        int expResult = 2;
        int result = instance.get(0);
        assertEquals(expResult, result);
    }

    /**
     * Test of remove method, of class SegmentedOasisList.
     */
    @Test
    public void testRemove_Contains_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        instance.add(1);
        instance.add(1);
        instance.add(1);
        instance.add(1);
        instance.add(1);
        instance.add(1);

        Integer item = 1;
        boolean expResult = true;
        boolean result = instance.remove(item);
        assertEquals(expResult, result);
    }

    /**
     * Test of remove method, of class SegmentedOasisList.
     */
    @Test
    public void testRemove_Not_Contains_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        instance.add(1);
        instance.add(1);
        instance.add(1);
        instance.add(1);
        instance.add(1);
        instance.add(1);

        Integer item = 2;
        boolean expResult = false;
        boolean result = instance.remove(item);
        assertEquals(expResult, result);
    }

    /**
     * Test of remove method, of class SegmentedOasisList.
     */
    @Test
    public void testRemove_Contains_Size_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        instance.add(1);
        instance.add(1);
        instance.add(1);
        instance.add(1);
        instance.add(1);
        instance.add(1);

        instance.remove(new Integer(1));

        int expResult = 5;
        int result = instance.size();
        assertEquals(expResult, result);
    }

    /**
     * Test of remove method, of class SegmentedOasisList.
     */
    @Test
    public void testRemove_Contains_First_Ocurrence_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        instance.add(3);
        instance.add(2);
        instance.add(1);
        instance.add(1);
        instance.add(1);
        instance.add(1);
        instance.add(3);

        instance.remove(new Integer(3));

        int expResult = 2;
        int result = instance.get(0);
        assertEquals(expResult, result);
    }

    /**
     * Test of containsAll method, of class SegmentedOasisList.
     */
    @Test
    public void testContainsAll_Contains() {

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        instance.add(3);
        instance.add(2);
        instance.add(1);
        instance.add(4);

        Collection c = Arrays.asList(1, 2, 3);

        boolean expResult = true;
        boolean result = instance.containsAll(c);
        assertEquals(expResult, result);
    }

    /**
     * Test of containsAll method, of class SegmentedOasisList.
     */
    @Test
    public void testContainsAll_Not_Contain() {

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        instance.add(3);
        instance.add(2);
        instance.add(1);
        instance.add(4);

        Collection c = Arrays.asList(1, 2, 5);

        boolean expResult = false;
        boolean result = instance.containsAll(c);
        assertEquals(expResult, result);
    }

    /**
     * Test of containsAll method, of class SegmentedOasisList.
     */
    @Test
    public void testContainsAll_Contains_Overflow() {

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        instance.add(3);
        instance.add(2);
        instance.add(1);
        instance.add(4);
        instance.add(5);
        instance.add(4);
        instance.add(7);

        Collection c = Arrays.asList(3, 4, 5, 7);

        boolean expResult = true;
        boolean result = instance.containsAll(c);
        assertEquals(expResult, result);
    }

    /**
     * Test of containsAll method, of class SegmentedOasisList.
     */
    @Test
    public void testContainsAll_Not_Contain_Overflow() {

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        instance.add(3);
        instance.add(2);
        instance.add(1);
        instance.add(4);
        instance.add(5);
        instance.add(4);
        instance.add(7);

        Collection c = Arrays.asList(1, 2, 5, 8);

        boolean expResult = false;
        boolean result = instance.containsAll(c);
        assertEquals(expResult, result);
    }

    /**
     * Test of addAll method, of class SegmentedOasisList.
     */
    @Test
    public void testAddAll_Collection() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        instance.add(3);
        instance.add(2);

        Collection c = Arrays.asList(1, 2, 3);

        boolean expResult = true;
        boolean result = instance.addAll(c);
        assertEquals(expResult, result);

    }

    /**
     * Test of addAll method, of class SegmentedOasisList.
     */
    @Test
    public void testAddAll_Collection_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        instance.add(3);
        instance.add(2);
        instance.add(2);

        Collection c = Arrays.asList(1, 2, 3);

        boolean expResult = true;
        boolean result = instance.addAll(c);
        assertEquals(expResult, result);

    }

    /**
     * Test of addAll method, of class SegmentedOasisList.
     */
    @Test
    public void testAddAll_int_Collection() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        instance.add(3);
        instance.add(2);

        Collection c = Arrays.asList(1, 2, 3);

        boolean expResult = true;
        boolean result = instance.addAll(1, c);
        assertEquals(expResult, result);

    }

    /**
     * Test of addAll method, of class SegmentedOasisList.
     */
    @Test
    public void testAddAll_int_Collection_Size() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        instance.add(3);
        instance.add(2);

        Collection c = Arrays.asList(1, 2, 3);
        instance.addAll(1, c);

        int expResult = 5;
        int result = instance.size();
        assertEquals(expResult, result);

    }

    /**
     * Test of addAll method, of class SegmentedOasisList.
     */
    @Test
    public void testAddAll_int_Collection_Order() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        instance.add(3);
        instance.add(2);

        Collection c = Arrays.asList(1, 2, 3);
        instance.addAll(1, c);

        List expResult = Arrays.asList(3, 1, 2, 3, 2);

        for (int i = 0; i < expResult.size(); i++) {
            assertEquals(expResult.get(i), instance.get(i));
        }
    }

    /**
     * Test of addAll method, of class SegmentedOasisList.
     */
    @Test
    public void testAddAll_int_Collection_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        instance.add(3);
        instance.add(2);
        instance.add(3);
        instance.add(2);
        instance.add(3);
        instance.add(2);

        Collection c = Arrays.asList(1, 2, 3);

        boolean expResult = true;
        boolean result = instance.addAll(1, c);
        assertEquals(expResult, result);

    }

    /**
     * Test of addAll method, of class SegmentedOasisList.
     */
    @Test
    public void testAddAll_int_Collection_Size_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        instance.add(3);
        instance.add(2);
        instance.add(3);
        instance.add(2);
        instance.add(3);
        instance.add(2);

        Collection c = Arrays.asList(1, 2, 3);
        instance.addAll(1, c);

        int expResult = 9;
        int result = instance.size();
        assertEquals(expResult, result);

    }

    /**
     * Test of addAll method, of class SegmentedOasisList.
     */
    @Test
    public void testAddAll_int_Collection_Order_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        instance.add(3);
        instance.add(2);
        instance.add(3);
        instance.add(2);
        instance.add(3);
        instance.add(2);

        Collection c = Arrays.asList(1, 2, 3);
        instance.addAll(1, c);

        List expResult = Arrays.asList(3, 1, 2, 3, 2, 3, 2, 3, 2);

        for (int i = 0; i < expResult.size(); i++) {
            assertEquals(expResult.get(i), instance.get(i));
        }
    }

    /**
     * Test of removeAll method, of class SegmentedOasisList.
     */
    @Test
    public void testRemoveAll() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        instance.add(3);
        instance.add(2);
        instance.add(3);
        instance.add(2);
        instance.add(3);
        instance.add(2);

        Collection c = Arrays.asList(2, 3);
        boolean expectedResult = true;
        boolean result = instance.removeAll(c);

        assertEquals(expectedResult, result);

    }

    /**
     * Test of removeAll method, of class SegmentedOasisList.
     */
    @Test
    public void testRemoveAll_Size() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        instance.add(3);
        instance.add(2);
        instance.add(3);
        instance.add(2);
        instance.add(3);
        instance.add(2);

        Collection c = Arrays.asList(2, 3);
        instance.removeAll(c);

        int expectedResult = 0;
        assertEquals(expectedResult, instance.size());

    }

    /**
     * Test of removeAll method, of class SegmentedOasisList.
     */
    @Test
    public void testRemoveAll_Not_Empty() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        instance.add(3);
        instance.add(2);
        instance.add(3);
        instance.add(2);
        instance.add(3);
        instance.add(1);
        instance.add(2);

        Collection c = Arrays.asList(2, 3);
        instance.removeAll(c);

        int expectedResult = 1;
        assertEquals(expectedResult, instance.size());

    }

    /**
     * Test of removeAll method, of class SegmentedOasisList.
     */
    @Test
    public void testRemoveAll_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        instance.add(3);
        instance.add(2);
        instance.add(3);
        instance.add(2);
        instance.add(3);
        instance.add(2);

        Collection c = Arrays.asList(2, 3);
        boolean expectedResult = true;
        boolean result = instance.removeAll(c);

        assertEquals(expectedResult, result);

    }

    /**
     * Test of removeAll method, of class SegmentedOasisList.
     */
    @Test
    public void testRemoveAll_Size_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        instance.add(3);
        instance.add(2);
        instance.add(3);
        instance.add(2);
        instance.add(3);
        instance.add(2);

        Collection c = Arrays.asList(2, 3);
        instance.removeAll(c);

        int expectedResult = 0;
        assertEquals(expectedResult, instance.size());

    }

    /**
     * Test of removeAll method, of class SegmentedOasisList.
     */
    @Test
    public void testRemoveAll_Not_Empty_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        instance.add(3);
        instance.add(2);
        instance.add(3);
        instance.add(2);
        instance.add(3);
        instance.add(1);
        instance.add(2);

        Collection c = Arrays.asList(2, 3);
        instance.removeAll(c);

        int expectedResult = 1;
        assertEquals(expectedResult, instance.size());

    }

    /**
     * Test of retainAll method, of class SegmentedOasisList.
     */
    @Test
    public void testRetainAll() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        instance.add(3);
        instance.add(2);
        instance.add(1);
        instance.add(2);
        instance.add(3);
        instance.add(2);

        Collection c = Arrays.asList(2, 3);
        boolean expectedResult = true;
        boolean result = instance.retainAll(c);

        assertEquals(expectedResult, result);
    }

    /**
     * Test of retainAll method, of class SegmentedOasisList.
     */
    @Test
    public void testRetainAll_Size() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        instance.add(3);
        instance.add(2);
        instance.add(1);
        instance.add(2);
        instance.add(3);
        instance.add(2);

        Collection c = Arrays.asList(2, 3);
        instance.retainAll(c);

        int expectedResult = 5;
        assertEquals(expectedResult, instance.size());

    }

    /**
     * Test of retainAll method, of class SegmentedOasisList.
     */
    @Test
    public void testRetainAll_Empty() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();

        instance.add(1);
        instance.add(1);
        instance.add(1);
        instance.add(1);

        Collection c = Arrays.asList(2, 3);
        instance.retainAll(c);

        int expectedResult = 0;
        assertEquals(expectedResult, instance.size());

    }

    /**
     * Test of retainAll method, of class SegmentedOasisList.
     */
    @Test
    public void testRetainAll_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        instance.add(3);
        instance.add(2);
        instance.add(1);
        instance.add(2);
        instance.add(3);
        instance.add(2);

        Collection c = Arrays.asList(2, 3);
        boolean expectedResult = true;
        boolean result = instance.retainAll(c);

        assertEquals(expectedResult, result);

    }

    /**
     * Test of retainAll method, of class SegmentedOasisList.
     */
    @Test
    public void testRetainAll_Size_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);

        Collection c = Arrays.asList(3, 2, 3, 2, 3, 1, 2);
        instance.addAll(c);

        c = Arrays.asList(2, 3);
        instance.retainAll(c);

        int expectedResult = 6;
        assertEquals(expectedResult, instance.size());

    }

    /**
     * Test of retainAll method, of class SegmentedOasisList.
     */
    @Test
    public void testRetainAll_Empty_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        Collection c = Arrays.asList(1, 1, 1, 1, 1, 1, 1);
        instance.addAll(c);

        c = Arrays.asList(2, 3);
        instance.retainAll(c);

        int expectedResult = 0;
        assertEquals(expectedResult, instance.size());

    }

    /**
     * Test of clear method, of class SegmentedOasisList.
     */
    @Test
    public void testClear() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        Collection c = Arrays.asList(1, 1, 1, 1, 1, 1, 1);
        instance.addAll(c);
        instance.clear();

        assertTrue(instance.isEmpty());

    }

    /**
     * Test of clear method, of class SegmentedOasisList.
     */
    @Test
    public void testClear_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        Collection c = Arrays.asList(1, 1, 1, 1, 1, 1, 1);
        instance.addAll(c);
        instance.clear();

        assertTrue(instance.isEmpty());

    }

    /**
     * Test of get method, of class SegmentedOasisList.
     */
    @Test
    public void testGet() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        Collection c = Arrays.asList(1, 1, 7, 1, 1, 1, 1);
        instance.addAll(c);
        Integer expResult = 7;
        Integer result = instance.get(2);
        assertEquals(expResult, result);

    }

    /**
     * Test of get method, of class SegmentedOasisList.
     */
    @Test
    public void testGet_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        Collection c = Arrays.asList(1, 1, 7, 1, 1, 1, 1);
        instance.addAll(c);
        Integer expResult = 7;
        Integer result = instance.get(2);
        assertEquals(expResult, result);

    }

    /**
     * Test of set method, of class SegmentedOasisList.
     */
    @Test
    public void testSet() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        Collection c = Arrays.asList(1, 1, 7, 1, 1, 1, 1);
        instance.addAll(c);
        Integer expResult = 7;
        Integer result = instance.set(2, 9);
        assertEquals(expResult, result);

    }

    /**
     * Test of set method, of class SegmentedOasisList.
     */
    @Test
    public void testSet_Order() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        Collection c = Arrays.asList(1, 1, 7, 1, 1, 1, 1);
        instance.addAll(c);

        instance.set(2, 9);

        List<Integer> expResult = Arrays.asList(1, 1, 9, 1, 1, 1, 1);

        assertTrue(isEqual(expResult, instance));

    }

    /**
     * Test of set method, of class SegmentedOasisList.
     */
    @Test
    public void testSet_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        Collection c = Arrays.asList(1, 1, 7, 1, 1, 1, 1);
        instance.addAll(c);
        Integer expResult = 7;
        Integer result = instance.set(2, 9);
        assertEquals(expResult, result);

    }

    /**
     * Test of set method, of class SegmentedOasisList.
     */
    @Test
    public void testSet_Order_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        Collection c = Arrays.asList(1, 1, 7, 1, 1, 1, 1);
        instance.addAll(c);

        instance.set(2, 9);

        List<Integer> expResult = Arrays.asList(1, 1, 9, 1, 1, 1, 1);

        assertTrue(isEqual(expResult, instance));

    }

    /**
     * Test of add method, of class SegmentedOasisList.
     */
    @Test
    public void testAdd_int_GenericType() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        Collection c = Arrays.asList(1, 1, 7, 1, 1, 1, 1);
        instance.addAll(c);
        int expResult = 8;
        instance.add(2, 9);
        assertEquals(expResult, instance.size());
    }

    /**
     * Test of add method, of class SegmentedOasisList.
     */
    @Test
    public void testAdd_int_GenericType_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        Collection c = Arrays.asList(1, 1, 7, 1, 1, 1, 1);
        instance.addAll(c);
        int expResult = 8;
        instance.add(2, 9);
        assertEquals(expResult, instance.size());
    }

    /**
     * Test of add method, of class SegmentedOasisList.
     */
    @Test
    public void testAdd_int_GenericType_Order() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        Collection c = Arrays.asList(1, 1, 7, 1, 1, 1, 1);
        instance.addAll(c);

        instance.add(2, 9);
        List<Integer> expResult = Arrays.asList(1, 1, 9, 7, 1, 1, 1, 1);

        assertTrue(isEqual(expResult, instance));
    }

    /**
     * Test of add method, of class SegmentedOasisList.
     */
    @Test
    public void testAdd_int_GenericType_Order_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        Collection c = Arrays.asList(1, 1, 7, 1, 1, 1, 1);
        instance.addAll(c);

        instance.add(2, 9);
        List<Integer> expResult = Arrays.asList(1, 1, 9, 7, 1, 1, 1, 1);

        assertTrue(isEqual(expResult, instance));
    }

    /**
     * Test of remove method, of class SegmentedOasisList.
     */
    @Test
    public void testRemove_int() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        Collection c = Arrays.asList(1, 1, 7, 1, 1, 1, 1);
        instance.addAll(c);

        Integer expResult = 7;
        Integer result = instance.remove(2);

        assertEquals(expResult, result);

    }

    /**
     * Test of remove method, of class SegmentedOasisList.
     */
    @Test
    public void testRemove_int_Size() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        Collection c = Arrays.asList(1, 1, 7, 1, 1, 1, 1);
        instance.addAll(c);

        instance.remove(2);

        Integer expResult = 6;
        Integer result = instance.size();

        assertEquals(expResult, result);

    }

    /**
     * Test of remove method, of class SegmentedOasisList.
     */
    @Test
    public void testRemove_int_Order() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        Collection c = Arrays.asList(1, 1, 7, 1, 1, 1, 1);
        instance.addAll(c);

        instance.remove(2);

        List<Integer> expResult = Arrays.asList(1, 1, 1, 1, 1, 1);

        assertTrue(isEqual(expResult, instance));

    }

    /**
     * Test of remove method, of class SegmentedOasisList.
     */
    @Test
    public void testRemove_int_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        Collection c = Arrays.asList(1, 1, 7, 1, 1, 1, 1);
        instance.addAll(c);

        Integer expResult = 7;
        Integer result = instance.remove(2);

        assertEquals(expResult, result);

    }

    /**
     * Test of remove method, of class SegmentedOasisList.
     */
    @Test
    public void testRemove_int_Size_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        Collection c = Arrays.asList(1, 1, 7, 1, 1, 1, 1);
        instance.addAll(c);

        instance.remove(2);

        Integer expResult = 6;
        Integer result = instance.size();

        assertEquals(expResult, result);

    }

    /**
     * Test of remove method, of class SegmentedOasisList.
     */
    @Test
    public void testRemove_int_Order_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        Collection c = Arrays.asList(1, 1, 7, 1, 1, 1, 1);
        instance.addAll(c);

        instance.remove(2);

        List<Integer> expResult = Arrays.asList(1, 1, 1, 1, 1, 1);

        assertTrue(isEqual(expResult, instance));

    }

    /**
     * Test of indexOf method, of class SegmentedOasisList.
     */
    @Test
    public void testIndexOf() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1);
        instance.addAll(c);
        Integer arg = 7;

        int result = instance.indexOf(arg);
        int expResult = 2;

        assertEquals(expResult, result);

    }

    /**
     * Test of indexOf method, of class SegmentedOasisList.
     */
    @Test
    public void testIndexOf_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1);
        instance.addAll(c);
        Integer arg = 7;

        int result = instance.indexOf(arg);
        int expResult = 2;

        assertEquals(expResult, result);

    }

    /**
     * Test of lastIndexOf method, of class SegmentedOasisList.
     */
    @Test
    public void testLastIndexOf() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1);
        instance.addAll(c);
        Integer arg = 7;

        int result = instance.lastIndexOf(arg);
        int expResult = 3;

        assertEquals(expResult, result);

    }

    /**
     * Test of lastIndexOf method, of class SegmentedOasisList.
     */
    @Test
    public void testLastIndexOf_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1);
        instance.addAll(c);
        Integer arg = 7;

        int result = instance.lastIndexOf(arg);
        int expResult = 3;

        assertEquals(expResult, result);

    }

    /**
     * Test of listIterator method, of class SegmentedOasisList.
     */
    @Test
    public void testListIterator() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 6, 8));

        ListIterator<Integer> instance = baseList.listIterator();
        assertTrue(isEqual(instance, baseList));

    }

    /**
     * Test of listIterator method, of class SegmentedOasisList.
     */
    @Test
    public void testListIterator_Overflow() {
        OasisList<Integer> baseList = new SegmentedOasisList<>(2, 2);
        baseList.addAll(Arrays.asList(1, 2, 6, 8, 4, 9, 67, 87));

        ListIterator<Integer> instance = baseList.listIterator();
        assertTrue(isEqual(instance, baseList));

    }

    /**
     * Test of listIterator method, of class SegmentedOasisList.
     */
    @Test
    public void testListIterator_valid_index_From_Start() {
        int index = 0;
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 6, 8));

        ListIterator<Integer> instance = baseList.listIterator(index);
        assertTrue(isEqual(instance, baseList));

    }

    /**
     * Test of listIterator method, of class SegmentedOasisList.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testListIterator_invalid_index_negative() {
        int index = -1;
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 6, 8));

        baseList.listIterator(index);
    }

    /**
     * Test of listIterator method, of class SegmentedOasisList.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testListIterator_Invalid_Index_Size() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 6, 8));

        baseList.listIterator(baseList.size());
    }

    /**
     * Test of listIterator method, of class SegmentedOasisList.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testListIterator_Invalid_Index_Above_Size() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 6, 8));

        baseList.listIterator(baseList.size() + 1);
    }

    /**
     * Test of subList method, of class SegmentedOasisList.
     */
    @Test
    public void testSubList() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1);
        instance.addAll(c);

        List expResult = Arrays.asList(1, 7, 7);
        OasisList<Integer> result = (OasisList<Integer>) instance.subList(1, 4);

        assertTrue(isEqual(expResult, result));

    }

    /**
     * Test of subList method, of class SegmentedOasisList.
     */
    @Test
    public void testSubList_SubList_Item_Update() {
        SegmentedOasisList<StringBuilder> instance = new SegmentedOasisList<>();

        instance.add(new StringBuilder("1"));
        instance.add(new StringBuilder("1"));
        instance.add(new StringBuilder("7"));
        instance.add(new StringBuilder("7"));
        instance.add(new StringBuilder("1"));
        instance.add(new StringBuilder("1"));

        List<StringBuilder> expResult = Arrays.asList(new StringBuilder("17"), new StringBuilder("7"),
                new StringBuilder("7"));
        List<StringBuilder> result = instance.subList(1, 4);

        instance.get(1).append("7");
        assertTrue(isEqual(expResult, result));
    }

    /**
     * Test of subList method, of class SegmentedOasisList.
     */
    @Test
    public void testSubList_SubList_Item_Update_idempotent() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1);
        instance.addAll(c);

        List<Integer> expResult = Arrays.asList(1, 7, 7);
        SegmentedOasisList<Integer> result = (SegmentedOasisList<Integer>) instance.subList(1, 4);

        instance.set(3, 8);

        assertTrue(isEqual(expResult, result));

    }

    /**
     * Test of subList method, of class SegmentedOasisList.
     */
    @Test
    public void testSubList_SubList_Item_Added() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1);
        instance.addAll(c);

        List<Integer> result = (SegmentedOasisList<Integer>) instance.subList(1, 4);
        result.add(8);

        List<Integer> expResult = Arrays.asList(1, 1, 7, 7, 1, 1, 1);

        assertTrue(isEqual(expResult, instance));

    }

    /**
     * Test of subList method, of class SegmentedOasisList.
     */
    @Test
    public void testSubList_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1);
        instance.addAll(c);

        List expResult = Arrays.asList(1, 7, 7);
        OasisList<Integer> result = (OasisList<Integer>) instance.subList(1, 4);

        assertTrue(isEqual(expResult, result));

    }

    /**
     * Test of subList method, of class SegmentedOasisList.
     */
    @Test
    public void testSubList_SubList_Item_Update_Overflow() {
        SegmentedOasisList<StringBuilder> instance = new SegmentedOasisList<>(2, 2);

        instance.add(new StringBuilder("1"));
        instance.add(new StringBuilder("1"));
        instance.add(new StringBuilder("7"));
        instance.add(new StringBuilder("7"));
        instance.add(new StringBuilder("1"));
        instance.add(new StringBuilder("1"));

        List<StringBuilder> expResult = Arrays.asList(new StringBuilder("17"), new StringBuilder("7"),
                new StringBuilder("7"));
        List<StringBuilder> result = instance.subList(1, 4);

        instance.get(1).append("7");
        assertTrue(isEqual(expResult, result));

    }

    /**
     * Test of equals, of class SegmentedOasisList.
     */
    @Test
    public void testEquals() throws Exception {

        File file = File.createTempFile("oasis-collection-test-persistence", "");
        file.mkdirs();
        file.deleteOnExit();

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1);
        instance.addAll(c);

        SegmentedOasisList<Integer> readInstance;

        try (ObjectOutputStream outObjStream = new ObjectOutputStream(new FileOutputStream(file))) {
            outObjStream.writeObject(instance);
        }

        try (ObjectInputStream inObjStream = new ObjectInputStream(new FileInputStream(file))) {
            readInstance = (SegmentedOasisList<Integer>) inObjStream.readObject();
        }

        assertEquals(instance, readInstance);

    }

    /**
     * Test of equals, of class SegmentedOasisList.
     */
    @Test
    public void testEquals_Overflow() throws Exception {

        File file = File.createTempFile("oasis-collection-test-persistence", "");
        file.mkdirs();
        file.deleteOnExit();

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1);
        instance.addAll(c);

        SegmentedOasisList<Integer> readInstance;

        try (ObjectOutputStream outObjStream = new ObjectOutputStream(new FileOutputStream(file))) {
            outObjStream.writeObject(instance);
        }

        try (ObjectInputStream inObjStream = new ObjectInputStream(new FileInputStream(file))) {
            readInstance = (SegmentedOasisList<Integer>) inObjStream.readObject();
        }

        assertEquals(instance, readInstance);

    }

    /**
     * Test of persistedSegmentCount method, of class SegmentedOasisList.
     */
    @Test
    public void testPersistedSegmentCount() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1);
        instance.addAll(c);

        int expResult = 0;
        int result = instance.persistedSegmentCount();
        assertEquals(expResult, result);

    }

    /**
     * Test of persistedSegmentCount method, of class SegmentedOasisList.
     */
    @Test
    public void testPersistedSegmentCount_Overflow() {
        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1);
        instance.addAll(c);

        int expResult = 2;
        int result = instance.persistedSegmentCount();
        assertEquals(expResult, result);

    }

    /**
     * Test of Constructor, of class SegmentedOasisList.
     */
    @Test
    public void testConstructor_Custom_Store_File() {

        File file = new File(
                System.getProperty("java.io.tmpdir") + File.separator + "oasis-list-testConstructor_Custom_Store_File");
        file.mkdirs();
        file.deleteOnExit();

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(file);
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1);
        instance.addAll(c);

        File[] subDirs = file.listFiles();

        assertNotNull(subDirs);
    }

    /**
     * Test of Constructor, of class SegmentedOasisList.
     */
    @Test
    public void testConstructor_Custom_Store_File_Overflow() {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator
                + "oasis-list-testConstructor_Custom_Store_File_Overflow");
        file.mkdirs();
        file.deleteOnExit();

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(file);
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1);
        instance.addAll(c);

        File[] subDirs = file.listFiles();

        assertNotNull(subDirs);
    }

    /**
     * Test of Constructor, of class SegmentedOasisList.
     */
    @Test
    public void testConstructor_Custom_Store_String() {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator
                + "oasis-collection-testConstructor_Custom_Store_String");
        file.mkdirs();
        file.deleteOnExit();

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(file.getAbsolutePath());
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1);
        instance.addAll(c);

        File[] subDirs = file.listFiles();

        assertNotNull(subDirs);

    }

    /**
     * Test of Constructor, of class SegmentedOasisList.
     */
    @Test
    public void testConstructor_Custom_Store_String_Overflow() {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "oasis-collection-test-2");
        file.mkdirs();
        file.deleteOnExit();

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2, file.getAbsolutePath());
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1);
        instance.addAll(c);

        File[] subDirs = file.listFiles();

        assertNotNull(subDirs);

    }

    /**
     * Test of Persistence, of class SegmentedOasisList.
     */
    @Test
    public void testPersistence_size() throws Exception {

        File file = File.createTempFile("oasis-collection-test-persistence", "");
        file.mkdirs();
        file.deleteOnExit();

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>();
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1);
        instance.addAll(c);

        SegmentedOasisList<Integer> readInstance;

        try (ObjectOutputStream outObjStream = new ObjectOutputStream(new FileOutputStream(file))) {
            outObjStream.writeObject(instance);
        }

        try (ObjectInputStream inObjStream = new ObjectInputStream(new FileInputStream(file))) {
            readInstance = (SegmentedOasisList<Integer>) inObjStream.readObject();
        }

        assertEquals(instance.size(), readInstance.size());

    }

    /**
     * Test of Persistence, of class SegmentedOasisList.
     */
    @Test
    public void testPersistence_size_Overflow() throws Exception {

        File file = File.createTempFile("oasis-collection-test-persistence", "");
        file.mkdirs();
        file.deleteOnExit();

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1);
        instance.addAll(c);

        SegmentedOasisList<Integer> readInstance;

        try (ObjectOutputStream outObjStream = new ObjectOutputStream(new FileOutputStream(file))) {
            outObjStream.writeObject(instance);
        }

        try (ObjectInputStream inObjStream = new ObjectInputStream(new FileInputStream(file))) {
            readInstance = (SegmentedOasisList<Integer>) inObjStream.readObject();
        }

        assertEquals(instance.size(), readInstance.size());

    }

    /**
     * Test of Persistence, of class SegmentedOasisList.
     */
    @Test
    public void testPersistence_Elements_Overflow() throws Exception {

        File file = File.createTempFile("oasis-collection-test-persistence", "");
        file.mkdirs();
        file.deleteOnExit();

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2);
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1);
        instance.addAll(c);

        SegmentedOasisList<Integer> readInstance;

        try (ObjectOutputStream outObjStream = new ObjectOutputStream(new FileOutputStream(file))) {
            outObjStream.writeObject(instance);
        }

        try (ObjectInputStream inObjStream = new ObjectInputStream(new FileInputStream(file))) {
            readInstance = (SegmentedOasisList<Integer>) inObjStream.readObject();
        }

        assertEquals(instance, readInstance);

    }

    /**
     * Test of Persistence, of class SegmentedOasisList.
     */
    @Test
    public void testPersistence_Custom_Store() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator
                + "oasis-collection-testPersistence_Custom_Store");
        file.mkdirs();
        file.deleteOnExit();

        File saveToFile = File.createTempFile("oasis-collection-test-persistence", "");
        file.mkdirs();
        file.deleteOnExit();

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(file);
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1);
        instance.addAll(c);

        SegmentedOasisList<Integer> readInstance;

        try (ObjectOutputStream outObjStream = new ObjectOutputStream(new FileOutputStream(saveToFile))) {
            outObjStream.writeObject(instance);
        }

        try (ObjectInputStream inObjStream = new ObjectInputStream(new FileInputStream(saveToFile))) {
            readInstance = (SegmentedOasisList<Integer>) inObjStream.readObject();
        }

        assertEquals(instance, readInstance);

    }

    /**
     * Test of Persistence, of class SegmentedOasisList.
     */
    @Test
    public void testPersistence_Custom_Store_Overflow() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator
                + "oasis-collection-testPersistence_Custom_Store_Overflow");
        file.mkdirs();
        file.deleteOnExit();

        File saveToFile = File.createTempFile("oasis-collection-test-persistence", "");
        file.mkdirs();
        file.deleteOnExit();

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2, file);
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1);
        instance.addAll(c);

        SegmentedOasisList<Integer> readInstance;

        try (ObjectOutputStream outObjStream = new ObjectOutputStream(new FileOutputStream(saveToFile))) {
            outObjStream.writeObject(instance);
        }

        try (ObjectInputStream inObjStream = new ObjectInputStream(new FileInputStream(saveToFile))) {
            readInstance = (SegmentedOasisList<Integer>) inObjStream.readObject();
        }

        assertEquals(instance, readInstance);

    }

    /**
     * Test of compact, of class SegmentedOasisList.
     */
    @Test
    public void testCompact_Overflow_Caching_Enabled() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator
                + "oasis-collection-testCompact_Overflow_Caching_Enabled");
        file.mkdirs();
        file.deleteOnExit();

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2, file);
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1, 1);
        instance.enableCache();
        instance.addAll(c);

        instance.remove(2);
        instance.compact();
        instance.disableCache();

        assertNumberFilesInDirectory(instance.instanceStore(), 2);

    }

    /**
     * Test of compact, of class SegmentedOasisList.
     */
    @Test
    public void testCompact_Overflow_Caching_Disabled() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator
                + "oasis-collection-testCompact_Overflow_Caching_Disabled");
        file.mkdirs();
        file.deleteOnExit();

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2, file);
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1, 1);
        instance.addAll(c);

        instance.remove(2);
        instance.compact();

        assertNumberFilesInDirectory(instance.instanceStore(), 2);
    }

    /**
     * Test of compact, of class SegmentedOasisList.
     */
    @Test
    public void testCompactFast_One_Sgement_Removed() throws Exception {

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(3, 2);

        // 2 persisted segment counts
        Collection c = Arrays.asList(1, 1, 2, 3, 4, 5, 6, 7);
        instance.addAll(c);

        // remove 2 items form memory before compacting fast
        instance.remove(1);
        instance.remove(1);

        instance.compactFast();

        assertEquals(1, instance.persistedSegmentCount());

    }

    /**
     * Test of compact, of class SegmentedOasisList.
     */
    @Test
    public void testCompactFast_Muliple_Sgements_Removed() throws Exception {

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(3, 1);

        // 1 persisted segment counts
        Collection c = Arrays.asList(1, 1, 2, 3, 4, 5);
        instance.addAll(c);

        // remove 2 items form memory before compacting fast
        instance.remove(0);
        instance.remove(0);

        instance.compactFast();

        assertEquals(0, instance.persistedSegmentCount());

    }

    
    /**
     * Test of compact, of class SegmentedOasisList.
     */
    @Test
    public void testCompactFast_Sgement_Removed_Item_Order() throws Exception {

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(3, 1);

        // 1 persisted segment counts
        List c = Arrays.asList(1, 1, 2, 3, 4, 5);
        List expected = c.subList(2, c.size());
        instance.addAll(c);

        // remove 2 items form memory before compacting fast
        instance.remove(0);
        instance.remove(0);

        instance.compactFast();

        assertEquals(expected, instance.subList(0, instance.size()));

    }

    /**
     * Test of compact, of class SegmentedOasisList.
     */
    @Test
    public void testCompactFast_Sgement_Not_Removed() throws Exception {

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(3, 2);

        // 2 persisted segment counts
        Collection c = Arrays.asList(1, 1, 2, 3, 4, 5, 6, 7);
        instance.addAll(c);

        // remove 2 items form memory before compacting fast
        instance.remove(1);

        instance.compactFast();

        assertEquals(2, instance.persistedSegmentCount());

    }


    /**
     * Test of compact, of class SegmentedOasisList.
     */
    @Test
    public void testCompact_Last_Segment_Caching_Enabled() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator
                + "oasis-collection-testCompact_Last_Segment_Caching_Enabled");
        file.mkdirs();
        file.deleteOnExit();

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2, file);
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1, 1);
        instance.enableCache();
        instance.addAll(c);

        instance.remove(7);
        instance.disableCache();

        assertNumberFilesInDirectory(instance.instanceStore(), 2);

    }

    /**
     * Test of compact, of class SegmentedOasisList.
     */
    @Test
    public void testCompact_Last_Segment_Caching_Disabled() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator
                + "oasis-collection-testCompact_Last_Segment_Caching_Disabled");
        file.mkdirs();
        file.deleteOnExit();

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2, file);
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1, 1);
        instance.addAll(c);

        instance.remove(7);
        instance.compact();

        assertNumberFilesInDirectory(instance.instanceStore(), 2);
    }

    /**
     * Test of compact, of class SegmentedOasisList.
     */
    @Test
    public void testCompact_Last_Segment_Emptied_Caching_Enabled() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator
                + "oasis-collection-testCompact_Last_Segment_Emptied_Caching_Enabled");
        file.mkdirs();
        file.deleteOnExit();

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2, file);
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1, 1);
        instance.enableCache();
        instance.addAll(c);

        instance.remove(6);
        instance.remove(6);
        instance.compact();
        instance.disableCache();

        assertNumberFilesInDirectory(instance.instanceStore(), 2);

    }

    /**
     * Test of compact, of class SegmentedOasisList.
     */
    @Test
    // @Ignore
    public void testCompact_Last_Segment_Emptied_Caching_Disabled() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator
                + "oasis-collection-testCompact_Last_Segment_Emptied_Caching_Disabled");
        file.mkdirs();
        file.deleteOnExit();

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2, file);
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1, 1);
        instance.addAll(c);

        instance.remove(6);
        instance.remove(6);
        instance.compact();

        assertEquals(2, instance.persistedSegmentCount());

    }

    /**
     * Test of compact, of class SegmentedOasisList.
     */
    @Test
    public void testCompact_Mid_Segment_Caching_Enabled() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator
                + "oasis-collection-testCompact_Last_Segment_Caching_Enabled");
        file.mkdirs();
        file.deleteOnExit();

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2, file);
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1, 1);
        instance.enableCache();
        instance.addAll(c);

        instance.remove(5);
        instance.compact();
        instance.disableCache();

        assertNumberFilesInDirectory(instance.instanceStore(), 2);

    }

    /**
     * Test of compact, of class SegmentedOasisList.
     */
    @Test
    public void testCompact_Mid_Segment_Caching_Disabled() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator
                + "oasis-collection-testCompact_Last_Segment_Caching_Disabled");
        file.mkdirs();
        file.deleteOnExit();

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2, file);
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1, 1);
        instance.addAll(c);

        instance.remove(5);
        instance.compact();

        assertNumberFilesInDirectory(instance.instanceStore(), 2);
    }

    /**
     * Test of compact, of class SegmentedOasisList.
     */
    @Test
    public void testCompact_Mid_Segment_Emptied_Caching_Enabled() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator
                + "oasis-collection-testCompact_Last_Segment_Emptied_Caching_Enabled");
        file.mkdirs();
        file.deleteOnExit();

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2, file);
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1, 1);
        instance.enableCache();
        instance.addAll(c);

        instance.remove(4);
        instance.remove(5);
        instance.compact();
        instance.disableCache();

        assertNumberFilesInDirectory(instance.instanceStore(), 2);

    }

    /**
     * Test of compact, of class SegmentedOasisList.
     */
    @Test
    public void testCompact_Mid_Segment_Emptied_Caching_Disabled() throws Exception {

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator
                + "oasis-collection-testCompact_Last_Segment_Emptied_Caching_Disabled");
        file.mkdirs();
        file.deleteOnExit();

        SegmentedOasisList<Integer> instance = new SegmentedOasisList<>(2, 2, file);
        Collection c = Arrays.asList(1, 1, 7, 7, 1, 1, 1, 1);

        instance.addAll(c);

        instance.remove(4);
        instance.remove(5);
        instance.compact();

        assertEquals(2, instance.persistedSegmentCount());

    }

    // ====================== Iterator Test =========================
    /**
     * Test of hasNext method, of class oasisListIterator.
     */
    @Test
    public void testHasNext_Start() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 3, 4, 5));

        ListIterator<Integer> instance = baseList.listIterator();
        boolean expResult = true;
        boolean result = instance.hasNext();

        assertEquals(expResult, result);
    }

    /**
     * Test of hasNext method, of class oasisListIterator.
     */
    @Test
    public void testHasNext_End() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2));

        ListIterator<Integer> instance = baseList.listIterator();
        boolean expResult = false;
        instance.next();
        instance.next();

        boolean result = instance.hasNext();

        assertEquals(expResult, result);
    }

    /**
     * Test of hasNext method, of class oasisListIterator.
     */
    @Test
    public void testHasNext_middle() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 3, 4));

        ListIterator<Integer> instance = baseList.listIterator();
        boolean expResult = true;
        instance.next();

        boolean result = instance.hasNext();

        assertEquals(expResult, result);
    }

    /**
     * Test of next method, of class oasisListIterator.
     */
    @Test
    public void testNext_Start() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 3, 4, 5));

        ListIterator<Integer> instance = baseList.listIterator();
        Integer expResult = 1;
        Integer result = instance.next();
        assertEquals(expResult, result);
    }

    /**
     * Test of next method, of class oasisListIterator.
     */
    @Test(expected = NoSuchElementException.class)
    public void testNext_End() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2));

        ListIterator<Integer> instance = baseList.listIterator();
        instance.next();
        instance.next();
        instance.next();
    }

    /**
     * Test of next method, of class oasisListIterator.
     */
    @Test
    public void testNext_Middle() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 4, 6));

        ListIterator<Integer> instance = baseList.listIterator();
        instance.next();
        instance.next();

        Integer expResult = 4;
        Integer result = instance.next();
        assertEquals(expResult, result);
    }

    /**
     * Test of hasPrevious method, of class oasisListIterator.
     */
    @Test
    public void testHasPrevious_Start() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 3, 4, 5));

        ListIterator<Integer> instance = baseList.listIterator();
        boolean expResult = false;
        boolean result = instance.hasPrevious();

        assertEquals(expResult, result);
    }

    /**
     * Test of hasPrevious method, of class oasisListIterator.
     */
    @Test
    public void testHasPrevious_End() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2));

        ListIterator<Integer> instance = baseList.listIterator();

        instance.next();
        instance.next();

        boolean expResult = true;
        boolean result = instance.hasPrevious();

        assertEquals(expResult, result);
    }

    /**
     * Test of hasPrevious method, of class oasisListIterator.
     */
    @Test
    public void testHasPrevious_Middle() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 3, 4, 5));

        ListIterator<Integer> instance = baseList.listIterator();

        instance.next();
        instance.next();

        boolean expResult = true;
        boolean result = instance.hasPrevious();

        assertEquals(expResult, result);
    }

    /**
     * Test of previous method, of class oasisListIterator.
     */
    @Test(expected = NoSuchElementException.class)
    public void testPrevious_Start() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 5, 8));

        ListIterator<Integer> instance = baseList.listIterator();
        instance.previous();
    }

    /**
     * Test of previous method, of class oasisListIterator.
     */
    @Test
    public void testPrevious_End() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2));

        ListIterator<Integer> instance = baseList.listIterator();
        instance.next();
        instance.next();

        Integer expResult = 2;
        Integer result = instance.previous();
        assertEquals(expResult, result);
    }

    /**
     * Test of previous method, of class oasisListIterator.
     */
    @Test
    public void testPrevious_Middle() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 5, 8));

        ListIterator<Integer> instance = baseList.listIterator();
        instance.next();
        instance.next();
        instance.next();

        Integer expResult = 5;
        Integer result = instance.previous();
        assertEquals(expResult, result);
    }

    /**
     * Test of nextIndex method, of class oasisListIterator.
     */
    @Test
    public void testNextIndex_Start() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 3, 4, 5));

        ListIterator<Integer> instance = baseList.listIterator();
        int expResult = 0;
        int result = instance.nextIndex();

        assertEquals(expResult, result);
    }

    /**
     * Test of nextIndex method, of class oasisListIterator.
     */
    @Test
    public void testNextIndex_End() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 3));

        ListIterator<Integer> instance = baseList.listIterator();
        instance.next();
        instance.next();

        int expResult = 2;
        int result = instance.nextIndex();

        assertEquals("Calling nextIndex at the end of the iterator should return size of the associated list",
                expResult, result);
    }

    /**
     * Test of nextIndex method, of class oasisListIterator.
     */
    @Test
    public void testNextIndex_Middle() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 3, 4, 5));

        ListIterator<Integer> instance = baseList.listIterator();
        instance.next();
        instance.next();

        int expResult = 2;
        int result = instance.nextIndex();

        assertEquals(expResult, result);
    }

    /**
     * Test of previousIndex method, of class oasisListIterator.
     */
    @Test
    public void testPreviousIndex_Start() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 3, 4, 5));

        ListIterator<Integer> instance = baseList.listIterator();
        int expResult = -1;
        int result = instance.previousIndex();

        assertEquals(expResult, result);
    }

    /**
     * Test of previousIndex method, of class oasisListIterator.
     */
    @Test
    public void testPreviousIndex_End() {

        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2));

        ListIterator<Integer> instance = baseList.listIterator();
        instance.next();
        instance.next();

        int expResult = 1;
        int result = instance.previousIndex();

        assertEquals(expResult, result);
    }

    /**
     * Test of previousIndex method, of class oasisListIterator.
     */
    @Test
    public void testPreviousIndex_Middle() {

        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 6, 8));

        ListIterator<Integer> instance = baseList.listIterator();
        instance.next();
        instance.next();

        int expResult = 1;
        int result = instance.previousIndex();

        assertEquals(expResult, result);
    }

    /**
     * Test of remove method, of class oasisListIterator.
     */
    @Test
    public void testRemove_After_Next() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 6, 8));

        ListIterator<Integer> instance = baseList.listIterator();
        instance.next();
        instance.next();

        instance.remove();

        assertFalse(baseList.contains(2));
    }

    /**
     * Test of remove method, of class oasisListIterator.
     */
    @Test
    public void testRemove_After_Previous() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 6, 8));

        ListIterator<Integer> instance = baseList.listIterator();
        instance.next();
        instance.next();
        instance.previous();

        instance.remove();

        assertFalse(baseList.contains(2));
    }

    /**
     * Test of remove method, of class oasisListIterator.
     */
    @Test(expected = IllegalStateException.class)
    public void testRemove_After_Add() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 6, 8));

        ListIterator<Integer> instance = baseList.listIterator();
        instance.next();
        instance.add(2);

        instance.remove();

    }

    /**
     * Test of remove method, of class oasisListIterator.
     */
    @Test(expected = IllegalStateException.class)
    public void testRemove_After_Set() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 6, 8));

        ListIterator<Integer> instance = baseList.listIterator();
        instance.next();
        instance.set(2);

        instance.remove();

    }

    /**
     * Test of remove method, of class oasisListIterator.
     */
    @Test(expected = IllegalStateException.class)
    public void testRemove_After_Remove() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 6, 8));

        ListIterator<Integer> instance = baseList.listIterator();
        instance.next();
        instance.remove();

        instance.remove();

    }

    /**
     * Test of set method, of class oasisListIterator.
     */
    @Test
    public void testSet_After_Next() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 6, 8));

        ListIterator<Integer> instance = baseList.listIterator();
        instance.next();
        instance.next();

        instance.set(9);
        int expResult = 1;

        assertEquals(expResult, baseList.indexOf(9));
    }

    /**
     * Test of set method, of class oasisListIterator.
     */
    @Test
    public void testSet_After_Previous() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 6, 8));

        ListIterator<Integer> instance = baseList.listIterator();
        instance.next();
        instance.next();
        instance.previous();

        instance.set(9);
        int expResult = 1;

        assertEquals(expResult, baseList.indexOf(9));
    }

    /**
     * Test of set method, of class oasisListIterator.
     */
    @Test(expected = IllegalStateException.class)
    public void testSet_After_Add() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 6, 8));

        ListIterator<Integer> instance = baseList.listIterator();
        instance.next();
        instance.next();
        instance.add(3);

        instance.set(2);

    }

    /**
     * Test of set method, of class oasisListIterator.
     */
    @Test(expected = IllegalStateException.class)
    public void testSet_After_Set() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 6, 8));

        ListIterator<Integer> instance = baseList.listIterator();
        instance.next();
        instance.next();
        instance.set(3);

        instance.set(2);

    }

    /**
     * Test of set method, of class oasisListIterator.
     */
    @Test(expected = IllegalStateException.class)
    public void testSet_After_Remove() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 6, 8));

        ListIterator<Integer> instance = baseList.listIterator();
        instance.next();
        instance.next();
        instance.remove();

        instance.set(2);

    }

    /**
     * Test of add method, of class oasisListIterator.
     */
    @Test
    public void testAdd_Size() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 6, 8));

        ListIterator<Integer> instance = baseList.listIterator();
        instance.next();
        instance.next();

        instance.add(9);
        int expResult = 5;

        assertEquals(expResult, baseList.size());
    }

    /**
     * Test of add method, of class oasisListIterator.
     */
    @Test
    public void testAdd_After_Next() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 6, 8));

        ListIterator<Integer> instance = baseList.listIterator();
        instance.next();
        instance.next();

        instance.add(9);
        int expResult = 2;

        assertEquals(expResult, baseList.indexOf(9));
    }

    /**
     * Test of add method, of class oasisListIterator.
     */
    @Test
    public void testAdd_After_Previous() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 6, 8));

        ListIterator<Integer> instance = baseList.listIterator();
        instance.next();
        instance.next();
        instance.previous();

        instance.add(9);
        int expResult = 1;

        assertEquals(expResult, baseList.indexOf(9));
    }

    /**
     * Test of add method, of class oasisListIterator.
     */
    @Test
    public void testAdd_After_Add() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 6, 8));

        ListIterator<Integer> instance = baseList.listIterator();
        instance.next();
        instance.next();

        instance.add(3);
        instance.add(9);
        int expResult = 3;

        assertEquals(expResult, baseList.indexOf(9));
    }

    /**
     * Test of add method, of class oasisListIterator.
     */
    @Test
    public void testAdd_After_Set() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 6, 8));

        ListIterator<Integer> instance = baseList.listIterator();
        instance.next();
        instance.next();
        ;

        instance.set(3);
        instance.add(9);
        int expResult = 2;

        assertEquals(expResult, baseList.indexOf(9));
    }

    /**
     * Test of add method, of class oasisListIterator.
     */
    @Test
    public void testAdd_After_Remove() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 6, 8));

        ListIterator<Integer> instance = baseList.listIterator();
        instance.next();
        instance.next();
        ;

        instance.remove();
        instance.add(9);
        int expResult = 1;

        assertEquals(expResult, baseList.indexOf(9));
    }

    /**
     * Test of add method, of class oasisListIterator.
     */
    @Test
    public void testIterator_Forward() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 6, 8));

        ListIterator<Integer> instance = baseList.listIterator();

        int i = 0;
        while (instance.hasNext()) {
            assertEquals(baseList.get(i++), instance.next());
        }

    }

    /**
     * Test of add method, of class oasisListIterator.
     */
    @Test
    public void testIterator_Backward() {
        OasisList<Integer> baseList = new SegmentedOasisList<>();
        baseList.addAll(Arrays.asList(1, 2, 6, 8));

        ListIterator<Integer> instance = baseList.listIterator();

        int i = 0;
        while (instance.hasNext()) {
            instance.next();
        }

        i = baseList.size() - 1;
        while (instance.hasPrevious()) {
            assertEquals(baseList.get(i--), instance.previous());
        }

    }

    // =========== End of Iterator Tests ==========================
    private boolean isEqual(Iterator<?> instance, OasisList<?> baseList) {

        boolean equal = true;
        int i = 0;
        while (instance.hasNext()) {
            if (!baseList.get(i++).equals(instance.next())) {
                equal = false;
                break;
            }
        }

        return equal;
    }

    private boolean isEqual(List<?> expected, OasisList<?> result) {

        boolean equal = true;
        for (int i = 0; i < expected.size(); i++) {

            if (!expected.get(i).equals(result.get(i))) {
                return false;
            }
        }

        return equal;
    }

    private boolean isEqual(List<StringBuilder> expected, List<StringBuilder> result) {

        boolean equal = true;
        for (int i = 0; i < expected.size(); i++) {

            if (!expected.get(i).toString().equals(result.get(i).toString())) {
                return false;
            }
        }

        return equal;
    }

    private void assertNumberFilesInDirectory(String dir, int fileCount) {
        assertEquals(fileCount, new File(dir).listFiles().length);
    }
}
