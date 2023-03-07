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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.util.DatatypeComparator;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.ArgumentCheck;

/**
 * This is an adapter for an <code>IEnumType</code> that adapts the <code>EnumDatatype</code>
 * interface.
 * 
 * @see IEnumType
 * @see org.faktorips.datatype.EnumDatatype
 * 
 * @author Peter Kuntz
 */
public class EnumTypeDatatypeAdapter implements EnumDatatype {

    private IEnumType enumType;

    private IEnumContent enumContent;

    private IEnumAttribute nameAttribute;

    /**
     * Creates a new <code>EnumTypeDatatypeAdapter</code>.
     * 
     * @param enumType The <code>IEnumType</code> that is adapted. This parameter cannot be
     *            <code>null</code>.
     * @param enumContent The <code>IEnumContent</code> that is adapted. This parameter can be
     *            <code>null</code>.
     */
    public EnumTypeDatatypeAdapter(IEnumType enumType, IEnumContent enumContent) {
        super();
        ArgumentCheck.notNull(enumType, this);
        this.enumType = enumType;
        this.enumContent = enumContent;
    }

    @Override
    public boolean isEnum() {
        return true;
    }

    /**
     * Returns the IDs of the values of adapted enumeration type respectively the enumeration
     * content. The attribute value referring to the enumeration attribute marked as identifier is
     * considered to be the ID.
     * <p>
     * Returns a string array containing only <code>null</code> as a value if the enumeration type
     * of this adapter doesn't contain values and the enumeration content of this adapter is
     * <code>null</code> and the parameter includeNull is set to true. Returns an empty string array
     * if the parameter <code>includeNull</code> is set to false.
     * <p>
     * Returns an empty string array if the identifier attribute of the adapted enumeration type has
     * not been specified.
     * 
     * @throws RuntimeException If the process of determining the <code>IEnumAttributeValue</code>s
     *             throws a <code>CoreException</code>.
     */
    @Override
    public String[] getAllValueIds(boolean includeNull) {
        List<String> result = findAllIdentifierAttributeValues(includeNull);
        return result.toArray(new String[result.size()]);
    }

    /**
     * Searches for the {@link IEnumValue} with the specified id and returns its display name. The
     * display name is the {@link IEnumAttributeValue value} of the {@link IEnumAttribute} that is
     * marked as display name.
     * <p>
     * Returns <code>null</code> if:
     * <ul>
     * <li>the specified id is <code>null</code></li>
     * <li>no {@link IEnumValue} was found for the specified id</li>
     * <li>there is no attribute marked as display name</li>
     * </ul>
     */
    @Override
    public String getValueName(String id) {
        if (id == null) {
            return null;
        }

        IIpsProject ipsProject = getEnumValueContainer().getIpsProject();
        IEnumValue enumValue = getEnumValueContainer().findEnumValue(id, ipsProject);
        if (enumValue == null) {
            return null;
        }

        return getValueName(ipsProject, enumValue);

    }

    private String getValueName(IIpsProject ipsProject, IEnumValue enumValue) {
        IEnumAttribute displayNameAttribute = getNameAttribute(ipsProject);
        IEnumAttributeValue enumAttributeValue = enumValue.getEnumAttributeValue(displayNameAttribute);
        if (enumAttributeValue != null) {
            return IIpsModel.get().getMultiLanguageSupport().getLocalizedContent(enumAttributeValue.getValue(),
                    ipsProject);
        } else {
            return null;
        }
    }

    private IEnumAttribute getNameAttribute(IIpsProject ipsProject) {
        if (nameAttribute == null || !nameAttribute.findIsUsedAsNameInFaktorIpsUi(ipsProject)) {
            nameAttribute = enumType.findUsedAsNameInFaktorIpsUiAttribute(ipsProject);
        }
        return nameAttribute;
    }

