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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsSrcFile;

/**
 * Opens a selected product component in an editor.
 * 
 * @author Thorsten Guenther
 * @author Stefan Widmaier
 */
public class OpenEditorAction extends IpsAction {

    public OpenEditorAction(ISelectionProvider selectionProvider) {
        super(selectionProvider);
        super.setText(Messages.OpenEditorAction_name);
        super.setDescription(Messages.OpenEditorAction_description);
        super.setToolTipText(Messages.OpenEditorAction_tooltip);
    }
    
    public void run(IStructuredSelection selection) {
        IFile fileToEdit= null;
		
		IIpsSrcFile srcFile = getIpsSrcFileForSelection(selection);
		if (srcFile != null) {
			fileToEdit = srcFile.getCorrespondingFile();
		}else{
			Object selectedObject= selection.getFirstElement();
			if(selectedObject instanceof IFile){
				fileToEdit= (IFile) selectedObject;
			}
		}
		openEditorForFile(fileToEdit);
    }
    
    protected void openEditorForFile(IFile fileToEdit){
        if(fileToEdit==null){
        	return;
        }
    	try {
	        IWorkbench workbench= IpsPlugin.getDefault().getWorkbench();
	        IFileEditorInput editorInput = new FileEditorInput(fileToEdit);
	        IEditorDescriptor editor = workbench.getEditorRegistry().getDefaultEditor(fileToEdit.getName());
	        if(editor!=null & editorInput!=null){
	        	// Open Editor for registered filetype
				workbench.getActiveWorkbenchWindow().getActivePage().openEditor(editorInput, editor.getId());
	        }else{
	        	// Let IDE guess filtype and open the corresponding editor
	        	IDE.openEditor(workbench.getActiveWorkbenchWindow().getActivePage(), fileToEdit, true, true);
	        }
        } catch (PartInitException e) {
            IpsPlugin.logAndShowErrorDialog(e);
		}
    }
}
