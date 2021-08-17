/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.productcmpt.InferTemplateWizard;

public class InferTemplateHandler extends AbstractHandler {

    public static final String CONTRIBUTION_ID = "org.faktorips.devtools.core.ui.command.inferTemplate"; //$NON-NLS-1$

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
        InferTemplateWizard.open(window, HandlerUtil.getCurrentSelection(event));
        return null;
    }

    public static IContributionItem createContributionItem(IServiceLocator serviceLocator) {
        CommandContributionItemParameter inferProductTemplateParameter = new CommandContributionItemParameter(
                serviceLocator, null, InferTemplateHandler.CONTRIBUTION_ID, SWT.PUSH);
        inferProductTemplateParameter.icon = IpsUIPlugin.getImageHandling().createImageDescriptor(
                "InferProductTemplate.gif"); //$NON-NLS-1$
        CommandContributionItem inferProductTemplateItem = new CommandContributionItem(inferProductTemplateParameter);
        inferProductTemplateItem.setVisible(inferProductTemplateItem.isEnabled());

        return inferProductTemplateItem;
    }

}
