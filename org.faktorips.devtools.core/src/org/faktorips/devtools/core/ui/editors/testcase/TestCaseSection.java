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

package org.faktorips.devtools.core.ui.editors.testcase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.Validatable;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.ui.PdObjectSelectionDialog;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.message.MessageList;

/**
 * Section to show the test case.
 */
public class TestCaseSection extends IpsSection {
	private static final String VALUESECTION = "VALUESECTION";

	/** The treeview which displays all test policy components which are available in this test */
	private TreeViewer treeViewer;

	/** Contains the test case which is displayed in this section */
	private ITestCase testCase;
	
	/** UI toolkit for creating the controls */
	private UIToolkit toolkit;

	/** Buttons */
	Button addButton;
	Button removeButton;
	
	/** Title of the test case tree structure section */
    private String sectionTreeStructureTitle;
    
    /** Title of the detail section */
    private String sectionDetailTitle;	
    
	/** Contains the content provider of the test policy component object */
	private TestCaseContentProvider contentProvider;

	/** The deatil area of the test case */
	private TestCaseDetailArea testCaseDetailArea;
	
	/** Previous selected entries in the tree to */
	private List prevTestPolicyCmpt;
	boolean prevIsValue;
	
	/** The form which contains this section */
	private ScrolledForm form;
	
	/** Indicates if all objects of the test case are displayed or 
	 * the child objects of selected root are visible */
	boolean showAll = false;
	
	/** Indicates if the tree selection was doubleclicked */
	private boolean isDoubleClicked = false;

	public TestCaseSection(Composite parent,
			UIToolkit toolkit, TestCaseContentProvider contentProvider, String title, String detailTitle, 
			ScrolledForm form) {
		super(parent, Section.NO_TITLE, GridData.FILL_BOTH, toolkit);
		this.contentProvider = contentProvider;
		this.form = form;
    	this.sectionTreeStructureTitle = title;
    	this.sectionDetailTitle = detailTitle;
		initControls();
		setText(title);
		
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.horizontalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		parent.setLayout(gridLayout);
	}
    
