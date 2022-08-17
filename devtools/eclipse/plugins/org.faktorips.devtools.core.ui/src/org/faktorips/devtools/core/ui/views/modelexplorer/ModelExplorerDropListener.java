/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelexplorer;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.refactor.MoveOperation;
import org.faktorips.devtools.core.internal.refactor.NonIPSMoveOperation;
import org.faktorips.devtools.core.refactor.IIpsCompositeMoveRefactoring;
import org.faktorips.devtools.core.ui.refactor.IpsRefactoringOperation;
import org.faktorips.devtools.core.ui.views.IpsElementDropListener;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsStatus;

public class ModelExplorerDropListener extends IpsElementDropListener {

    public ModelExplorerDropListener() {
        // nothing to do
    }

    @Override
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

    @Override
    public void drop(DropTargetEvent event) {
        if (!FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
            return;
        }

        Shell shell = event.display.getActiveShell();
        try {
            Object target = getTarget(event);
            Object[] sources = getTransferedElements(event.currentDataType);
            if (sources == null) {
                return;
            }
            // The new refactoring support goes from here
            if (target instanceof IIpsPackageFragment) {
                Set<IIpsElement> ipsElements = new LinkedHashSet<>(sources.length);
                for (Object source : sources) {
                    if ((source instanceof IIpsSrcFile)) {
                        IIpsSrcFile ipsSourceFile = (IIpsSrcFile)source;
                        if (ipsSourceFile.isDirty()) {
                            return;
                        }
                        ipsElements.add(ipsSourceFile.getIpsObject());
                    } else if (source instanceof IIpsPackageFragment) {
                        ipsElements.add((IIpsPackageFragment)source);
                    }
                }

                if (!ipsElements.isEmpty()) {
                    IIpsCompositeMoveRefactoring ipsCompositeMoveRefactoring = IpsPlugin.getIpsRefactoringFactory()
                            .createCompositeMoveRefactoring(ipsElements);
                    ipsCompositeMoveRefactoring.setTargetIpsPackageFragment((IIpsPackageFragment)target);
                    IpsRefactoringOperation refactoringOperation = new IpsRefactoringOperation(
                            ipsCompositeMoveRefactoring, shell);
                    refactoringOperation.runDirectExecution();
                    return;
                }
            }

            moveNonIPSObjects(sources, target, shell);
        } catch (IpsException e) {
            logCoreException(shell, e);
        }
    }

    private void logCoreException(Shell shell, IpsException e) {
        IStatus status = e.getStatus();
        if (status instanceof IpsStatus) {
            MessageDialog.openError(shell, Messages.ModelExplorer_errorTitle, ((IpsStatus)status).getMessage());
        } else {
            IpsPlugin.log(e);
        }
    }

    private void moveNonIPSObjects(Object[] sources, Object target, Shell shell) {
        try {
            NonIPSMoveOperation moveOp = null;
            if (target instanceof IIpsPackageFragment) {
                moveOp = new NonIPSMoveOperation(sources, (IIpsPackageFragment)target);
            } else if (target instanceof IContainer) {
                moveOp = new NonIPSMoveOperation(((IContainer)target).getProject(), sources, ((IResource)target)
                        .getLocation().toOSString());
            }

            if (moveOp == null) {
                return;
            }

            ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
            // Run the operation with fork = true to ensure UI responsiveness.
            dialog.run(true, false, new ModifyOperation(moveOp));
        } catch (InvocationTargetException | InterruptedException e) {
            IpsPlugin.log(e);
        }
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

    @Override
    public void dropAccept(DropTargetEvent event) {
        // Nothing to do.
    }

    @Override
    public int getSupportedOperations() {
        return DND.DROP_MOVE;
    }

    private static class ModifyOperation extends WorkspaceModifyOperation {

        private NonIPSMoveOperation move;

        public ModifyOperation(NonIPSMoveOperation toExecute) {
            super();
            move = toExecute;
        }

        @Override
        protected void execute(final IProgressMonitor monitor) throws IpsException, InterruptedException {
            try {
                move.run(monitor);
            } catch (InvocationTargetException e) {
                IpsPlugin.log(e);
            }
        }

    }

}
