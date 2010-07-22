/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
    public void resolve(IObjectReferenceStore store) throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        for (IUnresolvedReference unresolvedReference : store.getAllUnresolvedReferences()) {
            DefaultUnresolvedReference reference = (DefaultUnresolvedReference)unresolvedReference;
            Object target = store.getObject(reference.getTargetClass(), reference.getTargetId());
            reference.getEstablishMethod().invoke(reference.getSourceObj(), new Object[] { target });
        }
    }

}
