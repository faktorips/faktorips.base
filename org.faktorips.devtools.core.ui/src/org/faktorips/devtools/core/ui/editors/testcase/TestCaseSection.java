// CSOFF: FileLengthCheck
// This class is a huge ugly moloch and needs to be completely rewritten scratch.
/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.testcase.IIpsTestRunner;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.StyledCellMessageCueLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.CollapseAllAction;
import org.faktorips.devtools.core.ui.actions.ExpandAllAction;
import org.faktorips.devtools.core.ui.actions.ExpandSelectedAction;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.actions.ToggleAction;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.TreeMessageHoverService;
import org.faktorips.devtools.core.ui.editors.pctype.ContentsChangeListenerForWidget;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.Validatable;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.testcase.TestCase;
import org.faktorips.devtools.model.internal.testcase.TestCaseHierarchyPath;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.testcase.IIpsTestRunListener;
import org.faktorips.devtools.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcase.ITestObject;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.model.testcase.ITestRule;
import org.faktorips.devtools.model.testcase.ITestValue;
import org.faktorips.devtools.model.testcase.TestRuleViolationType;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.testcasetype.ITestRuleParameter;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.StringUtil;

/**
 * Section to display and edit a ips test case.
 */
public class TestCaseSection extends IpsSection implements IIpsTestRunListener {

    public static final String VALUESECTION = "VALUESECTION"; //$NON-NLS-1$

    // Used for saving the current layout style and filter in a eclipse memento.
    private static final String DIALOG_SETTINGS_KEY = "TestCaseSection"; //$NON-NLS-1$
    private static final String CONTENT_TYPE_KEY = "contenttype"; //$NON-NLS-1$
    private static final String SHOW_POLICY_COMPONENT_TYPE = "showpolicycomponenttype"; //$NON-NLS-1$
    private static final String SHOW_ASSOCIATION_KEY = "associations"; //$NON-NLS-1$
    private static final String SHOW_ALL_KEY = "all"; //$NON-NLS-1$

    // Ui refresh interval
    static final int REFRESH_INTERVAL = 400;

    // The tree view which displays all test policy components and test values which are available
    // in this test
    private TreeViewer treeViewer;

    // Contains the test case which is displayed in this section
    private ITestCase testCase;

    // Contains the test case editor
    private IpsObjectEditor editor;

    // UI toolkit for creating the controls
    private UIToolkit toolkit;

    // Title of the test case tree structure section
    private String sectionTreeStructureTitle;

    // Title of the detail section
    private String sectionDetailTitle;

    // Contains the content provider of the test policy component object
    private TestCaseContentProvider contentProvider;

    // Contains the label provider for the test objects
    private TestCaseLabelProvider labelProvider;

    // The detail area of the test case
    private TestCaseDetailArea testCaseDetailArea;

    // Previous selected entries in the tree to
    private List<ITestObject> prevTestObjects;

    // The form which contains this section
    private ScrolledForm form;

    // Indicates if all objects of the test case are displayed or
    // the child objects of selected root are visible
    private boolean showAll = false;

    // Indicates if the tree selection was double clicked
    private boolean isDoubleClicked = false;

    // Actions
    private Action actionAssociation;
    private Action actionAll;
    private ToggleContentTypeAction[] toggleContentTypeActions;
    private Action actionRunAndStoreExpectedResult;

    // Stores the last test run status
    private boolean isTestRunError;
    private boolean isTestRunFailure;
    private int failureCount;

    // Indicates if the corresponding test case type changed
    private boolean testCaseTypeChanged = false;

    // Contains the color for the failure indicators
    private Color fFailureColor;
    // Color to indicate test ok
    private Color fOkColor;

    // Listener for the run test and store expected result action
    private IIpsTestRunListener runAndStoreExpectedResultListener;

    // Contains all failure details for one test run
    private List<FailureDetails> allFailureDetails = new ArrayList<>();

    private TestCaseContentChangeListener changeListener;

    // Contains the menu in the title if available
    private Menu sectionTitleContextMenu;

    private boolean hasNewDialogSettings;

    private IEditorSite site;

    // ips project used to search
    private IIpsProject ipsProject;

    private OpenInNewEditorAction openInNewEditorAction;

    private WritableValue<Boolean> canShowPolicyComponentType = new WritableValue<>(Boolean.TRUE, Boolean.class);

    private RemoveAction removeAction;

    private AddAction addAction;

    private Section structureSection;

    /**
     * Indicates whether a drag and drop operation was started from within this section. If this
     * flag is {@code false} and a drop event occurs, the drag was started from outside the section.
     */
    private boolean localDragAndDrop;

    public TestCaseSection(Composite parent, TestCaseEditor editor, UIToolkit toolkit,
            TestCaseContentProvider contentProvider, final String title, String detailTitle, ScrolledForm form,
            IEditorSite site) {

        super(parent, Section.NO_TITLE, GridData.FILL_BOTH, toolkit);

        this.editor = editor;
        this.contentProvider = contentProvider;
        this.form = form;
        sectionTreeStructureTitle = title;
        sectionDetailTitle = detailTitle;
        this.site = site;
        testCase = contentProvider.getTestCase();
        ipsProject = getTestCase().getIpsProject();

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
        // if the model changed reset the test run status
        changeListener = new TestCaseContentChangeListener(this);
        getTestCase().getIpsModel().addChangeListener(changeListener);
        addDisposeListener(e -> {
            fFailureColor.dispose();
            fOkColor.dispose();
            removeAllListener();
        });
    }

    private void storeActualAsExpectedValue(FailureDetails failureDetails, String tooltip) {
        testCaseDetailArea.storeActualValueInExpResult(findUniqueEditFieldKey(failureDetails),
                failureDetails.getActualValue(), tooltip);
    }

    private String findUniqueEditFieldKey(FailureDetails failureDetails) {
        String uniqueEditFieldKey = getUniqueEditFieldKey(failureDetails.getObjectName(),
                failureDetails.getAttributeName());
        EditField<?> editField = testCaseDetailArea.getEditField(uniqueEditFieldKey);
        if (editField != null) {
            return uniqueEditFieldKey;
        }

        // field wasn't found, maybe the 0 index was not given
        // identify with obj.child => obj#0.child#0
        String fieldKeyWithOffset = failureDetails.getObjectName().replaceAll("\\.", //$NON-NLS-1$
                TestCaseHierarchyPath.OFFSET_SEPARATOR + "0\\."); //$NON-NLS-1$
        uniqueEditFieldKey = getUniqueEditFieldKey(fieldKeyWithOffset + TestCaseHierarchyPath.OFFSET_SEPARATOR + "0", //$NON-NLS-1$
                failureDetails.getAttributeName());
        editField = testCaseDetailArea.getEditField(uniqueEditFieldKey);
        if (editField != null) {
            return uniqueEditFieldKey;
        }

        // field wasn't found, maybe the separator wasn't correct used
        // identify with obj0.child0 => obj#0.child#0
        fieldKeyWithOffset = failureDetails.getObjectName();
        String lastKey = fieldKeyWithOffset.substring(fieldKeyWithOffset.length() - 1, fieldKeyWithOffset.length());
        fieldKeyWithOffset = fieldKeyWithOffset.substring(0, fieldKeyWithOffset.length() - 1)
                + TestCaseHierarchyPath.OFFSET_SEPARATOR + lastKey;
        fieldKeyWithOffset = fieldKeyWithOffset.replaceAll("[0-9]\\.", TestCaseHierarchyPath.OFFSET_SEPARATOR + "0\\."); //$NON-NLS-1$ //$NON-NLS-2$
        uniqueEditFieldKey = getUniqueEditFieldKey(fieldKeyWithOffset, failureDetails.getAttributeName());
        editField = testCaseDetailArea.getEditField(uniqueEditFieldKey);
        if (editField != null) {
            return uniqueEditFieldKey;
        }
        return null;
    }

