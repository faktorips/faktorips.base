/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.productcmpt.link.AbstractAssociationViewItem;
import org.faktorips.devtools.core.ui.util.LinkCreatorUtil;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;

/**
 * Sorts existing links alphabetically. Used for sorting links in the plugin menu
 *
 * @since 25.1
 */
public class SortAlphabetically extends AbstractHandler {

    public static final String COMMAND_ID = "org.faktorips.devtools.core.ui.commands.SortAlphabetically";

    public SortAlphabetically() {
        super();
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection structuredSelection) {
            sortAlphabetically(event);
        } else {
            throw new RuntimeException();
        }
        return null;
    }

    /**
     * Helper method to apply the sorted list of links to the UI
     *
     * @param event event which triggers the sorting
     */
    private void sortAlphabetically(ExecutionEvent event) {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        TypedSelection<AbstractAssociationViewItem> typedSelection = new TypedSelection<>(
                AbstractAssociationViewItem.class, selection);
        if (!typedSelection.isValid()) {
            return;
        }

        IEditorPart editor = HandlerUtil.getActiveEditor(event);
        if (!(editor instanceof ProductCmptEditor productCmptEditor)) {
            return;
        }
        AbstractAssociationViewItem associationViewItem = typedSelection.getFirstElement();
        ProductCmpt productCmpt = (ProductCmpt)productCmptEditor.getProductCmpt();
        IProductCmptType productCmptType = productCmpt.findProductCmptType(productCmpt.getIpsProject());
        IProductCmptTypeAssociation association = (IProductCmptTypeAssociation)productCmptType.findAssociation(
                associationViewItem.getAssociationName(), productCmpt.getIpsProject());
        IpsUIPlugin.getDefault().runWorkspaceModification($ -> {
            getExistingLinksInOrder(productCmpt.getIpsProject(),
                    (IProductCmptGeneration)productCmptEditor.getActiveGeneration(), association);
        });
    }

    /**
     * Retrieves existing links in a list and sorts them in alphabetical order
     *
     * @param ipsProject current IPS project
     * @param activeGeneration active generation of the product component
     * @param association associated product component type
     */
    public static void getExistingLinksInOrder(IIpsProject ipsProject,
            IProductCmptGeneration activeGeneration,
            IProductCmptTypeAssociation association) {
        IProductCmptLinkContainer container = LinkCreatorUtil.getLinkContainerFor(activeGeneration, association);
        List<IProductCmptLink> links = container.getLinksAsList(association.getName());
        List<IProductCmptLink> sortedLinks = links.stream().sorted(
                (x, y) -> x.findTarget(ipsProject).getName().compareToIgnoreCase(y.findTarget(ipsProject).getName()))
                .collect(Collectors.toList());
        if (!links.equals(sortedLinks)) {
            for (int i = 0; i < sortedLinks.size(); i++) {
                IProductCmptLink expectedLink = sortedLinks.get(i);

                IProductCmptLink currentLink = links.get(i);
                if (!expectedLink.equals(currentLink)) {
                    container.moveLink(expectedLink, currentLink, true);
                    // Refresh the list to reflect the new order after moving
                    links = container.getLinksAsList(association.getName());
                }
            }
        }
    }

}
