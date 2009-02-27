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

package org.faktorips.devtools.core.model.enumtype;

import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;

/**
 * An enum type represents the structure of an enumeration in the Faktor-IPS model.
 * <p>
 * An enumeration is a table-like structure. An enum type has several enum attributes where each
 * enum attribute can be seen as a column.
 * <p>
 * For example there may be an enum type <em>Gender</em> with the enum attributes <em>id</em> and
 * <em>name</em>.
 * <p>
 * Each row in the table is represented by an enum value. In the above example there would be two
 * enum values:
 * <ul>
 * <li>id: m, name: male</li>
 * <li>id: w, name: female</li>
 * </ul>
 * <p>
 * Enum values can be defined directly in the enum type itself or separate from the enum type by the
 * product side (as enum content).
 * <p>
 * At least one enum attribute needs to be marked as <em>identifier</em> which implies that each
 * enum attribute value of this enum attribute needs to be unique.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumType extends IEnumValueContainer, Datatype {

    /** The xml tag for this ips object. */
    public final static String XML_TAG = "EnumType"; //$NON-NLS-1$

    /** Name of the supertype property. */
    public final static String PROPERTY_SUPERTYPE = "superEnumType"; //$NON-NLS-1$

    /** Name of the isAbstract property. */
    public final static String PROPERTY_ABSTRACT = "abstract"; //$NON-NLS-1$

    /** Name of the valuesArePartOfModel property. */
    public final static String PROPERTY_VALUES_ARE_PART_OF_MODEL = "valuesArePartOfModel"; //$NON-NLS-1$

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
     * Returns the qualified name of the super enum type of this enum type.
     * 
     * @return A <code>String</code> representing the qualified name of the super enum type. An
     *         empty string is returned if this enum type does not have a super enum type.
     */
    public String getSuperEnumType();

    /**
     * Sets the super enum type for this enum type.
     * 
     * @param superEnumTypeQualifiedName The qualified name of the super enum type or an empty
     *            string if there shall be no super enum type.
     * 
     * @throws NullPointerException If superEnumTypeQualifiedName is <code>null</code>.
     */
    public void setSuperEnumType(String superEnumTypeQualifiedName);

    /**
     * Searches and returns the super enum type of this enum type if any is specified.
     * 
     * @return A reference to the super enum type or <code>null</code> if none is specified.
     * 
     * @throws CoreException If an error occurs while searching the ips project for the super enum
     *             type.
     */
    public IEnumType findSuperEnumType() throws CoreException;

    /**
     * Returns <code>true</code> if this enum type is abstract in terms of the object oriented
     * abstract concept, <code>false</code> if not.
     * 
     * @return Flag indicating whether this enum type is abstract.
     */
    public boolean isAbstract();

    /**
     * Sets the abstract property for this enum type.
     * 
     * @param isAbstract Flag indicating whether this enum type shall be abstract or not.
     */
    public void setAbstract(boolean isAbstract);

    /**
     * Returns true if the values for this enum type are defined in the enum type itself.
     * 
     * @return Flag indicating whether the values for this enum type are defined in the enum type
     *         itself.
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
     * 
     * @return A list containing all enum attributes that belong to this enum type.
     */
    public List<IEnumAttribute> getEnumAttributes();

    /**
     * Returns the index of the given enum attribute in the enum attributes collection.
     * 
     * @param enumAttribute The enum attribute to obtain its index for.
     * 
     * @return The index identifying the given enum attribute in the enum attributes collection.
     * 
     * @throws NoSuchElementException If there is no such enum attribute in this enum type.
     * @throws NullPointerException If enumAttribute is <code>null</code>.
     */
    public int getIndexOfEnumAttribute(IEnumAttribute enumAttribute);

    /**
     * Returns the enum attribute with the given name or <code>null</code> if there is no enum
     * attribute with the given name.
     * 
     * @param name The name of the enum attribute to obtain.
     * 
     * @return The enum attribute identified by the given name or <code>null</code> if no such enum
     *         attribute could be found.
     * 
     * @throws NullPointerException If name is <code>null</code>.
     */
    public IEnumAttribute getEnumAttribute(String name);

    /**
     * <p>
     * Creates a new enum attribute and returns a reference to it.
     * </p>
     * <p>
     * Note that all enum values <strong>that are defined directly in this enum type</strong> will
     * get new <code>EnumAttributeValue</code> objects for the new enum attribute.
     * </p>
     * 
     * @return A reference to the newly created enum attribute.
     * 
     * @throws CoreException If an error occurs while creating the new enum attribute.
     */
    public IEnumAttribute newEnumAttribute() throws CoreException;

    /**
     * Returns how many enum attributes are currently assigned to this enum type.
     * 
     * @return The number of enum attributes currently assigned to this enum type.
     */
    public int getEnumAttributesCount();

    /**
     * Moves the given enum attribute one position down or up in the containing list.
     * <p>
     * If the given enum attribute is already the last / first enum attribute then nothing will be
     * done.
     * <p>
     * Note that all referencing enum attribute values <strong>that are defined in this enum
     * type</strong> will also be moved one position down / up in their containing list.
     * 
     * @param enumAttribute The enum attribute to move.
     * @param up Flag indicating whether to move up (<code>true</code>) or down (<code>false</code>
     *            ).
     * 
     * @return The new index of the enum attribute.
     * 
     * @throws CoreException If an error occurs while moving the enum attribute.
     * @throws NullPointerException If enumAttribute is <code>null</code>.
     * @throws NoSuchElementException If the given enum attribute is not a part of this enum type.
     */
    public int moveEnumAttribute(IEnumAttribute enumAttribute, boolean up) throws CoreException;

    /**
     * Checks whether an enum attribute with the given name exists.
     * 
     * @param name The name of the enum attribute that will be checked for existence.
     * 
     * @return A boolean flag that is <code>true</code> if an enum attribute with the given name
     *         exists and <code>false</code> if not.
     * 
     * @throws NullPointerException If name is <code>null</code>.
     */
    public boolean enumAttributeExists(String name);

    /**
     * Deletes the given enum attribute and all the <code>EnumAttributeValue</code> objects
     * <strong>that are defined in the enum type itself</strong> refering to it.
     * 
     * @param enumAttribute The enum attribute to delete.
     * 
     * @throws CoreException If an error occurs while delete the enum attribute.
     * @throws IllegalArgumentException If the given enum attribute is not part of this enum type.
     * @throws NullPointerException If enumAttribute is <code>null</code>.
     */
    public void deleteEnumAttributeWithValues(IEnumAttribute enumAttribute) throws CoreException;

    /**
     * Returns whether this enum type has a super enum type.
     * 
     * @return A flag indicating whether this enum type has a super enum type (<code>true</code>) or
     *         not (<code>false</code>).
     */
    public boolean hasSuperEnumType();

    /**
     * Searches all enum types in the supertype hierarchy this enum type is a subtype of and returns
     * them in a list.
     * 
     * @return A list containing all enum types from the supertype hierarchy this enum type is a
     *         subtype of. Never returns <code>null</code>.
     * 
     * @throws CoreException If an error occurs while searching for the enum types in the supertype
     *             hierarchy.
     */
    public List<IEnumType> findAllSuperEnumTypes() throws CoreException;

    /**
     * Returns all enum attributes of this enum type that are marked as inherited.
     * 
     * @return A list containing all enum attributes of this enum type being marked as inherited.
     *         Never returns <code>null</code>.
     */
    // TODO pk: wird wahrscheinlich nicht benötigt
    public List<IEnumAttribute> getInheritedAttributes();

}
