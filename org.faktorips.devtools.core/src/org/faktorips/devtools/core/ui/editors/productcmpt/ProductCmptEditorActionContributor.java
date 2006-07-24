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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.faktorips.devtools.core.ui.actions.IpsDeleteAction;

/**
 * The ProductCmptEditorActionContributor distinguishes between multiple instances 
 * of ProductCmptEditor and organizes actions accordingly.
 * The contributor makes sure retargetable actions for the ProductCmptEditor always
 * use the currently active editor as a selectionprovider. </b>
 * Note: The contributor uses the selectionprovider returned by the editorsite.
 * Which GUI element actually provides the selection is up to the Editor.
 * In the future the selection should be retrieved unsing the SelectionService.
 * @see org.faktorips.devtools.core.ui.actions.IpsDeleteAction
 * @author Stefan Widmaier
 */
public class ProductCmptEditorActionContributor extends
		EditorActionBarContributor {
	
	public ProductCmptEditorActionContributor() {
		super();
	}
	
	public void init(IActionBars bars, IWorkbenchPage page) {
		super.init(bars, page);
	}
	
	/**
	 * Communicates the new SelectionProvider (of the currently active editor)
	 * to all actions. <b/>
	 * This method is automatically called by the workbench every time
	 * a ProductCmptEditor is activated.
	 */
	public void setActiveEditor(IEditorPart targetEditor) {
		super.setActiveEditor(targetEditor);
		if(!(targetEditor instanceof ProductCmptEditor)){
			return;
		}
		ProductCmptEditor editor= (ProductCmptEditor) targetEditor;
		IpsDeleteAction deleteAction= new IpsDeleteAction(editor.getEditorSite().getSelectionProvider());
		editor.getEditorSite().getActionBars().setGlobalActionHandler(ActionFactory.DELETE.getId(), deleteAction);
	}
}
