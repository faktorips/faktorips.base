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

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.PluginMessages;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.core.ui.wizards.productcmpt.AddNewProductCmptCommand;

public class AddNewProductCmptLinkSubmenuFactory extends ExtensionContributionFactory {

    public AddNewProductCmptLinkSubmenuFactory() {
        super();
    }

    @Override
    public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
        ISelectionService selectionService = (ISelectionService)serviceLocator.getService(ISelectionService.class);
        TypedSelection<IProductCmptReference> typedSelection = new TypedSelection<IProductCmptReference>(
                IProductCmptReference.class, selectionService.getSelection());
        if (!typedSelection.isValid()) {
            return;
        }
        IProductCmptReference productCmptRef = typedSelection.getFirstElement();
        IProductCmptTypeAssociationReference[] children = productCmptRef.getStructure()
                .getChildProductCmptTypeAssociationReferences(productCmptRef);
        // if there is only one child the default handler would handle the command
        if (children.length == 0) {
            // adding the command item knowing that it is disabled because the handler is disabled
            addDisabledCommand(serviceLocator, additions);
        }
        if (children.length > 1) {
            createMultipleAddNewItem(serviceLocator, additions, children);
        }
    }

    private void addDisabledCommand(IServiceLocator serviceLocator, IContributionRoot additions) {
        CommandContributionItemParameter parameters = new CommandContributionItemParameter(serviceLocator,
                StringUtils.EMPTY, AddNewProductCmptCommand.COMMAND_ID, SWT.PUSH);
        parameters.icon = getAddNewImageDescriptor();
        CommandContributionItem item = new CommandContributionItem(parameters);
        item.setVisible(true);
        additions.addContributionItem(item, null);
    }

    private void createMultipleAddNewItem(IServiceLocator serviceLocator,
            IContributionRoot additions,
            IProductCmptTypeAssociationReference[] children) {
        MenuManager menuManager = new MenuManager(PluginMessages.newProductCmpt_label, getAddNewImageDescriptor(), null);
        additions.addContributionItem(menuManager, null);
        for (IProductCmptStructureReference child : children) {
            if (child instanceof IProductCmptTypeAssociationReference) {
                IProductCmptTypeAssociationReference associationReference = (IProductCmptTypeAssociationReference)child;
                CommandContributionItemParameter itemParameter = new CommandContributionItemParameter(serviceLocator,
                        StringUtils.EMPTY, AddNewProductCmptCommand.COMMAND_ID, SWT.PUSH);
                itemParameter.label = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(
                        associationReference.getAssociation());
                HashMap<String, String> parameters = new HashMap<String, String>();
                parameters.put(AddNewProductCmptCommand.PARAMETER_SELECTED_ASSOCIATION, associationReference
                        .getAssociation().getName());
                itemParameter.parameters = parameters;
                CommandContributionItem item = new CommandContributionItem(itemParameter);
                item.setVisible(true);
                menuManager.add(item);
            }
        }

    }

    private ImageDescriptor getAddNewImageDescriptor() {
        ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("Add_new.gif", true); //$NON-NLS-1$
        return imageDescriptor;
    }
}
