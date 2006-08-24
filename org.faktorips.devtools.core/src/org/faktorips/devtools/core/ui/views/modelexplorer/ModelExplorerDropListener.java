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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.refactor.MoveOperation;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.ui.views.IpsElementDropListener;

public class ModelExplorerDropListener extends IpsElementDropListener {
	
	public ModelExplorerDropListener(){
		
	}
	
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
        IIpsElement[] sources = getTransferedElements(event.currentDataType);
        if(isTargetDenied(target) || sourcesDenied(sources) || isMoveDenied(sources, target)){
            event.detail = DND.DROP_NONE;
        }
        else{
            event.detail = DND.DROP_MOVE;
        }
        event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
    }

    /** 
     * Returns true if the given IIpsElement array contains at least one IIpsProject, fals otherwise.
     */
	private boolean sourcesDenied(IIpsElement[] sources) {
        boolean denied= false;
        for (int i = 0; i < sources.length; i++) {
            if(sources[i] instanceof IIpsProject){
                denied= true;
              System.out.println("Element denied: "+sources[i]);
            }
        }
        return denied;
    }

    /**
     * If target object is of type <code>IIpsObject</code>, <code>IIpsObjectPart</code>
     * or <code>IResource</code> true is returned. For all other types returns false.
     */
    private boolean isTargetDenied(Object target) {
        return target instanceof IIpsObject || target instanceof IIpsObjectPart || target instanceof IResource;
    }
    
    /**
     * Returns true for unallowed move operations.
     * <p>
     * The current implementation returns true if the given target is element of the given array of sources, 
     * e.g. moving a packagefragment in itself.
     */
    private boolean isMoveDenied(IIpsElement[] sources, Object target) {
        boolean denied= false;
        for (int i = 0; i < sources.length; i++) {
            if(sources[i].equals(target)){
                denied= true;
            }
        }
        return denied;
    }

    /**
	 * {@inheritDoc}
	 */
	public void drop(DropTargetEvent event) {
		if (!FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
			return;
		}
		try {
			IIpsPackageFragment target = getTarget(event);
			if (target == null) {
				return;
			}
			IIpsElement[] sources = getTransferedElements(event.currentDataType);
			MoveOperation moveOp = new MoveOperation(sources, target);
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(event.display.getActiveShell());
			dialog.run(false, false, moveOp);
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

	private IIpsPackageFragment getTarget(DropTargetEvent event) throws CoreException {
		if(event.item == null){
			return null;
		}
		Object dropTarget = event.item.getData();
		IIpsPackageFragment target = null; 
		if (dropTarget instanceof IIpsPackageFragment) {
			target = (IIpsPackageFragment)dropTarget;
		}
        else if (dropTarget instanceof IIpsPackageFragmentRoot) {
            target = ((IIpsPackageFragmentRoot)dropTarget).getIpsDefaultPackageFragment();
        }
        else if (dropTarget instanceof IIpsProject) {
            target = ((IIpsProject)dropTarget).getIpsPackageFragmentRoots()[0].getIpsDefaultPackageFragment();
        }
		return target;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void dropAccept(DropTargetEvent event) {
		// nothing to do
	}
}
