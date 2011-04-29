/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.Test;

/**
 * Tests the class {@link TypedSelection}.
 */
public class TypedSelectionTest {
    /** Error message. */
    private static final String NO_EXCEPTION_ON_WRONG_TYPE = "No exception is thrown: Wrong element type is ignored."; //$NON-NLS-1$
    /** Error message. */
    private static final String NO_EXCEPTION_ON_EMPTY = "No exception is thrown: Empty selection is ignored."; //$NON-NLS-1$
    /** Error message. */
    private static final String WRONG_EQUALS_EVALUATION = "Wrong equals evaluation."; //$NON-NLS-1$
    /** Assertion not checked message. */
    private static final String NO_EXCEPTION_IS_THROWN = "No exception is thrown: minimum number not checked."; //$NON-NLS-1$
    /** Error message. */
    private static final String WRONG_NUMBER_OF_ELEMENTS = "Wrong number of elements in selection"; //$NON-NLS-1$
    /** Error message. */
    private static final String WRONG_ELEMENT_IN_SELECTION = "Wrong element in selection."; //$NON-NLS-1$
    /** Error message. */
    private static final String WRONG_VALID_STATE_EVALUATION = "Wrong valid state evaluation."; //$NON-NLS-1$
    /** Long value used in the tests. */
    private static final Long LONG_VALUE = Long.valueOf(121212);
    /** Integer value used in the tests. */
    private static final int INTEGER_VALUE = 42;
    /** Test string used in the tests. */
    private static final String TEST_STRING = "SELECTION-TEST"; //$NON-NLS-1$
    /** Second test string used in the tests. */
    private static final String SECOND_TEST_STRING = "SECOND-SELECTION-TEST"; //$NON-NLS-1$

    /**
     * Checks whether we correctly identify a selection of String elements (any count).
     */
    @Test
    public void testAnyCount() {
        assertAnyCountInSelection(new String[] { TEST_STRING });
        assertAnyCountInSelection(new String[] { TEST_STRING, TEST_STRING });
        assertAnyCountInSelection(new String[] { TEST_STRING, TEST_STRING, TEST_STRING, TEST_STRING });

        TypedSelection<String> validator = TypedSelection.create(String.class, new StructuredSelection(), 1,
                TypedSelection.INFINITY);
        assertFalse(WRONG_VALID_STATE_EVALUATION, validator.isValid());
    }

    /**
     * Checks whether we correctly identify a selection of String elements (any count).
     * 
     * @param selectionInput the selection to validate
     */
    public void assertAnyCountInSelection(final String[] selectionInput) {
        StructuredSelection selection = new StructuredSelection(selectionInput);

        TypedSelection<String> validator = TypedSelection.create(String.class, selection, 1, TypedSelection.INFINITY);
        assertTrue(WRONG_VALID_STATE_EVALUATION, validator.isValid());

        validator = TypedSelection.createAnyCount(String.class, selection);
        assertTrue(WRONG_VALID_STATE_EVALUATION, validator.isValid());
    }

    /**
     * Checks whether we correctly identify a selection of two elements.
     * 
     * @throws Exception in case of an unexpected error during the test
     */
    @Test
    public void testCorrectSelectionOfTwo() throws Exception {
        StructuredSelection selection = new StructuredSelection(new String[] { TEST_STRING, SECOND_TEST_STRING });
        TypedSelection<String> validator = TypedSelection.create(String.class, selection, 2);
        assertTrue(WRONG_VALID_STATE_EVALUATION, validator.isValid());

        assertEquals(WRONG_ELEMENT_IN_SELECTION, TEST_STRING, validator.getFirstElement());
        assertEquals(WRONG_ELEMENT_IN_SELECTION, SECOND_TEST_STRING, validator.getSecondElement());

        Collection<String> elements = validator.getElements();
        assertEquals(WRONG_NUMBER_OF_ELEMENTS, 2, elements.size());
        assertEquals(WRONG_NUMBER_OF_ELEMENTS, 2, validator.getElementCount());
        Iterator<String> iterator = elements.iterator();
        assertEquals(WRONG_ELEMENT_IN_SELECTION, TEST_STRING, iterator.next());
        assertEquals(WRONG_ELEMENT_IN_SELECTION, SECOND_TEST_STRING, iterator.next());
    }

