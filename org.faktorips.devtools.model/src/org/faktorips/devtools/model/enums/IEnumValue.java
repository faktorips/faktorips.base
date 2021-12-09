/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.enums;

import java.util.List;
import java.util.NoSuchElementException;

import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.value.IValue;

/**
 * An <code>IEnumValue</code> represents a complete set of <code>IEnumAttributeValue</code>s for an
 * <code>IEnumType</code>. <code>IEnumValue</code>s are always contained in an
 * <code>IEnumValueContainer</code>.
 * 
 * @see org.faktorips.devtools.model.enums.IEnumType
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
     * Validation message code to indicate that there are not as many
     * <code>IEnumAttributeValue</code>s as <code>IEnumAttribute</code>s in the
     * <code>IEnumType</code>.
     */
    public static final String MSGCODE_ENUM_VALUE_NUMBER_ATTRIBUTE_VALUES_DOES_NOT_CORRESPOND_TO_NUMBER_ATTRIBUTES = MSGCODE_PREFIX
            + "EnumValueNumberAttributeValuesDoesNotCorrespondToNumberAttributes"; //$NON-NLS-1$

    /** Returns a list containing all <code>IEnumAttributeValue</code>s. */
    public List<IEnumAttributeValue> getEnumAttributeValues();

    /**
     * Creates a new <code>IEnumAttributeValue</code> and returns it.
     * 
     * @throws CoreRuntimeException If the <code>IEnumType</code> this <code>IEnumValue</code> is based
     *             upon cannot be found.
     */
    public IEnumAttributeValue newEnumAttributeValue() throws CoreRuntimeException;

    /**
     * Creates a new <code>IEnumLiteralNameAttributeValue</code> and returns it.
     */
    public IEnumLiteralNameAttributeValue newEnumLiteralNameAttributeValue();

    /**
     * Returns the <code>IEnumValueContainer</code> this <code>IEnumValue</code> is being stored in.
     * <p>
     * This is a shortcut for: <code>(IEnumValueContainer)this.getParent();</code>
     */
    public IEnumValueContainer getEnumValueContainer();

    /**
     * Searches and returns the <code>IEnumAttributeValue</code> that refers to the given
     * <code>IEnumAttribute</code>.
     * <p>
     * Returns <code>null</code> if none can be found or if the provided <code>IEnumAttribute</code>
     * is <code>null</code>.
     * <p>
     * <strong>Important:</strong> For performance reasons this information is obtained trough the
     * index of the provided <code>IEnumAttribute</code>. There is no check if the given
     * <code>IEnumAttribute</code> really is part of the <code>IEnumType</code> referenced by the
     * <code>IEnumValueContainer</code>. Clients have to care to only pass
     * <code>IEnumAttribute</code>s that are really part of the referenced <code>IEnumType</code>.
     * 
     * @param enumAttribute The <code>IEnumAttribute</code> to obtain the
     *            <code>IEnumAttributeValue</code> for.
     */
    public IEnumAttributeValue getEnumAttributeValue(IEnumAttribute enumAttribute);

    /**
     * Returns how many <code>IEnumAttributeValue</code>s this <code>IEnumValue</code> is currently
     * containing.
     */
    public int getEnumAttributeValuesCount();

    /**
     * Moves the given <code>IEnumAttributeValue</code> up or down by 1 and returns its new index.
     * <p>
     * If the <code>IEnumAttributeValue</code> is already the first / last one then nothing will be
     * done.
     * 
     * @param enumAttributeValue The <code>IEnumAttributeValue</code> that is to be moved.
     * @param up Flag indicating whether to move upwards (<code>true</code>) or downwards (
     *            <code>false</code>).
     * 
     * @throws NullPointerException If <code>enumAttributeValue</code> is <code>null</code>.
     * @throws NoSuchElementException If the given <code>IEnumAttributeValue</code> is not a part of
     *             this <code>IEnumValue</code>.
     */
    public int moveEnumAttributeValue(IEnumAttributeValue enumAttributeValue, boolean up);

    /**
     * Swaps the position of two EnumAttributeValues
     */
    public void swapEnumAttributeValue(int firstColumnIndex, int secondColumnIndex);

    /**
     * Sets the value of the <code>IEnumAttributeValue</code> that refers to the given
     * <code>IEnumAttribute</code>.
     * <p>
     * This version of <code>setEnumAttributeValue</code> offers best performance.
     * 
     * @see #setEnumAttributeValue(String, IValue)
     * @see #setEnumAttributeValue(int, IValue)
     * 
     * @param enumAttribute The <code>IEnumAttribute</code> for that the value shall be set.
     * @param value The new value. May also be <code>null</code>.
     * 
     * @throws CoreRuntimeException If an error occurs while searching for the
     *             <code>IEnumAttributeValue</code> that refers to the given
     *             <code>IEnumAttribute</code>.
     * @throws NullPointerException If <code>enumAttribute</code> is <code>null</code>.
     */
    public void setEnumAttributeValue(IEnumAttribute enumAttribute, IValue<?> value) throws CoreRuntimeException;

    /**
     * Sets the value of the <code>IEnumAttributeValue</code> that refers to the
     * <code>IEnumAttribute</code> identified by the given name.
     * 
     * @param enumAttributeName The name of the <code>IEnumAttribute</code> for that the value shall
     *            be set.
     * @param value The new value. May also be <code>null</code>.
     * 
     * @see #setEnumAttributeValue(IEnumAttribute, IValue)
     * @see #setEnumAttributeValue(int, IValue)
     * 
     * @throws CoreRuntimeException If an error occurs while searching for the <code>IEnumAttribute</code>
     *             identified by the given name or while searching for the
     *             <code>IEnumAttributeValue</code> that refers to the <code>IEnumAttribute</code>.
     * @throws NullPointerException If <code>enumAttributeName</code> is <code>null</code>.
     * @throws NoSuchElementException If there is no <code>IEnumAttribute</code> with the given name
     *             in the parent <code>IEnumType</code>.
     */
    public void setEnumAttributeValue(String enumAttributeName, IValue<?> value) throws CoreRuntimeException;

    /**
     * Sets the value of the <code>IEnumAttributeValue</code> identified by the given index.
     * <p>
     * <strong>Attention:</strong> Use this operation only if you must because the ordering of the
     * <code>IEnumAttributeValue</code>s changes often.
     * 
     * @see #setEnumAttributeValue(IEnumAttribute, IValue)
     * @see #setEnumAttributeValue(String, IValue)
     * 
     * @param enumAttributeValueIndex The index of the <code>IEnumAttributeValue</code> which value
     *            shall be set.
     * @param value The new value. May also be <code>null</code>.
     * 
     * @throws IndexOutOfBoundsException If the given index is out of bounds.
     */
    public void setEnumAttributeValue(int enumAttributeValueIndex, IValue<?> value);

    /**
     * Returns a list containing all <code>IEnumAttributeValue</code>s that refer to the given
     * unique <code>IEnumAttribute</code>s.
     * <p>
     * Returns an empty list if none could be found (either none exist or it was not possible to
     * find the referenced <code>IEnumType</code>). Never returns <code>null</code>.
     * 
     * @param uniqueEnumAttributes A list containing all <code>IEnumAttribute</code>s for which the
     *            <code>IEnumAttributeValue</code>s shall be returned.
     * @param ipsProject The IPS project which IPS object path is used for the search of the
     *            referenced <code>IEnumType</code>. This is not necessarily the project this
     *            <code>IEnumValue</code> is part of.
     * 
     * @throws CoreRuntimeException If an error occurs while searching for the referenced
     *             <code>IEnumType</code>.
     * @throws NullPointerException If any parameter is <code>null</code>.
     */
    public List<IEnumAttributeValue> findUniqueEnumAttributeValues(List<IEnumAttribute> uniqueEnumAttributes,
            IIpsProject ipsProject) throws CoreRuntimeException;

    /**
     * Returns the index of the given <code>IEnumAttributeValue</code> in this
     * <code>IEnumValue</code> or -1 if the given <code>IEnumAttributeValue</code> does not exist in
     * this <code>IEnumValue</code>.
     * 
     * @param enumAttributeValue The <code>IEnumAttributeValue</code> to obtain its index for.
     * 
     * @throws NullPointerException If <code>enumAttributeValue</code> is <code>null</code>.
     */
    public int getIndexOfEnumAttributeValue(IEnumAttributeValue enumAttributeValue);

    /**
     * Returns the <code>IEnumAttributeValue</code> referencing the
     * <code>IEnumLiteralNameAttribute</code> or <code>null</code> if there exists none or this
     * <code>IEnumValue</code> is part of an <code>IEnumContent</code>.
     */
    public IEnumLiteralNameAttributeValue getEnumLiteralNameAttributeValue();

}
