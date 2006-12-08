/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende:  Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  
 **************************************************************************************************/

package org.faktorips.devtools.core.ui.wizards.fixdifferences;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.FixDifferencesToModelSupport;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;

/**
 * 
 * @author Daniel Hohenberger
 */
public class OpenFixDifferencesToModelWizardAction extends ActionDelegate implements IWorkbenchWindowActionDelegate, IObjectActionDelegate {
    private IWorkbenchWindow window;
    private Set ipsElementsToFix = new HashSet();

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }

    /**
     * {@inheritDoc}
     */
    public void run(IAction action) {
        // check for open editors
        boolean dirtyeditor = false;
        if (PlatformUI.isWorkbenchRunning()) {
            IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
            for (int i = 0; i < windows.length && !dirtyeditor; i++) {
                IWorkbenchPage[] pages = windows[i].getPages();
                for (int j = 0; j < pages.length && !dirtyeditor; j++) {
                    IEditorReference[] refs = pages[j].getEditorReferences();
                    for (int k = 0; k < refs.length && !dirtyeditor; k++) {
                        dirtyeditor = refs[k].isDirty();
                    }
                }
            }
        }

        if (dirtyeditor) {
            MessageDialog
                    .openError(window.getShell(), "Fixing Differences not Possible",
                            "The differences can be fixed only if all open documents are saved. Please save all documents and try again.");
            return;
        }

        FixDifferencesToModelWizard wizard = new FixDifferencesToModelWizard(ipsElementsToFix);
        wizard.init(window.getWorkbench(), getCurrentSelection());
        WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
        dialog.open();
    }

    private IStructuredSelection getCurrentSelection() {
        if (window != null) {
            ISelection selection = window.getSelectionService().getSelection();
            if (selection instanceof IStructuredSelection) {
                return (IStructuredSelection)selection;
            }
        }
        IWorkbenchPart part = window.getPartService().getActivePart();
        if (part instanceof IEditorPart) {
            IEditorInput input = ((IEditorPart)part).getEditorInput();
            if (input instanceof IFileEditorInput) {
                return new StructuredSelection(((IFileEditorInput)input).getFile());
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection.isEmpty()) {
            action.setEnabled(false);
            return;
        }
        ipsElementsToFix = new HashSet();
        if (selection instanceof IStructuredSelection) {
            try {
                IStructuredSelection sel = (IStructuredSelection)selection;
                for (Iterator iter = sel.iterator(); iter.hasNext();) {
                    Object selected = iter.next();
                    if (selected instanceof IJavaProject) {
                        IIpsProject project = getIpsProject((IJavaProject)selected);
                        addIpsElement(project);
                    }
                    else if (selected instanceof IIpsProject) {
                        addIpsElement((IIpsProject)selected);
                    }
                    else if (selected instanceof IPackageFragmentRoot) {
                        IIpsPackageFragmentRoot root = getIpsProject(((IPackageFragmentRoot)selected).getJavaProject())
                                .findIpsPackageFragmentRoot(((IPackageFragmentRoot)selected).getElementName());
                        addIpsElement(root);
                    }
                    else if (selected instanceof IIpsPackageFragmentRoot) {
                        addIpsElement((IIpsPackageFragmentRoot)selected);
                    }
                    else if (selected instanceof IPackageFragment) {
                        IIpsProject project = getIpsProject(((IPackageFragment)selected).getJavaProject());
                        IIpsPackageFragmentRoot[] roots = project.getIpsPackageFragmentRoots();
                        for (int i = 0; i < roots.length; i++) {
                            IIpsPackageFragment pack = roots[i].getIpsPackageFragment(((IPackageFragment)selected)
                                    .getElementName());
                            addIpsElement(pack);
                        }
                    }
                    else if (selected instanceof IIpsPackageFragment) {
                        addIpsElement((IIpsPackageFragment)selected);
                    }
                }
            }
            catch (CoreException e) {
                // don't disturb
                IpsPlugin.log(e);
            }
            action.setEnabled(ipsElementsToFix.size() > 0);
        }
        else {
            action.setEnabled(false);
        }
    }

    private IIpsProject getIpsProject(IJavaProject jProject) {
        return IpsPlugin.getDefault().getIpsModel().getIpsProject(jProject.getProject());
    }

    private void addIpsElement(IIpsElement element) throws CoreException {
        if (element == null) {
            return;
        }
        if (!element.exists()) {
            return;
        }
        if (element instanceof IIpsProject) {
            IIpsPackageFragmentRoot[] roots = ((IIpsProject)element).getIpsPackageFragmentRoots();
            for (int i = 0; i < roots.length; i++) {
                IIpsPackageFragmentRoot root = roots[i];
                IIpsPackageFragment[] packs = root.getIpsPackageFragments();
                for (int j = 0; j < packs.length; j++) {
                    addIpsElements(packs[j]);
                }
            }
        }
        else if (element instanceof IIpsPackageFragmentRoot) {
            IIpsPackageFragment[] packs = ((IIpsPackageFragmentRoot)element).getIpsPackageFragments();
            for (int j = 0; j < packs.length; j++) {
                addIpsElements(packs[j]);
            }
        }
        else if (element instanceof IIpsPackageFragment) {
            addIpsElements((IIpsPackageFragment)element);
        }
    }

    private void addIpsElements(IIpsPackageFragment pack) throws CoreException {
        IIpsElement[] elements = pack.getChildren();
        for (int i = 0; i < elements.length; i++) {
            IIpsElement element = elements[i];
            if(element instanceof IIpsSrcFile){
                element = ((IIpsSrcFile)element).getIpsObject();
            }
            if(element instanceof FixDifferencesToModelSupport){
                FixDifferencesToModelSupport ipsElementToFix = (FixDifferencesToModelSupport)element;
                if(ipsElementToFix.containsDifferenceToModel()){
                    ipsElementsToFix.add(ipsElementToFix);
                }
            }else if (element instanceof IIpsPackageFragment){
                addIpsElements((IIpsPackageFragment)element);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        window = targetPart.getSite().getWorkbenchWindow();
    }

}
