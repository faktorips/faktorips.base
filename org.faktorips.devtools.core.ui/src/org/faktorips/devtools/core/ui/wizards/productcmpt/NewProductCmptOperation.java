/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionOperation;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;

/**
 * Operation that is intended to be used by {@link NewProductWizard} to create the new
 * {@link IIpsSrcFile}.
 */
public class NewProductCmptOperation extends NewProductDefinitionOperation<NewProductCmptPMO> {

    public NewProductCmptOperation(NewProductCmptPMO pmo) {
        super(pmo);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The {@link NewProductCmptOperation} implementation sets the product component type and
     * runtime id as configured by the user. Furthermore, a product component generation is created
     * being valid from the effective date as provided by the user. Finally, all differences to the
     * model are fixed.
     */
    @Override
    protected void finishIpsSrcFile(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor) {
        IProductCmpt newProductCmpt = initProductCmpt(ipsSrcFile);
        IProductCmptGeneration generation = (IProductCmptGeneration)newProductCmpt.newGeneration();
        generation.setValidFrom(getPmo().getEffectiveDate());
        newProductCmpt.fixAllDifferencesToModel(getPmo().getIpsProject());
        monitor.worked(1);
    }

    protected IProductCmpt initProductCmpt(IIpsSrcFile ipsSrcFile) {
        IProductCmpt newProductCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
        newProductCmpt.setProductCmptType(getPmo().getSelectedType().getQualifiedName());
        newProductCmpt.setRuntimeId(getPmo().getRuntimeId());
        newProductCmpt.setTemplate(getPmo().getSelectedTemplate() == null ? null
                : getPmo().getSelectedTemplate()
                        .getName());
        return newProductCmpt;
    }

    @Override
    protected void postProcess(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor) {
        // do nothing
    }

}
