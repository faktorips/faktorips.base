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
import java.util.Set;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsMetaClass;
import org.faktorips.devtools.model.ipsobject.IVersionControlledElement;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IOverridableElement;

/**
 * An <code>IEnumType</code> represents the structure of an enumeration in the Faktor-IPS model.
 * <p>
 * It contains several <code>IEnumAttribute</code>s where each <code>IEnumAttribute</code>
 * represents a property of the enumeration.
 * <p>
 * For example there may be an <code>IEnumType</code> <code>Gender</code> with the
 * <code>IEnumAttribute</code>s <code>id</code> and <code>name</code>.
 * <p>
 * Instances of an enumeration are represented by <code>IEnumValue</code>s. In the above example
 * there would be two <code>IEnumValue</code>s:
 * <ul>
 * <li>id: m, name: male</li>
 * <li>id: w, name: female</li>
 * </ul>
 * <p>
 * <code>IEnumValue</code>s can be defined directly in the <code>IEnumType</code> itself or separate
 * from it as product content (<code>IEnumContent</code>).
 * <p>
 * There must exist exactly one <code>IEnumLiteralNameAttribute</code> that will be used to identify
 * the respective <code>IEnumValue</code>s in the generated source code if the values are defined
 * directly in the <code>IEnumType</code>.
 * 
 * @see IEnumContent
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumType extends IOverridableElement, IEnumValueContainer, IIpsMetaClass, IVersionControlledElement {

    /** The XML tag for this IPS object. */
    String XML_TAG = "EnumType"; //$NON-NLS-1$

    /** Name of the <code>superEnumType</code> property. */
    String PROPERTY_SUPERTYPE = "superEnumType"; //$NON-NLS-1$

    /** Name of the <code>abstract</code> property. */
    String PROPERTY_ABSTRACT = "abstract"; //$NON-NLS-1$

    /** Name of the <code>extensible</code> property. */
    String PROPERTY_EXTENSIBLE = "extensible"; //$NON-NLS-1$

    /** Name of the <code>identifierBoundary</code> property. */
    String PROPERTY_IDENTIFIER_BOUNDARY = "identifierBoundary"; //$NON-NLS-1$

    /** Name of the <code>enumContentPackageFragment</code> property. */
    String PROPERTY_ENUM_CONTENT_NAME = "enumContentName"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    String MSGCODE_PREFIX = "ENUMTYPE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the super type of this <code>IEnumType</code> does
     * not exist.
     */
    String MSGCODE_ENUM_TYPE_SUPERTYPE_DOES_NOT_EXIST = MSGCODE_PREFIX
            + "EnumTypeSupertypeDoesNotExist"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the super type of this <code>IEnumType</code> is not
     * abstract.
     */
    String MSGCODE_ENUM_TYPE_SUPERTYPE_IS_NOT_ABSTRACT = MSGCODE_PREFIX
            + "EnumTypeSupertypeIsNotAbstract"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that not all attributes defined in the the super type
     * hierarchy have been inherited.
     */
    String MSGCODE_ENUM_TYPE_NOT_INHERITED_ATTRIBUTES_IN_SUPERTYPE_HIERARCHY = MSGCODE_PREFIX
            + "EnumTypeNotInheritedAttributesInSupertypeHierarchy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this <code>IEnumType</code> has no
     * <code>IEnumLiteralNameAttribute</code>.
     */
    String MSGCODE_ENUM_TYPE_NO_LITERAL_NAME_ATTRIBUTE = MSGCODE_PREFIX
            + "EnumTypeNoLiteralNameAttribute"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this <code>IEnumType</code> has multiple
     * <code>IEnumLiteralNameAttribute</code>s.
     */
    String MSGCODE_ENUM_TYPE_MULTIPLE_LITERAL_NAME_ATTRIBUTES = MSGCODE_PREFIX
            + "EnumTypeMultipleLiteralNameAttributes"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there exists a cycle in the hierarchy of this
     * enumeration type.
     */
    String MSGCODE_CYCLE_IN_TYPE_HIERARCHY = MSGCODE_PREFIX + "CycleInTypeHierarchy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there exists an inconsistency in the hierarchy of
     * this enumeration type. The inconsistency can result from a type in the super type hierarchy
     * that is missing its super type or that the super type is not abstract which is an additional
     * constraint for enumeration types.
     */
    String MSGCODE_INCONSISTENT_TYPE_HIERARCHY = MSGCODE_PREFIX + "InconsistentTypeHierachy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this <code>IEnumType</code> does not contain any
     * <code>IEnumAttribute</code> being marked to be used as (default) identifier while not being
     * abstract.
     */
    String MSGCODE_ENUM_TYPE_NO_USED_AS_ID_IN_FAKTOR_IPS_UI_ATTRIBUTE = MSGCODE_PREFIX
            + "EnumTypeNoUsedAsIdInFaktorIpsUiAttribute"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this <code>IEnumType</code> does not contain any
     * <code>IEnumAttribute</code> being marked to be used as name in the Faktor-IPS UI while not
     * being abstract.
     */
    String MSGCODE_ENUM_TYPE_NO_USED_AS_NAME_IN_FAKTOR_IPS_UI_ATTRIBUTE = MSGCODE_PREFIX
            + "EnumTypeNoUsedAsNameInFaktorIpsUiAttribute"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name specification of the
     * <code>IEnumContent</code> is empty while this <code>IEnumType</code> delegates it's
     * enumeration values.
     */
    String MSGCODE_ENUM_TYPE_ENUM_CONTENT_NAME_EMPTY = MSGCODE_PREFIX
            + "EnumTypeEnumContentNameEmpty"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the <code>IEnumType</code> stores
     * <code>IEnumValue</code>s even tough it is abstract.
     */
    String MSGCODE_ENUM_TYPE_ENUM_VALUES_OBSOLETE = MSGCODE_PREFIX + "EnumTypeEnumValuesObsolete"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the specified <code>IEnumContent</code> is already
     * used by another <code>IEnumType</code>.
     */
    String MSGCODE_ENUM_TYPE_ENUM_CONTENT_ALREADY_USED = MSGCODE_PREFIX
            + "EnumTypeEnumContentAlreadyUsed"; //$NON-NLS-1$

    /** Returns the qualified name a referencing <code>IEnumContent</code> needs to have. */
    String getEnumContentName();

    /**
     * @return The IEnumContent referencing this EnumType.
     */
    IEnumContent findEnumContent(IIpsProject ipsProject) throws IpsException;

    /**
     * Sets the qualified name a referencing <code>IEnumContent</code> must have.
     * 
     * @param name The qualified name a referencing <code>IEnumContent</code> must be have.
     * 
     * @throws NullPointerException If <code>name</code> is <code>null</code>.
     */
    void setEnumContentName(String name);

    /**
     * Returns the qualified name of the super enumeration type of this <code>IEnumType</code>.
     * <p>
     * An empty <code>String</code> will be returned if this <code>IEnumType</code> does not have a
     * super enumeration type.
     */
    String getSuperEnumType();

    /**
     * Sets the super enumeration type for this <code>IEnumType</code>.
     * 
     * @param superEnumTypeQualifiedName The qualified name of the super enumeration type or an
     *            empty <code>String</code> if there shall be no super enumeration type.
     * 
     * @throws NullPointerException If <code>superEnumTypeQualifiedName</code> is <code>null</code>.
     */
    void setSuperEnumType(String superEnumTypeQualifiedName);

    /**
     * Returns <code>true</code> if this <code>IEnumType</code> is a sub enumeration type of the
     * given super enumeration type candidate, <code>false</code> otherwise. Returns also
     * <code>false</code> if the super enumeration type candidate is <code>null</code>.
     * 
     * @param superEnumTypeCandidate The type which is possibly a super enumeration type of this
     *            <code>IEnumType</code>.
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     */
    boolean isSubEnumTypeOf(IEnumType superEnumTypeCandidate, IIpsProject ipsProject);

    /**
     * Returns <code>true</code> if this <code>IEnumType</code> is a sub enumeration type of the
     * given candidate, or if the candidate is the same. Returns <code>false</code> otherwise.
     * Returns also <code>false</code> if candidate is <code>null</code>.
     * 
     * @param superEnumTypeCandidate The <code>IEnumType</code> which is the possibly a super
     *            enumeration type of this <code>IEnumType</code>.
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     */
    boolean isSubEnumTypeOrSelf(IEnumType superEnumTypeCandidate, IIpsProject ipsProject);

    /**
     * Searches and returns the super enumeration type of this <code>IEnumType</code> if any is
     * specified.
     * <p>
     * Returns <code>null</code> if no super enumeration type is specified.
     * 
     * @param ipsProject The IPS project which IPS object path is used for the search of the super
     *            enumeration type. This is not necessarily the project this <code>IEnumType</code>
     *            is part of.
     * 
     * @throws NullPointerException If <code>ipsProject</code> is <code>null</code>.
     */
    IEnumType findSuperEnumType(IIpsProject ipsProject);

    /**
     * Searches and returns all enumeration types that subclass this enumeration type.
     * 
     * @throws IpsException If an error occurs while searching for sub classing enumeration types.
     */
    Set<IEnumType> searchSubclassingEnumTypes() throws IpsException;

    /**
     * Sets the abstract property for this <code>IEnumType</code>.
     * 
     * @param isAbstract Flag indicating whether this <code>IEnumType</code> shall be abstract (
     *            <code>true</code>) or not (<code>false</code>).
     */
    void setAbstract(boolean isAbstract);

    /**
     * Returns <code>true</code> if this {@link IEnumType} does contains at least one value.
     */
    boolean containsValues();

    /**
     * Returns <code>true</code> if this <code>IEnumType</code> is extensible.
     * <p>
     * An <code>IEnumType</code> is extensible if its values can be defined in the
     * <code>IEnumType</code> itself AND in a separated content.
     */
    boolean isExtensible();

    /**
     * Sets whether this <code>IEnumType</code> will be extensible.
     * 
     * @param extensible Flag indicating the extensibility of this <code>IEnumType</code>.
     */
    void setExtensible(boolean extensible);

    /**
     * Returns the boundary for the values of the identifier attribute, or an empty string or
     * <code>null</code> for no boundary.
     * <p>
     * This functionality is useful if this {@link IEnumType} is extensible. The user can explicitly
     * define a boundary for identifiers used in this {@link IEnumType} and in the extending
     * {@link IEnumContent}. Values in this {@link IEnumType} can ONLY use identifiers less than the
     * boundary. Values in the corresponding {@link IEnumContent} can ONLY use identifiers greater
     * than or equal to the boundary. Therefore the identifier-attribute's data-type has to
     * implement {@link Comparable}.
     */
    String getIdentifierBoundary();

    /**
     * Sets the identifier attribute boundary for this {@link IEnumType}.
     * <p>
     * If the boundary is set to the empty string or <code>null</code> then no restriction will be
     * made for the values in the {@link IEnumType} and its corresponding {@link IEnumContent}.
     * 
     * @see #getIdentifierBoundary()
     */
    void setIdentifierBoundary(String identifierBoundary);

    /**
     * Returns a list containing all <code>IEnumAttribute</code>s that belong to this
     * <code>IEnumType</code> .
     * <p>
     * <code>IEnumAttribute</code>s that are inherited from the super type hierarchy are
     * <strong>not</strong> included.
     * 
     * @see #getEnumAttributesIncludeSupertypeCopies(boolean)
     * 
     * @param includeLiteralName When set to <code>true</code> the
     *            <code>IEnumLiteralNameAttribute</code>s will be contained in the returned list.
     */
    List<IEnumAttribute> getEnumAttributes(boolean includeLiteralName);

    /**
     * Returns a list containing all <code>IEnumAttribute</code>s that belong to this
     * <code>IEnumType</code> <strong>plus</strong> all <code>IEnumAttribute</code>s that have been
     * inherited from the super type hierarchy (these are not the original
     * <code>IEnumAttribute</code>s defined in the respective super types but copies created based
     * upon the originals).
     * <p>
     * If the original <code>IEnumAttribute</code>s defined in the respective super types are needed
     * use <code>findAllEnumAttributesIncludeSupertypeOriginals(boolean, IIpsProject)</code>.
     * 
     * @see #getEnumAttributes(boolean)
     * 
     * @param includeLiteralName When set to <code>true</code> the
     *            <code>IEnumLiteralNameAttribute</code>s will be contained in the returned list.
     */
    List<IEnumAttribute> getEnumAttributesIncludeSupertypeCopies(boolean includeLiteralName);

    /**
     * Returns a list containing all <code>IEnumAttribute</code>s that belong to this
     * <code>IEnumType</code> <strong>plus</strong> all <code>IEnumAttribute</code>s that belong to
     * super types of this <code>IEnumType</code>.
     * <p>
     * If attributes are inherited, the original attributes are <strong>not</strong> included.
     * <p>
     * If there are multiple attributes with the same name and none of them is marked as inherited,
     * all of those attributes (or "duplicates") are contained in the resulting list. This case is
     * necessary to detect errors during object validation.
     * 
     * @see #getEnumAttributes(boolean)
     * @see #getEnumAttributesIncludeSupertypeCopies(boolean)
     * 
     * @param includeLiteralName If this flag is <code>true</code> all
     *            <code>IEnumLiteralNameAttribute</code>s will be contained in the returned list.
     * @param ipsProject The IPS project which IPS object path is used for the search of the super
     *            enumeration types. This is not necessarily the project this <code>IEnumType</code>
     *            is part of.
     * 
     * @throws NullPointerException If <code>ipsProject</code> is <code>null</code>.
     */
    List<IEnumAttribute> findAllEnumAttributes(boolean includeLiteralName, IIpsProject ipsProject);

    /**
     * Looks up the enumeration attribute for which the <code>identifier</code> property is
     * <code>true</code> and returns it. If none is found <code>null</code> will be returned.
     * 
     * @param ipsProject The IPS project used for look up in the super type hierarchy if necessary.
     */
    IEnumAttribute findIdentiferAttribute(IIpsProject ipsProject);

    /**
     * Looks up the enumeration attribute for which the <code>isUsedAsNameInFaktorIpsUi</code> is
     * <code>true</code>. Returns <code>null</code> if none is found.
     * 
     * @param ipsProject The IPS project used for look up in the super type hierarchy if necessary.
     */
    IEnumAttribute findUsedAsNameInFaktorIpsUiAttribute(IIpsProject ipsProject);

    /**
     * Returns the index of the given <code>IEnumAttribute</code> or -1 if the given
     * <code>IEnumAttribute</code> does not exist in this <code>IEnumType</code>.
     * <p>
     * Be careful: If the given <code>IEnumAttribute</code> is an original from the super type
     * hierarchy, for which this <code>IEnumType</code> only stores a copy, the element won't be
     * found!
     * 
     * @param enumAttribute The <code>IEnumAttribute</code> to obtain its index for.
     * @param considerLiteralName <code>true</code> if you want to consider the literal name
     *            columns, <code>false</code> to ignore it.
     * 
     * @throws NullPointerException If <code>enumAttribute</code> is <code>null</code>.
     */
    int getIndexOfEnumAttribute(IEnumAttribute enumAttribute, boolean considerLiteralName);

    /**
     * Returns the index of the first <code>IEnumLiteralNameAttribute</code> or -1 if no
     * <code>IEnumLiteralNameAttribute</code> exists in this <code>IEnumType</code>.
     */
    int getIndexOfEnumLiteralNameAttribute();

    /**
     * Returns the <code>IEnumAttribute</code> with the given name or <code>null</code> if there is
     * no <code>IEnumAttribute</code> with the given name in this <code>IEnumType</code>.
     * <p>
     * Inherited <code>IEnumAttribute</code>s are <strong>not</strong> included in the search.
     * 
     * @see #getEnumAttributeIncludeSupertypeCopies(String)
     * @see #findEnumAttributeIncludeSupertypeOriginals(IIpsProject, String)
     * 
     * @param name The name of the <code>IEnumAttribute</code> to obtain.
     * 
     * @throws NullPointerException If <code>name</code> is <code>null</code>.
     */
    IEnumAttribute getEnumAttribute(String name);

    /**
     * Returns the <code>IEnumAttribute</code> with the given name or <code>null</code> if there is
     * no <code>IEnumAttribute</code> with the given name in this <code>IEnumType</code>.
     * <p>
     * Inherited <code>IEnumAttribute</code>s <strong>are</strong> included in the search. Note that
     * in this context an inherited <code>IEnumAttribute</code> is just a copy referring to the
     * original <code>IEnumAttribute</code> defined in the respective super enumeration type.
     * 
     * @see #getEnumAttributes(boolean)
     * 
     * @param name The name of the <code>IEnumAttribute</code> to obtain.
     * 
     * @throws NullPointerException If <code>name</code> is <code>null</code>.
     */
    IEnumAttribute getEnumAttributeIncludeSupertypeCopies(String name);

    /**
     * Returns the <code>IEnumAttribute</code> with the given name or <code>null</code> if there is
     * no <code>IEnumAttribute</code> with the given name in this <code>IEnumType</code> or in the
     * super type hierarchy.
     * <p>
     * Note that <code>IEnumAttribute</code>s <strong>defined in super enumeration types are
     * included</strong> in the search and <strong>copies</strong> created due to inheritance
     * <strong>are ignored</strong>.
     * 
     * @param ipsProject The IPS project which IPS object path is used for the search of the super
     *            enumeration types. This is not necessarily the project this <code>IEnumType</code>
     *            is part of.
     * @param name The name of the <code>IEnumAttribute</code> to obtain.
     * 
     * @throws NullPointerException If <code>ipsProject</code> or <code>name</code> is
     *             <code>null</code>.
     */
    IEnumAttribute findEnumAttributeIncludeSupertypeOriginals(IIpsProject ipsProject, String name);

    /**
     * Creates a new <code>IEnumAttribute</code> and returns a reference to it.
     * <p>
     * Note that for all <code>IEnumValue</code>s <strong>that are defined directly in this
     * <code>IEnumType</code></strong> new <code>IEnumAttributeValue</code>s will be created (these
     * will also be moved to their right positions).
     * <p>
     * Fires a <code>WHOLE_CONTENT_CHANGED</code> event.
     * 
     * @throws IpsException If an error occurs while creating the new <code>IEnumAttribute</code>.
     */
    IEnumAttribute newEnumAttribute() throws IpsException;

    /**
     * Creates a new <code>IEnumLiteralNameAttribute</code> and returns a reference to it. The
     * attribute will already have a default name and be of data type <code>String</code>.
     * <p>
     * Note that for all <code>IEnumValue</code>s <strong>that are defined directly in this
     * <code>IEnumType</code></strong> new <code>IEnumAttributeValue</code>s will be created.
     * <p>
     * Fires a <code>WHOLE_CONTENT_CHANGED</code> event.
     * 
     * @throws IpsException If an error occurs while creating the new
     *             <code>IEnumLiteralNameAttribute</code>.
     */
    IEnumLiteralNameAttribute newEnumLiteralNameAttribute() throws IpsException;

    /**
     * Returns how many <code>IEnumAttribute</code>s are currently part of this
     * <code>IEnumType</code>.
     * <p>
     * This operation does <strong>not</strong> inherited <code>IEnumAttribute</code>s.
     * 
     * @see #getEnumAttributesCountIncludeSupertypeCopies(boolean)
     * 
     * @param includeLiteralName When set to <code>true</code> the
     *            <code>IEnumLiteralNameAttribute</code>s will be counted, too.
     */
    int getEnumAttributesCount(boolean includeLiteralName);

    /**
     * Returns how many <code>IEnumAttribute</code>s are currently part of this
     * <code>IEnumType</code>.
     * <p>
     * this operation <strong>does</strong> count inherited <code>IEnumAttribute</code>s.
     * 
     * @see #getEnumAttributesCount(boolean)
     * 
     * @param includeLiteralName When set to <code>true</code> the
     *            <code>IEnumLiteralNameAttribute</code>s will be counted, too.
     */
    int getEnumAttributesCountIncludeSupertypeCopies(boolean includeLiteralName);

    /**
     * Moves the given <code>IEnumAttribute</code> one position up or down and returns its new
     * index.
     * <p>
     * If the given <code>IEnumAttribute</code> is already the first / last
     * <code>IEnumAttribute</code> then nothing will be done.
     * <p>
     * Note that all referencing <code>IEnumAttributeValues</code> <strong>that are defined in this
     * <code>IEnumType</code></strong> will also be moved one position up / down.
     * <p>
     * Fires a <code>WHOLE_CONTENT_CHANGED</code> event if moving was performed.
     * 
     * @param enumAttribute The <code>IEnumAttribute</code> to move.
     * @param up Flag indicating whether to move up (<code>true</code>) or down
     *            (<code>false</code>).
     * 
     * @throws IpsException If an error occurs while moving the <code>IEnumAttribute</code>.
     * @throws NullPointerException If <code>enumAttribute</code> is <code>null</code>.
     * @throws NoSuchElementException If the given <code>IEnumAttribute</code> is not a part of this
     *             <code>IEnumType</code>.
     */
    int moveEnumAttribute(IEnumAttribute enumAttribute, boolean up) throws IpsException;

    /** Returns whether this <code>IEnumType</code> has a super enumeration type. */
    boolean hasSuperEnumType();

    /**
     * Returns <code>true</code> if values are ONLY definable in this {@link IEnumType} and not in a
     * content. Returns <code>false</code> if the enum type is either extensible or is abstract and
     * hence doesn't allow values at all.
     * <p>
     * This is the case when this <code>IEnumType</code> is not abstract and is not extensible.
     */
    boolean isInextensibleEnum();

    /**
     * Returns whether this <code>IEnumType</code> has a super enumeration type that really exists.
     * 
     * @param ipsProject The <code>IIpsProject</code> that provides the object path that is used to
     *            search for the super enumeration type.
     */
    boolean hasExistingSuperEnumType(IIpsProject ipsProject);

    /** Returns if this <code>IEnumType</code> is abstract. */
    boolean isAbstract();

    /**
     * Searches all <code>IEnumType</code>s in the super type hierarchy this <code>IEnumType</code>
     * is a subtype of and returns them in a list (the <code>IEnumType</code> the operation is
     * called upon is not included in the list).
     * <p>
     * It is possible that a cycle is detected in the super type hierarchy. In this case the
     * returned list will contain all super enumeration types up to the point where the cycle was
     * found.
     * <p>
     * Never returns <code>null</code>.
     * 
     * @param ipsProject The IPS project which IPS object path is used for the search of the super
     *            enumeration types. This is not necessarily the project this
     *            <code>IEnumAttribute</code> is part of.
     * 
     * @throws NullPointerException If <code>ipsProject</code> is <code>null</code>.
     */
    List<IEnumType> findAllSuperEnumTypes(IIpsProject ipsProject);

    /**
     * Returns a list containing all <code>IEnumAttribute</code>s from the super type hierarchy that
     * have not yet been inherited by this <code>IEnumType</code>.
     * 
     * @param ipsProject The IPS project which IPS object path is used for the search of the super
     *            enumeration types. This is not necessarily the project this
     *            <code>IEnumAttribute</code> is part of.
     * 
     * @throws IpsException If an error occurs while searching the super type hierarchy for the not
     *             inherited <code>IEnumAttribute</code>s.
     * @throws NullPointerException If <code>ipsProject</code> is <code>null</code>.
     */
    List<IEnumAttribute> findInheritEnumAttributeCandidates(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns a list containing all <code>IEnumAttribute</code>s that are unique. Attributes
     * inherited from the supertype hierarchy are also scanned (only their copies will be returned
     * however).
     * 
     * @param includeLiteralName If this flag is <code>true</code> the
     *            <code>IEnumLiteralNameAttribute</code>s of this <code>IEnumType</code> will be
     *            contained in the returned list (<em>those of the super enumeration types
     *            not</em>).
     * @param ipsProject The IPS project which IPS object path is used for the search.
     * 
     * @throws IpsException If an error occurs while searching the super type hierarchy.
     * @throws NullPointerException If <code>ipsProject</code> is <code>null</code>.
     */
    List<IEnumAttribute> findUniqueEnumAttributes(boolean includeLiteralName, IIpsProject ipsProject)
            throws IpsException;

    /**
     * Creates and returns new <code>IEnumAttribute</code>s in this <code>IEnumType</code>
     * inheriting the given <code>IEnumAttribute</code>s from the super type hierarchy.
     * <p>
     * If any of the given super enumeration attributes is already inherited by this
     * <code>IEnumType</code> it will be skipped.
     * 
     * @param superEnumAttributes The <code>IEnumAttribute</code>s from the super type hierarchy to
     *            inherit by this <code>IEnumType</code>.
     * 
     * @throws IpsException If an error occurs while searching the super type hierarchy.
     * @throws IllegalArgumentException If any of the given <code>IEnumAttribute</code>s is not part
     *             of the supertype hierarchy of this <code>IEnumType</code>.
     */
    List<IEnumAttribute> inheritEnumAttributes(List<IEnumAttribute> superEnumAttributes) throws IpsException;

    /**
     * Returns whether an <code>IEnumAttribute</code> with the given name exists in this
     * <code>IEnumType</code>.
     * <p>
     * The check does <strong>not</strong> include the copies from the super type hierarchy.
     * 
     * @see #containsEnumAttributeIncludeSupertypeCopies(String)
     * 
     * @param attributeName The name of the <code>IEnumAttribute</code> to check for existence in
     *            this <code>IEnumType</code>.
     */
    boolean containsEnumAttribute(String attributeName);

    /**
     * Returns whether an <code>IEnumAttribute</code> with the given name exists in this
     * <code>IEnumType</code>.
     * <p>
     * The check <strong>does</strong> include the copies from the super type hierarchy.
     * 
     * @see #containsEnumAttribute(String)
     * 
     * @param attributeName The name of the <code>IEnumAttribute</code> to check for existence in
     *            this <code>IEnumType</code>.
     */
    boolean containsEnumAttributeIncludeSupertypeCopies(String attributeName);

    /**
     * Returns the first <code>IEnumLiteralNameAttribute</code> of this <code>IEnumType</code> or
     * <code>null</code> if none exists.
     */
    IEnumLiteralNameAttribute getEnumLiteralNameAttribute();

    /**
     * Returns whether this <code>IEnumType</code> currently has at least one
     * <code>IEnumLiteralNameAttribute</code> (<code>true</code>) or not (<code>false</code>).
     */
    boolean hasEnumLiteralNameAttribute();

    /**
     * Returns <code>true</code> if this <code>IEnumType</code> currently has an
     * <code>IEnumLiteralNameAttribute</code>.
     */
    boolean containsEnumLiteralNameAttribute();

    @Override
    default IOverridableElement findOverriddenElement(IIpsProject ipsProject) {
        return findSuperEnumType(ipsProject);
    }
}
