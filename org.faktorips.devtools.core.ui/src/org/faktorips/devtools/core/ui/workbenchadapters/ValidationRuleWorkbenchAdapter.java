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
import org.faktorips.devtools.core.ui.OverlayIcons;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.pctype.IValidationRule;

public class ValidationRuleWorkbenchAdapter extends IpsElementWorkbenchAdapter {

    public static final String VALIDATION_RULE_DEF_BASE_IMAGE = "ValidationRuleDef.gif"; //$NON-NLS-1$

    private final ImageDescriptor imageDescriptor;

    public ValidationRuleWorkbenchAdapter() {
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor(VALIDATION_RULE_DEF_BASE_IMAGE, true);
    }

    /**
     * {@inheritDoc}
     * 
     * If the given {@link IIpsElement} is an {@link IValidationRule}, this method returns the
     * validation rule's label.
     */
    @Override
    protected String getLabel(IIpsElement ipsElement) {
        if (ipsElement instanceof IValidationRule) {
            IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel((IValidationRule)ipsElement);
        }
        return super.getLabel(ipsElement);
    }

    /**
     * If the given {@link IIpsElement} is an {@link IValidationRule}, this method returns the
     * validation rule's icon, else <code>null</code>. In case of a configurable validation rule an
     * overlayIcon (product configurable overlay) is created.
     */
    @Override
    protected ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        String[] overlays = new String[4];
        if (ipsElement instanceof IValidationRule) {
            IValidationRule rule = (IValidationRule)ipsElement;
            if (rule.isConfigurableByProductComponent()) {
                overlays[1] = OverlayIcons.PRODUCT_OVR;
                if (!rule.isChangingOverTime()) {
                    overlays[0] = OverlayIcons.NOT_CHANGEOVERTIME_OVR;
                }
                return IpsUIPlugin.getImageHandling().getSharedOverlayImage(VALIDATION_RULE_DEF_BASE_IMAGE, overlays);
            } else {
                return getDefaultImageDescriptor();
            }
        }
        return null;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return imageDescriptor;
    }

}
