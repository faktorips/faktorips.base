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
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsMetaClass;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * An enum type represents the structure of an enum in the Faktor-IPS model.
 * <p>
 * It contains several enum attributes where each enum attribute represents a property of the enum.
 * <p>
 * For example there may be an enum type <code>Gender</code> with the enum attributes
 * <code>id</code> and <code>name</code>.
 * <p>
 * Instances of an enum are represented by enum values. In the above example there would be two enum
 * values:
 * <ul>
 * <li>id: m, name: male</li>
 * <li>id: w, name: female</li>
 * </ul>
 * <p>
 * Enum values can be defined directly in the <code>IEnumType</code> itself or separate from it as
 * product content (<code>IEnumContent</code>).
 * <p>
 * At least one enum attribute needs to be marked as <code>useAsLiteralName</code> which implies
 * that the values for this enum attribute will be used to identify the respective enum values in
 * the generated source code.
 * 
 * @see IEnumContent
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumType extends IEnumValueContainer, IIpsMetaClass {

    /** The xml tag for this ips object. */
    public final static String XML_TAG = "EnumType"; //$NON-NLS-1$

    /** Name of the <code>superEnumType</code> property. */
    public final static String PROPERTY_SUPERTYPE = "superEnumType"; //$NON-NLS-1$

    /** Name of the <code>abstract</code> property. */
    public final static String PROPERTY_ABSTRACT = "abstract"; //$NON-NLS-1$

    /** Name of the <code>containingValues</code> property. */
    public final static String PROPERTY_CONTAINING_VALUES = "containingValues"; //$NON-NLS-1$

    /** Name of the <code>enumContentPackageFragment</code> property. */
    public final static String PROPERTY_ENUM_CONTENT_NAME = "enumContentName"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    public final static String MSGCODE_PREFIX = "ENUMTYPE-"; //$NON-NLS-1$

    /** Validation message code to indicate that the supertype of this enum type does not exist. */
    public final static String MSGCODE_ENUM_TYPE_SUPERTYPE_DOES_NOT_EXIST = MSGCODE_PREFIX
            + "EnumTypeSupertypeDoesNotExist"; //$NON-NLS-1$

    /** Validation message code to indicate that the supertype of this enum type is not abstract. */
    public final static String MSGCODE_ENUM_TYPE_SUPERTYPE_IS_NOT_ABSTRACT = MSGCODE_PREFIX
            + "EnumTypeSupertypeIsNotAbstract"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that not all attributes defined in the the supertype
     * hierarchy have been inherited.
     */
    public final static String MSGCODE_ENUM_TYPE_NOT_INHERITED_ATTRIBUTES_IN_SUPERTYPE_HIERARCHY = MSGCODE_PREFIX
            + "EnumTypeNotInheritedAttributesInSupertypeHierarchy"; //$NON-NLS-1$

    /** Validation message code to indicate that this enum type has no literal name attribute. */
    public final static String MSGCODE_ENUM_TYPE_NO_LITERAL_NAME_ATTRIBUTE = MSGCODE_PREFIX
            + "EnumTypeNoLiteralNameAttribute"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there exists a cycle in the hierarchy of this
     * enumeration type.
     */
    public final static String MSGCODE_CYCLE_IN_TYPE_HIERARCHY = MSGCODE_PREFIX + "CycleInTypeHierarchy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there exists an inconsistency in the hierarchy of
     * this enumeration type. The inconsistency can result from a type in the super type hierarchy
     * that is missing its super type or that the super type is not abstract which is an additional
     * constraint for enumeration types.
     */
    public final static String MSGCODE_INCONSISTENT_TYPE_HIERARCHY = MSGCODE_PREFIX + "InconsistentTypeHierachy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this enum type does not contain any enum attribute
     * being marked to be used as ID in the Faktor-IPS UI while not being abstract.
     */
    public final static String MSGCODE_ENUM_TYPE_NO_USED_AS_ID_IN_FAKTOR_IPS_UI_ATTRIBUTE = MSGCODE_PREFIX
            + "EnumTypeNoUsedAsIdInFaktorIpsUiAttribute"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this enum type does not contain any enum attribute
     * being marked to be used as name in the Faktor-IPS UI while not being abstract.
     */
    public final static String MSGCODE_ENUM_TYPE_NO_USED_AS_NAME_IN_FAKTOR_IPS_UI_ATTRIBUTE = MSGCODE_PREFIX
            + "EnumTypeNoUsedAsNameInFaktorIpsUiAttribute"; //$NON-NLS-1$

    /** Validation message code to indicate that the enum content package fragment is empty. */
    public final static String MSGCODE_ENUM_TYPE_ENUM_CONTENT_NAME_EMPTY = MSGCODE_PREFIX
            + "EnumTypeEnumContentNameEmpty"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the enum type stores enum values even tough it does
     * not contain values or is abstract.
     */
    public final static String MSGCODE_ENUM_TYPE_ENUM_VALUES_OBSOLETE = MSGCODE_PREFIX + "EnumTypeEnumValuesObsolete"; //$NON-NLS-1$

    /** Returns the package fragment a referencing enum content must be stored in. */
    public String getEnumContentName();

    /**
     * Sets the package fragment a referencing enum content must be stored in.
     * 
     * @param packageFragmentQualifiedName The qualified name of the package fragment a referencing
     *            enum content must be stored in.
     * 
     * @throws NullPointerException If <code>packageFragmentQualifiedName</code> is
     *             <code>null</code>.
     */
    public void setEnumContentName(String name);

    /**
     * Returns the qualified name of the super enum type of this enum type.
     * <p>
     * An empty string will be returned if this enum type does not have a super enum type.
     */
    public String getSuperEnumType();

    /**
     * Sets the super enum type for this enum type.
     * 
     * @param superEnumTypeQualifiedName The qualified name of the super enum type or an empty
     *            string if there shall be no super enum type.
     * 
     * @throws NullPointerException If <code>superEnumTypeQualifiedName</code> is <code>null</code>.
     */
    public void setSuperEnumType(String superEnumTypeQualifiedName);

    /**
     * Returns <code>true</code> if this enum type is a sub enum type of the given super enum type
     * candidate, returns <code>false</code> otherwise. Returns <code>false</code> if super enum
     * type candidate is <code>null</code>.
     * 
     * @param superEnumTypeCandidate The type which is the possibly a super enum type of this enum
     *            type
     * @param ipsProject The project which ips object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws CoreException if an error occurs while searching the type hierarchy.
     */
    public boolean isSubEnumTypeOf(IEnumType superEnumTypeCandidate, IIpsProject ipsProject) throws CoreException;

    /**
     * Returns <code>true</code> if this enum type is a sub enum type of the given candidate, or if
     * the candidate is the same. Returns <code>false</code> otherwise. Returns <code>false</code>
     * if candidate is <code>null</code>.
     * 
     * @param superEnumTypeCandidate The enum type which is the possibly a super enum type of this
     *            enum type
     * @param ipsProject The project which ips object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws CoreException if an error occurs while searching the type hierarchy.
     */
    public boolean isSubEnumTypeOrSelf(IEnumType superEnumTypeCandidate, IIpsProject ipsProject) throws CoreException;

    /**
     * Searches and returns the super enum type of this enum type if any is specified.
     * <p>
     * Returns <code>null</code> if no super enum type is specified.
     * 
     * @param ipsProject The ips project which ips object path is used for the search of the super
     *            enum type. This is not necessarily the project this enum attribute is part of.
     * 
     * @throws CoreException If an error occurs while searching the given ips project for the super
     *             enum type.
     * @throws NullPointerException If <code>ipsProject</code> is <code>null</code>.
     */
    public IEnumType findSuperEnumType(IIpsProject ipsProject) throws CoreException;

    /**
     * Sets the abstract property for this enum type.
     * 
     * @param isAbstract Flag indicating whether this enum type shall be abstract (<code>true</code>
     *            ) or not (<code>false</code>).
     */
    public void setAbstract(boolean isAbstract);

    /**
     * Returns <code>true</code> if the values for this enum type are defined in the enum type
     * itself.
     */
    public boolean isContainingValues();

    /**
     * Allows to set the property whether the values for this enum type will be defined in the enum
     * type itself.
     * 
     * @param containingValues Flag indicating whether the values for this enum type will be defined
     *            in the enum type itself.
     */
    public void setContainingValues(boolean containingValues);

    /**
     * Returns a list containing all enum attributes that belong to this enum type.
     * <p>
     * Enum attributes that are inherited from the supertype hierarchy are <strong>not</strong>
     * included.
     * 
     * @see #getEnumAttributesIncludeSupertypeCopies()
     * @see #findAllEnumAttributesIncludeSupertypeOriginals()
     */
    public List<IEnumAttribute> getEnumAttributes();

    /**
     * Returns a list containing all enum attributes that belong to this enum type
     * <strong>plus</strong> all enum attributes that have been inherited from the supertype
     * hierarchy (these are not the original enum attributes defined in the respective supertypes
     * but copies created based upon the originals).
     * <p>
     * If the original enum attributes defined in the respective supertypes are needed use
     * <code>findAllEnumAttributesIncludeSupertypeOriginals()</code>.
     * 
     * @see #getEnumAttributes()
     * @see #findAllEnumAttributesIncludeSupertypeOriginals()
     */
    public List<IEnumAttribute> getEnumAttributesIncludeSupertypeCopies();

    /**
     * Returns a list containing all enum attributes that belong to this enum type
     * <strong>plus</strong> all enum attributes that belong to supertypes of this enum type.
     * <p>
     * Copies created due to inheritation are <strong>not</strong> included.
     * 
     * @see #getEnumAttributes()
     * @see #getEnumAttributesIncludeSupertypeCopies()
     * 
     * @param ipsProject The ips project which ips object path is used for the search of the super
     *            enum types. This is not necessarily the project this enum attribute is part of.
     * 
     * @throws CoreException If an error occurs while searching the given ips project for the super
     *             enum types.
     * @throws NullPointerException If <code>ipsProject</code> is <code>null</code>.
     */
    public List<IEnumAttribute> findAllEnumAttributesIncludeSupertypeOriginals(IIpsProject ipsProject)
            throws CoreException;

    /**
     * Returns the enum attribute that has been marked to be used as literal name or
     * <code>null</code> if no such enum attribute exists in this enum type.
     * <p>
     * Enum attributes inherited from the supertype hierarchy <strong>are</strong> included in the
     * search (as copies of their respective originals).
     * 
     * @throws CoreException If an error occurs while searching the supertype hierarchy.
     */
    public IEnumAttribute findLiteralNameAttribute(IIpsProject ipsProject) throws CoreException;

    /**
     * Looks up the enumeration attribute for which the isIdentifier property is <code>true</code>
     * and returns it. If none is found <code>null</code> will be returned. The supertype hierachy
     * is considered if necessary.
     * 
     * @param ipsProject used for look up in the supertype hierarchy if necessary
     * @throws CoreException if an exception occurs during the look up
     */
    public IEnumAttribute findIsIdentiferAttribute(IIpsProject ipsProject) throws CoreException;

    /**
     * Looks up the enumeration attribute for which the isUsedAsNameInFaktorIpsUi is true.
     * <code>null</code> is returned if none is found. The supertype hierachy is considered if
     * necessary.
     * 
     * @param ipsProject The ips project used for look up in the supertype hierarchy if necessary.
     * 
     * @throws CoreException If an exception occurs during the look up.
     */
    public IEnumAttribute findIsUsedAsNameInFaktorIpsUiAttribute(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the index of the given enum attribute in the containing list.
     * <p>
     * Be careful: If the given enum attribute is an original from the supertype hierarchy, for
     * which this enum type only stores a copy, the element won't be found and an exception is
     * thrown.
     * 
     * @param enumAttribute The enum attribute to obtain its index for.
     * 
     * @throws NullPointerException If <code>enumAttribute</code> is <code>null</code>.
     * @throws NoSuchElementException If there is no such enum attribute in this enum type.
     */
    public int getIndexOfEnumAttribute(IEnumAttribute enumAttribute);

    /**
     * Returns the enum attribute with the given name or <code>null</code> if there is no enum
     * attribute with the given name in this enum type.
     * <p>
     * Inherited enum attributes are <strong>not</strong> included in the search.
     * 
     * @see #getEnumAttributeIncludeSupertypeCopies(String)
     * @see #findEnumAttributeIncludeSupertypeOriginals(String)
     * 
     * @param name The name of the enum attribute to obtain.
     * 
     * @throws NullPointerException If <code>name</code> is <code>null</code>.
     */
    public IEnumAttribute getEnumAttribute(String name);

    /**
     * Returns the enum attribute with the given name or <code>null</code> if there is no enum
     * attribute with the given name in this enum type.
     * <p>
     * Inherited enum attributes <strong>are</strong> included in the search. Note that in this
     * context an inherited enum attribute is just a copy refering to the original enum attribute
     * defined in the respective super enum type.
     * 
     * @see #getEnumAttributes()
     * @see #findAllEnumAttributesIncludeSupertypeOriginals()
     * 
     * @param name The name of the enum attribute to obtain.
     * 
     * @throws NullPointerException If <code>name</code> is <code>null</code>.
     */
    public IEnumAttribute getEnumAttributeIncludeSupertypeCopies(String name);

    /**
     * Returns the enum attribute with the given name or <code>null</code> if there is no enum
     * attribute with the given name in this enum type or in the supertype hierarchy.
     * <p>
     * Note that enum attributes <strong>defined in super enum types are included</strong> in the
     * search and <strong>copies</strong> created due to inheritation <strong>are ignored</strong>.
     * 
     * @param ipsProject The ips project which ips object path is used for the search of the super
     *            enum types. This is not necessarily the project this enum attribute is part of.
     * @param name The name of the enum attribute to obtain.
     * 
     * @throws CoreException If an error occurs while searching the given ips project for the super
     *             enum types.
     * @throws NullPointerException If <code>ipsProject</code> or <code>name</code> is
     *             <code>null</code>.
     */
    public IEnumAttribute findEnumAttributeIncludeSupertypeOriginals(IIpsProject ipsProject, String name)
            throws CoreException;

    /**
     * <p>
     * Creates a new enum attribute and returns a reference to it.
     * <p>
     * Note that for all enum values <strong>that are defined directly in this enum type</strong>
     * new <code>EnumAttributeValue</code> objects will be created.
     * <p>
     * Fires a <code>WHOLE_CONTENT_CHANGED</code> event.
     * 
     * @return A reference to the newly created enum attribute.
     * 
     * @throws CoreException If an error occurs while creating the new enum attribute.
     */
    public IEnumAttribute newEnumAttribute() throws CoreException;

    /**
     * Returns how many enum attributes are currently part of this enum type.
     * 
     * @param includeInherited Flag indicating whether to count inherited enum attributes.
     */
    public int getEnumAttributesCount(boolean includeInherited);

    /**
     * Moves the given enum attribute one position up or down in the containing list and returns its
     * new index.
     * <p>
     * If the given enum attribute is already the first / last enum attribute then nothing will be
     * done.
     * <p>
     * Note that all referencing enum attribute values <strong>that are defined in this enum
     * type</strong> will also be moved one position up / down in their containing list.
     * <p>
     * Fires a <code>WHOLE_CONTENT_CHANGED</code> event if moving was performed.
     * 
     * @param enumAttribute The enum attribute to move.
     * @param up Flag indicating whether to move up (<code>true</code>) or down (<code>false</code>
     *            ).
     * 
     * @throws CoreException If an error occurs while moving the enum attribute.
     * @throws NullPointerException If <code>enumAttribute</code> is <code>null</code>.
     * @throws NoSuchElementException If the given enum attribute is not a part of this enum type.
     */
    public int moveEnumAttribute(IEnumAttribute enumAttribute, boolean up) throws CoreException;

    /**
     * Checks whether an enum attribute with the given name exists.
     * 
     * @param name The name of the enum attribute that will be checked for existence.
     * 
     * @throws NullPointerException If <code>name</code> is <code>null</code>.
     */
    public boolean enumAttributeExists(String name);

    /**
     * Deletes the given enum attribute and all the <code>EnumAttributeValue</code> objects
     * <strong>that are defined in the enum type itself</strong> refering to it.
     * <p>
     * Fires a <code>WHOLE_CONTENT_CHANGED</code> event.
     * 
     * @param enumAttribute The enum attribute to delete.
     * 
     * @throws IllegalArgumentException If the given enum attribute is not part of this enum type.
     * @throws NullPointerException If <code>enumAttribute</code> is <code>null</code>.
     */
    public void deleteEnumAttributeWithValues(IEnumAttribute enumAttribute);

    /**
     * Returns whether this enum type has a super enum type.
     */
    public boolean hasSuperEnumType();

    /**
     * Returns if this enumeration type is abstract.
     */
    public boolean isAbstract();

    /**
     * Searches all enum types in the supertype hierarchy this enum type is a subtype of and returns
     * them in a list (the enum type the operation is called upon is not included in the list).
     * <p>
     * It is possible that a cycle is detected in the supertype hierarchy. In this case the returned
     * list will contain all super enum types up to the point where the cycle was found.
     * <p>
     * Never returns <code>null</code>.
     * 
     * @param ipsProject The ips project which ips object path is used for the search of the super
     *            enum types. This is not necessarily the project this enum attribute is part of.
     * 
     * @throws CoreException If an error occurs while searching the given ips project for the super
     *             enum types.
     * @throws NullPointerException If <code>ipsProject</code> is <code>null</code>.
     */
    public List<IEnumType> findAllSuperEnumTypes(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns a list containing all enum attributes from the supertype hierarchy that have not yet
     * been inherited by this enum type.
     * 
     * @param ipsProject The ips project which ips object path is used for the search of the super
     *            enum types. This is not necessarily the project this enum attribute is part of.
     * 
     * @throws CoreException If an error occurs while searching the supertype hierarchy for the not
     *             inherited enum attributes.
     * @throws NullPointerException If <code>ipsProject</code> is <code>null</code>.
     */
    public List<IEnumAttribute> findInheritEnumAttributeCandidates(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns a list containing all enum attributes that are unique. Attributes inherited from the
     * supertype hierarchy are also scanned (only their copies will be returned however).
     * 
     * @param ipsProject The ips project which ips object path is used for the search.
     * 
     * @throws CoreException If an error occurs while searching the supertype hierarchy.
     * @throws NullPointerException If <tt>ipsProject</tt> is <tt>null</tt>.
     */
    public List<IEnumAttribute> findUniqueEnumAttributes(IIpsProject ipsProject) throws CoreException;

    /**
     * Creates and returns new enum attributes in this enum type inheriting the given enum
     * attributes.
     * <p>
     * If any of the given super enum attributes is already inherited by this enum type it will be
     * skipped.
     * 
     * @param superEnumAttributes The enum attributes from the supertype hierarchy to inherit by
     *            this enum type.
     * 
     * @throws CoreException If an error occurs while searching the supertype hierarchy.
     * @throws IllegalArgumentException If any of the given enum attributes is not part of the
     *             supertype hierarchy of this enum type.
     */
    public List<IEnumAttribute> inheritEnumAttributes(List<IEnumAttribute> superEnumAttributes) throws CoreException;

}
