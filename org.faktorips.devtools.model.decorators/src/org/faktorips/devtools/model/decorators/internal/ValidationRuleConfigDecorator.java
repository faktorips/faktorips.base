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
import org.faktorips.devtools.model.decorators.IIpsObjectPartDecorator;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.productcmpt.IValidationRuleConfig;

public class ValidationRuleConfigDecorator implements IIpsObjectPartDecorator {

    public static final String VALIDATION_RULE_DEF_BASE_IMAGE = "ValidationRuleDef.gif"; //$NON-NLS-1$

    /**
     * {@inheritDoc}
     * 
     * If the given {@link IIpsElement} is an {@link IValidationRuleConfig}, this method returns the
     * validation rule configuration's caption.
     */
    @Override
    public String getLabel(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IValidationRuleConfig) {
            return IIpsModel.get().getMultiLanguageSupport().getLocalizedCaption(ipsObjectPart);
        }
        return IIpsObjectPartDecorator.super.getLabel(ipsObjectPart);
    }

    @Override
    public ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IValidationRuleConfig) {
            IValidationRuleConfig config = (IValidationRuleConfig)ipsObjectPart;
            if (config.isActive()) {
                return getDefaultImageDescriptor();
            } else {
                return IIpsDecorators.getImageHandling().getDisabledImageDescriptor(getDefaultImageDescriptor());
            }
        }
        return getDefaultImageDescriptor();
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IIpsDecorators.getImageHandling().getSharedImageDescriptor(VALIDATION_RULE_DEF_BASE_IMAGE, true);
    }

}
