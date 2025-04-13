/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.refactor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.ResizableWizard;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;

/**
 * A wizard that guides the user through the process of updating the "Valid From" date and
 * optionally the generation ID for a product component and its structure.
 */
public class IpsUpdateValidfromWizard extends ResizableWizard {

    private static final String IMAGE_PATH = "icons/wizards/UpdateValidFromWizard.png"; //$NON-NLS-1$
    private static final String PLUGIN_ID = "org.faktorips.devtools.core.ui"; //$NON-NLS-1$
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 800;

    private static final String SECTION_NAME = "UpdateValidfromWizard"; //$NON-NLS-1$

    private final UpdateValidfromPresentationModel presentationModel;

    public IpsUpdateValidfromWizard(IProductCmpt product) {
        super(SECTION_NAME, IpsPlugin.getDefault().getDialogSettings(), DEFAULT_WIDTH, DEFAULT_HEIGHT);

        presentationModel = new UpdateValidfromPresentationModel(product);
        ImageDescriptor img = IpsPlugin.imageDescriptorFromPlugin(PLUGIN_ID,
                IMAGE_PATH);
        setDefaultPageImageDescriptor(img);
        settingDefaults();
    }

    @Override
    public void addPages() {
        UpdateValidFromSourcePage sourcePage = new UpdateValidFromSourcePage(UpdateValidFromSourcePage.PAGE_ID);
        addPage(sourcePage);
        UpdateValidFromPreviewPage previewPage = new UpdateValidFromPreviewPage(UpdateValidFromPreviewPage.PAGE_ID);
        addPage(previewPage);
    }

    /**
     * Applies default configuration, such as setting the default "Valid From" date.
     */
    private void settingDefaults() {
        getPresentationModel().setNewValidFrom(IpsUIPlugin.getDefault().getDefaultValidityDate());
    }

    public UpdateValidfromPresentationModel getPresentationModel() {
        return presentationModel;
    }

    public IProductCmptTreeStructure getStructure() {
        return getPresentationModel().getStructure();
    }

    @Override
    public boolean performFinish() {
        // TODO Auto-generated method stub
        return false;
    }

}