package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.product.IProductCmpt;
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
		setText("Copy Product ...");
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
		}
	}
}
