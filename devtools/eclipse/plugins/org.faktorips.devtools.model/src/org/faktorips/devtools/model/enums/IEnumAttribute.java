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
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IVersionControlledElement;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IOverridableElement;
import org.faktorips.devtools.model.type.IOverridableLabeledElement;

/**
 * An {@link IEnumAttribute} is a part of an {@link IEnumType} that describes a property of this
 * {@link IEnumType}.
 * <p>
 * {@link IEnumAttribute IEnumAttributes} are always of a specific data type and can be inherited
 * from an {@link IEnumType} in the super type hierarchy. If an {@link IEnumAttribute} is inherited
 * from the super type hierarchy it is treated as a copy of the original {@link IEnumAttribute}
 * referring to its properties.
 * <p>
 * An {@link IEnumAttribute} can be marked as unique, which implies that each value for this
 * {@link IEnumAttribute} must be unique.
 * <p>
 * Furthermore an {@link IEnumAttribute} can be marked to be used as name in the Faktor-IPS UI or to
 * be used as (default) identifier.
 * <p>
 * For more information about how {@link IEnumAttribute IEnumAttributes} relate to the entire
 * Faktor-IPS enumeration concept please read the documentation of {@link IEnumType}.
 *
 * @see IEnumType
 *
 * @author Alexander Weickmann
 *
 * @since 2.3
 */
