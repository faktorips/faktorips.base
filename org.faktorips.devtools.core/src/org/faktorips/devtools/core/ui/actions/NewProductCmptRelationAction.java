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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.devtools.core.ui.editors.productcmpt.RelationEditDialog;
import org.faktorips.devtools.core.ui.editors.productcmpt.RelationsSection;

/**
 * Opens the wizard to create a new product component relation.
 * 
 * @author Thorsten Guenther
 */
public class NewProductCmptRelationAction extends IpsAction {

	private Shell shell;
	private RelationsSection parent;
	
	public NewProductCmptRelationAction(Shell shell, ISelectionProvider selectionProvider, RelationsSection parent) {
		super(selectionProvider);
		this.shell = shell;
		this.parent = parent;
		setText(Messages.NewProductCmptRelationAction_name);
		
		selectionProvider.addSelectionChangedListener(new ISelectionChangedListener() {
		
			public void selectionChanged(SelectionChangedEvent event) {
				Object selected = ((IStructuredSelection)event.getSelection()).getFirstElement();
				setEnabled(selected instanceof IProductCmptTypeRelation);
			}
		
		});
	}

	/** 
	 * {@inheritDoc}
	 */
	public void run(IStructuredSelection selection) {
		Object selected = selection.getFirstElement();
		if (selected instanceof IProductCmptTypeRelation) {
			parent.setSyncpoint();
			IProductCmptRelation relation = parent.newRelation((IProductCmptTypeRelation)selected);
			RelationEditDialog dialog = new RelationEditDialog(relation, shell);
			if (dialog.open() == Dialog.CANCEL) {
				parent.reset();
			}
		}
	}
	
}
