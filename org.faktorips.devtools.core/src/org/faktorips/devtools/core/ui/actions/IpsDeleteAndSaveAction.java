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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.pctype.Attribute;
import org.faktorips.devtools.core.model.IIpsObjectPart;

/**
 * DeleteAction for ViewParts that saves IpsObjects after deleting 
 * their IpsObjectParts (e.g. Attributes). 
 * @see org.faktorips.devtools.core.ui.actions.IpsDeleteAction
 * @author Stefan Widmaier
 */
public class IpsDeleteAndSaveAction extends Action{

	private ISelectionProvider selectionProvider;
	
	private IpsDeleteAction deleteActionDelegate;
	
	public IpsDeleteAndSaveAction(ISelectionProvider selProv){
		selectionProvider= selProv;
		deleteActionDelegate= new IpsDeleteAction(selProv);
	}
	
	public void runWithEvent(Event event){
		ISelection selection= selectionProvider.getSelection();
		if(selection instanceof StructuredSelection){
			Object[] deletedItems= ((StructuredSelection)selection).toArray();
			deleteActionDelegate.runWithEvent(event);
			saveChanges(deletedItems);
		}
	}
	
	/**
	 * Saves all IpsObjects whose IpsObjectParts were deleted. 
	 * Directly deleted IpsObjects are "saved" automatically during the process
	 * of deleting the corresponding ressources.
	 * TODO ignore Attributes rule must be removed when refactoring IpsDeleteAction
	 * @see IpsDeleteAction
	 */
	private void saveChanges(Object[] deletedItems) {
		for(int i=0, size=deletedItems.length; i<size; i++){
			try {
				Object del= deletedItems[i];
				if(del instanceof IIpsObjectPart){
            		// IpsDeleteAction ignores Attributes, thus their IpsObjects must not be saved.
            		if(!(del instanceof Attribute)){
    					((IIpsObjectPart)del).getIpsObject().getIpsSrcFile().save(true, null);
            		}	
				}
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
		}
	}
}
