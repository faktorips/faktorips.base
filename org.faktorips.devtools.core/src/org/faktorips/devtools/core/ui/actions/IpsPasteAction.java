/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ResourceTransfer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPartState;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;

/**
 * Action to paste IpsElements or resources.
 * 
 * @author Thorsten Guenther
 */
public class IpsPasteAction extends IpsAction {

	/**
	 * The clipboard used to transfer the data
	 */
    private Clipboard clipboard;
    
    /**
     * The shell for this session
     */
    private Shell shell;
    
    /**
     * Creates a new action to paste <code>IIpsElement</code>s or resources.
     * 
     * @param selectionProvider The provider for the selection to get the target from.
     * @param shell The shell for this session.
     */
    public IpsPasteAction(ISelectionProvider selectionProvider, Shell shell) {
        super(selectionProvider);
        clipboard = new Clipboard(shell.getDisplay());
        this.shell = shell;
    }

    /**
     * {@inheritDoc}
     */
    public void run(IStructuredSelection selection) {
        Object selected = selection.getFirstElement();
        if (selected instanceof IIpsObjectPartContainer) {
        	paste((IIpsObjectPartContainer)selected);
        }
        else if (selected instanceof IIpsProject) {
        	try {
				paste(((IIpsProject)selected).getIpsPackageFragmentRoots()[0].getIpsDefaultPackageFragment());
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
        }
        else if (selected instanceof IIpsPackageFragmentRoot) {
        	paste(((IIpsPackageFragmentRoot)selected).getIpsDefaultPackageFragment());
        }
        else if (selected instanceof IIpsPackageFragment) {
        	paste((IIpsPackageFragment)selected);
        }
    }

    /**
     * Try to paste an <code>IIpsObject</code> to an <code>IIpsObjectPartContainer</code>. If 
     * it is not possible because the stored data does not support this (e.g. is a resource
     * and not a string) paste(IIpsPackageFragement) is called.
     *  
     * @param parent The parent to paste to.
     */
    private void paste(IIpsObjectPartContainer parent) {

        String stored = (String)clipboard.getContents(TextTransfer.getInstance());
        if (stored == null) {
        	IIpsElement pack = parent.getParent();
        	while (pack != null && !(pack instanceof IIpsPackageFragment)) {
        		pack = pack.getParent();
        	}
        	if (pack != null) {
        		paste((IIpsPackageFragment)pack);
        	}
        }
        else {        
        	try {
        		IpsObjectPartState state = new IpsObjectPartState(stored);
        		state.newPart(parent);
        	} catch (RuntimeException e) {
        		IpsPlugin.log(e);
        	}
        }
    }

    /**
     * Try to paste the <code>IResource</code> stored on the clipboard to the given parent.
     * 
     * @param parent
     */
    private void paste(IIpsPackageFragment parent) {
    	Object stored = clipboard.getContents(ResourceTransfer.getInstance());
    	if (stored instanceof IResource[]) {
			IResource[] res = (IResource[]) stored;
			for (int i = 0; i < res.length; i++) {
				try {
					IPath targetPath = ((IIpsElement)parent).getCorrespondingResource().getFullPath();	
					copy(targetPath, res[i]);
				} catch (CoreException e) {
					IpsPlugin.logAndShowErrorDialog(e);
				}
			}
		}   	
    }
    
    /**
     * Copy the given resource to the given target path.

     * @throws CoreException If copy failed.
     */
    private void copy(IPath targetPath, IResource resource) throws CoreException {
    	if (targetPath == null) {
    		return;
    	}

    	String name = resource.getName();
    	String extension = ""; //$NON-NLS-1$
		String suggestedName = name;

    	if (resource.getType() == IResource.FOLDER) {
    		if (((IFolder)resource).getFullPath().equals(targetPath)) {
    			MessageDialog.openError(shell, Messages.IpsPasteAction_errorTitle, Messages.IpsPasteAction_msgSrcAndTargetSame);
    			return;
    		}
    	}
    	else {
    		int index = name.lastIndexOf("."); //$NON-NLS-1$
    		if (index == -1) {
    			suggestedName = name;
    		}
    		else {
    			suggestedName = name.substring(0, index);
    			extension = name.substring(index);
    		}    		
    	}

    	String nameWithoutExtension = suggestedName;
    	Validator validator = new Validator(targetPath, resource, extension);
    	
    	int doCopy = InputDialog.OK;
    	if (validator.isValid(suggestedName) != null) {
    		for (int count = 0; validator.isValid(suggestedName) != null; count++) {
    			if (count == 0) {
    				suggestedName = Messages.IpsPasteAction_suggestedNamePrefixSimple + nameWithoutExtension;
    			}
    			else {
    				suggestedName = NLS.bind(Messages.IpsPasteAction_suggestedNamePrefixComplex, new Integer(count), nameWithoutExtension); 
    			}
    		}
    		    		
    		InputDialog dialog = new InputDialog(shell, Messages.IpsPasteAction_titleNamingConflict, NLS.bind(Messages.IpsPasteAction_msgNamingConflict, nameWithoutExtension), suggestedName, validator);
    		dialog.setBlockOnOpen(true);
    		doCopy = dialog.open();
    		nameWithoutExtension = dialog.getValue();
    	}
    	if (doCopy == InputDialog.OK) {
    		resource.copy(targetPath.append(nameWithoutExtension + extension), true, null);
    	}
    }

    /**
     * Validator for new resource name.
     * 
     * @author Thorsten Guenther
     */
    private class Validator implements IInputValidator {
    	IPath root;
    	IResource resource;
    	String extension;
    	
    	public Validator(IPath root, IResource resource, String extension) {
    		this.root = root;
    		this.resource = resource;
    		this.extension = extension;
    	}

    	/**
    	 * {@inheritDoc}
    	 */
		public String isValid(String newText) {
			IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
			IResource test = null;
			if (resource.getType() == IResource.FILE) {
				test = wsRoot.getFile(root.append(newText + extension));
			}
			else if (resource.getType() == IResource.FOLDER) {
				test = wsRoot.getFolder(root.append(newText));
			}
			if (test != null && test.exists()) {
				return newText + extension + Messages.IpsPasteAction_msgFileAllreadyExists;
			}
			
			return null;
		}
    	
    }
}
