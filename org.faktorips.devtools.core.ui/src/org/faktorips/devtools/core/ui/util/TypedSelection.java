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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.faktorips.util.ArgumentCheck;

/**
 * Type-safe wrapper for {@link ISelection} instances. Ensures that
 * <ul>
 * <li>the selection is a structured selection</li>
 * <li>the number of elements in the selection is within the specified range</li>
 * <li>the selection contains only elements of the specified type <b>T</b>.
 * </ul>
 * Several convenience methods are available to provide a type-safe access to the elements of the
 * selection. Additionally, several factory methods will simplify the creation of
 * {@link TypedSelection} instances.
 * <p>
 * Instances of this class are immutable and do not update if the selection changes.
 * </p>
 * 
 * @param <T> type of the selection
 */
public class TypedSelection<T> {
    /** Arbitrary number of elements allowed. */
    public static final int INFINITY = Integer.MAX_VALUE;

    /** The actual elements of this selection. */
    private List<T> elements;
    /** Defines whether this selection is valid. */
    private final boolean isValidSelection;
    /** Name of the type in the selection. */
    private String typeName = "undefined"; //$NON-NLS-1$

    /**
     * Creates a validator that validates <code>false</code> for any type of selection.
     * 
     * @param <T> the type of the selection
     * @return the created instance
     */
    public static <T> TypedSelection<T> createNullValidator() {
        return new TypedSelection<T>();
    }

    /**
     * Creates a validator that validates <code>true</code> for any type of selection.
     * 
     * @param <T> the type of the selection
     * @return the created instance
     */
    public static <T> TypedSelection<T> createTrueValidator() {
        return new TypedSelection<T>(true);
    }

    /**
     * Creates a validator that validates <code>false</code> for any type of selection.
     * 
     * @param <T> the type of the selection
     * @return the created instance
     */
    public static <T> TypedSelection<T> createFalseValidator() {
        return new TypedSelection<T>();
    }

    /**
     * Creates a new instance of {@link TypedSelection}. The selection is valid if exactly one
     * element of the given type is in the selection.
     * 
     * @param <T> type of the selection
     * @param type the expected type of the selection (must be the same as the type parameter
     *            <b>T</b>)
     * @param selection the selection to validate
     * @return the created instance
     */
    public static <T> TypedSelection<T> create(final Class<T> type, final ISelection selection) {
        return new TypedSelection<T>(type, selection);
    }

    /**
     * Creates a new instance of {@link TypedSelection}. The selection is valid if at least one
     * element of the given type is in the selection.
     * 
     * @param <T> type of the selection
     * @param type the expected type of the selection (must be the same as the type parameter
     *            <b>T</b>)
     * @param selection the selection to validate
     * @return the created instance
     */
    public static <T> TypedSelection<T> createAnyCount(final Class<T> type, final ISelection selection) {
        return new TypedSelection<T>(type, selection, 1, INFINITY);
    }

    /**
     * Creates a new instance of {@link TypedSelection}. The selection is valid if at least the
     * specified number of elements of the given type is in the selection.
     * 
     * @param <T> type of the selection
     * @param type the expected type of the selection (must be the same as the type parameter
     *            <b>T</b>)
     * @param selection the selection to validate
     * @param minElements the minimum number of elements in the selection
     * @return the created instance
     */
    public static <T> TypedSelection<T> createAtLeast(final Class<T> type,
            final ISelection selection,
            final int minElements) {
        return new TypedSelection<T>(type, selection, minElements, INFINITY);
    }

    /**
     * Creates a new instance of {@link TypedSelection}. The selection is valid if it exactly
     * contains the specified number of elements.
     * 
     * @param <T> type of the selection
     * @param type the expected type of the selection (must be the same as the type parameter
     *            <b>T</b>)
     * @param selection the selection to validate
     * @param elements the number of elements that must be in the selection
     * @return the created instance
     */
    public static <T> TypedSelection<T> create(final Class<T> type, final ISelection selection, final int elements) {
        return new TypedSelection<T>(type, selection, elements);
    }

