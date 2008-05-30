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

import java.lang.reflect.InvocationTargetException;

/**
 * Resolves the unresolved references in the given store.
 * 
 * @author Jan Ortmann
 */
public class DefaultReferenceResolver {

    /**
     * Resolves the unresolved references in the given store. 
     */
    public void resolve(IObjectReferenceStore store) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        for (IUnresolvedReference unresolvedReference : store.getAllUnresolvedReferences()) {
            DefaultUnresolvedReference reference = (DefaultUnresolvedReference)unresolvedReference;
            Object target = store.getObject(reference.getTargetClass(), reference.getTargetId());
            reference.getEstablishMethod().invoke(reference.getSourceObj(), new Object[]{target});
        }
    }
    
}
