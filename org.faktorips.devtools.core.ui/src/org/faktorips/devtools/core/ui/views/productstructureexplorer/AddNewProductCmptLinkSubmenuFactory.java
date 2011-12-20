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

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.ui.util.TypedSelection;

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
        if (productCmptRef.getChildren().length == 1) {
            createSingleAddNewItem(serviceLocator, additions);
        } else if (productCmptRef.getChildren().length > 1) {
            createMultipleAddNewItem(serviceLocator, additions);
        }

        MenuManager menuManager = new MenuManager("asdasd");
        additions.addContributionItem(menuManager, null);
    }

    private void createSingleAddNewItem(IServiceLocator serviceLocator, IContributionRoot additions) {
        CommandContributionItemParameter parameters = new CommandContributionItemParameter(serviceLocator, "",
                "org.faktorips.devtools.core.ui.wizards.productcmpt.newProductCmpt", SWT.PUSH);
        CommandContributionItem item = new CommandContributionItem(parameters);
        item.setVisible(true);
        additions.addContributionItem(item, null);
    }

    private void createMultipleAddNewItem(IServiceLocator serviceLocator, IContributionRoot additions) {
        CommandContributionItemParameter parameters = new CommandContributionItemParameter(serviceLocator, "",
                "org.faktorips.devtools.core.ui.wizards.productcmpt.newProductCmpt", SWT.PUSH);
        CommandContributionItem item = new CommandContributionItem(parameters);
        item.setVisible(true);
        additions.addContributionItem(item, null);
    }
}
