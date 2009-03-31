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
import org.faktorips.datatype.EnumDatatype;

/**
 * An enum type represents the structure of an enum in the Faktor-IPS model.
 * <p>
 * It contains several enum attributes where each enum attribute represents a property of the enum.
 * <p>
 * For example there may be an enum type <em>Gender</em> with the enum attributes <em>id</em> and
 * <em>name</em>.
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
 * At least one enum attribute needs to be marked as <em>identifier</em> which implies that each
 * enum attribute value of this enum attribute needs to be unique.
 * 
 * @see IEnumContent
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumType extends IEnumValueContainer, EnumDatatype {

    /** The xml tag for this ips object. */
    public final static String XML_TAG = "EnumType"; //$NON-NLS-1$

    /** Name of the <code>superEnumType</code> property. */
    public final static String PROPERTY_SUPERTYPE = "superEnumType"; //$NON-NLS-1$

    /** Name of the <code>abstract</code> property. */
    public final static String PROPERTY_ABSTRACT = "abstract"; //$NON-NLS-1$

    /** Name of the <code>valuesArePartOfModel</code> property. */
    public final static String PROPERTY_VALUES_ARE_PART_OF_MODEL = "valuesArePartOfModel"; //$NON-NLS-1$

    /** Name of the <code>enumContentPackageFragment</code> property. */
    public final static String PROPERTY_ENUM_CONTENT_PACKAGE_FRAGMENT = "enumContentPackageFragment"; //$NON-NLS-1$

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

    /** Validation message code to indicate that this enum type has no identifier attribute. */
    public final static String MSGCODE_ENUM_TYPE_NO_IDENTIFIER_ATTRIBUTE = MSGCODE_PREFIX
            + "EnumTypeNoIdentifierAttribute"; //$NON-NLS-1$

    /**
     * Returns the package fragment a referecning enum content must be stored in.
     */
    public String getEnumContentPackageFragment();

    /**
     * Sets the package fragment a referencing enum content must be stored in.
     * 
     * @param packageFragmentQualifiedName The qualified name of the package fragment a referencing
     *            enum content must be stored in.
     * 
     * @throws NullPointerException If <code>packageFragmentQualifiedName</code> is
     *             <code>null</code>.
     */
    public void setEnumContentPackageFragment(String packageFragmentQualifiedName);

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
     * Searches and returns the super enum type of this enum type if any is specified.
     * <p>
     * Returns <code>null</code> if no super enum type is specified.
     * 
     * @throws CoreException If an error occurs while searching the ips project for the super enum
     *             type.
     */
    public IEnumType findSuperEnumType() throws CoreException;

    /**
     * Returns <code>true</code> if this enum type is abstract in terms of the object oriented
     * abstract concept, <code>false</code> if not.
     */
    public boolean isAbstract();

    /**
     * Sets the abstract property for this enum type.
     */
    public void setAbstract(boolean isAbstract);

    /**
     * Returns <code>true</code> if the values for this enum type are defined in the enum type
     * itself.
     */
    public boolean getValuesArePartOfModel();

    /**
     * Allows to set the property whether the values for this enum type will be defined in the enum
     * type itself.
     * 
     * @param valuesArePartOfModel Flag indicating whether the values for this enum type will be
     *            defined in the enum type itself.
     */
    public void setValuesArePartOfModel(boolean valuesArePartOfModel);

    /**
     * Returns a list containing all enum attributes that belong to this enum type.
     * <p>
     * Attributes that are inherited are <strong>not</strong> included.
     */
    public List<IEnumAttribute> getEnumAttributes();

    /**
     * Returns a list containing all enum attributes that belong to this enum type
     * <strong>plus</strong> all enum attributes that are inherited from the supertype hierarchy.
     */
    public List<IEnumAttribute> findAllEnumAttributes();

    /**
     * Returns the index of the given enum attribute in the containing list.
     * 
     * @param enumAttribute The enum attribute to obtain its index for.
     * 
     * @throws NoSuchElementException If there is no such enum attribute in this enum type.
     * @throws NullPointerException If <code>enumAttribute</code> is <code>null</code>.
     */
    public int getIndexOfEnumAttribute(IEnumAttribute enumAttribute);

    /**
     * Returns the enum attribute with the given name or <code>null</code> if there is no enum
     * attribute with the given name in this enum type.
     * <p>
     * Inherited enum attributes are <strong>not</strong> included in the search.
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
     * Inherited enum attributes <strong>are</strong> included in the search.
     * 
     * @param name The name of the enum attribute to obtain.
     * 
     * @throws NullPointerException If <code>name</code> is <code>null</code>.
     */
    public IEnumAttribute findEnumAttribute(String name);

    /**
     * <p>
     * Creates a new enum attribute and returns a reference to it.
     * </p>
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
     * @throws CoreException If an error occurs while delete the enum attribute.
     * @throws IllegalArgumentException If the given enum attribute is not part of this enum type.
     * @throws NullPointerException If <code>enumAttribute</code> is <code>null</code>.
     */
    public void deleteEnumAttributeWithValues(IEnumAttribute enumAttribute) throws CoreException;

    /**
     * Returns whether this enum type has a super enum type.
     */
    public boolean hasSuperEnumType();

    /**
     * Searches all enum types in the supertype hierarchy this enum type is a subtype of and returns
     * them in a list.
     * <p>
     * Never returns <code>null</code>.
     * 
     * @throws CoreException If an error occurs while searching for the enum types in the supertype
     *             hierarchy.
     */
    public List<IEnumType> findAllSuperEnumTypes() throws CoreException;

}
