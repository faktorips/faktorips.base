package org.faktorips.devtools.core.ui.wizards.deepcopy;


import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.wizard.Wizard;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.product.DeepCopyOperation;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptStructure;

/**
 * A wizard to create a deep copy from a given product component.
 * 
 * @author Thorsten Guenther
 */
public class DeepCopyWizard extends Wizard {
	private IProductCmptStructure structure;
	private SourcePage sourcePage;
	private ReferenceAndPreviewPage previewPage;
	private IProductCmpt copiedRoot;
	
	/**
	 * Creates a new wizard which can make a deep copy of the given product
	 */
	public DeepCopyWizard(IProductCmpt product) {
		super();
		structure = product.getStructure();
		super.setWindowTitle("Copy Product");
	}

	/**
	 * {@inheritDoc}
	 */
	public void addPages() {
		sourcePage = new SourcePage(structure);
		super.addPage(sourcePage);
		previewPage = new ReferenceAndPreviewPage(structure, sourcePage);
		super.addPage(previewPage);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean performFinish() {
		boolean finished = false;
		try {
			DeepCopyOperation dco = new DeepCopyOperation(previewPage.getProductsToCopy(), previewPage.getProductsToRefer(), previewPage.getHandles());
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(super.getShell());
			dialog.run(true, false, dco);
			copiedRoot = dco.getCopiedRoot();
			finished = true;
		} catch (InvocationTargetException e) {
			IpsPlugin.logAndShowErrorDialog(e);
		} catch (InterruptedException e) {
			IpsPlugin.logAndShowErrorDialog(e);
		} catch (CoreException e) {
			IpsPlugin.logAndShowErrorDialog(e);
		}		
		return finished;
	}

	/**
	 * Returns the root product component which was copied.
	 */
	public IProductCmpt getCopiedRoot() {
		return copiedRoot;
	}
}
