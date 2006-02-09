package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableTreeViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.Node;
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
			DeepCopyDialog dialog = new DeepCopyDialog(shell);
			dialog.setInput(root);
			dialog.open();
//			CheckedTreeSelectionDialog dialog = new CheckedTreeSelectionDialog(shell, new ProductStructureLabelProvider(), new ProductStructureContentProvider(true));
//			dialog.setInput(root);
//			dialog.setContainerMode(true);
//			dialog.setBlockOnOpen(true);
//			dialog.open();
//			Object[] result = dialog.getResult();
//			if (result != null) {
//				copy(result);
//			}
		}
	}
	
	
	private void copy(Object[] sources) {
		
		
		for (int i = 0; i < sources.length; i++) {
			System.out.println("Deep copy of " + ((Node)sources[i]).getWrappedElement().getName());
		}
	}
	
	private class DeepCopyDialog extends Dialog {
		private ContainerCheckedTreeViewer viewer;
		private Object input;
		
		protected DeepCopyDialog(Shell parentShell) {
			super(parentShell);
		}

		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite)super.createDialogArea(parent);
			viewer = new ContainerCheckedTreeViewer(composite);
			viewer.setLabelProvider(new ProductStructureLabelProvider());
			viewer.setContentProvider(new ProductStructureContentProvider(true));
			TreeColumn col = new TreeColumn(viewer.getTree(), SWT.CENTER);
			col.setText("Col1");
			viewer.getTree().setHeaderVisible(true);
			if (input != null)
			viewer.setInput(input);			
			
			return composite;
		}
		
		public void setInput(Object input) {
			this.input = input;
		}
		
	}
}
