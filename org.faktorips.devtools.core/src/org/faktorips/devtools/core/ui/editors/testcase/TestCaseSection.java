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
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.Validatable;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.testcase.IIpsTestRunListener;
import org.faktorips.devtools.core.model.testcase.IIpsTestRunner;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcase.ITestRule;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcase.TestRuleViolationType;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestRuleParameter;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.MessageCueLabelProvider;
import org.faktorips.devtools.core.ui.PdObjectSelectionDialog;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.TreeMessageHoverService;
import org.faktorips.devtools.core.ui.editors.pctype.ContentsChangeListenerForWidget;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.MessageList;

/**
 * Section to display and edit a ips test case.
 */
public class TestCaseSection extends IpsSection implements IIpsTestRunListener {
	public static final String VALUESECTION = "VALUESECTION"; //$NON-NLS-1$
	
    //Ui refresh intervall
    static final int REFRESH_INTERVAL = 400;
    
	// The treeview which displays all test policy components and test values which are available in this test
	private TreeViewer treeViewer;

	// Contains the test case which is displayed in this section
	private ITestCase testCase;
	
	// Contains the test case editor
	private IpsObjectEditor editor;
	
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

    // Contains the label provider for the test objects
    private TestCaseLabelProvider labelProvider;
    
	// The deatil area of the test case
	private TestCaseDetailArea testCaseDetailArea;
	
	// Previous selected entries in the tree to
	private List prevTestObjects;
	
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
	
    // Indicates that the tree is refreshing
    private boolean isTreeRefreshing = false;
    
    // Indicates if the corresponding test case type changed
    private boolean testCaseTypeChanged = false;
    
    // Contains the color for the failure indicators
    private Color fFailureColor;
    // Color to indicate test ok
    private Color fOkColor;
    
    // Listener for the run test and store expected result action
    private IIpsTestRunListener runAndStoreExpectedResultListener;

    // Contains all failure details for one test run
    private List allFailureDetails = new ArrayList();

    // Listener about content changes
    private TestCaseContentChangeListener changeListener;

    // Contains the menu in the title if available
    private Menu sectionTitleContextMenu;
    
    /*
     * State class contains the enable state of all actions (for buttons and context menu)
     */
    private class TreeActionEnableState{
        boolean productCmptEnable = false;
        boolean removeEnable = false;
        boolean addEnable = false;
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
     * Label provider for validation rules.
     * Displays as text the name followed by the policy cmpt type which the rule belongs to.
     */
    private class ValidationRuleLabelProvider extends DefaultLabelProvider {
        /**
         * {@inheritDoc}
         */
        public String getText(Object element) {
            if (!(element instanceof IValidationRule)) {
                return super.getText(element);
            } 
            IValidationRule validationRule = (IValidationRule) element;
            String nameWithPolicyCmptType = validationRule.getName();
            nameWithPolicyCmptType += " - " + ((PolicyCmptType)validationRule.getParent()).getName(); //$NON-NLS-1$
            return nameWithPolicyCmptType;
        }
    }
    
    /*
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
                return relation.getImage();
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
        		text += Messages.TestCaseLabelProvider_LabelSuffix_RequiresProductCmpt;
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
            fFailureColor.dispose();
            fOkColor.dispose();
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

    /*
     * Class to represent one ips test runner failure
     */
    private class FailureDetails{
        private String[] failureDetails;
        
        public FailureDetails(String[] failureDetails) {
            this.failureDetails = failureDetails;
        }
        public String getActualValue() {
            return failureDetails[4];
        }
        public String getAttributeName() {
            return failureDetails[2];
        }
        public String getExpectedValue() {
            return failureDetails[3];
        }
        public String getMessage() {
            return failureDetails[5];
        }
        public String getObjectName() {
            return failureDetails[1];
        }
        public String[] getFailureDetails(){
            return failureDetails;
        }
    }
    
    /*
     * Class to represent the context menu to store the actual value in the expected value field
     */
    private class EditFieldMenu extends MenuManager implements IMenuListener, ISelectionProvider{
        private List failureDetailsList;
        public EditFieldMenu(String text, List failureDetailsList){
            super(text);
            this.failureDetailsList = failureDetailsList;
        }
        public void addSelectionChangedListener(ISelectionChangedListener listener) {
        }
        public ISelection getSelection() {
            return null;
        }
        public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        }
        public void setSelection(ISelection selection) {
        }
        public List getFailureDetailsList() {
            return failureDetailsList;
        }
        public void menuAboutToShow(IMenuManager manager) {
            final EditFieldMenu contextMenuManager = (EditFieldMenu) manager;

            Action actionStoreActualValue = new Action("actionStoreActualValue", Action.AS_PUSH_BUTTON) { //$NON-NLS-1$
                public void run() {
                    List failureDetailsList = contextMenuManager.getFailureDetailsList();
                    if(failureDetailsList.size()>1){
                        boolean overwriteExpectedResult = MessageDialog
                                .openQuestion(getShell(), Messages.TestCaseSection_MessageDialog_TitleStoreExpectedResults,
                                        Messages.TestCaseSection_MessageDialog_QuestionStoreExpectedResults);
                        if (!overwriteExpectedResult) {
                            return;
                        }                        
                    }
                    for (Iterator iter = failureDetailsList.iterator(); iter.hasNext();) {
                        FailureDetails failureDetails = (FailureDetails)iter.next();
                        testCaseDetailArea.storeActualValueInExpResult(getUniqueEditFieldKey(failureDetails
                                .getObjectName(), failureDetails.getAttributeName()), failureDetails.getActualValue(),
                                failureDetails.getMessage());
                    }
                    testCaseDetailArea.updateUi();
                }
            };
            if (failureDetailsList.size()>1){
                actionStoreActualValue.setText(Messages.TestCaseSection_Action_StoreExpectedResults);
                actionStoreActualValue.setToolTipText(Messages.TestCaseSection_Action_ToolTipStoreExpectedResults);
            } else {
                actionStoreActualValue.setText(Messages.TestCaseSection_Action_StoreExpectedResult);
                actionStoreActualValue.setToolTipText(Messages.TestCaseSection_Action_ToolTipStoreExpectedResult);
            }
            actionStoreActualValue.setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("TestCaseStoreExpResult.gif")); //$NON-NLS-1$
            
