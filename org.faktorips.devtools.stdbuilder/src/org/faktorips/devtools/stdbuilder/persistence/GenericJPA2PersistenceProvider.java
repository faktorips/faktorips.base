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
import org.faktorips.devtools.model.builder.IPersistenceProvider;
import org.faktorips.devtools.model.pctype.persistence.IPersistentAttributeInfo;
import org.faktorips.devtools.model.pctype.persistence.IPersistentTypePartInfo;
import org.faktorips.devtools.model.util.PersistenceSupportNames;

/**
 * Persistence provider for standard generic JPA 2 support
 */
public class GenericJPA2PersistenceProvider implements IPersistenceProvider {

    /**
     * @deprecated Use {@link PersistenceSupportNames#ID_GENERIC_JPA_2} instead.
     */
    @Deprecated(forRemoval = true, since = "21.12")
    public static final String ID_GENERIC_JPA_2 = PersistenceSupportNames.ID_GENERIC_JPA_2;

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