    /**
     * Checks whether we correctly identify a selection of one element.
     * 
     * @throws Exception in case of an unexpected error during the test
     */
    @Test
    public void testCorrectSingleSelection() throws Exception {
        StructuredSelection selection = new StructuredSelection(new String[] { TEST_STRING });
        TypedSelection<String> validator = TypedSelection.create(String.class, selection);
        assertTrue(WRONG_VALID_STATE_EVALUATION, validator.isValid());

        assertEquals(WRONG_ELEMENT_IN_SELECTION, TEST_STRING, validator.getElement());

        Collection<String> elements = validator.getElements();
        assertEquals(WRONG_NUMBER_OF_ELEMENTS, 1, elements.size());
        assertEquals(WRONG_NUMBER_OF_ELEMENTS, 1, validator.getElementCount());
        assertEquals(WRONG_ELEMENT_IN_SELECTION, TEST_STRING, elements.iterator().next());
    }

    /**
     * Checks whether we correctly identify a selection containing sub types.
     * 
     * @throws Exception in case of an unexpected error during the test
     */
    @Test
    public void testSubTypes() throws Exception {
        StructuredSelection selection = new StructuredSelection(new Number[] { INTEGER_VALUE, LONG_VALUE });
        TypedSelection<Number> validator = TypedSelection.create(Number.class, selection, 2);
        assertTrue(WRONG_VALID_STATE_EVALUATION, validator.isValid());

        assertEquals(WRONG_ELEMENT_IN_SELECTION, INTEGER_VALUE, validator.getFirstElement());
        assertEquals(WRONG_ELEMENT_IN_SELECTION, LONG_VALUE, validator.getSecondElement());
    }

    /**
     * Checks whether we correctly decline a selection of invalid elements.
     * 
     * @throws Exception in case of an unexpected error during the test
     */
    @Test
    public void testDeclinedSelectionOfTwo() throws Exception {
        StructuredSelection selection = new StructuredSelection();

        TypedSelection<String> validator = TypedSelection.create(String.class, selection, 2, 2);
        assertFalse(WRONG_VALID_STATE_EVALUATION, validator.isValid());

        selection = new StructuredSelection(new String[] { TEST_STRING });
        validator = new TypedSelection<String>(String.class, selection, 2, 2);
        assertFalse(WRONG_VALID_STATE_EVALUATION, validator.isValid());

        selection = new StructuredSelection(new Object[] { TEST_STRING, TEST_STRING, INTEGER_VALUE });
        validator = new TypedSelection<String>(String.class, selection, 2, 2);
        assertFalse(WRONG_VALID_STATE_EVALUATION, validator.isValid());

        selection = new StructuredSelection(new Object[] { TEST_STRING, INTEGER_VALUE });
        validator = new TypedSelection<String>(String.class, selection, 2, 2);
        assertFalse(WRONG_VALID_STATE_EVALUATION, validator.isValid());

        selection = new StructuredSelection(new Object[] { INTEGER_VALUE, INTEGER_VALUE });
        validator = new TypedSelection<String>(String.class, selection, 2, 2);
        assertFalse(WRONG_VALID_STATE_EVALUATION, validator.isValid());

        selection = new StructuredSelection(new String[] { TEST_STRING, TEST_STRING, TEST_STRING });
        validator = new TypedSelection<String>(String.class, selection, 2, 2);
        assertFalse(WRONG_VALID_STATE_EVALUATION, validator.isValid());

        validator = new TypedSelection<String>(String.class, new TextSelection(0, 0), 1);
        assertFalse(WRONG_VALID_STATE_EVALUATION, validator.isValid());
    }

    /**
     * Checks the null validator.
     */
    @Test
    public void testNullValidator() {
        TypedSelection<Object> validator = TypedSelection.createNullValidator();

        assertFalse(WRONG_VALID_STATE_EVALUATION, validator.isValid());
    }

    /**
     * Checks that the validator only accepts a positive minimum count.
     */
    @Test
    public void testPositiveMinimum() {
        boolean isThrown = false;
        try {
            TypedSelection.create(String.class, new StructuredSelection(), 0);
        } catch (IllegalArgumentException exception) {
            isThrown = true;
        }
        assertTrue(NO_EXCEPTION_IS_THROWN, isThrown);

        isThrown = false;
        try {
            TypedSelection.create(String.class, new StructuredSelection(), 0, TypedSelection.INFINITY);
        } catch (IllegalArgumentException exception) {
            isThrown = true;
        }
        assertTrue(NO_EXCEPTION_IS_THROWN, isThrown);
    }

