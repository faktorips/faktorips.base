/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.persistence;

import org.faktorips.devtools.core.builder.IPersistenceProvider;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.stdbuilder.AbstractAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;

/**
 * Abstract base class for annotation generators.
 */
public abstract class AbstractJpaAnnotationGenerator extends AbstractAnnotationGenerator {

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        IIpsObjectPartContainer ipsElement = modelNode.getIpsObjectPartContainer();
        return (getPersistenceProvider(ipsElement) != null) && isGenerateAnnotationForInternal(ipsElement);
    }

    IPersistenceProvider getPersistenceProvider(IIpsElement ipsElement) {
        IIpsArtefactBuilderSet ipsArtefactBuilderSet = ipsElement.getIpsProject().getIpsArtefactBuilderSet();
        IPersistenceProvider persistenceProviderImpl = ipsArtefactBuilderSet.getPersistenceProvider();
        return persistenceProviderImpl;
    }

    protected abstract boolean isGenerateAnnotationForInternal(IIpsElement ipsElement);
}
