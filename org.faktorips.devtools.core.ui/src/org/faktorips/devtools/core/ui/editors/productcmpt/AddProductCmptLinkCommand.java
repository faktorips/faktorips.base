/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.util.memento.Memento;

/**
 * Opens the wizard to create a new product component relation.
 * 
 * @author Thorsten Guenther
 */
public class AddProductCmptLinkCommand extends AbstractHandler {

    private Memento syncpoint;

    private boolean isDirty = true;

    public AddProductCmptLinkCommand() {
        super();
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        IEditorPart editor = HandlerUtil.getActiveEditorChecked(event);
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        TypedSelection<String> typedSelection = new TypedSelection<String>(String.class, selection);
        if (editor instanceof ProductCmptEditor && typedSelection.isValid()) {
            ProductCmptEditor productCmptEditor = (ProductCmptEditor)editor;
            IProductCmptGeneration activeGeneration = (IProductCmptGeneration)productCmptEditor.getActiveGeneration();
            String associationName = typedSelection.getFirstElement();
            setSyncpoint(activeGeneration);
            IProductCmptLink relation = activeGeneration.newLink(associationName);
            relation.setMaxCardinality(1);
            relation.setMinCardinality(0); // todo get min from modell
            LinkEditDialog dialog = new LinkEditDialog(relation, shell);
            dialog.setProductCmptsToExclude(getRelationTargetsFor(activeGeneration, associationName));
            int rc = dialog.open();
            if (rc == Window.CANCEL) {
                reset(activeGeneration);
            } else if (rc == Window.OK) {
                // parent.refresh();
            }
        }
        return null;
    }

    /**
     * Returns all targets for all relations defined with the given product component relation type.
     * 
     * @param associationName The type of the relations to find.
     */
    public IProductCmpt[] getRelationTargetsFor(IProductCmptGeneration activeGeneration, String associationName) {
        IProductCmptLink[] links = activeGeneration.getLinks(associationName);
        IProductCmpt[] targets = new IProductCmpt[links.length];
        for (int i = 0; i < links.length; i++) {
            try {
                targets[i] = (IProductCmpt)activeGeneration.getIpsProject().findIpsObject(IpsObjectType.PRODUCT_CMPT,
                        links[i].getTarget());
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        return targets;
    }

    private void setSyncpoint(IProductCmptGeneration generation) {
        syncpoint = generation.newMemento();
        isDirty = generation.getIpsObject().getIpsSrcFile().isDirty();
    }

    private void reset(IProductCmptGeneration generation) {
        if (syncpoint != null) {
            generation.setState(syncpoint);
        }
        if (!isDirty) {
            generation.getIpsObject().getIpsSrcFile().markAsClean();
        }
    }

}
