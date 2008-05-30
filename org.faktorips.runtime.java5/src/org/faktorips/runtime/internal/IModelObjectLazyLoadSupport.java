/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.runtime.internal;

import org.faktorips.runtime.IModelObject;

/**
 * An interface that marks an {@link IModelObject} as supporting lazy load.
 * Note that this interface is in the internal package as it should be transparent
 * to clients of the model if the objects are loaded lazily or not.
 * 
 * <p><strong>
 * This interface is experimental in this version.
 * The API might change without notice until it is finalized in a future version.
 * </strong>
 * 
 * @see IModelObjectLazyLoadSupport 
 * 
 * @author Jan Ortmann
 */
public interface IModelObjectLazyLoadSupport {

    /**
     * Returns the model object repository's lazy load support.
     */
    public IModelObjectRepositoryLazyLoadSupport getModelObjectRepositoryLazyLoadSupport();
    
    /**
     * This method should only be called by implementations of {@link IModelObjectRepositoryLazyLoadSupport#loadAssociation(IModelObject, String)}. 
     * 
     * @param association The name of the association
     * @param objectOrList The object (for to 1 associations) or the list of objects (for to many associations)
     * that have been loaded for the association.
     * 
     * @see IModelObjectRepositoryLazyLoadSupport#loadAssociation(IModelObject, String)
     */
    public void initernalSetLazilyLoadedObjects(String association, Object objectOrList);
}
