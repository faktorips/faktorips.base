/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
