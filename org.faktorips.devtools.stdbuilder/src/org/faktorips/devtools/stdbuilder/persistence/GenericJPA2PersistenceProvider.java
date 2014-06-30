/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.persistence;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.builder.IPersistenceProvider;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;

/**
 * Persistence provider for standard generic JPA 2 support
 * 
 * @author Joerg Ortmann
 */
public class GenericJPA2PersistenceProvider implements IPersistenceProvider {

    @Override
    public boolean isSupportingConverters() {
        return false;
    }

    @Override
    public boolean isSupportingOrphanRemoval() {
        return true;
    }

    @Override
    public void addAnnotationOrphanRemoval(JavaCodeFragment javaCodeFragment) {
        // nothing to do
    }

    @Override
    public String getRelationshipAnnotationAttributeOrphanRemoval() {
        return "orphanRemoval=true"; //$NON-NLS-1$
    }

    @Override
    public JavaCodeFragment getConverterAnnotations(IPersistentAttributeInfo persistentAttributeInfo) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSupportingIndex() {
        return false;
    }

    @Override
    public JavaCodeFragment getIndexAnnotations(IPersistentAttributeInfo persistentAttributeInfo) {
        throw new UnsupportedOperationException();
    }

}
