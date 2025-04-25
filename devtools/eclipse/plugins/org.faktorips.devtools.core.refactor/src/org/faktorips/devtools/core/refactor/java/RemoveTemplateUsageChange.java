/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.refactor.java;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.resource.ResourceChange;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.productcmpt.Cardinality;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;

/**
 * A {@link Change} that removes the used template from a product component.
 */
public class RemoveTemplateUsageChange extends ResourceChange {

    private final IIpsSrcFile templateUsingSrcFile;

    RemoveTemplateUsageChange(IIpsSrcFile templateUsingSrcFile) {
        this.templateUsingSrcFile = templateUsingSrcFile;
    }

    @Override
    public Change perform(IProgressMonitor progressMonitor) {
        if (!templateUsingSrcFile.exists()) {
            return null;
        }
        IProgressMonitor pm = IProgressMonitor.nullSafe(progressMonitor);
        pm.beginTask("", 1); //$NON-NLS-1$
        try {
            IProductCmpt product = (IProductCmpt)templateUsingSrcFile.getIpsObject();
            String templateName = product.getTemplate();
            pm.setTaskName(NLS.bind(Messages.RemoveTemplateUsageChange_TaskName, templateName, getName()));
            product.setTemplate(null);
            boolean deletedLinks = removeUndefinedLinks(product.getLinksAsList());
            for (IProductCmptGeneration generation : product.getProductCmptGenerations()) {
                deletedLinks |= removeUndefinedLinks(generation.getLinksAsList());
            }
            templateUsingSrcFile.save(pm);
            if (deletedLinks) {
                // Can't undo this, because the order of links is destroyed. The user will have to
                // reset via VCS.
                return null;
            } else {
                return new UndoRemoveTemplateUsageChange(templateUsingSrcFile, templateName);
            }
        } catch (IpsException e) {
            if (!templateUsingSrcFile.exists()) {
                return null;
            } else {
                throw e;
            }
        } finally {
            pm.done();
        }
    }

    private boolean removeUndefinedLinks(List<IProductCmptLink> links) {
        boolean deletedLinks = false;
        for (IProductCmptLink link : links) {
            if (Cardinality.UNDEFINED.equals(link.getCardinality())) {
                link.delete();
                deletedLinks = true;
            }
        }
        return deletedLinks;
    }

    @Override
    public String getName() {
        return templateUsingSrcFile.getQualifiedNameType().getName();
    }

    @Override
    protected IResource getModifiedResource() {
        return templateUsingSrcFile.getCorrespondingResource().unwrap();
    }

    static class UndoRemoveTemplateUsageChange extends ResourceChange {

        private final IIpsSrcFile templateUsingSrcFile;
        private final String templateName;

        UndoRemoveTemplateUsageChange(IIpsSrcFile templateUsingSrcFile, String templateName) {
            this.templateUsingSrcFile = templateUsingSrcFile;
            this.templateName = templateName;
        }

        @Override
        public Change perform(IProgressMonitor progressMonitor) {
            IProgressMonitor pm = IProgressMonitor.nullSafe(progressMonitor);
            pm.beginTask("", 1); //$NON-NLS-1$
            IProductCmpt product = (IProductCmpt)templateUsingSrcFile.getIpsObject();
            pm.setTaskName(NLS.bind(Messages.UndoRemoveTemplateUsageChange_TaskName, templateName, getName()));

            product.setTemplate(templateName);
            templateUsingSrcFile.save(pm);

            pm.done();
            // TODO return Undo Change
            return null;
        }

        @Override
        public String getName() {
            return templateUsingSrcFile.getQualifiedNameType().getName();
        }

        @Override
        protected IResource getModifiedResource() {
            return templateUsingSrcFile.getCorrespondingResource().unwrap();
        }
    }
}
