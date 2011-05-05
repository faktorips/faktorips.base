/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class ValidationRuleConfigWorkbenchAdapter extends IpsElementWorkbenchAdapter {

    private final ImageDescriptor imageDescriptor;

    public ValidationRuleConfigWorkbenchAdapter(ImageDescriptor imageDescriptor) {
        this.imageDescriptor = imageDescriptor;
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
            IpsPlugin.getMultiLanguageSupport().getDefaultCaption((IValidationRuleConfig)ipsElement);
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
