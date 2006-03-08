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
