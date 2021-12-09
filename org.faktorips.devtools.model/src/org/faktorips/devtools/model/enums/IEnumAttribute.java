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

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.model.ipsobject.IVersionControlledElement;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * An <code>IEnumAttribute</code> is a part of an <code>IEnumType</code> that describes a property
 * of this <code>IEnumType</code>.
 * <p>
 * <code>IEnumAttribute</code>s are always of a specific data type and can be inherited from an
 * <code>IEnumType</code> in the super type hierarchy. If an <code>IEnumAttribute</code> is
 * inherited from the super type hierarchy it is treated as a copy of the original
 * <code>IEnumAttribute</code> referring to its properties.
 * <p>
 * An <code>IEnumAttribute</code> can be marked as unique, which implies that each value for this
 * <code>IEnumAttribute</code> must be unique.
 * <p>
 * Furthermore an <code>IEnumAttribute</code> can be marked to be used as name in the Faktor-IPS UI
 * or to be used as (default) identifier.
 * <p>
 * For more information about how <code>IEnumAttribute</code>s relate to the entire Faktor-IPS
 * enumeration concept please read the documentation of <code>IEnumType</code>.
 * 
 * @see IEnumType
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumAttribute extends IIpsObjectPart, IDescribedElement, ILabeledElement, IVersionControlledElement {

    /** The XML tag for this IPS object part. */
    public static final String XML_TAG = "EnumAttribute"; //$NON-NLS-1$

    /** Name of the <code>datatype</code> property. */
    public static final String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$

    /** Name of the <code>inherited</code> property. */
    public static final String PROPERTY_INHERITED = "inherited"; //$NON-NLS-1$

    /** Name of the <code>unique</code> property. */
    public static final String PROPERTY_UNIQUE = "unique"; //$NON-NLS-1$

    /** Name of the <code>usedAsNameInFaktorIpsUi</code> property. */
    public static final String PROPERTY_USED_AS_NAME_IN_FAKTOR_IPS_UI = "usedAsNameInFaktorIpsUi"; //$NON-NLS-1$

    /** Name of the <code>identifier</code> property. */
    public static final String PROPERTY_IDENTIFIER = "identifier"; //$NON-NLS-1$

    /** Name of the <code>multilingual</code> property. */
    public static final String PROPERTY_MULTILINGUAL = "multilingual"; //$NON-NLS-1$

    public static final String PROPERTY_MULTILINGUAL_SUPPORTED = "multilingualSupported"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    public static final String MSGCODE_PREFIX = "ENUMATTRIBUTE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name of this <code>IEnumAttribute</code> is
     * missing.
     */
    public static final String MSGCODE_ENUM_ATTRIBUTE_NAME_MISSING = MSGCODE_PREFIX + "EnumAttributeNameMissing"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name of this <code>IEnumAttribute</code> is
     * already used.
     */
    public static final String MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_NAME = MSGCODE_PREFIX + "EnumAttributeDuplicateName"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name of this <code>IEnumAttribute</code> is
     * already used in the super type hierarchy.
     */
    public static final String MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_NAME_IN_SUPERTYPE_HIERARCHY = MSGCODE_PREFIX
            + "EnumAttributeDuplicateNameInSupertypeHierarchy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of this <code>IEnumAttribute</code> is
     * missing.
     */
    public static final String MSGCODE_ENUM_ATTRIBUTE_DATATYPE_MISSING = MSGCODE_PREFIX
            + "EnumAttributeDatatypeMissing"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of this <code>IEnumAttribute</code>
     * does not exist.
     */
    public static final String MSGCODE_ENUM_ATTRIBUTE_DATATYPE_DOES_NOT_EXIST = MSGCODE_PREFIX
            + "EnumAttributeDatatypeDoesNotExist"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute is defined multilingual and also used
     * as default identifier.
     */
    public static final String MSGCODE_MULTILINGUAL_ATTRIBUTES_CANNOT_BE_IDENTIFIERS = MSGCODE_PREFIX
            + "IdentifierNotAllowed"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of this <code>IEnumAttribute</code> is
     * the void data type, which is forbidden.
     */
    public static final String MSGCODE_ENUM_ATTRIBUTE_DATATYPE_IS_VOID = MSGCODE_PREFIX + "EnumAttributeDatatypeIsVoid"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of this <code>IEnumAttribute</code> is
     * an abstract data type, which is forbidden.
     */
    public static final String MSGCODE_ENUM_ATTRIBUTE_DATATYPE_IS_ABSTRACT = MSGCODE_PREFIX
            + "EnumAttributeDatatypeIsAbstract"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of this <code>IEnumAttribute</code> is
     * the containing <code>IEnumType</code> or a subclass of it, which is forbidden.
     */
    public static final String MSGCODE_ENUM_ATTRIBUTE_DATATYPE_IS_CONTAINING_ENUM_TYPE_OR_SUBCLASS = MSGCODE_PREFIX
            + "EnumAttributeDatatypeIsContainingEnumTypeOrSubclass"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this <code>IEnumAttribute</code> is marked as
     * literal name but is not of data type <code>String</code>.
     */
    public static final String MSGCODE_ENUM_ATTRIBUTE_LITERAL_NAME_NOT_OF_DATATYPE_STRING = MSGCODE_PREFIX
            + "EnumAttributeLiteralNameNotOfDatatypeString"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this <code>IEnumAttribute</code> is inherited from
     * the super type hierarchy but there is no such <code>IEnumAttribute</code> in the supertype
     * hierarchy.
     */
    public static final String MSGCODE_ENUM_ATTRIBUTE_NO_SUCH_ATTRIBUTE_IN_SUPERTYPE_HIERARCHY = MSGCODE_PREFIX
            + "EnumAttributeNoSuchAttributeInSupertypeHierarchy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this <code>IEnumAttribute</code> is inherited from
     * the super type hierarchy but the containing <code>IEnumType</code> has no super enumeration
     * type.
     */
    public static final String MSGCODE_ENUM_ATTRIBUTE_INHERITED_BUT_NO_SUPERTYPE = MSGCODE_PREFIX
            + "EnumAttributeInheritedButNoSupertype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there is at least one other
     * <code>IEnumAttribute</code> marked as literal name in the parent <code>IEnumType</code>.
     */
    public static final String MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_LITERAL_NAME = MSGCODE_PREFIX
            + "EnumAttributeDuplicateLiteralName"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this <code>IEnumAttribute</code> is marked to be
     * used as literal name but is not a unique identifier.
     */
    public static final String MSGCODE_ENUM_ATTRIBUTE_LITERAL_NAME_BUT_NOT_UNIQUE_IDENTIFIER = MSGCODE_PREFIX
            + "EnumAttributeLiteralNameButNotUniqueIdentifier"; //$NON-NLS-1$ ;

    /**
     * Validation message code to indicate that there is at least one other
     * <code>IEnumAttribute</code> marked to be used as name in the Faktor-IPS UI in the parent
     * <code>IEnumType</code>.
     */
    public static final String MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_USED_AS_NAME_IN_FAKTOR_IPS_UI = MSGCODE_PREFIX
            + "EnumAttributeDuplicateUsedAsNameInFaktorIpsUi"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there is at least one other
     * <code>IEnumAttribute</code> marked to be used as ID in the Faktor-IPS UI in the parent
     * <code>IEnumType</code>.
     */
    public static final String MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_USED_AS_ID_IN_FAKTOR_IPS_UI = MSGCODE_PREFIX
            + "EnumAttributeDuplicateUsedAsIdInFaktorIpsUi"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of this <code>IEnumAttribute</code> is
     * an <code>IEnumType</code> that does not contain values while the parent
     * <code>IEnumType</code> does.
     */
    public static final String MSGCODE_ENUM_ATTRIBUTE_ENUM_DATATYPE_DOES_NOT_CONTAIN_VALUES_BUT_PARENT_ENUM_TYPE_DOES = MSGCODE_PREFIX
            + "EnumAttributeEnumDatatypeDoesNotContainValuesButParentEnumTypeDoes"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name of this <code>IEnumAttribute</code> is not
     * a valid Java field name.
     */
    public static final String MSGCODE_ENUM_ATTRIBUTE_NAME_NOT_A_VALID_FIELD_NAME = MSGCODE_PREFIX
            + "EnumAttributeNameNotAValidFieldName"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there is a mismatch with the setting multilingual at
     * the supertype
     */
    public static final String MSGCODE_ENUM_ATTRIBUTE_INHERITED_LINGUAL_MISMATCH = MSGCODE_PREFIX
            + "EnumAttributeInheritedLingualMismatch"; //$NON-NLS-1$

    /**
     * Sets the name of this <code>IEnumAttribute</code>.
     * 
     * @param name The new name for this <code>IEnumAttribute</code>.
     * 
     * @throws NullPointerException If <code>name</code> is <code>null</code>.
     */
    public void setName(String name);

    /**
     * Returns the qualified name of the data type of this <code>IEnumAttribute</code>.
     * <p>
     * <strong>Important:</strong> This operation does not search the supertype hierarchy for the
     * data type if this <code>IEnumAttribute</code> is inherited. Use
     * <code>findDatatype(IIpsProject)</code> in this case.
     * 
     * @see #findDatatype(IIpsProject)
     */
    public String getDatatype();

    /**
     * Returns this <code>IEnumAttribute</code>'s <code>ValueDatatype</code>.
     * <p>
     * If this <code>IEnumAttribute</code> is inherited the <code>ValueDatatype</code> of the super
     * <code>IEnumAttribute</code> will be returned.
     * <p>
     * Returns <code>null</code> if no <code>ValueDatatype</code> can be found or if the super
     * <code>IEnumAttribute</code> could not be found.
     * 
     * @param ipsProject The IPS project which IPS object path is used for the search of the super
     *            enumeration attribute. This is not necessarily the project this
     *            <code>IEnumAttribute</code> is part of.
     * 
     * @see #getDatatype()
     * 
     * @throws CoreRuntimeException If an error occurs while searching the given IPS project for the value
     *             data type.
     * @throws NullPointerException If <code>ipsProject</code> is <code>null</code>.
     */
    public ValueDatatype findDatatype(IIpsProject ipsProject) throws CoreRuntimeException;

    /**
     * Returns this <code>IEnumAttribute</code>'s <code>ValueDatatype</code>. If the datatype is
     * another enum type this method returns an {@link EnumTypeDatatypeAdapter} that only reflects
     * the values that are defined in the enum type not the values of any separated content. If the
     * datatype is no enum type it simply returns the same as {@link #findDatatype(IIpsProject)}.
     * 
     * @param ipsProject The IPS project which IPS object path is used for the search of the super
     *            enumeration attribute. This is not necessarily the project this
     *            <code>IEnumAttribute</code> is part of.
     * 
     * @see #getDatatype()
     * @see #findDatatype(IIpsProject)
     * 
     * @throws CoreRuntimeException If an error occurs while searching the given IPS project for the value
     *             data type.
     * @throws NullPointerException If <code>ipsProject</code> is <code>null</code>.
     */
    public ValueDatatype findDatatypeIgnoreEnumContents(IIpsProject ipsProject) throws CoreRuntimeException;

    /**
     * Sets the data type of this <code>IEnumAttribute</code>.
     * 
     * @param dataType The unqualified name of the data type.
     * 
     * @throws NullPointerException If <code>dataType</code> is <code>null</code>.
     */
    public void setDatatype(String dataType);

    /**
     * Returns <code>true</code> if this <code>IEnumAttribute</code> is inherited from the super
     * type hierarchy, <code>false</code> if not.
     */
    public boolean isInherited();

    /**
     * Sets whether this <code>IEnumAttribute</code> is inherited from the super type hierarchy.
     * <p>
     * If this property is set to <code>true</code> this <code>IEnumAttribute</code> is treated like
     * a copy of the original <code>IEnumAttribute</code> in the respective super
     * <code>IEnumType</code>. This means the <code>datatype</code>, <code>unique</code>,
     * <code>identifier</code> and <code>usedAsNameInFaktorIpsUi</code> properties are then derived
     * from the original <code>IEnumAttribute</code>. The properties will be set to an empty string
     * or <code>false</code> and the respective setters and getters will throw
     * <code>IllegalStateException</code>s when called from now on.
     * 
     * @param isInherited Flag indicating whether this <code>IEnumAttribute</code> is inherited from
     *            the super type hierarchy.
     */
    public void setInherited(boolean isInherited);

    /**
     * Returns the <code>IEnumType</code> this <code>IEnumAttribute</code> belongs to.
     * <p>
     * This is a shortcut for: <code>(IEnumType)this.getParent();</code>
     */
    public IEnumType getEnumType();

    /**
     * Returns <code>true</code> if by means of this attribute a value of this enumeration type can
     * be identified uniquely.
     * <p>
     * <strong>Important:</strong> This method does not search the super type hierarchy for the
     * <code>unique</code> property. The method <code>findIsUnique()</code> takes the super type
     * hierarchy into account.
     * 
     * @see #findIsUnique(IIpsProject)
     */
    public boolean isUnique();

    /**
     * Returns <code>true</code> if this enumeration attribute is marked as unique,
     * <code>false</code> if not.
     * <p>
     * If this attribute is inherited the property of the super type attribute will be returned.
     * Returns <code>false</code> if the super type attribute cannot be found.
     * 
     * @see #isUnique()
     * 
     * @param ipsProject The IPS project that is used to the search the <code>unique</code> property
     *            in the super type hierarchy.
     * 
     * @throws CoreRuntimeException If an error occurs while searching
     * @throws NullPointerException If <code>ipsProject</code> is <code>null</code>.
     */
    public boolean findIsUnique(IIpsProject ipsProject) throws CoreRuntimeException;

    /**
     * Sets whether this <code>IEnumAttribute</code> is a unique identifier.
     * 
     * @param uniqueIdentifier Flag indicating whether this <code>IEnumAttribute</code> will be a
     *            unique identifier.
     */
    public void setUnique(boolean uniqueIdentifier);

    /**
     * Sets whether the values of this <code>IEnumAttribute</code> shall be used as name of
     * enumeration values in the Faktor-IPS UI.
     * 
     * @param usedAsNameInFaktorIpsUi Flag indicating whether this <code>IEnumAttribute</code> shall
     *            be used as name of enumeration values in the Faktor-IPS UI (<code>true</code>) or
     *            not ( <code>false</code>).
     */
    public void setUsedAsNameInFaktorIpsUi(boolean usedAsNameInFaktorIpsUi);

    /**
     * Returns a flag indicating whether this <code>IEnumAttribute</code> is marked to be used as
     * name of enumeration values in the Faktor-IPS UI.
     * <p>
     * <strong>Important:</strong> This operation does not search the super type hierarchy for the
     * <code>usedAsNameInFaktorIpsUi</code> property if this <code>IEnumAttribute</code> is
     * inherited. Use <code>findIsUsedAsNameInFaktorIpsUi()</code> in this case.
     * 
     * @see #findIsUsedAsNameInFaktorIpsUi(IIpsProject)
     */
    public boolean isUsedAsNameInFaktorIpsUi();

    /**
     * Sets whether the values of this <code>IEnumAttribute</code> shall be used as (default)
     * identifier of enumeration values in the Faktor-IPS UI.
     * 
     * @param identifier Flag indicating whether this <code>IEnumAttribute</code> shall be used as
     *            (default) identifier of enumeration values in the Faktor-IPS UI
     *            (<code>true</code>) or not ( <code>false</code>).
     */
    public void setIdentifier(boolean identifier);

    /**
     * Returns <code>true</code> if this attribute is the identifying attribute of the parent
     * <code>IEnumType</code>. Only one attribute within an <code>IEnumType</code> can be the
     * identifying attribute.
     * <p>
     * <strong>Important:</strong> This method does not search the super type hierarchy to look for
     * the this property. Use <code>findIsIdentifier()</code> if necessary.
     * 
     * @see #findIsIdentifier(IIpsProject)
     */
    public boolean isIdentifier();

    /**
     * Returns <code>true</code> if this <code>IEnumAttribute</code> is marked to be used as name in
     * the Faktor-IPS UI, <code>false</code> if not.
     * <p>
     * If this <code>IEnumAttribute</code> is inherited the property of the super
     * <code>IEnumAttribute</code> will be returned. Returns <code>false</code> if the super
     * <code>IEnumAttribute</code> cannot be found.
     * 
     * @see #isUsedAsNameInFaktorIpsUi()
     * 
     * @param ipsProject The IPS project which IPS object path is used for the search of the super
     *            <code>IEnumAttribute</code>. This is not necessarily the project this
     *            <code>IEnumAttribute</code> is part of.
     * 
     * @throws NullPointerException If <code>ipsProject</code> is <code>null</code>.
     */
    public boolean findIsUsedAsNameInFaktorIpsUi(IIpsProject ipsProject);

    /**
     * Returns the original <code>IEnumAttribute</code> this <code>IEnumAttribute</code> is a copy
     * of (if this <code>IEnumAttribute</code> is inherited).
     * <p>
     * Returns <code>null</code> if this <code>IEnumAttribute</code> is not inherited or the super
     * <code>IEnumAttribute</code> cannot be found.
     */
    public IEnumAttribute findSuperEnumAttribute(IIpsProject ipsProject) throws CoreRuntimeException;

    /**
     * Returns <code>true</code> if this <code>IEnumAttribute</code> is marked to be used as ID in
     * the Faktor-IPS UI, <code>false</code> if not.
     * <p>
     * If this <code>IEnumAttribute</code> is inherited the property of the super
     * <code>IEnumAttribute</code> will be returned. Returns <code>false</code> if the super
     * <code>IEnumAttribute</code> cannot be found.
     * 
     * @see #isIdentifier()
     * 
     * @param ipsProject The IPS project which IPS object path is used for the search. This is not
     *            necessarily the project this <code>IEnumAttribute</code> is part of.
     * 
     * @throws NullPointerException If <code>ipsProject</code> is <code>null</code>.
     */
    public boolean findIsIdentifier(IIpsProject ipsProject);

    /**
     * Returns <code>true</code> if this <code>IEnumAttribute</code> is used by the first
     * <code>IEnumLiteralNameAttribute</code> to obtain default literal names, <code>false</code>
     * otherwise.
     * <p>
     * Returns <code>false</code> if there is no <code>IEnumLiteralNameAttribute</code>.
     * <p>
     * This information is only valid for the <code>IEnumAttribute</code> itself, not for copies
     * created due to inheritance.
     */
    public boolean isLiteralNameDefaultValueProvider();

    /**
     * Returns a list containing all copies of this <code>IEnumAttribute</code> in the hierarchy of
     * subclasses.
     * 
     * @param ipsProject The IPS project which IPS object path is used for the search. This is not
     *            necessarily the project this <code>IEnumAttribute</code> is part of.
     * 
     * @throws CoreRuntimeException If an error occurs while searching for the copies.
     * @throws NullPointerException If <code>ipsProject</code> is <code>null</code>.
     */
    public List<IEnumAttribute> searchInheritedCopies(IIpsProject ipsProject) throws CoreRuntimeException;

    /**
     * Returns whether this <code>IEnumAttribute</code> is a <code>IEnumLiteralNameAttribute</code>.
     */
    public boolean isEnumLiteralNameAttribute();

    /**
     * Sets the multilingual property of this attribute. If multilingual is set to <code>true</code>
     * and the datatype of this attribute is String, the attribute value can be specified in
     * different languages.
     * 
     * @param multilingual true to enable multi language support, false otherwise.
     */
    public void setMultilingual(boolean multilingual);

    /**
     * Returns true if multi language support for this attribute is activated and supported, false
     * otherwise.
     * 
     * Note that the return value of this method can be false although multi value support has
     * previously been enabled by calling <code>setMultilingual(true)</code>. This happens if multi
     * language support has been set but is not supported by this attribute (for example, because it
     * has a data type that does not allow more than one language).
     */
    public boolean isMultilingual();

    /**
     * Returns true if multi language support for this attribute is supported, false otherwise.
     */
    public boolean isMultilingualSupported();
}
