/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.fixdifferences;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.adapter.IIpsSrcFileWrapper;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IFixDifferencesToModelSupport;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.MessageList;

/**
 * 
 * @author Daniel Hohenberger
 */
public class OpenFixDifferencesToModelWizardAction extends ActionDelegate
        implements IWorkbenchWindowActionDelegate, IObjectActionDelegate {
    private IWorkbenchWindow window;
    // the last selection
    private ISelection selection;

    @Override
    public void dispose() {
        // nothing to do
    }

    @Override
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }

    @Override
    public void run(IAction action) {
        BusyIndicator.showWhile(Display.getCurrent(), () -> {
            // save dirty editors
            if (!IpsUIPlugin.getDefault().saveAllEditors()) {
                return;
            }

            Set<IFixDifferencesToModelSupport> ipsElementsToFix = findObjectsToFix();
            FixDifferencesToModelWizard wizard = new FixDifferencesToModelWizard(ipsElementsToFix);
            wizard.init(window.getWorkbench(), getCurrentSelection());
            WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
            dialog.open();
        });
    }

    private IStructuredSelection getCurrentSelection() {
        if (window != null) {
            ISelection newSelection = window.getSelectionService().getSelection();
            if (newSelection instanceof IStructuredSelection) {
                return (IStructuredSelection)newSelection;
            }
            IWorkbenchPart part = window.getPartService().getActivePart();
            if (part instanceof IEditorPart) {
                IEditorInput input = ((IEditorPart)part).getEditorInput();
                if (input instanceof IFileEditorInput) {
                    return new StructuredSelection(((IFileEditorInput)input).getFile());
                }
            }
        }
        return null;
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection.isEmpty()) {
            action.setEnabled(false);
            return;
        }
        this.selection = selection;
    }

    private Set<IFixDifferencesToModelSupport> findObjectsToFix() {
        Set<IFixDifferencesToModelSupport> ipsElementsToFix = new HashSet<>();
        if (selection instanceof IStructuredSelection) {
            try {
                IStructuredSelection sel = (IStructuredSelection)selection;
                for (Iterator<?> iter = sel.iterator(); iter.hasNext();) {
                    Object selected = iter.next();
                    addElementToFix(ipsElementsToFix, selected);
                }
            } catch (CoreException e) {
                // don't disturb
                IpsPlugin.log(e);
            }
        }
        return ipsElementsToFix;
    }

    /* private */ void addElementToFix(Set<IFixDifferencesToModelSupport> ipsElementsToFix, Object selected)
            throws CoreRuntimeException {
        if (selected instanceof IJavaProject) {
            IIpsProject project = getIpsProject((IJavaProject)selected);
            addIpsElements(project, ipsElementsToFix);
        } else if (selected instanceof IIpsProject) {
            addIpsElements((IIpsProject)selected, ipsElementsToFix);
        } else if (selected instanceof IIpsPackageFragmentRoot) {
            addIpsElements((IIpsPackageFragmentRoot)selected, ipsElementsToFix);
        } else if (selected instanceof IIpsPackageFragment) {
            addIpsElements((IIpsPackageFragment)selected, ipsElementsToFix);
        } else if (selected instanceof IIpsSrcFile) {
            addElementToFix(ipsElementsToFix, ((IIpsSrcFile)selected).getIpsObject());
        } else if (selected instanceof IIpsSrcFileWrapper) {
            addElementToFix(ipsElementsToFix, ((IIpsSrcFileWrapper)selected).getWrappedIpsSrcFile().getIpsObject());
        } else if (selected instanceof IFixDifferencesToModelSupport) {
            IFixDifferencesToModelSupport ipsElementToFix = (IFixDifferencesToModelSupport)selected;
            addIpsElement(ipsElementToFix, ipsElementsToFix);
        } else if (selected instanceof IResource) {
            Object objToAdd = IIpsModel.get().getIpsElement((IResource)selected);
            addElementToFix(ipsElementsToFix, objToAdd);
        }
    }

    private IIpsProject getIpsProject(IJavaProject jProject) {
        return IIpsModel.get().getIpsProject(jProject.getProject());
    }

    private void addIpsElements(IIpsProject ipsProject, Set<IFixDifferencesToModelSupport> ipsElementsToFix)
            throws CoreRuntimeException {
        if (ipsProject == null) {
            return;
        }
        if (!ipsProject.exists()) {
            return;
        }
        IIpsPackageFragmentRoot[] roots = ipsProject.getIpsPackageFragmentRoots(false);
        for (IIpsPackageFragmentRoot root : roots) {
            if (root.isBasedOnSourceFolder()) {
                addElementToFix(ipsElementsToFix, root);
            }
        }
    }

    private void addIpsElements(IIpsPackageFragmentRoot element, Set<IFixDifferencesToModelSupport> ipsElementsToFix)
            throws CoreRuntimeException {
        if (element == null) {
            return;
        }
        if (!element.exists()) {
            return;
        }
        IIpsPackageFragment pack = element.getDefaultIpsPackageFragment();
        addElementToFix(ipsElementsToFix, pack);
    }

    private void addIpsElements(IIpsPackageFragment pack, Set<IFixDifferencesToModelSupport> ipsElementsToFix)
            throws CoreRuntimeException {
        IIpsSrcFile[] ipsSrcFiles = pack.getIpsSrcFiles();
        for (IIpsElement element : ipsSrcFiles) {
            addElementToFix(ipsElementsToFix, element);
        }
        IIpsPackageFragment[] childIpsPackageFragments = pack.getChildIpsPackageFragments();
        for (IIpsPackageFragment packageFragment : childIpsPackageFragments) {
            addElementToFix(ipsElementsToFix, packageFragment);
        }
    }

    private void addIpsElement(IFixDifferencesToModelSupport ipsElementToFix,
            Set<IFixDifferencesToModelSupport> ipsElementsToFix) throws CoreRuntimeException {
        IIpsProject ipsProject = ipsElementToFix.getIpsSrcFile().getIpsProject();
        MessageList msgListProperties = ipsProject.validate();
        if (msgListProperties.containsErrorMsg()) {
            return;
        }
        if (ipsElementToFix.containsDifferenceToModel(ipsProject)) {
            ipsElementsToFix.add(ipsElementToFix);
        }
    }

    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        window = targetPart.getSite().getWorkbenchWindow();
    }

}
