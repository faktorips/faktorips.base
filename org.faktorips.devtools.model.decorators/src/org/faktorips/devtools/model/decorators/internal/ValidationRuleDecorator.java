/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.decorators.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsElementDecorator;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.pctype.IValidationRule;

public class ValidationRuleDecorator implements IIpsElementDecorator {

    public static final String VALIDATION_RULE_DEF_BASE_IMAGE = "ValidationRuleDef.gif"; //$NON-NLS-1$

    /**
     * If the given {@link IIpsElement} is an {@link IValidationRule}, this method returns the
     * validation rule's icon, else <code>null</code>. In case of a configurable validation rule an
     * overlayIcon (product configurable overlay) is created.
     */
    @Override
    public ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        String[] overlays = new String[4];
        if (ipsElement instanceof IValidationRule) {
            IValidationRule rule = (IValidationRule)ipsElement;
            if (rule.isConfigurableByProductComponent()) {
                overlays[1] = OverlayIcons.PRODUCT_RELEVANT;
                if (!rule.isChangingOverTime()) {
                    overlays[0] = OverlayIcons.STATIC;
                }
                return IIpsDecorators.getImageHandling().getSharedOverlayImageDescriptor(VALIDATION_RULE_DEF_BASE_IMAGE,
                        overlays);
            }
        }
        return getDefaultImageDescriptor();
    }

    /**
     * {@inheritDoc}
     * 
     * If the given {@link IIpsElement} is an {@link IValidationRule}, this method returns the
     * validation rule's label.
     */
    @Override
    public String getLabel(IIpsElement ipsElement) {
        if (ipsElement instanceof IValidationRule) {
            IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel((IValidationRule)ipsElement);
        }
        return IIpsElementDecorator.super.getLabel(ipsElement);
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IIpsDecorators.getImageHandling().getSharedImageDescriptor(VALIDATION_RULE_DEF_BASE_IMAGE, true);
    }

}
