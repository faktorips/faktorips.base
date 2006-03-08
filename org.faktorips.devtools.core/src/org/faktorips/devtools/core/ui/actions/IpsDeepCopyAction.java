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
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.ProductStructureExplorer;
import org.faktorips.devtools.core.ui.wizards.deepcopy.DeepCopyWizard;

/**
 * Performs a deep copy (copy of all objects and all related objects of this one and all 
 * related of the related ones and so on).
 * 
 * @author Thorsten Guenther
 */
public class IpsDeepCopyAction extends IpsAction {

	private Shell shell;
	
	public IpsDeepCopyAction(Shell shell, ISelectionProvider selectionProvider) {
		super(selectionProvider);
		this.shell = shell;
		setText(Messages.IpsDeepCopyAction_name);
	}

	/** 
	 * {@inheritDoc}
	 */
	public void run(IStructuredSelection selection) {
		Object selected  = selection.getFirstElement();
		if (selected instanceof IProductCmpt) {
			IProductCmpt root = (IProductCmpt)selected;
			DeepCopyWizard dcw = new DeepCopyWizard(root);
			WizardDialog wd = new WizardDialog(shell, dcw);
			wd.open();
			if (wd.getReturnCode() == WizardDialog.OK) {
				
				try {
					IViewReference[] views = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
					IViewPart pe = null;
					for (int i = 0; i < views.length; i++) {
						if (views[i].getId().equals(ProductStructureExplorer.EXTENSION_ID)) {
							pe = views[i].getView(true);
							break;
						}
					}
					
					if (pe == null) {
						pe = IpsPlugin.getDefault().getWorkbench().getViewRegistry().find(ProductStructureExplorer.EXTENSION_ID).createView();
					}
					
					if (pe == null) {
						return;
					}
					
					((ProductStructureExplorer)pe).showStructure(dcw.getCopiedRoot());
					
				} catch (CoreException e) {
					IpsPlugin.log(e);
				}
			}
		}
	}
}
