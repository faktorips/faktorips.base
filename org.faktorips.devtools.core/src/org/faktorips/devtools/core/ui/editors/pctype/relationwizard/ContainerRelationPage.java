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

package org.faktorips.devtools.core.ui.editors.pctype.relationwizard;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;

/**
 * Optional relation wizard page to chose the container relation if available.
 */
public class ContainerRelationPage extends AbstractPcTypeRelationWizardPage {
	private static final String PAGE_ID = "NewPcTypeRelationWizard.ContainerRelation"; //$NON-NLS-1$
	
	private ComboField containerRelationsField;
	
	// stores the previous target to indicate changes on the target
	private String prevTarget=""; //$NON-NLS-1$
	
	public ContainerRelationPage(NewPcTypeRelationWizard newPcTypeRelationWizard) {
		super(PAGE_ID,
				Messages.NewPcTypeRelationWizard_containerRelation_title,
				Messages.NewPcTypeRelationWizard_containerRelation_description,
				newPcTypeRelationWizard);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void createControls(Composite parent) {
		UIToolkit uiToolkit = wizard.getUiToolkit();
		
		// create controls
		Composite workArea = uiToolkit.createLabelEditColumnComposite(parent);
		workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		uiToolkit.createFormLabel(workArea,
				Messages.NewPcTypeRelationWizard_containerRelation_labelContainerRelation);
		final Combo containerRelationsCombo = uiToolkit.createCombo(workArea);	
		containerRelationsCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				wizard.getRelation().setContainerRelation(containerRelationsCombo.getItem(containerRelationsCombo
						.getSelectionIndex()));
			}
		});
		
		containerRelationsField = new ComboField(containerRelationsCombo);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void connectToModel() {
		wizard.addToUiControllerRelation(containerRelationsField, IRelation.PROPERTY_CONTAINER_RELATION);     
	}

	/**
	 * {@inheritDoc}
	 */
	protected boolean isPageVisible(){
		boolean isPageVisible=false;
		
		// visible only if target has a container relation
		try {
			isPageVisible = getSourceContainerRelations().size() > 0;
		} catch (CoreException e) {
			IpsPlugin.log(e);
			wizard.showErrorPage(e);
		}
		
		// visible only if this relation is no container relation
		if (wizard.getRelation().isReadOnlyContainer()){
			isPageVisible = false;
		}

		return isPageVisible;
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected boolean updateControlStatus() {
		// insert all container relations of the source in the drop down field
		if (! prevTarget.equals(wizard.getRelation().getTarget())){
			prevTarget = wizard.getRelation().getTarget();
			ArrayList containerRelations = null;
			try {
				containerRelations = getSourceContainerRelations();
				String[] names = new String[containerRelations.size() + 1];
				names[0] = ""; // first entry to select none container relation //$NON-NLS-1$
				for (int i = 0; i < containerRelations.size(); i++) {
					names[i+1] = ((IRelation)containerRelations.get(i)).getName();
				}
				if (containerRelations.size()>0){
					containerRelationsField.getCombo().setItems(names);
				}else{
					containerRelationsField.getCombo().setItems(new String[0]);
				}
			} catch (CoreException e) {
				IpsPlugin.log(e);
				wizard.showErrorPage(e);
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns the container relations of the source policy component type.
	 * 
	 * @throws CoreException if there was an error getting the type hierarchy.
	 */
	private ArrayList getSourceContainerRelations() throws CoreException {
		ArrayList containerRelations = new ArrayList();
		ITypeHierarchy hierarchy = wizard.getRelation().getPolicyCmptType().getSupertypeHierarchy();
		IRelation[] relations = hierarchy.getAllRelations(wizard.getRelation().getPolicyCmptType());
		for (int i = 0; i < relations.length; i++) {
			if (relations[i].isReadOnlyContainer()) {
				containerRelations.add(relations[i]);
			}
		}
		return containerRelations;
	}	
}