    @Override
    public IEnumValue getValue(String value) {
        IIpsProject ipsProject = getEnumValueContainer().getIpsProject();
        IEnumValue enumValue = getEnumValueContainer().findEnumValue(value, ipsProject);
        if (enumValue == null) {
            return null;
        }
        return enumValue;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <code>true</code>.
     */
    @Override
    public boolean isSupportingNames() {
        return true;
    }

    private List<String> findAllIdentifierAttributeValues(boolean includesNull) {
        List<String> result = getEnumValueContainer().findAllIdentifierAttributeValues(
                getEnumValueContainer().getIpsProject());
        if (includesNull) {
            result.add(null);
        }
        return result;
    }

    private boolean isIdentifierAttributeValues(String value) {
        try {
            return getEnumValueContainer().findEnumValue(value, getEnumValueContainer().getIpsProject()) != null;
        } catch (IpsException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation only throws an {@link IllegalArgumentException} if both values are the
     * same using the {@link String#equals(Object)} compare. If the values are different this method
     * always returns <code>false</code> also if one value may not be part of the enum. This is done
     * because of performance issues.
     */
    @Override
    public boolean areValuesEqual(String valueA, String valueB) {
        if (Objects.equals(valueA, valueB)) {
            if (isParsable(valueA)) {
                return true;
            } else {
                throw new IllegalArgumentException(
                        "The values seems to be equal but the value " + valueA + " is not parseable by the enum " //$NON-NLS-1$ //$NON-NLS-2$
                                + this);
            }
        }
        return false;
    }

    @Override
    public MessageList checkReadyToUse() {
        return new MessageList();
        /*
         * TODO pk 07-08-2009: We need to provide an effective implementation for this method a
         * simple call to the validate method of the EnumType is not efficient since all EnumValues
         * of the EnumType are validated and that means that the system slows down with the
         * increasing number of EnumValues.
         */
    }

    /**
     * Compares enum valueA with enum valueB for order. Returns a negative integer, zero, or a
     * positive integer as valueA is less than, equal to, or greater than valueB. A null value is
     * considered to be bigger than any other enum value.
     * <p>
     * When a list of enum values is sorted, valueA will have a smaller index than valueB. Null
     * value will always be placed at the end of the list.
     * 
     */
    @Override
    public int compare(String valueA, String valueB) {
        IIpsProject ipsProject = getEnumValueContainer().getIpsProject();
        IEnumValue enumValueA = getEnumValueContainer().findEnumValue(valueA, ipsProject);
        IEnumValue enumValueB = getEnumValueContainer().findEnumValue(valueB, ipsProject);
        if (enumValueA == null) {
            return enumValueB == null ? 0 : 1;
        } else {
            if (enumValueB == null) {
                return -1;
            }
        }
        int indexA = getEnumValueContainer().getIndexOfEnumValue(enumValueA);
        int indexB = getEnumValueContainer().getIndexOfEnumValue(enumValueB);
        return indexA - indexB;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <code>null</code>.
     */
    @Override
    public String getDefaultValue() {
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <code>null</code>.
     */
    @Override
    public ValueDatatype getWrapperType() {
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <code>true</code>.
     */
    @Override
    public boolean isImmutable() {
        return !isMutable();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <code>false</code>.
     */
    @Override
    public boolean isMutable() {
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <code>true</code> if the provided value is <code>null</code>
     */
    @Override
    public boolean isNull(String value) {
        return value == null;
    }

    /**
     * Returns <code>true</code> if the provided value is in the list of enumeration attribute
     * values of the literal name attribute of the adapted enumeration type.
     */
    @Override
    public boolean isParsable(String value) {
        return value == null || isIdentifierAttributeValues(value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <code>true</code>.
     */
    @Override
    public boolean supportsCompare() {
        return true;
    }

    @Override
    public String getName() {
        return enumType.getName();
    }

    @Override
    public String getQualifiedName() {
        return enumType.getQualifiedName();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <code>false</code>.
     */
    @Override
    public boolean hasNullObject() {
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <code>true</code> if the adapted enumeration type is abstract.
     */
    @Override
    public boolean isAbstract() {
        return enumType.isAbstract();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <code>false</code>.
     */
    @Override
    public boolean isPrimitive() {
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <code>true</code>.
     */
    @Override
    public boolean isValueDatatype() {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <code>false</code>.
     */
    @Override
    public boolean isVoid() {
        return false;
    }

    @Override
    public int compareTo(Datatype o) {
        return DatatypeComparator.doCompare(this, o);
    }

    /** Returns the enumeration type of this adapter. */
    public IEnumType getEnumType() {
        return enumType;
    }

    /**
     * Returns the enumeration content if this adapter adapts one, otherwise <code>null</code> is
     * returned.
     */
    public IEnumContent getEnumContent() {
        return enumContent;
    }

    /**
     * Returns the enumeration content if this adapter adapts a content, otherwise the enumeration
     * type is returned.
     */
    public IEnumValueContainer getEnumValueContainer() {
        if (enumContent == null) {
            return enumType;
        }
        return enumContent;
    }

    /** Returns <code>true</code> if this adapter adapts an enumeration content. */
    public boolean hasEnumContent() {
        return getEnumContent() != null;
    }

    /**
     * Checks whether this {@link EnumTypeDatatypeAdapter} is covariant to the given datatype. This
     * enum is covariant if both datatypes are equal or if the other datatype is also a
     * {@link EnumTypeDatatypeAdapter} and its enum type is a super type of this one. That means
     * this enum type must be the same or a subtype of the other enum type.
     * 
     * @param datatype The datatype to check
     * @return <code>true</code> if this enum type is covariant to the given datatype
     */
    public boolean isCovariant(ValueDatatype datatype) {
        if (datatype instanceof EnumTypeDatatypeAdapter) {
            return isCovariant((EnumTypeDatatypeAdapter)datatype);
        } else {
            return false;
        }
    }

    private boolean isCovariant(EnumTypeDatatypeAdapter datatype) {
        return enumType.isSubEnumTypeOrSelf(datatype.enumType, enumType.getIpsProject());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EnumTypeDatatypeAdapter other) {
            return enumType.equals(other.enumType) && contentsEqual(other);
        }
        return super.equals(obj);
    }

    private boolean contentsEqual(EnumTypeDatatypeAdapter other) {
        return Objects.equals(enumContent, other.enumContent);
    }

    @Override
    public int hashCode() {
        return enumType.hashCode();
    }

    @Override
    public String toString() {
        return getEnumValueContainer().getQualifiedName();
    }

    @Override
    public Object getValueByName(String name) {
        IIpsProject ipsProject = getEnumValueContainer().getIpsProject();
        return Arrays.stream(getAllValueIds(false))
                .map(this::getValue)
                .filter(v -> Objects.equals(name, getValueName(ipsProject, v)))
                .findFirst()
                .orElse(null);
    }
}
