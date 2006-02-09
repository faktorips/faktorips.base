package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.ProductStructureLabelProvider;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.ProductStructureContentProvider;

public class IpsDeepCopyAction extends IpsAction {

	private Shell shell;
	
	public IpsDeepCopyAction(Shell shell, ISelectionProvider selectionProvider) {
		super(selectionProvider);
		this.shell = shell;
		setText("Deep Copy");
	}

	public void run(IStructuredSelection selection) {
		Object selected  = selection.getFirstElement();
		if (selected instanceof IProductCmpt) {
			IProductCmpt root = (IProductCmpt)selected;
			CheckedTreeSelectionDialog dialog = new CheckedTreeSelectionDialog(shell, new ProductStructureLabelProvider(), new ProductStructureContentProvider(true));
			dialog.setInput(root);
			dialog.setContainerMode(true);
			dialog.setBlockOnOpen(true);
			dialog.open();
			Object[] result = dialog.getResult();
			if (result != null && result.length > 0) {
				System.out.println("Doing Deep Copy... finished");
			}
		}
	}	
}
