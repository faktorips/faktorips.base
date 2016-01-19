/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.views.producttemplate;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * Action to show the {@link TemplatePropertyUsageView} for a property.
 */
public class ShowTemplatePropertyUsageViewAction extends Action {

    private final IPropertyValue propertyValue;

    /**
     * Creates an action to show the {@link TemplatePropertyUsageView}
     * 
     * @param templatePropertyValue The property value which is the root of the template hierarchy
     * @param text The caption of this action
     */
    public ShowTemplatePropertyUsageViewAction(IPropertyValue templatePropertyValue, String text) {
        super(text, IpsUIPlugin.getImageHandling().getSharedImageDescriptor("TemplateUsage.gif", true)); //$NON-NLS-1$
        this.propertyValue = templatePropertyValue;
    }

    @Override
    public void run() {
        try {
            TemplatePropertyUsageViewPart viewPart = (TemplatePropertyUsageViewPart)PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getActivePage().showView(TemplatePropertyUsageView.VIEW_ID);
            viewPart.setPropertyValue(propertyValue);
        } catch (PartInitException e) {
            throw new CoreRuntimeException(e);
        }
    }

}
