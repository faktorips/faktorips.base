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
 * This interface defines the lazy load support for a model object repository. A model object repository can
 * implementation can either implement this interface direclty or provide a different object (like a DAO) that
 * supports lazy load. A model object that is returned by such a repository might not be fully loaded when returned
 * from the repository. As soon as the client of the model object navigates an association which hasn't been
 * loaded yet, the method {@link #loadAssociation(IModelObject, String)} is called. 
 * 
 * @author Jan Ortmann
 */
public interface IModelObjectRepositoryLazyLoadSupport {

    /**
     * This method loads the object (or the objects) for the given association of the given
     * model object. Once pulled from the database (or another storage medium) the object(s)
     * can be set to the model object with {#link {@link IModelObjectLazyLoadSupport#initernalSetLazilyLoadedObjects(String, Object)}.
     * 
     * @param modelObject The model object that is the owner of the association.  
     * @param association The association for that the objects have to be loaded.
     */
    public void loadAssociation(IModelObjectLazyLoadSupport modelObject, String association);
}
