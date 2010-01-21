/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeRelationReference;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.actions.Messages;
import org.faktorips.util.memento.Memento;

/**
 * Opens the wizard to create a new product component relation.
 * 
 * @author Thorsten Guenther
 */
public class NewProductCmptRelationAction extends IpsAction {

    private Shell shell;
    private LinksSection parent;
    private Memento syncpoint;

    private boolean isDirty = true;

    public NewProductCmptRelationAction(Shell shell, ISelectionProvider selectionProvider, LinksSection parent) {
        super(selectionProvider);
        this.shell = shell;
        this.parent = parent;
        setControlWithDataChangeableSupport(parent);
        setText(Messages.NewProductCmptRelationAction_name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean computeEnabledProperty(IStructuredSelection selection) {
        if (selection.isEmpty()) {
            return false;
        }
        Object selected = selection.getFirstElement();
        return (selected instanceof IProductCmptTypeRelationReference);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(IStructuredSelection selection) {
        Object selected = selection.getFirstElement();
        if (selected instanceof IProductCmptTypeRelationReference) {
            IProductCmptTypeRelationReference reference = (IProductCmptTypeRelationReference)selected;
            setSyncpoint();
            IProductCmptLink relation = parent.newLink(reference.getRelation().getName());
            relation.setMaxCardinality(1);
            relation.setMinCardinality(0); // todo get min from modell
            LinkEditDialog dialog = new LinkEditDialog(relation, shell);
            dialog.setProductCmptsToExclude(parent.getRelationTargetsFor(reference.getRelation().getName()));
            int rc = dialog.open();
            if (rc == Window.CANCEL) {
                reset();
            } else if (rc == Window.OK) {
                parent.refresh();
            }
        }
    }

    private void setSyncpoint() {
        IProductCmptGeneration generation = parent.getActiveGeneration();
        syncpoint = generation.newMemento();
        isDirty = generation.getIpsObject().getIpsSrcFile().isDirty();
    }

    private void reset() {
        IProductCmptGeneration generation = parent.getActiveGeneration();
        if (syncpoint != null) {
            generation.setState(syncpoint);
        }
        if (!isDirty) {
            generation.getIpsObject().getIpsSrcFile().markAsClean();
        }
        parent.refresh();
    }

}