    /**
     * Checks whether the equal method correctly compares selection providers with model
     * collections.
     */
    @Test
    public void testEqual() {
        StructuredSelection selection = new StructuredSelection();

        List<String> model = new ArrayList<String>();

        model.add(TEST_STRING);

        assertFalse(WRONG_EQUALS_EVALUATION, TypedSelection.isEqual(selection, model));
        assertTrue(WRONG_EQUALS_EVALUATION, TypedSelection.isNotEqual(selection, model));

        model.remove(TEST_STRING);

        assertTrue(WRONG_EQUALS_EVALUATION, TypedSelection.isEqual(selection, model));
        assertFalse(WRONG_EQUALS_EVALUATION, TypedSelection.isNotEqual(selection, model));

        model.add(TEST_STRING);
        model.add(NO_EXCEPTION_IS_THROWN);

        List<String> selectionModel = new ArrayList<String>();
        selectionModel.add(NO_EXCEPTION_IS_THROWN);
        selectionModel.add(TEST_STRING);

        assertFalse(WRONG_EQUALS_EVALUATION, model.equals(selectionModel));
        assertTrue(WRONG_EQUALS_EVALUATION, TypedSelection.isEqual(new StructuredSelection(selectionModel), model));
    }

    /**
     * Checks whether we could convert a selection to a collection.
     */
    @Test
    public void testConversion() {
        List<String> selectionModel = new ArrayList<String>();
        selectionModel.add(TEST_STRING);
        StructuredSelection selection = new StructuredSelection(selectionModel);

        Collection<String> collection = TypedSelection.convert(String.class, selection);

        assertEquals(WRONG_NUMBER_OF_ELEMENTS, 1, collection.size());
        assertEquals(WRONG_ELEMENT_IN_SELECTION, TEST_STRING, collection.iterator().next());

        selectionModel.add(NO_EXCEPTION_IS_THROWN);

        selection = new StructuredSelection(selectionModel);
        collection = TypedSelection.convert(String.class, selection);

        assertEquals(WRONG_NUMBER_OF_ELEMENTS, 2, collection.size());
    }

    /**
     * Checks whether we could convert a selection to a single element.
     */
    @Test
    public void testSingleElementConversion() {
        List<String> selectionModel = new ArrayList<String>();
        selectionModel.add(TEST_STRING);
        StructuredSelection selection = new StructuredSelection(selectionModel);

        assertEquals(WRONG_ELEMENT_IN_SELECTION, TEST_STRING,
                TypedSelection.convertSingleElement(String.class, selection));
    }

    /**
     * Checks whether we throw an exception if the converted selection is empty.
     */
    @Test
    public void testEmptyCollectionForConvert() {
        boolean isThrown = false;
        try {
            TypedSelection.convert(String.class, new StructuredSelection());
        } catch (IllegalArgumentException exception) {
            isThrown = true;
        }
        assertTrue(NO_EXCEPTION_ON_EMPTY, isThrown);
    }

    /**
     * Checks whether we throw an exception if the converted selection is empty.
     */
    @Test
    public void testEmptyCollectionForSingleConvert() {
        boolean isThrown = false;
        try {
            TypedSelection.convertSingleElement(String.class, new StructuredSelection());
        } catch (IllegalArgumentException exception) {
            isThrown = true;
        }
        assertTrue("No exception is thrown: Selection with two elements is ignored.", isThrown); //$NON-NLS-1$
    }

    /**
     * Checks whether we throw an exception if the converted selection contains two elements.
     */
    @Test
    public void testWrongNumberOfElementsForConvertSingle() {
        boolean isThrown = false;
        try {
            List<String> selectionModel = new ArrayList<String>();
            selectionModel.add(TEST_STRING);
            selectionModel.add(TEST_STRING);
            StructuredSelection selection = new StructuredSelection(selectionModel);
            TypedSelection.convertSingleElement(String.class, selection);
        } catch (IllegalArgumentException exception) {
            isThrown = true;
        }
        assertTrue(NO_EXCEPTION_ON_WRONG_TYPE, isThrown);
    }

    /**
     * Checks whether we throw an exception if the converted selection contains the wrong type.
     */
    @Test
    public void testWrongElementForConvert() {
        boolean isThrown = false;
        try {
            List<String> selectionModel = new ArrayList<String>();
            selectionModel.add(TEST_STRING);
            StructuredSelection selection = new StructuredSelection(selectionModel);
            TypedSelection.convert(Boolean.class, selection);
        } catch (IllegalArgumentException exception) {
            isThrown = true;
        }
        assertTrue(NO_EXCEPTION_ON_WRONG_TYPE, isThrown);
    }

    /**
     * Checks whether we throw an exception if the converted selection is empty or contains the
     * wrong type.
     */
    @Test
    public void testWrongElementForConvertSingle() {
        boolean isThrown = false;
        try {
            List<String> selectionModel = new ArrayList<String>();
            selectionModel.add(TEST_STRING);
            StructuredSelection selection = new StructuredSelection(selectionModel);
            TypedSelection.convertSingleElement(Boolean.class, selection);
        } catch (IllegalArgumentException exception) {
            isThrown = true;
        }
        assertTrue(NO_EXCEPTION_ON_WRONG_TYPE, isThrown);
    }
}
