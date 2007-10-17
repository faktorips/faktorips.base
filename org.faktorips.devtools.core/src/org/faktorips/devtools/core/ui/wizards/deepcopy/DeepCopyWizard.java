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

package org.faktorips.devtools.core.ui.wizards.deepcopy;


import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.product.DeepCopyOperation;
import org.faktorips.devtools.core.internal.model.product.ProductCmptStructure;
import org.faktorips.devtools.core.model.CycleException;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptReference;
import org.faktorips.devtools.core.ui.wizards.ResizableWizard;

/**
 * A wizard to create a deep copy from a given product component.
 *
 * @author Thorsten Guenther
 */
public class DeepCopyWizard extends ResizableWizard {

	public static final int TYPE_COPY_PRODUCT = 10;
	public static final int TYPE_NEW_VERSION = 100;

	private ProductCmptStructure structure;
	private SourcePage sourcePage;
	private ReferenceAndPreviewPage previewPage;
	private IProductCmpt copiedRoot;
	private ISchedulingRule schedulingRule;
	private int type;

	/**
	 * Creates a new wizard which can make a deep copy of the given product.
	 *
	 * @param type One of TYPE_COPY_PRODUCT or TYPE_NEW_VERSION. The first one
	 * allows to enter the version id (if supported by product component naming strategy)
	 * free and enter a search- and a rename-pattern. The second one does neither support
	 * to set the version id manually nor does it allow the user to enter a search- and a
	 * rename-pattern.
	 *
	 * @throws IllegalArgumentException if the given type is not valid.
	 */
	public DeepCopyWizard(IProductCmpt product, int type) throws IllegalArgumentException {
		super("DeepCopyWizard", IpsPlugin.getDefault().getDialogSettings(), 600, 800);
        setNeedsProgressMonitor(true);

		if (type != TYPE_COPY_PRODUCT && type != TYPE_NEW_VERSION) {
			throw new IllegalArgumentException("The given type is neither TYPE_COPY_PRODUCT nor TYPE_NEW_VERSION."); //$NON-NLS-1$
		}
		this.type = type;

		try {
			// the working date lies before the valid from date of the first available generation
			// of the given product component - so we have to take this valid-from date rather
			// then the working date to build the product component structure.
			if (IpsPlugin.getDefault().getIpsPreferences().getWorkingDate().before(product.getFirstGeneration().getValidFrom())) {
				String title = Messages.DeepCopyWizard_titleWorkingDateNotUsed;
				String msg = NLS
						.bind(
								Messages.DeepCopyWizard_msgWorkingDateNotUsed,
								IpsPlugin.getDefault().getIpsPreferences()
										.getChangesOverTimeNamingConvention()
										.getGenerationConceptNameSingular());

				MessageDialog.openInformation(getShell(), title, msg);

				structure = (ProductCmptStructure)product.getStructure(product.getFirstGeneration().getValidFrom(), product.getIpsProject());
			} else {
				structure = (ProductCmptStructure)product.getStructure(product.getIpsProject());
			}

		} catch (CycleException e) {
			IpsPlugin.log(e);
		}

		if (type == TYPE_COPY_PRODUCT) {
			super.setWindowTitle(Messages.DeepCopyWizard_title);
            super.setDefaultPageImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("wizards/DeepCopyWizard.png")); //$NON-NLS-1$
		} else {
			String title = NLS.bind(Messages.DeepCopyWizard_titleNewVersion, IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention().getVersionConceptNameSingular());
			super.setWindowTitle(title);
            super.setDefaultPageImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("wizards/NewVersionWizard.png")); //$NON-NLS-1$
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addPages() {
		sourcePage = new SourcePage(structure, type);
		super.addPage(sourcePage);
		previewPage = new ReferenceAndPreviewPage(structure, sourcePage, type);
		super.addPage(previewPage);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean performFinish() {
		try {
			final IProductCmptReference[] toCopy = previewPage.getProductsToCopy();
			final IProductCmptReference[] toRefer = previewPage.getProductsToRefer();
			final Map handles = previewPage.getHandles();
			schedulingRule = structure.getRoot().getProductCmpt().getIpsProject().getCorrespondingResource().getWorkspace().getRoot();
			WorkspaceModifyOperation operation = new WorkspaceModifyOperation(schedulingRule){

				protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
					DeepCopyOperation dco = new DeepCopyOperation(toCopy, toRefer, handles);
					dco.run(monitor);
					copiedRoot = dco.getCopiedRoot();
				}

			};
			getContainer().run(true, true, operation);

		} catch (Exception e) {
			IpsPlugin.logAndShowErrorDialog(new IpsStatus("An error occured during the copying process.",e)); //$NON-NLS-1$
		}

		//this implementation of this method should always return true since this causes the wizard dialog to close.
		//in either case if an exception arises or not it doesn't make sense to keep the dialog up
		return super.performFinish();
	}

	/**
	 * Returns the root product component which was copied.
	 */
	public IProductCmpt getCopiedRoot() {
		return copiedRoot;
	}

}
