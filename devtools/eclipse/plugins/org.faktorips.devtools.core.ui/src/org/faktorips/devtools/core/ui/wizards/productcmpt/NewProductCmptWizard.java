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

import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class NewProductCmptWizard extends NewProductWizard {

    public static final String PRODUCT_CMPT_WIZARD_ID = "newProductCmptWizard"; //$NON-NLS-1$

    public NewProductCmptWizard() {
        super(new NewProductCmptPMO(false));
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "wizards/NewProductCmptWizard.png")); //$NON-NLS-1$
    }

    @Override
    protected String getDialogId() {
        return PRODUCT_CMPT_WIZARD_ID;
    }

}
