/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import java.util.ArrayList;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.ITimedIpsObject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewGenerationWizard;

/**
 * Presents a wizard to the user that allows him to create a new {@link IIpsObjectGeneration
 * generation} for selected {@link IProductCmpt product components}.
 */
public class CreateNewGenerationAction extends IpsAction {

    private final Shell shell;

    public CreateNewGenerationAction(Shell shell, ISelectionProvider selectionProvider) {
        super(selectionProvider);
        this.shell = shell;
        setText(Messages.CreateNewGenerationAction_title);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("CreateNewGenerationWizard.gif")); //$NON-NLS-1$
    }

    @Override
    protected boolean computeEnabledProperty(IStructuredSelection selection) {
        TypedSelection<IAdaptable> typedSelection = TypedSelection.createAnyCount(IAdaptable.class, selection);
        if (!typedSelection.isValid()) {
            return false;
        }

        for (IAdaptable selectedElement : typedSelection.getElements()) {
            if (!(selectedElement instanceof IProductCmpt)) {
                // If the selection contains any other type of elements, it cannot be started
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

        TypedSelection<ITimedIpsObject> typedSelection = TypedSelection
                .createAnyCount(ITimedIpsObject.class, selection);
        Wizard wizard = new NewGenerationWizard(new ArrayList<ITimedIpsObject>(typedSelection.getElements()));
        Dialog dialog = new WizardDialog(shell, wizard);
        dialog.open();
    }

}
