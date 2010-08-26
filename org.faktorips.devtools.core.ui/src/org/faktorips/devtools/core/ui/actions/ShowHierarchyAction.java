/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsMetaClass;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.views.ipshierarchy.IpsHierarchy;

public class ShowHierarchyAction extends IpsAction {
    /**
     * Use this constructor if you did not already extracted the IIpsElement from the
     * selectionProvider
     * 
     * @param selectionProvider the selection provider for this action
     */
    public ShowHierarchyAction(ISelectionProvider selectionProvider) {
        super(selectionProvider);
        if (selectionProvider.getSelection() instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)selectionProvider.getSelection();
            initialize(getIpsObjectForSelection(structuredSelection));
        }
    }

    /**
     * Use this constructor if you already have extracted the IIpsElement from the selectionProvider
     * Uses the IpsAction(ISelectionProvider) constructor of {@link IpsAction}
     * 
     * @param selectedElement the selected element, this action is constructed for
     * @param selectionProvider the selection provider
     */
    public ShowHierarchyAction(IIpsElement selectedElement, ISelectionProvider selectionProvider) {
        super(selectionProvider);
        initialize(selectedElement);
    }

    private void initialize(IIpsElement selectedElement) {
        if (selectedElement instanceof IIpsMetaClass) {
            setText(Messages.ShowHierarchyAction_nameForTypes);
            setDescription(Messages.ShowHierarchyAction_descriptionForTypes);
            setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(IpsHierarchy.IMAGE));

        } else {
            setText(NLS.bind(Messages.ShowHierarchyAction_nameForInstances, "")); //$NON-NLS-1$
        }
        setToolTipText(getDescription());
    }

    @Override
    public void run(IStructuredSelection selection) {
        IIpsObject ipsObject = getIpsObjectForSelection(selection);
        if (ipsObject == null) {
            return;
        }
        if (IpsHierarchy.supports(ipsObject)) {
            try {
                IViewPart pse = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage()
                        .showView(IpsHierarchy.EXTENSION_ID);
                ((IpsHierarchy)pse).showInstancesOf(ipsObject);
            } catch (PartInitException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
    }
}
