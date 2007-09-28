/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.deltapresentation;


import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.product.IGenerationToTypeDelta;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Dialog to display differences between a product component and its type.
 * 
 * @author Thorsten Guenther
 */
public class ProductCmptDeltaDialog extends TitleAreaDialog {

	private IProductCmptGeneration[] generations;
	private IGenerationToTypeDelta[] deltas;
	private TreeViewer tree;
	private List generationsList;
	private UIToolkit toolkit;
	private Composite parent;
	
	/**
	 * Create a new dialog, showing all the differences given. The first delta found
	 * in the given deltas has to be for the first generation and so on.
	 * 
	 * @param parentShell The SWT parent-shell
	 * @param generations All generations with differences.
	 * @param deltas All deltas for the generations.
	 */
	public ProductCmptDeltaDialog(IProductCmptGeneration[] generations, IGenerationToTypeDelta[] deltas, Shell parentShell) {
		super(parentShell);
		super.setShellStyle(getShellStyle() | SWT.RESIZE);
		this.generations = generations;
		this.deltas = deltas;
		this.toolkit = new UIToolkit(null);
	}

	/**
	 * {@inheritDoc}
	 */
	protected Control createDialogArea(Composite parent) {
		this.parent = parent;
		
		String genTextPlural = IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNamePlural();
		String genTextSingular = IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNameSingular();
		Composite root = (Composite)super.createDialogArea(parent);
		
		// layouting
		
		// create composite with margins
		Composite listParent = toolkit.createGridComposite(root, 3, false, false);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.minimumHeight = 10+30+50+10;
		listParent.setLayoutData(gridData);

        // line 1 (label for list)
		toolkit.createVerticalSpacer(listParent, 1);
		String text = NLS.bind(Messages.ProductCmptDeltaDialog_labelSelectGeneration, genTextPlural);
		Label label = toolkit.createLabel(listParent, text, false);
		((GridData)label.getLayoutData()).minimumHeight = 10;
		toolkit.createVerticalSpacer(listParent, 1);
		
		// line 2 (list)
		toolkit.createVerticalSpacer(listParent, 1);
		generationsList = new List(listParent, SWT.SINGLE | SWT.BORDER);
		toolkit.createVerticalSpacer(listParent, 1);
		
		for (int i = 0; i < generations.length; i++) {
			generationsList.add(generations[i].getName());
		}

		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.minimumHeight = 30;
		generationsList.setLayoutData(gridData);

		// line 3 (label for tree)
		toolkit.createVerticalSpacer(listParent, 1);
		text = NLS.bind(Messages.ProductCmptDeltaDialog_labelSelectedDifferences, genTextSingular);
		label = toolkit.createLabel(listParent, text, false);
		((GridData)label.getLayoutData()).minimumHeight = 10;
		toolkit.createVerticalSpacer(listParent, 1);
		
		// line 4 (tree)
		toolkit.createVerticalSpacer(listParent, 1);
		tree = new TreeViewer(listParent);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.minimumHeight = 50;
		tree.getTree().setLayoutData(gridData);
		toolkit.createVerticalSpacer(listParent, 1);

		// adding data and behaviour
        getShell().setText(Messages.ProductCmptDeltaDialog_title);
        
        if (isProductEditableInAllGenerations()) {
            setMessage(Messages.ProductCmptDeltaDialog_message, IMessageProvider.INFORMATION);
        } else {
            // set warning to inform that recent generation could not be edit,
            // but with this wizard recent generation could be changed
            setMessage(Messages.ProductCmptDeltaDialog_message
                    + "\n" //$NON-NLS-1$
                    + NLS.bind(Messages.ProductCmptDeltaDialog_messageWarningRecentGenerationCouldBeChanged,
                            genTextPlural), IMessageProvider.WARNING);
        }

        tree.setContentProvider(new DeltaContentProvider());
        tree.setLabelProvider(new DeltaLabelProvider());
		
		generationsList.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			public void widgetSelected(SelectionEvent e) {
				updateDeltaView();
			}
		});

		// initialize view
		generationsList.setSelection(0);
		updateDeltaView();
		
		return root;
	}


    /**
	 * {@inheritDoc}
	 */
	protected Control createButtonBar(Composite parent) {
		Control buttons = super.createButtonBar(parent);
		super.getButton(OK).setText(Messages.ProductCmptDeltaDialog_fix);
		super.getButton(CANCEL).setText(Messages.ProductCmptDeltaDialog_ignore);
		return buttons; 
	}

    /*
	 * Returns <code>true</code> if the user can edit recent generations, if
	 * recent generations couldn't be edit return <code>false</code>.
	 */
	private boolean isProductEditableInAllGenerations() {
	    return IpsPlugin.getDefault().getIpsPreferences().canEditRecentGeneration();
	}

	private void updateDeltaView() {
		tree.setInput(deltas[generationsList.getSelectionIndex()]);
		tree.refresh();
		tree.expandAll();
		parent.layout();
	}
	
	public IProductCmptGeneration[] getGenerations() {
		return generations;
	}
	
	public IGenerationToTypeDelta[] getDeltas() {
		return deltas;
	}
}
