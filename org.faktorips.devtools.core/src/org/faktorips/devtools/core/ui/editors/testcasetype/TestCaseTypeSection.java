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

package org.faktorips.devtools.core.ui.editors.testcasetype;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterRole;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.MessageCueLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;
import org.faktorips.devtools.core.ui.controller.UIController;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.editors.testcase.TreeViewerExpandStateStorage;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.MessageList;

/**
 * Section to display and edit the test case type.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTypeSection extends IpsSection {

    // The editing test case type
    private ITestCaseType testCaseType;
    
    // The label provider for the test case type
    private TestCaseTypeLabelProvider labelProvider;
    
    // UI toolkit for creating the controls
    private UIToolkit toolkit;

    // The treeview which displays all test policy cmpt type params which are available in this test
    private TreeViewer treeViewer;
    
    // Title of the test case type tree structure section
    private String sectionTreeStructureTitle;
    
    // Title of the test case type details section
    private String sectionDetailsTitle;
    
    // Buttons
    //   belongs to tree structure
    private Button addRootTestValueParameterButton;
    private Button addRootTestPolicyCmptTypeParamButton;
    private Button changeTargetButton;
    private Button removeButton;
    private Button addButton;
    private Button changeRelationButton;
    
    // The form which contains this section
    private ScrolledForm form;
    
    // The detail area will be used to render the dynamically created detail controls
    private Composite parentOfdetailsArea;
    private Composite detailsArea;

    // The previous selected parameter
    private ITestParameter prevSelectedTestParam;
    
    private boolean showAll = false;
    
    // Indicates that the tree is refreshing
    private boolean isTreeRefreshing = false;
    
    // Containers for all sections and their objects
    private HashMap sections = new HashMap();
    private HashMap sectionObjects = new HashMap();
    private HashMap sectionFirstEditField = new HashMap();
    private HashMap attributeIdx = new HashMap();
    
    // Contains the currently selected object in the detail area
    private IIpsObjectPart currSelectedDetailObject;
    
    // Container for the attribute ui controllers
    //  used to update the attribute edit fields if the test case type changed,
    //  thus if the role of a test policy cmpt type param changed the attribute will
    //  show the correct validation result
    private List attributeControllers = new ArrayList();

    private HashMap sectionButtons = new HashMap();
    
    private class SectionButtons{
        private Section section;
        private Button addAtributeButton;
        private Button removeAttributeButton;
        private Button changeAttributeButton;
        private Button moveAttributeUp;
        private Button moveAttributeDown;
        
        public void createButtons(Composite structureComposite, Section section) {
            this.section = section;
            
            Composite buttons = toolkit.getFormToolkit().createComposite(structureComposite);
            buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
            GridLayout buttonLayout = new GridLayout(1, true);
            buttons.setLayout(buttonLayout);
            
            addAtributeButton = toolkit.createButton(buttons, Messages.TestCaseTypeSection_Button_AddAttribute);
            
            createButtonSeparator(buttons);
            removeAttributeButton = toolkit.createButton(buttons, Messages.TestCaseTypeSection_Button_RemoveAttribute);
            changeAttributeButton = toolkit.createButton(buttons, Messages.TestCaseTypeSection_Button_ChangeAttribute);
            moveAttributeUp = toolkit.createButton(buttons, Messages.TestCaseTypeSection_Button_MoveAttributeUp);
            moveAttributeDown = toolkit.createButton(buttons, Messages.TestCaseTypeSection_Button_MoveAttributeDown);
            
            addAtributeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            removeAttributeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            changeAttributeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            moveAttributeUp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            moveAttributeDown.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            
            addAtributeButton.setEnabled(false);
            removeAttributeButton.setEnabled(false);
            changeAttributeButton.setEnabled(false);
            moveAttributeUp.setEnabled(false);
            moveAttributeDown.setEnabled(false);
            
            hookButtonListenersDetail();
        }
        
        public void updateDetailButtonStatus(IIpsObjectPart mainSectionObject, IIpsObjectPart currSelectedDetailObject) {
            if (section != sections.get(getKeyFor(mainSectionObject))){
                addAtributeButton.setEnabled(false);
                removeAttributeButton.setEnabled(false);
                changeAttributeButton.setEnabled(false);
                moveAttributeUp.setEnabled(false);
                moveAttributeDown.setEnabled(false);
                return;
            }
            
            if (currSelectedDetailObject instanceof ITestPolicyCmptTypeParameter){
                addAtributeButton.setEnabled(true);
                removeAttributeButton.setEnabled(false);
                changeAttributeButton.setEnabled(false);
                moveAttributeUp.setEnabled(false);
                moveAttributeDown.setEnabled(false);
            } else if (currSelectedDetailObject instanceof ITestAttribute){
                addAtributeButton.setEnabled(true);
                removeAttributeButton.setEnabled(true);
                changeAttributeButton.setEnabled(true);
                moveAttributeUp.setEnabled(true);
                moveAttributeDown.setEnabled(true);
            }
        }
        
        
        /**
         * Adds the button listener for the details area.
         */
        private void hookButtonListenersDetail() {
            addAtributeButton.addSelectionListener(new SelectionListener() {
                public void widgetSelected(SelectionEvent e) {
                    try {
                        addAttributeClicked(currSelectedDetailObject);
                    } catch (Exception ex) {
                        IpsPlugin.logAndShowErrorDialog(ex);
                    }
                }
                public void widgetDefaultSelected(SelectionEvent e) {
                }
            });
            removeAttributeButton.addSelectionListener(new SelectionListener() {
                public void widgetSelected(SelectionEvent e) {
                    try {
                        removeAttributeClicked(currSelectedDetailObject);
                    } catch (Exception ex) {
                        IpsPlugin.logAndShowErrorDialog(ex);
                    }
                }
                public void widgetDefaultSelected(SelectionEvent e) {
                }
            });
            changeAttributeButton.addSelectionListener(new SelectionListener() {
                public void widgetSelected(SelectionEvent e) {
                    try {
                        changeAttributeClicked(currSelectedDetailObject);
                    } catch (Exception ex) {
                        IpsPlugin.logAndShowErrorDialog(ex);
                    }
                }
                public void widgetDefaultSelected(SelectionEvent e) {
                }
            });
            moveAttributeUp.addSelectionListener(new SelectionListener() {
                public void widgetSelected(SelectionEvent e) {
                    try {
                        moveUpAttributeClicked(currSelectedDetailObject);
                    } catch (Exception ex) {
                        IpsPlugin.logAndShowErrorDialog(ex);
                    }
                }
                public void widgetDefaultSelected(SelectionEvent e) {
                }
            });
            moveAttributeDown.addSelectionListener(new SelectionListener() {
                public void widgetSelected(SelectionEvent e) {
                    try {
                        moveDownAttributeClicked(currSelectedDetailObject);
                    } catch (Exception ex) {
                        IpsPlugin.logAndShowErrorDialog(ex);
                    }
                }
                public void widgetDefaultSelected(SelectionEvent e) {
                }
            }); 
        }        
    }
    
    /*
     * Mouse listener class to select the section if the mouse button is clicked
     */
    private class SectionSelectMouseListener implements MouseListener {
        private Section section;
        private IIpsObjectPart object;
        
        public SectionSelectMouseListener(Section section, IIpsObjectPart object){
            this.section = section;
            this.object = object;
        }
        public void mouseDown(MouseEvent e) {
            currSelectedDetailObject = object;
            if (section == null){
                Section section = (Section) sections.get(getKeyFor(object));
                if (section != null){
                    selectSection(section);
                }                
            }else{
                selectSection(section);
            }
        }
        public void mouseDoubleClick(MouseEvent e) {
            // nothing to do
        }
        public void mouseUp(MouseEvent e) {
            // nothing to do
        }
    }
    
    /*
     * Expansion section listener class to select the section if expand or collapsed
     */
    private class SectionExpandListern implements IExpansionListener{
        private Section section;
        private IIpsObjectPart object;
        
        public SectionExpandListern(Section section, IIpsObjectPart object){
            this.section = section;
            this.object = object;
        }
        public void expansionStateChanged(ExpansionEvent e) {
            currSelectedDetailObject = object;
            selectSection(section);
            final ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter;
            if (object instanceof ITestAttribute)
                testPolicyCmptTypeParameter = (ITestPolicyCmptTypeParameter)((ITestAttribute)object).getParent();
            else if (object instanceof ITestPolicyCmptTypeParameter)
                testPolicyCmptTypeParameter = (ITestPolicyCmptTypeParameter)object;
            else
                return;

            try {
                if (!object.isValid() && !e.getState())
                    postAsyncRunnable(new Runnable() {
                        public void run() {
                            if (isDisposed())
                                return;
                            redrawDetailArea(testPolicyCmptTypeParameter, object);
                        }
                    });

            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }
        }

        public void expansionStateChanging(ExpansionEvent e) {
            // nothing to do
        }
    }
    
    private void postAsyncRunnable(Runnable r) {
        if (!isDisposed())
            getDisplay().asyncExec(r);
    }       

    public TestCaseTypeSection(Composite parent, UIToolkit toolkit, final ITestCaseType testCaseType, String title,
            String detailTitle, ScrolledForm form) {
        super(parent, Section.NO_TITLE, GridData.FILL_BOTH, toolkit);

        this.testCaseType = testCaseType;
        this.sectionTreeStructureTitle = title;
        this.sectionDetailsTitle = detailTitle;
        this.form = form;
        
        initControls();
        setText(title);
        
        // add listener on model,
        //   if the model changed reset the test run status
        testCaseType.getIpsModel().addChangeListener(new ContentsChangeListener() {
            public void contentsChanged(ContentChangeEvent event) {
                if (event.getIpsSrcFile().equals(testCaseType.getIpsSrcFile())) {
                    try{
                        setFormRedraw(false);
                        refreshTree();
                        refreshSectionTitles();
                        // refresh attribute edit fields
                        for (Iterator iter = attributeControllers.iterator(); iter.hasNext();) {
                            UIController controller = (UIController) iter.next();
                            controller.updateUI();
                        }
                        updateTreeButtonStatus(getRootSectionObject(currSelectedDetailObject));
                    }finally{
                        setFormRedraw(true);
                    }
                }
            }
        });        
    }

    /**
     * {@inheritDoc}
     */
    protected void performRefresh() {
    }

    /**
     * {@inheritDoc}
     */
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        this.toolkit = toolkit;
        
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
        labelProvider = new TestCaseTypeLabelProvider();
        MessageCueLabelProvider msgCueLabelProvider = new MessageCueLabelProvider(labelProvider);
        treeViewer.setLabelProvider(msgCueLabelProvider);
        TestCaseTypeContentProvider contentProvider = new TestCaseTypeContentProvider();
        treeViewer.setContentProvider(contentProvider);
        treeViewer.setInput(testCaseType);        
        treeViewer.expandAll();
        treeViewer.collapseAll();
        hookTreeListeners();

        // Buttons belongs to the tree structure
        Composite buttons = toolkit.getFormToolkit().createComposite(structureComposite);
        buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        GridLayout buttonLayout = new GridLayout(1, true);
        buttons.setLayout(buttonLayout);
        
        addRootTestValueParameterButton = toolkit.createButton(buttons, Messages.TestCaseTypeSection_Button_AddRootTestValue);
        addRootTestPolicyCmptTypeParamButton = toolkit.createButton(buttons, Messages.TestCaseTypeSection_Button_AddRootTestPolicyCmpt);
        changeTargetButton = toolkit.createButton(buttons, Messages.TestCaseTypeSection_Button_ChangeTarget);
        removeButton = toolkit.createButton(buttons, Messages.TestCaseTypeSection_Button_Remove);
        createButtonSeparator(buttons);
        addButton = toolkit.createButton(buttons, Messages.TestCaseTypeSection_Button_AddRelation);
        changeRelationButton = toolkit.createButton(buttons, Messages.TestCaseTypeSection_Button_ChangeRelation);
        addRootTestValueParameterButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addRootTestPolicyCmptTypeParamButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        changeTargetButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        changeRelationButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        updateTreeButtonStatus(null);
        hookButtonListeners();

        // Details section
        Section detailsSection = toolkit.getFormToolkit().createSection(client, Section.TITLE_BAR);     
        detailsSection.setLayoutData(new GridData(GridData.FILL_BOTH));     
        detailsSection.setText(sectionDetailsTitle);

        parentOfdetailsArea = toolkit.createComposite(detailsSection);
        parentOfdetailsArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout detailLayout = new GridLayout(1, false);
        detailLayout.horizontalSpacing = 0;
        detailLayout.marginWidth = 0;
        detailLayout.marginHeight = 0;
        parentOfdetailsArea.setLayout(detailLayout);
        detailsSection.setClient(parentOfdetailsArea);
        
        if (showAll)
            createDetailsArea(null);
        
        configureToolBar();
        
        redrawForm();
    }
    
    /*
     * Configuration of toolbar
     */
    private void configureToolBar(){
        // Toolbar item show all
        Action actionAll = new Action("structureAll", Action.AS_CHECK_BOX) { //$NON-NLS-1$
            public void run() {
                showAllClicked();
            }
        };
        actionAll.setChecked(showAll);
        actionAll.setToolTipText(Messages.TestCaseTypeSection_Action_ShowAll_ToolTip);
        actionAll.setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("TestCase_flatView.gif")); //$NON-NLS-1$
        form.getToolBarManager().add(actionAll);
        form.updateToolBar();
    }
    
    /*
     * Show all test parameter elements
     */
    private void showAllClicked() {
        showAll = ! showAll;
        if (showAll)
            treeViewer.expandAll();
        createDetailsArea(getRootSectionObject(currSelectedDetailObject));
    }

    /*
     * Creates the test case type details area depending on the given object.
     */
    private void createDetailsArea(ITestParameter testParam) {
        if (isTreeRefreshing)
            return;
        
        clearDetailArea();
        
        if (testParam == null && ! showAll){
            updateTreeButtonStatus(null);
            return;
        }
        
        ITestParameter firstTestParam = testParam;
        Section firstSection = null;
        if (showAll){
            ITestParameter[] testParms = null;
            try {
                testParms = testCaseType.getTestParameters();
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
            if (testParms != null && testParms.length > 0){
                createDetailsForAllTestParams(testParms);
                firstTestParam = testParms[0];
                firstSection = (Section) sections.get(getKeyFor(firstTestParam));
            } else {
                redrawForm();
                return;
            }
        } else {
            firstSection = createDetailsForTestParam(testParam);
        }
        
        redrawForm();
        
        prevSelectedTestParam = firstTestParam;
        
        currSelectedDetailObject = firstTestParam;
        selectSection(firstSection);
    }
    
    /*
     * Creates the details all given test parameter
     */
    private void createDetailsForAllTestParams(ITestParameter[] testParams){
        for (int i = 0; i < testParams.length; i++) {
            createDetailsForTestParam(testParams[i]);
            if (testParams[i] instanceof ITestPolicyCmptTypeParameter){
                ITestPolicyCmptTypeParameter testPolicyCmptTypeParam = (ITestPolicyCmptTypeParameter) testParams[i];
                createDetailsForAllTestParams(testPolicyCmptTypeParam.getTestPolicyCmptTypeParamChilds());
            }
        } 
    }
    
    /*
     * Creates the details for the given test parameter
     */
    private Section createDetailsForTestParam(ITestParameter testParam) {
        Composite structureComposite = toolkit.getFormToolkit().createComposite(detailsArea);
        structureComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout structureLayout = new GridLayout(2, false);
        structureLayout.horizontalSpacing = 0;
        structureLayout.marginWidth = 0;
        structureLayout.marginHeight = 3;
        structureComposite.setLayout(structureLayout);
        
        Composite details = createBorderComposite(structureComposite);
        Section section = toolkit.getFormToolkit().createSection(details, 0);
        section.setText(labelProvider.getText(testParam));
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        // create separator line
        toolkit.getFormToolkit().createCompositeSeparator(section);
        storeSection(section, testParam);
        
        IpsPartUIController uiController = createUIController(testParam);

 
        // create common edit fields
        Composite editFieldsComposite = toolkit.createLabelEditColumnComposite(section);
        section.setClient(editFieldsComposite);

        createTestParamDetails(editFieldsComposite, testParam, uiController);
        
        if (testParam instanceof ITestPolicyCmptTypeParameter){
            ITestPolicyCmptTypeParameter testPolicyCmptTypeParam = (ITestPolicyCmptTypeParameter) testParam;
            
            createTestPolicyCmptTypeParamDetails(editFieldsComposite, testPolicyCmptTypeParam, uiController);

            // Spacer between test policy cmpt type param and test attribute
            toolkit.createVerticalSpacer(details, 10).setBackground(details.getBackground());
            
            // Create a detail sections for each attribute
            ITestAttribute[] testAttributes = testPolicyCmptTypeParam.getTestAttributes();
            for (int i = 0; i < testAttributes.length; i++) {
                createTestAttributeDetailSection(details, testAttributes[i]);
                attributeIdx.put(getKeyFor(testAttributes[i]), new Integer(i));
            }
        } else if (testParam instanceof ITestValueParameter){
            createTestValueParamDetails(editFieldsComposite, (ITestValueParameter)testParam, uiController);
        }
        
        if (testParam instanceof ITestPolicyCmptTypeParameter){
            SectionButtons currSectionButtons = new SectionButtons();
            currSectionButtons.createButtons(structureComposite, section);
            sectionButtons.put(getKeyFor(testParam), currSectionButtons);
        }
        
        uiController.updateUI();
        return section;
    }

    /**
     * Add attribute to test policy cmpt type param
     */
    private void addAttributeClicked(IIpsObjectPart object) {
        ITestPolicyCmptTypeParameter testPolicyCmptTypeParam;
        if (object instanceof ITestAttribute)
            testPolicyCmptTypeParam = (ITestPolicyCmptTypeParameter)((ITestAttribute)object).getParent();
        else if (object instanceof ITestPolicyCmptTypeParameter)
            testPolicyCmptTypeParam = (ITestPolicyCmptTypeParameter)object;
        else
            throw new RuntimeException(NLS.bind(Messages.TestCaseTypeSection_Error_UnexpectedObjectClass, object.getClass().getName()));
        
        // open a dialog to select an attribute of the policy cmpt 
        // wich is related by the given test policy cmpt type param
        String[] attributeNames;
        try {
            attributeNames = selectAttributeByDialog(testPolicyCmptTypeParam,
                    Messages.TestCaseTypeSection_Dialog_SelectAttributeAdd_Message, true);
        } catch (CoreException e1) {
            throw new RuntimeException(e1);
        }
        if (attributeNames == null)
            return;
        
        ITestAttribute testAttribute = null;
        for (int i = 0; i < attributeNames.length; i++) {
            String attributeName = attributeNames[i];
            try {
                if (testPolicyCmptTypeParam.isInputParameter()){
                    testAttribute = testPolicyCmptTypeParam.newInputTestAttribute();
                } else if (testPolicyCmptTypeParam.isExpextedResultParameter()){
                    testAttribute = testPolicyCmptTypeParam.newExpectedResultTestAttribute();
                } else {
                    // if the role of the parent is combined 
                    //   then create a new input attribute
                    testAttribute = testPolicyCmptTypeParam.newInputTestAttribute();
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
            
            testAttribute.setAttribute(attributeName);
            testAttribute.setName(testCaseType.generateUniqueNameForTestAttribute(testAttribute, attributeName));
        }

        redrawDetailArea(testPolicyCmptTypeParam, testAttribute);
    }

    /**
     * Add attribute to test policy cmpt type param
     */
    private void changeAttributeClicked(IIpsObjectPart object) {
        ArgumentCheck.isInstanceOf(object, ITestAttribute.class);
        ITestAttribute testAttribute = (ITestAttribute) object;
        
        String[] attributeNames;
        try {
            attributeNames = selectAttributeByDialog((ITestPolicyCmptTypeParameter)testAttribute.getParent(),
                    Messages.TestCaseTypeSection_Dialog_SelectAttributeChange_Message, false); 
        } catch (Exception e) {
            throw new RuntimeException(e);
        }        
        
        if (attributeNames == null)
            return;
        
        testAttribute.setAttribute(attributeNames[0]);

        redrawDetailArea((ITestPolicyCmptTypeParameter) testAttribute.getParent(), testAttribute);
        
    }

    /**
     * Add attribute to test policy cmpt type param
     */
    private void removeAttributeClicked(IIpsObjectPart object) {
        ArgumentCheck.isInstanceOf(object, ITestAttribute.class);
        ITestAttribute testAttribute = (ITestAttribute) object;
        testAttribute.delete();
        redrawDetailArea((ITestPolicyCmptTypeParameter) testAttribute.getParent(), (ITestPolicyCmptTypeParameter) testAttribute.getParent());
    } 

    /**
     * Move attribute up
     */
    private void moveUpAttributeClicked(IIpsObjectPart object) {
        ArgumentCheck.isInstanceOf(object, ITestAttribute.class);
        moveTestAttribute((ITestAttribute) object, true);
    }

    /**
     * Move attribute down
     */
    private void moveDownAttributeClicked(IIpsObjectPart object) {
        ArgumentCheck.isInstanceOf(object, ITestAttribute.class);
        moveTestAttribute((ITestAttribute) object, false);
    } 
    
    /*
     * Creates the edit fields for the test parameter
     */
    private void createTestParamDetails(Composite editFieldsComposite,
            ITestParameter testParam,
            IpsPartUIController uiController) {
        Label label = toolkit.createFormLabel(editFieldsComposite, Messages.TestCaseTypeSection_EditFieldLabel_Name);
        EditField editFieldName = new TextField(toolkit.createText(editFieldsComposite));
        editFieldName.setText(testParam.getName());
        addSectionSelectionListeners(editFieldName, label,testParam);
        sectionFirstEditField.put(getKeyFor(testParam), editFieldName);
        
        label = toolkit.createFormLabel(editFieldsComposite, Messages.TestCaseTypeSection_EditFieldLabel_Role);
        EditField editFieldRole = new EnumValueField(toolkit.createCombo(editFieldsComposite, TestParameterRole
                .getEnumType()), TestParameterRole.getEnumType());
        addSectionSelectionListeners(editFieldRole, label, testParam);

        uiController.add(editFieldName, ITestParameter.PROPERTY_NAME);
        uiController.add(editFieldRole, ITestParameter.PROPERTY_TEST_PARAMETER_ROLE);
    }

    /*
     * Adds a listener to mark the section as selected if the given edit field gets the focus
     */
    private void addSectionSelectionListeners(EditField editField, Label label, final IIpsObjectPart object) {
        if (editField != null){
            editField.getControl().addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    Section section = (Section) sections.get(getKeyFor(object));
                    if (section != null){
                        currSelectedDetailObject = (IIpsObjectPart) sectionObjects.get(getKeyFor(object));
                        selectSection(section);
                    }
                }
            });
            editField.getControl().addMouseListener(new SectionSelectMouseListener(null, object));
        }
        if (label != null){
            label.addMouseListener(new SectionSelectMouseListener(null, object));
        }
    }

    /*
     * Select the given section, means change the color of the section and store the current object.
     * If the given section is <code>null</code> do nothing.
     */
    private void selectSection(Section section) {
        if (section == null)
            return;
        resetSectionSelectedColor();
        section.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        updateDetailButtonStatus(currSelectedDetailObject);
        
        // select the corresponding object in the tree
        ITestParameter testParam = getRootSectionObject(currSelectedDetailObject);
        treeViewer.setSelection(new StructuredSelection(testParam));
        updateTreeButtonStatus(testParam);
    }

    /*
     * Update the enable status of the detail buttons
     */
    private void updateDetailButtonStatus(IIpsObjectPart currSelectedDetailObject) {
        for (Iterator iter = sectionButtons.values().iterator(); iter.hasNext();) {
            SectionButtons buttons = (SectionButtons) iter.next();
            buttons.updateDetailButtonStatus(getRootSectionObject(currSelectedDetailObject), currSelectedDetailObject);
        }
    }

    /*
     * Returns the object which is the root of a section, the returned object is the object in the tree
     */
    private ITestParameter getRootSectionObject(IIpsObjectPart partInMainSection) {
        if (partInMainSection == null)
            return null;
        
        if (partInMainSection instanceof ITestAttribute)
            partInMainSection = (ITestPolicyCmptTypeParameter) partInMainSection.getParent();
        return (ITestParameter) partInMainSection;
    }

    /*
     * Reset the selection color of all sections
     */
    private void resetSectionSelectedColor(){
        for (Iterator iter = sections.values().iterator(); iter.hasNext();) {
            Section section = (Section) iter.next();
            section.setBackground(form.getBackground());
        }
    }
    
    /*
     * Creates the edit fields for the test value parameter
     */
    private void createTestValueParamDetails(Composite editFieldsComposite,
            ITestValueParameter parameter,
            IpsPartUIController uiController) {
        Label label = toolkit.createFormLabel(editFieldsComposite, Messages.TestCaseTypeSection_EditFieldLabel_Datatype);
        EditField editFieldDatatype = new TextField(toolkit.createText(editFieldsComposite));
        addSectionSelectionListeners(editFieldDatatype, label, parameter);
        editFieldDatatype.getControl().setEnabled(false);
        addSectionSelectionListeners(editFieldDatatype, label, parameter);
        
        // connect to model
        uiController.add(editFieldDatatype, ITestValueParameter.PROPERTY_VALUEDATATYPE);
    }
    
    /*
     * Creates the edit fields for the test policy cmpt type parameter
     */
    private void createTestPolicyCmptTypeParamDetails(Composite editFieldsComposite,
            ITestPolicyCmptTypeParameter parameter,
            IpsPartUIController uiController) {
        Label label = toolkit.createFormLabel(editFieldsComposite, Messages.TestCaseTypeSection_EditFieldLabel_MinInstances);
        EditField editFieldMin = new CardinalityField(toolkit.createText(editFieldsComposite));
        editFieldMin.setText(""+parameter.getMinInstances()); //$NON-NLS-1$
        addSectionSelectionListeners(editFieldMin, label, parameter);

        label = toolkit.createFormLabel(editFieldsComposite, Messages.TestCaseTypeSection_EditFieldLabel_MaxInstances);
        EditField editFieldMax = new CardinalityField(toolkit.createText(editFieldsComposite));
        editFieldMax.setText(""+parameter.getMaxInstances()); //$NON-NLS-1$
        addSectionSelectionListeners(editFieldMax, label, parameter);
        
        label = toolkit.createFormLabel(editFieldsComposite, Messages.TestCaseTypeSection_EditFieldLabel_RequiresProduct);
        EditField editFieldReqProd = new CheckboxField(toolkit.createCheckbox(editFieldsComposite));
        addSectionSelectionListeners(editFieldReqProd, label, parameter);
        
        label = toolkit.createFormLabel(editFieldsComposite, Messages.TestCaseTypeSection_TestCaseTypeSection_EditFieldLabel_PolicyCmptType);
        EditField editFieldPolicyCmptType = new TextField(toolkit.createText(editFieldsComposite));
        addSectionSelectionListeners(editFieldPolicyCmptType, label, parameter);
        editFieldPolicyCmptType.getControl().setEnabled(false);
        addSectionSelectionListeners(editFieldPolicyCmptType, label, parameter);
        
        EditField editFieldRelation = null;
        if (!parameter.isRoot()){
            label = toolkit.createFormLabel(editFieldsComposite, Messages.TestCaseTypeSection_EditFieldLabel_Relation);
            editFieldRelation = new TextField(toolkit.createText(editFieldsComposite));
            addSectionSelectionListeners(editFieldRelation, label, parameter);
            editFieldRelation.getControl().setEnabled(false);
            addSectionSelectionListeners(editFieldRelation, label, parameter);
        }
        
        // connect to model
        uiController.add(editFieldMin, ITestPolicyCmptTypeParameter.PROPERTY_MIN_INSTANCES);
        uiController.add(editFieldMax, ITestPolicyCmptTypeParameter.PROPERTY_MAX_INSTANCES);
        uiController.add(editFieldReqProd, ITestPolicyCmptTypeParameter.PROPERTY_REQUIRES_PRODUCTCMT);
        uiController.add(editFieldPolicyCmptType, ITestPolicyCmptTypeParameter.PROPERTY_POLICYCMPTTYPE);
        if (editFieldRelation != null){
            uiController.add(editFieldRelation, ITestPolicyCmptTypeParameter.PROPERTY_RELATION);
        }
    }

    /*
     * Creates the edit fields for the test attributes
     */
    private void createTestAttributeDetailSection(Composite parent, ITestAttribute testAttribute) {
        Section section = toolkit.getFormToolkit().createSection(parent, Section.COMPACT | Section.TWISTIE);
        section.setText(labelProvider.getText(testAttribute));
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        storeSection(section, testAttribute);
        
        Composite editFieldsComposite = toolkit.createLabelEditColumnComposite(section);
        section.setClient(editFieldsComposite);
        
        Label label = toolkit.createFormLabel(editFieldsComposite, Messages.TestCaseTypeSection_EditFieldLabel_Name);
        EditField editFieldName = new TextField(toolkit.createText(editFieldsComposite));
        editFieldName.setText(testAttribute.getName());
        addSectionSelectionListeners(editFieldName, label, testAttribute);
        
        label = toolkit.createFormLabel(editFieldsComposite, Messages.TestCaseTypeSection_EditFieldLabel_Role);
        EditField editFieldRole = new EnumValueField(toolkit.createCombo(editFieldsComposite, TestParameterRole
                .getEnumType()), TestParameterRole.getEnumType());
        addSectionSelectionListeners(editFieldRole, label, testAttribute);
        // removed the combined entry for test attributes, obly input and expected are allowed
        ((Combo) editFieldRole.getControl()).remove(TestParameterRole.COMBINED.getName());
        
        label = toolkit.createFormLabel(editFieldsComposite, Messages.TestCaseTypeSection_EditFieldLabel_Attribute);
        EditField editFieldAttribute = new TextField(toolkit.createText(editFieldsComposite));
        editFieldAttribute.getControl().setEnabled(false);
        addSectionSelectionListeners(editFieldAttribute, label, testAttribute);
        
        // connect to model
        IpsPartUIController uiController = createUIController(testAttribute);
        attributeControllers.add(uiController);

        uiController.add(editFieldName, ITestAttribute.PROPERTY_NAME);
        uiController.add(editFieldRole, ITestAttribute.PROPERTY_TEST_ATTRIBUTE_ROLE);
        uiController.add(editFieldAttribute, ITestAttribute.PROPERTY_ATTRIBUTE);
        
        uiController.updateUI();
    }

    /**
     * Clears the detail area.
     */
    public void clearDetailArea() {
        if (detailsArea != null)
            detailsArea.dispose();

        currSelectedDetailObject = null;
        sections.clear();
        sectionObjects.clear();
        sectionFirstEditField.clear();
        attributeControllers.clear();
        attributeIdx.clear();
        sectionButtons.clear();
        
        detailsArea = toolkit.getFormToolkit().createComposite(parentOfdetailsArea);
        detailsArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout detailLayout = new GridLayout(1, true);
        detailLayout.horizontalSpacing = 0;
        detailLayout.marginWidth = 0;
        detailLayout.marginHeight = 0;
        detailsArea.setLayout(detailLayout);
    }
    
    /**
     * Performs and returns validation messages on the given element.
     */
    MessageList validateElement(Object element) throws CoreException{
        MessageList messageList = new MessageList();
        // validate element
        if (element instanceof IIpsObjectPartContainer){
            messageList.add(((IIpsObjectPartContainer)element).validate());
        }
        return messageList;
    }

    /*
     * Store the given section and the object which is displayed in the section
     */
    private void storeSection(Section section, IIpsObjectPart object) {
        sections.put(getKeyFor(object), section);
        sectionObjects.put(getKeyFor(object), object);
        section.getChildren()[0].addMouseListener(new SectionSelectMouseListener(section, object));
        section.addMouseListener(new SectionSelectMouseListener(section, object));
        section.addExpansionListener(new SectionExpandListern(section, object));
    }
    
    /*
     * Returns the key identifier for the givenn object
     */
    private Integer getKeyFor(IIpsObjectPart object){
        return new Integer(System.identityHashCode(object));
    }

    /*
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
        new TableMessageHoverService(treeViewer) {
            protected MessageList getMessagesFor(Object element) throws CoreException {
                if (element != null) {
                    return validateElement(element);
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
                currSelectedDetailObject = (IIpsObjectPart)selected;
                selectFirstEditFieldInSection(selected);
            }
        });
    }

    /*
     * Selects the first edit field in the section contains the given object
     */
    private void selectFirstEditFieldInSection(Object selected) {
        EditField editFieldName = (EditField) sectionFirstEditField.get(getKeyFor((IIpsObjectPart)selected));
        if (editFieldName == null)
            return;
        editFieldName.getControl().setFocus();
    }
    
    /*
     * Adds the button listener for the tree area.
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
        changeRelationButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    changeRelationClicked();
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        addRootTestValueParameterButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    addRootTestValueParameterClicked();
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });        
        addRootTestPolicyCmptTypeParamButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    addRootTestPolicyCmptTypeParameterClicked();
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        changeTargetButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    changeTargetClicked();
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });        
    }
    
    /*
     * Redraws the form.
     */
    private void redrawForm() {
        // redraw the form
        try{
            setFormRedraw(false);
            pack();
            getParent().layout(true);
            form.reflow(true);
        } finally{
            setFormRedraw(true);
        }
    }
    
    /*
     * Add test value parameter button was clicked.
     */
    private void addRootTestValueParameterClicked() {
        String[] datatypeNames = null;
        try {
            datatypeNames = selectValueDatatypeByDialog(Messages.TestCaseTypeSection_Dialog_SelectDatatypeAdd_Message, true); 
        } catch (Exception e) {
            throw new RuntimeException(e);
        }        
        
        if (datatypeNames == null)
            return;        
        
        ITestValueParameter newParam = null;
        for (int i = 0; i < datatypeNames.length; i++) {
            newParam = testCaseType.newInputTestValueParameter();
            newParam.setName(StringUtil.unqualifiedName(datatypeNames[i]));
            newParam.setValueDatatype(datatypeNames[i]);
        }
        
        refreshTreeAndDetails(newParam);             
    }
    
    /*
     * Add root test policy cmpt type parameter clicked.
     */
    private void addRootTestPolicyCmptTypeParameterClicked() {
        String[] policyCmptTypeNames = null;
        try {
            policyCmptTypeNames = selectPolicyCmptTypeByDialog(null,
                    Messages.TestCaseTypeSection_Dialog_SelectPolicyCmptTypeAdd_Message, true); 
        } catch (Exception e) {
            throw new RuntimeException(e);
        }        
        
        if (policyCmptTypeNames == null)
            return;        
        
        ITestPolicyCmptTypeParameter newParam = null;
        for (int i = 0; i < policyCmptTypeNames.length; i++) {
            newParam = testCaseType.newInputTestPolicyCmptTypeParameter();
            newParam.setName(StringUtil.unqualifiedName(policyCmptTypeNames[i]));
            newParam.setPolicyCmptType(policyCmptTypeNames[i]);
        }
        
        refreshTreeAndDetails(newParam);        
    }
    
    /*
     * Add button was clicked.
     */
    private void addClicked() {
        ITestParameter testParameter = getSelectedTestParameterInTree();
        if (testParameter == null)
            return;
        
        if (! (testParameter instanceof ITestPolicyCmptTypeParameter))
                return;
        
        ITestPolicyCmptTypeParameter testPolicyCmptTypeParam = (ITestPolicyCmptTypeParameter) testParameter;
        IRelation[] relations;
        try {
            relations = selectRelationByDialog(testPolicyCmptTypeParam,
                    Messages.TestCaseTypeSection_Dialog_SelectRelationAdd_Message, true); 
        } catch (Exception e) {
            throw new RuntimeException(e);
        }        
        
        if (relations == null)
            return;        
        
        ITestPolicyCmptTypeParameter child = null;
        for (int i = 0; i < relations.length; i++) {
            child = testPolicyCmptTypeParam.newTestPolicyCmptTypeParamChild();
            child.setRelation(relations[i].getName());
            child.setPolicyCmptType(relations[i].getTarget());
            child.setName(StringUtil.unqualifiedName(relations[i].getTarget()));
            child.setTestParameterRole(testPolicyCmptTypeParam.getTestParameterRole());
        }
        
        refreshTreeAndDetails(child);
    }

    /*
     * Remove button was clicked.
     */
    private void removeClicked() {
        ITestParameter testParameter = getSelectedTestParameterInTree();
        if (testParameter != null)
            testParameter.delete();
        
        currSelectedDetailObject = getSelectedTestParameterInTree();
        refreshTreeAndDetails(getRootSectionObject(currSelectedDetailObject));
    }
    
    /*
     * Change relation button was clicked.
     */
    private void changeRelationClicked() {
        ITestParameter testParameter = getSelectedTestParameterInTree();
        if (testParameter == null)
            return;

        if (!(testParameter instanceof ITestPolicyCmptTypeParameter))
            return;

        ITestPolicyCmptTypeParameter testPolicyCmptTypeParam = (ITestPolicyCmptTypeParameter)testParameter;

        try {
            ITestPolicyCmptTypeParameter testPolicyCmptTypeParamParent = (ITestPolicyCmptTypeParameter)testPolicyCmptTypeParam
                    .getParent();
            IRelation[] relations = selectRelationByDialog(testPolicyCmptTypeParamParent,
                    Messages.TestCaseTypeSection_Dialog_SelectRelationChange_Message, false);

            if (relations == null)
                return;

            testPolicyCmptTypeParam.setRelation(relations[0].getName());
            testPolicyCmptTypeParam.setPolicyCmptType(relations[0].getTarget());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        refreshTreeAndDetails(testPolicyCmptTypeParam);
    }
    
    /*
     * Change target button was clicked.
     */ 
    private void changeTargetClicked() {
        ITestParameter testParameter = getSelectedTestParameterInTree();
        if (testParameter == null)
            return;

        if (testParameter instanceof ITestPolicyCmptTypeParameter){
            // change policy cmpt type target
            ITestPolicyCmptTypeParameter testPolicyCmptTypeParam = (ITestPolicyCmptTypeParameter)testParameter;
    
            try {
                String[] policyCmptTypeNames = null;
                policyCmptTypeNames = selectPolicyCmptTypeByDialog(testPolicyCmptTypeParam.findRelation().findTarget(),
                        Messages.TestCaseTypeSection_Dialog_SelectPolicyCmptTypeChange_Message, false);
                if (policyCmptTypeNames == null)
                    return;
    
                testPolicyCmptTypeParam.setPolicyCmptType(policyCmptTypeNames[0]);
    
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (testParameter instanceof ITestValueParameter) {
            // change datavalue targt
            ITestValueParameter testValueParameter = (ITestValueParameter) testParameter;
            
            try {
                String[] valueDatatypes = null;
                valueDatatypes = selectValueDatatypeByDialog(Messages.TestCaseTypeSection_Dialog_SelectDatatypeChange_Message, false);
                if (valueDatatypes == null)
                    return;
    
                testValueParameter.setValueDatatype(valueDatatypes[0]);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } 
        
        refreshTreeAndDetails(testParameter);
    } 

    /*
     * Gets the currently selected test parameter or null if no test parameter is selected in the
     * tree
     */
    private ITestParameter getSelectedTestParameterInTree(){
        ISelection selection = treeViewer.getSelection();
        if (! (selection instanceof IStructuredSelection))
            return null;
        Object selectedElem = ((IStructuredSelection)selection).getFirstElement();
        if (! (selectedElem instanceof ITestParameter))
            return null;
        return (ITestParameter) selectedElem;
    }
    
    /*
     * Moves the give test attribute up or down
     */
    private void moveTestAttribute(ITestAttribute testAttribute, boolean up) {
        Integer testAttributeIdx = (Integer) attributeIdx.get(getKeyFor(testAttribute));
        if (testAttributeIdx == null)
            throw new RuntimeException(Messages.TestCaseTypeSection_Error_WrongTestAttributeIndex);
        
        ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter = (ITestPolicyCmptTypeParameter) testAttribute.getParent();
        testPolicyCmptTypeParameter.moveTestAttributes(new int[]{testAttributeIdx.intValue()}, up);
        redrawDetailArea(testPolicyCmptTypeParameter, testAttribute);
    }
    
    /*
     * The selection in the tree changed the given object is selected.
     * @throws CoreException 
     */
    private void selectionInTreeChanged(IStructuredSelection selection) {
        if (selection instanceof IStructuredSelection){
            Object selectedObj = ((IStructuredSelection) selection).getFirstElement();
            if (selectedObj != null && selectedObj instanceof ITestParameter){
                // if the selected object is the previous rendered object 
                // or all elements are visible return
                if (showAll || (ITestParameter) selectedObj == prevSelectedTestParam)
                    return;

                createDetailsArea((ITestParameter) selectedObj);
            }
            
            updateTreeButtonStatus((ITestParameter) selectedObj);
        }
    }
    
    /*
     * Updates the enable state of the buttons which belongs to the tree
     */
    private void updateTreeButtonStatus(ITestParameter parameter) {
        if (parameter instanceof ITestValueParameter){
            removeButton.setEnabled(true);
            changeTargetButton.setEnabled(true);
            addButton.setEnabled(false);
            changeRelationButton.setEnabled(false);
        } else if (parameter instanceof ITestPolicyCmptTypeParameter){
            removeButton.setEnabled(true);
            changeTargetButton.setEnabled(true);
            addButton.setEnabled(true);
            boolean changeRelationBtnEnabled = false;
            if (getSelectedTestParameterInTree() != null)
                changeRelationBtnEnabled = !getSelectedTestParameterInTree().isRoot();
            changeRelationButton.setEnabled(changeRelationBtnEnabled);
        } else{
            removeButton.setEnabled(false);
            changeTargetButton.setEnabled(false);
            addButton.setEnabled(false);
            changeRelationButton.setEnabled(false);
        }
    }

    /*
     * Refreshs the tree and details
     */
    private void refreshTreeAndDetails(ITestParameter param) {
        refreshTree();
        createDetailsArea(param);
        currSelectedDetailObject = param;
        selectSection((Section)sections.get(getKeyFor(param)));
        selectFirstEditFieldInSection(param);
    }
    
    /*
     * Refresh the tree.
     */
    private void refreshTree(){
        if (treeViewer.getTree().isDisposed())
            return;
        
        try{
            isTreeRefreshing = true;
            treeViewer.getTree().setRedraw(false);
            TreeViewerExpandStateStorage treeexpandStorage = new TreeViewerExpandStateStorage(treeViewer);
            treeexpandStorage.storeExpandedStatus();
            treeViewer.refresh();
            treeViewer.expandAll();
            treeViewer.collapseAll();
            treeexpandStorage.restoreExpandedStatus();
        }finally {
            treeViewer.getTree().setRedraw(true);
            isTreeRefreshing = false;
        }
    }

    /*
     * Refreshs all section titles
     */
    private void refreshSectionTitles() {
        for (Iterator iter = sections.keySet().iterator(); iter.hasNext();) {
            Integer key = (Integer) iter.next();
            Section section = (Section) sections.get(key);
            section.setText(labelProvider.getText((IIpsObjectPart) sectionObjects.get(key)));
            section.getParent().setRedraw(false);
            section.pack();
            section.getParent().layout();
            section.getParent().setRedraw(true);
        }
    }
    
    /*
     * Redraw and select the section of the new attribute
     */
    private void redrawDetailArea(ITestPolicyCmptTypeParameter testPolicyCmptTypeParam, IIpsObjectPart selectedObject) {
        
        // store the expanded sections state
        List expandedSectionKeys = new ArrayList(sections.size());
        for (Iterator iter = sections.keySet().iterator(); iter.hasNext();) {
            Integer key = (Integer) iter.next();
            if (((Section)sections.get(key)).isExpanded())
                expandedSectionKeys.add(key);
        }
        
        try {
            setFormRedraw(false);
            refreshTree();
            prevSelectedTestParam = null;
            createDetailsArea(testPolicyCmptTypeParam);
            currSelectedDetailObject = selectedObject;
            selectSection((Section)sections.get(getKeyFor(selectedObject)));

            // restore the expanded section states
            for (Iterator iter = expandedSectionKeys.iterator(); iter.hasNext();) {
                Section section = (Section)sections.get((Integer)iter.next());
                section.setExpanded(true);
            }
        } finally {
            setFormRedraw(true);
        }
    }
    
    /*
     * Create a bordered composite
     */
    private Composite createBorderComposite(Composite parent){
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
    
    /*
     * Creates a special separator styled for separation of two buttons
     */
    private void createButtonSeparator(Composite buttons) {
        toolkit.createVerticalSpacer(buttons, 2).setBackground(buttons.getBackground());
        toolkit.createVerticalSpacer(buttons, 2);
        toolkit.createVerticalSpacer(buttons, 2).setBackground(buttons.getBackground());
    }
    
    /*
     * Sets the redraw state of the form
     */
    private void setFormRedraw(boolean redraw){
        form.setRedraw(redraw);
    }
    
    /*
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
        return controller;
    }
   
    /*
     * Displays a dialog to select a policy component type.
     */
    private String[] selectPolicyCmptTypeByDialog(IPolicyCmptType policyCmptType, String message, boolean multi) throws CoreException {
        ElementListSelectionDialog selectDialog = new ElementListSelectionDialog(getShell(), new DefaultLabelProvider());
        selectDialog.setTitle(Messages.TestCaseTypeSection_Dialog_SelectPolicyCmptType_Title);
        selectDialog.setMessage(message);
        selectDialog.setMultipleSelection(multi);
        
        IIpsObject[] policyCmptTypes = null;
        if (policyCmptType == null){
            // find all policy component types
            policyCmptTypes = testCaseType.getIpsProject().findIpsObjects(IpsObjectType.POLICY_CMPT_TYPE);
        } else {
            // find all policy component of the given type (incl. subclasses)
            ITypeHierarchy subTypeHierarchy = policyCmptType.getSubtypeHierarchy();
            IPolicyCmptType[] subTypes = subTypeHierarchy.getAllSubtypes(policyCmptType);
            if (subTypes == null)
                subTypes = new IPolicyCmptType[0];
            policyCmptTypes = new IIpsObject[subTypes.length + 1];
            System.arraycopy(subTypes, 0, policyCmptTypes, 0, subTypes.length);
            policyCmptTypes[subTypes.length] = policyCmptType;
        }
        
        selectDialog.setElements(policyCmptTypes);
        if (selectDialog.open() == Window.OK) {
            if (selectDialog.getResult().length > 0) {
                Object[] result = (Object[])selectDialog.getResult();
                String[] qualifiedNames = new String[result.length];
                for (int i = 0; i < result.length; i++) {
                    qualifiedNames[i] = ((IPolicyCmptType)result[i]).getQualifiedName();
                }
                return qualifiedNames;
            }
        }
        return null;
    }

    
    /*
     * Displays a dialog to select attributes of a policy component.
     */
    private String[] selectAttributeByDialog(ITestPolicyCmptTypeParameter testPolicyCmptTypeParam,
            String message,
            boolean multi) throws CoreException {
        ElementListSelectionDialog selectDialog = new ElementListSelectionDialog(getShell(), new DefaultLabelProvider());
        selectDialog.setMultipleSelection(multi);
        selectDialog.setTitle(Messages.TestCaseTypeSection_Dialog_SelectAttribute_Title);
        selectDialog.setMessage(message);

        IPolicyCmptType policyCmptType = testPolicyCmptTypeParam.findPolicyCmptType();
        ITypeHierarchy superTypeHierarchy = policyCmptType.getSupertypeHierarchy();
        IAttribute[] attributes = superTypeHierarchy.getAllAttributes(policyCmptType);

        selectDialog.setElements(attributes);
        if (selectDialog.open() == Window.OK) {
            if (selectDialog.getResult().length > 0) {
                Object[] result = (Object[])selectDialog.getResult();
                String[] attributeNames = new String[result.length];
                for (int i = 0; i < result.length; i++) {
                    attributeNames[i] = ((IAttribute)result[i]).getName();
                }
                return attributeNames;
            }
        }
        return null;
    }
    
    /*
     * Displays a dialog to select data valuetypes.
     */
    private String[] selectValueDatatypeByDialog(String message, boolean multi) throws CoreException {
        ElementListSelectionDialog selectDialog = new ElementListSelectionDialog(getShell(), new DefaultLabelProvider());
        selectDialog.setMultipleSelection(multi);
        selectDialog.setTitle(Messages.TestCaseTypeSection_Dialog_SelectDatatype_Title);
        selectDialog.setMessage(message);

        Datatype[] datatypes = (Datatype[]) testCaseType.getIpsProject().findDatatypes(true, true, true);
        selectDialog.setElements(datatypes);
        if (selectDialog.open() == Window.OK) {
            Object[] result = selectDialog.getResult();
            if (result.length > 0) {
                String[] valueDatatypeResult = new String[result.length];
                for (int i = 0; i < result.length; i++) {
                    valueDatatypeResult[i] = ((Datatype)result[i]).getQualifiedName();
                }
                return valueDatatypeResult;
            }
        }
        return null;
    }
    
    /*
     * Displays a dialog to select relations of a policy component.
     */
    private IRelation[] selectRelationByDialog(ITestPolicyCmptTypeParameter testPolicyCmptTypeParam,
            String message,
            boolean multi) throws CoreException {
        ElementListSelectionDialog selectDialog = new ElementListSelectionDialog(getShell(), new DefaultLabelProvider());
        selectDialog.setMultipleSelection(multi);
        selectDialog.setTitle(Messages.TestCaseTypeSection_Dialog_SelectRelation_Title);
        selectDialog.setMessage(message);

        IPolicyCmptType policyCmptType = testPolicyCmptTypeParam.findPolicyCmptType();
        ITypeHierarchy superTypeHierarchy = policyCmptType.getSupertypeHierarchy();
        IRelation[] relations = superTypeHierarchy.getAllRelations(policyCmptType);
        selectDialog.setElements(relations);
        if (selectDialog.open() == Window.OK) {
            Object[] result = selectDialog.getResult();
            if (result.length > 0) {
                IRelation[] relationsResult = new IRelation[result.length];
                System.arraycopy(result, 0, relationsResult, 0, result.length);
                return relationsResult;
            }
        }
        return null;
    }
}
