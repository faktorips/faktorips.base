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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * An enum attribute is a part of an enum type that describes a property of the enum type.
 * <p>
 * Enum attributes are always of a specific <code>datatype</code> and can be <code>inherited</code>
 * from an enum type in the supertype hierarchy. If an enum attribute is inherited from the
 * supertype hierarchy it is treated as a copy of the original enum attribute refering to its
 * properties.
 * <p>
 * An enum attribute can be marked as <code>useAsLiteralName</code> which implies that the values
 * for this enum attribute will be used to identify the respective enum values in the generated
 * source code. An enum attribute can also be marked as <code>uniqueIdentifier</code> which implies
 * that each value for this enum attribute must be unique.
 * <p>
 * For more information about how enum attributes relate to the entire Faktor-IPS enums concept
 * please read the documentation of IEnumType.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumType
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumAttribute extends IIpsObjectPart {

    /** The xml tag for this ips object part. */
    public final static String XML_TAG = "EnumAttribute"; //$NON-NLS-1$

    /** Name of the <code>datatype</code> property. */
    public final static String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$

    /** Name of the <code>literalName</code> property. */
    public final static String PROPERTY_LITERAL_NAME = "literalName"; //$NON-NLS-1$

    /** Name of the <code>inherited</code> property. */
    public final static String PROPERTY_INHERITED = "inherited"; //$NON-NLS-1$

    /** Name of the <code>uniqueIdentifier</code> property. */
    public final static String PROPERTY_UNIQUE_IDENTIFIER = "uniqueIdentifier"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    public final static String MSGCODE_PREFIX = "ENUMATTRIBUTE-"; //$NON-NLS-1$

    /** Validation message code to indicate that the name of this enum attribute is missing. */
    public final static String MSGCODE_ENUM_ATTRIBUTE_NAME_MISSING = MSGCODE_PREFIX + "EnumAttributeNameMissing"; //$NON-NLS-1$

    /** Validation message code to indicate that the name of this enum attribute is already used. */
    public final static String MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_NAME = MSGCODE_PREFIX + "EnumAttributeNameMissing"; //$NON-NLS-1$

    /** Validation message code to indicate that the datatype of this enum attribute is missing. */
    public final static String MSGCODE_ENUM_ATTRIBUTE_DATATYPE_MISSING = MSGCODE_PREFIX
            + "EnumAttributeDatatypeMissing"; //$NON-NLS-1$

    /** Validation message code to indicate that the datatype of this enum attribute does not exist. */
    public final static String MSGCODE_ENUM_ATTRIBUTE_DATATYPE_DOES_NOT_EXIST = MSGCODE_PREFIX
            + "EnumAttributeDatatypeDoesNotExist"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this enum attribute is marked as literal name but is
     * not of datatype String.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_LITERAL_NAME_NOT_OF_DATATYPE_STRING = MSGCODE_PREFIX
            + "EnumAttributeLiteralNameNotOfDatatypeString"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this enum attribute is inherited from the supertype
     * hierarchy but there is no such enum attribute in the supertype hierarchy.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_NO_SUCH_ATTRIBUTE_IN_SUPERTYPE_HIERARCHY = MSGCODE_PREFIX
            + "EnumAttributeNoSuchAttributeInSupertypeHierarchy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there is at least one other enum attribute marked as
     * literal name in the parent enum type.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_LITERAL_NAME = MSGCODE_PREFIX
            + "EnumAttributeDuplicateLiteralName"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this enum attribute is marked to be used as literal
     * name but is not a unique identifier.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_LITERAL_NAME_BUT_NOT_UNIQUE_IDENTIFIER = MSGCODE_PREFIX
            + "EnumAttributeLiteralNameButNotUniqueIdentifier"; //$NON-NLS-1$;

    /**
     * Sets the name of this enum attribute.
     * 
     * @param name The new name for this enum attribute.
     * 
     * @throws NullPointerException If <code>name</code> is <code>null</code>.
     */
    public void setName(String name);

    /**
     * Returns the unqualified name of the datatype of this enum attribute.
     * <p>
     * <strong>Important:</strong> This operation does not search the supertype hierarchy for the
     * datatype if this enum attribute is inherited. Use <code>findDatatype(IIpsProject)</code> in
     * this case.
     * 
     * @see #findDatatype(IIpsProject)
     */
    public String getDatatype();

    /**
     * Returns this enum attribute's value datatype.
     * <p>
     * If this enum attribute is inherited the value datatype of the super enum attribute will be
     * returned.
     * <p>
     * Returns <code>null</code> if no value datatype can be found or if the super enum attribute
     * could not be found.
     * 
     * @param ipsProject The ips project which ips object path is used for the search. This is not
     *            necessarily the project this enum attribute is part of.
     * 
     * @see #getDatatype()
     * 
     * @throws NullPointerException If <code>ipsProject</code> is <code>null</code>.
     * @throws CoreException If an error occurs while searching the given ips project for the value
     *             datatype.
     */
    public ValueDatatype findDatatype(IIpsProject ipsProject) throws CoreException;

    /**
     * Sets the datatype of this enum attribute.
     * 
     * @param datatype The unqualified name of the datatype.
     * 
     * @throws NullPointerException If <code>datatype</code> is <code>null</code>.
     */
    public void setDatatype(String datatype);

    /**
     * Returns <code>true</code> if this enum attribute is used as literal name, <code>false</code>
     * if not.
     * <p>
     * <strong>Important:</strong> This operation does not search the supertype hierarchy for the
     * literal name property if this enum attribute is inherited. Use
     * <code>findIsLiteralName()</code> in this case.
     * 
     * @see #findIsLiteralName()
     */
    public boolean isLiteralName();

    /**
     * Returns <code>true</code> if this enum attribute is used as literal name, <code>false</code>
     * if not.
     * <p>
     * If this enum attribute is inherited the property of the super enum attribute will be
     * returned. Returns <code>null</code> if the super enum attribute cannot be found.
     * 
     * @see #isLiteralName()
     * 
     * @throws CoreException If an error occurs while searching for the super enum attribute.
     */
    // TODO aw: ips project as parameter neccessary?
    public Boolean findIsLiteralName() throws CoreException;

    /**
     * Sets whether this enum attribute is used as literal name.
     * 
     * @param isLiteralName Flag indicating whether this enum attribute will be used as literal
     *            name.
     */
    public void setLiteralName(boolean isLiteralName);

    /**
     * Returns <code>true</code> if this enum attribute is inherited from the supertype hierarchy,
     * <code>false</code> if not.
     */
    public boolean isInherited();

    /**
     * Sets whether this enum attribute is inherited from the supertype hierarchy.
     * <p>
     * If this property is set to <code>true</code> this enum attribute is treated like a copy of
     * the original enum attribute in the respective super enum type. This means the
     * <code>datatype</code>, <code>useAsLiteralName</code> and <code>uniqueIdentifier</code>
     * properties are then defined by the original enum attribute. The properties will be set to an
     * empty string or <code>false</code> and the respective setters and getters will throw
     * <code>IllegalStateException</code>s when called from now on.
     * 
     * @param isInherited Flag indicating whether this enum attribute is inherited from the
     *            supertype hierarchy.
     */
    public void setInherited(boolean isInherited);

    /**
     * Returns the enum type this enum attribute belongs to.
     * <p>
     * This is a shortcut for: <code>(IEnumType)this.getParent();</code>
     */
    public IEnumType getEnumType();

    /**
     * Returns <code>true</code> if this enum attribute is a unique identifier, <code>false</code>
     * if not.
     * <p>
     * <strong>Important:</strong> This operation does not search the supertype hierarchy for the
     * unique identifier property if this enum attribute is inherited. Use
     * <code>findIsUniqueIdentifier()</code> in this case.
     * 
     * @see #findIsUniqueIdentifier()
     */
    public boolean isUniqueIdentifier();

    /**
     * Returns <code>true</code> if this enum attribute is marked as unique identifier,
     * <code>false</code> if not.
     * <p>
     * If this enum attribute is inherited the property of the super enum attribute will be
     * returned. Returns <code>null</code> if the super enum attribute cannot be found.
     * 
     * @see #isUniqueIdentifier()
     * 
     * @throws CoreException If an error occurs while searching for the super enum attribute.
     */
    // TODO aw: ips project as parameter neccessary?
    public Boolean findIsUniqueIdentifier() throws CoreException;

    /**
     * Sets whether this enum attribute is a unique identifier.
     * 
     * @param isUniqueIdentifier Flag indicating whether this enum attribute will be a unique
     *            identifier.
     */
    public void setUniqueIdentifier(boolean isUniqueIdentifier);

}
