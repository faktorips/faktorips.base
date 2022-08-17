/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewGenerationWizard;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsobject.ITimedIpsObject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;

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
        List<ITimedIpsObject> timedIpsObjects = getTimedIpsObject(selection);
        if (timedIpsObjects.isEmpty()) {
            return false;
        }
        for (ITimedIpsObject timedIpsObject : timedIpsObjects) {
            if (!timedIpsObject.allowGenerations()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void run(IStructuredSelection selection) {
        if (!isEnabled()) {
            return;
        }

        List<ITimedIpsObject> timedIpsObjects = getTimedIpsObject(selection);

        Wizard wizard = new NewGenerationWizard(timedIpsObjects);
        Dialog dialog = new WizardDialog(shell, wizard);
        dialog.open();
    }

    private List<ITimedIpsObject> getTimedIpsObject(IStructuredSelection selection) {
        TypedSelection<IAdaptable> typedSelection = TypedSelection.createAnyCount(IAdaptable.class, selection);
        if (typedSelection.isValid()) {
            List<ITimedIpsObject> timedIpsObjects = collectTimedIpsObjects(typedSelection);
            if (selectionContainsInvalidType(timedIpsObjects, typedSelection)) {
                timedIpsObjects.clear();
            }
            return timedIpsObjects;
        } else {
            return new ArrayList<>(0);
        }
    }

    private List<ITimedIpsObject> collectTimedIpsObjects(TypedSelection<IAdaptable> typedSelection) {
        List<ITimedIpsObject> timedIpsObjects = new ArrayList<>(typedSelection.getElementCount());
        for (IAdaptable selectedElement : typedSelection.getElements()) {
            IIpsObject ipsObject = selectedElement.getAdapter(IIpsObject.class);
            if (ipsObject instanceof ITimedIpsObject) {
                timedIpsObjects.add((ITimedIpsObject)ipsObject);
            }
        }
        return timedIpsObjects;
    }

    private boolean selectionContainsInvalidType(List<ITimedIpsObject> timedIpsObjects,
            TypedSelection<IAdaptable> typedSelection) {
        return timedIpsObjects.size() != typedSelection.getElementCount();
    }

}
