/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.template;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.SingleEventModification;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;

/**
 * Updates a product component by removing the configured template (remove the reference, not the
 * template itself) and switch every property value that was {@link TemplateValueStatus#INHERITED}
 * to {@link TemplateValueStatus#DEFINED}
 * 
 * @author dirmeier
 */
public class RemoveTemplateOperation implements IWorkspaceRunnable {

    private final IProductCmpt productCmpt;

    public RemoveTemplateOperation(IProductCmpt productCmpt) {
        this.productCmpt = productCmpt;
    }

    @Override
    public void run(IProgressMonitor monitor) throws CoreException {
        RemoveTemplateModification modification = new RemoveTemplateModification(productCmpt);
        ((IpsModel)productCmpt.getIpsModel()).executeModificationsWithSingleEvent(modification);
    }

    protected static class RemoveTemplateModification extends SingleEventModification<Object> {

        private IProductCmpt productCmpt;

        RemoveTemplateModification(IProductCmpt productCmpt) {
            super(productCmpt.getIpsSrcFile());
            this.productCmpt = productCmpt;
        }

        @Override
        protected boolean execute() throws CoreException {
            processPropertyValues(productCmpt);
            processLinks(productCmpt);
            for (IProductCmptGeneration container : productCmpt.getProductCmptGenerations()) {
                processPropertyValues(container);
                processLinks(container);
            }
            return true;
        }

        private void processPropertyValues(IPropertyValueContainer container) {
            for (IPropertyValue value : container.getAllPropertyValues()) {
                switchInheritedValuesToDefined(value);
            }
        }

        private void processLinks(IProductCmptLinkContainer container) {
            container.removeUndefinedLinks();
            for (IProductCmptLink link : container.getLinksAsList()) {
                switchInheritedValuesToDefined(link);
            }
        }

        private void switchInheritedValuesToDefined(ITemplatedValue value) {
            if (value.getTemplateValueStatus() == TemplateValueStatus.INHERITED) {
                value.setTemplateValueStatus(TemplateValueStatus.DEFINED);
            }
        }

    }

}
