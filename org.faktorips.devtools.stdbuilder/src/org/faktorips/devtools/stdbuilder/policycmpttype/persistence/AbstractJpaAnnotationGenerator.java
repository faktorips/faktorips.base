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

package org.faktorips.devtools.stdbuilder.policycmpttype.persistence;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.stdbuilder.AbstractAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.persistence.IPersistenceProvider;

/**
 * Abstract base class for annotation generators.
 */
public abstract class AbstractJpaAnnotationGenerator extends AbstractAnnotationGenerator {

    @Override
    public boolean isGenerateAnnotationFor(IIpsElement ipsElement) {
        return (getPersistenceProvider(ipsElement) != null) && isGenerateAnnotationForInternal(ipsElement);
    }

    IPersistenceProvider getPersistenceProvider(IIpsElement ipsElement) {
        IIpsArtefactBuilderSet ipsArtefactBuilderSet = ipsElement.getIpsProject().getIpsArtefactBuilderSet();
        if (ipsArtefactBuilderSet instanceof StandardBuilderSet) {
            StandardBuilderSet standardBuilderSet = (StandardBuilderSet)ipsArtefactBuilderSet;
            IPersistenceProvider persistenceProviderImpl = standardBuilderSet.getPersistenceProviderImplementation();
            return persistenceProviderImpl;
        }
        return null;
    }

    protected abstract boolean isGenerateAnnotationForInternal(IIpsElement ipsElement);
}