            manager.add(actionStoreActualValue);
        }
    }
    
    /*
     * Content change class to listen for content changes.
     */
    private class TestCaseContentChangeListener extends ContentsChangeListenerForWidget{
        public TestCaseContentChangeListener(Widget widget) {
            super(widget);
        }
        public void contentsChangedAndWidgetIsNotDisposed(ContentChangeEvent event) {
            contentsHasChanged(event);
        }
    }
    
    /*
     * Action to add an element
     */
    private class AddAction extends IpsAction {
        public AddAction() {
            super(treeViewer);
            setText(Messages.TestCaseSection_ButtonAdd);
        }

        /**
         * {@inheritDoc}
         */
        protected boolean computeEnabledProperty(IStructuredSelection selection) {
            TreeActionEnableState actionEnableState = evaluateTreeActionEnableState(selection.getFirstElement());
            return actionEnableState.addEnable;
        }

        /**
         * {@inheritDoc}
         */
        public void run(IStructuredSelection selection) {
            try {
                addClicked();
            }
            catch (Exception e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
    }

    /*
     * Action to change the product cmpt
     */
    private class ProductCmptAction extends IpsAction {
        public ProductCmptAction() {
            super(treeViewer);
            setText(Messages.TestCaseSection_ButtonProductCmpt);
        }

        /**
         * {@inheritDoc}
         */
        protected boolean computeEnabledProperty(IStructuredSelection selection) {
            TreeActionEnableState actionEnableState = evaluateTreeActionEnableState(selection.getFirstElement());
            return actionEnableState.productCmptEnable;
        }

        /**
         * {@inheritDoc}
         */
        public void run(IStructuredSelection selection) {
            try {
                productCmptClicked();
            }
            catch (Exception e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
    }

    /*
     * Action to remove the product cmpt
     */
    private class RemoveAction extends IpsAction {
        public RemoveAction() {
            super(treeViewer);
            setText(Messages.TestCaseSection_ButtonRemove);
        }

        /**
         * {@inheritDoc}
         */
        protected boolean computeEnabledProperty(IStructuredSelection selection) {
            TreeActionEnableState actionEnableState = evaluateTreeActionEnableState(selection.getFirstElement());
            return actionEnableState.removeEnable;
        }

        /**
         * {@inheritDoc}
         */
        public void run(IStructuredSelection selection) {
            try {
                removeClicked();
            }
            catch (Exception e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
    }
    
    public TestCaseSection(Composite parent, TestCaseEditor editor, UIToolkit toolkit,
            TestCaseContentProvider contentProvider, final String title, String detailTitle, ScrolledForm form,
            IEditorSite site) {
        super(parent, Section.NO_TITLE, GridData.FILL_BOTH, toolkit);
        
        this.editor = editor;
        this.contentProvider = contentProvider;
        this.form = form;
        this.sectionTreeStructureTitle = title;
        this.sectionDetailTitle = detailTitle;
        
        initControls();
        setText(title);
        
        // Colors are taken from the JUnit test runner to show a corporate identify for test support
        fFailureColor = new Color(getDisplay(), 159, 63, 63);
        fOkColor = new Color(getDisplay(), 95, 191, 95);

        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.horizontalSpacing = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        parent.setLayout(gridLayout);

        registerTestRunListener();

        
        // add listener on model,
        //   if the model changed reset the test run status
        testCase.getIpsModel().addChangeListener(new TestCaseContentChangeListener(this));
    }

    /**
     * {@inheritDoc}
     */
    private void contentsHasChanged(ContentChangeEvent event) {
        // refresh and check for delta to the test case type 
        //   if the test case type changed
        try {
            ITestCaseType testCaseType = testCase.findTestCaseType();
            if (testCaseType == null){
                return;
            }
            if (event.getIpsSrcFile().equals(testCaseType.getIpsSrcFile())) {
                testCaseTypeChanged = true;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * {@inheritDoc}
     */    
    public void dispose() {
        removeAllListener();
        super.dispose();
    }

    /**
	 * Initialization of the main section.
	 * {@inheritDoc}
	 */
	protected void initClientComposite(Composite client, UIToolkit toolkit) {
		this.toolkit = toolkit;
		this.testCase = contentProvider.getTestCase();
		
		configureToolBar();
		
		// Layout main section with two columns
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
        Tree tree = toolkit.getFormToolkit().createTree(treeComposite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
        tree.setLayoutData(new GridData(GridData.FILL_BOTH));
        treeViewer = new TreeViewer(tree);
        treeViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        hookTreeListeners();
        treeViewer.setContentProvider(contentProvider);
        labelProvider = new TestCaseLabelProvider();
        treeViewer.setLabelProvider(new MessageCueLabelProvider(labelProvider));
        treeViewer.setUseHashlookup(true);
        treeViewer.setInput(testCase);
        buildContextMenu();
        
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
        testCaseDetailArea = new TestCaseDetailArea(toolkit, contentProvider, this);
		testCaseDetailArea.createInitialDetailArea(client, sectionDetailTitle);

        // Initialize the previous selected objects as empty list
		prevTestObjects = new ArrayList();
        
        // Set the state of the buttons
        updateButtonEnableState(null);

        refreshTree();
	}

    /**
	 * Creates the tool bar actions.
	 */
	private void configureToolBar() {
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
        
        // Toolbar item run and store expected result
		Action actionRunAndStoreExpectedResult = new Action("runAndStoreExpectedResult", Action.AS_PUSH_BUTTON) { //$NON-NLS-1$
			public void run() {
				runAndStoreExpectedResultClicked();
			}
		};
        actionRunAndStoreExpectedResult.setToolTipText(Messages.TestCaseSection_Action_RunTestAndStoreExpectedResults);
        actionRunAndStoreExpectedResult.setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("TestCaseRunAndStoreExpResult.gif")); //$NON-NLS-1$
        // enable run test case functionality only if a toc file exists for this test case
        try {
            actionRunAndStoreExpectedResult.setEnabled(getTocFilePackage() != null);
        } catch (CoreException e) {
            actionRunAndStoreExpectedResult.setEnabled(false);
        }
        
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
        
        // Add actions for fitering the content type
        addContentTypeAction();
        
        form.getToolBarManager().add(new Separator());
		form.getToolBarManager().add(actionRelation);
		form.getToolBarManager().add(actionAll);
		
        form.getToolBarManager().add(new Separator());
        form.getToolBarManager().add(actionRunAndStoreExpectedResult);
        
        form.getToolBarManager().add(new Separator());
        form.getToolBarManager().add(actionTest);
        
        form.updateToolBar();
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
     * Switch (filter) to the selected content type. And reresh the editor contents.
     */
    private void switchContentType(int contentType){
        ISelection selection = treeViewer.getSelection();
        contentProvider.setContentType(contentType);
        refreshTreeAndDetailArea();
        treeViewer.setSelection(selection);
    }
	
	/**
	 * Recursive search the given childs for the given target object.
	 */
	private TreeItem searchChildsByLabel(String labelPath, TreeItem[] childs) {
		for (int i = 0; i < childs.length; i++) {
			TreeItem currItem = childs[i];
				if (currItem.getText().equals(labelPath) || currItem.getText().equals(labelPath + Messages.TestCaseLabelProvider_LabelSuffix_RequiresProductCmpt))
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
			if (currItem.getText().equals(currPathItem) || currItem.getText().equals(currPathItem + Messages.TestCaseLabelProvider_LabelSuffix_RequiresProductCmpt)){
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
	 * The selection in the tree changed the given object is selected.
	 */
	private void selectionInTreeChanged(IStructuredSelection selection) {
        if (isTreeRefreshing){
            return;
        }
        
		updateButtonEnableState(selection.getFirstElement());		
        
		if (!showAll){
            // show only the elements which belongs to the selection
            //   if a test value is selected show the value objects
            //   if a test policy cmpt or a child of a policy cmpt is selected, show all elements inside
            //     the hierarchy of the root test policy cmpt
            List objectsToDisplay = new ArrayList();
            for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
    			Object domainObject = iterator.next();
    			
                if (domainObject instanceof ITestValue){
                    objectsToDisplay.add(domainObject);
                } else if (domainObject instanceof TestCaseTypeRule){
                    // show all test rule objects if the corresponding parameter is chosen
                    ITestRule[] testRules = testCase.getTestRule(((TestCaseTypeRule)domainObject).getName());
                    for (int i = 0; i < testRules.length; i++) {
                        objectsToDisplay.add(testRules[i]);
                    }
                } else if (domainObject instanceof ITestRule){
                    // in case of a rule selection don't 
                    // change the detail area if the rule is already displayed (e.g. the root rule node is selected)
                    // otherwise display only the selected rule
                    if (prevTestObjects.contains(domainObject)){
                        objectsToDisplay = prevTestObjects;
                        continue;
                    } else {
                        objectsToDisplay.add(domainObject);
                    }
                } else {
                    ITestPolicyCmpt testPolicyCmpt = getTestPolicyCmpFromDomainObject(domainObject);
                    if (testPolicyCmpt == null){
                        prevTestObjects = null;
                        break;
                    }
                    objectsToDisplay.add(testPolicyCmpt.getRoot());
                }
    		}
    		//  if the selection has changed redraw the detail section
    		if (! objectsToDisplay.equals(prevTestObjects)){
    		    testCaseDetailArea.clearDetailArea();
    		    testCaseDetailArea.createTestObjectSections(objectsToDisplay);
    		    prevTestObjects = objectsToDisplay;
    		    redrawForm();
            }
        }
        
		// Mark the section of the tree object as selected 
        Object selected = selection.getFirstElement();
        selectInDetailArea(selected, false);
	}
	
    /*
     * Evaluates the state of the available actions
     */
    private TreeActionEnableState evaluateTreeActionEnableState(Object selection){
        TreeActionEnableState actionEnableState = new TreeActionEnableState();

        if (selection == null || ! isDataChangeable()){
            return actionEnableState;
        }
        
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
                    actionEnableState.addEnable = true;
                    actionEnableState.removeEnable = false;
                }
            } catch (CoreException e) {
                // disable add and enable remove button and ignore exception
                // maybe the test case type model and test case are inconsistence
                // in this case the whole relation could be deleted but no new childs could be added
                actionEnableState.removeEnable = true;
            }
        }else if (selection instanceof ITestPolicyCmpt){
            ITestPolicyCmpt testPolicyCmpt = (ITestPolicyCmpt) selection;
            try {
                ITestPolicyCmptTypeParameter param = testPolicyCmpt.findTestPolicyCmptTypeParameter();
                // root elements could not be deleted
                actionEnableState.removeEnable = !((ITestPolicyCmpt)selection).isRoot();
                if (param != null){
                    // type parameter exists,
                    //   enable add button only if relations are defined
                    actionEnableState.addEnable = param.getTestPolicyCmptTypeParamChilds().length>0;
                    // product component select button is only enabled if the type parameter specified this
                    actionEnableState.productCmptEnable = param.isRequiresProductCmpt();
                }
            } catch (CoreException e) {
                // disable add and remove button and ignore exception
                // maybe the test case type model and test case are inconsistence
                // in this case the parent relation could be removed but not this child element
            }
        }else if (selection instanceof ITestPolicyCmptRelation){
            // the relation object indicates, that the test policy type parameter for this relation
            // not exists, therefore only remove is enabled
            actionEnableState.removeEnable = true;
        } else if (selection instanceof TestCaseTypeRule){
            // group of test rule parameter (test rules i.e. validation rules)
            actionEnableState.addEnable = true;
        } else if (selection instanceof ITestRule){
            // a concrete test rule inside the test case
            actionEnableState.removeEnable = true;
        }
        
        return actionEnableState;
    }
    
	/**
	 * Update the button state depending on the given object.
	 */
	private void updateButtonEnableState(Object selection) {
        if (!isDataChangeable()){
            toolkit.setDataChangeable(productCmptButton, false);
            toolkit.setDataChangeable(removeButton, false);
            toolkit.setDataChangeable(addButton, false);
            return;
        }
        TreeActionEnableState actionEnableState = evaluateTreeActionEnableState(selection);
        productCmptButton.setEnabled(actionEnableState.productCmptEnable);
		removeButton.setEnabled(actionEnableState.removeEnable);
		addButton.setEnabled(actionEnableState.addEnable);
	}
	
	/**
	 * Add the tree listener to the tree.
	 */
	private void hookTreeListeners() {
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					selectionInTreeChanged(selection);				
				}
			}
		});
        new TreeMessageHoverService(treeViewer) {
            protected MessageList getMessagesFor(Object element) throws CoreException {
                if (element instanceof Validatable) {
                    return ((Validatable)element).validate();
                } else
                    return null;
            }
        };
        
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
        treeViewer.getTree().addKeyListener(new KeyListener(){
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.DEL){
                    try {
                        removeClicked();
                    }
                    catch (CoreException exception) {
                        IpsPlugin.logAndShowErrorDialog(exception);
                    }
                }
            }
            public void keyReleased(KeyEvent e) {
            }
        });        
	}
    
    /*
     * Build the context menu
     */
    private void buildContextMenu() {
        MenuManager manager = new MenuManager();
        manager.setRemoveAllWhenShown(false);
        manager.add(new AddAction());
        manager.add(new RemoveAction());
        manager.add(new ProductCmptAction());
        Menu contextMenu = manager.createContextMenu(treeViewer.getControl());
        treeViewer.getControl().setMenu(contextMenu);
    }
    
    /**
     * Select the section which is identified by the unique path.
     */
    private void selectSection(String uniquePath){
        Section sectionCtrl = testCaseDetailArea.getSection(uniquePath);
        if (sectionCtrl != null){
            sectionCtrl.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        }
    }
    
	/**
	 * Select the given object in the detail area, change the color of the section.
	 * 
	 * @param selected The object which will be searched in the detail area.
	 * @param withFocusChange If <code>true</code> also the focus will be set to the first edit field in the
	 *                        found section. If <code>false</code> no focus will be moved. 
	 */
	void selectInDetailArea(Object selected, boolean withFocusChange){
		String uniquePath = ""; //$NON-NLS-1$
        testCaseDetailArea.resetSectionColors(form);
        if (selected instanceof ITestValue) {
            uniquePath = VALUESECTION + ((ITestValue)selected).getTestParameterName();
        } else {
            uniquePath = getUniqueKey(selected);
        }
        
        if (selected instanceof ITestValue || selected instanceof ITestRule) {
            if (withFocusChange) {
                EditField firstField = testCaseDetailArea.getTestValueEditField(uniquePath);
                if (firstField != null) {
                    isDoubleClicked = true;
                    firstField.getControl().setFocus();
                }
            }
        } else {
            // selecte first attribute edit field
            if (uniquePath.length() > 0) {
                if (withFocusChange) {
                    EditField firstField = testCaseDetailArea.getFirstAttributeEditField(uniquePath);
                    if (firstField != null) {
                        firstField.getControl().setFocus();
                    }
                }
            }
        }
        selectSection(uniquePath);
	}

	/**
	 * Gets the unique key of the given object
	 */
	String getUniqueKey(Object selected){
		String uniquePath = ""; //$NON-NLS-1$
        
        if ( selected instanceof ITestRule){
            ITestRule rule = (ITestRule)selected;
            try {
                IValidationRule validationRule = rule.findValidationRule();
                uniquePath = validationRule==null?null:validationRule.getMessageCode();
            } catch (CoreException e1) {
                // ignore exception while seraching the validation rule object
            }
            if (uniquePath == null){
                // validation rule not found use rule name as identifier
                uniquePath = rule.getValidationRule();
            }
            return rule.getTestParameterName() + uniquePath;
        } else if (selected instanceof ITestValue) {
            return TestCaseSection.VALUESECTION + ((ITestValue)selected).getTestValueParameter();
        }
        
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
	 * Adds the button listener to the buttons.
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
        ISelection selection = treeViewer.getSelection();
		contentProvider.setWithoutRelations(!contentProvider.isWithoutRelations());
		refreshTreeAndDetailArea();
        treeViewer.setSelection(selection);
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
				// are defined in the test case type parameter if more than one type defined
				ITestPolicyCmpt testPolicyCmpt = (ITestPolicyCmpt) selectedObject;
				TestCaseTypeRelation relationType = selectTestCaseTypeRelationByDialog(testPolicyCmpt);
				if (relationType != null){
					addRelation(relationType);
                }
			} else if (selectedObject instanceof TestCaseTypeRule){
                // add test rules for the test rule parameter
                IValidationRule validationRule = selectValidationRuleByDialog();
                if (validationRule != null) {
                    addTestRule(((TestCaseTypeRule)selectedObject).getTestRuleParameter(), validationRule);
                }
            } else {
				return;
			}
		} catch (CoreException e) {
			IpsPlugin.logAndShowErrorDialog(e);
		}
	}

    /*
     * Add a test rule the test case
     */
    private void addTestRule(ITestRuleParameter testRuleParameter, IValidationRule validationRule) {
        ITestRule testRule = testCase.newTestRule();
        testRule.setValidationRule(validationRule.getName());
        testRule.setTestRuleParameter(testRuleParameter.getName());
        
        // sort the test objects to ensure that the test rule is in the correct order
        try {
            testCase.sortTestObjects();
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        
        refreshTreeAndDetailArea();
        selectInTreeByObject(testRuleParameter, true);
    }
    
    /*
	 * Add a new relation based an the given test case type relation.
	 */
	private void addRelation(TestCaseTypeRelation relationType) throws CoreException{
		String productCmptQualifiedName = ""; //$NON-NLS-1$
		if (relationType.isRequiresProductCmpt()) {
            productCmptQualifiedName = selectProductCmptDialog(relationType.getPolicyCmptTypeTarget(), relationType
                    .getParentTestPolicyCmpt().findProductCmpt());
            if (productCmptQualifiedName == null)
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
				addTestPcTypeRelation(relationType.getTestPolicyCmptTypeParam(), productCmptQualifiedName, targetName);
			ITestPolicyCmpt newTestPolicyCmpt = newRelation.findTarget();
			if (newTestPolicyCmpt == null){
				throw new CoreException(new IpsStatus(Messages.TestCaseSection_Error_CreatingRelation));
			}
			
			refreshTreeAndDetailArea();
			selectInTreeByObject(newRelation, true);
		}else{
			// composition relatation will be added	
			ITestPolicyCmptRelation newRelation = relationType.getParentTestPolicyCmpt().
				addTestPcTypeRelation(relationType.getTestPolicyCmptTypeParam(), productCmptQualifiedName, ""); //$NON-NLS-1$
			if (newRelation == null)
				throw new CoreException(new IpsStatus(Messages.TestCaseSection_Error_CreatingRelation));
				
			ITestPolicyCmpt newTestPolicyCmpt = newRelation.findTarget();	
			if (newTestPolicyCmpt == null)
				throw new CoreException(new IpsStatus(Messages.TestCaseSection_Error_CreatingRelation));				
			
			refreshTreeAndDetailArea();
			selectInTreeByObject(newTestPolicyCmpt, true);
		}		
	}
	
    /*
     * Returns the next possible tree item after deleting of the given object
     */
    private TreeItem getNextSelectionInTreeAfterDelete(Object objectDeletedInTree){
        Widget item = treeViewer.testFindItem(objectDeletedInTree);
        if (item instanceof TreeItem){
            TreeItem currTreeItem = (TreeItem)item;
            TreeItem parent = currTreeItem.getParentItem();
            TreeItem[] itemsSameLevel;
            if (parent != null){
                itemsSameLevel = parent.getItems();
            } else {
                itemsSameLevel = currTreeItem.getParent().getItems();
            }
            TreeItem prevItem = null;
            for (int i = 0; i < itemsSameLevel.length; i++) {
                if (itemsSameLevel[i].equals(currTreeItem)){
                    break;
                }
                prevItem = itemsSameLevel[i];
            }
            if (prevItem != null){
                return prevItem;
            } else if (parent != null){
                return parent;
            } else if (itemsSameLevel.length > 2) {
                return itemsSameLevel[2];
            }
        }
        return null;
    }
    
	/**
	 * Remove button was clicked.
	 */
	private void removeClicked() throws CoreException {
		ISelection selection = treeViewer.getSelection();
		if (selection instanceof IStructuredSelection){
			TreeItem nextItemToSelect = null;
			for (Iterator iterator = ((IStructuredSelection)selection).iterator(); iterator.hasNext();) {			
                Object domainObject = iterator.next();
                nextItemToSelect = getNextSelectionInTreeAfterDelete(domainObject);
				if (domainObject instanceof ITestPolicyCmptRelation){
					((ITestPolicyCmptRelation) domainObject).delete();
				} else if (domainObject instanceof ITestObject) {
					((ITestObject) domainObject).delete();
                } else {
                    throw new RuntimeException("Remove object with type " + domainObject.getClass().getName() + " is not supported!" ); //$NON-NLS-1$ //$NON-NLS-2$
                }
				refreshTreeAndDetailArea();
			}
			treeViewer.getTree().setSelection(new TreeItem[]{nextItemToSelect});
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
					testTypeParam = testPolicyCmpt.findTestPolicyCmptTypeParameter();
				} catch (CoreException e) {
					// ignored, the validation shows the unknown type failure message
					return;
				}
				String productCmptQualifiedName = ""; //$NON-NLS-1$
				if (testTypeParam.isRequiresProductCmpt()){
					productCmptQualifiedName = selectProductCmptDialog(testTypeParam.getPolicyCmptType(), testPolicyCmpt.findProductCmpt());
					if (productCmptQualifiedName == null)
						// chancel
						return;
				}
				testPolicyCmpt.setProductCmpt(productCmptQualifiedName);
				testPolicyCmpt.setName(
						testCase.generateUniqueNameForTestPolicyCmpt(testPolicyCmpt, StringUtil.unqualifiedName(productCmptQualifiedName)));
                
                boolean updateTestAttrValuesWithDefault = MessageDialog
                        .openQuestion(
                                getShell(),
                                Messages.TestCaseSection_DialogOverwriteWithDefault_Title,
                                Messages.TestCaseSection_DialogOverwriteWithDefault_Text);
                if (updateTestAttrValuesWithDefault) {
                    testPolicyCmpt.updateDefaultTestAttributeValues();
                }
                
                refreshTreeAndDetailArea();
			}
		}
	}
	
	private void showAllClicked(){
        ISelection selection = treeViewer.getSelection();
        showAll(! showAll);
        treeViewer.setSelection(selection);   
	}

    private void runAndStoreExpectedResultClicked() {
        if (!isDataChangeable()){
            return;
        }
        boolean overwriteExpectedResult = MessageDialog.openQuestion(getShell(), Messages.TestCaseSection_MessageDialog_TitleRunTestAndStoreExpectedResults,
                Messages.TestCaseSection_MessageDialog_QuestionRunTestAndStoreExpectedResults);
        if (!overwriteExpectedResult) {
            return;
        }
        resetTestRunStatus();
        showAll(true);
        registerRunTestAndStoreExpectedResultLister();
        startTestRunner();
    }

    /*
     * Show all objects in the detail area if showAll is <code>true</code>, otherwise show only
     * the object which is selected in the tree
     */
	private void showAll(boolean showAll){
        this.showAll = showAll;
		if (showAll) {
            // show all test objects wich are provided by the content provider
            createDetailsSectionsForAll();
            treeViewer.expandAll();
        } else {
            // show only the selected test objetcs
            ISelection selection = treeViewer.getSelection();
            if (selection instanceof IStructuredSelection) {
                Object domainObject = ((IStructuredSelection)selection).getFirstElement();
                testCaseDetailArea.clearDetailArea();
                List list = new ArrayList();
                if (domainObject instanceof ITestValue) {
                    list.addAll(Arrays.asList(new ITestValue[] { (ITestValue)domainObject }));
                    testCaseDetailArea.createTestObjectSections(list);
                    prevTestObjects = list;
                } else {
                    ITestPolicyCmpt testPolicyCmpt = (ITestPolicyCmpt)getTestPolicyCmpFromDomainObject(domainObject);
                    if (testPolicyCmpt != null) {
                        list.addAll(Arrays.asList(new ITestPolicyCmpt[] { (ITestPolicyCmpt)testPolicyCmpt.getRoot() }));
                        testCaseDetailArea.createTestObjectSections(list);
                        prevTestObjects = list;
                    } else {
                        // no valid selection in tree
                        prevTestObjects.clear();
                    }
                }
            }
        }
		redrawForm();
	}

    /*
     * Draws the detail section for all test object in the test case, which are provided by the content provider. 
     */
    private void createDetailsSectionsForAll() {
        testCaseDetailArea.clearDetailArea();
        List list = new ArrayList();
        list.addAll(Arrays.asList(contentProvider.getTestObjects()));
        testCaseDetailArea.createTestObjectSections(list);
        prevTestObjects = list;
    }

	private void runTestClicked(){
        resetTestRunStatus();
        registerTestRunListener();
	    startTestRunner();
	}
    
    /*
     * Returns the toc file package name which stores the current test case.
     */
    private String getTocFilePackage() throws CoreException {
        IIpsPackageFragment packageFragment = testCase.getIpsPackageFragment();
        if (packageFragment == null)
            return null;
        IIpsPackageFragmentRoot root = packageFragment.getRoot();
        IIpsArtefactBuilderSet builderSet = root.getIpsProject().getIpsArtefactBuilderSet();
        return builderSet.getRuntimeRepositoryTocResourceName(root);        
    }
    
	/**
     * Shows the select product component dialog and returns the selected product component
     * qualified name. Returns <code>null</code> if no selection or an unsupported type was
     * chosen.
     * 
     * @throws CoreException If an error occurs
     */
	private String selectProductCmptDialog(String qualifiedTypeName, IProductCmpt productCmptParent) throws CoreException {
	    PdObjectSelectionDialog dialog = new PdObjectSelectionDialog(getShell(), Messages.TestCaseSection_DialogSelectProductCmpt_Title, 
        		Messages.TestCaseSection_DialogSelectProductCmpt_Description);
        dialog.setElements(getProductCmptObjects(qualifiedTypeName));
        if (dialog.open()==Window.OK) {
            if (dialog.getResult().length>0) {
            	IProductCmpt productCmpt = (IProductCmpt) dialog.getResult()[0];
        		return productCmpt.getQualifiedName();
            }
        }
	    return null;
	}
	
	/**
	 * Returns all product component objects in the model which are related to the given type name.
	 * 
	 * @throws CoreException If an error occurs
	 */
    public IIpsObject[] getProductCmptObjects(String qualifiedTypeName) throws CoreException {
        List allProductCmpts = new ArrayList();
        
        getProductCmptObjects(testCase.getIpsProject(), qualifiedTypeName, allProductCmpts);
        
        IProductCmpt[] cmpts = (IProductCmpt[]) allProductCmpts.toArray(new IProductCmpt[0]);
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
			}else {
				return domainObject;
			}
		}
		return null;
	}
	
	/**
	 * Refresh the tree and form.
	 */
	private void refreshTreeAndDetailArea(){
		form.setRedraw(false);
        try {
            testCaseDetailArea.clearDetailArea();

            if (showAll) {
                createDetailsSectionsForAll();
            } else {
                testCaseDetailArea.createTestObjectSections(prevTestObjects);
            }
            refreshTree();
            redrawForm();
        } finally {
            form.setRedraw(true);
        }
	}
	
	/**
	 * Refresh the tree.
	 */
	void refreshTree(){
        if (treeViewer.getTree().isDisposed()){
            return;
        }

        try {
            isTreeRefreshing = true;
            treeViewer.getTree().setRedraw(false);
            TreeViewerExpandStateStorage treeexpandStorage = new TreeViewerExpandStateStorage(treeViewer);
            treeexpandStorage.storeExpandedStatus();
            treeViewer.refresh();
            treeViewer.expandAll();
            treeViewer.collapseAll();
            treeexpandStorage.restoreExpandedStatus();
        } finally {
            isTreeRefreshing = false;
            treeViewer.getTree().setRedraw(true);
        }
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

    void selectInTreeByObject(ITestRule testRule) {
	    treeViewer.setSelection(new StructuredSelection(testRule));
	}
	
    private void selectInTreeByObject(ITestRuleParameter testRuleParameter, boolean b) {
        treeViewer.setSelection(new StructuredSelection(testRuleParameter));
    }

	/**
	 * Select the given test policy component in the tree.
	 */
	private void  selectInTreeByObject(ITestPolicyCmpt testPolicyCmpt, ITestPolicyCmptRelation relation, boolean focusChange) {
		if (!isDoubleClicked){
            if (testPolicyCmpt != null)
                selectInDetailArea(testPolicyCmpt, false);
            else if (relation != null)
                selectInDetailArea(relation, false);
            
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
	 * Selects the given test object in the tree.
	 */
	void selectTestObjectInTree(ITestObject testObject){
		if (!isDoubleClicked){
    		selectInDetailArea(testObject, false);
    		// goto the corresponding value object in the tree
    		Tree tree = treeViewer.getTree();
            
        	TreeItem found = searchChildsByLabel(labelProvider.getText(testObject), tree.getItems());
        	if (found != null) {
    			// select the tree entry
    			TreeItem[] select = new TreeItem[1];
    			select[0] = found;
    			tree.setSelection(select);
    		}
        }else{
        	isDoubleClicked = false;
        	selectInDetailArea(testObject, true);
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
	
	/*
	 * Displays a dialog to select the type definition of a test relation.
	 * Returns the selected test case type relation object or <code>null</code> if the user select nothing.
	 */
	private TestCaseTypeRelation selectTestCaseTypeRelationByDialog(ITestPolicyCmpt parentTestPolicyCmpt) throws CoreException {
		ElementListSelectionDialog selectDialog = 
			new ElementListSelectionDialog(getShell(), new TestCaseTypeRelationLabelProvider());
		selectDialog.setTitle(Messages.TestCaseSection_DialogSelectTestRelation_Title);
		selectDialog.setMessage(Messages.TestCaseSection_DialogSelectTestRelation_Description);
		
		ITestPolicyCmptTypeParameter param = parentTestPolicyCmpt.findTestPolicyCmptTypeParameter();
		TestCaseTypeRelation[] dummyRelations = new TestCaseTypeRelation[param.getTestPolicyCmptTypeParamChilds().length];
        ITestPolicyCmptTypeParameter[] childParams = param.getTestPolicyCmptTypeParamChilds();
        for (int i = 0; i < childParams.length; i++) {
            TestCaseTypeRelation relation = new TestCaseTypeRelation(childParams[i], parentTestPolicyCmpt);
            dummyRelations[i] = relation;
        }        
		if (dummyRelations.length == 1){
            // exactly one type found, return this type
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
    
    /*
     * Displays a dialog to select on of a validation rule inside test test policy cmpts in this test case.
     * Returns the selected validation rule object or <code>null</code> if the user select nothing.
     */
    private IValidationRule selectValidationRuleByDialog() {
        ElementListSelectionDialog selectDialog = 
            new ElementListSelectionDialog(getShell(), new ValidationRuleLabelProvider());
        selectDialog.setTitle(Messages.TestCaseSection_SelectDialogValidationRule_Title);
        selectDialog.setMessage(Messages.TestCaseSection_SelectDialogValidationRule_Decription);
        
        try {
            IValidationRule[] rules = testCase.getTestRuleCandidates();
            selectDialog.setElements(rules);
            if (selectDialog.open() == Window.OK) {
                if (selectDialog.getResult().length > 0) {
                    return (IValidationRule)selectDialog.getResult()[0];
                }
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return null;
    }
	
	/**
     * (@inheritDoc)
     */
    protected void performRefresh() {
        treeViewer.refresh();
        // reset the test runner status
        postResetTestRunStatus();

        // if in the meanwhile the test case type changed check for inconsistence between test case
        // and test case type, only if the data is changeable
        if (testCaseTypeChanged) {
            testCaseTypeChanged = false;
            postResetTestRunStatus();
            refreshTreeAndDetailArea();
        }
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
		
        String[] failureDetailsToFormat = new String[failureDetails.length];
        System.arraycopy(failureDetails, 0, failureDetailsToFormat, 0 , failureDetails.length);
        
        failureDetailsToFormat[3] = mapRuleValueTest(failureDetailsToFormat[3]);
        failureDetailsToFormat[4] = mapRuleValueTest(failureDetailsToFormat[4]);
        
        if (failureDetailsToFormat.length>1)
		    failureFormat= failureFormat + (!"<null>".equals(failureDetailsToFormat[1])?failureFormatObject:""); //$NON-NLS-1$ //$NON-NLS-2$
		if (failureDetailsToFormat.length>2)
		    failureFormat= failureFormat + (!"<null>".equals(failureDetailsToFormat[2])?failureFormatAttribute:"");		 //$NON-NLS-1$ //$NON-NLS-2$
		if (failureDetailsToFormat.length>3)
			failureFormat= failureFormat + (failureExpected); //$NON-NLS-1$
		if (failureDetailsToFormat.length>4)
			failureFormat= failureFormat + (failureActual); //$NON-NLS-1$
		if (failureDetailsToFormat.length>5)
		    failureFormat= failureFormat + (!"<null>".equals(failureDetailsToFormat[5])?failureFormatMessage:""); //$NON-NLS-1$ //$NON-NLS-2$
		return MessageFormat.format(failureFormat, failureDetailsToFormat); 
	}
	
    /*
     * Maps the rule values id to the corresponding name, if the given string is no id
     * return the given string without mapping.
     */
    private String mapRuleValueTest(String id) {
        if (id.equals(TestRuleViolationType.VIOLATED.getId())){
            return TestRuleViolationType.VIOLATED.getText();
        } else if (id.equals(TestRuleViolationType.NOT_VIOLATED.getId())){
            return TestRuleViolationType.NOT_VIOLATED.getText();
        }
        return id;
    }

    /*
     * Converts the given failure details to one store actual value in expected result detail row.
     */
    private String failureDetailsToStoreInExpResultToString(String[] failureDetails) {
        return failureDetailsToString(failureDetails);
    }
    
	/**
	 * {@inheritDoc}
	 */
	public void testErrorOccured(String qualifiedTestName, String[] errorDetails) {
		if (StringUtils.isNotEmpty(qualifiedTestName) && ! canListenToTestRun(qualifiedTestName)){
			return;
        }
		isTestRunError = true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void testFailureOccured(String qualifiedTestName, String[] failureDetails) {
		if (! canListenToTestRun(qualifiedTestName)){
			return;
        }
        
        // if not all fields are visible, first show all fields
        if (!actionAll.isChecked()){
            postShowAll();
        }
        
		isTestRunFailure = true;
		failureCount ++;
		
        FailureDetails failureDetailsObj = new FailureDetails(failureDetails);
        String formatedFailure = failureDetailsToString(failureDetails);

        // inform about the failure in the form title
        postAddFailureTooltipInFormTitle(formatedFailure);

        String uniqueEditFieldKey = getUniqueEditFieldKey(failureDetailsObj.getObjectName(), failureDetailsObj
                .getAttributeName());

        // indicate edit fiels as failure
        testCaseDetailArea.markEditFieldAsFailure(uniqueEditFieldKey, formatedFailure, failureDetails);

        // create context menu to store actual value
        EditField editField = testCaseDetailArea.getEditField(getUniqueEditFieldKey(failureDetailsObj.getObjectName(),
                failureDetailsObj.getAttributeName()));
        if (editField != null){
            postAddExpectedResultContextMenu(editField.getControl(), failureDetailsObj, false);
        }
        
        // store the failure details for later use (e.g. store all actual values)
        allFailureDetails.add(failureDetailsObj);
	}
    
    /*
     * Returns the unique key to indicate the edit field
     */
    private String getUniqueEditFieldKey(String objectName, String attributeName){
        if (StringUtils.isEmpty(attributeName) || "<null>".equals(attributeName)){ //$NON-NLS-1$
            // no attribute given expect that the failure was in an value object
            return objectName;
        } else {
            return objectName + attributeName;
        }
    }
    
    private void postAddFailureTooltipInFormTitle(final String failureToolTip) {
        postAsyncRunnable(new Runnable() {
            public void run() {
                if (isDisposed())
                    return;
                form.getContent().setToolTipText(form.getContent().getToolTipText() + "\n" + failureToolTip); //$NON-NLS-1$
            }
        });
    }    
    
    void postAddExpectedResultContextMenu(Control control, String[] failureDetails){
        postAddExpectedResultContextMenu(control, new FailureDetails(failureDetails), false);
    }
    
    private void postAddExpectedResultContextMenu(Control control, FailureDetails failureDetails, boolean isSectionTitleMenu) {
        ArrayList list = new ArrayList(1);
        list.add(failureDetails);
        postAddExpectedResultContextMenu(control, list, isSectionTitleMenu);
    }
    
    private Control getSectionTitleControl(){
        return form.getContent();
    }
    
    private void postAddExpectedResultContextMenu(final Control control,
            final List failureDetails,
            final boolean isSectionTitleMenu) {
        postAsyncRunnable(new Runnable() {
            public void run() {
                if (isDisposed() || control == null || !isDataChangeable())
                    return;

                if (failureDetails.size() == 0)
                    return;

                EditFieldMenu menuMgr = new EditFieldMenu("#PopupMenu", failureDetails); //$NON-NLS-1$
                menuMgr.setRemoveAllWhenShown(true);
                menuMgr.addMenuListener(menuMgr);

                Menu menu = menuMgr.createContextMenu(control);
                control.setMenu(menu);
                if (isSectionTitleMenu) {
                    sectionTitleContextMenu = menu;
                }
            }
        });
    }

	/**
     * {@inheritDoc}
     */	
	public void testFinished(String qualifiedTestName) {
		if (! canListenToTestRun(qualifiedTestName)){
			return;
        }
        
		postSetTestRunStatus(isTestRunError, isTestRunFailure, failureCount);
        
        // create context menu to store actual value as expected value
        List allFailuresCopy = new ArrayList();
        allFailuresCopy.addAll(allFailureDetails);
        postAddExpectedResultContextMenu(getSectionTitleControl(), allFailuresCopy, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public void testStarted(String qualifiedTestName) {
		if (! canListenToTestRun(qualifiedTestName)){
			return;
        }
        
        // resets the status, thus if a test runner for this test case is started in the background
        // - e.g. by using the modelexplorer - the status will be removed correctly
        postResetTestRunStatus();
		
        // remove the contextmenu in the section title
        postAsyncRunnable(new Runnable() {
            public void run() {
                if (isDisposed())
                    return;
                if (sectionTitleContextMenu != null) {
                    sectionTitleContextMenu.dispose();
                }
            }
        });
        
		isTestRunError = false;
		isTestRunFailure = false;
		failureCount = 0;
		postShowAll();
	}

	/**
	 * {@inheritDoc}
	 */	
	public void testRunEnded(String elapsedTime) {
        if (isTestRunError){
            // set the status only if an error occured, otherwise the status is set by the testFinished event
            postSetTestRunStatus(isTestRunError, isTestRunFailure, failureCount);
        }
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
	
    /**
     * {@inheritDoc}
     */
	public void testTableEntries(String[] qualifiedName, String[] fullPath) {
	    // nothing to do
    }
    
    void postAsyncRunnable(Runnable r) {
        if (!isDisposed())
            getDisplay().asyncExec(r);
    }   
	
    /*
	 * Returns <code>true</code> if the test run listener is relevant for this test case.<br>
     * Returns <code>false<code> if the file is changed and not saved (source file is dirty).<br>
     * Returns <code>false<code> if the given test case name doesn't match the current editing test case.
	 */
	private boolean canListenToTestRun(String testCaseQualifiedName) {
        if (testCase.getIpsSrcFile().isDirty()){
            postResetTestRunStatus();
            return false;
        }
		return testCaseQualifiedName.equals(testCase.getQualifiedName());
	}

	void postSetTestRunStatus(final boolean isError, final boolean isFailure, final int failureCount) {
        postAsyncRunnable(new Runnable() {
			public void run() {
				if (isDisposed())
                    return;
                setTitleStatus(isError, isFailure, false, failureCount, form.getContent().getToolTipText());
			}

		});
	}
	
    /*
     * Sets the status in the section title. One of:<ul>
     * <li>error - color red, tooltip contains error ocurred (no further details)
     * <li>failure - color red, tooltip contains failure details
     * <li>overridden - color yellow, tooltip informs about the overridden fields
     * <li>ok - color green, no tooltip
     * </ul>
     */
	private void setTitleStatus(boolean isError,
            boolean isFailure,
            boolean isOverridden,
            int failureCount,
            String titleMessage) {
        if (isError) {
            form.getContent().setBackground(fFailureColor);
            form.getContent().setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
            form.getContent().setToolTipText(Messages.TestCaseEditor_Title_Error);
        } else if (isFailure) {
            form.getContent().setBackground(fFailureColor);
            form.getContent().setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
            form.getContent().setToolTipText(
                    NLS.bind(Messages.TestCaseEditor_Title_Failure, "" + failureCount) + titleMessage); //$NON-NLS-1$
        } else if (isOverridden) {
            form.getContent().setBackground(getDisplay().getSystemColor(SWT.COLOR_YELLOW));
            form.getContent().setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
            form.getContent().setToolTipText(titleMessage);
        } else {
            form.getContent().setBackground(fOkColor);
            form.getContent().setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
            form.getContent().setToolTipText(Messages.TestCaseEditor_Title_Success);
        }
    }

    void postSetFailureBackgroundAndToolTip(final EditField editField, final String expectedResult) {
        postAsyncRunnable(new Runnable() {
			public void run() {
				if(isDisposed()) 
					return;
				editField.getControl().setBackground(fFailureColor);
				editField.getControl().setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
				editField.getControl().setToolTipText(expectedResult);
				editField.getControl().setFocus();
			}
		});
	}

    public void postSetOverriddenValueBackgroundAndToolTip(final EditField editField, final String message) {
        postAsyncRunnable(new Runnable() {
            public void run() {
                if(isDisposed()) 
                    return;
                editField.getControl().setBackground(fOkColor);
                editField.getControl().setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
                editField.getControl().setToolTipText(message);
                editField.getControl().setFocus();
            }
        });
    }
    
	void postShowAll() {
        postAsyncRunnable(new Runnable() {
			public void run() {
				if(isDisposed()) 
					return;
				showAll(true);
				actionAll.setChecked(true);
			}
		});
	}
    
    void postResetTestRunStatus(){
        postAsyncRunnable(new Runnable() {
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
        allFailureDetails.clear();
    }
	
	/**
	 * Returns the corresponding test case editor.
	 */
    IpsObjectEditor getTestCaseEditor(){
		return editor;
	}

    /**
	 * Returns the content provider.
	 */
	TestCaseContentProvider getContentProvider() {
		return contentProvider;
	}

    /** 
     * Returns the label provider.
     */
    public TestCaseLabelProvider getLabelProvider() {
        return labelProvider;
    }

    /*
     * Starts the test runner to execute this test case
     */
    private void startTestRunner() {
        try {
            IIpsTestRunner testRunner = IpsPlugin.getDefault().getIpsTestRunner();
            testRunner.setIpsProject(testCase.getIpsProject());
            String repositoryPackage = getTocFilePackage();
            if (repositoryPackage != null)
                testRunner.startTestRunnerJob(repositoryPackage, testCase.getQualifiedName());
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /*
     * Register the listener to run the test and store the expected result.
     */
    private void registerRunTestAndStoreExpectedResultLister() {
        if (runAndStoreExpectedResultListener != null){
            IpsPlugin.getDefault().getIpsTestRunner().removeIpsTestRunListener(runAndStoreExpectedResultListener);
            runAndStoreExpectedResultListener = null;
        }        
        runAndStoreExpectedResultListener = new RunAndStoreExpectedResultListener(this);
        IpsPlugin.getDefault().getIpsTestRunner().removeIpsTestRunListener(this);
        IpsPlugin.getDefault().getIpsTestRunner().addIpsTestRunListener(runAndStoreExpectedResultListener);
    }

    /*
     * Register the test run listener.
     */
    private void registerTestRunListener(){
        IpsPlugin.getDefault().getIpsTestRunner().removeIpsTestRunListener(this);
        IpsPlugin.getDefault().getIpsTestRunner().addIpsTestRunListener(this);
        if (runAndStoreExpectedResultListener != null){
            IpsPlugin.getDefault().getIpsTestRunner().removeIpsTestRunListener(runAndStoreExpectedResultListener);
            runAndStoreExpectedResultListener = null;
        }
    }

    /*
     * Removes the listener for the ips test runner
     */
    private void removeAllListener() {
        testCase.getIpsModel().removeChangeListener(changeListener);
        if (runAndStoreExpectedResultListener != null){
            IpsPlugin.getDefault().getIpsTestRunner().removeIpsTestRunListener(runAndStoreExpectedResultListener);
        }
        IpsPlugin.getDefault().getIpsTestRunner().removeIpsTestRunListener(this);
    }
    
    /**
     * Forwarded listener function for test run and store expected result. This method is calling when
     * the test runner ends and if failures occured during the test run.
     */
    public void testFailureOccuredToStoreExpResult(String qualifiedTestName, final List failureDetailsList) {
        if (!canListenToTestRun(qualifiedTestName)) {
            return;
        }

        postAsyncRunnable(new Runnable() {
            public void run() {
                if (isDisposed())
                    return;
                // register as the runner (removes the run and store exp. result listener)
                registerTestRunListener();
                
                String titleMessage = Messages.TestCaseSection_SectionTitleToolTip_StoredExpectedResults;
                for (Iterator iter = failureDetailsList.iterator(); iter.hasNext();) {
                    final String[] failureDetails = (String[])iter.next();
                    final String formatedMessage = failureDetailsToStoreInExpResultToString(failureDetails);
                    titleMessage += titleMessage.length()>0? "\n" + formatedMessage : formatedMessage; //$NON-NLS-1$
                    form.getContent().setBackground(fFailureColor);
                    form.getContent().setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

                    String objectName = failureDetails[1];
                    String attributeName = failureDetails[2];
                    String actualValue = failureDetails[4];
                    String uniqueKey = getUniqueEditFieldKey(objectName, attributeName);
                    testCaseDetailArea.storeActualValueInExpResult(uniqueKey, actualValue, formatedMessage);                    
                }
                if (failureDetailsList.size()>0){
                    // set the status in the title
                    setTitleStatus(false, false, true, 0, titleMessage);
                } else {
                    // test is ok, no expected value overridden
                    setTitleStatus(false, false, false, 0, ""); //$NON-NLS-1$
                }
            }
        });
    }
    
    /**
     * Forwarded listener function for test run and store expected result
     */
    public void testRunEndedToStoreExpResult(String qualifiedTestName) {
        if (!qualifiedTestName.equals(testCase.getQualifiedName())) {
            return;
        }

        postAsyncRunnable(new Runnable() {
            public void run() {
                if (isDisposed())
                    return;
     
                if (isTestRunError) {
                    setTitleStatus(true, false, false, 0, ""); //$NON-NLS-1$
                    return;
                }

                testCaseDetailArea.updateUi();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public boolean canNavigateToFailure() {
        return true;
    }
}
