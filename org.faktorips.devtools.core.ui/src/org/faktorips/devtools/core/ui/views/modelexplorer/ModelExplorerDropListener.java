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

package org.faktorips.devtools.core.ui.views.modelexplorer;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.refactor.MoveOperation;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.views.IpsElementDropListener;

public class ModelExplorerDropListener extends IpsElementDropListener {
	
    private class ModifyOperation extends WorkspaceModifyOperation {
        private MoveOperation move;
        
        public ModifyOperation(MoveOperation toExecute) {
            super();
            this.move = toExecute;
        }
        
        protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
            move.run(monitor);
        }       
    }
    
	public ModelExplorerDropListener(){}
	/**
	 * {@inheritDoc}
	 */
	public void dragEnter(DropTargetEvent event) {
        event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
	}
    
    /**
     * Denies drop operation if one of the following rules apply: 
     * <ul>
     * <li>A source (object to be moved) is an <code>IIpsProject</code></li>
     * <li>The target is of type <code>IIpsObject</code>, <code>IIpsObjectPart</code>
     * or <code>IResource</code></li>
     * <li>The target is at the same time a source.</li>
     * </ul>
     * Allows drop otherwise.
     * {@inheritDoc}
     */
    public void dragOver(DropTargetEvent event) {
        if(event.item==null){
            event.detail = DND.DROP_NONE;
            return;
        }
        Object target= event.item.getData();
        Object[] sources = getTransferedElements(event.currentDataType);
        if(MoveOperation.canMove(sources, target)){
            event.detail = DND.DROP_MOVE;
        }
        else{
            event.detail = DND.DROP_NONE;
        }
        event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
    }

    /**
	 * {@inheritDoc}
	 */
	public void drop(DropTargetEvent event) {
		if (!FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
			return;
		}
		try {
            MoveOperation moveOp = null; 
            Object target = getTarget(event);
            Object[] sources = getTransferedElements(event.currentDataType);
            if (target instanceof IIpsPackageFragment) {
			    moveOp = new MoveOperation(sources, (IIpsPackageFragment)target);
			} else if (target instanceof IContainer){
                moveOp = new MoveOperation(((IContainer)target).getProject(), sources, ((IResource)target).getLocation().toOSString());
            }
            
            if (moveOp == null){
                return;
            }
            
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(event.display.getActiveShell());
            dialog.run(true, false, new ModifyOperation(moveOp));
            // run the operation with fork=true to ensure UI responsiveness 
		} catch (CoreException e) {
			IStatus status = e.getStatus();
			if (status instanceof IpsStatus) {
				MessageDialog.openError(event.display.getActiveShell(), Messages.ModelExplorer_errorTitle, ((IpsStatus)status).getMessage());
			}
			else {
				IpsPlugin.log(e);
			}
		} catch (InvocationTargetException e) {
			IpsPlugin.log(e);
		} catch (InterruptedException e) {
			IpsPlugin.log(e);
		}
	}
    
	private Object getTarget(DropTargetEvent event) throws CoreException {
		if(event.item == null){
			return null;
		}
		Object dropTarget = event.item.getData();
        if (dropTarget instanceof IIpsPackageFragmentRoot) {
            return ((IIpsPackageFragmentRoot)dropTarget).getDefaultIpsPackageFragment();
        } else if (dropTarget instanceof IIpsProject){
            return ((IIpsProject)dropTarget).getProject();
        }
		return dropTarget;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void dropAccept(DropTargetEvent event) {
		// nothing to do
	}
}
