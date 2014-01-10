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

package org.faktorips.devtools.stdbuilder.persistence;

import org.faktorips.codegen.JavaCodeFragment;
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
    public void addAnnotationConverter(JavaCodeFragment javaCodeFragment,
            IPersistentAttributeInfo persistentAttributeInfo) {
        throw new UnsupportedOperationException();
    }

}
