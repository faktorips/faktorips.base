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
 * An <tt>IEnumAttribute</tt> is a part of an <tt>IEnumType</tt> that describes a property of this
 * <tt>IEnumType</tt>.
 * <p>
 * <tt>IEnumAttribute</tt>s are always of a specific data type and can be inherited from an
 * <tt>IEnumType</tt> in the supertype hierarchy. If an <tt>IEnumAttribute</tt> is inherited from
 * the supertype hierarchy it is treated as a copy of the original <tt>IEnumAttribute</tt> referring
 * to its properties.
 * <p>
 * An <tt>IEnumAttribute</tt> can be marked as unique, which implies that each value for this
 * <tt>IEnumAttribute</tt> must be unique.
 * <p>
 * Furthermore an <tt>IEnumAttribute</tt> can be marked to be used as name in the Faktor-IPS UI or
 * to be used as (default) identifier.
 * <p>
 * For more information about how <tt>IEnumAttribute</tt>s relate to the entire Faktor-IPS
 * enumeration concept please read the documentation of <tt>IEnumType</tt>.
 * 
 * @see IEnumType
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumAttribute extends IIpsObjectPart {

    /** The XML tag for this IPS object part. */
    public final static String XML_TAG = "EnumAttribute"; //$NON-NLS-1$

    /** Name of the <tt>datatype</tt> property. */
    public final static String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$

    /** Name of the <tt>inherited</tt> property. */
    public final static String PROPERTY_INHERITED = "inherited"; //$NON-NLS-1$

    /** Name of the <tt>unique</tt> property. */
    public final static String PROPERTY_UNIQUE = "unique"; //$NON-NLS-1$

    /** Name of the <tt>usedAsNameInFaktorIpsUi</tt> property. */
    public final static String PROPERTY_USED_AS_NAME_IN_FAKTOR_IPS_UI = "usedAsNameInFaktorIpsUi"; //$NON-NLS-1$

    /** Name of the <tt>identifier</tt> property. */
    public final static String PROPERTY_IDENTIFIER = "identifier"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    public final static String MSGCODE_PREFIX = "ENUMATTRIBUTE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name of this <tt>IEnumAttribute</tt> is missing.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_NAME_MISSING = MSGCODE_PREFIX + "EnumAttributeNameMissing"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name of this <tt>IEnumAttribute</tt> is already
     * used.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_NAME = MSGCODE_PREFIX + "EnumAttributeNameMissing"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of this <tt>IEnumAttribute</tt> is
     * missing.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_DATATYPE_MISSING = MSGCODE_PREFIX
            + "EnumAttributeDatatypeMissing"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of this <tt>IEnumAttribute</tt> does
     * not exist.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_DATATYPE_DOES_NOT_EXIST = MSGCODE_PREFIX
            + "EnumAttributeDatatypeDoesNotExist"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of this <tt>IEnumAttribute</tt> is a
     * primitive data type, which is forbidden.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_DATATYPE_IS_PRIMITIVE = MSGCODE_PREFIX
            + "EnumAttributeDatatypeIsPrimitive"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of this <tt>IEnumAttribute</tt> is the
     * void data type, which is forbidden.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_DATATYPE_IS_VOID = MSGCODE_PREFIX + "EnumAttributeDatatypeIsVoid"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of this <tt>IEnumAttribute</tt> is an
     * abstract data type, which is forbidden.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_DATATYPE_IS_ABSTRACT = MSGCODE_PREFIX
            + "EnumAttributeDatatypeIsAbstract"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of this <tt>IEnumAttribute</tt> is the
     * containing <tt>IEnumType</tt> or a subclass of it, which is forbidden.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_DATATYPE_IS_CONTAINING_ENUM_TYPE_OR_SUBCLASS = MSGCODE_PREFIX
            + "EnumAttributeDatatypeIsContainingEnumTypeOrSubclass"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this <tt>IEnumAttribute</tt> is marked as literal
     * name but is not of data type <tt>String</tt>.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_LITERAL_NAME_NOT_OF_DATATYPE_STRING = MSGCODE_PREFIX
            + "EnumAttributeLiteralNameNotOfDatatypeString"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this <tt>IEnumAttribute</tt> is inherited from the
     * supertype hierarchy but there is no such <tt>IEnumAttribute</tt> in the supertype hierarchy.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_NO_SUCH_ATTRIBUTE_IN_SUPERTYPE_HIERARCHY = MSGCODE_PREFIX
            + "EnumAttributeNoSuchAttributeInSupertypeHierarchy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this <tt>IEnumAttribute</tt> is inherited from the
     * supertype hierarchy but the containing <tt>IEnumType</tt> has no super enumeration type.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_INHERITED_BUT_NO_SUPERTYPE = MSGCODE_PREFIX
            + "EnumAttributeInheritedButNoSupertype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this <tt>IEnumAttribute</tt> is inherited from the
     * supertype hierarchy but the super enumeration type of the containing <tt>IEnumType</tt> does
     * not exist.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_INHERITED_BUT_NO_EXISTING_SUPERTYPE = MSGCODE_PREFIX
            + "EnumAttributeInheritedButNoExistingSupertype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there is at least one other <tt>IEnumAttribute</tt>
     * marked as literal name in the parent <tt>IEnumType</tt>.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_LITERAL_NAME = MSGCODE_PREFIX
            + "EnumAttributeDuplicateLiteralName"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this <tt>IEnumAttribute</tt> is marked to be used as
     * literal name but is not a unique identifier.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_LITERAL_NAME_BUT_NOT_UNIQUE_IDENTIFIER = MSGCODE_PREFIX
            + "EnumAttributeLiteralNameButNotUniqueIdentifier"; //$NON-NLS-1$;

    /**
     * Validation message code to indicate that there is at least one other <tt>IEnumAttribute</tt>
     * marked to be used as name in the Faktor-IPS UI in the parent <tt>IEnumType</tt>.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_USED_AS_NAME_IN_FAKTOR_IPS_UI = MSGCODE_PREFIX
            + "EnumAttributeDuplicateUsedAsNameInFaktorIpsUi"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there is at least one other <tt>IEnumAttribute</tt>
     * marked to be used as ID in the Faktor-IPS UI in the parent <tt>IEnumType</tt>.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_USED_AS_ID_IN_FAKTOR_IPS_UI = MSGCODE_PREFIX
            + "EnumAttributeDuplicateUsedAsIdInFaktorIpsUi"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of this <tt>IEnumAttribute</tt> is an
     * <tt>IEnumType</tt> that does not contain values while the parent <tt>IEnumType</tt> does.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_ENUM_DATATYPE_DOES_NOT_CONTAIN_VALUES_BUT_PARENT_ENUM_TYPE_DOES = MSGCODE_PREFIX
            + "EnumAttributeEnumDatatypeDoesNotContainValuesButParentEnumTypeDoes"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name of this <tt>IEnumAttribute</tt> is not a
     * valid Java field name.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_NAME_NOT_A_VALID_FIELD_NAME = MSGCODE_PREFIX
            + "EnumAttributeNameNotAValidFieldName"; //$NON-NLS-1$

    /**
     * Sets the name of this <tt>IEnumAttribute</tt>.
     * 
     * @param name The new name for this <tt>IEnumAttribute</tt>.
     * 
     * @throws NullPointerException If <tt>name</tt> is <tt>null</tt>.
     */
    public void setName(String name);

    /**
     * Returns the qualified name of the data type of this <tt>IEnumAttribute</tt>.
     * <p>
     * <strong>Important:</strong> This operation does not search the supertype hierarchy for the
     * data type if this <tt>IEnumAttribute</tt> is inherited. Use
     * <code>findDatatype(IIpsProject)</code> in this case.
     * 
     * @see #findDatatype(IIpsProject)
     */
    public String getDatatype();

    /**
     * Returns this <tt>IEnumAttribute</tt>'s <tt>ValueDatatype</tt>.
     * <p>
     * If this <tt>IEnumAttribute</tt> is inherited the <tt>ValueDatatype</tt> of the super
     * <tt>IEnumAttribute</tt> will be returned.
     * <p>
     * Returns <code>null</code> if no <tt>ValueDatatype</tt> can be found or if the super
     * <tt>IEnumAttribute</tt> could not be found.
     * 
     * @param ipsProject The IPS project which IPS object path is used for the search of the super
     *            enumeration attribute. This is not necessarily the project this
     *            <tt>IEnumAttribute</tt> is part of.
     * 
     * @see #getDatatype()
     * 
     * @throws CoreException If an error occurs while searching the given IPS project for the value
     *             data type.
     * @throws NullPointerException If <code>ipsProject</code> is <code>null</code>.
     */
    public ValueDatatype findDatatype(IIpsProject ipsProject) throws CoreException;

    /**
     * Sets the data type of this <tt>IEnumAttribute</tt>.
     * 
     * @param dataType The unqualified name of the data type.
     * 
     * @throws NullPointerException If <tt>dataType</tt> is <tt>null</tt>.
     */
    public void setDatatype(String dataType);

    /**
     * Returns <code>true</code> if this <tt>IEnumAttribute</tt> is inherited from the supertype
     * hierarchy, <code>false</code> if not.
     */
    public boolean isInherited();

    /**
     * Sets whether this <tt>IEnumAttribute</tt> is inherited from the supertype hierarchy.
     * <p>
     * If this property is set to <tt>true</tt> this <tt>IEnumAttribute</tt> is treated like a copy
     * of the original <tt>IEnumAttribute</tt> in the respective super <tt>IEnumType</tt>. This
     * means the <tt>datatype</tt>, <tt>unique</tt>, <tt>identifier</tt> and
     * <tt>usedAsNameInFaktorIpsUi</tt> properties are then derived from the original
     * <tt>IEnumAttribute</tt>. The properties will be set to an empty string or <tt>false</tt> and
     * the respective setters and getters will throw <tt>IllegalStateException</tt>s when called
     * from now on.
     * 
     * @param isInherited Flag indicating whether this <tt>IEnumAttribute</tt> is inherited from the
     *            supertype hierarchy.
     */
    public void setInherited(boolean isInherited);

    /**
     * Returns the <tt>IEnumType</tt> this <tt>IEnumAttribute</tt> belongs to.
     * <p>
     * This is a shortcut for: <tt>(IEnumType)this.getParent();</tt>
     */
    public IEnumType getEnumType();

    /**
     * Returns <tt>true</tt> if by means of this attribute a value of this enumeration type can be
     * identified uniquely.
     * <p>
     * <strong>Important:</strong> This method does not search the supertype hierarchy for the
     * <tt>unique</tt> property. The method <tt>findIsUnique()</tt> takes the supertype hierarchy
     * into account.
     * 
     * @see #findIsUnique()
     */
    public boolean isUnique();

    /**
     * Returns <tt>true</tt> if this enumeration attribute is marked as unique, <tt>false</tt> if
     * not.
     * <p>
     * If this attribute is inherited the property of the supertype attribute will be returned.
     * Returns <tt>null</tt> if the supertype attribute cannot be found.
     * 
     * @see #isUnique()
     * 
     * @param ipsProject The IPS project that is used to the search the <tt>unique</tt> property in
     *            the supertype hierarchy.
     * 
     * @throws CoreException If an error occurs while searching
     * @throws NullPointerException If <tt>ipsProject</tt> is <tt>null</tt>.
     */
    public Boolean findIsUnique(IIpsProject ipsProject) throws CoreException;

    /**
     * Sets whether this <tt>IEnumAttribute</tt> is a unique identifier.
     * 
     * @param uniqueIdentifier Flag indicating whether this <tt>IEnumAttribute</tt> will be a unique
     *            identifier.
     */
    public void setUnique(boolean uniqueIdentifier);

    /**
     * Sets whether the values of this <tt>IEnumAttribute</tt> shall be used as name of enumeration
     * values in the Faktor-IPS UI.
     * 
     * @param usedAsNameInFaktorIpsUi Flag indicating whether this <tt>IEnumAttribute</tt> shall be
     *            used as name of enumeration values in the Faktor-IPS UI (<tt>true</tt>) or not (
     *            <tt>false</tt>).
     */
    public void setUsedAsNameInFaktorIpsUi(boolean usedAsNameInFaktorIpsUi);

    /**
     * Returns a flag indicating whether this <tt>IEnumAttribute</tt> is marked to be used as name
     * of enumeration values in the Faktor-IPS UI.
     * <p>
     * <strong>Important:</strong> This operation does not search the supertype hierarchy for the
     * <tt>usedAsNameInFaktorIpsUi</tt> property if this <tt>IEnumAttribute</tt> is inherited. Use
     * <tt>findIsUsedAsNameInFaktorIpsUi()</tt> in this case.
     * 
     * @see #findIsUsedAsNameInFaktorIpsUi()
     */
    public boolean isUsedAsNameInFaktorIpsUi();

    /**
     * Sets whether the values of this <tt>IEnumAttribute</tt> shall be used as (default) identifier
     * of enumeration values in the Faktor-IPS UI.
     * 
     * @param identifier Flag indicating whether this <tt>IEnumAttribute</tt> shall be used as
     *            (default) identifier of enumeration values in the Faktor-IPS UI (<tt>true</tt>) or
     *            not ( <tt>false</tt>).
     */
    public void setIdentifier(boolean identifier);

    /**
     * Returns <tt>true</tt> if this attribute is the identifying attribute of the parent
     * <tt>IEnumType</tt>. Only one attribute within an <tt>IEnumType</tt> can be the identifying
     * attribute.
     * <p>
     * <strong>Important:</strong> This method does not search the supertype hierarchy to look for
     * the this property. Use <tt>findIsIdentifier()</tt> if necessary.
     * 
     * @see #findIsIdentifier(IIpsProject)
     */
    public boolean isIdentifier();

    /**
     * Returns <tt>true</tt> if this <tt>IEnumAttribute</tt> is marked to be used as name in the
     * Faktor-IPS UI, <tt>false</tt> if not.
     * <p>
     * If this <tt>IEnumAttribute</tt> is inherited the property of the super
     * <tt>IEnumAttribute</tt> will be returned. Returns <tt>null</tt> if the super
     * <tt>IEnumAttribute</tt> cannot be found.
     * 
     * @see #isUsedAsNameInFaktorIpsUi()
     * 
     * @param ipsProject The IPS project which IPS object path is used for the search of the super
     *            <tt>IEnumAttribute</tt>. This is not necessarily the project this
     *            <tt>IEnumAttribute</tt> is part of.
     * 
     * @throws CoreException If an error occurs while searching the given IPS project for the super
     *             <tt>IEnumAttribute</tt>.
     * @throws NullPointerException If <tt>ipsProject</tt> is <tt>null</tt>.
     */
    public Boolean findIsUsedAsNameInFaktorIpsUi(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the original <tt>IEnumAttribute</tt> this <tt>IEnumAttribute</tt> is a copy of (if
     * this <tt>IEnumAttribute</tt> is inherited).
     * <p>
     * Returns <tt>null</tt> if this <tt>IEnumAttribute</tt> is not inherited or the super
     * <tt>IEnumAttribute</tt> cannot be found.
     */
    public IEnumAttribute findSuperEnumAttribute(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns <tt>true</tt> if this <tt>IEnumAttribute</tt> is marked to be used as ID in the
     * Faktor-IPS UI, <tt>false</tt> if not.
     * <p>
     * If this <tt>IEnumAttribute</tt> is inherited the property of the super
     * <tt>IEnumAttribute</tt> will be returned. Returns <tt>null</tt> if the super
     * <tt>IEnumAttribute</tt> cannot be found.
     * 
     * @see #isIdentifier()
     * 
     * @param ipsProject The IPS project which IPS object path is used for the search of the super
     *            <tt>IEnumAttribute</tt>. This is not necessarily the project this
     *            <tt>IEnumAttribute</tt> is part of.
     * 
     * @throws CoreException If an error occurs while searching the given IPS project for the super
     *             <tt>IEnumAttribute</tt>.
     * @throws NullPointerException If <tt>ipsProject</tt> is <tt>null</tt>.
     */
    public Boolean findIsIdentifier(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns <tt>true</tt> if this <tt>IEnumAttribute</tt> is used by the first
     * <tt>IEnumLiteralNameAttribute</tt> to obtain default literal names, <tt>false</tt> otherwise.
     * <p>
     * Returns <tt>false</tt> if there is no <tt>IEnumLiteralNameAttribute</tt>.
     * <p>
     * This information is only valid for the <tt>IEnumAttribute</tt> itself, not for copies created
     * due to inheritance.
     */
    public boolean isLiteralNameDefaultValueProvider();

}
