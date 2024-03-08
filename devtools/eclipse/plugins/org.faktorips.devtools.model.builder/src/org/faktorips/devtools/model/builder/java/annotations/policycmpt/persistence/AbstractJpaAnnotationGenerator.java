/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.annotations.policycmpt.persistence;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.builder.IPersistenceProvider;
import org.faktorips.devtools.model.builder.java.annotations.AbstractAnnotationGenerator;
import org.faktorips.devtools.model.builder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Abstract base class for annotation generators.
 */
public abstract class AbstractJpaAnnotationGenerator extends AbstractAnnotationGenerator {

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        IIpsObjectPartContainer ipsElement = modelNode.getIpsObjectPartContainer();
        return (getPersistenceProvider(ipsElement.getIpsProject()) != null)
                && isGenerateAnnotationForInternal(ipsElement);
    }

    protected IPersistenceProvider getPersistenceProvider(IIpsProject ipsProject) {
        IIpsArtefactBuilderSet ipsArtefactBuilderSet = ipsProject.getIpsArtefactBuilderSet();
        return ipsArtefactBuilderSet.getPersistenceProvider();
    }

    protected abstract boolean isGenerateAnnotationForInternal(IIpsElement ipsElement);

}
