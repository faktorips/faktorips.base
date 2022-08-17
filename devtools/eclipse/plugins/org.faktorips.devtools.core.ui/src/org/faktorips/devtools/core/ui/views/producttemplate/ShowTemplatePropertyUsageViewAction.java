/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;

/**
 * Action to show the {@link TemplatePropertyUsageView} for a property.
 */
public class ShowTemplatePropertyUsageViewAction extends Action {

    private final ITemplatedValue templateValue;

    /**
     * Creates an action to show the {@link TemplatePropertyUsageView}
     * 
     * @param templateValue The templated value which is the root of the template hierarchy
     * @param text The caption of this action
     */
    public ShowTemplatePropertyUsageViewAction(ITemplatedValue templateValue, String text) {
        super(text, IpsUIPlugin.getImageHandling().getSharedImageDescriptor("TemplateUsage.gif", true)); //$NON-NLS-1$
        this.templateValue = templateValue;
    }

    @Override
    public void run() {
        try {
            TemplatePropertyUsageViewPart viewPart = (TemplatePropertyUsageViewPart)PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getActivePage().showView(TemplatePropertyUsageView.VIEW_ID);
            viewPart.setTemplateValue(templateValue);
        } catch (PartInitException e) {
            throw new IpsException(e);
        }
    }

}
