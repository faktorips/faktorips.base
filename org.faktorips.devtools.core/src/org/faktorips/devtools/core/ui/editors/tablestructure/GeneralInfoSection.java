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

package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructureType;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.ComboViewerField;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.ArgumentCheck;

/**
 * Shows a table structure's general properties and allowes to edit them.
 *  
 * @author Thorsten Waertel
 */
public class GeneralInfoSection extends IpsSection {

	private ITableStructure tableStructure;
	
    private IpsObjectUIController uiController;

    private ComboViewerField typeField; 
	
	public GeneralInfoSection(
			ITableStructure tableStructure, 
			Composite parent,
			UIToolkit toolkit) {
		
        super(parent, Section.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);
        ArgumentCheck.notNull(tableStructure);
        this.tableStructure = tableStructure;
        
        initControls();
        setText("General information");
	}

	/**
	 * {@inheritDoc}
	 */
	protected void initClientComposite(Composite client, UIToolkit toolkit) {
        client.setLayout(new GridLayout(1, false));
        Composite composite = toolkit.createLabelEditColumnComposite(client);

        toolkit.createFormLabel(composite, "Type of table:");
        Combo combo = toolkit.createCombo(composite);
        
        combo.add(TableStructureType.SINGLE_CONTENT.getName());
        combo.add(TableStructureType.MULTIPLE_CONTENTS.getName());
        combo.add(TableStructureType.ENUMTYPE_MODEL.getName());
        combo.add(TableStructureType.ENUMTYPE_PRODUCTDEFINTION.getName());
        
        ComboViewer viewer = new ComboViewer(combo);
        viewer.setContentProvider(new IStructuredContentProvider() {
		
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				((ComboViewer)viewer).add(TableStructureType.getAll());
			}
		
			public void dispose() {
				// nothing to do
			}

			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof String) {
					return TableStructureType.getAll();
				}
				return null;
			}
		});
        
        viewer.setLabelProvider(new ILabelProvider() {
		
			public void removeListener(ILabelProviderListener listener) {
				// nothing to do
			}
		
			public boolean isLabelProperty(Object element, String property) {
				return true;
			}
		
			public void dispose() {
				// nothing to do
			}
		
			public void addListener(ILabelProviderListener listener) {
				// nothing to do
			}

			public Image getImage(Object element) {
				return null;
			}

			public String getText(Object element) {
				return ((TableStructureType)element).getName();
			}
		
		});
        
        viewer.setInput("");
        typeField = new ComboViewerField(viewer);
        
        uiController = new IpsObjectUIController(tableStructure);
        uiController.add(typeField, ITableStructure.PROPERTY_TYPE);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void performRefresh() {
		uiController.updateUI();
	}

}
