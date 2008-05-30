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

package org.faktorips.runtime;

import java.util.Collection;
import java.util.List;

/**
 * 
 * @author Jan Ortmann
 */
public interface IObjectReferenceStore {

    public void putObject(Object id, Object entityObject);
    
    public void addUnresolvedReference(IUnresolvedReference reference);
    
    public Object getObject(Class<?> clazz, Object id);

    /**
     * Returns a list containing all objects registered.
     */
    public Collection<List<Object>> getAllObjects();
    
    public Collection<IUnresolvedReference> getAllUnresolvedReferences();

    public void resolveReferences() throws Exception;
}

