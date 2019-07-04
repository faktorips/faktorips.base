/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.enums;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.ipsobject.IVersionControlledElement;
import org.faktorips.devtools.core.model.IIpsMetaClass;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * An <tt>IEnumType</tt> represents the structure of an enumeration in the Faktor-IPS model.
 * <p>
 * It contains several <tt>IEnumAttribute</tt>s where each <tt>IEnumAttribute</tt> represents a
 * property of the enumeration.
 * <p>
 * For example there may be an <tt>IEnumType</tt> <tt>Gender</tt> with the <tt>IEnumAttribute</tt>s
 * <tt>id</tt> and <tt>name</tt>.
 * <p>
 * Instances of an enumeration are represented by <tt>IEnumValue</tt>s. In the above example there
 * would be two <tt>IEnumValue</tt>s:
 * <ul>
 * <li>id: m, name: male</li>
 * <li>id: w, name: female</li>
 * </ul>
 * <p>
 * <tt>IEnumValue</tt>s can be defined directly in the <tt>IEnumType</tt> itself or separate from it
 * as product content (<tt>IEnumContent</tt>).
 * <p>
 * There must exist exactly one <tt>IEnumLiteralNameAttribute</tt> that will be used to identify the
 * respective <tt>IEnumValue</tt>s in the generated source code if the values are defined directly
 * in the <tt>IEnumType</tt>.
 * 
 * @see IEnumContent
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumType extends IEnumValueContainer, IIpsMetaClass, IVersionControlledElement {

    /** The XML tag for this IPS object. */
    public static final String XML_TAG = "EnumType"; //$NON-NLS-1$

    /** Name of the <tt>superEnumType</tt> property. */
    public static final String PROPERTY_SUPERTYPE = "superEnumType"; //$NON-NLS-1$

    /** Name of the <tt>abstract</tt> property. */
    public static final String PROPERTY_ABSTRACT = "abstract"; //$NON-NLS-1$

    /**
     * Name of the <tt>containingValues</tt> property.
     * 
     * @deprecated replaced by {@link #PROPERTY_EXTENSIBLE}
     */
    @Deprecated
    public static final String PROPERTY_CONTAINING_VALUES = "containingValues"; //$NON-NLS-1$

    /** Name of the <tt>extensible</tt> property. */
    public static final String PROPERTY_EXTENSIBLE = "extensible"; //$NON-NLS-1$

    /** Name of the <tt>identifierBoundary</tt> property. */
    public static final String PROPERTY_IDENTIFIER_BOUNDARY = "identifierBoundary"; //$NON-NLS-1$

    /** Name of the <tt>enumContentPackageFragment</tt> property. */
    public static final String PROPERTY_ENUM_CONTENT_NAME = "enumContentName"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    public static final String MSGCODE_PREFIX = "ENUMTYPE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the super type of this <tt>IEnumType</tt> does not
     * exist.
     */
    public static final String MSGCODE_ENUM_TYPE_SUPERTYPE_DOES_NOT_EXIST = MSGCODE_PREFIX
            + "EnumTypeSupertypeDoesNotExist"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the super type of this <tt>IEnumType</tt> is not
     * abstract.
     */
    public static final String MSGCODE_ENUM_TYPE_SUPERTYPE_IS_NOT_ABSTRACT = MSGCODE_PREFIX
            + "EnumTypeSupertypeIsNotAbstract"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that not all attributes defined in the the super type
     * hierarchy have been inherited.
     */
    public static final String MSGCODE_ENUM_TYPE_NOT_INHERITED_ATTRIBUTES_IN_SUPERTYPE_HIERARCHY = MSGCODE_PREFIX
            + "EnumTypeNotInheritedAttributesInSupertypeHierarchy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this <tt>IEnumType</tt> has no
     * <tt>IEnumLiteralNameAttribute</tt>.
     */
    public static final String MSGCODE_ENUM_TYPE_NO_LITERAL_NAME_ATTRIBUTE = MSGCODE_PREFIX
            + "EnumTypeNoLiteralNameAttribute"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this <tt>IEnumType</tt> has multiple
     * <tt>IEnumLiteralNameAttribute</tt>s.
     */
    public static final String MSGCODE_ENUM_TYPE_MULTIPLE_LITERAL_NAME_ATTRIBUTES = MSGCODE_PREFIX
            + "EnumTypeMultipleLiteralNameAttributes"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there exists a cycle in the hierarchy of this
     * enumeration type.
     */
    public static final String MSGCODE_CYCLE_IN_TYPE_HIERARCHY = MSGCODE_PREFIX + "CycleInTypeHierarchy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there exists an inconsistency in the hierarchy of
     * this enumeration type. The inconsistency can result from a type in the super type hierarchy
     * that is missing its super type or that the super type is not abstract which is an additional
     * constraint for enumeration types.
     */
    public static final String MSGCODE_INCONSISTENT_TYPE_HIERARCHY = MSGCODE_PREFIX + "InconsistentTypeHierachy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this <tt>IEnumType</tt> does not contain any
     * <tt>IEnumAttribute</tt> being marked to be used as (default) identifier while not being
     * abstract.
     */
    public static final String MSGCODE_ENUM_TYPE_NO_USED_AS_ID_IN_FAKTOR_IPS_UI_ATTRIBUTE = MSGCODE_PREFIX
            + "EnumTypeNoUsedAsIdInFaktorIpsUiAttribute"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this <tt>IEnumType</tt> does not contain any
     * <tt>IEnumAttribute</tt> being marked to be used as name in the Faktor-IPS UI while not being
     * abstract.
     */
    public static final String MSGCODE_ENUM_TYPE_NO_USED_AS_NAME_IN_FAKTOR_IPS_UI_ATTRIBUTE = MSGCODE_PREFIX
            + "EnumTypeNoUsedAsNameInFaktorIpsUiAttribute"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name specification of the <tt>IEnumContent</tt>
     * is empty while this <tt>IEnumType</tt> delegates it's enumeration values.
     */
    public static final String MSGCODE_ENUM_TYPE_ENUM_CONTENT_NAME_EMPTY = MSGCODE_PREFIX
            + "EnumTypeEnumContentNameEmpty"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the <tt>IEnumType</tt> stores <tt>IEnumValue</tt>s
     * even tough it is abstract.
     */
    public static final String MSGCODE_ENUM_TYPE_ENUM_VALUES_OBSOLETE = MSGCODE_PREFIX + "EnumTypeEnumValuesObsolete"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the specified <tt>IEnumContent</tt> is already used
     * by another <tt>IEnumType</tt>.
     */
    public static final String MSGCODE_ENUM_TYPE_ENUM_CONTENT_ALREADY_USED = MSGCODE_PREFIX
            + "EnumTypeEnumContentAlreadyUsed"; //$NON-NLS-1$

    /** Returns the qualified name a referencing <tt>IEnumContent</tt> needs to have. */
    public String getEnumContentName();

    /**
     * @return The IEnumContent referencing this EnumType.
     */
    public IEnumContent findEnumContent(IIpsProject ipsProject) throws CoreException;

    /**
     * Sets the qualified name a referencing <tt>IEnumContent</tt> must have.
     * 
     * @param name The qualified name a referencing <tt>IEnumContent</tt> must be have.
     * 
     * @throws NullPointerException If <tt>name</tt> is <tt>null</tt>.
     */
    public void setEnumContentName(String name);

    /**
     * Returns the qualified name of the super enumeration type of this <tt>IEnumType</tt>.
     * <p>
     * An empty <tt>String</tt> will be returned if this <tt>IEnumType</tt> does not have a super
     * enumeration type.
     */
    public String getSuperEnumType();

    /**
     * Sets the super enumeration type for this <tt>IEnumType</tt>.
     * 
     * @param superEnumTypeQualifiedName The qualified name of the super enumeration type or an
     *            empty <tt>String</tt> if there shall be no super enumeration type.
     * 
     * @throws NullPointerException If <tt>superEnumTypeQualifiedName</tt> is <tt>null</tt>.
     */
    public void setSuperEnumType(String superEnumTypeQualifiedName);

    /**
     * Returns <tt>true</tt> if this <tt>IEnumType</tt> is a sub enumeration type of the given super
     * enumeration type candidate, <tt>false</tt> otherwise. Returns also <tt>false</tt> if the
     * super enumeration type candidate is <tt>null</tt>.
     * 
     * @param superEnumTypeCandidate The type which is possibly a super enumeration type of this
     *            <tt>IEnumType</tt>.
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     */
    public boolean isSubEnumTypeOf(IEnumType superEnumTypeCandidate, IIpsProject ipsProject);

    /**
     * Returns <tt>true</tt> if this <tt>IEnumType</tt> is a sub enumeration type of the given
     * candidate, or if the candidate is the same. Returns <tt>false</tt> otherwise. Returns also
     * <tt>false</tt> if candidate is <tt>null</tt>.
     * 
     * @param superEnumTypeCandidate The <tt>IEnumType</tt> which is the possibly a super
     *            enumeration type of this <tt>IEnumType</tt>.
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     */
    public boolean isSubEnumTypeOrSelf(IEnumType superEnumTypeCandidate, IIpsProject ipsProject);

    /**
     * Searches and returns the super enumeration type of this <tt>IEnumType</tt> if any is
     * specified.
     * <p>
     * Returns <tt>null</tt> if no super enumeration type is specified.
     * 
     * @param ipsProject The IPS project which IPS object path is used for the search of the super
     *            enumeration type. This is not necessarily the project this <tt>IEnumType</tt> is
     *            part of.
     * 
     * @throws NullPointerException If <tt>ipsProject</tt> is <tt>null</tt>.
     */
    public IEnumType findSuperEnumType(IIpsProject ipsProject);

    /**
     * Searches and returns all enumeration types that subclass this enumeration type.
     * 
     * @throws CoreException If an error occurs while searching for sub classing enumeration types.
     */
    public Set<IEnumType> searchSubclassingEnumTypes() throws CoreException;

    /**
     * Sets the abstract property for this <tt>IEnumType</tt>.
     * 
     * @param isAbstract Flag indicating whether this <tt>IEnumType</tt> shall be abstract (
     *            <tt>true</tt>) or not (<tt>false</tt>).
     */
    public void setAbstract(boolean isAbstract);

    /**
     * Returns <tt>true</tt> if the values for this <tt>IEnumType</tt> are defined in the
     * <tt>IEnumType</tt> itself.
     * 
     * @deprecated This method is deprecated use {@link #isExtensible()} instead.
     */
    @Deprecated
    public boolean isContainingValues();

    /**
     * Sets whether the values for this <tt>IEnumType</tt> will be defined in the <tt>IEnumType</tt>
     * itself.
     * 
     * @param containingValues Flag indicating whether the values for this <tt>IEnumType</tt> will
     *            be defined in the <tt>IEnumType</tt> itself.
     * @deprecated This method is deprecated use {@link #setExtensible(boolean)} instead.
     */
    @Deprecated
    public void setContainingValues(boolean containingValues);

    /**
     * Returns <code>true</code> if this {@link IEnumType} does contains at least one value.
     */
    public boolean containsValues();

    /**
     * Returns <tt>true</tt> if this <tt>IEnumType</tt> is extensible.
     * <p>
     * An <tt>IEnumType</tt> is extensible if its values can be defined in the <tt>IEnumType</tt>
     * itself AND in a separated content.
     */
    public boolean isExtensible();

    /**
     * Sets whether this <tt>IEnumType</tt> will be extensible.
     * 
     * @param extensible Flag indicating the extensibility of this <tt>IEnumType</tt>.
     */
    public void setExtensible(boolean extensible);

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
    public String getIdentifierBoundary();

    /**
     * Sets the identifier attribute boundary for this {@link IEnumType}.
     * <p>
     * If the boundary is set to the empty string or <code>null</code> then no restriction will be
     * made for the values in the {@link IEnumType} and its corresponding {@link IEnumContent}.
     * 
     * @see #getIdentifierBoundary()
     */
    public void setIdentifierBoundary(String identifierBoundary);

    /**
     * Returns a list containing all <tt>IEnumAttribute</tt>s that belong to this <tt>IEnumType</tt>
     * .
     * <p>
     * <tt>IEnumAttribute</tt>s that are inherited from the super type hierarchy are
     * <strong>not</strong> included.
     * 
     * @see #getEnumAttributesIncludeSupertypeCopies(boolean)
     * @see #findAllEnumAttributesIncludeSupertypeOriginals(boolean, IIpsProject)
     * 
     * @param includeLiteralName When set to <tt>true</tt> the <tt>IEnumLiteralNameAttribute</tt>s
     *            will be contained in the returned list.
     */
    public List<IEnumAttribute> getEnumAttributes(boolean includeLiteralName);

    /**
     * Returns a list containing all <tt>IEnumAttribute</tt>s that belong to this <tt>IEnumType</tt>
     * <strong>plus</strong> all <tt>IEnumAttribute</tt>s that have been inherited from the super
     * type hierarchy (these are not the original <tt>IEnumAttribute</tt>s defined in the respective
     * super types but copies created based upon the originals).
     * <p>
     * If the original <tt>IEnumAttribute</tt>s defined in the respective super types are needed use
     * <tt>findAllEnumAttributesIncludeSupertypeOriginals(boolean, IIpsProject)</tt>.
     * 
     * @see #getEnumAttributes(boolean)
     * @see #findAllEnumAttributesIncludeSupertypeOriginals(boolean, IIpsProject)
     * 
     * @param includeLiteralName When set to <tt>true</tt> the <tt>IEnumLiteralNameAttribute</tt>s
     *            will be contained in the returned list.
     */
    public List<IEnumAttribute> getEnumAttributesIncludeSupertypeCopies(boolean includeLiteralName);

    /**
     * Returns a list containing all <tt>IEnumAttribute</tt>s that belong to this <tt>IEnumType</tt>
     * <strong>plus</strong> all <tt>IEnumAttribute</tt>s that belong to super types of this
     * <tt>IEnumType</tt>.
     * <p>
     * Copies created due to inheritance are <strong>not</strong> included.
     * 
     * @see #getEnumAttributes(boolean)
     * @see #getEnumAttributesIncludeSupertypeCopies(boolean)
     * 
     * @param includeLiteralName If this flag is <tt>true</tt> all
     *            <tt>IEnumLiteralNameAttribute</tt>s will be contained in the returned list.
     * @param ipsProject The IPS project which IPS object path is used for the search of the super
     *            enumeration types. This is not necessarily the project this <tt>IEnumType</tt> is
     *            part of.
     * 
     * @throws CoreException If an error occurs while searching the given IPS project for the super
     *             enumeration types.
     * @throws NullPointerException If <tt>ipsProject</tt> is <tt>null</tt>.
     * @deprecated use {@link #findAllEnumAttributes(boolean, IIpsProject)} instead, as it returns
     *             the inherited attributes in correct order.
     */
    @Deprecated
    public List<IEnumAttribute> findAllEnumAttributesIncludeSupertypeOriginals(boolean includeLiteralName,
            IIpsProject ipsProject) throws CoreException;

    /**
     * Returns a list containing all <tt>IEnumAttribute</tt>s that belong to this <tt>IEnumType</tt>
     * <strong>plus</strong> all <tt>IEnumAttribute</tt>s that belong to super types of this
     * <tt>IEnumType</tt>.
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
     * @param includeLiteralName If this flag is <tt>true</tt> all
     *            <tt>IEnumLiteralNameAttribute</tt>s will be contained in the returned list.
     * @param ipsProject The IPS project which IPS object path is used for the search of the super
     *            enumeration types. This is not necessarily the project this <tt>IEnumType</tt> is
     *            part of.
     * 
     * @throws NullPointerException If <tt>ipsProject</tt> is <tt>null</tt>.
     */
    public List<IEnumAttribute> findAllEnumAttributes(boolean includeLiteralName, IIpsProject ipsProject);

    /**
     * Looks up the enumeration attribute for which the <tt>identifier</tt> property is
     * <tt>true</tt> and returns it. If none is found <tt>null</tt> will be returned.
     * 
     * @param ipsProject The IPS project used for look up in the super type hierarchy if necessary.
     */
    public IEnumAttribute findIdentiferAttribute(IIpsProject ipsProject);

    /**
     * Looks up the enumeration attribute for which the <tt>isUsedAsNameInFaktorIpsUi</tt> is
     * <tt>true</tt>. Returns <tt>null</tt> if none is found.
     * 
     * @param ipsProject The IPS project used for look up in the super type hierarchy if necessary.
     */
    public IEnumAttribute findUsedAsNameInFaktorIpsUiAttribute(IIpsProject ipsProject);

    /**
     * Returns the index of the given <tt>IEnumAttribute</tt> or -1 if the given
     * <tt>IEnumAttribute</tt> does not exist in this <tt>IEnumType</tt>.
     * <p>
     * Be careful: If the given <tt>IEnumAttribute</tt> is an original from the super type
     * hierarchy, for which this <tt>IEnumType</tt> only stores a copy, the element won't be found!
     * 
     * @param enumAttribute The <tt>IEnumAttribute</tt> to obtain its index for.
     * 
     * @throws NullPointerException If <tt>enumAttribute</tt> is <tt>null</tt>.
     * @deprecated Since 3.11 use {@link #getIndexOfEnumAttribute(IEnumAttribute,boolean)} instead
     *             because in the case of enum contents there we need to ignore the literal name
     *             column if we want to get the column index
     */
    @Deprecated
    public int getIndexOfEnumAttribute(IEnumAttribute enumAttribute);

    /**
     * Returns the index of the given <tt>IEnumAttribute</tt> or -1 if the given
     * <tt>IEnumAttribute</tt> does not exist in this <tt>IEnumType</tt>.
     * <p>
     * Be careful: If the given <tt>IEnumAttribute</tt> is an original from the super type
     * hierarchy, for which this <tt>IEnumType</tt> only stores a copy, the element won't be found!
     * 
     * @param enumAttribute The <tt>IEnumAttribute</tt> to obtain its index for.
     * @param considerLiteralName <code>true</code> if you want to consider the literal name
     *            columns, <code>false</code> to ignore it.
     * 
     * @throws NullPointerException If <tt>enumAttribute</tt> is <tt>null</tt>.
     */
    public int getIndexOfEnumAttribute(IEnumAttribute enumAttribute, boolean considerLiteralName);

    /**
     * Returns the index of the first <tt>IEnumLiteralNameAttribute</tt> or -1 if no
     * <tt>IEnumLiteralNameAttribute</tt> exists in this <tt>IEnumType</tt>.
     */
    public int getIndexOfEnumLiteralNameAttribute();

    /**
     * Returns the <tt>IEnumAttribute</tt> with the given name or <tt>null</tt> if there is no
     * <tt>IEnumAttribute</tt> with the given name in this <tt>IEnumType</tt>.
     * <p>
     * Inherited <tt>IEnumAttribute</tt>s are <strong>not</strong> included in the search.
     * 
     * @see #getEnumAttributeIncludeSupertypeCopies(String)
     * @see #findEnumAttributeIncludeSupertypeOriginals(IIpsProject, String)
     * 
     * @param name The name of the <tt>IEnumAttribute</tt> to obtain.
     * 
     * @throws NullPointerException If <tt>name</tt> is <tt>null</tt>.
     */
    public IEnumAttribute getEnumAttribute(String name);

    /**
     * Returns the <tt>IEnumAttribute</tt> with the given name or <tt>null</tt> if there is no
     * <tt>IEnumAttribute</tt> with the given name in this <tt>IEnumType</tt>.
     * <p>
     * Inherited <tt>IEnumAttribute</tt>s <strong>are</strong> included in the search. Note that in
     * this context an inherited <tt>IEnumAttribute</tt> is just a copy referring to the original
     * <tt>IEnumAttribute</tt> defined in the respective super enumeration type.
     * 
     * @see #getEnumAttributes(boolean)
     * @see #findAllEnumAttributesIncludeSupertypeOriginals(boolean, IIpsProject)
     * 
     * @param name The name of the <tt>IEnumAttribute</tt> to obtain.
     * 
     * @throws NullPointerException If <tt>name</tt> is <tt>null</tt>.
     */
    public IEnumAttribute getEnumAttributeIncludeSupertypeCopies(String name);

    /**
     * Returns the <tt>IEnumAttribute</tt> with the given name or <tt>null</tt> if there is no
     * <tt>IEnumAttribute</tt> with the given name in this <tt>IEnumType</tt> or in the super type
     * hierarchy.
     * <p>
     * Note that <tt>IEnumAttribute</tt>s <strong>defined in super enumeration types are
     * included</strong> in the search and <strong>copies</strong> created due to inheritance
     * <strong>are ignored</strong>.
     * 
     * @param ipsProject The IPS project which IPS object path is used for the search of the super
     *            enumeration types. This is not necessarily the project this <tt>IEnumType</tt> is
     *            part of.
     * @param name The name of the <tt>IEnumAttribute</tt> to obtain.
     * 
     * @throws NullPointerException If <tt>ipsProject</tt> or <tt>name</tt> is <tt>null</tt>.
     */
    public IEnumAttribute findEnumAttributeIncludeSupertypeOriginals(IIpsProject ipsProject, String name);

    /**
     * Creates a new <tt>IEnumAttribute</tt> and returns a reference to it.
     * <p>
     * Note that for all <tt>IEnumValue</tt>s <strong>that are defined directly in this
     * <tt>IEnumType</tt></strong> new <tt>IEnumAttributeValue</tt>s will be created (these will
     * also be moved to their right positions).
     * <p>
     * Fires a <tt>WHOLE_CONTENT_CHANGED</tt> event.
     * 
     * @throws CoreException If an error occurs while creating the new <tt>IEnumAttribute</tt>.
     */
    public IEnumAttribute newEnumAttribute() throws CoreException;

    /**
     * Creates a new <tt>IEnumLiteralNameAttribute</tt> and returns a reference to it. The attribute
     * will already have a default name and be of data type <tt>String</tt>.
     * <p>
     * Note that for all <tt>IEnumValue</tt>s <strong>that are defined directly in this
     * <tt>IEnumType</tt></strong> new <tt>IEnumAttributeValue</tt>s will be created.
     * <p>
     * Fires a <tt>WHOLE_CONTENT_CHANGED</tt> event.
     * 
     * @throws CoreException If an error occurs while creating the new
     *             <tt>IEnumLiteralNameAttribute</tt>.
     */
    public IEnumLiteralNameAttribute newEnumLiteralNameAttribute() throws CoreException;

    /**
     * Returns how many <tt>IEnumAttribute</tt>s are currently part of this <tt>IEnumType</tt>.
     * <p>
     * This operation does <strong>not</strong> inherited <tt>IEnumAttribute</tt>s.
     * 
     * @see #getEnumAttributesCountIncludeSupertypeCopies(boolean)
     * 
     * @param includeLiteralName When set to <tt>true</tt> the <tt>IEnumLiteralNameAttribute</tt>s
     *            will be counted, too.
     */
    public int getEnumAttributesCount(boolean includeLiteralName);

    /**
     * Returns how many <tt>IEnumAttribute</tt>s are currently part of this <tt>IEnumType</tt>.
     * <p>
     * this operation <strong>does</strong> count inherited <tt>IEnumAttribute</tt>s.
     * 
     * @see #getEnumAttributesCount(boolean)
     * 
     * @param includeLiteralName When set to <tt>true</tt> the <tt>IEnumLiteralNameAttribute</tt>s
     *            will be counted, too.
     */
    public int getEnumAttributesCountIncludeSupertypeCopies(boolean includeLiteralName);

    /**
     * Moves the given <tt>IEnumAttribute</tt> one position up or down and returns its new index.
     * <p>
     * If the given <tt>IEnumAttribute</tt> is already the first / last <tt>IEnumAttribute</tt> then
     * nothing will be done.
     * <p>
     * Note that all referencing <tt>IEnumAttributeValues</tt> <strong>that are defined in this
     * <tt>IEnumType</tt></strong> will also be moved one position up / down.
     * <p>
     * Fires a <tt>WHOLE_CONTENT_CHANGED</tt> event if moving was performed.
     * 
     * @param enumAttribute The <tt>IEnumAttribute</tt> to move.
     * @param up Flag indicating whether to move up (<tt>true</tt>) or down (<tt>false</tt>).
     * 
     * @throws CoreException If an error occurs while moving the <tt>IEnumAttribute</tt>.
     * @throws NullPointerException If <tt>enumAttribute</tt> is <tt>null</tt>.
     * @throws NoSuchElementException If the given <tt>IEnumAttribute</tt> is not a part of this
     *             <tt>IEnumType</tt>.
     */
    public int moveEnumAttribute(IEnumAttribute enumAttribute, boolean up) throws CoreException;

    /**
     * Deletes the given <tt>IEnumAttribute</tt> and all the <tt>EnumAttributeValue</tt>s
     * <strong>that are defined in the <tt>IEnumType</tt> itself</strong> referring to it.
     * <p>
     * Fires a <code>WHOLE_CONTENT_CHANGED</code> event.
     * <p>
     * Nothing will happen if <tt>null</tt> or an <tt>IEnumAttribute</tt> that is not part of this
     * <tt>IEnumType</tt> is given.
     * <p>
     * Will return <tt>true</tt> if the <tt>IEnumAttribute</tt> was deleted from this
     * <tt>IEnumType</tt>, <tt>false</tt> if not (following the behavior of the Java collections
     * here).
     * 
     * @param enumAttribute The <tt>IEnumAttribute</tt> to delete.
     * 
     * @throws CoreException If an error occurs while searching the given IPS project for the super
     *             enumeration types.
     * 
     * @deprecated Do not use this method. Always remove an enumAttribute by calling
     *             {@link IEnumAttribute#delete()}.
     */
    @Deprecated
    public boolean deleteEnumAttributeWithValues(IEnumAttribute enumAttribute) throws CoreException;

    /** Returns whether this <tt>IEnumType</tt> has a super enumeration type. */
    public boolean hasSuperEnumType();

    /**
     * Returns <code>true</code> if values are ONLY definable in this {@link IEnumType} and not in a
     * content. Returns <code>false</code> if the enum type is either extensible or is abstract and
     * hence doesn't allow values at all.
     * <p>
     * This is the case when this <tt>IEnumType</tt> is not abstract and is not extensible.
     */
    public boolean isInextensibleEnum();

    /**
     * Returns whether this <tt>IEnumType</tt> has a super enumeration type that really exists.
     * 
     * @param ipsProject The <tt>IIpsProject</tt> that provides the object path that is used to
     *            search for the super enumeration type.
     */
    public boolean hasExistingSuperEnumType(IIpsProject ipsProject);

    /** Returns if this <tt>IEnumType</tt> is abstract. */
    public boolean isAbstract();

    /**
     * Searches all <tt>IEnumType</tt>s in the super type hierarchy this <tt>IEnumType</tt> is a
     * subtype of and returns them in a list (the <tt>IEnumType</tt> the operation is called upon is
     * not included in the list).
     * <p>
     * It is possible that a cycle is detected in the super type hierarchy. In this case the
     * returned list will contain all super enumeration types up to the point where the cycle was
     * found.
     * <p>
     * Never returns <tt>null</tt>.
     * 
     * @param ipsProject The IPS project which IPS object path is used for the search of the super
     *            enumeration types. This is not necessarily the project this
     *            <tt>IEnumAttribute</tt> is part of.
     * 
     * @throws NullPointerException If <tt>ipsProject</tt> is <tt>null</tt>.
     */
    public List<IEnumType> findAllSuperEnumTypes(IIpsProject ipsProject);

    /**
     * Returns a list containing all <tt>IEnumAttribute</tt>s from the super type hierarchy that
     * have not yet been inherited by this <tt>IEnumType</tt>.
     * 
     * @param ipsProject The IPS project which IPS object path is used for the search of the super
     *            enumeration types. This is not necessarily the project this
     *            <tt>IEnumAttribute</tt> is part of.
     * 
     * @throws CoreException If an error occurs while searching the super type hierarchy for the not
     *             inherited <tt>IEnumAttribute</tt>s.
     * @throws NullPointerException If <tt>ipsProject</tt> is <tt>null</tt>.
     */
    public List<IEnumAttribute> findInheritEnumAttributeCandidates(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns a list containing all <tt>IEnumAttribute</tt>s that are unique. Attributes inherited
     * from the supertype hierarchy are also scanned (only their copies will be returned however).
     * 
     * @param includeLiteralName If this flag is <tt>true</tt> the
     *            <tt>IEnumLiteralNameAttribute</tt>s of this <tt>IEnumType</tt> will be contained
     *            in the returned list (<em>those of the super enumeration types not</em>).
     * @param ipsProject The IPS project which IPS object path is used for the search.
     * 
     * @throws CoreException If an error occurs while searching the super type hierarchy.
     * @throws NullPointerException If <tt>ipsProject</tt> is <tt>null</tt>.
     */
    public List<IEnumAttribute> findUniqueEnumAttributes(boolean includeLiteralName, IIpsProject ipsProject)
            throws CoreException;

    /**
     * Creates and returns new <tt>IEnumAttribute</tt>s in this <tt>IEnumType</tt> inheriting the
     * given <tt>IEnumAttribute</tt>s from the super type hierarchy.
     * <p>
     * If any of the given super enumeration attributes is already inherited by this
     * <tt>IEnumType</tt> it will be skipped.
     * 
     * @param superEnumAttributes The <tt>IEnumAttribute</tt>s from the super type hierarchy to
     *            inherit by this <tt>IEnumType</tt>.
     * 
     * @throws CoreException If an error occurs while searching the super type hierarchy.
     * @throws IllegalArgumentException If any of the given <tt>IEnumAttribute</tt>s is not part of
     *             the supertype hierarchy of this <tt>IEnumType</tt>.
     */
    public List<IEnumAttribute> inheritEnumAttributes(List<IEnumAttribute> superEnumAttributes) throws CoreException;

    /**
     * Returns whether an <tt>IEnumAttribute</tt> with the given name exists in this
     * <tt>IEnumType</tt>.
     * <p>
     * The check does <strong>not</strong> include the copies from the super type hierarchy.
     * 
     * @see #containsEnumAttributeIncludeSupertypeCopies(String)
     * 
     * @param attributeName The name of the <tt>IEnumAttribute</tt> to check for existence in this
     *            <tt>IEnumType</tt>.
     */
    public boolean containsEnumAttribute(String attributeName);

    /**
     * Returns whether an <tt>IEnumAttribute</tt> with the given name exists in this
     * <tt>IEnumType</tt>.
     * <p>
     * The check <strong>does</strong> include the copies from the super type hierarchy.
     * 
     * @see #containsEnumAttribute(String)
     * 
     * @param attributeName The name of the <tt>IEnumAttribute</tt> to check for existence in this
     *            <tt>IEnumType</tt>.
     */
    public boolean containsEnumAttributeIncludeSupertypeCopies(String attributeName);

    /**
     * Returns the first <tt>IEnumLiteralNameAttribute</tt> of this <tt>IEnumType</tt> or
     * <tt>null</tt> if none exists.
     */
    public IEnumLiteralNameAttribute getEnumLiteralNameAttribute();

    /**
     * Returns whether this <tt>IEnumType</tt> currently has at least one
     * <tt>IEnumLiteralNameAttribute</tt> (<tt>true</tt>) or not (<tt>false</tt>).
     */
    public boolean hasEnumLiteralNameAttribute();

    /**
     * Returns <tt>true</tt> if this <tt>IEnumType</tt> currently has an
     * <tt>IEnumLiteralNameAttribute</tt>.
     */
    public boolean containsEnumLiteralNameAttribute();
}
