/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.core.model.IIpsMetaObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.views.instanceexplorer.InstanceExplorer;

/**
 * An Action to show a selected element in instance explorer
 * 
 * @author Cornelius Dirmeier
 */
public class ShowInstanceAction extends IpsAction {

    /**
     * Use this constructor if you did not already extracted the IIpsElement from the
     * selectionProvider
     * 
     * @param selectionProvider the selection provider for this action
     */
    public ShowInstanceAction(ISelectionProvider selectionProvider) {
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
    public ShowInstanceAction(IIpsElement selectedElement, ISelectionProvider selectionProvider) {
        super(selectionProvider);
        initialize(selectedElement);
    }

    private void initialize(IIpsElement selectedElement) {
        if (selectedElement instanceof IIpsMetaClass) {
            setText(Messages.ShowInstanceAction_nameForTypes);
            setDescription(Messages.ShowInstanceAction_descriptionForTypes);
        } else if (selectedElement instanceof IIpsMetaObject) {
            setText(NLS.bind(Messages.ShowInstanceAction_nameForInstances, ((IIpsMetaObject)selectedElement)
                    .getIpsObjectType().getDisplayNamePlural()));
            setDescription(Messages.ShowInstanceAction_descriptionForInstances);
        } else {
            setText(NLS.bind(Messages.ShowInstanceAction_nameForInstances, "")); //$NON-NLS-1$
        }
        setToolTipText(getDescription());
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(InstanceExplorer.IMAGE));
    }

    @Override
    public void run(IStructuredSelection selection) {
        IIpsObject ipsObject = getIpsObjectForSelection(selection);
        if (ipsObject == null) {
            return;
        }
        if (InstanceExplorer.supports(ipsObject)) {
            try {
                IViewPart pse = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage()
                        .showView(InstanceExplorer.EXTENSION_ID);
                ((InstanceExplorer)pse).showInstancesOf(ipsObject);
            } catch (PartInitException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
    }

}
