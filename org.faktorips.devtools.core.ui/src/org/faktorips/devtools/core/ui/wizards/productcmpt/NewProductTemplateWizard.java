/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.wizards.productcmpt;

import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class NewProductTemplateWizard extends NewProductWizard {

    public static final String PRODUCT_TEMPLATE_WIZARD_ID = "newProductTemplateWizard"; //$NON-NLS-1$

    public NewProductTemplateWizard() {
        super(new NewProductCmptPMO(true));
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "wizards/NewProductTemplateWizard.png")); //$NON-NLS-1$
    }

    @Override
    protected String getDialogId() {
        return PRODUCT_TEMPLATE_WIZARD_ID;
    }

}
