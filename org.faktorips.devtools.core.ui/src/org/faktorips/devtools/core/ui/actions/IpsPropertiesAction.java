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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Opens a properties dialog for the selected object.
 * 
 * @author Stefan Widmaier
 */
public class IpsPropertiesAction extends IpsAction {

    private IWorkbenchSite site;

    public IpsPropertiesAction(IWorkbenchSite site, ISelectionProvider selectionProvider) {
        super(selectionProvider);
        this.site = site;
        setText(Messages.IpsPropertiesAction_name);
    }

    /**
     * Opens the Properties dialog for the selected Project. {@inheritDoc}
     */
    @Override
    public void run(IStructuredSelection selection) {
        Object selected = selection.getFirstElement();
        if (selected instanceof IIpsProject) {
            openPropertiesDialog(((IIpsProject)selected).getProject());
        } else if (selected != null) {
            openPropertiesDialog(selected);
        }
    }

    private void openPropertiesDialog(Object selectedObject) {
        IStructuredSelection sel = new StructuredSelection(selectedObject);
        SelProvider provider = new SelProvider(sel);
        PropertyDialogAction action = new PropertyDialogAction(site, provider);
        action.run();
    }

    /*
     * Helper class to simulate a selection provider
     */
    private static final class SelProvider implements ISelectionProvider {
        private IStructuredSelection projectSelection = StructuredSelection.EMPTY;

        protected SelProvider(IStructuredSelection selection) {
            projectSelection = selection;
        }

        public void addSelectionChangedListener(ISelectionChangedListener listener) {
            // do nothing
        }

        public ISelection getSelection() {
            return projectSelection;
        }

        public void removeSelectionChangedListener(ISelectionChangedListener listener) {
            // do nothing
        }

        public void setSelection(ISelection selection) {
            // do nothing
        }
    }

    /**
     * Returns <code>true</code> if the property action supports the given object.
     */
    public boolean isEnabledFor(Object selected) {
        if (selected instanceof IIpsObjectPart) {
            return false;
        }
        return true;
    }
}
