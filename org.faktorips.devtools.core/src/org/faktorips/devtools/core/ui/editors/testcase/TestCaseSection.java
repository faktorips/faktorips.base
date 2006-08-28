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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.Validatable;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.testcase.IIpsTestRunListener;
import org.faktorips.devtools.core.model.testcase.IIpsTestRunner;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
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
import org.faktorips.devtools.core.ui.views.testrunner.IpsTestRunnerViewPart;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.MessageList;

/**
 * Section to show the test case.
 */
public class TestCaseSection extends IpsSection implements IIpsTestRunListener {
	private static final String VALUESECTION = "VALUESECTION"; //$NON-NLS-1$
	public static final String REQUIRES_PRODUCT_CMPT_SUFFIX = " (P)"; //$NON-NLS-1$
	
	// The treeview which displays all test policy components which are available in this test
	private TreeViewer treeViewer;

	// Contains the test case which is displayed in this section
	private ITestCase testCase;
	
	// Contains the test case editor
	private TestCaseEditor editor;
	
	// UI toolkit for creating the controls
	private UIToolkit toolkit;

	// Buttons
	private Button addButton;
	private Button removeButton;
	private Button productCmptButton;
	
	// Title of the test case tree structure section
    private String sectionTreeStructureTitle;
    
    // Title of the detail section
    private String sectionDetailTitle;	
    
	// Contains the content provider of the test policy component object
	private TestCaseContentProvider contentProvider;

	// The deatil area of the test case
	private TestCaseDetailArea testCaseDetailArea;
	
	// Previous selected entries in the tree to
	private List prevTestPolicyCmpt;
	boolean prevIsValue;
	
	// The form which contains this section
	private ScrolledForm form;
	
	// Indicates if all objects of the test case are displayed or 
	// the child objects of selected root are visible
	boolean showAll = false;
	
	// Indicates if the tree selection was doubleclicked
	private boolean isDoubleClicked = false;

	// Actions
	private Action actionAll;
	
	// Stores the last test run status
	private boolean isTestRunError;
	private boolean isTestRunFailure;
	private int failureCount;
	
	public TestCaseSection(Composite parent, TestCaseEditor editor,
			UIToolkit toolkit, TestCaseContentProvider contentProvider, 
			String title, String detailTitle, 
			ScrolledForm form) {
		super(parent, Section.NO_TITLE, GridData.FILL_BOTH, toolkit);
		this.editor = editor;
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
		
        // add listener to test runner
		IpsPlugin.getDefault().getIpsTestRunner().addIpsTestRunListener(this);
        
        // add listener on model, 
        //   if the model changed reset the test run status
        testCase.getIpsModel().addChangeListener(new ContentsChangeListener(){
            public void contentsChanged(ContentChangeEvent event) {
                if (event.getIpsSrcFile().equals(testCase.getIpsSrcFile())){
                    postResetTestRunStatus();
                }
            }   
        });        
	}
    
    /**
     * {@inheritDoc}
     */    
    public void dispose() {
        IpsPlugin.getDefault().getIpsTestRunner().removeIpsTestRunListener((IIpsTestRunListener) this);
        super.dispose();
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
		treeViewer.setLabelProvider(new TestCaseMessageCueLabelProvider(labelProvider, this));
		new TableMessageHoverService(treeViewer) {
            protected MessageList getMessagesFor(Object element) throws CoreException {
                if (element!=null){
                	return validateElement(element);
                } else
                	return null;
            }
		};
		treeViewer.setInput(testCase);

		// Buttons belongs to the tree structure
		Composite buttons = toolkit.getFormToolkit().createComposite(structureComposite);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		GridLayout buttonLayout = new GridLayout(1, true);
		buttons.setLayout(buttonLayout);
		addButton = toolkit.createButton(buttons, Messages.TestCaseSection_ButtonAdd);
		removeButton = toolkit.createButton(buttons, Messages.TestCaseSection_ButtonRemove);
		productCmptButton = toolkit.createButton(buttons, Messages.TestCaseSection_ButtonProductCmpt);
		hookButtonListeners();
		addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		productCmptButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Details section
		prevTestPolicyCmpt = new ArrayList();
		
		testCaseDetailArea = new TestCaseDetailArea(toolkit, contentProvider, this);
		testCaseDetailArea.createInitialDetailArea(client, sectionDetailTitle);
		
		prevIsValue = false;
		refreshTree();
        
        updateButtonEnableState(null);
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
		actionRelation.setToolTipText(Messages.TestCaseSection_ToolBar_WithoutRelation);
		actionRelation.setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("ShowRelationTypeNodes.gif")); //$NON-NLS-1$
		
