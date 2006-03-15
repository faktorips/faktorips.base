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

import java.util.ArrayList;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeItem;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptStructure;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.ProductStructureContentProvider;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.ProductStructureLabelProvider;

/**
 * Page to let the user select products related to each other. 
 * 
 * @author Thorsten Guenther
 */
public class SourcePage extends WizardPage implements ICheckStateListener {
	private IProductCmptStructure structure;
	private CheckboxTreeViewer tree;
	
	private static final String PAGE_ID = "deepCopyWizard.source"; //$NON-NLS-1$

	/**
	 * Creates a new page to select the objects to copy.
	 */
	protected SourcePage(IProductCmptStructure structure) {
		super(PAGE_ID, Messages.SourcePage_title, null); 
		this.structure = structure;
		setPageComplete();
		
		super.setTitle(Messages.SourcePage_pageTitle);
		super.setDescription(Messages.SourcePage_description);
	}

	/**
	 * {@inheritDoc}
	 */
	public void createControl(Composite parent) {

		if (structure == null) {
			Label errormsg = new Label(parent, SWT.WRAP);
			GridData layoutData = new GridData(SWT.LEFT, SWT.TOP, true, false);
			errormsg.setLayoutData(layoutData);
			errormsg.setText(Messages.SourcePage_msgCircleRelation);
			this.setControl(errormsg);
			return;
		}
		
		tree = new CheckboxTreeViewer(parent);
		
		tree.setLabelProvider(new ProductStructureLabelProvider());
		tree.setContentProvider(new ProductStructureContentProvider(true));
		tree.setInput(this.structure);
		tree.expandAll();
		setCheckedAll(tree.getTree().getItems(), true);
		tree.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tree.addCheckStateListener(this);		
		tree.addCheckStateListener(new CheckStateListener(null));
		this.setControl(tree.getControl());
	}

	private void setCheckedAll(TreeItem[] items, boolean checked) {
		for (int i = 0; i < items.length; i++) {
			items[i].setChecked(checked);
			setCheckedAll(items[i].getItems(), checked);
		}
		setPageComplete();
	}

	/**
	 * Set the current completion state (and, if neccessary, messages for the user
	 * to help him to get the page complete).
	 */
	private void setPageComplete() {
		super.setPageComplete(tree != null && tree.getCheckedElements().length > 0);
		
		if (tree != null && tree.getCheckedElements().length > 0) {
			super.setMessage(null);
		}
		else if (structure == null) {
			super.setMessage(Messages.SourcePage_msgCircleRelationShort, ERROR);
		} else {
			super.setMessage(Messages.SourcePage_msgSelect, INFORMATION);
		}
	}
	
	/**
	 * Returns all products checked for copy. 
	 */
	public IProductCmpt[] getCheckedProducts() {
		ArrayList result = new ArrayList();
		
		Object[] checked = tree.getCheckedElements();
		for (int i = 0; i < checked.length; i++) {
			if (checked[i] instanceof IProductCmpt) {
				result.add(checked[i]);
			}
		}
		
		return (IProductCmpt[])result.toArray(new IProductCmpt[result.size()]);
	}
	
	public void checkStateChanged(CheckStateChangedEvent event) {
		setPageComplete(); 
	}		
}


