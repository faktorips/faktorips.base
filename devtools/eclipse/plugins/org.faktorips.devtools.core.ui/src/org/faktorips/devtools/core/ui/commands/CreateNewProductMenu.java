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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.actions.Messages;
import org.faktorips.devtools.model.ipsproject.IChangesOverTimeNamingConvention;

/**
 * This dynamic menu builder creates commands for work with products. With these commands you could
 * create a new copy of a product, create a new version or add a new generation.
 * 
 * @author dirmeier
 */
public class CreateNewProductMenu extends CompoundContributionItem implements IWorkbenchContribution {

    public static final String COMMAND_DEEP_COPY_PRODUCT = "org.faktorips.devtools.core.ui.command.DeepCopyProduct"; //$NON-NLS-1$

    public static final String COMMAND_CREATE_NEW_VERSION = "org.faktorips.devtools.core.ui.command.CreateNewVersion"; //$NON-NLS-1$

    public static final String COMMAND_CREATE_NEW_GENERATION = "org.faktorips.devtools.core.ui.command.CreateNewGeneration"; //$NON-NLS-1$

    public static final String COMMAND_COPY_RUNTIMEID = "org.faktorips.devtools.core.ui.command.CopyRuntimeId"; //$NON-NLS-1$

    private IServiceLocator serviceLocator;

    @Override
    public void initialize(IServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    @Override
    protected IContributionItem[] getContributionItems() {
        IChangesOverTimeNamingConvention changesOverTimeNamingConvention = IpsPlugin.getDefault().getIpsPreferences()
                .getChangesOverTimeNamingConvention();
        List<IContributionItem> contributionItems = new ArrayList<>();

        CommandContributionItemParameter createNewVersionParameter = new CommandContributionItemParameter(
                serviceLocator, null, COMMAND_CREATE_NEW_VERSION, SWT.PUSH);
        createNewVersionParameter.label = NLS.bind(Messages.IpsDeepCopyAction_nameNewVersion,
                changesOverTimeNamingConvention.getVersionConceptNameSingular());
        createNewVersionParameter.icon = IpsUIPlugin.getImageHandling().createImageDescriptor("NewVersionWizard.gif"); //$NON-NLS-1$
        CommandContributionItem createNewVersionItem = new CommandContributionItem(createNewVersionParameter);
        createNewVersionItem.setVisible(createNewVersionItem.isEnabled());
        contributionItems.add(createNewVersionItem);

        CommandContributionItemParameter createNewGenerationParameter = new CommandContributionItemParameter(
                serviceLocator, null, COMMAND_CREATE_NEW_GENERATION, SWT.PUSH);
        createNewGenerationParameter.label = NLS.bind(Messages.CreateNewGenerationAction_title,
                changesOverTimeNamingConvention.getGenerationConceptNameSingular());
        createNewGenerationParameter.icon = IpsUIPlugin.getImageHandling()
                .createImageDescriptor("NewProductCmptGeneration.gif"); //$NON-NLS-1$
        CommandContributionItem createNewGenerationItem = new CommandContributionItem(createNewGenerationParameter);
        createNewGenerationItem.setVisible(createNewGenerationItem.isEnabled());
        contributionItems.add(createNewGenerationItem);

        CommandContributionItemParameter copyProductParameter = new CommandContributionItemParameter(serviceLocator,
                null, COMMAND_DEEP_COPY_PRODUCT, SWT.PUSH);
        copyProductParameter.label = Messages.IpsDeepCopyAction_name;
        copyProductParameter.icon = IpsUIPlugin.getImageHandling().createImageDescriptor("DeepCopyWizard.gif"); //$NON-NLS-1$
        CommandContributionItem copyProductItem = new CommandContributionItem(copyProductParameter);
        copyProductItem.setVisible(copyProductItem.isEnabled());
        contributionItems.add(copyProductItem);

        CommandContributionItemParameter copyRuntimeIdParameter = new CommandContributionItemParameter(serviceLocator,
                null, COMMAND_COPY_RUNTIMEID, SWT.PUSH);
        copyRuntimeIdParameter.label = Messages.CopyRuntimeId_name;
        copyRuntimeIdParameter.icon = IpsUIPlugin.getImageHandling().createImageDescriptor("CopyRuntimeId.gif"); //$NON-NLS-1$
        CommandContributionItem copyRuntimeIdItem = new CommandContributionItem(copyRuntimeIdParameter);
        copyRuntimeIdItem.setVisible(copyRuntimeIdItem.isEnabled());
        contributionItems.add(copyRuntimeIdItem);

        contributionItems.add(InferTemplateHandler.createContributionItem(serviceLocator));

        return contributionItems.toArray(new IContributionItem[contributionItems.size()]);
    }
}
