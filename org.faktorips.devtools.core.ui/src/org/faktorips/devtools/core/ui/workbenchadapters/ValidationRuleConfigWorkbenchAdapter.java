/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.productcmpt.IValidationRuleConfig;

public class ValidationRuleConfigWorkbenchAdapter extends IpsElementWorkbenchAdapter {

    private final ImageDescriptor imageDescriptor;

    public ValidationRuleConfigWorkbenchAdapter() {
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("ValidationRuleDef.gif", true); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     * 
     * If the given {@link IIpsElement} is an {@link IValidationRuleConfig}, this method returns the
     * validation rule configuration's caption.
     */
    @Override
    protected String getLabel(IIpsElement ipsElement) {
        if (ipsElement instanceof IValidationRuleConfig) {
            return IIpsModel.get().getMultiLanguageSupport().getLocalizedCaption((IValidationRuleConfig)ipsElement);
        }
        return super.getLabel(ipsElement);
    }

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        if (ipsElement instanceof IValidationRuleConfig) {
            IValidationRuleConfig config = (IValidationRuleConfig)ipsElement;
            if (config.isActive()) {
                return getDefaultImageDescriptor();
            } else {
                return IpsUIPlugin.getImageHandling().createDisabledImageDescriptor(getDefaultImageDescriptor());
            }
        }
        return null;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return imageDescriptor;
    }

}
