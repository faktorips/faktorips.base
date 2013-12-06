/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.enums;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;

/**
 * This is an adapter for an <tt>IEnumType</tt> that adapts the <tt>EnumDatatype</tt> interface.
 * 
 * @see IEnumType
 * @see org.faktorips.datatype.EnumDatatype
 * 
 * @author Peter Kuntz
 */
public class EnumTypeDatatypeAdapter implements EnumDatatype {

    private IEnumType enumType;

    private IEnumContent enumContent;

    /**
     * Creates a new <tt>EnumTypeDatatypeAdapter</tt>.
     * 
     * @param enumType The <tt>IEnumType</tt> that is adapted. This parameter cannot be
     *            <tt>null</tt>.
     * @param enumContent The <tt>IEnumContent</tt> that is adapted. This parameter can be
     *            <tt>null</tt>.
     */
    public EnumTypeDatatypeAdapter(IEnumType enumType, IEnumContent enumContent) {
        super();
        ArgumentCheck.notNull(enumType, this);
        this.enumType = enumType;
        this.enumContent = enumContent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnum() {
        return true;
    }

    /**
     * Returns the IDs of the values of adapted enumeration type respectively the enumeration
     * content. The attribute value referring to the enumeration attribute marked as identifier is
     * considered to be the ID.
     * <p>
     * Returns a string array containing only <tt>null</tt> as a value if the enumeration type of
     * this adapter doesn't contain values and the enumeration content of this adapter is
     * <tt>null</tt> and the parameter includeNull is set to true. Returns an empty string array if
     * the parameter <tt>includeNull</tt> is set to false.
     * <p>
     * Returns an empty string array if the identifier attribute of the adapted enumeration type has
     * not been specified.
     * 
     * @throws RuntimeException If the process of determining the <tt>IEnumAttributeValue</tt>s
     *             throws a <tt>CoreException</tt>.
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

        try {
            IIpsProject ipsProject = getEnumValueContainer().getIpsProject();
            IEnumValue enumValue = getEnumValueContainer().findEnumValue(id, ipsProject);
            if (enumValue == null) {
                return null;
            }

            IEnumAttribute displayNameAttribute = enumType.findUsedAsNameInFaktorIpsUiAttribute(ipsProject);
            IEnumAttributeValue enumAttributeValue = enumValue.getEnumAttributeValue(displayNameAttribute);
            if (enumAttributeValue != null) {
                return IpsPlugin.getMultiLanguageSupport().getLocalizedContent(enumAttributeValue.getValue(),
                        ipsProject);
            } else {
                return null;
            }

        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getValue(String value) {
        try {
            IIpsProject ipsProject = getEnumValueContainer().getIpsProject();
            IEnumValue enumValue = getEnumValueContainer().findEnumValue(value, ipsProject);
            if (enumValue == null) {
                return null;
            }
            return enumValue;
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>true</tt>.
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
        } catch (CoreException e) {
            return false;
        }
    }

    @Override
    public boolean areValuesEqual(String valueA, String valueB) {
        if (ObjectUtils.equals(valueA, valueB)) {
            if (isParsable(valueA)) {
                return true;
            } else {
                throw new IllegalArgumentException("Either the value of parameter valueA=" + valueA //$NON-NLS-1$
                        + " or the one of parameter valueB=" //$NON-NLS-1$
                        + " is not part of this enumeration type. Therefore the equality cannot be determined."); //$NON-NLS-1$
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
        try {
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
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>null</tt>.
     */
    @Override
    public String getDefaultValue() {
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>null</tt>.
     */
    @Override
    public ValueDatatype getWrapperType() {
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>true</tt>.
     */
    @Override
    public boolean isImmutable() {
        return !isMutable();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>false</tt>.
     */
    @Override
    public boolean isMutable() {
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>true</tt> if the provided value is <tt>null</tt>
     */
    @Override
    public boolean isNull(String value) {
        return value == null;
    }

    /**
     * Returns <tt>true</tt> if the provided value is in the list of enumeration attribute values of
     * the literal name attribute of the adapted enumeration type.
     */
    @Override
    public boolean isParsable(String value) {
        return value == null || isIdentifierAttributeValues(value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>true</tt>.
     */
    @Override
    public boolean supportsCompare() {
        return true;
    }

    @Override
    public String getJavaClassName() {
        return getEnumValueContainer().getIpsProject().getDatatypeHelper(this).getJavaClassName();
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
     * Returns <tt>false</tt>.
     */
    @Override
    public boolean hasNullObject() {
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>true</tt> if the adapted enumeration type is abstract.
     */
    @Override
    public boolean isAbstract() {
        return enumType.isAbstract();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>false</tt>.
     */
    @Override
    public boolean isPrimitive() {
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>true</tt>.
     */
    @Override
    public boolean isValueDatatype() {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>false</tt>.
     */
    @Override
    public boolean isVoid() {
        return false;
    }

    @Override
    public int compareTo(Datatype o) {
        EnumDatatype other = (EnumDatatype)o;
        return getQualifiedName().compareTo(other.getQualifiedName());
    }

    /** Returns the enumeration type of this adapter. */
    public IEnumType getEnumType() {
        return enumType;
    }

    /**
     * Returns the enumeration content if this adapter adapts one, otherwise <tt>null</tt> is
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

    /** Returns <tt>true</tt> if this adapter adapts an enumeration content. */
    public boolean hasEnumContent() {
        return getEnumContent() != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EnumTypeDatatypeAdapter) {
            EnumTypeDatatypeAdapter other = (EnumTypeDatatypeAdapter)obj;
            if (other.enumContent == null && enumContent == null) {
                return enumType.equals(other.enumType);
            } else if ((other.enumContent == null && enumContent != null)
                    || (other.enumContent != null && enumContent == null)) {
                return false;
            }
            return enumType.equals(other.enumType) && enumContent.equals(other.enumContent);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return enumType.hashCode();
    }

    @Override
    public String toString() {
        return getEnumValueContainer().getQualifiedName();
    }

}
