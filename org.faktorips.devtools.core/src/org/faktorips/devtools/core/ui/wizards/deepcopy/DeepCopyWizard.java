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
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.product.DeepCopyOperation;
import org.faktorips.devtools.core.model.CycleException;
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
	private ISchedulingRule schedulingRule;
	
	/**
	 * Creates a new wizard which can make a deep copy of the given product
	 */
	public DeepCopyWizard(IProductCmpt product) {
		super();
		try {
			structure = product.getStructure();
		} catch (CycleException e) {
			IpsPlugin.log(e);
		}
		super.setWindowTitle(Messages.DeepCopyWizard_title);
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
		try {
			final IProductCmpt[] toCopy = previewPage.getProductsToCopy();
			final IProductCmpt[] toRefer = previewPage.getProductsToRefer();
			final Map handles = previewPage.getHandles();
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
		return true;
	}

	/**
	 * Returns the root product component which was copied.
	 */
	public IProductCmpt getCopiedRoot() {
		return copiedRoot;
	}
}