public interface IEnumAttribute
        extends IOverridableLabeledElement, IIpsObjectPart, IVersionControlledElement {

    /** The XML tag for this IPS object part. */
    String XML_TAG = "EnumAttribute"; //$NON-NLS-1$

    /** Name of the {@code datatype} property. */
    String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$

    /** Name of the {@code inherited} property. */
    String PROPERTY_INHERITED = "inherited"; //$NON-NLS-1$

    /** Name of the {@code unique} property. */
    String PROPERTY_UNIQUE = "unique"; //$NON-NLS-1$

    /** Name of the {@code usedAsNameInFaktorIpsUi} property. */
    String PROPERTY_USED_AS_NAME_IN_FAKTOR_IPS_UI = "usedAsNameInFaktorIpsUi"; //$NON-NLS-1$

    /** Name of the {@code identifier} property. */
    String PROPERTY_IDENTIFIER = "identifier"; //$NON-NLS-1$

    /** Name of the {@code multilingual} property. */
    String PROPERTY_MULTILINGUAL = "multilingual"; //$NON-NLS-1$

    String PROPERTY_MULTILINGUAL_SUPPORTED = "multilingualSupported"; //$NON-NLS-1$

    String PROPERTY_MANDATORY = "mandatory"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    String MSGCODE_PREFIX = "ENUMATTRIBUTE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name of this {@link IEnumAttribute} is missing.
     */
    String MSGCODE_ENUM_ATTRIBUTE_NAME_MISSING = MSGCODE_PREFIX + "EnumAttributeNameMissing"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name of this {@link IEnumAttribute} is already
     * used.
     */
    String MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_NAME = MSGCODE_PREFIX + "EnumAttributeDuplicateName"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name of this {@link IEnumAttribute} is already
     * used in the super type hierarchy.
     */
    String MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_NAME_IN_SUPERTYPE_HIERARCHY = MSGCODE_PREFIX
            + "EnumAttributeDuplicateNameInSupertypeHierarchy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of this {@link IEnumAttribute} is
     * missing.
     */
    String MSGCODE_ENUM_ATTRIBUTE_DATATYPE_MISSING = MSGCODE_PREFIX
            + "EnumAttributeDatatypeMissing"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of this {@link IEnumAttribute} does
     * not exist.
     */
    String MSGCODE_ENUM_ATTRIBUTE_DATATYPE_DOES_NOT_EXIST = MSGCODE_PREFIX
            + "EnumAttributeDatatypeDoesNotExist"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute is defined multilingual and also used
     * as default identifier.
     */
    String MSGCODE_MULTILINGUAL_ATTRIBUTES_CANNOT_BE_IDENTIFIERS = MSGCODE_PREFIX
            + "IdentifierNotAllowed"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of this {@link IEnumAttribute} is the
     * void data type, which is forbidden.
     */
    String MSGCODE_ENUM_ATTRIBUTE_DATATYPE_IS_VOID = MSGCODE_PREFIX + "EnumAttributeDatatypeIsVoid"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of this {@link IEnumAttribute} is an
     * abstract data type, which is forbidden.
     */
    String MSGCODE_ENUM_ATTRIBUTE_DATATYPE_IS_ABSTRACT = MSGCODE_PREFIX
            + "EnumAttributeDatatypeIsAbstract"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of this {@link IEnumAttribute} is the
     * containing {@link IEnumType} or a subclass of it, which is forbidden.
     */
    String MSGCODE_ENUM_ATTRIBUTE_DATATYPE_IS_CONTAINING_ENUM_TYPE_OR_SUBCLASS = MSGCODE_PREFIX
            + "EnumAttributeDatatypeIsContainingEnumTypeOrSubclass"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this {@link IEnumAttribute} is marked as literal
     * name but is not of data type {@code String}.
     */
    String MSGCODE_ENUM_ATTRIBUTE_LITERAL_NAME_NOT_OF_DATATYPE_STRING = MSGCODE_PREFIX
            + "EnumAttributeLiteralNameNotOfDatatypeString"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this {@link IEnumAttribute} is inherited from the
     * super type hierarchy but there is no such {@link IEnumAttribute} in the supertype hierarchy.
     */
    String MSGCODE_ENUM_ATTRIBUTE_NO_SUCH_ATTRIBUTE_IN_SUPERTYPE_HIERARCHY = MSGCODE_PREFIX
            + "EnumAttributeNoSuchAttributeInSupertypeHierarchy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this {@link IEnumAttribute} is inherited from the
     * super type hierarchy but the containing {@link IEnumType} has no super enumeration type.
     */
    String MSGCODE_ENUM_ATTRIBUTE_INHERITED_BUT_NO_SUPERTYPE = MSGCODE_PREFIX
            + "EnumAttributeInheritedButNoSupertype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there is at least one other {@link IEnumAttribute}
     * marked as literal name in the parent [@link IEnumType}.
     */
    String MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_LITERAL_NAME = MSGCODE_PREFIX
            + "EnumAttributeDuplicateLiteralName"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this {@link IEnumAttribute} is marked to be used as
     * literal name but is not a unique identifier.
     */
    String MSGCODE_ENUM_ATTRIBUTE_LITERAL_NAME_BUT_NOT_UNIQUE_IDENTIFIER = MSGCODE_PREFIX
            + "EnumAttributeLiteralNameButNotUniqueIdentifier"; //$NON-NLS-1$ ;

    /**
     * Validation message code to indicate that there is at least one other {@link IEnumAttribute}
     * marked to be used as name in the Faktor-IPS UI in the parent {@link IEnumType}.
     */
    String MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_USED_AS_NAME_IN_FAKTOR_IPS_UI = MSGCODE_PREFIX
            + "EnumAttributeDuplicateUsedAsNameInFaktorIpsUi"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there is at least one other {@link IEnumAttribute}
     * marked to be used as ID in the Faktor-IPS UI in the parent {@link IEnumType}.
     */
    String MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_USED_AS_ID_IN_FAKTOR_IPS_UI = MSGCODE_PREFIX
            + "EnumAttributeDuplicateUsedAsIdInFaktorIpsUi"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of this {@link IEnumAttribute} is an
     * {@link IEnumType} that does not contain values while the parent {@link IEnumType} does.
     */
    String MSGCODE_ENUM_ATTRIBUTE_ENUM_DATATYPE_DOES_NOT_CONTAIN_VALUES_BUT_PARENT_ENUM_TYPE_DOES = MSGCODE_PREFIX
            + "EnumAttributeEnumDatatypeDoesNotContainValuesButParentEnumTypeDoes"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name of this {@link IEnumAttribute} is not a
     * valid Java field name.
     */
    String MSGCODE_ENUM_ATTRIBUTE_NAME_NOT_A_VALID_FIELD_NAME = MSGCODE_PREFIX
            + "EnumAttributeNameNotAValidFieldName"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there is a mismatch with the setting multilingual at
     * the supertype
     */
    String MSGCODE_ENUM_ATTRIBUTE_INHERITED_LINGUAL_MISMATCH = MSGCODE_PREFIX
            + "EnumAttributeInheritedLingualMismatch"; //$NON-NLS-1$

    /**
     * Sets the name of this {@link IEnumAttribute}.
     *
     * @param name The new name for this {@link IEnumAttribute}.
     *
     * @throws NullPointerException If {@code name} is {@code null}.
     */
    void setName(String name);

    /**
     * Returns the qualified name of the data type of this {@link IEnumAttribute}.
     * <p>
     * <strong>Important:</strong> This operation does not search the supertype hierarchy for the
     * data type if this {@link IEnumAttribute} is inherited. Use {@code findDatatype(IIpsProject)}
     * in this case.
     *
     * @see #findDatatype(IIpsProject)
     */
    String getDatatype();

    /**
     * Returns this {@link IEnumAttribute}'s {@code ValueDatatype}.
     * <p>
     * If this {@link IEnumAttribute} is inherited the {@code ValueDatatype} of the super
     * {@link IEnumAttribute} will be returned.
     * <p>
     * Returns {@code null} if no {@code ValueDatatype} can be found or if the super
     * {@link IEnumAttribute} could not be found.
     *
     * @param ipsProject The IPS project which IPS object path is used for the search of the super
     *            enumeration attribute. This is not necessarily the project this
     *            {@link IEnumAttribute} is part of.
     *
     * @see #getDatatype()
     *
     * @throws IpsException If an error occurs while searching the given IPS project for the value
     *             data type.
     *
     * @throws NullPointerException If {@code ipsProject} is {@code null}.
     */
    ValueDatatype findDatatype(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns this {@link IEnumAttribute}'s {@code ValueDatatype}. If the datatype is another enum
     * type this method returns an {@link EnumTypeDatatypeAdapter} that only reflects the values
     * that are defined in the enum type not the values of any separated content. If the datatype is
     * no enum type it simply returns the same as {@link #findDatatype(IIpsProject)}.
     *
     * @param ipsProject The IPS project which IPS object path is used for the search of the super
     *            enumeration attribute. This is not necessarily the project this
     *            {@link IEnumAttribute} is part of.
     *
     * @see #getDatatype()
     * @see #findDatatype(IIpsProject)
     *
     * @throws IpsException If an error occurs while searching the given IPS project for the value
     *             data type.
     *
     * @throws NullPointerException If {@code ipsProject} is {@code null}.
     */
    ValueDatatype findDatatypeIgnoreEnumContents(IIpsProject ipsProject) throws IpsException;

    /**
     * Sets the data type of this {@link IEnumAttribute}.
     *
     * @param dataType The unqualified name of the data type.
     *
     * @throws NullPointerException If {@code dataType} is {@code null}.
     */
    void setDatatype(String dataType);

    /**
     * Returns {@code true} if this {@link IEnumAttribute} is inherited from the super type
     * hierarchy, {@code false} if not.
     */
    boolean isInherited();

    /**
     * Sets whether this {@link IEnumAttribute} is inherited from the super type hierarchy.
     * <p>
     * If this property is set to {@code true} this {@link IEnumAttribute} is treated like a copy of
     * the original {@link IEnumAttribute} in the respective super {@link IEnumType}. This means the
     * {@code datatype}, {@code unique}, {@code identifier}, {@code usedAsNameInFaktorIpsUi} and
     * {@code mandatory} properties are then derived from the original {@link IEnumAttribute}. The
     * properties will be set to an empty string or {@code false} and the respective setters and
     * getters will throw {@code IllegalStateException}s when called from now on.
     *
     * @param isInherited Flag indicating whether this {@link IEnumAttribute} is inherited from the
     *            super type hierarchy.
     */
    void setInherited(boolean isInherited);

    /**
     * Returns the {@link IEnumType} this {@link IEnumAttribute} belongs to.
     * <p>
     * This is a shortcut for: {@code (IEnumType)this.getParent();}
     */
    IEnumType getEnumType();

    /**
     * Returns {@code true} if by means of this attribute a value of this enumeration type can be
     * identified uniquely.
     * <p>
     * <strong>Important:</strong> This method does not search the super type hierarchy for the
     * {@code unique} property. The method {@link #findIsUnique(IIpsProject)} takes the super type
     * hierarchy into account.
     *
     * @see #findIsUnique(IIpsProject)
     */
    boolean isUnique();

    /**
     * Returns {@code true} if this enumeration attribute is marked as unique, {@code false} if not.
     * <p>
     * If this attribute is inherited the property of the super type attribute will be returned.
     * Returns {@code false} if the super type attribute cannot be found.
     *
     * @see #isUnique()
     *
     * @param ipsProject The IPS project that is used to the search the {@code unique} property in
     *            the super type hierarchy.
     *
     * @throws IpsException If an error occurs while searching
     * @throws NullPointerException If {@code ipsProject} is {@code null}.
     */
    boolean findIsUnique(IIpsProject ipsProject) throws IpsException;

    /**
     * Sets whether this {@link IEnumAttribute} is a unique identifier.
     *
     * @param uniqueIdentifier Flag indicating whether this {@link IEnumAttribute} will be a unique
     *            identifier.
     */
    void setUnique(boolean uniqueIdentifier);

    /**
     * Sets whether the values of this {@link IEnumAttribute} shall be used as name of enumeration
     * values in the Faktor-IPS UI.
     *
     * @param usedAsNameInFaktorIpsUi Flag indicating whether this {@link IEnumAttribute} shall be
     *            used as name of enumeration values in the Faktor-IPS UI ({@code true}) or not (
     *            {@code false}).
     */
    void setUsedAsNameInFaktorIpsUi(boolean usedAsNameInFaktorIpsUi);

    /**
     * Returns a flag indicating whether this {@link IEnumAttribute} is marked to be used as name of
     * enumeration values in the Faktor-IPS UI.
     * <p>
     * <strong>Important:</strong> This operation does not search the super type hierarchy for the
     * {@code usedAsNameInFaktorIpsUi} property if this {@link IEnumAttribute} is inherited. Use
     * {@link #findIsUsedAsNameInFaktorIpsUi(IIpsProject)} in this case.
     *
     * @see #findIsUsedAsNameInFaktorIpsUi(IIpsProject)
     */
    boolean isUsedAsNameInFaktorIpsUi();

    /**
     * Sets whether the values of this {@link IEnumAttribute} shall be used as (default) identifier
     * of enumeration values in the Faktor-IPS UI.
     *
     * @param identifier Flag indicating whether this {@link IEnumAttribute} shall be used as
     *            (default) identifier of enumeration values in the Faktor-IPS UI ({@code true}) or
     *            not ( {@code false}).
     */
    void setIdentifier(boolean identifier);

    /**
     * Returns {@code true} if this attribute is the identifying attribute of the parent
     * {@link IEnumType}. Only one attribute within an {@link IEnumType} can be the identifying
     * attribute.
     * <p>
     * <strong>Important:</strong> This method does not search the super type hierarchy to look for
     * the this property. Use {@link #findIsIdentifier(IIpsProject)} if necessary.
     *
     * @see #findIsIdentifier(IIpsProject)
     */
    boolean isIdentifier();

    /**
     * Returns {@code true} if this {@link IEnumAttribute} is marked to be used as name in the
     * Faktor-IPS UI, {@code false} if not.
     * <p>
     * If this {@link IEnumAttribute} is inherited the property of the super {@link IEnumAttribute}
     * will be returned. Returns {@code false} if the super {@link IEnumAttribute} cannot be found.
     *
     * @see #isUsedAsNameInFaktorIpsUi()
     *
     * @param ipsProject The IPS project which IPS object path is used for the search of the super
     *            {@link IEnumAttribute}. This is not necessarily the project this
     *            {@link IEnumAttribute} is part of.
     *
     * @throws NullPointerException If {@code ipsProject} is {@code null}.
     */
    boolean findIsUsedAsNameInFaktorIpsUi(IIpsProject ipsProject);

    /**
     * Returns the original {@link IEnumAttribute} this {@link IEnumAttribute} is a copy of (if this
     * {@link IEnumAttribute} is inherited).
     * <p>
     * Returns {@code null} if this {@link IEnumAttribute} is not inherited or the super
     * {@link IEnumAttribute} cannot be found.
     */
    IEnumAttribute findSuperEnumAttribute(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns {@code true} if this {@link IEnumAttribute} is marked to be used as ID in the
     * Faktor-IPS UI, {@code false} if not.
     * <p>
     * If this {@link IEnumAttribute} is inherited the property of the super {@link IEnumAttribute}
     * will be returned. Returns {@code false} if the super {@link IEnumAttribute} cannot be found.
     *
     * @see #isIdentifier()
     *
     * @param ipsProject The IPS project which IPS object path is used for the search. This is not
     *            necessarily the project this {@link IEnumAttribute} is part of.
     *
     * @throws NullPointerException If {@code ipsProject} is {@code null}.
     */
    boolean findIsIdentifier(IIpsProject ipsProject);

    /**
     * Returns {@code true} if this {@link IEnumAttribute} is marked as mandatory, {@code false} if
     * not.
     * <p>
     * If this {@link IEnumAttribute} is inherited the property of the super {@link IEnumAttribute}
     * will be returned. Returns {@code false} if the super {@link IEnumAttribute} cannot be found.
     *
     * @see #isMandatory()
     *
     * @param ipsProject The IPS project which IPS object path is used for the search. This is not
     *            necessarily the project this {@link IEnumAttribute} is part of.
     *
     * @throws NullPointerException If {@code ipsProject} is {@code null}.
     */
    boolean findIsMandatory(IIpsProject ipsProject);

    /**
     * Returns {@code true} if this {@link IEnumAttribute} is used by the first
     * {@link IEnumLiteralNameAttribute} to obtain default literal names, {@code false} otherwise.
     * <p>
     * Returns {@code false}if there is no {@link IEnumLiteralNameAttribute}.
     * <p>
     * This information is only valid for the {@link IEnumAttribute} itself, not for copies created
     * due to inheritance.
     */
    boolean isLiteralNameDefaultValueProvider();

    /**
     * Returns a list containing all copies of this {@link IEnumAttribute} in the hierarchy of
     * subclasses.
     *
     * @param ipsProject The IPS project which IPS object path is used for the search. This is not
     *            necessarily the project this {@link IEnumAttribute} is part of.
     *
     * @throws IpsException If an error occurs while searching for the copies.
     * @throws NullPointerException If {@code ipsProject} is {@code null}.
     */
    List<IEnumAttribute> searchInheritedCopies(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns whether this {@link IEnumAttribute} is a {@link IEnumLiteralNameAttribute}.
     */
    boolean isEnumLiteralNameAttribute();

    /**
     * Sets the multilingual property of this attribute. If multilingual is set to {@code true} and
     * the datatype of this attribute is String, the attribute value can be specified in different
     * languages.
     *
     * @param multilingual {@code true} to enable multi language support, {@code false} otherwise.
     */
    void setMultilingual(boolean multilingual);

    /**
     * Returns {@code true} if multi language support for this attribute is activated and supported,
     * false otherwise.
     *
     * Note that the return value of this method can be {@code false} although multi value support
     * has previously been enabled by calling {@code setMultilingual(true)}. This happens if multi
     * language support has been set but is not supported by this attribute (for example, because it
     * has a data type that does not allow more than one language).
     */
    boolean isMultilingual();

    /**
     * Returns {@code true} if multi language support for this attribute is supported, {@code false}
     * otherwise.
     */
    boolean isMultilingualSupported();

    /** {@return whether this attribute is mandatory, always requiring a value} */
    boolean isMandatory();

    /**
     * Sets this attribute to mandatory, always requiring a value (or, with {@code false}, to
     * optional, allowing empty/{@code null} values).
     */
    void setMandatory(boolean mandatory);

    @Override
    default IOverridableElement findOverriddenElement(IIpsProject ipsProject) {
        return findSuperEnumAttribute(ipsProject);
    }
}
