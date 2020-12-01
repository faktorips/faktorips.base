/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.modeltype;

import java.util.List;

import org.faktorips.runtime.model.type.Type;

/**
 * A {@link IModelType} represents a type of Faktor-IPS. It provides all meta data for the type as
 * well as for properties like attributes or associations.
 * 
 * @deprecated Use {@link Type} directly. Will be removed in Faktor-IPS 3.20+
 */
@Deprecated
public interface IModelType extends IModelElement {

    /**
     * Returns the Java class for this type.
     */
    public Class<?> getJavaClass();

    /**
     * Returns the published interface for this type. Returns <code>null</code> if published
     * interfaces are not generated.
     */
    public Class<?> getJavaInterface();

    /**
     * Returns this model type's super type or <code>null</code> if it has none.
     */
    // TODO Java 8 Optional
    public IModelType getSuperType();

    /**
     * Returns a list containing all attributes declared in this model type. Attributes defined in
     * the type's super types are not returned.
     */
    public List<? extends IModelTypeAttribute> getDeclaredAttributes();

    /**
     * Returns a list containing the type's attributes including those defined in the type's super
     * types.
     */
    public List<? extends IModelTypeAttribute> getAttributes();

    /**
     * Returns the declared attribute at the given <code>index</code>.
     * 
     * @throws IndexOutOfBoundsException if no attribute exists for the given <code>index</code>.
     */
    public IModelTypeAttribute getDeclaredAttribute(int index);

    /**
     * Returns the attribute with the given <code>name</code> declared in this type. Attributes
     * defined in the type's super types are not returned.
     * 
     * @throws IllegalArgumentException if no attribute with the given <code>name</code> exists.
     */
    public IModelTypeAttribute getDeclaredAttribute(String name);

    /**
     * Returns the attribute with the given <code>name</code> declared in this type or one of it's
     * super types.
     * 
     * @throws IllegalArgumentException if no attribute with the given <code>name</code> exists.
     */
    public IModelTypeAttribute getAttribute(String name);

    /**
     * Returns a list containing all associations declared in this model type. Associations defined
     * in the type's super types are not returned.
     */
    public List<? extends IModelTypeAssociation> getDeclaredAssociations();

    /**
     * Returns the type's associations including those defined in it's super types.
     */
    public List<? extends IModelTypeAssociation> getAssociations();

    /**
     * Returns the association at the given <code>index</code>. Associations defined in the type's
     * super types are not returned.
     * 
     * @throws IndexOutOfBoundsException if no association exists for the given <code>index</code>.
     */
    public IModelTypeAssociation getDeclaredAssociation(int index);

    /**
     * Returns the association with the given <code>name</code> declared in this type. Associations
     * defined in the type's super types are not considered. The name could either be the singular
     * or the plural name.
     * 
     * @throws IllegalArgumentException if no association with the given <code>name</code> exists.
     */
    public IModelTypeAssociation getDeclaredAssociation(String name);

    /**
     * Returns the association with the given <code>name</code> declared in this type or one of it's
     * super types. The name could either be the singular or the plural name.
     * 
     * @throws IllegalArgumentException if no association with the given <code>name</code> exists.
     */
    public IModelTypeAssociation getAssociation(String name);

}
