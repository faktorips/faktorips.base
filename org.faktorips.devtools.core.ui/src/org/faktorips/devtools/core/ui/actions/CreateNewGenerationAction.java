/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.ITimedIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewGenerationWizard;

/**
 * Presents a wizard to the user that allows to create a new {@linkplain IIpsObjectGeneration IPS
 * Object Generation} for selected {@linkplain IProductCmpt Product Components}.
 */
public class CreateNewGenerationAction extends IpsAction {

    private final Shell shell;

    public CreateNewGenerationAction(Shell shell, ISelectionProvider selectionProvider) {
        super(selectionProvider);
        this.shell = shell;

        setText(NLS.bind(Messages.CreateNewGenerationAction_title, IpsPlugin.getDefault().getIpsPreferences()
                .getChangesOverTimeNamingConvention().getGenerationConceptNameSingular()));
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("NewProductCmptGeneration.gif")); //$NON-NLS-1$

        updateEnabledProperty();
    }

    @Override
    protected boolean computeEnabledProperty(IStructuredSelection selection) {
        TypedSelection<IAdaptable> typedSelection = TypedSelection.createAnyCount(IAdaptable.class, selection);
        if (!typedSelection.isValid()) {
            return false;
        }

        for (IAdaptable selectedElement : typedSelection.getElements()) {
            // If the selection contains any other type of elements, it cannot be started
            if (!(selectedElement instanceof IProductCmpt) && !(selectedElement instanceof IProductCmptReference)
                    && !(selectedElement instanceof IIpsSrcFile)) {
                return false;
            }

            // If the selection contains a source file, it must contain a product component
            if (selectedElement instanceof IIpsSrcFile) {
                IIpsSrcFile ipsSrcFile = (IIpsSrcFile)selectedElement;
                if (!IpsObjectType.PRODUCT_CMPT.equals(ipsSrcFile.getIpsObjectType())) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void run(IStructuredSelection selection) {
        if (!isEnabled()) {
            return;
        }

        TypedSelection<IAdaptable> typedSelection = TypedSelection.createAnyCount(IAdaptable.class, selection);
        List<ITimedIpsObject> timedIpsObjects = new ArrayList<ITimedIpsObject>(typedSelection.getElementCount());
        for (IAdaptable selectedElement : typedSelection.getElements()) {
            IIpsObject ipsObject = (IIpsObject)selectedElement.getAdapter(IIpsObject.class);
            if (ipsObject instanceof IProductCmpt) {
                timedIpsObjects.add((IProductCmpt)ipsObject);
            }
        }

        Wizard wizard = new NewGenerationWizard(timedIpsObjects);
        Dialog dialog = new WizardDialog(shell, wizard);
        dialog.open();
    }

}
