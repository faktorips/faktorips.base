/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.modeltype;

import java.util.List;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.modeltype.IModelTypeAttribute.AttributeType;

/**
 * 
 * @author Daniel Hohenberger
 */
public interface IModelType extends IModelElement {

    public static final String XML_TAG = "ModelType";

    public static final String PROPERTY_CLASS = "class";

    public static final String PROPERTY_SUPERTYPE = "supertype";

    /**
     * Returns the Java class for this type.
     * 
     * @throws ClassNotFoundException if the class could not be loaded.
     */
    public Class<?> getJavaClass() throws ClassNotFoundException;

    /**
     * Returns the Java interface for this type.
     * 
     * @throws ClassNotFoundException if the class could not be loaded.
     */
    public Class<?> getJavaInterface() throws ClassNotFoundException;

    /**
     * Returns this model type's super type or <code>null</code> if it has none.
     */
    public IModelType getSuperType();

    /**
     * Returns a list containing all attributes declared in this model type. Attributes defined in
     * the type's super types are not returned.
     */
    public List<IModelTypeAttribute> getDeclaredAttributes();

    /**
     * Returns a list containing the type's attributes including those defined in the type's super
     * types.
     */
    public List<IModelTypeAttribute> getAttributes();

    /**
     * Returns the declared attribute at the given <code>index</code>.
     * 
     * @throws IndexOutOfBoundsException if no attribute exists for the given <code>index</code>.
     */
    public IModelTypeAttribute getDeclaredAttribute(int index) throws IndexOutOfBoundsException;

    /**
     * Returns the attribute with the given <code>name</code> declared in this type. Attributes
     * defined in the type's super types are not returned.
     * 
     * @throws IllegalArgumentException if no attribute with the given <code>name</code> exists.
     */
    public IModelTypeAttribute getDeclaredAttribute(String name) throws IllegalArgumentException;

    /**
     * Returns the attribute with the given <code>name</code> declared in this type or one of it's
     * super types.
     * 
     * @throws IllegalArgumentException if no attribute with the given <code>name</code> exists.
     */
    public IModelTypeAttribute getAttribute(String name) throws IllegalArgumentException;

    /**
     * Returns a list containing all associations declared in this model type. Associations defined
     * in the type's super types are not returned.
     */
    public List<IModelTypeAssociation> getDeclaredAssociations();

    /**
     * Returns the type's associations including those defined in it's super types.
     */
    public List<IModelTypeAssociation> getAssociations();

    /**
     * Returns the association at the given <code>index</code>. Associations defined in the type's
     * super types are not returned.
     * 
     * @throws IndexOutOfBoundsException if no association exists for the given <code>index</code>.
     */
    public IModelTypeAssociation getDeclaredAssociation(int index);

    /**
     * Returns the association with the given <code>name</code> declared in this type. Associations
     * defined in the type's super types are not considered.
     * 
     * @throws IllegalArgumentException if no association with the given <code>name</code> exists.
     */
    public IModelTypeAssociation getDeclaredAssociation(String name);

    /**
     * Returns a list of the target(s) of the given model object's association identified by the
     * given association name.
     * 
     * @param source a model object corresponding to this model type
     * @param associationName the name of the association
     * @return a list of the target(s) of the given model object's association identified by the
     *         given association name
     * @throws IllegalArgumentException if the model does not fit this model type, has no
     *             association by the given name or that association is not accessible for any
     *             reason
     */
    public List<IModelObject> getTargetObjects(IModelObject source, String associationName);

    /**
     * Returns the value of the given model object's attribute identified by the given attribute
     * name.
     * 
     * @see IModelTypeAttribute#getValue(IModelObject)
     * @param modelObject a model object corresponding to this model type
     * @param attributeName the name of the attribute
     * @return the value of the given model object's attribute identified by the given attribute
     *         name
     * @throws IllegalArgumentException if the model object does not fit this model type, has no
     *             attribute by the given name or that attribute is not accessible for any reason
     */
    public Object getAttributeValue(IModelObject modelObject, String attributeName);

    /**
     * Sets the given model object's attribute identified by the given name to the given value. This
     * only works for attributes of type {@link AttributeType#CHANGEABLE}.
     * 
     * @see IModelTypeAttribute#setValue(IModelObject, Object)
     * @param modelObject a model object corresponding to this model type
     * @param value an object of the datatype for the {@link IModelTypeAttribute} by the given name
     * @throws IllegalArgumentException if the model object does not fit this model type, has no
     *             changeable attribute by the given name or that attribute is not accessible for
     *             any reason or the value does not fit the attribute's datatype.
     */
    public void setAttributeValue(IModelObject modelObject, String attributeName, Object value);

    /**
     * Returns the association with the given <code>name</code> declared in this type or one of it's
     * super types.
     * 
     * @throws IllegalArgumentException if no association with the given <code>name</code> exists.
     */
    IModelTypeAssociation getAssociation(String name) throws IllegalArgumentException;

}