    /**
     * Creates a new instance of {@link TypedSelection}.
     * 
     * @param <T> type of the selection
     * @param type the expected type of the selection (must be the same as the type parameter
     *            <b>T</b>)
     * @param selection the selection to validate
     * @param minElements the minimum number of elements in the selection
     * @param maxElements the maximum number of elements in the selection
     * @return the created instance
     */
    public static <T> TypedSelection<T> create(final Class<T> type,
            final ISelection selection,
            final int minElements,
            final int maxElements) {
        return new TypedSelection<T>(type, selection, minElements, maxElements);
    }

    /**
     * Compares the specified {@link ISelection} with the given elements of a model collection. Both
     * collections are considered equal if they contain the same elements (ordering is not
     * relevant).
     * 
     * @param selection the selection to compare
     * @param collection the collection containing the model elements
     * @return <code>true</code> if both collections are equal
     */
    public static boolean isEqual(final ISelection selection, final Collection<?> collection) {
        if (selection instanceof StructuredSelection) {
            StructuredSelection structuredSelection = (StructuredSelection)selection;
            Set<Object> providerElements = new HashSet<Object>(Arrays.asList(structuredSelection.toArray()));
            Set<Object> modelElements = new HashSet<Object>(collection);
            return providerElements.equals(modelElements);
        }
        return false;
    }

    /**
     * Compares the specified {@link ISelection} with the given elements of a model collection. Both
     * collections are considered equal if they contain the same elements (ordering is not
     * relevant).
     * 
     * @param selection the selection to compare
     * @param collection the collection containing the model elements
     * @return <code>true</code> if both collections are equal
     */
    public static boolean isNotEqual(final ISelection selection, final Collection<?> collection) {
        return !isEqual(selection, collection);
    }

    /**
     * Converts the specified {@link ISelection} to a collection of the specified type. If the
     * selection contains elements of the wrong type or is empty then an
     * <code>AssertionFailedException</code> is thrown.
     * 
     * @param <T> type of the selection
     * @param type the expected type of the selection (must be the same as the type parameter
     *            <b>T</b>)
     * @param selection the selection to validate
     * @return the selection as an collection
     */
    public static <T> Collection<T> convert(final Class<T> type, final ISelection selection) {
        TypedSelection<T> validator = createAnyCount(type, selection);
        failIfNotValid(validator);

        return validator.getElements();
    }

    /**
     * Converts the specified {@link ISelection} to an element of the specified type. If the
     * selection contains elements of the wrong type or does not contain exactly one element then an
     * <code>AssertionFailedException</code> is thrown.
     * 
     * @param <T> type of the selection
     * @param type the expected type of the selection (must be the same as the type parameter
     *            <b>T</b>)
     * @param selection the selection to validate
     * @return the selection as an collection
     */
    public static <T> T convertSingleElement(final Class<T> type, final ISelection selection) {
        TypedSelection<T> validator = create(type, selection);
        failIfNotValid(validator);

        return validator.getElement();
    }

    private static void failIfNotValid(final TypedSelection<?> validator) {
        ArgumentCheck.isTrue(validator.isValid(), "Selection is not valid."); //$NON-NLS-1$
    }

    /**
     * Creates a new instance of {@link TypedSelection}. The selection is valid if exactly one
     * element of the given type is in the selection.
     * 
     * @param type the expected type of the selection (must be the same as the type parameter
     *            <b>T</b>)
     * @param selection the selection to validate
     */
    public TypedSelection(final Class<T> type, final ISelection selection) {
        this(type, selection, 1, 1);
    }

