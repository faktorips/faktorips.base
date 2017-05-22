/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.enums;

import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.value.IValue;

/**
 * An <tt>IEnumValue</tt> represents a complete set of <tt>IEnumAttributeValue</tt>s for an
 * <tt>IEnumType</tt>. <tt>IEnumValue</tt>s are always contained in an <tt>IEnumValueContainer</tt>.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumType
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumValue extends IIpsObjectPart, IDescribedElement {

    /** The XML tag for this IPS object part. */
    public static final String XML_TAG = "EnumValue"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    public static final String MSGCODE_PREFIX = "ENUMVALUE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there are not as many <tt>IEnumAttributeValue</tt>s
     * as <tt>IEnumAttribute</tt>s in the <tt>IEnumType</tt>.
     */
    public static final String MSGCODE_ENUM_VALUE_NUMBER_ATTRIBUTE_VALUES_DOES_NOT_CORRESPOND_TO_NUMBER_ATTRIBUTES = MSGCODE_PREFIX
            + "EnumValueNumberAttributeValuesDoesNotCorrespondToNumberAttributes"; //$NON-NLS-1$

    /** Returns a list containing all <tt>IEnumAttributeValue</tt>s. */
    public List<IEnumAttributeValue> getEnumAttributeValues();

    /**
     * Creates a new <tt>IEnumAttributeValue</tt> and returns it.
     * 
     * @throws CoreException If the <tt>IEnumType</tt> this <tt>IEnumValue</tt> is based upon cannot
     *             be found.
     */
    public IEnumAttributeValue newEnumAttributeValue() throws CoreException;

    /**
     * Creates a new <tt>IEnumLiteralNameAttributeValue</tt> and returns it.
     */
    public IEnumLiteralNameAttributeValue newEnumLiteralNameAttributeValue();

    /**
     * Returns the <tt>IEnumValueContainer</tt> this <tt>IEnumValue</tt> is being stored in.
     * <p>
     * This is a shortcut for: <tt>(IEnumValueContainer)this.getParent();</tt>
     */
    public IEnumValueContainer getEnumValueContainer();

    /**
     * Searches and returns the <tt>IEnumAttributeValue</tt> that refers to the given
     * <tt>IEnumAttribute</tt>.
     * <p>
     * Returns <tt>null</tt> if none can be found or if the provided <tt>IEnumAttribute</tt> is
     * <tt>null</tt>.
     * <p>
     * <strong>Important:</strong> For performance reasons this information is obtained trough the
     * index of the provided <tt>IEnumAttribute</tt>. There is no check if the given
     * <tt>IEnumAttribute</tt> really is part of the <tt>IEnumType</tt> referenced by the
     * <tt>IEnumValueContainer</tt>. Clients have to care to only pass <tt>IEnumAttribute</tt>s that
     * are really part of the referenced <tt>IEnumType</tt>.
     * 
     * @param enumAttribute The <tt>IEnumAttribute</tt> to obtain the <tt>IEnumAttributeValue</tt>
     *            for.
     */
    public IEnumAttributeValue getEnumAttributeValue(IEnumAttribute enumAttribute);

    /**
     * Returns how many <tt>IEnumAttributeValue</tt>s this <tt>IEnumValue</tt> is currently
     * containing.
     */
    public int getEnumAttributeValuesCount();

    /**
     * Moves the given <tt>IEnumAttributeValue</tt> up or down by 1 and returns its new index.
     * <p>
     * If the <tt>IEnumAttributeValue</tt> is already the first / last one then nothing will be
     * done.
     * 
     * @param enumAttributeValue The <tt>IEnumAttributeValue</tt> that is to be moved.
     * @param up Flag indicating whether to move upwards (<tt>true</tt>) or downwards (
     *            <tt>false</tt>).
     * 
     * @throws NullPointerException If <tt>enumAttributeValue</tt> is <tt>null</tt>.
     * @throws NoSuchElementException If the given <tt>IEnumAttributeValue</tt> is not a part of
     *             this <tt>IEnumValue</tt>.
     */
    public int moveEnumAttributeValue(IEnumAttributeValue enumAttributeValue, boolean up);

    /**
     * Swaps the position of two EnumAttributeValues
     */
    public void swapEnumAttributeValue(int firstColumnIndex, int secondColumnIndex);

    /**
     * Sets the value of the <tt>IEnumAttributeValue</tt> that refers to the given
     * <tt>IEnumAttribute</tt>.
     * <p>
     * This version of <tt>setEnumAttributeValue</tt> offers best performance.
     * 
     * @see #setEnumAttributeValue(String, IValue)
     * @see #setEnumAttributeValue(int, IValue)
     * 
     * @param enumAttribute The <tt>IEnumAttribute</tt> for that the value shall be set.
     * @param value The new value. May also be <tt>null</tt>.
     * 
     * @throws CoreException If an error occurs while searching for the <tt>IEnumAttributeValue</tt>
     *             that refers to the given <tt>IEnumAttribute</tt>.
     * @throws NullPointerException If <tt>enumAttribute</tt> is <tt>null</tt>.
     */
    public void setEnumAttributeValue(IEnumAttribute enumAttribute, IValue<?> value) throws CoreException;

    /**
     * Sets the value of the <tt>IEnumAttributeValue</tt> that refers to the <tt>IEnumAttribute</tt>
     * identified by the given name.
     * 
     * @param enumAttributeName The name of the <tt>IEnumAttribute</tt> for that the value shall be
     *            set.
     * @param value The new value. May also be <tt>null</tt>.
     * 
     * @see #setEnumAttributeValue(IEnumAttribute, IValue)
     * @see #setEnumAttributeValue(int, IValue)
     * 
     * @throws CoreException If an error occurs while searching for the <tt>IEnumAttribute</tt>
     *             identified by the given name or while searching for the
     *             <tt>IEnumAttributeValue</tt> that refers to the <tt>IEnumAttribute</tt>.
     * @throws NullPointerException If <tt>enumAttributeName</tt> is <tt>null</tt>.
     * @throws NoSuchElementException If there is no <tt>IEnumAttribute</tt> with the given name in
     *             the parent <tt>IEnumType</tt>.
     */
    public void setEnumAttributeValue(String enumAttributeName, IValue<?> value) throws CoreException;

    /**
     * Sets the value of the <tt>IEnumAttributeValue</tt> identified by the given index.
     * <p>
     * <strong>Attention:</strong> Use this operation only if you must because the ordering of the
     * <tt>IEnumAttributeValue</tt>s changes often.
     * 
     * @see #setEnumAttributeValue(IEnumAttribute, IValue)
     * @see #setEnumAttributeValue(String, IValue)
     * 
     * @param enumAttributeValueIndex The index of the <tt>IEnumAttributeValue</tt> which value
     *            shall be set.
     * @param value The new value. May also be <tt>null</tt>.
     * 
     * @throws IndexOutOfBoundsException If the given index is out of bounds.
     */
    public void setEnumAttributeValue(int enumAttributeValueIndex, IValue<?> value);

    /**
     * Returns a list containing all <tt>IEnumAttributeValue</tt>s that refer to the given unique
     * <tt>IEnumAttribute</tt>s.
     * <p>
     * Returns an empty list if none could be found (either none exist or it was not possible to
     * find the referenced <tt>IEnumType</tt>). Never returns <tt>null</tt>.
     * 
     * @param uniqueEnumAttributes A list containing all <tt>IEnumAttribute</tt>s for which the
     *            <tt>IEnumAttributeValue</tt>s shall be returned.
     * @param ipsProject The IPS project which IPS object path is used for the search of the
     *            referenced <tt>IEnumType</tt>. This is not necessarily the project this
     *            <tt>IEnumValue</tt> is part of.
     * 
     * @throws CoreException If an error occurs while searching for the referenced
     *             <tt>IEnumType</tt>.
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    public List<IEnumAttributeValue> findUniqueEnumAttributeValues(List<IEnumAttribute> uniqueEnumAttributes,
            IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the index of the given <tt>IEnumAttributeValue</tt> in this <tt>IEnumValue</tt> or -1
     * if the given <tt>IEnumAttributeValue</tt> does not exist in this <tt>IEnumValue</tt>.
     * 
     * @param enumAttributeValue The <tt>IEnumAttributeValue</tt> to obtain its index for.
     * 
     * @throws NullPointerException If <tt>enumAttributeValue</tt> is <tt>null</tt>.
     */
    public int getIndexOfEnumAttributeValue(IEnumAttributeValue enumAttributeValue);

    /**
     * Returns the <tt>IEnumAttributeValue</tt> referencing the <tt>IEnumLiteralNameAttribute</tt>
     * or <tt>null</tt> if there exists none or this <tt>IEnumValue</tt> is part of an
     * <tt>IEnumContent</tt>.
     */
    public IEnumLiteralNameAttributeValue getEnumLiteralNameAttributeValue();

}
