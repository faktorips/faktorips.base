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

package org.faktorips.devtools.core.ui.views.modelexplorer;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringUI;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.refactor.MoveOperation;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.refactor.IIpsMoveProcessor;
import org.faktorips.devtools.core.ui.views.IpsElementDropListener;

public class ModelExplorerDropListener extends IpsElementDropListener {

    public ModelExplorerDropListener() {
        // nothing to do
    }

    public void dragEnter(DropTargetEvent event) {
        event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Denies drop operation if one of the following rules apply:
     * <ul>
     * <li>A source (object to be moved) is an <code>IIpsProject</code></li>
     * <li>The target is of type <code>IIpsObject</code>, <code>IIpsObjectPart</code> or
     * <code>IResource</code></li>
     * <li>The target is at the same time a source.</li>
     * </ul>
     * Allows drop otherwise.
     */
    @Override
    public void dragOver(DropTargetEvent event) {
        if (event.item == null) {
            event.detail = DND.DROP_NONE;
            return;
        }
        Object target = event.item.getData();
        Object[] sources = getTransferedElements(event.currentDataType);
        // In Linux sources is null while drag action.
        if (sources == null || MoveOperation.canMove(sources, target)) {
            event.detail = DND.DROP_MOVE;
        } else {
            event.detail = DND.DROP_NONE;
        }
        event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
    }

    public void drop(DropTargetEvent event) {
        if (!FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
            return;
        }
        try {
            Object target = getTarget(event);
            Object[] sources = getTransferedElements(event.currentDataType);

            // The new refactoring support goes from here.
            if (target instanceof IIpsPackageFragment) {
                boolean allIpsSrcFiles = true;
                for (Object source : sources) {
                    if (!(source instanceof IIpsSrcFile)) {
                        allIpsSrcFiles = false;
                        break;
                    }
                }
                if (allIpsSrcFiles) {
                    for (Object source : sources) {
                        IIpsSrcFile ipsSrcFile = (IIpsSrcFile)source;
                        IIpsObject ipsObject = ipsSrcFile.getIpsObject();
                        ProcessorBasedRefactoring moveRefactoring = ipsObject.getMoveRefactoring();
                        IIpsMoveProcessor moveProcessor = (IIpsMoveProcessor)moveRefactoring.getProcessor();
                        moveProcessor.setTargetIpsPackageFragment((IIpsPackageFragment)target);

                        final PerformRefactoringOperation workspaceOperation = new PerformRefactoringOperation(
                                moveRefactoring, CheckConditionsOperation.ALL_CONDITIONS);
                        ProgressMonitorDialog dialog = new ProgressMonitorDialog(event.display.getActiveShell());
                        dialog.run(true, false, new IRunnableWithProgress() {

                            public void run(IProgressMonitor monitor) throws InvocationTargetException,
                                    InterruptedException {
                                try {
                                    ResourcesPlugin.getWorkspace().run(workspaceOperation, monitor);
                                } catch (CoreException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });

                        // TODO CD: besser den Dialog an zentraler Stelle aufrufen und nicht hier
                        // manuell
                        final RefactoringStatus status = workspaceOperation.getConditionStatus();
                        if (status.hasWarning()) {
                            proceed(status, event.display);
                        }
                    }

                    return;
                }
            }

            // If the situation could not be handled by the new refactoring support the old code
            // is called.
            MoveOperation moveOp = null;
            if (target instanceof IIpsPackageFragment) {
                moveOp = new MoveOperation(sources, (IIpsPackageFragment)target);
            } else if (target instanceof IContainer) {
                moveOp = new MoveOperation(((IContainer)target).getProject(), sources, ((IResource)target)
                        .getLocation().toOSString());
            }

            if (moveOp == null) {
                return;
            }

            ProgressMonitorDialog dialog = new ProgressMonitorDialog(event.display.getActiveShell());
            // Run the operation with fork = true to ensure UI responsiveness.
            dialog.run(true, false, new ModifyOperation(moveOp));
        } catch (CoreException e) {
            IStatus status = e.getStatus();
            if (status instanceof IpsStatus) {
                MessageDialog.openError(event.display.getActiveShell(), Messages.ModelExplorer_errorTitle,
                        ((IpsStatus)status).getMessage());
            } else {
                IpsPlugin.log(e);
            }
        } catch (InvocationTargetException e) {
            IpsPlugin.log(e);
        } catch (InterruptedException e) {
            IpsPlugin.log(e);
        }
    }

    private boolean proceed(RefactoringStatus status, Display display) {
        final Dialog dialog = RefactoringUI.createRefactoringStatusDialog(status, display.getActiveShell(),
                "Refactoring Status", false); //$NON-NLS-1$
        final int[] result = new int[1];
        Runnable r = new Runnable() {
            public void run() {
                result[0] = dialog.open();
            }
        };
        display.syncExec(r);
        return result[0] == IDialogConstants.OK_ID;
    }

    private Object getTarget(DropTargetEvent event) {
        if (event.item == null) {
            return null;
        }
        Object dropTarget = event.item.getData();
        if (dropTarget instanceof IIpsPackageFragmentRoot) {
            return ((IIpsPackageFragmentRoot)dropTarget).getDefaultIpsPackageFragment();
        } else if (dropTarget instanceof IIpsProject) {
            return ((IIpsProject)dropTarget).getProject();
        }
        return dropTarget;
    }

    public void dropAccept(DropTargetEvent event) {
        // Nothing to do.
    }

    private class ModifyOperation extends WorkspaceModifyOperation {

        private MoveOperation move;

        public ModifyOperation(MoveOperation toExecute) {
            super();
            move = toExecute;
        }

        @Override
        protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
                InterruptedException {

            move.run(monitor);
        }

    }

}