    /**
     * Creates a new instance of {@link TypedSelection}. The selection is valid if it exactly
     * contains the specified number of elements.
     * 
     * @param type the expected type of the selection (must be the same as the type parameter
     *            <b>T</b>)
     * @param selection the selection to validate
     * @param elements the number of elements that must be in the selection
     */
    public TypedSelection(final Class<T> type, final ISelection selection, final int elements) {
        this(type, selection, elements, elements);
    }

    /**
     * Creates a new instance of {@link TypedSelection}. The selection is never valid.
     */
    private TypedSelection() {
        this(false);
    }

    /**
     * Creates a new instance of {@link TypedSelection}. The selection is never valid.
     * 
     * @param isValid determines whether the selection is valid
     */
    private TypedSelection(final boolean isValid) {
        isValidSelection = isValid;
        elements = Collections.emptyList();
    }

    /**
     * Creates a new instance of {@link TypedSelection}.
     * 
     * @param type the expected type of the selection (must be the same as the type parameter
     *            <b>T</b>)
     * @param selection the selection to validate
     * @param minElements the minimum number of elements in the selection
     * @param maxElements the maximum number of elements in the selection
     */
    public TypedSelection(final Class<T> type, final ISelection selection, final int minElements, final int maxElements) {
        ArgumentCheck.notNull(type);
        ArgumentCheck.isTrue(minElements > 0, "minElements must be positive: " + minElements); //$NON-NLS-1$
        ArgumentCheck.isTrue(minElements <= maxElements, "minElements <= maxElements: " + minElements + " <= " //$NON-NLS-1$ //$NON-NLS-2$
                + maxElements);

        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)selection;

            elements = new ArrayList<T>(structuredSelection.size());
            boolean wrongType = false;
            for (Iterator<?> iterator = structuredSelection.iterator(); iterator.hasNext();) {
                Object element = iterator.next();
                if (type.isInstance(element)) {
                    elements.add(type.cast(element));
                } else {
                    wrongType = true;
                }
            }
            isValidSelection = !wrongType && (minElements <= elements.size()) && (elements.size() <= maxElements);
            typeName = type.getCanonicalName();
        } else {
            isValidSelection = false;
        }
    }

    private void failIfNotValid() {
        failIfNotValid(this);
    }

    /**
     * Returns whether this selection is valid.
     * 
     * @return <code>true</code> if this selection is valid, <code>false</code> otherwise
     */
    public final boolean isValid() {
        return isValidSelection;
    }

    /**
     * Returns the element of this single selection.
     * 
     * @return the first element of this single selection.
     */
    public final T getElement() {
        failIfNotValid();
        ArgumentCheck.isTrue(elements.size() == 1,
                "getElement should be used for single selections only, but size is " + elements.size()); //$NON-NLS-1$

        return getFirstElement();
    }

    /**
     * Returns the first element of this selection.
     * 
     * @return the first element of this selection.
     */
    public final T getFirstElement() {
        failIfNotValid();

        return elements.get(0);
    }

    /**
     * Returns the second element of this selection.
     * 
     * @return the second element of this selection.
     */
    public final T getSecondElement() {
        failIfNotValid();
        ArgumentCheck.isTrue(elements.size() >= 2, "Can't extract second element of a selection of size < 2."); //$NON-NLS-1$

        return elements.get(1);
    }

    /**
     * Returns the contents of this selection as an unmodifiable list with "read-only" access.
     * Attempts to modify this list, whether direct or via its iterator, result in an
     * {@link UnsupportedOperationException}.
     * 
     * @return the contents of this selection
     */
    public final Collection<T> getElements() {
        failIfNotValid();

        return Collections.unmodifiableList(elements);
    }

    /**
     * Returns the number of elements in this selection.
     * 
     * @return the number of elements in this selection.
     */
    public final int getElementCount() {
        failIfNotValid();

        return elements.size();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        if (isValidSelection) {
            return "Valid selection of " + getElementCount() + " elements of type " + typeName; //$NON-NLS-1$//$NON-NLS-2$
        } else {
            return "Invalid selection."; //$NON-NLS-1$
        }
    }
}
