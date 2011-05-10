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
import org.eclipse.jface.viewers.IDecoration;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class ValidationRuleWorkbenchAdapter extends IpsElementWorkbenchAdapter {

    private final ImageDescriptor imageDescriptor;

    public ValidationRuleWorkbenchAdapter() {
        imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("ValidationRuleDef.gif", true); //$NON-NLS-1$
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
            IpsPlugin.getMultiLanguageSupport().getLocalizedLabel((IValidationRule)ipsElement);
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
        if (ipsElement instanceof IValidationRule) {
            IValidationRule rule = (IValidationRule)ipsElement;
            if (rule.isConfigurableByProductComponent()) {
                return IpsUIPlugin.getImageHandling().getSharedOverlayImage("ValidationRuleDef.gif", //$NON-NLS-1$
                        "ProductRelevantOverlay.gif", //$NON-NLS-1$
                        IDecoration.TOP_RIGHT);
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
