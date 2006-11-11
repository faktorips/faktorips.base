/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.ActionDelegate;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.CreateIpsArchiveOperation;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsProject;

/**
 * Action delegate to create an ips archive.
 * 
 * @see org.faktorips.devtools.core.model.IIpsArchive
 * 
 * @author Jan Ortmann
 */
public class CreateIpsArchiveAction extends ActionDelegate {

    private IIpsProject ipsProject;
    
    public CreateIpsArchiveAction() {
        super();
    }

    public void run(IAction action) {
        if (ipsProject==null) {
            return;
        }
        try {
            IFile archive = ipsProject.getProject().getFile("test.ipsar");
            CreateIpsArchiveOperation op = new CreateIpsArchiveOperation(ipsProject.getIpsPackageFragmentRoots(), archive);
            ResourcesPlugin.getWorkspace().run(op, null);
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void selectionChanged(IAction action, ISelection selection) {
        if (!(selection instanceof IStructuredSelection)) {
            ipsProject = null;
            return;
        }
        IIpsElement ipsEl = null;
        Object firstEl = ((IStructuredSelection)selection).getFirstElement();
        if (firstEl instanceof IIpsElement) {
            ipsEl = ((IIpsElement)firstEl);
        }
        if (firstEl instanceof IResource) {
            IResource res = (IResource)firstEl;
            ipsEl = IpsPlugin.getDefault().getIpsModel().getIpsElement(res);
        }
        if (ipsEl==null) {
            ipsProject = null;
        } else {
            ipsProject = ipsEl.getIpsProject();
        }
    }

    
}
