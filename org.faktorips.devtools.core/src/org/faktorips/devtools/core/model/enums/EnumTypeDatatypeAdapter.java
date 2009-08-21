/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.enums;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
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
    public String[] getAllValueIds(boolean includeNull) {
        if (enumContent == null) {
            if (!(enumType.isContainingValues()) && includeNull) {
                return new String[] { null };
            }
            if (!(enumType.isContainingValues())) {
                return new String[0];
            }
        }
        List<String> result = findAllIdentifierAttributeValues(includeNull);
        return result.toArray(new String[result.size()]);
    }

    /**
     * Checks if the provided ID is equal to one of the enumeration attribute values referencing to
     * the display name attribute adapted enumeration type. If so the display name is returned,
     * otherwise <tt>null</tt>.
     */
    public String getValueName(String id) {
        if (id == null) {
            return null;
        }

        if (enumContent == null) {
            if (!(enumType.isContainingValues())) {
                return null;
            }
        }

        try {
            IIpsProject ipsProject = getEnumValueContainer().getIpsProject();
            IEnumValue enumValue = getEnumValueContainer().findEnumValue(id, ipsProject);
            if (enumValue == null) {
                return null;
            }

            IEnumAttribute displayNameAttribute = enumType.findUsedAsNameInFaktorIpsUiAttribute(ipsProject);
            IEnumAttributeValue enumAttributeValue = enumValue.findEnumAttributeValue(ipsProject, displayNameAttribute);
            return enumAttributeValue.getValue();

        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>true</tt>.
     */
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

    public boolean areValuesEqual(String valueA, String valueB) {
        List<String> result = findAllIdentifierAttributeValues(true);
        if (result.contains(valueA) && result.contains(valueB)) {
            return ObjectUtils.equals(valueA, valueB);
        }
        throw new IllegalArgumentException("Either the value of parameter valueA=" + valueA
                + " or the one of parameter valueB="
                + " is not part of this enumeration type. Therefor the equality cannot be determined.");
    }

    public MessageList checkReadyToUse() {
        return new MessageList();
        /*
         * TODO pk 07-08-2009: We need to provide an effective implementation for this method a
         * simple call to the validate method of the EnumType is not efficient since all EnumValues
         * of the EnumType are validated and that means that the system slows down with the
         * increasing number of EnumValues.
         */
    }

    public int compare(String valueA, String valueB) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>null</tt>.
     */
    public String getDefaultValue() {
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>null</tt>.
     */
    public ValueDatatype getWrapperType() {
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>true</tt>.
     */
    public boolean isImmutable() {
        return !isMutable();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>false</tt>.
     */
    public boolean isMutable() {
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>true</tt> if the provided value is <tt>null</tt>
     */
    public boolean isNull(String value) {
        return value == null;
    }

    /**
     * Returns <tt>true</tt> if the provided value is in the list of enumeration attribute values of
     * the literal name attribute of the adapted enumeration type.
     */
    public boolean isParsable(String value) {
        List<String> result = findAllIdentifierAttributeValues(true);
        return result.contains(value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>false</tt>.
     */
    public boolean supportsCompare() {
        return false;
    }

    public String getJavaClassName() {
        return getEnumValueContainer().getIpsProject().getDatatypeHelper(this).getJavaClassName();
    }

    public String getName() {
        return enumType.getName();
    }

    public String getQualifiedName() {
        return enumType.getQualifiedName();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>false</tt>.
     */
    public boolean hasNullObject() {
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>true</tt> if the adapted enumeration type is abstract.
     */
    public boolean isAbstract() {
        return enumType.isAbstract();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>false</tt>.
     */
    public boolean isPrimitive() {
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>true</tt>.
     */
    public boolean isValueDatatype() {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>false</tt>.
     */
    public boolean isVoid() {
        return false;
    }

    public int compareTo(Object o) {
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

}
