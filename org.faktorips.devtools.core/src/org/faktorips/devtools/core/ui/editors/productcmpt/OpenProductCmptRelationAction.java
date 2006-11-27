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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IIpsSrcFileMemento;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.ui.actions.IpsAction;

/**
 * Opens the wizard to create a new product component relation.
 * 
 * @author Jan Ortmann
 */
public class OpenProductCmptRelationAction extends IpsAction {

	private RelationsSection parent;

	public OpenProductCmptRelationAction(
            RelationsSection parent, 
            ISelectionProvider selectionProvider) {
		super(selectionProvider);
		this.parent = parent;
		setText("Properties");
        
		selectionProvider.addSelectionChangedListener(new ISelectionChangedListener() {
		
			public void selectionChanged(SelectionChangedEvent event) {
				Object selected = ((IStructuredSelection)event.getSelection()).getFirstElement();
				setEnabled(selected instanceof IProductCmptRelation);
			}
		
		});
		
	}

	/** 
	 * {@inheritDoc}
	 */
	public void run(IStructuredSelection selection) {
		Object selected = selection.getFirstElement();
		if (selected instanceof IProductCmptRelation) {
            IProductCmptRelation relation = (IProductCmptRelation)selected;
            IIpsSrcFile file = relation.getIpsObject().getIpsSrcFile();
            try {
                IIpsSrcFileMemento memento = file.newMemento();
                RelationEditDialog dialog = new RelationEditDialog(relation, parent.getShell());
                int rc = dialog.open();
                if (rc == Dialog.CANCEL) {
                    file.setMemento(memento);
                } else if (rc == Dialog.OK){
                    parent.refresh();
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
		}
	}
	
}