		// Toolbar item show all
		actionAll = new Action("structureAll", Action.AS_CHECK_BOX) { //$NON-NLS-1$
			public void run() {
				showAllClicked();
			}
		};
		actionAll.setChecked(false);
		actionAll.setToolTipText(Messages.TestCaseSection_ToolBar_FlatStructure);
		actionAll.setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("TestCase_flatView.gif")); //$NON-NLS-1$
		
		// Toolbar item run test
		Action actionTest = new Action("runTest", Action.AS_PUSH_BUTTON) { //$NON-NLS-1$
			public void run() {
				runTestClicked();
			}
		};
		actionTest.setToolTipText(Messages.TestCaseSection_ToolBar_RunTest);
		actionTest.setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("TestCaseRun.gif")); //$NON-NLS-1$
        
        // enable run test case functionality only if a toc file exists for this test case
        try {
            actionTest.setEnabled(getTocFilePackage() != null);
        } catch (CoreException e) {
            actionTest.setEnabled(false);
        }
        
        addContentTypeAction();
        
        form.getToolBarManager().add(new Separator());
        
		form.getToolBarManager().add(actionRelation);
		form.getToolBarManager().add(actionAll);
		
        form.getToolBarManager().add(new Separator());
        
        form.getToolBarManager().add(actionTest);
        
	}
	
    /*
     * Adds the actions for the content type filter. 
     *
     */
    private void addContentTypeAction() {
        ToggleContentTypeAction[] fToggleContentTypeActions =
            new ToggleContentTypeAction[] {
                new ToggleContentTypeAction(TestCaseContentProvider.COMBINED),
                new ToggleContentTypeAction(TestCaseContentProvider.INPUT),
                new ToggleContentTypeAction(TestCaseContentProvider.EXPECTED_RESULT)};
        
        for (int i = 0; i < fToggleContentTypeActions.length; ++i)
            form.getToolBarManager().add(fToggleContentTypeActions[i]);
    }
    
    /*
     * Action which provides the content type filter.
     */
    private class ToggleContentTypeAction extends Action {
        private final int fActionContentType;
        
        public ToggleContentTypeAction(int actionContentType) {
            super("", AS_RADIO_BUTTON); //$NON-NLS-1$
            fActionContentType = actionContentType;
            if (actionContentType == TestCaseContentProvider.INPUT) {
                buttonChecked();
                setText(Messages.TestCaseSection_FilterInput);
                setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("TestCaseInput.gif")); //$NON-NLS-1$
                setToolTipText(Messages.TestCaseSection_FilterInput_ToolTip);
            } else if (actionContentType == TestCaseContentProvider.EXPECTED_RESULT) {
                buttonChecked();
                setText(Messages.TestCaseSection_FilterExpected); 
                setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("TestCaseExpResult.gif")); //$NON-NLS-1$
                setToolTipText(Messages.TestCaseSection_FilterExpected_ToolTip);
            } else if (actionContentType == TestCaseContentProvider.COMBINED) {     
                buttonChecked();
                setText(Messages.TestCaseSection_FilterCombined);  
                setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("TestCaseCombined.gif")); //$NON-NLS-1$
                setToolTipText(Messages.TestCaseSection_FilterCombined_ToolTip);
            }
        }
        
        public void run() {
            if (isChecked()) {
                switchContentType(fActionContentType);
            }
        }
        
        private void buttonChecked(){
            if (contentProvider.getContentType() == fActionContentType)
                setChecked(true);
        }
    }
    
    /*
     * Switch (filter) to the selected content type. And reresh the editor contents.
     */
    private void switchContentType(int contentType){
        contentProvider.setContentType(contentType);
        refreshTreeAndDetailArea();
    }
    
	/**
	 * Select the given tree entry and display the details of the selected element.
	 */
	void searchChildsByLabelPath(String target) throws CoreException {
		Tree tree = treeViewer.getTree();
		TreeItem found = searchChildsByLabel(target, tree.getItems());
		if (found != null) {
			// show details of selected tree entry
			if (!showAll && found.getData() instanceof ITestPolicyCmpt) {
				ArrayList selEntry = new ArrayList(1);
				selEntry.add((ITestPolicyCmpt) found.getData());
						
				testCaseDetailArea.createDetailSection(selEntry);
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
			
			EditField firstField = testCaseDetailArea.getFirstAttributeEditField(uniqueKey);
			if (firstField != null){
				firstField.getControl().setFocus();
			}
		}
	}
	
	/**
	 * Recursive search the given childs for the given target object.
	 */
	private TreeItem searchChildsByLabel(String labelPath, TreeItem[] childs) {
		for (int i = 0; i < childs.length; i++) {
			TreeItem currItem = childs[i];
				if (currItem.getText().equals(labelPath) || currItem.getText().equals(labelPath + REQUIRES_PRODUCT_CMPT_SUFFIX))
					return currItem;
			currItem = searchChildsByLabel(labelPath, currItem.getItems());
			if (currItem != null)
				return currItem;
		}
		return null;
	}

	/**
	 * Recursive search the given childs for the given target object.
	 */
	private TreeItem searchChildsByHierarchyPath(TestCaseHierarchyPath hierarchyPath, TreeItem[] childs) {
		if (! hierarchyPath.hasNext())
			return null;
		String currPathItem = hierarchyPath.next();
		TreeItem currItem = null;
		for (int i = 0; i < childs.length; i++) {
			currItem = childs[i];
			if (currItem.getText().equals(currPathItem) || currItem.getText().equals(currPathItem + REQUIRES_PRODUCT_CMPT_SUFFIX)){
				if (hierarchyPath.hasNext())
					currItem = searchChildsByHierarchyPath(hierarchyPath, currItem.getItems());
				break;
			}
		}
		return currItem;
	}
	
	/**
	 * Recursive search the given childs for the given target object.
	 */
	private TreeItem searchChildsByObject(ITestPolicyCmpt testPolicyCmpt, ITestPolicyCmptRelation relation, TreeItem[] childs) {
		if (testPolicyCmpt == null && relation == null)
			return null;
		
		for (int i = 0; i < childs.length; i++) {
			TreeItem currItem = childs[i];
			if (testPolicyCmpt != null && currItem.getData() instanceof ITestPolicyCmpt){
					ITestPolicyCmpt elem = (ITestPolicyCmpt) currItem.getData();
					if (elem == testPolicyCmpt)
						return currItem;
			}
			if (relation != null && currItem.getData() instanceof ITestPolicyCmptRelation){
				ITestPolicyCmptRelation elem = (ITestPolicyCmptRelation) currItem.getData();
				if (elem == relation)
					return currItem;
		}
			currItem = searchChildsByObject(testPolicyCmpt, relation, currItem.getItems());
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
						testCaseDetailArea.createValuesSection();
						redrawForm();
					}
				} else {
					ITestPolicyCmpt testPolicyCmpt = getTestPolicyCmpFromDomainObject(domainObject);
					// add the element in selected root element list only once
					ITestObject rootElem = testPolicyCmpt.getRoot();
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
				testCaseDetailArea.createDetailSection(testPolicyCmpts);
				prevTestPolicyCmpt = testPolicyCmpts;
				prevIsValue = false;		
				
				redrawForm();
			}
		}
		Object selected = selection.getFirstElement();
		selectInDetailArea(selected, false);
	}
	
	/**
	 * Update the button state depending on the given object.
	 */
	private void updateButtonEnableState(Object selection) {
		productCmptButton.setEnabled(false);
		removeButton.setEnabled(false);
		addButton.setEnabled(false);
		
        if (selection == null)
            return;
        
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
				}else{
					addButton.setEnabled(true);
					removeButton.setEnabled(false);
				}
			} catch (CoreException e) {
				// disable add and enable remove button and ignore exception
				// maybe the test case type model and test case are inconsistence
				// in this case the whole relation could be deleted but no new childs could be added
				removeButton.setEnabled(true);
				return;
			}
		}else if (selection instanceof ITestPolicyCmpt){
			ITestPolicyCmpt testPolicyCmpt = (ITestPolicyCmpt) selection;
			try {
				ITestPolicyCmptTypeParameter param = testPolicyCmpt.findTestPolicyCmptType();
				// root elements could not be deleted
				removeButton.setEnabled(!((ITestPolicyCmpt)selection).isRoot());	
				if (param != null){
					// type parameter exists,
					//   enable add button only if relations are defined
					addButton.setEnabled(param.getTestPolicyCmptTypeParamChilds().length>0);
					// product component select button is only enabled if the type parameter specified this
					productCmptButton.setEnabled(param.isRequiresProductCmpt());
				}
			} catch (CoreException e) {
				// disable add and remove button and ignore exception
				// maybe the test case type model and test case are inconsistence
				// in this case the parent relation could be removed but not this child element
			}
		}else if (selection instanceof ITestPolicyCmptRelation){
			// the relation object indicates, that the test policy type parameter for this relation
			// not exists, therefore only remove is enabled
			removeButton.setEnabled(true);
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
				
				// set focus to the first edit field in details area of the clicked element or
				// if an asszosiation was clicked set the focus to the entry in the tree
				if (selected instanceof ITestPolicyCmptRelation){
					// an assoziation relation was clicked
					ITestPolicyCmpt target = null;
					try {
						target = ((ITestPolicyCmptRelation) selected).findTarget();
					} catch (CoreException e) {
						// ignore exception, don't move the focus
					}
					if (target != null)
						selectInTreeByObject(target, true);
				} else {
					isDoubleClicked = true;
					selectInDetailArea(selected, true);
				}
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
	private void selectInDetailArea(Object selected, boolean withFocusChange){
		String uniquePath=""; //$NON-NLS-1$
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
			Section sectionCtrl = testCaseDetailArea.getSection(VALUESECTION + uniquePath);
			if (sectionCtrl != null){
				sectionCtrl.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
			}
			return;
		}else{
			uniquePath = getUniqueKey(selected);
		}
		if (uniquePath.length() > 0 ){
			if (withFocusChange){
				EditField firstField = testCaseDetailArea.getFirstAttributeEditField(uniquePath);
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
		String uniquePath = ""; //$NON-NLS-1$
		if (selected instanceof ITestPolicyCmptRelation){
			ITestPolicyCmptRelation relation = (ITestPolicyCmptRelation) selected;
			uniquePath = "." + relation.getTestPolicyCmptTypeParameter() + relation.getId(); //$NON-NLS-1$
		}
		ITestPolicyCmpt currTestPolicyCmpt = getTestPolicyCmpFromDomainObject(selected);
		if (currTestPolicyCmpt == null){
			return ""; //$NON-NLS-1$
		}
		if (!currTestPolicyCmpt.isRoot()){
			uniquePath = ((ITestPolicyCmpt)currTestPolicyCmpt).getName();
			while (!currTestPolicyCmpt.isRoot()){
				uniquePath = ((ITestPolicyCmptRelation)currTestPolicyCmpt.getParent()).getTestPolicyCmptTypeParameter() + uniquePath;
				currTestPolicyCmpt = getTestPolicyCmpFromDomainObject(currTestPolicyCmpt.getParent());
				uniquePath = currTestPolicyCmpt.getName() + "." + uniquePath; //$NON-NLS-1$
			}
		}else{
			uniquePath = currTestPolicyCmpt.getName() + uniquePath;
		}
		return uniquePath;
	}
    
	/**
	 * Gets the unique key for the given test policy component and arrtibute
	 */
	String getUniqueKey(ITestPolicyCmpt testPolicyCmpt, ITestAttributeValue attributeValue) {
		return getUniqueKey(testPolicyCmpt) + "/" + attributeValue.getTestAttribute(); //$NON-NLS-1$
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
		productCmptButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				try {
					productCmptClicked();
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
		refreshTreeAndDetailArea();
	}

	/**
	 * Add button was clicked.
	 */
	private void addClicked() {
		try {
			Object selectedObject = getFirstSelectedObjectFromTree();
			if (selectedObject instanceof TestCaseTypeRelation){
				// add a new child depending on the relation which was clicked
				TestCaseTypeRelation relationType = (TestCaseTypeRelation) selectedObject;
				addRelation(relationType);
			} else if (selectedObject instanceof ITestPolicyCmpt){
				// open a dialog to ask for the type of relation which 
				// are defined in the test case type parameter if more than one role defined
				ITestPolicyCmpt testPolicyCmpt = (ITestPolicyCmpt) selectedObject;
				TestCaseTypeRelation relationType = selectTestCaseTypeRelationByDialog(testPolicyCmpt);
				if (relationType != null)
					addRelation(relationType);
			} else{
				return;
			}
		} catch (CoreException e) {
			IpsPlugin.logAndShowErrorDialog(e);
		}
	}
	
	/**
	 * Add a new relation based an the given test case type relation.
	 */
	private void addRelation(TestCaseTypeRelation relationType) throws CoreException{
		String productCmpt = ""; //$NON-NLS-1$
		if (relationType.isRequiresProductCmpt()){
			productCmpt = selectProductCmptDialog(relationType.getPolicyCmptTypeTarget());
			if (productCmpt == null)
				// chancel
				return;
		}
		
		IRelation relation = relationType.findRelation();
		if (relation == null){
			// relation not found, no add allowed
			return;
		}
		
		// add a new child based on the selected relation
		if (relation.isAssoziation()){
			// assoziation relation will be added			
			String targetName = ""; //$NON-NLS-1$
			ITestPolicyCmpt selectedTarget = selectAssoziationByTreeDialog(relation.getTarget());
			if (selectedTarget == null)
				// chancel
				return;
			
			TestCaseHierarchyPath path = new TestCaseHierarchyPath(selectedTarget);
			
			targetName = path.getHierarchyPath();
			
			
			// add a new child based on the selected relation and selected target
			ITestPolicyCmptRelation newRelation = relationType.getParentTestPolicyCmpt().
				addTestPcTypeRelation(relationType.getTestPolicyCmptTypeParam(), productCmpt, targetName);
			ITestPolicyCmpt newTestPolicyCmpt = newRelation.findTarget();
			if (newTestPolicyCmpt == null){
				throw new CoreException(new IpsStatus(Messages.TestCaseSection_Error_CreatingRelation));
			}
			
			refreshTreeAndDetailArea();
			selectInTreeByObject(newRelation, true);
		}else{
			// composition relatation will be added	
			ITestPolicyCmptRelation newRelation = relationType.getParentTestPolicyCmpt().
				addTestPcTypeRelation(relationType.getTestPolicyCmptTypeParam(), productCmpt, ""); //$NON-NLS-1$
			if (newRelation == null)
				throw new CoreException(new IpsStatus(Messages.TestCaseSection_Error_CreatingRelation));
				
			ITestPolicyCmpt newTestPolicyCmpt = newRelation.findTarget();	
			if (newTestPolicyCmpt == null)
				throw new CoreException(new IpsStatus(Messages.TestCaseSection_Error_CreatingRelation));				
			
			refreshTreeAndDetailArea();
			selectInTreeByObject(newTestPolicyCmpt, true);
		}		
	}
	
	/**
	 * Remove button was clicked.
	 */
	private void removeClicked() throws CoreException {
		ISelection selection = treeViewer.getSelection();
		if (selection instanceof IStructuredSelection){
			ITestPolicyCmpt prevTestPolicyCmpt = null;
			ITestPolicyCmptRelation prevRelationObj = null;
			String prevRelation = ""; //$NON-NLS-1$
			for (Iterator iterator = ((IStructuredSelection)selection).iterator(); iterator.hasNext();) {			
				Object domainObject = iterator.next();
				if (domainObject instanceof ITestPolicyCmptRelation){
					ITestPolicyCmptRelation relation = (ITestPolicyCmptRelation) domainObject;
					ITestPolicyCmpt parent = (ITestPolicyCmpt) relation.getParent();

					prevRelation = new TestCaseHierarchyPath(relation, true).getHierarchyPath();
					ITestPolicyCmptRelation[] relations = parent.getTestPolicyCmptRelations(relation.getTestPolicyCmptTypeParameter());
					for (int i = 0; i < relations.length; i++) {
						if (relations[i] == relation)
							break;
						prevRelationObj = relations[i];
					}

					parent.removeRelation((ITestPolicyCmptRelation) domainObject);
				} else if (domainObject instanceof ITestPolicyCmpt) {
					ITestPolicyCmpt testPolicyCmpt = (ITestPolicyCmpt) domainObject;
					if (! testPolicyCmpt.isRoot()){ 
						// find the the previous to select it after delete
						ITestPolicyCmptRelation parentRelation = (ITestPolicyCmptRelation) testPolicyCmpt.getParent();
						prevRelation = new TestCaseHierarchyPath(parentRelation, true).getHierarchyPath();
						ITestPolicyCmpt parent = (ITestPolicyCmpt) parentRelation.getParent();
						ITestPolicyCmptRelation[] relations = parent.getTestPolicyCmptRelations(parentRelation.getTestPolicyCmptTypeParameter());
						for (int i = 0; i < relations.length; i++) {
							if (relations[i] == parentRelation)
								break;
							prevTestPolicyCmpt = relations[i].findTarget();
						}
					}
					testCase.removeTestObject((ITestObject) domainObject);
				}
				refreshTreeAndDetailArea();
				if (prevTestPolicyCmpt != null)
					selectInTreeByObject(prevTestPolicyCmpt, true);
				else if (prevRelationObj !=  null)
					selectInTreeByObject(prevRelationObj, true);
				else
					selectInTreeByLabelPath(prevRelation);
			}
		}
	}
	
	private void productCmptClicked() throws CoreException {
		ISelection selection = treeViewer.getSelection();
		if (selection instanceof IStructuredSelection){
			Object selectedObj = ((IStructuredSelection)selection).getFirstElement();
			if (selectedObj instanceof ITestPolicyCmpt){
				ITestPolicyCmpt testPolicyCmpt = (ITestPolicyCmpt) selectedObj;
				ITestPolicyCmptTypeParameter testTypeParam;
				try {
					testTypeParam = testPolicyCmpt.findTestPolicyCmptType();
				} catch (CoreException e) {
					// ignored, the validation shows the unknown type failure message
					return;
				}
				String productCmpt = ""; //$NON-NLS-1$
				if (testTypeParam.isRequiresProductCmpt()){
					productCmpt = selectProductCmptDialog(testTypeParam.getPolicyCmptType());
					if (productCmpt == null)
						// chancel
						return;
				}
				testPolicyCmpt.setProductCmpt(productCmpt);
				testPolicyCmpt.setName(
						testCase.generateUniqueLabelForTestPolicyCmpt(testPolicyCmpt, StringUtil.unqualifiedName(productCmpt)));
				refreshTreeAndDetailArea();
			}
		}
	}
	
	private void showAllClicked(){
		showAll(! showAll);
	}
	
	private void showAll(boolean showAll){
		this.showAll = showAll;
		if (showAll){
			ArrayList allInputTestPolicyCmpts;
			ITestPolicyCmpt[] policyCmpts = contentProvider.getPolicyCmpts();
			allInputTestPolicyCmpts = new ArrayList(policyCmpts.length);
			for (int i = 0; i < policyCmpts.length; i++) {
				allInputTestPolicyCmpts.add(policyCmpts[i]);
			}

            testCaseDetailArea.clearDetailArea();
			testCaseDetailArea.createValuesSection();
			testCaseDetailArea.createDetailSection(allInputTestPolicyCmpts);
			prevIsValue = true;
			prevTestPolicyCmpt = allInputTestPolicyCmpts;
            treeViewer.expandAll();
			redrawForm();
		}else{
			prevIsValue = false;
			prevTestPolicyCmpt = new ArrayList();
			ISelection selection = treeViewer.getSelection();
			if (selection instanceof IStructuredSelection){
				Object domainObject = ((IStructuredSelection)selection).getFirstElement();
				if (domainObject instanceof ITestValue){
                    testCaseDetailArea.clearDetailArea();
					testCaseDetailArea.createValuesSection();
				} else {
					ITestPolicyCmpt testPolicyCmpt = (ITestPolicyCmpt) getTestPolicyCmpFromDomainObject(domainObject).getRoot();
					ArrayList list = new ArrayList();
					list.add(testPolicyCmpt);
					
                    testCaseDetailArea.clearDetailArea();
					testCaseDetailArea.createDetailSection(list);
					prevTestPolicyCmpt = list;
					prevIsValue = false;		
			}
			redrawForm();
            }
        }
	}

	private void runTestClicked(){
        try {
            // check if the file is dirty
            if (testCase.getIpsSrcFile().isDirty()) {
                String msg = Messages.TestCaseSection_Dialog_SaveBeforeStartTest;
                MessageDialog.openInformation(getShell(),
                        Messages.TestCaseSection_Dialog_SaveBeforeStartTest_Title, msg);
                return;
            }
            // show test runner view
			IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(IpsTestRunnerViewPart.EXTENSION_ID);
		} catch (Exception e) {
			IpsPlugin.logAndShowErrorDialog(e);
		}
		// run test test
		try {
			IIpsTestRunner testRunner = IpsPlugin.getDefault().getIpsTestRunner();
			testRunner.setJavaProject(testCase.getIpsProject().getJavaProject());
			String repositoryPackage = getTocFilePackage();
            if (repositoryPackage != null)
                testRunner.startTestRunnerJob(repositoryPackage, testCase.getQualifiedName());
		} catch (CoreException e) {
			IpsPlugin.logAndShowErrorDialog(e);
		}
	}
    
    /*
     * Returns the toc file package name which stores the current test case.
     */
    private String getTocFilePackage() throws CoreException {
        IIpsPackageFragmentRoot root = testCase.getIpsPackageFragment().getRoot();
        IIpsArtefactBuilderSet builderSet = root.getIpsProject().getArtefactBuilderSet();
        return builderSet.getTocFilePackageName(root);        
    }
    
	/**
	 * Shows the select product component dialog and returns the selected product component.
	 * Returns <code>null</code> if no selection or an unsupported type was chosen.
	 *  
	 * @throws CoreException If an error occurs
	 */
	private String selectProductCmptDialog(String qualifiedTypeName) throws CoreException {
	    PdObjectSelectionDialog dialog = new PdObjectSelectionDialog(getShell(), Messages.TestCaseSection_DialogSelectProductCmpt_Title, 
        		Messages.TestCaseSection_DialogSelectProductCmpt_Description);
        dialog.setElements(getProductCmptObjects(qualifiedTypeName));
        if (dialog.open()==Window.OK) {
            if (dialog.getResult().length>0) {
            	IProductCmpt productCmpt = (IProductCmpt) dialog.getResult()[0];
        		return productCmpt.getRuntimeId();
            }
        }
	    return null;
	}
	
	/**
	 * Returns all product component objects in the model which are related to the given type name.
	 * 
	 * @throws CoreException If an error occurs
	 */
    private IIpsObject[] getProductCmptObjects(String qualifiedTypeName) throws CoreException {
        List result = new ArrayList();
        getProductCmptObjects(testCase.getIpsProject(), qualifiedTypeName, result);
    	IProductCmpt[] cmpts = (IProductCmpt[]) result.toArray(new IProductCmpt[0]);
    	List cmptList = new ArrayList();
    	cmptList.addAll(Arrays.asList(cmpts));
        return (IIpsObject[])cmptList.toArray(new IIpsObject[cmptList.size()]);
    }

    /*
     * Adds all product components in the given project and referenced projects
     * with the given qualified name to the given result list.
     */
    private void getProductCmptObjects(IIpsProject ipsProject, String qualifiedTypeName, List result) throws CoreException {
        IProductCmpt[] cmpts = ipsProject.findProductCmpts(qualifiedTypeName, true);
        for (int i = 0; i < cmpts.length; i++) {
            result.add(cmpts[i]);
        }
        IIpsProject[] ipsProjects = ipsProject.getReferencedIpsProjects();
        for (int i = 0; i < ipsProjects.length; i++) {
            getProductCmptObjects(ipsProjects[i], qualifiedTypeName, result);
        }
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
		prevIsValue = false;
		form.setRedraw(false);
        testCaseDetailArea.clearDetailArea();
        if (showAll){
		    testCaseDetailArea.createValuesSection();
			testCaseDetailArea.createDetailSection(prevTestPolicyCmpt);
		} else {
			if (prevIsValue){
				testCaseDetailArea.createValuesSection();
			} else {
				testCaseDetailArea.createDetailSection(prevTestPolicyCmpt);
			}
		}
		refreshTree();
		redrawForm();
		form.setRedraw(true);
	}
	
	/**
	 * Refresh the tree.
	 */
	void refreshTree(){
		treeViewer.getTree().setRedraw(false);
		TreeViewerExpandStateStorage treeexpandStorage = new TreeViewerExpandStateStorage(treeViewer);
		treeexpandStorage.storeExpandedStatus();
		treeViewer.refresh();
		treeViewer.expandAll();
		treeViewer.collapseAll();
		treeexpandStorage.restoreExpandedStatus();
		treeViewer.getTree().setRedraw(true);
		
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

	void selectInTreeByObject(ITestPolicyCmpt testPolicyCmpt, boolean focusChange) {
		selectInTreeByObject(testPolicyCmpt, null, focusChange);
	}
	
	void selectInTreeByObject(ITestPolicyCmptRelation relation, boolean focusChange) {
		selectInTreeByObject(null, relation, focusChange);
	}
	
	/**
	 * Select the given test policy component in the tree.
	 */
	private void  selectInTreeByObject(ITestPolicyCmpt testPolicyCmpt, ITestPolicyCmptRelation relation, boolean focusChange) {
		if (!isDoubleClicked){
        	selectInDetailArea(testPolicyCmpt, false);
        	// goto the corresponding test policy component in the tree
    		Tree tree = treeViewer.getTree();
        	TreeItem found = searchChildsByObject(testPolicyCmpt, relation, tree.getItems());
        	if (found != null) {
        		// select the tree entry
    			TreeItem[] select = new TreeItem[1];
    			select[0] = found;
    			tree.setSelection(select);
    			if (focusChange){
    				treeViewer.getTree().setFocus();
    				updateButtonEnableState(found.getData()); 
    			}
    		}
        }else{
        	isDoubleClicked = false;
        	selectInDetailArea(testPolicyCmpt, true);
        }
	}
	
	/**
	 * Select the given test policy component in the tree.
	 */
	void selectInTreeByLabelPath(String label) {
		// goto the corresponding test policy component in the tree
		TestCaseHierarchyPath hierarchyPath = new TestCaseHierarchyPath(label);
		
		Tree tree = treeViewer.getTree();
		TreeItem found = null;
		if (hierarchyPath.count() > 1){
			found = searchChildsByHierarchyPath(hierarchyPath, tree.getItems());
		} else {
			found = searchChildsByLabel(label, tree.getItems());
		}
			if (found != null) {
			// select the tree entry
			TreeItem[] select = new TreeItem[1];
			select[0] = found;
			tree.setSelection(select);
			treeViewer.getTree().setFocus();
			updateButtonEnableState(found.getData());    			
		}
	}
	
	/**
	 * Selects the given test value in the tree.
	 */
	void selectTestValueInTree(ITestValue testValue){
		if (!isDoubleClicked){
    		selectInDetailArea(testValue, false);
    		// goto the corresponding value object in the tree
    		Tree tree = treeViewer.getTree();
        	TreeItem found = searchChildsByLabel(testValue.getTestValueParameter(), tree.getItems());
        	if (found != null) {
    			// select the tree entry
    			TreeItem[] select = new TreeItem[1];
    			select[0] = found;
    			tree.setSelection(select);
    		}
        }else{
        	isDoubleClicked = false;
        	selectInDetailArea(testValue, true);
        }
	}

	/**
	 * Redraws the form.
	 */
	private void redrawForm() {
		// redraw the form
		form.setRedraw(false);
		pack();
		getParent().layout(true);
        form.reflow(true);
        form.setRedraw(true);
	}
	
	/**
	 * Performs and returns validation messages on the parent of the given element,
	 * This function will be used to show the validation messages on the child element
	 * if the relation layer is hidden.
	 */
	MessageList validateParentElement(Object element) throws CoreException{
    	if (contentProvider.isWithoutRelations()){
			if (element instanceof ITestPolicyCmptRelation){
	    		ITestPolicyCmptRelation cmptRelation = (ITestPolicyCmptRelation) element;
	    		TestCaseTypeRelation dummyRelation = 
	    			new TestCaseTypeRelation(cmptRelation.findTestPolicyCmptTypeParameter(), (ITestPolicyCmpt)cmptRelation.getParent());
	    		return dummyRelation.validate();
	    	}else if (element instanceof ITestPolicyCmpt){
	    		ITestPolicyCmpt pc = (ITestPolicyCmpt) element;
	    		if (! pc.isRoot()){
		    		TestCaseTypeRelation dummyRelation = new TestCaseTypeRelation(pc.findTestPolicyCmptType(), pc.getParentPolicyCmpt());
		    		return dummyRelation.validate();
	    		}
	    	}
    	}
    	return null;
	}

	/**
	 * Performs and returns validation messages on the given element.
	 */
	MessageList validateElement(Object element) throws CoreException{
    	MessageList messageList = new MessageList();
    	// validate element
		if (element instanceof ITestPolicyCmptRelation){
			messageList.add(((ITestPolicyCmptRelation)element).validateSingle());
	    }else if (element instanceof ITestPolicyCmpt){
	    	messageList.add(((Validatable)element).validate());
    	}else if (element instanceof TestCaseTypeRelation){
    		messageList.add(((Validatable)element).validate());
    	}else if (element instanceof ITestValue){
    		messageList.add(((Validatable)element).validate());
    	}
		// if the relation level is hidden then validate the parent to display parent validation failures
		if (contentProvider.isWithoutRelations()){
			if (element instanceof ITestPolicyCmptRelation){
				messageList.add(((ITestPolicyCmptRelation)element).validateGroup());
	    	}else if (element instanceof ITestPolicyCmpt){
	    		ITestPolicyCmpt pc = (ITestPolicyCmpt) element;
	    		if (! pc.isRoot()){
		    		TestCaseTypeRelation dummyRelation = new TestCaseTypeRelation(pc.findTestPolicyCmptType(), pc.getParentPolicyCmpt());
		    		messageList.add(dummyRelation.validate());
	    		}
	    	}
		}
    	return messageList;
	}
	
	/**
	 * Displays the tree select dialog and return the selected object.
	 * Returns <code>null</code> if no or a wrong object was chosen or the user select nothing.
	 */
	private ITestPolicyCmpt selectAssoziationByTreeDialog(String filteredPolicyCmptType) throws CoreException {
		ITestPolicyCmpt testPolicyCmpt = null;
		TestPolicyCmptSelectionDialog dialog = new TestPolicyCmptSelectionDialog(getShell(), toolkit, testCase, TestCaseContentProvider.COMBINED, filteredPolicyCmptType);
        if (dialog.open()==Window.OK) {
            if (dialog.getResult().length>0) {
            	if (dialog.getResult()[0] instanceof ITestPolicyCmpt){
	            	testPolicyCmpt = (ITestPolicyCmpt) dialog.getResult()[0];	
            	}
            }
        }
        return testPolicyCmpt;
	}
	
	/**
	 * Displays a dialog to select the type definition of a test relation.
	 * Returns the selected test case type relation object or <code>null</code> if the user select nothing.
	 */
	private TestCaseTypeRelation selectTestCaseTypeRelationByDialog(ITestPolicyCmpt parentTestPolicyCmpt) throws CoreException {
		ElementListSelectionDialog selectDialog = 
			new ElementListSelectionDialog(getShell(), new TestCaseTypeRelationLabelProvider());
		selectDialog.setTitle(Messages.TestCaseSection_DialogSelectTestRelation_Title);
		selectDialog.setMessage(Messages.TestCaseSection_DialogSelectTestRelation_Description);
		
		ITestPolicyCmptTypeParameter param = parentTestPolicyCmpt.findTestPolicyCmptType();
		TestCaseTypeRelation[] dummyRelations = new TestCaseTypeRelation[param.getTestPolicyCmptTypeParamChilds().length];
        ITestPolicyCmptTypeParameter[] childParams = param.getTestPolicyCmptTypeParamChilds();
        for (int i = 0; i < childParams.length; i++) {
            TestCaseTypeRelation relation = new TestCaseTypeRelation(childParams[i], parentTestPolicyCmpt);
            dummyRelations[i] = relation;
        }        
		if (dummyRelations.length == 1){
            // exactly one role found, return this role
            return dummyRelations[0];
        }else{
            selectDialog.setElements(dummyRelations);
            if (selectDialog.open()==Window.OK) {
                if (selectDialog.getResult().length>0) {
                    return (TestCaseTypeRelation)selectDialog.getResult()[0];
                }
            }
        }
		return null;
	}
	
	/**
	 * Label provider for the test case type relation select dialog.
	 */
	private class TestCaseTypeRelationLabelProvider implements ILabelProvider{
		/**
		 * {@inheritDoc}
		 */
		public Image getImage(Object element) {
			return getImageFromRelationType((TestCaseTypeRelation)element);
		}

		/**
		 * Returns the image of the given relation test case type parameter.
		 */
		private Image getImageFromRelationType(TestCaseTypeRelation dummyRelation) {
			try {
				ITestPolicyCmptTypeParameter typeParam = null;
				typeParam = dummyRelation.getTestPolicyCmptTypeParam();
				IRelation relation = typeParam.findRelation();
				if (relation == null){
					return null;
				}				      	
				if (relation.isAssoziation()){
					return IpsPlugin.getDefault().getImage("Relation.gif"); //$NON-NLS-1$
				}else {
					return IpsPlugin.getDefault().getImage("Aggregation.gif"); //$NON-NLS-1$
				}
			} catch (CoreException e) {
				return null;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public String getText(Object element) {
	    	TestCaseTypeRelation dummyRelation = (TestCaseTypeRelation) element;
	    	String text = dummyRelation.getName();
	    	if (dummyRelation.isRequiresProductCmpt()){
	    		text += REQUIRES_PRODUCT_CMPT_SUFFIX;
	    	}
	    	return text;
		}

		/**
		 * {@inheritDoc}
		 */

		public void addListener(ILabelProviderListener listener) {
		}

		/**
		 * {@inheritDoc}
		 */

		public void dispose() {
		}

		/**
		 * {@inheritDoc}
		 */

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		public void removeListener(ILabelProviderListener listener) {
		}		
	}
	
	/**
	 * (@inheritDoc)
	 */
	protected void performRefresh() {
	}

	/*
	 * Converts the given failure details to one failure detail row.
	 */
	private String failureDetailsToString(String[] failureDetails){
		String failureFormat= Messages.TestCaseSection_FailureFormat_FailureIn;
		String failureActual = Messages.TestCaseSection_FailureFormat_Actual;
		String failureExpected = Messages.TestCaseSection_FailureFormat_Expected;
		String failureFormatAttribute= Messages.TestCaseSection_FailureFormat_Attribute;
		String failureFormatObject= Messages.TestCaseSection_FailureFormat_Object;
        String failureFormatMessage = Messages.TestCaseSection_FailureFormat_Message;
		if (failureDetails.length>1)
		    failureFormat= failureFormat + (!"<null>".equals(failureDetails[1])?failureFormatObject:""); //$NON-NLS-1$ //$NON-NLS-2$
		if (failureDetails.length>2)
		    failureFormat= failureFormat + (!"<null>".equals(failureDetails[2])?failureFormatAttribute:"");		 //$NON-NLS-1$ //$NON-NLS-2$
		if (failureDetails.length>3)
			failureFormat= failureFormat + (failureExpected); //$NON-NLS-1$
		if (failureDetails.length>4)
			failureFormat= failureFormat + (failureActual); //$NON-NLS-1$
		if (failureDetails.length>5)
		    failureFormat= failureFormat + (!"<null>".equals(failureDetails[5])?failureFormatMessage:""); //$NON-NLS-1$ //$NON-NLS-2$
		return MessageFormat.format(failureFormat, failureDetails); 
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void testErrorOccured(String qualifiedTestName, String[] errorDetails) {
		if (! canListenToTestRun(qualifiedTestName))
			return;

		isTestRunError = true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void testFailureOccured(String[] failureDetails) {
		if (! canListenToTestRun(failureDetails[0]))
			return;
		
		isTestRunFailure = true;
		failureCount ++;
		
		String objectName = failureDetails[1];
		String attributeName = failureDetails[2];
        String formatedFailure = failureDetailsToString(failureDetails);
        
        postAddFailureTooltip(formatedFailure);
		
        if (StringUtils.isEmpty(attributeName) || "<null>".equals(attributeName)){ //$NON-NLS-1$
            // no attribute given expect that the failure was in an value object
            testCaseDetailArea.markTestValueAsFailure(objectName, formatedFailure);
        } else {
            // mark attribut edit field as failure
            try {
               //String testPolicyCmptTypeParamPath = TestCaseHierarchyPath.evalTestPolicyCmptParamPath(testPolicyCmpt);
                testCaseDetailArea.markAttributeAsFailure(objectName + attributeName, formatedFailure);
            } catch (Exception e) {
                // ignore the excpetion while marking the edit field with the failure 
            }
        }
	}

	private void postAddFailureTooltip(final String failureToolTip) {
        postSyncRunnable(new Runnable() {
            public void run() {
                if (isDisposed())
                    return;
                form.getContent().setToolTipText(form.getContent().getToolTipText() + "\n" + failureToolTip); //$NON-NLS-1$
            }
        });
    }
    
	/**
	 * {@inheritDoc}
	 */	
	public void testFinished(String qualifiedTestName) {
		if (! canListenToTestRun(qualifiedTestName))
			return;
		
		postSetTestRunStatus(isTestRunError, isTestRunFailure, failureCount);
	}

	/**
	 * {@inheritDoc}
	 */
	public void testStarted(String qualifiedTestName) {
		if (! canListenToTestRun(qualifiedTestName))
			return;
        postResetTestRunStatus();
		
		isTestRunError = false;
		isTestRunFailure = false;
		failureCount = 0;
		postShowAll();
	}

	/**
	 * {@inheritDoc}
	 */	
	public void testRunEnded() {
		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	public void testRunStarted(int testCount, String repositoryPackage, String testPackage) {
		// nothing to do
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void testTableEntry(String qualifiedName, String fullPath) {
		// nothing to do
	}
	
	void postSyncRunnable(Runnable r) {
		if (!isDisposed())
			getDisplay().syncExec(r);
	}	

	/*
	 * Returns <code>true</code> if the test run listener is relevant for this test case.<br>
     * Return <code>false<code> if the file is changed and not saved (source file is dirty).<br>
     * Return <code>false<code> if the given test case name doesn't match the current editing test case.
	 */
	private boolean canListenToTestRun(String testCaseQualifiedName) {
        if (testCase.getIpsSrcFile().isDirty()){
            postResetTestRunStatus();
            return false;
        }
		return testCaseQualifiedName.equals(testCase.getQualifiedName());
	}

	void postSetTestRunStatus(final boolean isError, final boolean isFailure, final int failureCount) {
		postSyncRunnable(new Runnable() {
			public void run() {
				if (isDisposed())
                    return;
                if (isError) {
                    form.getContent().setBackground(getDisplay().getSystemColor(SWT.COLOR_RED));
                    form.getContent().setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
                    form.getContent().setToolTipText(Messages.TestCaseEditor_Title_Error);
                } else if (isFailure) {
                    form.getContent().setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
                    form.getContent().setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
                    form.getContent().setToolTipText(
                                    NLS.bind(Messages.TestCaseEditor_Title_Failure, "" + failureCount) + form.getContent().getToolTipText()); //$NON-NLS-1$
                } else {
                    form.getContent().setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
                    form.getContent().setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
                    form.getContent().setToolTipText(Messages.TestCaseEditor_Title_Success);
                }
			}
		});
	}
	
	void postSetFailureBackgroundAndToolTip(final EditField editField, final String expectedResult) {
		postSyncRunnable(new Runnable() {
			public void run() {
				if(isDisposed()) 
					return;
				editField.getControl().setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
				editField.getControl().setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
				editField.getControl().setToolTipText(expectedResult);
				editField.getControl().setFocus();
			}
		});
	}

	void postShowAll() {
		postSyncRunnable(new Runnable() {
			public void run() {
				if(isDisposed()) 
					return;
				showAll(true);
				actionAll.setChecked(true);
			}
		});
	}
    
    void postResetTestRunStatus(){
        postSyncRunnable(new Runnable() {
            public void run() {
                if(isDisposed()) 
                    return;
                resetTestRunStatus();
            }
        });
    }
    
    /**
     * Resets the test run status. Change the color and tooltips to default.
     *
     */
    public void resetTestRunStatus(){
        form.getContent().setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
        form.getContent().setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
        form.getContent().setToolTipText(""); //$NON-NLS-1$
        testCaseDetailArea.resetTestRun();
    }
	
	/**
	 * Returns the corresponding test case editor.
	 */
	TestCaseEditor getTestCaseEditor(){
		return editor;
	}

	/**
	 * Returns the corresponding content provider.
	 */
	TestCaseContentProvider getContentProvider() {
		return contentProvider;
	}
}
