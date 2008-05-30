/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.runtime.modeltype;

import java.util.List;


/**
 * 
 * @author Daniel Hohenberger
 */
public interface IModelType extends IModelElement {
    
    /**
     * @return the class generated for this type.
     */
    public Class<?> getGeneratedClass();
    
    /**
     * @return this model type's super type or <code>null</code> if it has none.
     */
    public IModelType getSuperType();
    
    /**
     * @return an array containing all attributes of this model type object.
     */
    public List<IModelTypeAttribute> getAttributes();
    
    /**
     * @return the attribute at the given <code>index</code>.
     * @throws IndexOutOfBoundsException if no attribute exists for the given <code>index</code>.
     */
    public IModelTypeAttribute getAttribute(int index) throws IndexOutOfBoundsException;
    
    /**
     * @return the attribute with the given <code>name</code>.
     * @throws IllegalArgumentException if no attribute with the given <code>name</code> exists.
     */
    public IModelTypeAttribute getAttribute(String name) throws IllegalArgumentException;

    /**
     * @return an array containing all associations of this model type object.
     */
    public List<IModelTypeAssociation> getAssociations();

    /**
     * @return the association at the given <code>index</code>.
     * @throws IndexOutOfBoundsException if no association exists for the given <code>index</code>.
     */
    public IModelTypeAssociation getAssociation(int index);

    /**
     * @return the association with the given <code>name</code>.
     * @throws IllegalArgumentException if no association with the given <code>name</code> exists.
     */
    public IModelTypeAssociation getAssociation(String name);
}
