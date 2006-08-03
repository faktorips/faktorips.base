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

package org.faktorips.devtools.core.ui.editors.testcase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.StringDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.util.StringUtil;

/**
 * Detail section class of the test case editor.
 * Supports dynamic creation of detail edit controls.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseDetailArea {
	static final String VALUESECTION = "VALUESECTION";

	// UI toolkit for creating the controls
	private UIToolkit toolkit;
	
	// Contains the content provider of the test policy component object
	private TestCaseContentProvider contentProvider;
	
	// Contains all test policy component edit sections
	private HashMap sectionControls = new HashMap();
	
	// Container holds all value edit fields
	HashMap valueEditFields = new HashMap();
	
	// Contains the first edit field of each test policy component edit area
	HashMap attributeEditFields = new HashMap();
	
	// Contains all ui controller
	private ArrayList uiControllers = new ArrayList();
	
	private TestCaseSection testCaseSection;
	
	// Composites to change the UI
	//   area which contains the dynamic detail controls
	private Composite detailsArea;
	//   area which contains alls detail controls
	private Composite dynamicArea;
	
	public TestCaseDetailArea(UIToolkit toolkit, TestCaseContentProvider contentProvider, TestCaseSection testCaseSection) {
		this.toolkit = toolkit;
		this.contentProvider = contentProvider;
		this.testCaseSection = testCaseSection;
	}
	
	/**
	 * Resets the color of all detail sections.
	 */
	public void resetSectionColors(ScrolledForm form){
		Iterator iter = sectionControls.values().iterator();
		while (iter.hasNext()) {
			Section section = (Section) iter.next();
			section.setBackground(form.getBackground());
		}
	}

	/**
	 * Returns the attribute edit fields given by the unique key.
	 */
	public EditField getAttributeEditField(String uniqueKey) {
		return (EditField) attributeEditFields.get(uniqueKey);
	}	
	
	/**
	 * Returns the test value edit fields given by the unique key.
	 */
	public EditField getTestValueEditField(String uniqueKey) {
		return (EditField) valueEditFields.get(uniqueKey);
	}

	/**
	 * Returns the section given by the unique key.
	 */
	public Section getSection(String uniqueKey) {
		return (Section) sectionControls.get(uniqueKey);
	}
	
	/**
	 * Creates the main detail area.
	 */
	public void createInitialDetailArea(Composite parent, String title) {
		Section detailsSection = toolkit.getFormToolkit().createSection(parent, Section.TITLE_BAR);		
		detailsSection.setLayoutData(new GridData(GridData.FILL_BOTH));		
		detailsSection.setText(title);
		detailsArea = toolkit.createComposite(detailsSection);
		detailsArea.setLayoutData(new GridData(GridData.FILL_BOTH));
		detailsSection.setClient(detailsArea);
		GridLayout detailLayout = new GridLayout(1, false);
		detailLayout.horizontalSpacing = 0;
		detailLayout.marginWidth = 0;
		detailLayout.marginHeight = 0;
		detailsArea.setLayout(detailLayout);
	}
	
	/**
	 * Create the detail area depending on the given test policy component types.<br>
	 * The detail will be cleaned and filled with the given element and all childs of the given element.<br>
	 * The test policy component will be rendered starting by the root element of the 
	 * given element, the root will be only displayed once if more than one childs are given.
	 */
	public void createDetailSection(List testPolicyCmpts){		
		// dynamically create the details depending on the given test policy components
		clearDetailArea();
		
		for (Iterator iter = testPolicyCmpts.iterator(); iter.hasNext();) {
			ITestPolicyCmpt currTestPolicyCmpt = (ITestPolicyCmpt) iter.next();
			
			Composite borderedComosite = createBorder(dynamicArea);
			if (currTestPolicyCmpt != null) {
				createPolicyCmptAndRelationSection(currTestPolicyCmpt, borderedComosite);
			}
		}
	}
	
	/**
	 * Creates the section with the test policy component object.<br>
	 * If the element is a child then the relation name could be given as input
	 * to display it in the section title beside the test policy component.
	 */
	private void createPolicyCmptSection(final ITestPolicyCmpt testPolicyCmpt, Composite details) {
		if (testPolicyCmpt == null)
			return;
		String uniquePath = testCaseSection.getUniqueKey(testPolicyCmpt);
		
		Section section = toolkit.getFormToolkit().createSection(details, 0);
		String sectionText = testPolicyCmpt.getLabel();
		if (testPolicyCmpt.getProductCmpt().length() > 0){
			String pckName = StringUtil.getPackageName(testPolicyCmpt.getProductCmpt());
			sectionText += pckName.length() > 0 ? " (" + pckName + ")" : "";
		}
		section.setText(sectionText);
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		toolkit.getFormToolkit().createCompositeSeparator(section);
		
		sectionControls.put(uniquePath, section);
		
		// create text edit fields for each attribute
		boolean failure = false;
		ITestAttributeValue[] testAttributeValue = testPolicyCmpt.getTestAttributeValues();
		Composite attributeComposite = toolkit.createLabelEditColumnComposite(section);
		for (int i = 0; i < testAttributeValue.length; i++) {
			final ITestAttributeValue attributeValue = testAttributeValue[i];
						
			IpsPartUIController uiController = createUIController(attributeValue);

			ValueDatatype datatype = null;
			ValueDatatypeControlFactory ctrlFactory = null;
			try {
				ITestAttribute testAttr = attributeValue.findTestAttribute();
				if (testAttr==null){
					// ignore not existing test attributes, will be checked in the vaidate method
					failure = true;
				}else{
					IAttribute attribute = testAttr.findAttribute();
					if (attribute==null){
						// ignore not existing attributes, will be checked in the vaidate method
						failure = true;
					}else{
						datatype = attribute.findDatatype();
						ctrlFactory = IpsPlugin.getDefault().getValueDatatypeControlFactory(datatype);
					}
				}
			} catch (CoreException e1) {
				// ignore error, will be check in the validate methods
				failure = true;
			}
			
			if (!failure){
				toolkit.createFormLabel(attributeComposite, StringUtils.capitalise(attributeValue.getTestAttribute()));
				final EditField editField = ctrlFactory.createEditField(toolkit, attributeComposite, datatype, null);
				uiController.add(editField, ITestAttributeValue.PROPERTY_VALUE);
				if (i==0){
					// store the first text edit control to get the focus if chosen in the tree
					attributeEditFields.put(uniquePath, editField);
				}
	
				editField.getControl().addFocusListener(new FocusAdapter() {
		            public void focusGained(FocusEvent e) {
		                testCaseSection.selectInTreeByObject(testPolicyCmpt, false);
		            }
		        });
			    uiController.updateUI();
			}
		}
		section.setClient(attributeComposite);
		
		toolkit.createVerticalSpacer(details, 10).setBackground(details.getBackground());
	}
	
	/**
	 * Creates the section for a relation of type association.<br>
	 * Create a hyperlink if the realtion exists is in the current test case or
	 * create a label with the test relation target.
	 */
	private void createRelationSectionAssociation(final ITestPolicyCmptRelation currRelation, Composite details) {
		String uniquePath = testCaseSection.getUniqueKey(currRelation);
		
		Section section = toolkit.getFormToolkit().createSection(details,0);
		section.setText(currRelation.getTestPolicyCmptType());
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Composite hyperlinkArea = toolkit.createGridComposite(details, 2, false, true);
		sectionControls.put(uniquePath, section);

		if ((((ITestPolicyCmpt)currRelation.getParent()).isInputObject() ? 
				contentProvider.getTestCase().findInputPolicyCmpt(currRelation.getTarget()):
				contentProvider.getTestCase().findExpectedResultPolicyCmpt(currRelation.getTarget()))!= null){
			Hyperlink relationHyperlink = toolkit.getFormToolkit()
				.createHyperlink(hyperlinkArea, TestCaseHierarchyPath.unqualifiedName(currRelation.getTarget()),SWT.WRAP);
			relationHyperlink.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					try {
						testCaseSection.selectInTreeByObject(currRelation.findTarget(), true);
					} catch (CoreException e1) {
						throw new RuntimeException(e1);
					}
				}
			});
			relationHyperlink.addFocusListener(new FocusListener() {
	            public void focusGained(FocusEvent e) {
	            	testCaseSection.selectInTreeByObject(currRelation, false);
	            }
	            public void focusLost(FocusEvent e) {
	            }
	        });
			toolkit.createLabel(hyperlinkArea, " (" + TestCaseHierarchyPath.getFolderName(currRelation.getTarget()) + ")");
		} else {
			// target not found in current test case
			toolkit.createLabel(hyperlinkArea, TestCaseHierarchyPath.unqualifiedName(currRelation.getTarget()));
			toolkit.createLabel(hyperlinkArea, " (" + TestCaseHierarchyPath.getFolderName(currRelation.getTarget()) + ")");
		}
	}
	
	/**
	 * Recursive create the sections for the relations and all their childs.
	 */
	private void createPolicyCmptAndRelationSection(ITestPolicyCmpt currTestPolicyCmpt, Composite details) {
		createPolicyCmptSection(currTestPolicyCmpt, details);
		ITestPolicyCmptRelation[] relations = currTestPolicyCmpt.getTestPolicyCmptRelations();
		for (int i = 0; i < relations.length; i++) {
			ITestPolicyCmptRelation currRelation = relations[i];
			if (currRelation.isComposition()) {
				try {
					ITestPolicyCmpt policyCmpt = currRelation.findTarget();
					if (policyCmpt != null){
						createPolicyCmptAndRelationSection(policyCmpt, details);
					}
				} catch (CoreException e) {
					IpsPlugin.logAndShowErrorDialog(e);
				}
			} else {
				// relation is an association
				createRelationSectionAssociation(currRelation, details);
			}
		}
	}
	
	/**
	 * Creates the section for the value objects.
	 * 
	 * @param cleanBefore <code>true</code> to clean the detail area before rendering the value section
	 *                    <code>false</code> the value section will be rendered below the existing sections
	 */
	public void createValuesSection(boolean cleanBefore) {
		if (cleanBefore)
			clearDetailArea();
		
		Composite borderedComposite = createBorder(dynamicArea);
		
		Section section = toolkit.getFormToolkit().createSection(borderedComposite, 0);
		section.setText("Values");
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		toolkit.getFormToolkit().createCompositeSeparator(section);
		
		sectionControls.put(VALUESECTION, section);
		
		Composite composite = toolkit.createLabelEditColumnComposite(section);
		section.setClient(composite);
		
		ITestValue[] values = contentProvider.getSortedValues();
		for (int i = 0; i < values.length; i++) {
			final ITestValue value = values[i];
			
			IpsPartUIController uiController = createUIController(value);
			
			ValueDatatype datatype = null;
			ValueDatatypeControlFactory ctrlFactory = null;
			try {
				ITestValueParameter param = value.findTestValueParameter();
				if (param != null){
					datatype = param.findValueDatatype();
					ctrlFactory = IpsPlugin.getDefault().getValueDatatypeControlFactory(datatype);
				} else {
					ctrlFactory = IpsPlugin.getDefault().getValueDatatypeControlFactory(new StringDatatype());
				}
			} catch (CoreException e1) {
				throw new RuntimeException(e1);
			}
			
			toolkit.createFormLabel(composite, StringUtils.capitalise(value.getTestValueParameter()) + ":");
			final EditField editField = ctrlFactory.createEditField(toolkit, composite, datatype, null);
			uiController.add(editField, ITestValue.PROPERTY_VALUE);
			
			editField.getControl().addFocusListener(new FocusAdapter() {
	            public void focusGained(FocusEvent e) {
	            	testCaseSection.selectTestValueInTree(value);
	            }
	        });
			
		    valueEditFields.put(value.getTestValueParameter(), editField);
		    uiController.updateUI();
		}
		toolkit.createVerticalSpacer(borderedComposite, 10).setBackground(dynamicArea.getBackground());
	}	

	/**
	 * Create a bordered composite
	 */
	private Composite createBorder(Composite parent){
		
		Composite c1 = toolkit.createLabelEditColumnComposite(parent);
		c1.setLayoutData(new GridData(GridData.FILL_BOTH));
		c1.setLayout(new GridLayout(1, true));
		
		Composite c2 = toolkit.getFormToolkit().createComposite(c1);
		c2.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout detailLayout = new GridLayout(1, true);
		
		detailLayout.horizontalSpacing = 10;
		detailLayout.marginWidth = 10;
		detailLayout.marginHeight = 10;
		c2.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
	
		c2.setLayout(detailLayout);
		return c2;
	}
	
	/** 
	 * Creates a new ui controller for the given object.
	 */
	private IpsPartUIController createUIController(IIpsObjectPart part) {
		IpsPartUIController controller = new IpsPartUIController(part) {
			public void valueChanged(FieldValueChangedEvent e) {
				try {
					super.valueChanged(e);
				} catch (Exception ex) {
					IpsPlugin.logAndShowErrorDialog(ex);
				}
			}
		};
		uiControllers.add(controller);
		
		return controller;
	}

	/**
	 * Resets the containers containing the control references.
	 */
	private void resetContainers() {
		valueEditFields.clear();
		attributeEditFields.clear();
		sectionControls.clear();
		uiControllers.clear(); 
	}
	
	/*
	 * Clear the dynamic detail area.
	 */
	private void clearDetailArea() {
		if (dynamicArea != null)
			dynamicArea.dispose();

		dynamicArea = toolkit.getFormToolkit().createComposite(detailsArea);
		dynamicArea.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout detailLayout = new GridLayout(1, true);
		detailLayout.horizontalSpacing = 0;
		detailLayout.marginWidth = 0;
		detailLayout.marginHeight = 0;
		dynamicArea.setLayout(detailLayout);
		
		resetContainers();
	}
}