    private void contentsHasChanged(ContentChangeEvent event) {
        // refresh and check for delta to the test case type
        // if the test case type changed
        try {
            ITestCaseType testCaseType = getTestCase().findTestCaseType(ipsProject);
            if (testCaseType == null) {
                return;
            }
            if (event.getIpsSrcFile().equals(testCaseType.getIpsSrcFile())) {
                testCaseTypeChanged = true;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        this.toolkit = toolkit;

        hookeSectionTitleHyperlink();

        configureToolBar();

        SashForm sashForm = new SashForm(client, SWT.NULL);
        toolkit.getFormToolkit().adapt(sashForm, true, true);
        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        sashForm.setSashWidth(15);

        structureSection = toolkit.getFormToolkit().createSection(sashForm, ExpandableComposite.TITLE_BAR);
        structureSection.setLayoutData(new GridData(GridData.FILL_BOTH));
        structureSection.setText(sectionTreeStructureTitle);

        Composite structureComposite = toolkit.createGridComposite(structureSection, 1, true, true);
        toolkit.paintBordersForComposite(structureComposite);
        structureSection.setClient(structureComposite);
        Tree tree = toolkit.getFormToolkit().createTree(structureComposite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
        tree.setLayoutData(new GridData(GridData.FILL_BOTH));
        setTreeViewer(new TreeViewer(tree));
        getTreeViewer().setContentProvider(contentProvider);
        hookTreeListeners();

        createStructureSectionToolbar(structureSection, canShowPolicyComponentType);
        labelProvider = new TestCaseLabelProvider(ipsProject, canShowPolicyComponentType);

        getTreeViewer().setLabelProvider(new StyledCellMessageCueLabelProvider(labelProvider, ipsProject));
        getTreeViewer().setUseHashlookup(true);
        getTreeViewer().setInput(getTestCase());

        getTreeViewer().addDropSupport(DND.DROP_LINK | DND.DROP_MOVE,
                new Transfer[] { FileTransfer.getInstance(), LocalSelectionTransfer.getTransfer() },
                new TestCaseSectionDropAdapter(getTreeViewer(), this));
        getTreeViewer().addDragSupport(DND.DROP_MOVE, new Transfer[] { LocalSelectionTransfer.getTransfer() },
                new TestCaseSectionDragAdapter(getTreeViewer(), this));
        buildContextMenu();

        // Details section
        testCaseDetailArea = new TestCaseDetailArea(toolkit, contentProvider, this, getBindingContext());
        Section detailAreaSection = testCaseDetailArea.createInitialDetailArea(sashForm, sectionDetailTitle);

        createDetailAreaSectionToolbar(detailAreaSection);

        // Initialize the previous selected objects as empty list
        prevTestObjects = new ArrayList<>();

        // Set the state of the buttons
        updateStructureSectionToolBarEnableStates(null);

        sashForm.setWeights(new int[] { 50, 50 });

        refreshTree();
    }

    private void createDetailAreaSectionToolbar(Section detailAreaSection) {
        // Toolbar item show all
        actionAll = new Action("structureAll", IAction.AS_CHECK_BOX) { //$NON-NLS-1$
            @Override
            public void run() {
                showAllClicked();
            }
        };
        actionAll.setChecked(false);
        actionAll.setToolTipText(Messages.TestCaseSection_ToolBar_FlatStructure);
        actionAll.setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("TestCase_flatView.gif")); //$NON-NLS-1$
        ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
        ToolBar toolbar = toolBarManager.createControl(detailAreaSection);
        toolBarManager.add(actionAll);
        toolBarManager.update(true);
        detailAreaSection.setTextClient(toolbar);
    }

    private void createStructureSectionToolbar(Section structureSection, WritableValue<Boolean> canShowExtension) {
        // Toolbar item show without association
        actionAssociation = new Action("withoutAssociation", IAction.AS_CHECK_BOX) { //$NON-NLS-1$
            @Override
            public void run() {
                showAssociationsClicked();
            }
        };
        // default is show associations
        actionAssociation.setChecked(true);
        actionAssociation.setToolTipText(Messages.TestCaseSection_ToolBar_ShowAssociations);
        actionAssociation.setImageDescriptor(
                IpsUIPlugin.getImageHandling().createImageDescriptor("ShowAssociationTypeNodes.gif")); //$NON-NLS-1$

        ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
        ToolBar toolbar = toolBarManager.createControl(structureSection);
        addAction = new AddAction();
        toolBarManager.add(addAction);
        removeAction = new RemoveAction();
        toolBarManager.add(removeAction);
        toolBarManager.add(new Separator());
        toolBarManager.add(new ExpandAllAction(getTreeViewer()));
        toolBarManager.add(new CollapseAllAction(getTreeViewer()));
        toolBarManager.add(new Separator());
        toolBarManager.add(new ToggleAction(Messages.TestCaseSection_ToolBar_TogglePolicyComponentTypeDisplay,
                "PolicyCmptType.gif", canShowExtension)); //$NON-NLS-1$
        toolBarManager.add(actionAssociation);
        toolBarManager.update(true);
        structureSection.setTextClient(toolbar);
    }

    /**
     * Update the button state depending on the given object.
     */
    private void updateStructureSectionToolBarEnableStates(Object selection) {
        TreeActionEnableState actionEnableState = evaluateTreeActionEnableState(selection);
        addAction.setEnabled(isDataChangeable() && actionEnableState.addEnable);
        removeAction.setEnabled(isDataChangeable() && actionEnableState.removeEnable);
    }

    /**
     * Creates the tool bar actions.
     */
    private void configureToolBar() {
        // Toolbar item run and store expected result
        actionRunAndStoreExpectedResult = new Action("runAndStoreExpectedResult", IAction.AS_PUSH_BUTTON) { //$NON-NLS-1$
            @Override
            public void run() {
                runAndStoreExpectedResultClicked();
            }
        };
        actionRunAndStoreExpectedResult.setToolTipText(Messages.TestCaseSection_Action_RunTestAndStoreExpectedResults);
        actionRunAndStoreExpectedResult.setImageDescriptor(
                IpsUIPlugin.getImageHandling().createImageDescriptor("TestCaseRunAndStoreExpResult.png")); //$NON-NLS-1$
        // enable run test case functionality only if a TOC file exists for this test case
        actionRunAndStoreExpectedResult.setEnabled(getTocFilePackage() != null);

        // Toolbar item run test
        Action actionTest = new Action("runTest", IAction.AS_PUSH_BUTTON) { //$NON-NLS-1$
            @Override
            public void run() {
                runTestClicked();
            }
        };
        actionTest.setToolTipText(Messages.TestCaseSection_ToolBar_RunTest);
        actionTest.setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("TestCaseRun.gif")); //$NON-NLS-1$

        // enable run test case functionality only if a TOC file exists for this test case
        actionTest.setEnabled(getTocFilePackage() != null);

        // Add actions for filtering the content type
        addContentTypeAction();

        form.getToolBarManager().add(new Separator());
        form.getToolBarManager().add(actionRunAndStoreExpectedResult);

        form.getToolBarManager().add(new Separator());
        form.getToolBarManager().add(actionTest);

        form.updateToolBar();
    }

    /**
     * Adds the actions for the content type filter.
     */
    private void addContentTypeAction() {
        toggleContentTypeActions = new ToggleContentTypeAction[] {
                new ToggleContentTypeAction(TestCaseContentProvider.COMBINED),
                new ToggleContentTypeAction(TestCaseContentProvider.INPUT),
                new ToggleContentTypeAction(TestCaseContentProvider.EXPECTED_RESULT) };

        for (ToggleContentTypeAction toggleContentTypeAction : toggleContentTypeActions) {
            form.getToolBarManager().add(toggleContentTypeAction);
        }
    }

    /**
     * Switch (filter) to the selected content type. And reresh the editor contents.
     */
    private void switchContentType(int contentType) {
        ISelection selection = getTreeViewer().getSelection();
        contentProvider.setContentType(contentType);
        refreshTreeAndDetailArea();
        getTreeViewer().setSelection(selection);
    }

    /**
     * Recursive search the given childs for the given target object.
     */
    private TreeItem searchChildsByLabel(String labelPath, TreeItem[] childs) {
        for (TreeItem child : childs) {
            TreeItem currItem = child;
            if (currItem.getText().equals(labelPath)) {
                return currItem;
            }
            currItem = searchChildsByLabel(labelPath, currItem.getItems());
            if (currItem != null) {
                return currItem;
            }
        }
        return null;
    }

    /**
     * Recursive search the given childs for the given target object.
     */
    private TreeItem searchChildsByHierarchyPath(TestCaseHierarchyPath hierarchyPath, TreeItem[] childs) {
        if (!hierarchyPath.hasNext()) {
            return null;
        }
        String currPathItem = hierarchyPath.next();
        TreeItem currItem = null;
        for (TreeItem child : childs) {
            currItem = child;
            if (currItem.getText().equals(currPathItem)) {
                if (hierarchyPath.hasNext()) {
                    currItem = searchChildsByHierarchyPath(hierarchyPath, currItem.getItems());
                }
                break;
            }
        }
        return currItem;
    }

    /**
     * Recursive search the given childs for the given target object.
     */
    private TreeItem searchChildsByObject(ITestPolicyCmpt testPolicyCmpt, ITestPolicyCmptLink link, TreeItem[] childs) {
        if (testPolicyCmpt == null && link == null) {
            return null;
        }

        for (TreeItem child : childs) {
            TreeItem currItem = child;
            if (testPolicyCmpt != null && currItem.getData() instanceof ITestPolicyCmpt) {
                ITestPolicyCmpt elem = (ITestPolicyCmpt)currItem.getData();
                if (elem == testPolicyCmpt) {
                    return currItem;
                }
            }
            if (link != null && currItem.getData() instanceof ITestPolicyCmptLink) {
                ITestPolicyCmptLink elem = (ITestPolicyCmptLink)currItem.getData();
                if (elem == link) {
                    return currItem;
                }
            }
            TreeItem resultItem = searchChildsByObject(testPolicyCmpt, link, currItem.getItems());
            if (resultItem != null) {
                return resultItem;
            }
        }
        return null;
    }

    /**
     * The selection in the tree changed the given object is the new object selected.
     */
    private void selectionInTreeChanged(IStructuredSelection selection) {
        updateStructureSectionToolBarEnableStates(selection.getFirstElement());

        if (!showAll && !selection.isEmpty()) {
            /*
             * show only the elements which belongs to the selection if a test value is selected
             * show the value objects if a test policy component or a child of a policy component is
             * selected, show all elements inside the hierarchy of the root test policy cmpt
             */
            List<ITestObject> objectsToDisplay = new ArrayList<>();
            for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
                Object domainObject = iterator.next();

                if (domainObject instanceof ITestValue) {
                    objectsToDisplay.add((ITestValue)domainObject);
                } else if (domainObject instanceof TestCaseTypeRule) {
                    // show all test rule objects if the corresponding parameter is chosen
                    ITestRule[] testRules = getTestCase().getTestRule(((TestCaseTypeRule)domainObject).getName());
                    for (ITestRule testRule : testRules) {
                        objectsToDisplay.add(testRule);
                    }
                } else if (domainObject instanceof ITestRule) {
                    /*
                     * in case of a rule selection don't change the detail area if the rule is
                     * already displayed (e.g. the root rule node is selected) otherwise display
                     * only the selected rule
                     */
                    if (prevTestObjects.contains(domainObject)) {
                        objectsToDisplay = prevTestObjects;
                        continue;
                    } else {
                        objectsToDisplay.add((ITestRule)domainObject);
                    }
                } else {
                    ITestPolicyCmpt testPolicyCmpt = getTestPolicyCmpFromDomainObject(domainObject);
                    if (testPolicyCmpt == null) {
                        prevTestObjects = null;
                        break;
                    }
                    objectsToDisplay.add(testPolicyCmpt);
                }
            }
            // if the selection has changed redraw the detail section
            if (!objectsToDisplay.equals(prevTestObjects)) {
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

    /**
     * Evaluates the state of the available actions
     */
    private TreeActionEnableState evaluateTreeActionEnableState(Object selection) {
        TreeActionEnableState actionEnableState = new TreeActionEnableState();

        if (selection == null || !isDataChangeable()) {
            return actionEnableState;
        }

        if (selection instanceof TestCaseTypeAssociation) {
            TestCaseTypeAssociation association = (TestCaseTypeAssociation)selection;
            try {
                IPolicyCmptTypeAssociation modelAssociation = null;
                if (association.getParentTestPolicyCmpt() == null) {
                    actionEnableState.addEnable = isNoTestPolicyCmptExistsFor(association);
                    actionEnableState.removeEnable = false;
                } else {
                    modelAssociation = association
                            .findAssociation(association.getParentTestPolicyCmpt().getIpsProject());
                }
                if (modelAssociation != null) {
                    actionEnableState.addEnable = true;
                    actionEnableState.removeEnable = false;
                }
                // else {
                // failure in test case type definition
                // test case type or model association not found
                // no add and removed allowed, because the test case type definition is wrong
                // }
            } catch (CoreException e) {
                // disable add and enable remove button and ignore exception
                // maybe the test case type model and test case are inconsistent
                // in this case the whole association could be deleted but no new children could be
                // added
                actionEnableState.removeEnable = true;
            }
        } else if (selection instanceof ITestPolicyCmpt) {
            ITestPolicyCmpt testPolicyCmpt = (ITestPolicyCmpt)selection;
            try {
                ITestPolicyCmptTypeParameter param = testPolicyCmpt.findTestPolicyCmptTypeParameter(ipsProject);
                // root elements couldn't be deleted
                actionEnableState.removeEnable = true;
                // root elements couldn't be deleted
                actionEnableState.moveEnable = !testPolicyCmpt.isRoot();
                actionEnableState.renameEnable = true;

                IPolicyCmptType paramType = param.findPolicyCmptType(ipsProject);
                // type parameter exists,
                // enable add button only if links are defined
                actionEnableState.addEnable = param.getTestPolicyCmptTypeParamChilds().length > 0;
                // product component select button is enabled for product configurable
                // policyCmptTypes
                actionEnableState.productCmptChangeEnable = paramType == null
                        || paramType.isConfigurableByProductCmptType();
                /*
                 * product component remove button is only enabled if the type parameter specifies
                 * that a product component is not required (formally: if
                 * isRequiresProductCmpt()==false) and if no product component is set at the same
                 * time.
                 */
                actionEnableState.productCmptRemoveEnable = !param.isRequiresProductCmpt()
                        && testPolicyCmpt.hasProductCmpt();
                // open in new editor is always enabled for ITestPolicyCmpt
                actionEnableState.openInNewEditorEnable = true;
            } catch (CoreException e) {
                // disable add and remove button and ignore exception
                // maybe the test case type model and test case are inconsistent
                // in this case the parent link could be removed but not this child element
            }
        } else if (selection instanceof ITestPolicyCmptLink) {
            // the link object indicates, that the test policy type parameter for this link
            // not exists, therefore only remove is enabled
            actionEnableState.removeEnable = true;
        } else if (selection instanceof TestCaseTypeRule) {
            // group of test rule parameter (test rules i.e. validation rules)
            actionEnableState.addEnable = true;
        } else if (selection instanceof ITestRule) {
            // a concrete test rule inside the test case
            actionEnableState.removeEnable = true;
        }

        return actionEnableState;
    }

    private boolean isNoTestPolicyCmptExistsFor(TestCaseTypeAssociation association) {
        ITestPolicyCmpt[] testPolicyCmpts = getTestCase().getTestPolicyCmpts();
        for (ITestPolicyCmpt testPolicyCmpt : testPolicyCmpts) {
            if (association.getName().equals(testPolicyCmpt.getTestParameterName())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add the tree listener to the tree.
     */
    private void hookTreeListeners() {
        getTreeViewer().addSelectionChangedListener(event -> {
            if (event.getSelection() instanceof IStructuredSelection) {
                IStructuredSelection selection = (IStructuredSelection)event.getSelection();
                selectionInTreeChanged(selection);
            }
        });
        new TreeMessageHoverService(getTreeViewer()) {
            @Override
            protected MessageList getMessagesFor(Object element) throws CoreException {
                if (element instanceof Validatable) {
                    return ((Validatable)element).validate(ipsProject);
                } else {
                    return null;
                }
            }
        };

        getTreeViewer().addDoubleClickListener(event -> {
            if (!(event.getSelection() instanceof IStructuredSelection)) {
                return;
            }
            Object selected = ((IStructuredSelection)event.getSelection()).getFirstElement();

            // set focus to the first edit field in details area of the clicked element or
            // if an association was clicked set the focus to the entry in the tree
            if (selected instanceof ITestPolicyCmptLink) {
                // an association link was clicked
                ITestPolicyCmpt target = null;
                target = ((ITestPolicyCmptLink)selected).findTarget();
                if (target != null) {
                    selectInTreeByObject(target, true);
                }
            } else {
                isDoubleClicked = true;
                selectInDetailArea(selected, true);
            }
        });
        getTreeViewer().getTree().addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.DEL) {
                    RemoveAction removeAction = new RemoveAction();
                    removeAction.run();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // Nothing to do
            }
        });

        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                if ((e.stateMask & SWT.CTRL) != 0) {
                    openInNewEditorAction.run();
                }
            }
        };
        getTreeViewer().getTree().addMouseListener(adapter);
    }

    /**
     * Build the context menu
     */
    private void buildContextMenu() {
        MenuManager manager = new MenuManager();
        manager.setRemoveAllWhenShown(false);
        manager.add(new ExpandSelectedAction(getTreeViewer()));
        manager.add(new Separator());
        manager.add(new AddAction());
        manager.add(new RemoveAction());
        manager.add(new Separator());
        manager.add(new RenamePolicyCmptAction());
        manager.add(new ProductCmptChangeAction());
        manager.add(new ProductCmptRemoveAction());
        manager.add(new Separator());
        manager.add(new MoveAction(true));
        manager.add(new MoveAction(false));
        manager.add(new Separator());
        openInNewEditorAction = new OpenInNewEditorAction();
        manager.add(openInNewEditorAction);
        Menu contextMenu = manager.createContextMenu(getTreeViewer().getControl());
        getTreeViewer().getControl().setMenu(contextMenu);
    }

    /**
     * Select the section which is identified by the unique path.
     */
    private void selectSection(String uniquePath) {
        testCaseDetailArea.selectSection(uniquePath);
    }

    /**
     * Select the given object in the detail area, change the color of the section.
     * 
     * @param selected The object which will be searched in the detail area.
     * @param withFocusChange If <code>true</code> also the focus will be set to the first edit
     *            field in the found section. If <code>false</code> no focus will be moved.
     */
    void selectInDetailArea(Object selected, boolean withFocusChange) {
        String uniquePath = ""; //$NON-NLS-1$
        testCaseDetailArea.resetSectionColors(form);
        if (selected instanceof ITestValue) {
            uniquePath = VALUESECTION + ((ITestValue)selected).getTestParameterName();
        } else {
            uniquePath = getUniqueKey(selected);
        }

        if (selected instanceof ITestValue || selected instanceof ITestRule) {
            if (withFocusChange) {
                EditField<?> firstField = testCaseDetailArea.getTestValueEditField(uniquePath);
                if (firstField != null) {
                    isDoubleClicked = true;
                    firstField.getControl().setFocus();
                }
            }
        } else {
            // select first attribute edit field
            if (uniquePath.length() > 0) {
                if (withFocusChange) {
                    EditField<?> firstField = testCaseDetailArea.getFirstAttributeEditField(uniquePath);
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
    String getUniqueKey(Object selected) {
        String uniquePath = ""; //$NON-NLS-1$

        if (selected instanceof ITestRule) {
            ITestRule rule = (ITestRule)selected;
            try {
                IValidationRule validationRule = rule.findValidationRule(ipsProject);
                uniquePath = validationRule == null ? null : validationRule.getMessageCode();
            } catch (CoreException e1) {
                // ignore exception while seraching the validation rule object
            }
            if (uniquePath == null) {
                // validation rule not found use rule name as identifier
                uniquePath = rule.getValidationRule();
            }
            return rule.getTestParameterName() + uniquePath;
        } else if (selected instanceof ITestValue) {
            return TestCaseSection.VALUESECTION + ((ITestValue)selected).getTestValueParameter();
        }

        if (selected instanceof ITestPolicyCmptLink) {
            ITestPolicyCmptLink link = (ITestPolicyCmptLink)selected;
            uniquePath = "." + link.getTestPolicyCmptTypeParameter() + TestCaseHierarchyPath.OFFSET_SEPARATOR //$NON-NLS-1$
                    + link.getId();
        }
        ITestPolicyCmpt currTestPolicyCmpt = getTestPolicyCmpFromDomainObject(selected);
        if (currTestPolicyCmpt == null) {
            return ""; //$NON-NLS-1$
        }
        if (!currTestPolicyCmpt.isRoot()) {
            uniquePath = (currTestPolicyCmpt).getName();
            while (!currTestPolicyCmpt.isRoot()) {
                uniquePath = ((ITestPolicyCmptLink)currTestPolicyCmpt.getParent()).getTestPolicyCmptTypeParameter()
                        + uniquePath;
                currTestPolicyCmpt = getTestPolicyCmpFromDomainObject(currTestPolicyCmpt.getParent());
                uniquePath = currTestPolicyCmpt.getName() + "." + uniquePath; //$NON-NLS-1$
            }
        } else {
            uniquePath = currTestPolicyCmpt.getName() + uniquePath;
        }
        return uniquePath;
    }

    /**
     * Gets the unique key for the given test policy component and attribute
     */
    String getUniqueKey(ITestPolicyCmpt testPolicyCmpt, ITestAttributeValue attributeValue) {
        return getUniqueKey(testPolicyCmpt) + "/" + attributeValue.getTestAttribute(); //$NON-NLS-1$
    }

    /**
     * Show associations toolbar button was clicked.
     */
    private void showAssociationsClicked() {
        ISelection selection = getTreeViewer().getSelection();
        contentProvider.setWithoutAssociations(!contentProvider.isWithoutAssociations());
        refreshTreeAndDetailArea();
        getTreeViewer().setSelection(selection);
    }

    private void addRootTestPolicyCmptObject(final TestCaseTypeAssociation associationType,
            final ITestPolicyCmptTypeParameter testPolicyCmptTypeParam) {

        final IWorkspaceRunnable runnable = $ -> {
            ITestPolicyCmpt newTestPolicyCmpt = ((TestCase)getTestCase())
                    .addRootTestPolicyCmpt(testPolicyCmptTypeParam);
            if (testPolicyCmptTypeParam.isRequiresProductCmpt()) {
                boolean productCmptAssigned = changeProductCmpt(newTestPolicyCmpt);
                if (productCmptAssigned) {
                    newTestPolicyCmpt.addRequiredLinks(ipsProject);
                }
            }
            refreshTreeAndDetailArea();
            expandTreeAfterAdd(associationType, newTestPolicyCmpt);
            selectInTreeByObject(newTestPolicyCmpt, true);
            selectionInTreeChanged((IStructuredSelection)getTreeViewer().getSelection());
        };

        Runnable runnableForBusyIndicator = () -> IIpsModel.get().runAndQueueChangeEvents(runnable, null);
        BusyIndicator.showWhile(getDisplay(), runnableForBusyIndicator);
    }

    /**
     * Expands the tree viewer so that the provided test policy component and all it's children are
     * visible.
     * <p>
     * This operation can handle both configurations of
     * {@link TestCaseContentProvider#isWithoutAssociations()}.
     * 
     * @param parentItemParam parent of the provided test policy component within the tree (this is
     *            either a {@link TestCaseTypeAssociation} or another test policy component
     * @param testPolicyCmpt test policy component to expand in the tree (including children)
     */
    void expandTreeAfterAdd(Object parentItemParam, ITestPolicyCmpt testPolicyCmpt) {
        Object parentItem = parentItemParam;
        /*
         * If the user adds a test policy component onto another test policy component AND the
         * content provider is configured to also contain association nodes, we need to expand the
         * correct association node instead of the test policy component.
         */
        if (parentItem instanceof ITestPolicyCmpt && !contentProvider.isWithoutAssociations()) {
            for (Object child : contentProvider.getChildren(parentItem)) {
                try {
                    if (testPolicyCmpt.findTestPolicyCmptTypeParameter(ipsProject)
                            .equals(((TestCaseTypeAssociation)child).getTestPolicyCmptTypeParam())) {
                        parentItem = child;
                        break;
                    }
                } catch (CoreException e) {
                    throw new CoreRuntimeException(e);
                }
            }
        }

        getTreeViewer().expandToLevel(parentItem, 1);
        for (Object child : contentProvider.getChildren(parentItem)) {
            if (child.equals(testPolicyCmpt)) {
                getTreeViewer().expandToLevel(child, AbstractTreeViewer.ALL_LEVELS);
            }
        }
    }

    /**
     * Adds a new link target to the given association type
     */
    private ITestPolicyCmptLink addNewLink(TestCaseTypeAssociation associationType,
            String productCmptQualifiedName,
            String policyCmptTypeQualifiedName,
            String targetName) throws CoreException {

        ITestPolicyCmptLink newAssociation = associationType.getParentTestPolicyCmpt().addTestPcTypeLink(
                associationType.getTestPolicyCmptTypeParam(), productCmptQualifiedName, policyCmptTypeQualifiedName,
                targetName, true);
        ITestPolicyCmpt newTestPolicyCmpt = newAssociation.findTarget();
        if (newTestPolicyCmpt == null) {
            throw new CoreException(new IpsStatus(Messages.TestCaseSection_Error_CreatingAssociation));
        }
        return newAssociation;
    }

    private boolean changeProductCmpt(ITestPolicyCmpt testPolicyCmpt) throws CoreException {
        ITestPolicyCmptTypeParameter testTypeParam;
        try {
            testTypeParam = testPolicyCmpt.findTestPolicyCmptTypeParameter(ipsProject);
        } catch (CoreException e) {
            // ignored, the validation shows the unknown type failure message
            return false;
        }
        IPolicyCmptType policyCmptType = testTypeParam.findPolicyCmptType(testTypeParam.getIpsProject());
        if (policyCmptType == null) {
            // policy cmpt type not found, this is a validation error
            return false;
        }
        IProductCmptType productCmptType = policyCmptType.findProductCmptType(policyCmptType.getIpsProject());
        if (productCmptType == null) {
            // policy cmpt type not found, this is a validation error
            return false;
        }
        String[] productCmptQualifiedNames = selectProductCmptsDialog(testTypeParam,
                testPolicyCmpt.getParentTestPolicyCmpt(), false);
        if (productCmptQualifiedNames == null || productCmptQualifiedNames.length == 0) {
            // cancel
            return false;
        }
        testPolicyCmpt.setProductCmptAndNameAfterIfApplicable(productCmptQualifiedNames[0]);
        // reset the stored policy cmpt type, because the policy cmpt type can now
        // be searched using the product cmpt
        testPolicyCmpt.setPolicyCmptType(""); //$NON-NLS-1$

        boolean updateTestAttrValuesWithDefault = MessageDialog.openQuestion(getShell(),
                Messages.TestCaseSection_DialogOverwriteWithDefault_Title,
                Messages.TestCaseSection_DialogOverwriteWithDefault_Text);
        if (updateTestAttrValuesWithDefault) {
            testPolicyCmpt.updateDefaultTestAttributeValues();
        }

        refreshTreeAndDetailArea();

        return true;
    }

    private void changeProductCmpt() throws CoreException {
        ISelection selection = getTreeViewer().getSelection();
        if (selection instanceof IStructuredSelection) {
            Object selectedObj = ((IStructuredSelection)selection).getFirstElement();
            if (selectedObj instanceof ITestPolicyCmpt) {
                changeProductCmpt((ITestPolicyCmpt)selectedObj);
                getTreeViewer().setSelection(selection);
            }
        }
    }

    private void resetProductCmptToEmpty() {
        ISelection selection = getTreeViewer().getSelection();
        if (selection instanceof IStructuredSelection) {
            Object selectedObj = ((IStructuredSelection)selection).getFirstElement();
            if (selectedObj instanceof ITestPolicyCmpt) {
                ((ITestPolicyCmpt)selectedObj).setProductCmptAndNameAfterIfApplicable(""); //$NON-NLS-1$
                getTreeViewer().setSelection(selection);
            }
        }
    }

    private void renamePolicyCmpt(final ITestPolicyCmpt testPolicyCmpt) {
        IInputValidator validator = new IInputValidator() {
            @Override
            public String isValid(String nameCandidate) {
                if (testPolicyCmpt.getName().equals(nameCandidate)) {
                    return Messages.TestCaseSection_Rename_Problem_ChooseDifferentName;
                }
                if (!isNameValid(nameCandidate)) {
                    return Messages.TestCaseSection_Rename_Problem_NameInUse;
                }
                return null;
            }

            /*
             * Returns true if generateUniqueNameForTestPolicyCmpt() creates the same string as the
             * given one.
             */
            private boolean isNameValid(String nameCandidate) {
                ITestCase testCase = testPolicyCmpt.getTestCase();
                return testCase.generateUniqueNameForTestPolicyCmpt(testPolicyCmpt, nameCandidate)
                        .equals(nameCandidate);
            }
        };

        InputDialog dialog = new InputDialog(getShell(), Messages.TestCaseSection_RenameDialogTitle,
                Messages.TestCaseSection_RenameDialogDescription, testPolicyCmpt.getName(), validator);
        dialog.setBlockOnOpen(true);
        int result = dialog.open();
        if (result == Window.OK) {
            testPolicyCmpt.setName(dialog.getValue());
        }
    }

    private void showAllClicked() {
        ISelection selection = getTreeViewer().getSelection();
        showAll(!showAll);
        getTreeViewer().setSelection(selection);
    }

    private void runAndStoreExpectedResultClicked() {
        if (!isDataChangeable()) {
            return;
        }
        if (containsErrors()) {
            return;
        }
        boolean overwriteExpectedResult = MessageDialog.openQuestion(getShell(),
                Messages.TestCaseSection_MessageDialog_TitleRunTestAndStoreExpectedResults,
                Messages.TestCaseSection_MessageDialog_QuestionRunTestAndStoreExpectedResults);
        if (!overwriteExpectedResult) {
            return;
        }
        resetTestRunStatus();
        showAll(true);
        registerRunTestAndStoreExpectedResultLister();
        startTestRunner();
    }

    /**
     * Show all objects in the detail area if showAll is <code>true</code>, otherwise show only the
     * object which is selected in the tree
     */
    private void showAll(boolean showAll) {
        this.showAll = showAll;
        if (showAll) {
            // show all test objects which are provided by the content provider
            createDetailsSectionsForAll();
            getTreeViewer().expandAll();
        } else {
            // show only the selected test objects
            ISelection selection = getTreeViewer().getSelection();
            if (selection instanceof IStructuredSelection) {
                Object domainObject = ((IStructuredSelection)selection).getFirstElement();
                testCaseDetailArea.clearDetailArea();
                List<ITestObject> list = new ArrayList<>();
                if (domainObject instanceof ITestValue) {
                    list.addAll(Arrays.asList((ITestValue)domainObject));
                    testCaseDetailArea.createTestObjectSections(list);
                    prevTestObjects = list;
                } else {
                    ITestPolicyCmpt testPolicyCmpt = getTestPolicyCmpFromDomainObject(domainObject);
                    if (testPolicyCmpt != null) {
                        list.addAll(Arrays.asList(testPolicyCmpt));
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

    /**
     * Draws the detail section for all test object in the test case, which are provided by the
     * content provider.
     */
    private void createDetailsSectionsForAll() {
        testCaseDetailArea.clearDetailArea();
        List<ITestObject> list = new ArrayList<>();
        list.addAll(Arrays.asList(contentProvider.getTestObjects()));
        testCaseDetailArea.createTestObjectSections(list);
        prevTestObjects = list;
    }

    private void runTestClicked() {
        resetTestRunStatus();
        clearTestFailures(true);
        if (containsErrors()) {
            return;
        }
        registerTestRunListener();
        startTestRunner();
    }

    private boolean containsErrors() {
        try {
            if (getTestCase().validate(ipsProject).containsErrorMsg()) {
                MessageDialog.openWarning(getShell(), Messages.TestCaseSection_MessageDialog_TitleInfoTestNotExecuted,
                        Messages.TestCaseSection_MessageDialog_TextInfoTestNotExecuted);
                return true;
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return true;
        }
        return false;
    }

    /**
     * Returns the toc file package name which stores the current test case.
     */
    private String getTocFilePackage() {
        if (getTestCase().isContainedInArchive()) {
            // not available for archived files, because archives doesn't have toc files (the
            // content is not based on source folder)
            return null;
        }
        IIpsPackageFragment packageFragment = getTestCase().getIpsPackageFragment();
        if (packageFragment == null) {
            return null;
        }
        IIpsPackageFragmentRoot root = packageFragment.getRoot();
        IIpsArtefactBuilderSet builderSet = root.getIpsProject().getIpsArtefactBuilderSet();
        return builderSet.getRuntimeRepositoryTocResourceName(root);
    }

    /**
     * Shows the select product component dialog and returns the selected product component
     * qualified names. Returns <code>null</code> if no selection or an unsupported type was chosen.
     * 
     * @throws CoreException If an error occurs
     */
    private String[] selectProductCmptsDialog(ITestPolicyCmptTypeParameter testTypeParam,
            ITestPolicyCmpt testPolicyCmptParent,
            boolean multiSelectiion) throws CoreException {

        return selectIpsSrcFileDialog(multiSelectiion, getProductCmptSrcFiles(testTypeParam, testPolicyCmptParent),
                Messages.TestCaseSection_DialogSelectProductCmpt_Title,
                Messages.TestCaseSection_DialogSelectProductCmpt_Description);
    }

    /**
     * Shows the select policy cmpt type dialog and returns the selected policy cmpt type qualified
     * names. Returns <code>null</code> if no selection or an unsupported type was chosen.
     */
    private String[] selectPolicyCmptTypeDialog(ITestPolicyCmptTypeParameter testTypeParam, boolean multiSelectiion) {
        SubPolicyCmptTypesSrcFileFinder runnable = new SubPolicyCmptTypesSrcFileFinder(testTypeParam);
        BusyIndicator.showWhile(getDisplay(), runnable);
        return selectIpsSrcFileDialog(multiSelectiion, runnable.getPolicyCmptTypesSrcFiles(),
                Messages.TestCaseSection_SelectTypeDialog_Title, Messages.TestCaseSection_SelectTypeDialog_Description);
    }

    private String[] selectIpsSrcFileDialog(boolean multiSelectiion,
            IIpsSrcFile[] elements,
            String title,
            String description) {

        @SuppressWarnings("deprecation")
        org.faktorips.devtools.core.ui.IpsObjectSelectionDialog dialog = new org.faktorips.devtools.core.ui.IpsObjectSelectionDialog(
                getShell(), title, description);
        dialog.setElements(elements);
        dialog.setMultipleSelection(multiSelectiion);
        if (dialog.open() == Window.OK) {
            if (dialog.getResult().length > 0) {
                Object[] productCmpts = dialog.getResult();
                String[] qualifedNames = new String[productCmpts.length];
                for (int i = 0; i < productCmpts.length; i++) {
                    qualifedNames[i] = ((IIpsSrcFile)productCmpts[i]).getQualifiedNameType().getName();
                }
                return qualifedNames;
            }
        }
        return null;
    }

    IIpsSrcFile[] getProductCmptSrcFiles(ITestPolicyCmptTypeParameter testTypeParam,
            ITestPolicyCmpt testPolicyCmptParent) {

        try {
            return testTypeParam.getAllowedProductCmpt(ipsProject,
                    testPolicyCmptParent != null ? testPolicyCmptParent.findProductCmpt(ipsProject) : null);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private IIpsSrcFile[] getPolicyCmptTypesSrcFiles(ITestPolicyCmptTypeParameter testTypeParam) throws CoreException {

        IPolicyCmptType policyCmptType = ipsProject.findPolicyCmptType(testTypeParam.getPolicyCmptType());
        if (policyCmptType == null) {
            return new IIpsSrcFile[0];
        }

        List<IType> allSubtypes = policyCmptType.getSubtypeHierarchy().getAllSubtypes(policyCmptType);
        List<IIpsSrcFile> allIpsSrcFilesSubtypes = new ArrayList<>();
        for (IType subtype : allSubtypes) {
            if (subtype.isAbstract()) {
                continue;
            }
            allIpsSrcFilesSubtypes.add(subtype.getIpsSrcFile());
        }
        if (!policyCmptType.isAbstract()) {
            allIpsSrcFilesSubtypes.add(policyCmptType.getIpsSrcFile());
        }
        return allIpsSrcFilesSubtypes.toArray(new IIpsSrcFile[allIpsSrcFilesSubtypes.size()]);
    }

    /**
     * Refresh the tree and form.
     */
    protected void refreshTreeAndDetailArea() {
        form.setRedraw(false);
        try {
            // redraw the detail area in async way to reduce the time of refreshing
            Runnable runnable = () -> {
                refreshTree();
                testCaseDetailArea.clearDetailArea();
                if (showAll) {
                    createDetailsSectionsForAll();
                } else {
                    if (prevTestObjects != null) {
                        testCaseDetailArea.createTestObjectSections(prevTestObjects);
                    }
                }
                redrawForm();
            };
            getDisplay().syncExec(runnable);
        } finally {
            form.setRedraw(true);
        }
    }

    /**
     * Refresh the tree.
     */
    void refreshTree() {
        getTreeViewer().refresh();
    }

    /**
     * Returns the corresponding test policy component object from the domain object. The domain
     * object could either be a policy component object or a link object. In case of a link object
     * the parent test policy component will be returned. Returns <code>null</code> if the
     * domainObject is not such a kind of object, in this case additionally an error will be logged.
     */
    private ITestPolicyCmpt getTestPolicyCmpFromDomainObject(Object domainObject) {
        ITestPolicyCmpt testPolicyCmpt = null;
        if (domainObject instanceof ITestPolicyCmpt) {
            testPolicyCmpt = (ITestPolicyCmpt)domainObject;
        } else if (domainObject instanceof ITestPolicyCmptLink) {
            ITestPolicyCmptLink testPcTypeAssociation = (ITestPolicyCmptLink)domainObject;
            testPolicyCmpt = (ITestPolicyCmpt)testPcTypeAssociation.getParent();
        } else if (domainObject instanceof TestCaseTypeAssociation) {
            TestCaseTypeAssociation testCaseTypeAssociation = (TestCaseTypeAssociation)domainObject;
            testPolicyCmpt = testCaseTypeAssociation.getParentTestPolicyCmpt();
            if (testPolicyCmpt != null) {
                return testPolicyCmpt;
            }
            // this must be the root node
            // find and return the correspondinf test object in the test case
            ITestPolicyCmpt[] testPolicyCmpts = getTestCase().getTestPolicyCmpts();
            for (ITestPolicyCmpt testPolicyCmpt2 : testPolicyCmpts) {
                if (testCaseTypeAssociation.getTestPolicyCmptTypeParam().getName()
                        .equals(testPolicyCmpt2.getTestParameterName())) {
                    return testPolicyCmpt2;
                }
            }
        }
        return testPolicyCmpt;
    }

    void selectInTreeByObject(ITestPolicyCmpt testPolicyCmpt, boolean focusChange) {
        selectInTreeByObject(testPolicyCmpt, null, focusChange);
    }

    void selectInTreeByObject(ITestPolicyCmptLink link, boolean focusChange) {
        selectInTreeByObject(null, link, focusChange);
    }

    void selectInTreeByObject(ITestRule testRule) {
        getTreeViewer().setSelection(new StructuredSelection(testRule));
    }

    /**
     * Select the given test policy component in the tree.
     */
    private void selectInTreeByObject(ITestPolicyCmpt testPolicyCmpt, ITestPolicyCmptLink link, boolean focusChange) {
        if (!isDoubleClicked) {
            if (testPolicyCmpt != null) {
                selectInDetailArea(testPolicyCmpt, false);
            } else if (link != null) {
                selectInDetailArea(link, false);
            }

            // goto the corresponding test policy component in the tree
            Tree tree = getTreeViewer().getTree();
            TreeItem found = searchChildsByObject(testPolicyCmpt, link, tree.getItems());
            if (found != null) {
                // select the tree entry
                getTreeViewer().setSelection(new StructuredSelection(found.getData()));
                if (focusChange) {
                    getTreeViewer().getTree().setFocus();
                    updateStructureSectionToolBarEnableStates(found.getData());
                }
            }
        } else {
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

        Tree tree = getTreeViewer().getTree();
        TreeItem found = null;
        if (hierarchyPath.count() > 1) {
            found = searchChildsByHierarchyPath(hierarchyPath, tree.getItems());
        } else {
            found = searchChildsByLabel(label, tree.getItems());
        }
        if (found != null) {
            // select the tree entry
            TreeItem[] select = new TreeItem[1];
            select[0] = found;
            tree.setSelection(select);
            getTreeViewer().getTree().setFocus();
            updateStructureSectionToolBarEnableStates(found.getData());
        }
    }

    /**
     * Selects the given test object in the tree.
     */
    void selectTestObjectInTree(ITestObject testObject) {
        if (!isDoubleClicked) {
            selectInDetailArea(testObject, false);
            // goto the corresponding value object in the tree
            Tree tree = getTreeViewer().getTree();

            TreeItem found = searchChildsByLabel(labelProvider.getText(testObject), tree.getItems());
            if (found != null) {
                // select the tree entry
                TreeItem[] select = new TreeItem[1];
                select[0] = found;
                tree.setSelection(select);
            }
        } else {
            isDoubleClicked = false;
            selectInDetailArea(testObject, true);
        }
    }

    /**
     * Redraws the form.
     */
    private void redrawForm() {
        try {
            // Disable redraw for the time being to avoid flickering
            form.setRedraw(false);
            pack();
            getParent().layout();
            getBindingContext().updateUI();
        } finally {
            // Restore redraw
            form.setRedraw(true);
        }
        refreshTree();
    }

    /**
     * Displays the tree select dialog and return the selected object. Returns <code>null</code> if
     * no or a wrong object was chosen or the user select nothing.
     */
    private ITestPolicyCmpt selectAssoziationByTreeDialog(String filteredPolicyCmptType) {
        ITestPolicyCmpt testPolicyCmpt = null;
        TestPolicyCmptSelectionDialog dialog = new TestPolicyCmptSelectionDialog(getShell(), toolkit, getTestCase(),
                TestCaseContentProvider.COMBINED, filteredPolicyCmptType);
        if (dialog.open() == Window.OK) {
            if (dialog.getResult().length > 0) {
                if (dialog.getResult()[0] instanceof ITestPolicyCmpt) {
                    testPolicyCmpt = (ITestPolicyCmpt)dialog.getResult()[0];
                }
            }
        }
        return testPolicyCmpt;
    }

    @Override
    protected void performRefresh() {
        refreshTree();

        // reset the test status color of the title area
        postResetTestRunStatus();

        // if in the meanwhile the test case type changed check for inconsistence between test case
        // and test case type, only if the data is changeable
        if (testCaseTypeChanged) {
            testCaseTypeChanged = false;
            refreshTreeAndDetailArea();
        }
    }

    /**
     * Converts the given failure details to one failure detail row.
     */
    private String failureDetailsToString(String[] failureDetails) {
        String failureFormat = Messages.TestCaseSection_FailureFormat_FailureIn;
        String failureActual = Messages.TestCaseSection_FailureFormat_Actual;
        String failureExpected = Messages.TestCaseSection_FailureFormat_Expected;
        String failureFormatAttribute = Messages.TestCaseSection_FailureFormat_Attribute;
        String failureFormatObject = Messages.TestCaseSection_FailureFormat_Object;
        String failureFormatMessage = Messages.TestCaseSection_FailureFormat_Message;

        String[] failureDetailsToFormat = new String[failureDetails.length];
        System.arraycopy(failureDetails, 0, failureDetailsToFormat, 0, failureDetails.length);

        failureDetailsToFormat[3] = TestRuleViolationType.mapRuleValueTest(failureDetailsToFormat[3]);
        failureDetailsToFormat[4] = TestRuleViolationType.mapRuleValueTest(failureDetailsToFormat[4]);

        failureDetailsToFormat[1] = getLastObjectIdentifier(failureDetailsToFormat[1]);

        if (failureDetailsToFormat.length > 3) {
            failureFormat = failureFormat + (failureExpected);
        }
        if (failureDetailsToFormat.length > 4) {
            failureFormat = failureFormat + (failureActual);
        }
        if (failureDetailsToFormat.length > 2) {
            failureFormat = failureFormat + (!"<null>".equals(failureDetailsToFormat[2]) ? failureFormatAttribute : ""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (failureDetailsToFormat.length > 1) {
            failureFormat = failureFormat + (!"<null>".equals(failureDetailsToFormat[1]) ? failureFormatObject : ""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (failureDetailsToFormat.length > 5) {
            failureFormat = failureFormat + (!"<null>".equals(failureDetailsToFormat[5]) ? failureFormatMessage : ""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return MessageFormat.format(failureFormat, (Object[])failureDetailsToFormat);
    }

    private String getLastObjectIdentifier(String objectPath) {
        return StringUtil.unqualifiedName(objectPath);
    }

    /**
     * Converts the given failure details to one store actual value in expected result detail row.
     */
    private String failureDetailsToStoreInExpResultToString(String[] failureDetails) {
        return failureDetailsToString(failureDetails);
    }

    @Override
    public void testErrorOccured(String qualifiedTestName, String[] errorDetails) {
        if (StringUtils.isNotEmpty(qualifiedTestName) && !canListenToTestRun(qualifiedTestName)) {
            return;
        }
        isTestRunError = true;
    }

    @Override
    public void testFailureOccured(final String qualifiedTestName, final String[] failureDetails) {
        if (!canListenToTestRun(qualifiedTestName)) {
            return;
        }

        isTestRunFailure = true;
        failureCount++;

        final FailureDetails failureDetailsObj = new FailureDetails(failureDetails);
        final String formatedFailure = failureDetailsToString(failureDetails);

        // store the failure details for later use (e.g. store all actual values)
        allFailureDetails.add(failureDetailsObj);

        // display failure in several gui controls
        postAsyncRunnable(() -> {
            if (isDisposed()) {
                return;
            }

            // if not all fields are visible, first show all fields
            // to ensure that the error will be visible
            if (!actionAll.isChecked()) {
                showAll(true);
                actionAll.setChecked(true);
            }

            // inform about the failure in the form title
            setFormToolTipText(getFormToolTipText() + "\n" + formatedFailure); //$NON-NLS-1$

            // indicate edit fiels as failure and set message in status line if the field
            // couldn't be found
            String message = ""; //$NON-NLS-1$
            if (!testCaseDetailArea.markEditFieldAsFailure(findUniqueEditFieldKey(failureDetailsObj),
                    formatedFailure, failureDetails)) {
                // field wasn't found, set message in the status bar to inform user about
                // missing failure indicator
                message = NLS.bind(Messages.TestCaseSection_StatusMessage_FieldNotFound, formatedFailure);
            }
            IStatusLineManager statusLineManager = site.getActionBars().getStatusLineManager();
            if (statusLineManager != null) {
                statusLineManager.setMessage(message);
            }

            // create context menu to store actual value
            EditField<?> editField = testCaseDetailArea.getEditField(
                    getUniqueEditFieldKey(failureDetailsObj.getObjectName(), failureDetailsObj.getAttributeName()));
            if (editField != null) {
                ArrayList<FailureDetails> list = new ArrayList<>(1);
                list.add(failureDetailsObj);

                TestCaseSection.this.addExpectedResultContextMenu(editField.getControl(), list, false);
            }

        });
    }

    public void postSetStatusBarMessage(final String message) {
        postAsyncRunnable(() -> {
            IStatusLineManager statusLineManager = site.getActionBars().getStatusLineManager();
            if (statusLineManager != null) {
                statusLineManager.setMessage(message);
            }
        });
    }

    /**
     * Returns the unique key to indicate the edit field
     */
    private String getUniqueEditFieldKey(String objectName, String attributeName) {
        if (StringUtils.isEmpty(attributeName) || "<null>".equals(attributeName)) { //$NON-NLS-1$
            // no attribute given expect that the failure was in an value object
            return objectName;
        } else {
            return (objectName + attributeName).toUpperCase();
        }
    }

    void postAddExpectedResultContextMenu(Control control, String[] failureDetails) {
        postAddExpectedResultContextMenu(control, new FailureDetails(failureDetails), false);
    }

    private void postAddExpectedResultContextMenu(Control control,
            FailureDetails failureDetails,
            boolean isSectionTitleMenu) {

        ArrayList<FailureDetails> list = new ArrayList<>(1);
        list.add(failureDetails);
        postAddExpectedResultContextMenu(control, list, isSectionTitleMenu);
    }

    private void postAddExpectedResultContextMenu(final Control control,
            final List<FailureDetails> failureDetails,
            final boolean isSectionTitleMenu) {

        postAsyncRunnable(() -> {
            if (isDisposed() || control == null || !isDataChangeable()) {
                return;
            }

            addExpectedResultContextMenu(control, failureDetails, isSectionTitleMenu);
        });
    }

    private void addExpectedResultContextMenu(final Control control,
            final List<FailureDetails> failureDetails,
            final boolean isSectionTitleMenu) {

        if (control == null || failureDetails.size() == 0 || control.isDisposed()) {
            return;
        }

        EditFieldMenu menuMgr = new EditFieldMenu("#PopupMenu", failureDetails); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(menuMgr);

        Menu menu = menuMgr.createContextMenu(control);
        control.setMenu(menu);
        if (isSectionTitleMenu) {
            // special handling for title context menu (see getFormTitleLabel())
            getFormTitleLabel().setMenu(menu);
            sectionTitleContextMenu = menu;
        }
    }

    @Override
    public void testFinished(String qualifiedTestName) {
        if (!canListenToTestRun(qualifiedTestName)) {
            return;
        }

        if (getTestCase().getIpsSrcFile() == null || !getTestCase().getIpsSrcFile().isDirty()) {
            postSetTestRunStatus(isTestRunError, isTestRunFailure, failureCount);
        }

        // create context menu to store actual value as expected value
        List<FailureDetails> allFailuresCopy = new ArrayList<>();
        allFailuresCopy.addAll(allFailureDetails);
        postAddExpectedResultContextMenu(form.getContent(), allFailuresCopy, true);

        // set focus to the first error
        if (allFailureDetails.size() > 0) {
            FailureDetails failureDetails = allFailureDetails.get(0);
            setFocusOnFailureField(qualifiedTestName, failureDetails.getFailureDetails());
        }
    }

    public void setFocusOnFailureField(final String qualifiedTestName, final String[] failureDetails) {
        if (!canListenToTestRun(qualifiedTestName)) {
            return;
        }
        postAsyncRunnable(() -> {
            if (isDisposed()) {
                return;
            }
            testCaseDetailArea.setFocusOnEditField(findUniqueEditFieldKey(new FailureDetails(failureDetails)));
        });
    }

    @Override
    public void testStarted(String qualifiedTestName) {
        testStarted(qualifiedTestName, true);
    }

    public void testStarted(String qualifiedTestName, boolean clearFixedValueState) {
        if (!canListenToTestRun(qualifiedTestName)) {
            return;
        }

        // resets the status, thus if a test runner for this test case is started in the background
        // - e.g. by using the modelexplorer - the status will be removed correctly
        postResetTestRunStatus();

        clearTestFailures(clearFixedValueState);

        // remove the contextmenu in the section title
        postAsyncRunnable(() -> {
            if (isDisposed()) {
                return;
            }
            if (sectionTitleContextMenu != null) {
                sectionTitleContextMenu.dispose();
            }
        });

        // redraw all fields and reset the color of all fields
        postShowAllStructureAndAllTypes();

        isTestRunError = false;
        isTestRunFailure = false;
        failureCount = 0;
    }

    @Override
    public void testRunEnded(String elapsedTime) {
        if (isTestRunError) {
            // set the status only if an error occurred, otherwise the status is set by the
            // testFinished event
            postSetTestRunStatus(isTestRunError, isTestRunFailure, failureCount);
        }
    }

    @Override
    public void testRunStarted(int testCount, String repositoryPackage, String testPackage) {
        // nothing to do
    }

    @Override
    public void testTableEntry(String qualifiedName, String fullPath) {
        // nothing to do
    }

    @Override
    public void testTableEntries(String[] qualifiedName, String[] fullPath) {
        // nothing to do
    }

    void postAsyncRunnable(Runnable r) {
        if (!isDisposed()) {
            getDisplay().asyncExec(r);
        }
    }

    /**
     * Returns <code>true</code> if the test run listener is relevant for this test case.<br>
     * Returns <code>false</code> if the given test case name doesn't match the current editing test
     * case.
     */
    private boolean canListenToTestRun(String testCaseQualifiedName) {
        return testCaseQualifiedName.equals(getTestCase().getQualifiedName());
    }

    void postSetTestRunStatus(final boolean isError, final boolean isFailure, final int failureCount) {
        postAsyncRunnable(() -> {
            if (isDisposed()) {
                return;
            }
            setTitleStatus(isError, isFailure, false, failureCount, getFormToolTipText());
        });
    }

    /**
     * Sets the status in the section title. One of:
     * <ul>
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

        form.setDelayedReflow(true);

        if (isError) {
            form.getContent().setBackground(fFailureColor);
            form.getContent().setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
            setFormToolTipText(Messages.TestCaseEditor_Title_Error);
        } else if (isFailure) {
            form.getContent().setBackground(fFailureColor);
            form.getContent().setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
            setFormToolTipText(NLS.bind(Messages.TestCaseEditor_Title_Failure, "" + failureCount) + titleMessage); //$NON-NLS-1$
        } else if (isOverridden) {
            form.getContent().setBackground(getDisplay().getSystemColor(SWT.COLOR_YELLOW));
            form.getContent().setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
            setFormToolTipText(titleMessage);
        } else {
            form.getContent().setBackground(fOkColor);
            form.getContent().setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
            setFormToolTipText(Messages.TestCaseEditor_Title_Success);
        }
    }

    /**
     * Adds a link to the test case type. The link is activated if the mouse is clicked and the ctrl
     * button is pressed.
     */
    private void hookeSectionTitleHyperlink() {
        getFormTitleLabel().addMouseListener(new MouseListener() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                // Nothing to do
            }

            @Override
            public void mouseDown(MouseEvent event) {
                if ((event.stateMask & SWT.CTRL) > 0) {
                    if (!IpsPlugin.getDefault().getIpsPreferences().canNavigateToModelOrSourceCode()) {
                        return;
                    }
                    ITestCaseType type = null;
                    try {
                        type = getTestCase().findTestCaseType(ipsProject);
                    } catch (CoreException e) {
                        IpsPlugin.log(e);
                        return;
                    }
                    if (type == null) {
                        return;
                    }
                    IpsUIPlugin.getDefault().openEditor(type.getIpsSrcFile());
                }
            }

            @Override
            public void mouseUp(MouseEvent e) {
                // Nothing to do
            }
        });
    }

    void postSetFailureBackgroundAndToolTip(final EditField<?> editField, final String expectedResult) {
        postAsyncRunnable(() -> {
            if (isDisposed()) {
                return;
            }
            editField.getControl().setBackground(fFailureColor);
            editField.getControl().setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
            editField.getControl().setToolTipText(expectedResult);
        });
    }

    public void postSetOverriddenValueBackgroundAndToolTip(final EditField<?> editField,
            final String message,
            final boolean setFocus) {

        postAsyncRunnable(() -> {
            if (isDisposed()) {
                return;
            }

            editField.getControl().setBackground(fOkColor);
            editField.getControl().setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
            editField.getControl().setToolTipText(message);
            if (setFocus) {
                editField.getControl().setFocus();
            }
        });
    }

    void postShowAllStructureAndAllTypes() {
        postAsyncRunnable(() -> {
            if (isDisposed()) {
                return;
            }
            // if the content type is input then set the content type to combined
            if (contentProvider.getContentType() == TestCaseContentProvider.INPUT) {
                contentProvider.setContentType(TestCaseContentProvider.COMBINED);
            }
            // show all objects
            showAll(true);
            actionAll.setChecked(true);
        });
    }

    void postResetTestRunStatus() {
        postAsyncRunnable(() -> {
            if (isDisposed()) {
                return;
            }
            resetTestRunStatus();
        });
    }

    /**
     * Resets the test run status. Change the color and tooltips to default.
     */
    public void resetTestRunStatus() {
        form.getContent().setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
        form.getContent().setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
        setFormToolTipText(""); //$NON-NLS-1$
    }

    private void clearTestFailures(boolean clearFixedValueState) {
        testCaseDetailArea.resetTestRun(clearFixedValueState);
        allFailureDetails.clear();
    }

    /**
     * Returns the corresponding test case editor.
     */
    IpsObjectEditor getTestCaseEditor() {
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

    /**
     * Starts the test runner to execute this test case
     */
    private void startTestRunner() {
        try {
            IIpsTestRunner testRunner = IpsPlugin.getDefault().getIpsTestRunner();
            testRunner.setIpsProject(ipsProject);
            String repositoryPackage = getTocFilePackage();
            if (repositoryPackage != null) {
                testRunner.startTestRunnerJob(repositoryPackage, getTestCase().getQualifiedName());
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /**
     * Register the listener to run the test and store the expected result.
     */
    private void registerRunTestAndStoreExpectedResultLister() {
        if (runAndStoreExpectedResultListener != null) {
            IpsPlugin.getDefault().getIpsTestRunner().removeIpsTestRunListener(runAndStoreExpectedResultListener);
            runAndStoreExpectedResultListener = null;
        }
        runAndStoreExpectedResultListener = new RunAndStoreExpectedResultListener(this);
        IpsPlugin.getDefault().getIpsTestRunner().removeIpsTestRunListener(this);
        IpsPlugin.getDefault().getIpsTestRunner().addIpsTestRunListener(runAndStoreExpectedResultListener);
    }

    /**
     * Register the test run listener.
     */
    private void registerTestRunListener() {
        IpsPlugin.getDefault().getIpsTestRunner().removeIpsTestRunListener(this);
        IpsPlugin.getDefault().getIpsTestRunner().addIpsTestRunListener(this);
        if (runAndStoreExpectedResultListener != null) {
            IpsPlugin.getDefault().getIpsTestRunner().removeIpsTestRunListener(runAndStoreExpectedResultListener);
            runAndStoreExpectedResultListener = null;
        }
    }

    /**
     * Removes the listener for the ips test runner
     */
    private void removeAllListener() {
        getTestCase().getIpsModel().removeChangeListener(changeListener);
        if (runAndStoreExpectedResultListener != null) {
            IpsPlugin.getDefault().getIpsTestRunner().removeIpsTestRunListener(runAndStoreExpectedResultListener);
        }
        IpsPlugin.getDefault().getIpsTestRunner().removeIpsTestRunListener(this);
    }

    /**
     * Forwarded listener function for test run and store expected result. This method is calling
     * when the test runner ends and if failures occurred during the test run.
     */
    public void testFailureOccuredToStoreExpResult(String qualifiedTestName, final List<String[]> failureDetailsList) {
        if (!canListenToTestRun(qualifiedTestName)) {
            return;
        }

        postAsyncRunnable(() -> {
            if (isDisposed()) {
                return;
            }
            // register as the runner (removes the run and store exp. result listener)
            registerTestRunListener();

            String titleMessage = Messages.TestCaseSection_SectionTitleToolTip_StoredExpectedResults;
            for (String[] failureDetails : failureDetailsList) {
                final String formatedMessage = failureDetailsToStoreInExpResultToString(failureDetails);
                titleMessage += titleMessage.length() > 0 ? "\n" + formatedMessage : formatedMessage; //$NON-NLS-1$
                form.getContent().setBackground(fFailureColor);
                form.getContent().setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

                storeActualAsExpectedValue(new FailureDetails(failureDetails), formatedMessage);
            }
            if (failureDetailsList.size() > 0) {
                // set the status in the title
                setTitleStatus(false, false, true, 0, titleMessage);
            } else {
                // test is ok, no expected value overridden
                setTitleStatus(false, false, false, 0, ""); //$NON-NLS-1$
            }
        });
    }

    /**
     * Forwarded listener function for test run and store expected result
     */
    public void testRunEndedToStoreExpResult(String qualifiedTestName) {
        if (!qualifiedTestName.equals(getTestCase().getQualifiedName())) {
            return;
        }

        postAsyncRunnable(() -> {
            if (isDisposed()) {
                return;
            }

            if (isTestRunError) {
                setTitleStatus(true, false, false, 0, ""); //$NON-NLS-1$
            }
        });
    }

    @Override
    public boolean canNavigateToFailure() {
        return true;
    }

    private void setFormToolTipText(String text) {
        getFormTitleLabel().setToolTipText(text);
    }

    private String getFormToolTipText() {
        String text = getFormTitleLabel().getToolTipText();
        return text != null ? text : ""; //$NON-NLS-1$
    }

    /**
     * Returns the control of the title headline, must be done in this way, because between Eclipse
     * 3.1 and Eclipse 3.2 the headline control changed. Thus search for the lable statring at the
     * form content control.
     */
    private Control getFormTitleLabel() {
        Control content = form.getContent();
        if (content instanceof Form) {
            Form contentForm = (Form)content;
            Control[] childs = contentForm.getChildren();
            for (Control child : childs) {
                if (child instanceof Label) {
                    return child;
                } else if (child instanceof Canvas) {
                    Control[] childChilds = ((Canvas)child).getChildren();
                    for (Control childChild : childChilds) {
                        if (childChild instanceof Label) {
                            return childChild;
                        }
                    }
                }
            }
        }
        return content;
    }

    /**
     * Loads the layout style from the dialog settings
     */
    void init() {
        IDialogSettings workbenchSettings = IpsPlugin.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection(DIALOG_SETTINGS_KEY);

        if (section != null) {
            // load settings
            hasNewDialogSettings = false;
            contentProvider.setContentType(section.getInt(CONTENT_TYPE_KEY));
            contentProvider.setWithoutAssociations(section.getBoolean(SHOW_ASSOCIATION_KEY));
            showAll = section.getBoolean(SHOW_ALL_KEY);
            canShowPolicyComponentType.setValue(!section.getBoolean(SHOW_POLICY_COMPONENT_TYPE));

            // init menu state
            actionAll.setChecked(showAll);
            actionAssociation.setChecked(!contentProvider.isWithoutAssociations());
            for (int i = 0; i < toggleContentTypeActions.length; i++) {
                if (i == contentProvider.getContentType()) {
                    toggleContentTypeActions[i].setChecked(true);
                } else {
                    toggleContentTypeActions[i].setChecked(false);
                }
            }

            // refresh content
            refreshTreeAndDetailArea();
            if (showAll) {
                getTreeViewer().expandAll();
            }
        } else {
            hasNewDialogSettings = true;
        }
    }

    /**
     * Stores the layout style in the dialog settings
     */
    void saveState() {
        IDialogSettings workbenchSettings = IpsPlugin.getDefault().getDialogSettings();
        IDialogSettings section;
        if (hasNewDialogSettings) {
            section = workbenchSettings.addNewSection(DIALOG_SETTINGS_KEY);
        } else {
            section = workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
        }
        section.put(CONTENT_TYPE_KEY, contentProvider.getContentType());
        section.put(SHOW_ASSOCIATION_KEY, contentProvider.isWithoutAssociations());
        section.put(SHOW_ALL_KEY, showAll);
        section.put(SHOW_POLICY_COMPONENT_TYPE, !(Boolean)canShowPolicyComponentType.getValue());
    }

    public void setReadOnly(boolean readOnly) {
        actionRunAndStoreExpectedResult.setEnabled(!readOnly);
    }

    public void addDetailAreaRedrawListener(ITestCaseDetailAreaRedrawListener listener) {
        testCaseDetailArea.addDetailAreaRedrawListener(listener);
    }

    public void removeDetailAreaRedrawListener(ITestCaseDetailAreaRedrawListener listener) {
        testCaseDetailArea.removeDetailAreaRedrawListener(listener);
    }

    boolean isLocalDragAndDrop() {
        return localDragAndDrop;
    }

    public void setLocalDragAndDrop(boolean localDragAndDrop) {
        this.localDragAndDrop = localDragAndDrop;
    }

    /**
     * Returns the test case.
     */
    ITestCase getTestCase() {
        return testCase;
    }

    /**
     * Returns the treeViewer.
     */
    TreeViewer getTreeViewer() {
        return treeViewer;
    }

    /**
     * Sets the treeViewer.
     */
    void setTreeViewer(TreeViewer treeViewer) {
        this.treeViewer = treeViewer;
    }

    /**
     * State class contains the enable state of all actions (for buttons and context menu)
     */
    private class TreeActionEnableState {
        boolean productCmptChangeEnable = false;
        boolean productCmptRemoveEnable = false;
        boolean removeEnable = false;
        boolean addEnable = false;
        boolean moveEnable = false;
        boolean openInNewEditorEnable = true;
        boolean renameEnable = false;
    }

    /**
     * Action which provides the content type filter.
     */
    private class ToggleContentTypeAction extends Action {

        private final int fActionContentType;

        public ToggleContentTypeAction(int actionContentType) {
            super("", AS_RADIO_BUTTON); //$NON-NLS-1$
            setId("ToggleContentTypeAction" + actionContentType); //$NON-NLS-1$
            fActionContentType = actionContentType;
            if (actionContentType == TestCaseContentProvider.INPUT) {
                buttonChecked();
                setText(Messages.TestCaseSection_FilterInput);
                setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("TestCaseInput.gif")); //$NON-NLS-1$
                setToolTipText(Messages.TestCaseSection_FilterInput_ToolTip);
            } else if (actionContentType == TestCaseContentProvider.EXPECTED_RESULT) {
                buttonChecked();
                setText(Messages.TestCaseSection_FilterExpected);
                setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("TestCaseExpResult.gif")); //$NON-NLS-1$
                setToolTipText(Messages.TestCaseSection_FilterExpected_ToolTip);
            } else if (actionContentType == TestCaseContentProvider.COMBINED) {
                buttonChecked();
                setText(Messages.TestCaseSection_FilterCombined);
                setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("TestCaseCombined.gif")); //$NON-NLS-1$
                setToolTipText(Messages.TestCaseSection_FilterCombined_ToolTip);
            }
        }

        @Override
        public void run() {
            if (isChecked()) {
                switchContentType(fActionContentType);
            }
        }

        private void buttonChecked() {
            if (contentProvider.getContentType() == fActionContentType) {
                setChecked(true);
            }
        }
    }

    /**
     * Runnable to find all subtypes of a given test policy cmpt
     */
    private class SubPolicyCmptTypesSrcFileFinder implements Runnable {

        private IIpsSrcFile[] policyCmptTypesSrcFiles = null;
        private ITestPolicyCmptTypeParameter testTypeParam;

        public SubPolicyCmptTypesSrcFileFinder(ITestPolicyCmptTypeParameter testTypeParam) {
            this.testTypeParam = testTypeParam;
        }

        @Override
        public void run() {
            try {
                policyCmptTypesSrcFiles = TestCaseSection.this.getPolicyCmptTypesSrcFiles(testTypeParam);
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }

        public IIpsSrcFile[] getPolicyCmptTypesSrcFiles() {
            return policyCmptTypesSrcFiles;
        }
    }

    /**
     * Label provider for validation rules. Displays as text the name followed by the policy cmpt
     * type which the rule belongs to.
     */
    private class ValidationRuleLabelProvider extends DefaultLabelProvider {

        @Override
        public String getText(Object element) {
            if (!(element instanceof IValidationRule)) {
                return super.getText(element);
            }
            IValidationRule validationRule = (IValidationRule)element;
            String nameWithPolicyCmptType = validationRule.getName();
            nameWithPolicyCmptType += " - " + ((IPolicyCmptType)validationRule.getParent()).getName(); //$NON-NLS-1$
            return nameWithPolicyCmptType;
        }
    }

    /**
     * Label provider for the test case type association select dialog.
     */
    private class TestCaseTypeAssociationLabelProvider implements ILabelProvider {

        @Override
        public Image getImage(Object element) {
            return getImageFromAssociationType((TestCaseTypeAssociation)element);
        }

        /**
         * Returns the image of the given association test case type parameter.
         */
        private Image getImageFromAssociationType(TestCaseTypeAssociation dummyAssociation) {
            try {
                ITestPolicyCmptTypeParameter typeParam = null;
                typeParam = dummyAssociation.getTestPolicyCmptTypeParam();
                IPolicyCmptTypeAssociation association = typeParam.findAssociation(typeParam.getIpsProject());
                if (association == null) {
                    return null;
                }
                return IpsUIPlugin.getImageHandling().getImage(association);
            } catch (CoreException e) {
                return null;
            }
        }

        @Override
        public String getText(Object element) {
            TestCaseTypeAssociation dummyAssociation = (TestCaseTypeAssociation)element;
            return dummyAssociation.getName();
        }

        @Override
        public void addListener(ILabelProviderListener listener) {
            // Nothing to do
        }

        @Override
        public void dispose() {
            // Nothing to do
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
            // Nothing to do
        }
    }

    /**
     * Class to represent one ips test runner failure
     */
    private class FailureDetails {

        private String[] failureDetails;

        public FailureDetails(String[] failureDetails) {
            this.failureDetails = failureDetails;
        }

        public String getActualValue() {
            if (failureDetails.length >= 7) {
                return failureDetails[6];
            } else {
                return failureDetails[4];
            }
        }

        public String getAttributeName() {
            return failureDetails[2];
        }

        public String getObjectName() {
            return failureDetails[1];
        }

        public String[] getFailureDetails() {
            return failureDetails;
        }

    }

    /**
     * Class to represent the context menu to store the actual value in the expected value field
     */
    private class EditFieldMenu extends MenuManager implements IMenuListener, ISelectionProvider {

        private List<FailureDetails> failureDetailsList;

        public EditFieldMenu(String text, List<FailureDetails> failureDetailsList) {
            super(text);
            this.failureDetailsList = failureDetailsList;
        }

        @Override
        public void addSelectionChangedListener(ISelectionChangedListener listener) {
            // Nothing to do
        }

        @Override
        public ISelection getSelection() {
            return null;
        }

        @Override
        public void removeSelectionChangedListener(ISelectionChangedListener listener) {
            // Nothing to do
        }

        @Override
        public void setSelection(ISelection selection) {
            // Nothing to do
        }

        public List<FailureDetails> getFailureDetailsList() {
            return failureDetailsList;
        }

        @Override
        public void menuAboutToShow(IMenuManager manager) {
            final EditFieldMenu contextMenuManager = (EditFieldMenu)manager;

            Action actionStoreActualValue = new Action("actionStoreActualValue", IAction.AS_PUSH_BUTTON) { //$NON-NLS-1$
                @Override
                public void run() {
                    List<FailureDetails> failureDetailsList = contextMenuManager.getFailureDetailsList();
                    if (failureDetailsList.size() > 1) {
                        boolean overwriteExpectedResult = MessageDialog.openQuestion(getShell(),
                                Messages.TestCaseSection_MessageDialog_TitleStoreExpectedResults,
                                Messages.TestCaseSection_MessageDialog_QuestionStoreExpectedResults);
                        if (!overwriteExpectedResult) {
                            return;
                        }
                    }
                    for (FailureDetails failureDetails : failureDetailsList) {
                        storeActualAsExpectedValue(failureDetails,
                                failureDetailsToStoreInExpResultToString(failureDetails.getFailureDetails()));
                    }
                }

            };
            if (failureDetailsList.size() > 1) {
                actionStoreActualValue.setText(Messages.TestCaseSection_Action_StoreExpectedResults);
                actionStoreActualValue.setToolTipText(Messages.TestCaseSection_Action_ToolTipStoreExpectedResults);
            } else {
                actionStoreActualValue.setText(Messages.TestCaseSection_Action_StoreExpectedResult);
                actionStoreActualValue.setToolTipText(Messages.TestCaseSection_Action_ToolTipStoreExpectedResult);
            }
            actionStoreActualValue.setImageDescriptor(
                    IpsUIPlugin.getImageHandling().createImageDescriptor("TestCaseStoreExpResult.gif")); //$NON-NLS-1$

            manager.add(actionStoreActualValue);
        }
    }

    /**
     * Content change class to listen for content changes.
     */
    private class TestCaseContentChangeListener extends ContentsChangeListenerForWidget {
        public TestCaseContentChangeListener(Widget widget) {
            super(widget);
        }

        @Override
        public void contentsChangedAndWidgetIsNotDisposed(ContentChangeEvent event) {
            contentsHasChanged(event);
        }
    }

    private abstract class TestCaseAction extends Action {

        protected TestCaseAction(String text, ImageDescriptor image) {
            super(text, image);
        }

        protected final TreeViewer getTreeViewer() {
            return treeViewer;
        }

        protected final ISelection getSelection() {
            return getTreeViewer().getSelection();
        }

        protected final TestCaseContentProvider getTestCaseContentProvider() {
            return (TestCaseContentProvider)getTreeViewer().getContentProvider();
        }

        protected final ITestCase getTestCase() {
            return testCase;
        }

    }

    private class AddAction extends TestCaseAction {

        private static final String IMAGE_FILENAME = "Add.gif"; //$NON-NLS-1$

        private AddAction() {
            super(Messages.TestCaseSection_ButtonAdd,
                    IpsUIPlugin.getImageHandling().createImageDescriptor(IMAGE_FILENAME));
        }

        @Override
        public void run() {
            try {
                Object selectedObject = getFirstSelectedObjectFromTree();
                if (selectedObject instanceof TestCaseTypeAssociation) {
                    // add a new child depending on the association which was clicked
                    TestCaseTypeAssociation associationType = (TestCaseTypeAssociation)selectedObject;
                    addAssociation(associationType);
                } else if (selectedObject instanceof ITestPolicyCmpt) {
                    // open a dialog to ask for the type of association which
                    // are defined in the test case type parameter if more than one type defined
                    ITestPolicyCmpt testPolicyCmpt = (ITestPolicyCmpt)selectedObject;
                    TestCaseTypeAssociation associationType = selectTestCaseTypeAssociationByDialog(testPolicyCmpt);
                    if (associationType != null) {
                        getTreeViewer().expandToLevel(testPolicyCmpt, 1);
                        // get the cached dummy object, otherwise the expand doesn't work!
                        IDummyTestCaseObject dummyObject = getTestCaseContentProvider().getDummyObject(
                                associationType.getTestParameter(), associationType.getParentTestPolicyCmpt());
                        addAssociation((TestCaseTypeAssociation)dummyObject);
                    }
                } else if (selectedObject instanceof TestCaseTypeRule) {
                    // add test rules for the test rule parameter
                    IValidationRule validationRule = selectValidationRuleByDialog();
                    if (validationRule != null) {
                        addTestRule(((TestCaseTypeRule)selectedObject).getTestRuleParameter(), validationRule);
                    }
                }
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }

        /**
         * Returns the first selected object in the tree.
         */
        private Object getFirstSelectedObjectFromTree() {
            if (getSelection() instanceof IStructuredSelection) {
                Object domainObject = ((IStructuredSelection)getSelection()).getFirstElement();
                if (domainObject instanceof ITestPolicyCmpt) {
                    return getTestPolicyCmpFromDomainObject(domainObject);
                } else {
                    return domainObject;
                }
            }
            return null;
        }

        /**
         * Displays a dialog to select the type definition of a test association. Returns the
         * selected test case type association object or <code>null</code> if the user select
         * nothing.
         */
        private TestCaseTypeAssociation selectTestCaseTypeAssociationByDialog(ITestPolicyCmpt parentTestPolicyCmpt)
                throws CoreException {

            ElementListSelectionDialog selectDialog = new ElementListSelectionDialog(getShell(),
                    new TestCaseTypeAssociationLabelProvider());
            selectDialog.setTitle(Messages.TestCaseSection_DialogSelectTestAssociation_Title);
            selectDialog.setMessage(Messages.TestCaseSection_DialogSelectTestAssociation_Description);

            ITestPolicyCmptTypeParameter param = parentTestPolicyCmpt
                    .findTestPolicyCmptTypeParameter(getTestCase().getIpsProject());
            TestCaseTypeAssociation[] dummyAssociations = new TestCaseTypeAssociation[param
                    .getTestPolicyCmptTypeParamChilds().length];
            ITestPolicyCmptTypeParameter[] childParams = param.getTestPolicyCmptTypeParamChilds();
            for (int i = 0; i < childParams.length; i++) {
                TestCaseTypeAssociation association = new TestCaseTypeAssociation(childParams[i], parentTestPolicyCmpt);
                dummyAssociations[i] = association;
            }
            if (dummyAssociations.length == 1) {
                // exactly one type found, return this type
                return dummyAssociations[0];
            } else {
                selectDialog.setElements(dummyAssociations);
                if (selectDialog.open() == Window.OK) {
                    if (selectDialog.getResult().length > 0) {
                        return (TestCaseTypeAssociation)selectDialog.getResult()[0];
                    }
                }
            }
            return null;
        }

        /**
         * Displays a dialog to select on of a validation rule inside test test policy cmpts in this
         * test case. Returns the selected validation rule object or <code>null</code> if the user
         * select nothing.
         */
        private IValidationRule selectValidationRuleByDialog() {
            ElementListSelectionDialog selectDialog = new ElementListSelectionDialog(getShell(),
                    new ValidationRuleLabelProvider());
            selectDialog.setTitle(Messages.TestCaseSection_SelectDialogValidationRule_Title);
            selectDialog.setMessage(Messages.TestCaseSection_SelectDialogValidationRule_Decription);

            try {
                IValidationRule[] rules = getTestCase().getTestRuleCandidates(getTestCase().getIpsProject());
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
         * Add a new link based an the given test case type association.
         */
        private void addAssociation(final TestCaseTypeAssociation associationType) throws CoreException {
            String[] selectedTargetsQualifiedNames = null;
            boolean chooseProductCmpts = false;
            final ITestPolicyCmptTypeParameter testPolicyCmptTypeParam = associationType.getTestPolicyCmptTypeParam();
            if (associationType.getParentTestPolicyCmpt() == null) {
                addRootTestPolicyCmptObject(associationType, testPolicyCmptTypeParam);
                return;
            } else if (associationType.isRequiresProductCmpt()) {
                // target requires a product component
                chooseProductCmpts = true;

                IPolicyCmptTypeAssociation association = associationType
                        .findAssociation(associationType.getParentTestPolicyCmpt().getIpsProject());
                if (association == null) {
                    // validation error
                    return;
                }
                IPolicyCmptType policyCmptType = (IPolicyCmptType)association.findTarget(getTestCase().getIpsProject());
                if (policyCmptType == null) {
                    // validation error
                    return;
                }
                IProductCmptType productCmptType = policyCmptType.findProductCmptType(policyCmptType.getIpsProject());
                if (productCmptType == null) {
                    // validation error
                    return;
                }

                selectedTargetsQualifiedNames = selectProductCmptsDialog(testPolicyCmptTypeParam,
                        associationType.getParentTestPolicyCmpt(), true);
            } else {
                // target doesn't requires a product cmpt
                chooseProductCmpts = false;
                selectedTargetsQualifiedNames = selectPolicyCmptTypeDialog(testPolicyCmptTypeParam, true);
            }
            if (selectedTargetsQualifiedNames == null) {
                // maybe cancel pressed in the dialog
                return;
            }

            final IPolicyCmptTypeAssociation association = associationType
                    .findAssociation(associationType.getParentTestPolicyCmpt().getIpsProject());
            if (association == null) {
                // association not found, no add allowed
                return;
            }

            // perform the following operation by using queued changed events to
            // reduce the processing time
            final String[] finalSelectedTargetsQualifiedNames = selectedTargetsQualifiedNames;
            final boolean finalChooseProductCmpts = chooseProductCmpts;
            final IWorkspaceRunnable runnable = monitor -> {
                // add a new child based on the selected association

                if (association.isAssoziation()) {
                    // association will be added
                    String targetName = ""; //$NON-NLS-1$
                    ITestPolicyCmpt selectedTarget = selectAssoziationByTreeDialog(association.getTarget());
                    if (selectedTarget == null) {
                        // cancel in dialog
                        return;
                    }

                    TestCaseHierarchyPath path = new TestCaseHierarchyPath(selectedTarget);
                    targetName = path.getHierarchyPath();

                    ITestPolicyCmptLink newAssociation = null;
                    // add a new child based on the selected association and selected target
                    for (String finalSelectedTargetsQualifiedName1 : finalSelectedTargetsQualifiedNames) {
                        if (finalChooseProductCmpts) {
                            newAssociation = addNewLink(associationType, finalSelectedTargetsQualifiedName1, null,
                                    targetName);
                        } else {
                            newAssociation = addNewLink(associationType, null, finalSelectedTargetsQualifiedName1,
                                    targetName);
                        }
                    }
                    refreshTreeAndDetailArea();
                    expandTreeAfterAdd(associationType, null);
                    selectInTreeByObject(newAssociation, true);
                } else {
                    ITestPolicyCmptLink newLink = null;
                    // composition relation will be added
                    for (String finalSelectedTargetsQualifiedName2 : finalSelectedTargetsQualifiedNames) {
                        if (finalChooseProductCmpts) {
                            newLink = addNewLink(associationType, finalSelectedTargetsQualifiedName2, null, ""); //$NON-NLS-1$
                        } else {
                            newLink = addNewLink(associationType, null, finalSelectedTargetsQualifiedName2, ""); //$NON-NLS-1$
                        }
                    }
                    if (newLink == null) {
                        throw new CoreException(new IpsStatus(Messages.TestCaseSection_Error_CreatingAssociation));
                    }
                    ITestPolicyCmpt newTestPolicyCmpt = newLink.findTarget();
                    if (newTestPolicyCmpt == null) {
                        throw new CoreException(new IpsStatus(Messages.TestCaseSection_Error_CreatingAssociation));
                    }
                    refreshTreeAndDetailArea();
                    expandTreeAfterAdd(associationType, newTestPolicyCmpt);
                    selectionInTreeChanged((IStructuredSelection)getSelection());
                    selectInTreeByObject(newTestPolicyCmpt, true);
                }
            };

            BusyIndicator.showWhile(getDisplay(), () -> {
                try {
                    runnable.run(null);
                } catch (CoreException e) {
                    throw new CoreRuntimeException(e);
                }
            });
        }

        /**
         * Add a test rule the test case
         */
        private void addTestRule(ITestRuleParameter testRuleParameter, IValidationRule validationRule) {
            ITestRule testRule = getTestCase().newTestRule();
            testRule.setValidationRule(validationRule.getName());
            testRule.setTestRuleParameter(testRuleParameter.getName());

            // sort the test objects to ensure that the test rule is in the correct order
            try {
                getTestCase().sortTestObjects();
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }

            refreshTreeAndDetailArea();
            selectionInTreeChanged((IStructuredSelection)getSelection());
            getTreeViewer().expandToLevel(contentProvider.getDummyObject(testRuleParameter, null),
                    AbstractTreeViewer.ALL_LEVELS);
            selectInTreeByObject(testRule);
        }

    }

    private class RemoveAction extends TestCaseAction {

        private static final String IMAGE_FILENAME = "elcl16/trash.gif"; //$NON-NLS-1$

        private RemoveAction() {
            super(Messages.TestCaseSection_ButtonRemove,
                    IpsUIPlugin.getImageHandling().createImageDescriptor(IMAGE_FILENAME));
        }

        @Override
        public void run() {
            IWorkspaceRunnable runnable = monitor -> {
                if (getSelection() instanceof IStructuredSelection) {
                    Object nextItemToSelect = null;
                    for (Iterator<?> iterator = ((IStructuredSelection)getSelection()).iterator(); iterator
                            .hasNext();) {
                        Object currElement = iterator.next();
                        TreeItem nextTreeItem = getNextSelectionInTreeAfterDelete(currElement);
                        if (nextTreeItem != null) {
                            nextItemToSelect = nextTreeItem.getData();
                        }
                        if (currElement instanceof ITestObject) {
                            ((ITestObject)currElement).delete();
                        } else if (currElement instanceof ITestPolicyCmptLink) {
                            ((ITestPolicyCmptLink)currElement).delete();
                        } else if (currElement instanceof ITestPolicyCmpt
                                && ((ITestPolicyCmpt)currElement).isRoot()) {
                            /*
                             * is the root object will be deleted then the cache of all dummy GUI
                             * objects must be cleared, otherwise if the object are added again then
                             * all old / invalid objects are visible again
                             */
                            getTestCaseContentProvider()
                                    .clearChildDummyObjectsInCache((ITestPolicyCmpt)currElement);
                        } else {
                            throw new RuntimeException("Remove object with type " //$NON-NLS-1$
                                    + currElement.getClass().getName() + " is not supported!"); //$NON-NLS-1$
                        }
                    }
                    refreshTreeAndDetailArea();
                    getTreeViewer().getControl().setFocus();
                    if (nextItemToSelect != null) {
                        getTreeViewer().setSelection(new StructuredSelection(nextItemToSelect));
                    }
                }
            };
            IIpsModel.get().runAndQueueChangeEvents(runnable, null);
        }

        /**
         * Returns the next possible tree item after deletion of the given object.
         */
        private TreeItem getNextSelectionInTreeAfterDelete(Object objectDeletedInTree) {
            if (objectDeletedInTree == null) {
                return null;
            }
            Widget item = getTreeViewer().testFindItem(objectDeletedInTree);
            if (item instanceof TreeItem) {
                TreeItem currTreeItem = (TreeItem)item;
                TreeItem parent = currTreeItem.getParentItem();
                TreeItem[] itemsSameLevel;
                if (parent != null) {
                    itemsSameLevel = parent.getItems();
                } else {
                    itemsSameLevel = currTreeItem.getParent().getItems();
                }
                TreeItem prevItem = null;
                for (TreeItem element : itemsSameLevel) {
                    if (element.equals(currTreeItem)) {
                        break;
                    }
                    prevItem = element;
                }
                if (prevItem != null) {
                    return prevItem;
                } else if (parent != null) {
                    return parent;
                } else if (itemsSameLevel.length > 2) {
                    return itemsSameLevel[2];
                }
            }
            return null;
        }

    }

    /**
     * Action to change the product cmpt
     */
    private class ProductCmptChangeAction extends IpsAction {

        public ProductCmptChangeAction() {
            super(getTreeViewer());
            setText(Messages.TestCaseSection_ChangeProductCmpt);
        }

        @Override
        protected boolean computeEnabledProperty(IStructuredSelection selection) {
            TreeActionEnableState actionEnableState = evaluateTreeActionEnableState(selection.getFirstElement());
            return actionEnableState.productCmptChangeEnable;
        }

        @Override
        public void run(IStructuredSelection selection) {
            try {
                changeProductCmpt();
            } catch (Exception e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
    }

    /**
     * Action to reset the product cmpt, or set the product cmpt to null respectively
     */
    private class ProductCmptRemoveAction extends IpsAction {

        public ProductCmptRemoveAction() {
            super(getTreeViewer());
            setText(Messages.TestCaseSection_RemoveProductComponentAction_Text);
        }

        @Override
        protected boolean computeEnabledProperty(IStructuredSelection selection) {
            TreeActionEnableState actionEnableState = evaluateTreeActionEnableState(selection.getFirstElement());
            return actionEnableState.productCmptRemoveEnable;
        }

        @Override
        public void run(IStructuredSelection selection) {
            try {
                resetProductCmptToEmpty();
            } catch (Exception e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
    }

    /**
     * Action to move links up or down
     */
    private class MoveAction extends IpsAction {

        private boolean up;

        public MoveAction(boolean up) {
            super(getTreeViewer());
            this.up = up;
            setText(up ? Messages.TestCaseSection_Menu_Up : Messages.TestCaseSection_Menu_Down);
        }

        @Override
        protected boolean computeEnabledProperty(IStructuredSelection selection) {
            TreeActionEnableState actionEnableState = evaluateTreeActionEnableState(selection.getFirstElement());
            return actionEnableState.moveEnable;
        }

        @Override
        public void run(IStructuredSelection selection) {
            try {
                Object firstElement = selection.getFirstElement();
                if (firstElement instanceof ITestPolicyCmpt) {
                    ITestPolicyCmpt testPolicyCmpt = (ITestPolicyCmpt)firstElement;
                    ITestPolicyCmpt parentPolicyCmpt = testPolicyCmpt.getParentTestPolicyCmpt();
                    int index = parentPolicyCmpt.getIndexOfChildTestPolicyCmpt(testPolicyCmpt);
                    int newIndex = parentPolicyCmpt.moveTestPolicyCmptLink(new int[] { index }, up)[0];
                    if (newIndex != index) {
                        refreshTreeAndDetailArea();
                        getTreeViewer().setSelection(selection);
                    }
                } else {
                    throw new RuntimeException("Move action not supported for: " + firstElement.getClass().getName()); //$NON-NLS-1$
                }
            } catch (Exception e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
    }

    private class OpenInNewEditorAction extends IpsAction {

        public OpenInNewEditorAction() {
            super(getTreeViewer());
            setText(Messages.TestCaseSection_Menu_OpenInNewEditor);
        }

        @Override
        protected boolean computeEnabledProperty(IStructuredSelection selection) {
            TreeActionEnableState actionEnableState = evaluateTreeActionEnableState(selection.getFirstElement());
            return actionEnableState.openInNewEditorEnable;
        }

        @Override
        public void run(IStructuredSelection selection) {
            Object firstElement = selection.getFirstElement();
            boolean canNavigateToModelOrSourceCode = IpsPlugin.getDefault().getIpsPreferences()
                    .canNavigateToModelOrSourceCode();
            if (firstElement instanceof ITestPolicyCmpt) {
                try {
                    ITestPolicyCmpt tpct = (ITestPolicyCmpt)firstElement;
                    if (tpct.hasProductCmpt()) {
                        // open the product cmpt
                        IProductCmpt cmpt = tpct.findProductCmpt(tpct.getIpsProject());
                        IpsUIPlugin.getDefault().openEditor(cmpt);
                    } else if (canNavigateToModelOrSourceCode) {
                        // open the policy cmpt type because the param is not product relevanz
                        ITestPolicyCmptTypeParameter parameter = tpct.findTestPolicyCmptTypeParameter(ipsProject);
                        if (parameter != null) {
                            IPolicyCmptType type = parameter.findPolicyCmptType(parameter.getIpsProject());
                            IpsUIPlugin.getDefault().openEditor(type);
                        }
                    }
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            } else if (canNavigateToModelOrSourceCode && firstElement instanceof TestCaseTypeAssociation) {
                // open the target policy cmpt type of the association which is related to the
                // corresponding test parameter
                TestCaseTypeAssociation ta = (TestCaseTypeAssociation)firstElement;
                try {
                    IPolicyCmptTypeAssociation association = ta.findAssociation(ipsProject);
                    IpsUIPlugin.getDefault().openEditor(association.findTarget(ipsProject));
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        }
    }

    /**
     * Lets the user enter a new name for a policy component
     */
    private class RenamePolicyCmptAction extends IpsAction {

        public RenamePolicyCmptAction() {
            super(getTreeViewer());
            setText(Messages.TestCaseSection_RenameActionLabel);
        }

        @Override
        protected boolean computeEnabledProperty(IStructuredSelection selection) {
            TreeActionEnableState actionEnableState = evaluateTreeActionEnableState(selection.getFirstElement());
            return actionEnableState.renameEnable;
        }

        @Override
        public void run(IStructuredSelection selection) {
            Object firstElement = selection.getFirstElement();
            if (firstElement instanceof ITestPolicyCmpt) {
                renamePolicyCmpt((ITestPolicyCmpt)firstElement);
            }
        }

    }

}
// CSON: FileLengthCheck
