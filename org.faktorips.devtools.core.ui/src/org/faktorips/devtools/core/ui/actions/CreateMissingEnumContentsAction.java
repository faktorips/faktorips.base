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

import java.util.Iterator;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.enumcontent.CreateMissingEnumContentsWizard;

/**
 * This action opens up a wizard that enables the user to create missing {@link IEnumContent}.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.4
 */
public class CreateMissingEnumContentsAction extends IpsAction {

    private IWorkbenchWindow workbenchWindow;

    public CreateMissingEnumContentsAction(ISelectionProvider selectionProvider, IWorkbenchWindow workbenchWindow) {
        super(selectionProvider);
        this.workbenchWindow = workbenchWindow;
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("CreateMissingEnumContents.gif")); //$NON-NLS-1$
        setText(Messages.CreateMissingEnumContentsAction_text);
    }

    @Override
    public void run(IStructuredSelection selection) {
        if (selection.isEmpty()) {
            return;
        }

        IIpsElement preselectedIpsElement = null;
        IStructuredSelection sel = selection;
        for (Iterator<?> iter = sel.iterator(); iter.hasNext();) {
            Object selected = iter.next();
            if (selected instanceof IJavaProject) {
                preselectedIpsElement = IpsPlugin.getDefault().getIpsModel().getIpsProject(
                        ((IJavaProject)selected).getProject());
                break;
            } else if (selected instanceof IIpsElement) {
                preselectedIpsElement = (IIpsElement)selected;
                break;
            }
        }
        CreateMissingEnumContentsWizard wizard = new CreateMissingEnumContentsWizard(preselectedIpsElement);
        wizard.open(workbenchWindow.getShell());
    }

}
