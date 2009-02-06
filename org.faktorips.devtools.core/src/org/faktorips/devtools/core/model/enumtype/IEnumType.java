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

/**
 * <p>
 * An enum type represents the structure of an enumeration in the Faktor-IPS model.
 * </p>
 * <p>
 * An enumeration is a table-like structure. An enum type has several enum attributes where each
 * enum attribute can be seen as a column.
 * </p>
 * <p>
 * For example there may be an enum type <em>Gender</em> with the enum attributes <em>id</em> and
 * <em>name</em>.
 * </p>
 * <p>
 * Each row in the table is represented by an enum value. In the above example there would be two
 * enum values:
 * </p>
 * <ul>
 * <li>id: m, name: male</li>
 * <li>id: w, name: female</li>
 * </ul>
 * <p>
 * Enum values can be defined directly in the enum type itself or separate from the enum type by the
 * product side (as enum content).
 * </p>
 * <p>
 * At least one enum attribute needs to be marked as <em>identifier</em> which implies that each
 * enum attribute value of this enum attribute needs to be unique.
 * </p>
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumType extends IEnumValueContainer, Datatype {

    /** The xml tag for this ips object. */
    public final static String XML_TAG = "EnumType"; //$NON-NLS-1$

    /** Name of the supertype property. */
    public final static String PROPERTY_SUPERTYPE = "supertype"; //$NON-NLS-1$

    /** Name of the isAbstract property. */
    public final static String PROPERTY_ABSTRACT = "abstract"; //$NON-NLS-1$

    /** Name of the valuesArePartOfModel property. */
    public final static String PROPERTY_VALUES_ARE_PART_OF_MODEL = "valuesArePartOfModel"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    public final static String MSGCODE_PREFIX = "ENUMTYPE-"; //$NON-NLS-1$

    /** Validation message code to indicate that the supertype of this enum type does not exist. */
    public final static String MSGCODE_ENUM_TYPE_SUPERTYPE_DOES_NOT_EXIST = MSGCODE_PREFIX
            + "EnumTypeSupertypeDoesNotExist"; //$NON-NLS-1$

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
    public boolean valuesArePartOfModel();

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
     * Returns the enum attribute with the given id or <code>null</code> if there is no enum
     * attribute with the given id.
     * 
     * @param id The id of the enum attribute to obtain.
     * 
     * @return A reference to the enum attribute with the given id or <code>null</code> if no such
     *         enum attribute could be found.
     */
    public IEnumAttribute getEnumAttribute(int id);

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
     * Creates a new enum attribute and returns a reference to it.
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
    public int getNumberEnumAttributes();

    /**
     * <p>
     * Moves the given enum attribute one position upwards in the enum attribute collection.
     * </p>
     * <p>
     * If the given enum attribute is already the first enum attribute then absolutely nothing will
     * be done.
     * </p>
     * <p>
     * Note that all referencing enum attribute values will also be moved one position upwards in
     * their collection.
     * </p>
     * 
     * @param enumAttribute The enum attribute to move further up in the order.
     * 
     * @return The new index of the enum attribute.
     * 
     * @throws CoreException If an error occurs while moving the enum attributes.
     * @throws NullPointerException If enumAttribute is <code>null</code>.
     */
    public int moveEnumAttributeUp(IEnumAttribute enumAttribute) throws CoreException;

    /**
     * <p>
     * Moves the given enum attribute one position downwards in the enum attribute collection.
     * </p>
     * <p>
     * If the given enum attribute is already the last enum attribute then absolutely nothing will
     * be done.
     * </p>
     * <p>
     * Note that all referencing enum attribute values will also be moved one position downwards in
     * their collection.
     * </p>
     * 
     * @param enumAttribute The enum attribute to move further down in the order.
     * 
     * @return The new index of the enum attribute.
     * 
     * @throws CoreException If an error occurs while moving the enum attributes.
     * @throws NullPointerException If enumAttribute is <code>null</code>.
     */
    public int moveEnumAttributeDown(IEnumAttribute enumAttribute) throws CoreException;

    /**
     * <p>
     * Searches and returns all enum contents that are built upon this enum type. If none are found
     * an empty list will be returned. This method never returns <code>null</code>.
     * </p>
     * <p>
     * Note that this operation does not differentiate whether the values are part of the model or
     * not.
     * </p>
     * 
     * @return A list containing all enum contents that are built upon this enum type.
     * 
     * @throws CoreException If an error occurs while searching for the enum content objects.
     */
    public List<IEnumContent> findReferencingEnumContents() throws CoreException;

    /**
     * <p>
     * Deletes the given enum attribute and all enum attribute values that refer to it.
     * </p>
     * <p>
     * Note that this operation does not differentiate whether the values are part of the model or
     * not: all referencing enum attribute values will be deleted.
     * </p>
     * 
     * @param enumAttribute The enum attribute to delete.
     * 
     * @throws CoreException If an error occurs while delete the enum attribute.
     * @throws NoSuchElementException If there is no such enum attribute in this enum type.
     * @throws NullPointerException If enumAttribute is <code>null</code>.
     */
    public void deleteEnumAttributeWithValues(IEnumAttribute enumAttribute) throws CoreException;

    /**
     * <p>
     * Deletes the enum attribute identified by the given id and all enum attribute values that
     * refer to it.
     * </p>
     * <p>
     * Note that this operation does not differentiate whether the values are part of the model or
     * not: all referencing enum attribute values will be deleted.
     * </p>
     * 
     * @param id The id that identifies the enum attribute to delete.
     * 
     * @throws CoreException If an error occurs while delete the enum attribute.
     * @throws NoSuchElementException If there is no enum attribute with the given id.
     */
    public void deleteEnumAttributeWithValues(int id) throws CoreException;

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

}
