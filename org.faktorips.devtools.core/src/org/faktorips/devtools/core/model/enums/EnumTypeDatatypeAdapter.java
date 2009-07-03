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
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;

/**
 * 
 * 
 * @author Peter Kuntz
 */
// FIXME: This adapter should not be in the published package but this would require many changes.
public class EnumTypeDatatypeAdapter implements EnumDatatype {

    private IEnumType enumType;
    private IEnumContent enumContent;

    /**
     * Creates a new <code>EnumTypeDatatypeAdapter</code>.
     * 
     * @param enumType the {@link IEnumType} that is adapted. This parameter cannot be
     *            <code>null</code>
     * @param enumContent the {@link IEnumContent} that is adapted. this parameter can be
     *            <code>null</code>
     */
    public EnumTypeDatatypeAdapter(IEnumType enumType, IEnumContent enumContent) {
        super();
        ArgumentCheck.notNull(enumType, this);
        this.enumType = enumType;
        this.enumContent = enumContent;
    }

    /**
     * Returns the ids of the values of adapted enumeration type respectively the enumeration
     * content. The literal name attribute value is considered to be the id. </p>Returns a string
     * array containing only <code>null</code> as a value if the enumeration type of this adapter
     * doesn't contain values and the enumeration content of this adapter is <code>null</code> and
     * the parameter includeNull is set to true. Returns an empty string array if the parameter
     * includeNull is set to false.</p> Returns an empty string array if the literal name attribute
     * of the adapted enumeration type has not been specified.
     * 
     * @throws RuntimeException If the process of determining the enum attribute values throws a
     *             <code>CoreException</code>.
     */
    public String[] getAllValueIds(boolean includeNull) {
        if (enumContent == null) {
            if (!enumType.isContainingValues() && includeNull) {
                return new String[] { null };
            }
            if (!enumType.isContainingValues()) {
                return new String[0];
            }
        }
        List<String> result = getEnumValueContainer().findAllLiteralNameAttributeValues(includeNull,
                getEnumValueContainer().getIpsProject());
        return result.toArray(new String[result.size()]);
    }

    /**
     * Checks if the provided id is equal to one of the enumeration attribute values of the literal
     * name attribute of the adapted enumeration type. If so the id is returned otherwise
     * <code>null</code> is returned.
     */
    public String getValueName(String id) {
        if (enumContent == null) {
            if (id == null || !enumType.isContainingValues()) {
                return null;
            }
        }
        // TODO change to fips ui name
        List<String> result = getEnumValueContainer().findAllLiteralNameAttributeValues(true,
                getEnumValueContainer().getIpsProject());
        if (result.contains(id)) {
            return id;
        }
        return null;
    }

    /**
     * Returns true.
     */
    public boolean isSupportingNames() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean areValuesEqual(String valueA, String valueB) {
        List<String> result = getEnumValueContainer().findAllLiteralNameAttributeValues(true,
                getEnumValueContainer().getIpsProject());
        if (result.contains(valueA) && result.contains(valueB)) {
            return ObjectUtils.equals(valueA, valueB);
        }
        throw new IllegalArgumentException("Either the value of parameter valueA=" + valueA
                + " or the one of parameter valueB="
                + " is not part of this enumeration type. Therefor the equality cannot be determined.");
    }

    /**
     * {@inheritDoc}
     */
    public MessageList checkReadyToUse() {
        try {
            if (enumContent == null) {
                return enumType.validate(enumType.getIpsProject());
            }
            return enumType.validate(enumContent.getIpsProject());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public int compare(String valueA, String valueB) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns null.
     */
    public String getDefaultValue() {
        return null;
    }

    /**
     * Returns null.
     */
    public ValueDatatype getWrapperType() {
        return null;
    }

    /**
     * Returns true.
     */
    public boolean isImmutable() {
        return !isMutable();
    }

    /**
     * Returns false.
     */
    public boolean isMutable() {
        return false;
    }

    /**
     * Returns true if the provided value is <code>null</code>
     */
    public boolean isNull(String value) {
        return value == null;
    }

    /**
     * Returns true if the provided value is in the list of enumeration attribute values of the
     * literal name attribute of the adapted enumeration type.
     */
    public boolean isParsable(String value) {
        List<String> result = getEnumValueContainer().findAllLiteralNameAttributeValues(true,
                getEnumValueContainer().getIpsProject());
        return result.contains(value);
    }

    /**
     * Returns false.
     */
    public boolean supportsCompare() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public String getJavaClassName() {
        return getEnumValueContainer().getIpsProject().getDatatypeHelper(this).getJavaClassName();
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return enumType.getName();
    }

    /**
     * {@inheritDoc}
     */
    public String getQualifiedName() {
        return enumType.getQualifiedName();
    }

    /**
     * Returns false.
     */
    public boolean hasNullObject() {
        return false;
    }

    /**
     * Returns true if the adapted enumeration type is abstract.
     */
    public boolean isAbstract() {
        return enumType.isAbstract();
    }

    /**
     * Returns false.
     */
    public boolean isPrimitive() {
        return false;
    }

    /**
     * Returns true.
     */
    public boolean isValueDatatype() {
        return true;
    }

    /**
     * Returns false.
     */
    public boolean isVoid() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(Object o) {
        EnumDatatype other = (EnumDatatype)o;
        return getQualifiedName().compareTo(other.getQualifiedName());
    }

    /**
     * Returns the enumeration type of this adapter.
     */
    public IEnumType getEnumType() {
        return enumType;
    }

    /**
     * Returns the enumeration content if this adapter adapts one otherwise <code>null</code> is
     * returned.
     */
    public IEnumContent getEnumContent() {
        return enumContent;
    }

    /**
     * Returns the enumeration content if this adapter adapts a content otherwise the enumeration
     * type is returned.
     */
    public IEnumValueContainer getEnumValueContainer() {
        if (enumContent == null) {
            return enumType;
        }
        return enumContent;
    }

    /**
     * Returns true if this adapter adapts an enumeration content.
     */
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
