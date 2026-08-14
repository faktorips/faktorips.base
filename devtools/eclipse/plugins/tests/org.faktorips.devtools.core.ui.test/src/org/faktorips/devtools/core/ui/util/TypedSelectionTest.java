/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.junit.jupiter.api.Test;

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
    /** Error message. */
    private static final String UNEXPECTED_ELEMENT_IN_SELECTION = "Unexpected element in selection."; //$NON-NLS-1$
    /** Error message. */
    private static final String NO_ELEMENT_IN_SELECTION = "No element in selection."; //$NON-NLS-1$
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

        assertFalse(validator.isValid(), WRONG_VALID_STATE_EVALUATION);
    }

    /**
     * Checks whether we correctly identify a selection of String elements (any count).
     *
     * @param selectionInput the selection to validate
     */
    public void assertAnyCountInSelection(final String[] selectionInput) {
        StructuredSelection selection = new StructuredSelection(selectionInput);

        TypedSelection<String> validator = TypedSelection.create(String.class, selection, 1, TypedSelection.INFINITY);
        assertTrue(validator.isValid(), WRONG_VALID_STATE_EVALUATION);

        validator = TypedSelection.createAnyCount(String.class, selection);
        assertTrue(validator.isValid(), WRONG_VALID_STATE_EVALUATION);
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
        assertTrue(validator.isValid(), WRONG_VALID_STATE_EVALUATION);

        assertEquals(TEST_STRING, validator.getFirstElement(), WRONG_ELEMENT_IN_SELECTION);
        assertEquals(SECOND_TEST_STRING, validator.getSecondElement(), WRONG_ELEMENT_IN_SELECTION);

        Collection<String> elements = validator.getElements();
        assertEquals(2, elements.size(), WRONG_NUMBER_OF_ELEMENTS);
        assertEquals(2, validator.getElementCount(), WRONG_NUMBER_OF_ELEMENTS);
        Iterator<String> iterator = elements.iterator();
        assertEquals(TEST_STRING, iterator.next(), WRONG_ELEMENT_IN_SELECTION);
        assertEquals(SECOND_TEST_STRING, iterator.next(), WRONG_ELEMENT_IN_SELECTION);
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
        assertTrue(validator.isValid(), WRONG_VALID_STATE_EVALUATION);

        assertEquals(TEST_STRING, validator.getElement(), WRONG_ELEMENT_IN_SELECTION);

        Collection<String> elements = validator.getElements();
        assertEquals(1, elements.size(), WRONG_NUMBER_OF_ELEMENTS);
        assertEquals(1, validator.getElementCount(), WRONG_NUMBER_OF_ELEMENTS);
        assertEquals(TEST_STRING, elements.iterator().next(), WRONG_ELEMENT_IN_SELECTION);
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
        assertTrue(validator.isValid(), WRONG_VALID_STATE_EVALUATION);

        assertEquals(INTEGER_VALUE, validator.getFirstElement(), WRONG_ELEMENT_IN_SELECTION);
        assertEquals(LONG_VALUE, validator.getSecondElement(), WRONG_ELEMENT_IN_SELECTION);
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
        assertFalse(validator.isValid(), WRONG_VALID_STATE_EVALUATION);

        selection = new StructuredSelection(new String[] { TEST_STRING });
        validator = new TypedSelection<>(String.class, selection, 2, 2);
        assertFalse(validator.isValid(), WRONG_VALID_STATE_EVALUATION);

        selection = new StructuredSelection(new Object[] { TEST_STRING, TEST_STRING, INTEGER_VALUE });
        validator = new TypedSelection<>(String.class, selection, 2, 2);
        assertFalse(validator.isValid(), WRONG_VALID_STATE_EVALUATION);

        selection = new StructuredSelection(new Object[] { TEST_STRING, INTEGER_VALUE });
        validator = new TypedSelection<>(String.class, selection, 2, 2);
        assertFalse(validator.isValid(), WRONG_VALID_STATE_EVALUATION);

        selection = new StructuredSelection(new Object[] { INTEGER_VALUE, INTEGER_VALUE });
        validator = new TypedSelection<>(String.class, selection, 2, 2);
        assertFalse(validator.isValid(), WRONG_VALID_STATE_EVALUATION);

        selection = new StructuredSelection(new String[] { TEST_STRING, TEST_STRING, TEST_STRING });
        validator = new TypedSelection<>(String.class, selection, 2, 2);
        assertFalse(validator.isValid(), WRONG_VALID_STATE_EVALUATION);

        validator = new TypedSelection<>(String.class, new TextSelection(0, 0), 1);
        assertFalse(validator.isValid(), WRONG_VALID_STATE_EVALUATION);
    }

    /**
     * Checks the null validator.
     */
    @Test
    public void testNullValidator() {
        TypedSelection<Object> validator = TypedSelection.create(Object.class,
                new StructuredSelection(Collections.emptyList()));

        assertFalse(validator.isValid(), WRONG_VALID_STATE_EVALUATION);
    }

    /**
     * Checks that the validator only accepts count &ge; 0.
     */
    @Test
    public void testMinimum() {
        boolean isThrown = false;
        try {
            TypedSelection.create(String.class, new StructuredSelection(), -1);
        } catch (IllegalArgumentException exception) {
            isThrown = true;
        }
        assertTrue(isThrown, NO_EXCEPTION_IS_THROWN);

        isThrown = false;
        try {
            TypedSelection.create(String.class, new StructuredSelection(), -1, TypedSelection.INFINITY);
        } catch (IllegalArgumentException exception) {
            isThrown = true;
        }
        assertTrue(isThrown, NO_EXCEPTION_IS_THROWN);
    }

    /**
     * Checks whether the equal method correctly compares selection providers with model
     * collections.
     */
    @Test
    public void testEqual() {
        StructuredSelection selection = new StructuredSelection();

        List<String> model = new ArrayList<>();

        model.add(TEST_STRING);

        assertFalse(TypedSelection.isEqual(selection, model), WRONG_EQUALS_EVALUATION);
        assertTrue(TypedSelection.isNotEqual(selection, model), WRONG_EQUALS_EVALUATION);

        model.remove(TEST_STRING);

        assertTrue(TypedSelection.isEqual(selection, model), WRONG_EQUALS_EVALUATION);
        assertFalse(TypedSelection.isNotEqual(selection, model), WRONG_EQUALS_EVALUATION);

        model.add(TEST_STRING);
        model.add(NO_EXCEPTION_IS_THROWN);

        List<String> selectionModel = new ArrayList<>();
        selectionModel.add(NO_EXCEPTION_IS_THROWN);
        selectionModel.add(TEST_STRING);

        assertFalse(model.equals(selectionModel), WRONG_EQUALS_EVALUATION);
        assertTrue(TypedSelection.isEqual(new StructuredSelection(selectionModel), model), WRONG_EQUALS_EVALUATION);
    }

    /**
     * Checks whether we could convert a selection to a collection.
     */
    @Test
    public void testConversion() {
        List<String> selectionModel = new ArrayList<>();
        selectionModel.add(TEST_STRING);
        StructuredSelection selection = new StructuredSelection(selectionModel);

        Collection<String> collection = TypedSelection.convert(String.class, selection);

        assertEquals(1, collection.size(), WRONG_NUMBER_OF_ELEMENTS);
        assertEquals(TEST_STRING, collection.iterator().next(), WRONG_ELEMENT_IN_SELECTION);

        selectionModel.add(NO_EXCEPTION_IS_THROWN);

        selection = new StructuredSelection(selectionModel);
        collection = TypedSelection.convert(String.class, selection);

        assertEquals(2, collection.size(), WRONG_NUMBER_OF_ELEMENTS);
    }

    /**
     * Checks whether we could convert a selection to a single element.
     */
    @Test
    public void testSingleElementConversion() {
        List<String> selectionModel = new ArrayList<>();
        selectionModel.add(TEST_STRING);
        StructuredSelection selection = new StructuredSelection(selectionModel);

        assertEquals(TEST_STRING,
                TypedSelection.convertSingleElement(String.class, selection), WRONG_ELEMENT_IN_SELECTION);
    }

    @Test
    public void testSingleElement() {
        ISelection emptySelection = new StructuredSelection(Collections.emptyList());
        assertFalse(TypedSelection.singleElement(String.class, emptySelection)
                .isPresent(), UNEXPECTED_ELEMENT_IN_SELECTION);
        assertFalse(TypedSelection.singleElement(Integer.class, emptySelection)
                .isPresent(), UNEXPECTED_ELEMENT_IN_SELECTION);

        StructuredSelection singleSelection = new StructuredSelection(Arrays.asList(TEST_STRING));

        assertFalse(TypedSelection.singleElement(Integer.class, singleSelection)
                .isPresent(), UNEXPECTED_ELEMENT_IN_SELECTION);
        assertTrue(TypedSelection.singleElement(String.class, singleSelection).isPresent(), NO_ELEMENT_IN_SELECTION);
        assertEquals(TEST_STRING,
                TypedSelection.singleElement(String.class, singleSelection).get(), WRONG_ELEMENT_IN_SELECTION);

        StructuredSelection multiSelection = new StructuredSelection(Arrays.asList(TEST_STRING, TEST_STRING));

        assertFalse(TypedSelection.singleElement(Integer.class, multiSelection)
                .isPresent(), UNEXPECTED_ELEMENT_IN_SELECTION);
        assertFalse(TypedSelection.singleElement(String.class, multiSelection)
                .isPresent(), UNEXPECTED_ELEMENT_IN_SELECTION);
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
        assertTrue(isThrown, NO_EXCEPTION_ON_EMPTY);
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
        assertTrue(isThrown, "No exception is thrown: Selection with two elements is ignored."); //$NON-NLS-1$
    }

    /**
     * Checks whether we throw an exception if the converted selection contains two elements.
     */
    @Test
    public void testWrongNumberOfElementsForConvertSingle() {
        boolean isThrown = false;
        try {
            List<String> selectionModel = new ArrayList<>();
            selectionModel.add(TEST_STRING);
            selectionModel.add(TEST_STRING);
            StructuredSelection selection = new StructuredSelection(selectionModel);
            TypedSelection.convertSingleElement(String.class, selection);
        } catch (IllegalArgumentException exception) {
            isThrown = true;
        }
        assertTrue(isThrown, NO_EXCEPTION_ON_WRONG_TYPE);
    }

    /**
     * Checks whether we throw an exception if the converted selection contains the wrong type.
     */
    @Test
    public void testWrongElementForConvert() {
        boolean isThrown = false;
        try {
            List<String> selectionModel = new ArrayList<>();
            selectionModel.add(TEST_STRING);
            StructuredSelection selection = new StructuredSelection(selectionModel);
            TypedSelection.convert(Boolean.class, selection);
        } catch (IllegalArgumentException exception) {
            isThrown = true;
        }
        assertTrue(isThrown, NO_EXCEPTION_ON_WRONG_TYPE);
    }

    /**
     * Checks whether we throw an exception if the converted selection is empty or contains the
     * wrong type.
     */
    @Test
    public void testWrongElementForConvertSingle() {
        boolean isThrown = false;
        try {
            List<String> selectionModel = new ArrayList<>();
            selectionModel.add(TEST_STRING);
            StructuredSelection selection = new StructuredSelection(selectionModel);
            TypedSelection.convertSingleElement(Boolean.class, selection);
        } catch (IllegalArgumentException exception) {
            isThrown = true;
        }
        assertTrue(isThrown, NO_EXCEPTION_ON_WRONG_TYPE);
    }

    /**
     * Checks whether typed selection tries to adapt the selected element to the specified type
     * using {@link IWorkbenchAdapter}.
     */
    @Test
    public void testAdaptation() {
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class, withSettings().extraInterfaces(IAdaptable.class));
        IProductCmpt prodCmpt = mock(IProductCmpt.class);
        when(ipsSrcFile.getAdapter(IProductCmpt.class)).thenReturn(prodCmpt);

        StructuredSelection selection = new StructuredSelection(List.of(ipsSrcFile));
        TypedSelection<IProductCmpt> typedSelection = TypedSelection.<IProductCmpt> create(IProductCmpt.class,
                selection);
        assertThat(typedSelection.getElement(), is(prodCmpt));
    }

    /**
     * Checks whether typed selection tries to adapt the selected element to the specified type
     * using {@link IWorkbenchAdapter}.
     */
    @Test
    public void testAdaptation_withInvalidAdaptable() {
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class, withSettings().extraInterfaces(IAdaptable.class));

        StructuredSelection selection = new StructuredSelection(List.of(ipsSrcFile));
        TypedSelection<IProductCmpt> typedSelection = TypedSelection.<IProductCmpt> create(IProductCmpt.class,
                selection);
        assertThat(typedSelection.isValid(), is(false));
    }

}
