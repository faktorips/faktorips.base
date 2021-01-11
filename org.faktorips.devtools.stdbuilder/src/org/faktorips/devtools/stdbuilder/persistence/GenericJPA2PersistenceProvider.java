/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
import org.faktorips.devtools.core.model.pctype.IPersistentTypePartInfo;

/**
 * Persistence provider for standard generic JPA 2 support
 */
public class GenericJPA2PersistenceProvider implements IPersistenceProvider {

    public static final String ID_GENERIC_JPA_2 = "Generic JPA 2.0"; //$NON-NLS-1$

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
    public JavaCodeFragment getIndexAnnotations(IPersistentTypePartInfo persistentAttributeInfo) {
        throw new UnsupportedOperationException();
    }

}
