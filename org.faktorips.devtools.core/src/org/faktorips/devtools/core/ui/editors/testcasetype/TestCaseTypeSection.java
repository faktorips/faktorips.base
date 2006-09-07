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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.MessageCueLabelProvider;
import org.faktorips.devtools.core.ui.ProblemImageDescriptor;
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
import org.faktorips.util.memento.Memento;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.EnumValue;

/**
 * Section to display and edit the test case type.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTypeSection extends IpsSection {
    private Image empytImage;
    
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
    private Button addRootParameterButton;
    private Button addChildButton;
    private Button removeButton;
    
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
    
    // Contains the currently selected object in the detail area
    private IIpsObjectPart currSelectedDetailObject;
    
    // Container for the attribute ui controllers
    //  used to update the attribute edit fields if the test case type changed,
    //  thus if the type of a test policy cmpt type param changed the attribute will
    //  show the correct validation result
    private List attributeControllers = new ArrayList();

    private SectionDetailObjectCache objectCache;
    
    /*
     * Object cache to store several object to render the ui
     */
    private class SectionDetailObjectCache{
        // Containers for all sections and their objects
        private HashMap sections = new HashMap();
        private HashMap sectionObjects = new HashMap();
        private HashMap sectionFirstEditField = new HashMap();
        private HashMap attributeIdx = new HashMap();
        private HashMap sectionButtons = new HashMap();
        private HashMap attributeTableViewers = new HashMap();
        
        /*
         * Returns the key identifier for the given object
         */
        private Integer getKeyFor(IIpsObjectPart object){
            return new Integer(System.identityHashCode(object));
        }

        /*
         * Clear all container
         */
        public void clear() {
            sections.clear();
            sectionObjects.clear();
            sectionFirstEditField.clear();
            attributeControllers.clear();
            attributeIdx.clear();
            sectionButtons.clear();
            attributeTableViewers.clear();
        }

        public void putAttributeIdx(ITestAttribute attribute, int idx) {
            attributeIdx.put(getKeyFor(attribute), new Integer(idx));
            sectionObjects.put(getKeyFor(attribute), attribute);
        }

        public void putSectionButtons(ITestParameter testParam, SectionButtons currSectionButtons) {
            sectionButtons.put(getKeyFor(testParam), currSectionButtons);
        }

        public void putFirstEditFieldInSection(ITestParameter testParam, EditField editFieldName) {
            sectionFirstEditField.put(getKeyFor(testParam), editFieldName);
        }

        public void putSection(IIpsObjectPart object, Section section) {
            sections.put(getKeyFor(object), section);
        }

        public void putSectionObjects(IIpsObjectPart object) {
            sectionObjects.put(getKeyFor(object), object);
        }

        public Section getSection(IIpsObjectPart mainSectionObject) {
            if (mainSectionObject instanceof ITestAttribute)
                mainSectionObject = (IIpsObjectPart) ((ITestAttribute) mainSectionObject).getParent();
            
            return (Section) sections.get(getKeyFor(mainSectionObject));
        }

        public IIpsObjectPart getObject(IIpsObjectPart object) {
            return (IIpsObjectPart) sectionObjects.get(getKeyFor(object));
        }

        public Collection getAllSections() {
            return sections.values();
        }

        public EditField getSectionFirstEditField(Object selected) {
            return (EditField) sectionFirstEditField.get(getKeyFor((IIpsObjectPart)selected));
        }

        public Integer getIdxFromAttribute(ITestAttribute testAttribute) {
            return (Integer) attributeIdx.get(getKeyFor(testAttribute));
        }

        public ITestAttribute getAttributeByIndex(Integer idx){
            for (Iterator iter = attributeIdx.keySet().iterator(); iter.hasNext();) {
                Integer key = (Integer)iter.next();
                if (idx.equals(attributeIdx.get(key)))
                    return (ITestAttribute) getObjectByKey(key);
            }
            return null;
        }

        public Collection getAllSectionKeys() {
            return sections.keySet();
        }

        public Section getSectionByKey(Integer key) {
            return (Section) sections.get(key);
        }

        public Object getObjectByKey(Integer key) {
            return (IIpsObjectPart) sectionObjects.get(key);
        }

        public Collection getAllSectionButtons() {
            return sectionButtons.values();
        }

        public void putAttributeTable(ITestParameter testParam, TableViewer attributeTableViewer) {
            attributeTableViewers.put(getKeyFor(testParam), attributeTableViewer);
        }
        
        public TableViewer getAttributeTable(ITestParameter testParam){
            return (TableViewer) attributeTableViewers.get(getKeyFor(testParam));
        }
        
        public Collection getAllAttributeTable(){
            return attributeTableViewers.values();
        }
    }
    
    /*
     * Class used to store button objects for each section
     */
    private class SectionButtons{
        private Section section;
        private Button addAtributeButton;
        private Button removeAttributeButton;
        private Button moveAttributeUp;
        private Button moveAttributeDown;
        
        public void createButtons(Composite structureComposite, Section section) {
            this.section = section;
            
            Composite buttons = toolkit.getFormToolkit().createComposite(structureComposite);
            buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
            GridLayout buttonLayout = new GridLayout(1, true);
            buttons.setLayout(buttonLayout);
            
            addAtributeButton = toolkit.createButton(buttons, Messages.TestCaseTypeSection_Button_AddAttribute);
            removeAttributeButton = toolkit.createButton(buttons, Messages.TestCaseTypeSection_Button_RemoveAttribute);
            moveAttributeUp = toolkit.createButton(buttons, Messages.TestCaseTypeSection_Button_MoveAttributeUp);
            moveAttributeDown = toolkit.createButton(buttons, Messages.TestCaseTypeSection_Button_MoveAttributeDown);
            
            addAtributeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            removeAttributeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            moveAttributeUp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            moveAttributeDown.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            
            addAtributeButton.setEnabled(false);
            removeAttributeButton.setEnabled(false);
            moveAttributeUp.setEnabled(false);
            moveAttributeDown.setEnabled(false);
            
            hookButtonListenersDetail();
        }
        
        public void updateDetailButtonStatus(IIpsObjectPart mainSectionObject, IIpsObjectPart currSelectedDetailObject) {
            if (section != objectCache.getSection(mainSectionObject)){
                addAtributeButton.setEnabled(false);
                removeAttributeButton.setEnabled(false);
                moveAttributeUp.setEnabled(false);
                moveAttributeDown.setEnabled(false);
                return;
            }
            
            if (currSelectedDetailObject instanceof ITestPolicyCmptTypeParameter){
                addAtributeButton.setEnabled(true);
                removeAttributeButton.setEnabled(false);
                moveAttributeUp.setEnabled(false);
                moveAttributeDown.setEnabled(false);
            } else if (currSelectedDetailObject instanceof ITestAttribute){
                addAtributeButton.setEnabled(true);
                removeAttributeButton.setEnabled(true);
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
                Section section = objectCache.getSection(object);
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
     * Cell Modifier for the test attribute table
     */
    private class TestAttributeCellModifier implements ICellModifier {
        private TableViewer viewer;
        
        public TestAttributeCellModifier(TableViewer viewer){
            this.viewer = viewer;
        }
        
        /**
         * {@inheritDoc}
         */
        public boolean canModify(Object element, String property) {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public Object getValue(Object element, String property) {
            if (property.equals(ITestAttribute.PROPERTY_TEST_ATTRIBUTE_TYPE)) {
                return getTestAttributeFromObject(element).getTestAttributeType().getIndex();
            } else if (property.equals(ITestAttribute.PROPERTY_NAME)){
                return getTestAttributeFromObject(element).getName();
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public void modify(Object element, String property, Object value) {
            TableItem tableItem = (TableItem)element;
            if (property.equals(ITestAttribute.PROPERTY_TEST_ATTRIBUTE_TYPE)) {
                TestParameterType selectedType = TestParameterType.getTestParameterType((Integer)value);
                if (selectedType == null)
                    return;
                getTestAttributeFromObject(tableItem.getData()).setTestAttributeType(selectedType);
                tableItem.setText(2, selectedType.getName());
            } else if (property.equals(ITestAttribute.PROPERTY_NAME)){
                getTestAttributeFromObject(tableItem.getData()).setName((String)value);
                tableItem.setText(1, (String) value);
            }
            repackAttributeTable(viewer);
        }
        
        private ITestAttribute getTestAttributeFromObject(Object obj){
            ArgumentCheck.isInstanceOf(obj, ITestAttribute.class);
            return (ITestAttribute) obj;
        }
    }
    
    /*
     * Label provider for the attribute table
     */
    private class TestAttributeTblLabelProvider extends LabelProvider implements ITableLabelProvider{
        public Image getColumnImage(Object element, int columnIndex) {
            Image defaultImage = null;
            try {
                MessageList msgList = ((ITestAttribute) element).validate();
                switch (columnIndex) {
                    case 0:
                        defaultImage = getImageForAttribute(element);
                        msgList = msgList.getMessagesFor(element);
                        if (! msgList.isEmpty()){
                            return getImageForMsgList(defaultImage, msgList);
                        }
                        break;
                    case 1:
                        defaultImage = empytImage;
                        msgList = msgList.getMessagesFor(element, ITestAttribute.PROPERTY_NAME);
                        if (! msgList.isEmpty()){
                            return getImageForMsgList(defaultImage, msgList);
                        }
                        return null;
                    case 2:
                        defaultImage = ((ITestAttribute)element).getTestAttributeType().getImage();
                        msgList = msgList.getMessagesFor(element, ITestAttribute.PROPERTY_TEST_ATTRIBUTE_TYPE);
                        if (! msgList.isEmpty()){
                            return getImageForMsgList(defaultImage, msgList);
                        }
                    default:
                        break;
                }
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
            return defaultImage;
        }

        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof ITestAttribute){
                ITestAttribute testAttribute = (ITestAttribute) element;
                switch (columnIndex) {
                    case 0:
                        return testAttribute.getAttribute();
                    case 1:
                        return testAttribute.getName();
                    case 2:
                        return testAttribute.getTestAttributeType().getName();
                    default:
                        break;
                }
            }
            return null;
        }

        private Image getImageForAttribute(Object element) throws CoreException {
            Image defaultImage;
            IAttribute attribute = ((ITestAttribute) element).findAttribute();
            if (attribute != null){
                defaultImage = attribute.getImage();
            } else {
                // default image if attribute not found
                defaultImage = IpsPlugin.getDefault().getImage("MissingAttribute.gif"); //$NON-NLS-1$
            }
            return defaultImage;
        }
    }

    private Image getImageForMsgList(Image defaultImage, MessageList msgList) {
        ProblemImageDescriptor descriptor = new ProblemImageDescriptor(defaultImage, msgList.getSeverity());
        return IpsPlugin.getDefault().getImage(descriptor);
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
    
    public TestCaseTypeSection(Composite parent, UIToolkit toolkit, final ITestCaseType testCaseType, String title,
            String detailTitle, ScrolledForm form) {
        super(parent, Section.NO_TITLE, GridData.FILL_BOTH, toolkit);

        this.empytImage = new Image(getDisplay(), 16, 16);
        this.objectCache = new SectionDetailObjectCache();
        
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
                    postRefreshAll();
                }
            }
        });        
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        empytImage.dispose();
        super.dispose();
    }
    
    /**
     * {@inheritDoc}
     */
    protected void performRefresh() {
    }

    /*
     * Post the given runnable to the async executable list of the display
     */
    private void postAsyncRunnable(Runnable r) {
        if (!isDisposed())
            getDisplay().asyncExec(r);
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
        
        addRootParameterButton = toolkit.createButton(buttons, Messages.TestCaseTypeSection_Button_NewRootParameter);
        addChildButton = toolkit.createButton(buttons, Messages.TestCaseTypeSection_Button_NewChildParameter);
        removeButton = toolkit.createButton(buttons, Messages.TestCaseTypeSection_Button_Remove);
        
        addRootParameterButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addChildButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
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
                firstSection = objectCache.getSection(firstTestParam);
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
    private void createDetailsForAllTestParams(ITestParameter[] testParams) {
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
        Composite detailComposite = toolkit.getFormToolkit().createComposite(detailsArea);
        detailComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout structureLayout = new GridLayout(2, false);
        structureLayout.horizontalSpacing = 0;
        structureLayout.marginWidth = 0;
        structureLayout.marginHeight = 3;
        detailComposite.setLayout(structureLayout);
        
        MessageList msgList = null;
        try {
            msgList = testParam.validate();
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        Composite details = createBorderComposite(detailComposite);
        Section section = toolkit.getFormToolkit().createSection(details, 0);
        section.setText(labelProvider.getText(testParam));
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        // create error indicator in the description bar
        //   show errors for property policy cmpt type and property relation,
        //   because there are no edit fields which shows these errors
        String errorMessageText = ""; //$NON-NLS-1$
        MessageList msgListPolicyCmptType = msgList.getMessagesFor(testParam,
                ITestPolicyCmptTypeParameter.PROPERTY_POLICYCMPTTYPE);
        MessageList msgListPolicyCmptRelation = msgList.getMessagesFor(testParam,
                ITestPolicyCmptTypeParameter.PROPERTY_RELATION);
        if (!msgListPolicyCmptType.isEmpty()) {
            String msgText = msgListPolicyCmptType.getText();
            errorMessageText += errorMessageText.length()>0?"<br/>":""; //$NON-NLS-1$ //$NON-NLS-2$
            errorMessageText += "<p><img href=\"imagepccmpttype\"/> <span color=\"red\">" + msgText + "</span></p>"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (!msgListPolicyCmptRelation.isEmpty()) {
            String msgText = msgListPolicyCmptRelation.getText();
            errorMessageText += errorMessageText.length()>0?"<br/>":""; //$NON-NLS-1$ //$NON-NLS-2$
            errorMessageText += "<p><img href=\"imagerelation\"/> <span color=\"red\">" + msgText + "</span></p>"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (errorMessageText.length()>0){
            FormToolkit toolkit = new FormToolkit(form.getParent().getDisplay());
            FormText formText = toolkit.createFormText(section, false);
            formText.setImage("imagepccmpttype", getImageForMsgList(IpsObjectType.POLICY_CMPT_TYPE.getImage(), msgList)); //$NON-NLS-1$
            formText.setImage("imagerelation", getImageForMsgList(IpsPlugin.getDefault().getImage("Relation.gif"), msgList)); //$NON-NLS-1$ //$NON-NLS-2$
            formText.setColor("red", getDisplay().getSystemColor(SWT.COLOR_DARK_RED)); //$NON-NLS-1$
            formText.setText("<form>" + errorMessageText + "</form>", true, false); //$NON-NLS-1$ //$NON-NLS-2$
            section.setDescriptionControl(formText);
            addSectionSelectionListeners(null, formText, testParam);
        }
        
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
            
            Composite tableBtnComposite = toolkit.getFormToolkit().createComposite(details);
            tableBtnComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
            GridLayout tableBtnLayout = new GridLayout(2, false);
            tableBtnLayout.horizontalSpacing = 0;
            tableBtnLayout.marginWidth = 0;
            tableBtnLayout.marginHeight = 3;
            tableBtnComposite.setLayout(structureLayout);
            
            // create the attribute table
            final TableViewer attributeTableViewer = createTestAttributeTable(tableBtnComposite);
            objectCache.putAttributeTable(testParam, attributeTableViewer);
            
            // stores the index of the test attributes for faster move up/down
            ITestAttribute[] testAttributes = testPolicyCmptTypeParam.getTestAttributes();
            for (int i = 0; i < testAttributes.length; i++) {
                objectCache.putAttributeIdx(testAttributes[i], i);
            }
            // set the test attribute table input and pack the columns of the table
            attributeTableViewer.setInput(testAttributes);
            repackAttributeTable(attributeTableViewer);
            
            SectionButtons currSectionButtons = new SectionButtons();
            currSectionButtons.createButtons(tableBtnComposite, section);
            objectCache.putSectionButtons(testParam, currSectionButtons);
            
        } else if (testParam instanceof ITestValueParameter){
            createTestValueParamDetails(editFieldsComposite, (ITestValueParameter)testParam, uiController);
        }
        
        uiController.updateUI();
        return section;
    }

    /*
     * Repacks the columns in the test attribute table
     */
    private void repackAttributeTable(TableViewer attributeTableViewer) {
        for (int i = 0, n = attributeTableViewer.getTable().getColumnCount(); i < n; i++) {
            attributeTableViewer.getTable().getColumn(i).pack();
        }
    }

    /*
     * Create the test attribute table
     */
    private TableViewer createTestAttributeTable(Composite details) {
        Table table = new Table(details, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible (true);
        table.setLinesVisible (true);
        TableColumn column = new TableColumn(table, SWT.LEFT);
        column.setText(Messages.TestCaseTypeSection_AttributeTable_ColumnTitleAttribute);
        
        column = new TableColumn(table, SWT.LEFT);
        column.setText(Messages.TestCaseTypeSection_AttributeTable_ColumnTitleAttributeName);
        
        column = new TableColumn(table, SWT.LEFT);
        column.setText(Messages.TestCaseTypeSection_AttributeTable_ColumnTitleAttributeType);

        // Create the viewer and connect it to the view
        final TableViewer viewer = new TableViewer(table);
        viewer.setContentProvider (new ArrayContentProvider());
        viewer.setLabelProvider (new TestAttributeTblLabelProvider());
        
        EnumValue[] values = TestParameterType.getEnumType().getValues();
        String[] valueNames = new String[values.length -1];
        int idx=0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] != TestParameterType.COMBINED)
                valueNames[idx++] = values[i].getName();
        }
        
        // create the cell editors
        ComboBoxCellEditor cellEditorTestAttributeType = new ComboBoxCellEditor(table, valueNames, SWT.READ_ONLY); //$NON-NLS-1$ //$NON-NLS-2$
        TextCellEditor textCellEditor = new TextCellEditor(table);
        
        viewer.setCellEditors(new CellEditor[]{null, textCellEditor, cellEditorTestAttributeType});
        viewer.setCellModifier(new TestAttributeCellModifier(viewer));
        viewer.setColumnProperties(new String[] { ITestAttribute.PROPERTY_ATTRIBUTE, ITestAttribute.PROPERTY_NAME,
                ITestAttribute.PROPERTY_TEST_ATTRIBUTE_TYPE });

        // add listener to the table
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                if (event.getSelection() instanceof IStructuredSelection){
                    Object firstElement = ((IStructuredSelection)event.getSelection()).getFirstElement();
                    if (firstElement instanceof ITestAttribute){
                        currSelectedDetailObject = (ITestAttribute)firstElement;
                        updateDetailButtonStatus((ITestAttribute)firstElement);
                        selectSection(objectCache.getSection((ITestAttribute)firstElement));
                    }
                }
            }
        });
        new TableMessageHoverService(viewer) {
            protected MessageList getMessagesFor(Object element) throws CoreException {
                if (element != null) {
                    return validateElement(element);
                } else
                    return null;
            }
        };     
        return viewer;
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
        IAttribute[] attributesSelected;
        try {
            attributesSelected = selectAttributeByDialog(testPolicyCmptTypeParam,
                    Messages.TestCaseTypeSection_Dialog_SelectAttributeAdd_Message, true);
        } catch (CoreException e1) {
            throw new RuntimeException(e1);
        }
        if (attributesSelected == null)
            return;
        
        ITestAttribute testAttribute = null;
        for (int i = 0; i < attributesSelected.length; i++) {
            IAttribute modelAttribute = attributesSelected[i];
            try {
                if (testPolicyCmptTypeParam.isCombinedParameter()){
                    // if the type of the parent is combined 
                    //   create a new expected if attribute is derived or computed
                    if (modelAttribute.isDerivedOrComputed())
                        testAttribute = testPolicyCmptTypeParam.newExpectedResultTestAttribute();
                    else
                        testAttribute = testPolicyCmptTypeParam.newInputTestAttribute();
                } else if (testPolicyCmptTypeParam.isExpextedResultParameter()){
                    testAttribute = testPolicyCmptTypeParam.newExpectedResultTestAttribute();
                } else {
                    // default is input
                    testAttribute = testPolicyCmptTypeParam.newInputTestAttribute();
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
            
            testAttribute.setAttribute(modelAttribute.getName());
            testAttribute.setName(testCaseType.generateUniqueNameForTestAttribute(testAttribute, modelAttribute.getName()));
        }

        redrawDetailArea(testPolicyCmptTypeParam, testAttribute);
    }

    /**
     * Add attribute to test policy cmpt type param
     */
    private void removeAttributeClicked(IIpsObjectPart object) {
        ArgumentCheck.isInstanceOf(object, ITestAttribute.class);
        
        ITestParameter param = (ITestParameter) object.getParent();

        for (Iterator iter = getSelectedAttributes(object).iterator(); iter.hasNext();) {
            ((ITestAttribute)iter.next()).delete();
        }
        
        redrawDetailArea((ITestPolicyCmptTypeParameter) param, (ITestPolicyCmptTypeParameter) param);
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
        objectCache.putFirstEditFieldInSection(testParam, editFieldName);
        
        label = toolkit.createFormLabel(editFieldsComposite, Messages.TestCaseTypeSection_EditFieldLabel_TestParameterType);
        EditField editFieldType = new EnumValueField(toolkit.createCombo(editFieldsComposite, TestParameterType
                .getEnumType()), TestParameterType.getEnumType());
        addSectionSelectionListeners(editFieldType, label, testParam);

        uiController.add(editFieldName, ITestParameter.PROPERTY_NAME);
        uiController.add(editFieldType, ITestParameter.PROPERTY_TEST_PARAMETER_TYPE);
    }

    /*
     * Adds a listener to mark the section as selected if the given edit field gets the focus
     */
    private void addSectionSelectionListeners(EditField editField, Control label, final IIpsObjectPart object) {
        if (editField != null){
            editField.getControl().addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    Section section = objectCache.getSection(object);
                    if (section != null){
                        currSelectedDetailObject = objectCache.getObject(object);
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
        for (Iterator iter = objectCache.getAllSectionButtons().iterator(); iter.hasNext();) {
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
        for (Iterator iter = objectCache.getAllSections().iterator(); iter.hasNext();) {
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
        Label label = toolkit.createFormLabel(editFieldsComposite,
                Messages.TestCaseTypeSection_EditFieldLabel_RequiresProduct);
        EditField editFieldReqProd = new CheckboxField(toolkit.createCheckbox(editFieldsComposite));
        addSectionSelectionListeners(editFieldReqProd, label, parameter);

        // min and max instances only for child parameters
        label = toolkit.createFormLabel(editFieldsComposite,
                Messages.TestCaseTypeSection_EditFieldLabel_MinInstances);
        EditField editFieldMin = new CardinalityField(toolkit.createText(editFieldsComposite));
        editFieldMin.setText("" + parameter.getMinInstances()); //$NON-NLS-1$
        addSectionSelectionListeners(editFieldMin, label, parameter);

        label = toolkit.createFormLabel(editFieldsComposite,
                Messages.TestCaseTypeSection_EditFieldLabel_MaxInstances);
        EditField editFieldMax = new CardinalityField(toolkit.createText(editFieldsComposite));
        editFieldMax.setText("" + parameter.getMaxInstances()); //$NON-NLS-1$
        addSectionSelectionListeners(editFieldMax, label, parameter);

        // connect to model
        uiController.add(editFieldReqProd, ITestPolicyCmptTypeParameter.PROPERTY_REQUIRES_PRODUCTCMT);
        uiController.add(editFieldMin, ITestPolicyCmptTypeParameter.PROPERTY_MIN_INSTANCES);
        uiController.add(editFieldMax, ITestPolicyCmptTypeParameter.PROPERTY_MAX_INSTANCES);
        
        // disable min and max for root parameter
        if (parameter.isRoot()){
            editFieldMin.getControl().setEnabled(false);
            editFieldMax.getControl().setEnabled(false);
        }
    }

    /**
     * Clears the detail area.
     */
    public void clearDetailArea() {
        if (detailsArea != null)
            detailsArea.dispose();

        currSelectedDetailObject = null;
        objectCache.clear();
        
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
        objectCache.putSection(object, section);
        objectCache.putSectionObjects(object);
        
        section.getChildren()[0].addMouseListener(new SectionSelectMouseListener(section, object));
        section.addMouseListener(new SectionSelectMouseListener(section, object));
        section.addExpansionListener(new SectionExpandListern(section, object));
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
        EditField editFieldName = objectCache.getSectionFirstEditField(selected);
        if (editFieldName == null)
            return;
        editFieldName.getControl().setFocus();
    }
    
    /*
     * Adds the button listener for the tree area.
     */
    private void hookButtonListeners() {
        addChildButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    addChildParameterClicked();
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
        addRootParameterButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    addRootParameterClicked();
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
     * Add root parameter clicked.
     */
    private void addRootParameterClicked() {
        // open wizard to add a new root tes parameter
        Memento memento = testCaseType.newMemento();
        NewRootParameterWizard wizard = new NewRootParameterWizard(testCaseType);
        WizardDialog dialog = new WizardDialog(getShell(), wizard);
        dialog.open();
        if (dialog.getReturnCode() == Window.CANCEL) {
            testCaseType.setState(memento);
            return;
        }
        refreshTreeAndDetails(wizard.getNewCreatedTestParameter()); 
    }
    
    /*
     * Add button was clicked.
     */
    private void addChildParameterClicked() {
        // open wizard to add a new child test parameter
        ITestParameter testParamSelected = getSelectedTestParameterInTree();
        if (!(testParamSelected instanceof ITestPolicyCmptTypeParameter))
            return;

        Memento memento = testCaseType.newMemento();
        NewChildParameterWizard wizard = new NewChildParameterWizard(testCaseType,
                (ITestPolicyCmptTypeParameter)testParamSelected);
        WizardDialog dialog = new WizardDialog(getShell(), wizard);
        dialog.open();
        if (dialog.getReturnCode() == Window.CANCEL) {
            testCaseType.setState(memento);
            return;
        }
        refreshTreeAndDetails(wizard.getNewCreatedTestParameter());
    }

    /*
     * Remove button was clicked.
     */
    private void removeClicked() {
        ITestParameter testParameter = getSelectedTestParameterInTree();
        if (testParameter != null)
            testParameter.delete();
        
        currSelectedDetailObject = (IIpsObjectPart) (testParameter.isRoot()?null:testParameter.getParent());
        if (currSelectedDetailObject == null){
            // try to obtain the previous tree item and if extist use this item as new sel object after delete
            TreeItem[] selection = treeViewer.getTree().getSelection();
            TreeItem[] childs = treeViewer.getTree().getItems();
            TreeItem prevTreeItem = null;
            for (int i = 0; i < childs.length; i++) {
                if (childs[i].equals(selection[0]))
                    break;
                prevTreeItem = childs[i];
            }
            if (prevTreeItem != null)
                currSelectedDetailObject = (ITestParameter) prevTreeItem.getData();
        }
        refreshTreeAndDetails(getRootSectionObject(currSelectedDetailObject));
        
        // redraw details for the new selected object
        currSelectedDetailObject = getSelectedTestParameterInTree();
        createDetailsArea((ITestParameter)currSelectedDetailObject);
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
     * Return the selected attributes in the attribute table
     */
    private Set getSelectedAttributes(IIpsObjectPart object){
        Set testAttributesSelected = new HashSet(1);
        testAttributesSelected.add((ITestAttribute) object);
        
        ITestParameter param = (ITestParameter) object.getParent();
        TableViewer attributeTable = (TableViewer) objectCache.getAttributeTable(param);
        if (attributeTable != null){
            ISelection selection = attributeTable.getSelection();
            if (selection instanceof IStructuredSelection){
                for (Iterator iter = ((IStructuredSelection)selection).iterator(); iter.hasNext();) {
                    Object element = iter.next();
                    if (element instanceof ITestAttribute){
                        testAttributesSelected.add(element);
                    }
                }
            }
        }
        return testAttributesSelected;
    }
    
    /*
     * Moves the give test attribute up or down
     */
    private void moveTestAttribute(ITestAttribute testAttribute, boolean up) {
        Set selectedAttributes = getSelectedAttributes(testAttribute);
        int[] selectedAttributesIndexes = new int[selectedAttributes.size()];
        int i = 0;
        for (Iterator iter = selectedAttributes.iterator(); iter.hasNext();) {
            ITestAttribute element = (ITestAttribute)iter.next();
            Integer testAttributeIdx = objectCache.getIdxFromAttribute(element);
            if (testAttributeIdx == null)
                throw new RuntimeException(Messages.TestCaseTypeSection_Error_WrongTestAttributeIndex);
            selectedAttributesIndexes[i++] = testAttributeIdx.intValue();
        }

        ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter = (ITestPolicyCmptTypeParameter)testAttribute
                .getParent();
        int[] movedAttributesIndexes = testPolicyCmptTypeParameter.moveTestAttributes(selectedAttributesIndexes, up);
        redrawDetailArea(testPolicyCmptTypeParameter, testAttribute);
        
        TableViewer attrTable = objectCache.getAttributeTable(testPolicyCmptTypeParameter);
        List newAttributeIdx = new ArrayList(selectedAttributes.size());
        if (attrTable != null){
            for (int j = 0; j < movedAttributesIndexes.length; j++) {
                ITestAttribute movedAttribute = objectCache.getAttributeByIndex(new Integer(movedAttributesIndexes[j]));
                if (movedAttribute != null)
                    newAttributeIdx.add(movedAttribute);
            }
            attrTable.setSelection(new StructuredSelection(newAttributeIdx));
        }
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
            addChildButton.setEnabled(false);
        } else if (parameter instanceof ITestPolicyCmptTypeParameter){
            removeButton.setEnabled(true);
            addChildButton.setEnabled(true);
        } else{
            removeButton.setEnabled(false);
            addChildButton.setEnabled(false);
        }
    }

    /*
     * Refreshs the tree and details
     */
    private void refreshTreeAndDetails(ITestParameter param) {
        refreshTree();
        createDetailsArea(param);
        currSelectedDetailObject = param;
        selectSection(objectCache.getSection(param));
        selectFirstEditFieldInSection(param);
    }
    
    /*
     * Refresh the attribute table
     */
    private void refreshAttributeTable() {
        for (Iterator iter = objectCache.getAllAttributeTable().iterator(); iter.hasNext();) {
            TableViewer table = (TableViewer)iter.next();
            table.refresh();
        }
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
        for (Iterator iter = objectCache.getAllSectionKeys().iterator(); iter.hasNext();) {
            Integer key = (Integer) iter.next();
            Section section = objectCache.getSectionByKey(key);
            if (section.isDisposed())
                continue;
            section.setText(labelProvider.getText(objectCache.getObjectByKey(key)));
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
        List expandedSectionKeys = new ArrayList();
        for (Iterator iter = objectCache.getAllSectionKeys().iterator(); iter.hasNext();) {
            Integer key = (Integer) iter.next();
            if (objectCache.getSectionByKey(key).isExpanded())
                expandedSectionKeys.add(key);
        }
        
        try {
            setFormRedraw(false);
            refreshTree();
            prevSelectedTestParam = null;
            createDetailsArea(testPolicyCmptTypeParam);
            currSelectedDetailObject = selectedObject;
            selectSection(objectCache.getSection(selectedObject));

            if (selectedObject instanceof ITestAttribute){
                TableViewer attrTable = objectCache.getAttributeTable(testPolicyCmptTypeParam);
                if (attrTable != null)
                    attrTable.setSelection(new StructuredSelection(selectedObject));
            }
            // restore the expanded section states
            for (Iterator iter = expandedSectionKeys.iterator(); iter.hasNext();) {
                Section section = objectCache.getSectionByKey((Integer)iter.next());
                if (section == null)
                    return;
                section.setExpanded(true);
            }
        } finally {
            setFormRedraw(true);
        }
    }
    
    /*
     * Refresh the ui
     */
    private void postRefreshAll(){
        postAsyncRunnable(new Runnable() {
            public void run() {
                if (isDisposed())
                    return;
                try{
                    setFormRedraw(false);
                    refreshTree();
                    refreshAttributeTable();
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
        });        
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
     * Sets the redraw state of the form
     */
    private void setFormRedraw(boolean redraw){
        if (!form.isDisposed())
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
     * Displays a dialog to select attributes of a policy component.
     */
    private IAttribute[] selectAttributeByDialog(ITestPolicyCmptTypeParameter testPolicyCmptTypeParam,
            String message,
            boolean multi) throws CoreException {
        ElementListSelectionDialog selectDialog = new ElementListSelectionDialog(getShell(), new DefaultLabelProvider());
        selectDialog.setMultipleSelection(multi);
        selectDialog.setTitle(Messages.TestCaseTypeSection_Dialog_SelectAttribute_Title);
        selectDialog.setMessage(message);

        IPolicyCmptType policyCmptType = testPolicyCmptTypeParam.findPolicyCmptType();
        if (policyCmptType==null){
            String msg = NLS.bind(Messages.TestCaseTypeSection_ErrorDialog_AttributeChangingNotAllowedBecausePolicyCmptTypeNotExists, testPolicyCmptTypeParam.getPolicyCmptType());
            MessageDialog.openWarning(getShell(),
                    Messages.TestCaseTypeSection_ErrorDialog_AttributeChangingTitle, msg);
            return null;
        }
        
        ITypeHierarchy superTypeHierarchy = policyCmptType.getSupertypeHierarchy();
        IAttribute[] attributes = superTypeHierarchy.getAllAttributes(policyCmptType);
        List attributesInDialog = new ArrayList(attributes.length);
        // remove product relevant attributes
        for (int i = 0; i < attributes.length; i++) {
            if (!attributes[i].isProductRelevant())
                attributesInDialog.add(attributes[i]);
        }
        selectDialog.setElements(attributesInDialog.toArray(new IAttribute[0]));
        if (selectDialog.open() == Window.OK) {
            if (selectDialog.getResult().length > 0) {
                Object[] result = (Object[])selectDialog.getResult();
                IAttribute[] attributeResult = new IAttribute[result.length];
                System.arraycopy(result, 0, attributeResult, 0, result.length);
                return attributeResult;
            }
        }
        return null;
    }
}