	/**
	 * Initialization of the main section.
	 * {@inheritDoc}
	 */
	protected void initClientComposite(Composite client, UIToolkit toolkit) {
		this.toolkit = toolkit;
		this.testCase = contentProvider.getTestCase();
		
		createToolBarActions();
		form.updateToolBar();
		
		client.setLayout(new GridLayout(2, true));
		client.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// Tree structure section
		Section structureSection = toolkit.getFormToolkit().createSection(client, Section.TITLE_BAR);
		structureSection.setLayoutData(new GridData(GridData.FILL_BOTH));
		structureSection.setText(sectionTreeStructureTitle);
		Composite structureComposite = toolkit.getFormToolkit().createComposite(structureSection);
		structureComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout structureLayout = new GridLayout(2, false);
		structureLayout.horizontalSpacing = 0;
		structureLayout.marginWidth = 0;
		structureLayout.marginHeight = 3;
		structureComposite.setLayout(structureLayout);
		structureSection.setClient(structureComposite);
		Composite treeComposite = toolkit.createLabelEditColumnComposite(structureComposite);
		treeComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		Tree tree = toolkit.getFormToolkit().createTree(treeComposite,
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		treeViewer = new TreeViewer(tree);
		treeViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		hookTreeListeners();
		treeViewer.setContentProvider(contentProvider);
		TestCaseLabelProvider labelProvider = new TestCaseLabelProvider();
		treeViewer.setLabelProvider(new TestCaseMessageCueLabelProvider(labelProvider));
		new TableMessageHoverService(treeViewer) {
            protected MessageList getMessagesFor(Object element) throws CoreException {
                if (element!=null)
                	return ((Validatable)element).validate();
                else
                	return null;
            }
		};
		treeViewer.setInput(testCase);

		// Buttons belongs to the tree structure
		Composite buttons = toolkit.getFormToolkit().createComposite(structureComposite);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		GridLayout buttonLayout = new GridLayout(1, true);
		buttons.setLayout(buttonLayout);
		addButton = toolkit.createButton(buttons, Messages.TestPolicyCmptTypeSection_buttonAdd);
		removeButton = toolkit.createButton(buttons, Messages.TestPolicyCmptTypeSection_buttonRemove);
		hookButtonListeners();
		addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Details section
		prevTestPolicyCmpt = new ArrayList();
		
		testCaseDetailArea = new TestCaseDetailArea(toolkit, contentProvider, this);
		testCaseDetailArea.createDetailArea(client, sectionDetailTitle);
		
		testCaseDetailArea.createDynamicDetailSection(prevTestPolicyCmpt);
		prevIsValue = false;
		
	}


	/**
	 * Creates the tool bar actions.
	 */
	private void createToolBarActions() {
		// Toolbar item show without relation
		Action actionRelation = new Action("withoutRelation", Action.AS_CHECK_BOX) { //$NON-NLS-1$
			public void run() {
				showRelationsClicked();
			}
		};
		actionRelation.setChecked(false);
		actionRelation.setToolTipText("Without relation"); //$NON-NLS-1$
		actionRelation.setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("ShowRelationTypeNodes.gif"));
		
		// Toolbar item show all
		Action actionAll = new Action("structureAll", Action.AS_CHECK_BOX) { //$NON-NLS-1$
			public void run() {
				showAllClicked();
			}
		};
		actionAll.setChecked(false);
		actionAll.setToolTipText("Show flat structure"); //$NON-NLS-1$
		actionAll.setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("TestCase_flatView.gif"));
		form.getToolBarManager().add(actionRelation);
		form.getToolBarManager().add(actionAll);
	}
	
	/**
	 * Select the given tree entry and display the details of the selected element.
	 */
	void selectAndDisplayDetailsOfTreeEntry(String target) throws CoreException {
		Tree tree = treeViewer.getTree();
		TreeItem found = searchChilds(null, target, tree.getItems());
		if (found != null) {
			// show details of selected tree entry
			if (!showAll && found.getData() instanceof ITestPolicyCmpt) {
				ArrayList selEntry = new ArrayList(1);
				selEntry.add((ITestPolicyCmpt) found.getData());
				
				testCaseDetailArea.clearDetailArea();				
				testCaseDetailArea.createDynamicDetailSection(selEntry);
				prevTestPolicyCmpt = selEntry;
				prevIsValue = false;			
				
				redrawForm();
			}
			// select the tree entry
			TreeItem[] select = new TreeItem[1];
			select[0] = found;
			tree.setSelection(select);
			tree.setFocus();
			selectionInTree(new StructuredSelection(found.getData()));
			String uniqueKey = getUniqueKey(found.getData());
			
			EditField firstField = testCaseDetailArea.getAttributeEditField(uniqueKey);
			if (firstField != null){
				firstField.getControl().setFocus();
			}
		}
	}
	
	/**
	 * Recursive search the given childs for the given target object.
	 */
	private TreeItem searchChilds(ITestPolicyCmpt testPolicyCmpt, String label, TreeItem[] childs) {
		for (int i = 0; i < childs.length; i++) {
			TreeItem currItem = childs[i];
			if (testPolicyCmpt != null){
				if (currItem.getData() instanceof ITestPolicyCmpt){
					ITestPolicyCmpt elem = (ITestPolicyCmpt) currItem.getData();
					if (elem == testPolicyCmpt)
						return currItem;
				}
			}else{
				if (currItem.getText().equals(label) || currItem.getText().equals(label + " (P)"))
					return currItem;
			}
			currItem = searchChilds(testPolicyCmpt, label, currItem.getItems());
			if (currItem != null)
				return currItem;
		}
		return null;
	}

	/**
	 * Select the given object in the tree.
	 */
	private void selectionInTree(IStructuredSelection selection) {
		updateButtonEnableState(selection.getFirstElement());		
		if (!showAll){
			List testPolicyCmpts = new ArrayList();
			for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
				Object domainObject = iterator.next();
				if (domainObject instanceof ITestValue){
					prevTestPolicyCmpt = testPolicyCmpts;
					if (!prevIsValue){
						prevIsValue = true;
						testCaseDetailArea.clearDetailArea();
						testCaseDetailArea.createDynamicDetailSectionValues();
						redrawForm();
					}
				} else {
					ITestPolicyCmpt testPolicyCmpt = getTestPolicyCmpFromDomainObject(domainObject);
					// add the element in selected root element list only once
					ITestPolicyCmpt rootElem = testPolicyCmpt.getRoot();
					boolean exists = false;
					for (Iterator iter = testPolicyCmpts.iterator(); iter.hasNext();) {
						if (((ITestPolicyCmpt) iter.next()).equals(rootElem)){
							exists = true;
							break;
						}
					}
					if (!exists)
						testPolicyCmpts.add(rootElem);
				}
			}
			
			//	if the selection has changed redraw the detail section
			if (testPolicyCmpts.size() > 0 && ! testPolicyCmpts.equals(prevTestPolicyCmpt)){
				testCaseDetailArea.clearDetailArea();
				testCaseDetailArea.createDynamicDetailSection(testPolicyCmpts);
				prevTestPolicyCmpt = testPolicyCmpts;
				prevIsValue = false;		
				
				redrawForm();
			}
		}
		Object selected = selection.getFirstElement();
		setLinkCtrlFocus(selected, false);
	}
	
	/**
	 * Update the button state depending on the given object.
	 */
	private void updateButtonEnableState(Object selection) {
		if (selection instanceof TestCaseTypeRelation){
			TestCaseTypeRelation relation = (TestCaseTypeRelation) selection;
			try {
				IRelation modelRelation = null;
				if ( relation != null){
					modelRelation = relation.findRelation();
				}
				
				if (modelRelation == null){
					// failure in test case type definition
					// test case type or model relation not found
					// no add and removed allowed, because the test case type definition is wrong
					addButton.setEnabled(false);
					removeButton.setEnabled(false);
				}else{
					addButton.setEnabled(true);
					removeButton.setEnabled(false);
				}
			} catch (CoreException e) {
				// disable add and enable remove button and ignore exception
				// maybe the test case type model and test case are inconsistence
				// in this case the whole relation could be deleted but no new childs could be added
				addButton.setEnabled(false);
				removeButton.setEnabled(true);
				return;
			}
		}else if (selection instanceof ITestPolicyCmpt){
			ITestPolicyCmpt testPolicyCmpt = (ITestPolicyCmpt) selection;
			try {
				if (testPolicyCmpt.findTestPolicyCmptType() != null){
					// root elements could not be deleted
					removeButton.setEnabled(!((ITestPolicyCmpt)selection).isRoot());
					addButton.setEnabled(false);
				}
			} catch (CoreException e) {
				// disable add and remove button and ignore exception
				// maybe the test case type model and test case are inconsistence
				// in this case the parent relation could be removed but not this child element
				addButton.setEnabled(false);
				removeButton.setEnabled(false);
				return;
			}
		}else{
			// no relation or policy componet selected
			// disable the buttons
			removeButton.setEnabled(false);
			addButton.setEnabled(false);
		}
	}
	
	/**
	 * Add the tree listener to the tree.
	 */
	protected void hookTreeListeners() {
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					selectionInTree(selection);				
				}
			}
		});
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (!(event.getSelection() instanceof IStructuredSelection)) {
					return;
				}
				Object selected = ((IStructuredSelection)event.getSelection()).getFirstElement();
				isDoubleClicked = true;
				
				setLinkCtrlFocus(selected, true);
			}
		});
	}

	/**
	 * Select the given object in the detail area, change the color of the section.
	 * 
	 * @param selected The object which will be searched in the detail area.
	 * @param withFocusChange If <code>true</code> also the focus will be set to the first edit field in the
	 *                        found section. If <code>false</code> no focus will be moved. 
	 */
	private void setLinkCtrlFocus(Object selected, boolean withFocusChange){
		String uniquePath="";
		testCaseDetailArea.resetSectionColors(form);
		if(selected instanceof ITestValue){
			uniquePath = ((ITestValue) selected).getTestValueParameter();
			if (withFocusChange){
				EditField valueTextCtrl = testCaseDetailArea.getTestValueEditField(uniquePath);
				if (valueTextCtrl != null){
					isDoubleClicked = true;
					valueTextCtrl.getControl().setFocus();
				}
			}
			Section sectionCtrl = testCaseDetailArea.getSection(VALUESECTION);
			if (sectionCtrl != null){
				sectionCtrl.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
			}				
			return;
		}else{
			uniquePath = getUniqueKey(selected);
		}
		if (uniquePath.length() > 0 ){
			if (withFocusChange){
				EditField firstField = testCaseDetailArea.getAttributeEditField(uniquePath);
				if (firstField != null){
					firstField.getControl().setFocus();
				}
			}
			Section sectionCtrl = testCaseDetailArea.getSection(uniquePath);
			if (sectionCtrl != null){
				sectionCtrl.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
			}
		}
	}

	/**
	 * Gets the unique key of the given object
	 */
	String getUniqueKey(Object selected){
		String uniquePath = "";
		if (selected instanceof ITestPolicyCmptRelation){
			ITestPolicyCmptRelation relation = (ITestPolicyCmptRelation) selected;
			uniquePath = "." + relation.getTestPolicyCmptType() + relation.getId();
		}
		ITestPolicyCmpt currTestPolicyCmpt = getTestPolicyCmpFromDomainObject(selected);
		if (currTestPolicyCmpt == null){
			return "";
		}
		if (!currTestPolicyCmpt.isRoot()){
			uniquePath = ((ITestPolicyCmpt)currTestPolicyCmpt).getLabel();
			while (!currTestPolicyCmpt.isRoot()){
				uniquePath = ((ITestPolicyCmptRelation)currTestPolicyCmpt.getParent()).getTestPolicyCmptType() + uniquePath;
				currTestPolicyCmpt = getTestPolicyCmpFromDomainObject(currTestPolicyCmpt.getParent());
				uniquePath = currTestPolicyCmpt.getLabel() + "." + uniquePath;
			}
		}else{
			uniquePath = currTestPolicyCmpt.getLabel() + uniquePath;
		}
		return uniquePath;
	}
    
	/**
	 * Adds the button listener to the add and remove button.
	 */
	private void hookButtonListeners() {
		addButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				try {
					addClicked();
				} catch (Exception ex) {
					IpsPlugin.logAndShowErrorDialog(ex);
				}
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		removeButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				try {
					removeClicked();
				} catch (Exception ex) {
					IpsPlugin.logAndShowErrorDialog(ex);
				}
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	
	/**
	 * Show relations toolbar button was clicked.
	 */
	private void showRelationsClicked() {
		contentProvider.setWithoutRelations(!contentProvider.isWithoutRelations());
		treeViewer.refresh();
	}

	/**
	 * Add button was clicked.
	 */
	private void addClicked() {
		try {
			Object selectedObject = getFirstSelectedObjectFromTree();
			if (selectedObject instanceof TestCaseTypeRelation){
				// add a new child depending on the relation which was clicked
				TestCaseTypeRelation dummyRelation = (TestCaseTypeRelation) selectedObject;
				
				String productCmpt = "";
				if (dummyRelation.isRequiresProductCmpt()){
					productCmpt = selectProductCmptDialog(dummyRelation.getPolicyCmptTypeTarget());
					if (productCmpt == null)
						// chancel
						return;
				}
				
				IRelation relation = dummyRelation.findRelation();
				if (relation == null){
					// relation not found, no add allowed
					return;
				}
				
				if (relation.isAssoziation()){
					String targetName = "";
					ITestPolicyCmpt selectedTarget = selectAssoziationByTreeDialog(relation.getTarget());
					if (selectedTarget == null)
						// chancel
						return;
					
					TestCaseHierarchyPath path = new TestCaseHierarchyPath(selectedTarget, true);
					
					targetName = path.getHierarchyPath();
					
					
					// add a new child based on the selected relation and selected target
					ITestPolicyCmptRelation newRelation = dummyRelation.getParentTestPolicyCmpt().addTestPcTypeRelation(dummyRelation, productCmpt, targetName);
					ITestPolicyCmpt newTestPolicyCmpt = newRelation.findTarget();
					if (newTestPolicyCmpt != null){
						refreshTreeAndDetailArea();
						selectInTree(newTestPolicyCmpt, "");
					}
				}else{
					// add a new child based on the selected relation
					ITestPolicyCmptRelation newRelation = dummyRelation.getParentTestPolicyCmpt().addTestPcTypeRelation(dummyRelation, productCmpt, "");
					if (newRelation != null){
						ITestPolicyCmpt newTestPolicyCmpt = newRelation.findTarget();
						contentProvider.generateUniqueLabelOfTestPolicyCmpt(newTestPolicyCmpt);						
						refreshTreeAndDetailArea();
						selectInTree(newTestPolicyCmpt, "");
					}else{
						throw new CoreException(new IpsStatus("Error creating relation"));
					}
				}
			} else if (selectedObject instanceof ITestPolicyCmpt){
				// the relation level is not visible
				// open a dialog to ask for the type of relation 
				// if there are differet types configured in the test case type
				// e.g. a) association, b) composition, or c) composition with required product component
				// TODO Joerg: dialog relation type select
			} else {
				return;
			}

		} catch (CoreException e) {
			IpsPlugin.logAndShowErrorDialog(e);
		}
	}

	/**
	 * Remove button was clicked.
	 */
	private void removeClicked() {
		ISelection selection = treeViewer.getSelection();
		if (selection instanceof IStructuredSelection){
			ITestPolicyCmpt prevTestPolicyCmpt = null;
			String prevRelation = "";
			for (Iterator iterator = ((IStructuredSelection)selection).iterator(); iterator.hasNext();) {			
				Object domainObject = iterator.next();
				if (domainObject instanceof ITestPolicyCmptRelation){
					ITestPolicyCmpt parent = (ITestPolicyCmpt) ((ITestPolicyCmptRelation) domainObject).getParent();
					parent.removeRelation((ITestPolicyCmptRelation) domainObject);
				} else if (domainObject instanceof ITestPolicyCmpt) {
					ITestPolicyCmpt testPolicyCmpt = (ITestPolicyCmpt) domainObject;
					try {
						if (! testPolicyCmpt.isRoot()){ 
							// find the previous to select after delete
							ITestPolicyCmptRelation parentRelation = (ITestPolicyCmptRelation) testPolicyCmpt.getParent();
							prevRelation = parentRelation.getTestPolicyCmptType();
							ITestPolicyCmpt parent = (ITestPolicyCmpt) parentRelation.getParent();
							ITestPolicyCmptRelation[] relations = parent.getTestPcTypeRelations(parentRelation.getTestPolicyCmptType());
							for (int i = 0; i < relations.length; i++) {
								if (relations[i] == parentRelation)
									break;
								prevTestPolicyCmpt = relations[i].findTarget();
							}
						}
						testCase.removeTestObject((ITestObject) domainObject);
					} catch (CoreException e) {
						IpsPlugin.logAndShowErrorDialog(e);
					}	
				}
				refreshTreeAndDetailArea();
				selectInTree(prevTestPolicyCmpt, prevRelation);
			}
		}
	}
	
	private void showAllClicked(){
		showAll = ! showAll;
		if (showAll){
			
			ArrayList allInputTestPolicyCmpts;
			testCaseDetailArea.clearDetailArea();
			ITestPolicyCmpt[] policyCmpts = contentProvider.getSortedPolicyCmpts();
			allInputTestPolicyCmpts = new ArrayList(policyCmpts.length);
			for (int i = 0; i < policyCmpts.length; i++) {
				allInputTestPolicyCmpts.add(policyCmpts[i]);
			}

			testCaseDetailArea.createDynamicDetailSection(allInputTestPolicyCmpts);
			prevTestPolicyCmpt = allInputTestPolicyCmpts;
			prevIsValue = false;
			testCaseDetailArea.createDynamicDetailSectionValues();
			
			redrawForm();
			
			prevIsValue = true;
			prevTestPolicyCmpt = allInputTestPolicyCmpts;
		}else{
			testCaseDetailArea.clearDetailArea();
			prevIsValue = false;
			prevTestPolicyCmpt = new ArrayList();
			ISelection selection = treeViewer.getSelection();
			if (selection instanceof IStructuredSelection){
				Object domainObject = ((IStructuredSelection)selection).getFirstElement();
				if (domainObject instanceof ITestValue){
					testCaseDetailArea.createDynamicDetailSectionValues();
				} else {
					ITestPolicyCmpt testPolicyCmpt = getTestPolicyCmpFromDomainObject(domainObject);
					ArrayList list = new ArrayList();
					list.add(testPolicyCmpt);
					
					testCaseDetailArea.createDynamicDetailSection(list);
					prevTestPolicyCmpt = list;
					prevIsValue = false;		
				}
			}
			redrawForm();
		}
	}
	
	/**
	 * Select the given element in the tree. The element could either by identified by the given policy component
	 * or by the label
	 */
	private void selectInTree(ITestPolicyCmpt testPolicyCmpt, String label) {    	
		TreeItem found = searchChilds(testPolicyCmpt, label, treeViewer.getTree().getItems());
    	if (found != null) {
			// select the tree entry
			TreeItem[] select = new TreeItem[1];
			select[0] = found;
			treeViewer.getTree().setSelection(select);
			treeViewer.getTree().setFocus();
			updateButtonEnableState(found.getData());
    	}
	}

	/**
	 * Shows the select product component dialog and returns the selected product component.
	 * Returns <code>null</code> if no selection or an unsupported type was chosen.
	 *  
	 * @throws CoreException If an error occurs
	 */
	private String selectProductCmptDialog(String qualifiedTypeName) throws CoreException {
	    String productCmpt = null;
		
	    PdObjectSelectionDialog dialog = new PdObjectSelectionDialog(getShell(), "Product Component Selection", 
        		"Select a component (?=any character, *=any string)");
        dialog.setElements(getProductCmptObjects(qualifiedTypeName));
        if (dialog.open()==Window.OK) {
            if (dialog.getResult().length>0) {
                IIpsObject pdObject = (IIpsObject)dialog.getResult()[0];
                return pdObject.getQualifiedName();
            }
        }

	    return productCmpt;
	}
	
	/**
	 * Returns all product component objects in the model which are related to the given type name.
	 * 
	 * @throws CoreException If an error occurs
	 */
    private IIpsObject[] getProductCmptObjects(String qualifiedTypeName) throws CoreException {
    	IProductCmpt[] cmpts = testCase.getIpsProject().findProductCmpts(qualifiedTypeName, true);
    	List cmptList = new ArrayList();
    	cmptList.addAll(Arrays.asList(cmpts));
        return (IIpsObject[])cmptList.toArray(new IIpsObject[cmptList.size()]);
    }
    

	/**
	 * Returns the first selected object in the tree.
	 */
	private Object getFirstSelectedObjectFromTree(){
		ISelection selection = treeViewer.getSelection();
		if (selection instanceof IStructuredSelection){
			Object domainObject = ((IStructuredSelection) selection).getFirstElement();
			if (domainObject instanceof ITestPolicyCmpt){
				return getTestPolicyCmpFromDomainObject(domainObject);
			}else if (domainObject instanceof ITestPolicyCmptRelation) {
				return (ITestPolicyCmptRelation) domainObject;
			}else if (domainObject instanceof TestCaseTypeRelation) {
				return (TestCaseTypeRelation) domainObject;
			}
		}
		return null;
	}
	
	/**
	 * Refresh the tree and form.
	 */
	private void refreshTreeAndDetailArea(){
		form.setRedraw(false);
		treeViewer.refresh();
		treeViewer.expandAll();
		treeViewer.collapseAll();
		testCaseDetailArea.clearDetailArea();
		testCaseDetailArea.createDynamicDetailSection(prevTestPolicyCmpt);
		prevIsValue = false;		
		redrawForm();
		form.setRedraw(true);
	}
	
	/**
	 * (@inheritDoc)
	 */
	protected void performRefresh() {
	}
	
	/**
	 * Returns the corresponding test policy component object from the domain object.
	 * The domain object could either be a policy component object or a relation object.
	 * In case of a relation object the parent test policy component will be returned.
	 * Returns <code>null</code> ff the domainObject is not such a kind of object, in this
	 * case additionally an error will be logged.
	 */
	private ITestPolicyCmpt getTestPolicyCmpFromDomainObject(Object domainObject) {
		ITestPolicyCmpt testPolicyCmpt = null;
		if (domainObject instanceof ITestPolicyCmpt) {
			testPolicyCmpt = (ITestPolicyCmpt) domainObject;
		} else if (domainObject instanceof ITestPolicyCmptRelation) {
			ITestPolicyCmptRelation testPcTypeRelation = (ITestPolicyCmptRelation) domainObject;
			testPolicyCmpt = (ITestPolicyCmpt) testPcTypeRelation.getParent();
		} else if (domainObject instanceof TestCaseTypeRelation){
			testPolicyCmpt = ((TestCaseTypeRelation) domainObject).getParentTestPolicyCmpt();		
		}
		return testPolicyCmpt;
	}
	
	/**
	 * Displays the tree select dialog and return the selected object.
	 * Returns <code>null</code> if no or a wrong object was chosen.
	 */
	private ITestPolicyCmpt selectAssoziationByTreeDialog(String filteredPolicyCmptType) throws CoreException {
		ITestPolicyCmpt testPolicyCmpt = null;
		TestCaseTreeSelectionDialog dialog = new TestCaseTreeSelectionDialog(getShell(), toolkit, testCase, TestCaseContentProvider.TYPE_INPUT, filteredPolicyCmptType);
        if (dialog.open()==Window.OK) {
            if (dialog.getResult().length>0) {
            	ITestPolicyCmptTypeParameter param = null;
            	if (dialog.getResult()[0] instanceof ITestPolicyCmpt){
	            	testPolicyCmpt = (ITestPolicyCmpt) dialog.getResult()[0];	
	        		try {
	        			param = (ITestPolicyCmptTypeParameter) testPolicyCmpt.findTestPolicyCmptType();
	        		} catch (CoreException e) {
	        			// ignored exception and don't return the element
	        		}
            	}
            	if (param == null || ! param.getPolicyCmptType().equals(filteredPolicyCmptType)){
            		testPolicyCmpt = null;
                	// TODO Joerg: Errormsg not not allowed ...
                }
            }
        }
        return testPolicyCmpt;
	}

	/**
	 * Select the given test policy component in the tree.
	 */
	void selectInTree(ITestPolicyCmpt testPolicyCmpt) {
		if (!isDoubleClicked){
        	setLinkCtrlFocus(testPolicyCmpt, false);
        	// goto the corresponding test policy component in the tree
    		Tree tree = treeViewer.getTree();
        	TreeItem found = searchChilds(testPolicyCmpt, "", tree.getItems());
        	if (found != null) {
    			// select the tree entry
    			TreeItem[] select = new TreeItem[1];
    			select[0] = found;
    			tree.setSelection(select);
    		}
        }else{
        	isDoubleClicked = false;
        	setLinkCtrlFocus(testPolicyCmpt, true);
        }
	}
	
	/**
	 * Redraws the form.
	 */
	private void redrawForm() {
		// redraw the form
		pack();
		getParent().layout(true);
        form.reflow(true);
	}
	
	/**
	 * Selects the given test value in the tree.
	 */
	void selectTestValueInTree(ITestValue testValue){
		if (!isDoubleClicked){
    		setLinkCtrlFocus(testValue, false);
    		// goto the corresponding value object in the tree
    		Tree tree = treeViewer.getTree();
        	TreeItem found = searchChilds(null, testValue.getTestValueParameter(), tree.getItems());
        	if (found != null) {
    			// select the tree entry
    			TreeItem[] select = new TreeItem[1];
    			select[0] = found;
    			tree.setSelection(select);
    		}
        }else{
        	isDoubleClicked = false;
        	setLinkCtrlFocus(testValue, true);
        }
	}
}
