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
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.util.LinkCreatorUtil;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;

/**
 * Operation that is intended to be used by {@link NewProductWizard} to create the new
 * {@link IIpsSrcFile} any add the newly created component to a selected link.
 */
public class AddNewProductCmptOperation extends NewProductCmptOperation {

    public AddNewProductCmptOperation(NewProductCmptPMO pmo) {
        super(pmo);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The {@link AddNewProductCmptOperation} implementation adds the ability to also create a new
     * {@link IProductCmptLink} using the new product component as target. The link will be created
     * at the {@link IProductCmptGeneration} as configured by the {@link NewProductCmptPMO}.
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void postProcess(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor) {
        monitor.beginTask(null, 2);

        IProductCmptTypeAssociation association = getPmo().getAddToAssociation();
        IProductCmptGeneration generationToAddTo = getPmo().getAddToProductCmptGeneration();
        // @formatter:off
        if (generationToAddTo != null
                && IpsUIPlugin.getDefault().isGenerationEditable(generationToAddTo)
                && association != null) {
            // @formatter:on
            IIpsSrcFile srcFile = generationToAddTo.getIpsSrcFile();
            if (getPmo().getValidator().validateAddToGeneration().isEmpty()) {
                boolean wasDirty = srcFile.isDirty();

                String targetQName = ipsSrcFile.getQualifiedNameType().getName();
                new LinkCreatorUtil(false).createLink(association, generationToAddTo, targetQName);
                monitor.worked(1);

                if (!wasDirty) {
                    srcFile.save(true, new org.eclipse.core.runtime.SubProgressMonitor(monitor, 1));
                }
            }
        }
    }

}
