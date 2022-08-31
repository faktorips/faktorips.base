// CSOFF: FileLengthCheck
// CSOFF: RegexpHeaderCheck
// CSOFF: InnerTypeLastCheck
// TODO This class is a huge ugly moloch and needs to be completely rewritten scratch.
/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcasetype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.MessageCueLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.CollapseAllAction;
import org.faktorips.devtools.core.ui.actions.ExpandAllAction;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.UIController;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.EnumField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.editors.DescriptionEditComposite;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.editors.TreeMessageHoverService;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.core.ui.views.IpsProblemOverlayIcon;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestParameter;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.testcasetype.ITestRuleParameter;
import org.faktorips.devtools.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.model.testcasetype.TestParameterType;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.memento.Memento;

/**
 * Section to display and edit the test case type.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTypeSection extends IpsSection {

    private ResourceManager resourceManager;

    /** The editing test case type */
    private ITestCaseType testCaseType;

    /** The label provider for the test case type */
    private TestCaseTypeLabelProvider labelProvider;

    /** UI toolkit for creating the controls */
    private UIToolkit toolkit;

    /**
     * The treeview which displays all test policy cmpt type params which are available in this test
     */
    private TreeViewer treeViewer;

    /** Title of the test case type tree structure section */
    private String sectionTreeStructureTitle;

    /** Title of the test case type details section */
    private String sectionDetailsTitle;

    // Buttons
    // belongs to tree structure
    private Button addParameterButton;
    private Button removeButton;
    private Button moveUpButton;
    private Button moveDownButton;

    /** The form which contains this section */
    private ScrolledForm form;

    // The detail area will be used to render the dynamically created detail controls
    private Composite parentOfdetailsArea;
    private Composite detailsArea;

    /** The previous selected parameter */
    private ITestParameter prevSelectedTestParam;

    private boolean showAll = false;

    /** Indicates that the tree is refreshing */
    private boolean isTreeRefreshing = false;

    /** Contains the currently selected object in the detail area */
    private IIpsObjectPart currSelectedDetailObject;

    /**
     * Container for the attribute ui controllers used to update the attribute edit fields if the
     * test case type changed, thus if the type of a test policy cmpt type param changed the
     * attribute will show the correct validation result
     */
    private List<UIController> attributeControllers = new ArrayList<>();

    /** Object cache for the detail area */
    private SectionDetailObjectCache objectCache;

    private boolean showSubtypeAttributes;

    private OpenInNewEditorAction openInNewEditorAction;

    private class AttributeDetails {

        private Composite attributesDetails;
        private Text attributesPolicyCmptType;
        private DescriptionEditComposite attributesDescription;
        private ExpandableComposite attributeExpandable;
        private boolean manualTriggered = false;

        /**
         * Creates the attribute detail area
         */
        public void createAttributeDetailArea(Composite parent) {
            attributeExpandable = new ExpandableComposite(parent, SWT.NONE);
            attributeExpandable.setText(Messages.TestCaseTypeSection_labelAttributeDetails);
            GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
            gd.horizontalSpan = 2;
            attributeExpandable.setLayoutData(gd);
            attributeExpandable.setLayout(new GridLayout(1, true));
            attributeExpandable.addExpansionListener(new IExpansionListener() {
                @Override
                public void expansionStateChanged(ExpansionEvent e) {
                    redrawForm();
                    manualTriggered = true;
                }

                @Override
                public void expansionStateChanging(ExpansionEvent e) {
                    // Nothing to do
                }
            });

            attributesDetails = toolkit.createLabelEditColumnComposite(attributeExpandable);
            attributeExpandable.setClient(attributesDetails);

            attributesDetails.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);

            // details of the selected test attribute

            toolkit.createLabel(attributesDetails, Messages.TestCaseTypeSection_labelPolicyCmptType);
            attributesPolicyCmptType = toolkit.createText(attributesDetails);
            toolkit.setEnabled(attributesPolicyCmptType, false);

            toolkit.createLabel(attributesDetails, Messages.TestCaseTypeSection_labelDescription);
            toolkit.createVerticalSpacer(attributesDetails, 1);
            attributesDescription = new DescriptionEditComposite(attributesDetails, null, toolkit);
            gd = (GridData)attributesDescription.getLayoutData();
            gd.horizontalSpan = 2;
        }

        /**
         * Update the attributes deatailed area.
         */
        public void updateDetailAttributeArea(ITestAttribute selectedAttribute) {
            if (!manualTriggered) {
                attributeExpandable.setExpanded(true);
                redrawForm();
            }

            attributesDescription.setDescribedElement(selectedAttribute);

            if (selectedAttribute.isBasedOnModelAttribute()) {
                attributesPolicyCmptType.setText(selectedAttribute.getCorrespondingPolicyCmptType());
            } else {
                attributesPolicyCmptType
                        .setText(Messages.TestCaseTypeSection_infoTextTestAttributeWithoutPolicyCmptTypeAttr);
            }

            attributesDescription.refresh();
        }
    }

    /**
     * Object cache to store several object to render the ui
     */
    private class SectionDetailObjectCache {

        // Containers for all sections and their objects
        private HashMap<Integer, Section> sections = new HashMap<>();
        private HashMap<Integer, IIpsObjectPart> sectionObjects = new HashMap<>();
        private HashMap<Integer, EditField<?>> sectionFirstEditField = new HashMap<>();
        private HashMap<Integer, Integer> attributeIdx = new HashMap<>();
        private HashMap<Integer, SectionButtons> sectionButtons = new HashMap<>();
        private HashMap<Integer, TableViewer> attributeTableViewers = new HashMap<>();
        private HashMap<Integer, AttributeDetails> attributeDetailsMap = new HashMap<>();

        /**
         * Returns the key identifier for the given object
         */
        private Integer getKeyFor(IIpsObjectPart object) {
            return Integer.valueOf(System.identityHashCode(object));
        }

        /**
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
            attributeDetailsMap.clear();
        }

        public void putAttributeIdx(ITestAttribute attribute, int idx) {
            attributeIdx.put(getKeyFor(attribute), Integer.valueOf(idx));
            sectionObjects.put(getKeyFor(attribute), attribute);
        }

        public void putSectionButtons(ITestParameter testParam, SectionButtons currSectionButtons) {
            sectionButtons.put(getKeyFor(testParam), currSectionButtons);
        }

        public void putFirstEditFieldInSection(ITestParameter testParam, EditField<?> editFieldName) {
            sectionFirstEditField.put(getKeyFor(testParam), editFieldName);
        }

        public void putSection(IIpsObjectPart object, Section section) {
            sections.put(getKeyFor(object), section);
        }

        public void putSectionObjects(IIpsObjectPart object) {
            sectionObjects.put(getKeyFor(object), object);
        }

        public Section getSection(IIpsObjectPart mainSectionObject) {
            var part = mainSectionObject;
            if (part instanceof ITestAttribute) {
                part = (IIpsObjectPart)((ITestAttribute)part).getParent();
            }
            return sections.get(getKeyFor(part));
        }

        public IIpsObjectPart getObject(IIpsObjectPart object) {
            return sectionObjects.get(getKeyFor(object));
        }

        public Collection<Section> getAllSections() {
            return sections.values();
        }

        public EditField<?> getSectionFirstEditField(Object selected) {
            return sectionFirstEditField.get(getKeyFor((IIpsObjectPart)selected));
        }

        public Integer getIdxFromAttribute(ITestAttribute testAttribute) {
            return attributeIdx.get(getKeyFor(testAttribute));
        }

        public ITestAttribute getAttributeByIndex(Integer idx) {
            for (Integer key : attributeIdx.keySet()) {
                if (idx.equals(attributeIdx.get(key))) {
                    return (ITestAttribute)getObjectByKey(key);
                }
            }
            return null;
        }

        public Collection<Integer> getAllSectionKeys() {
            return sections.keySet();
        }

        public Section getSectionByKey(Integer key) {
            return sections.get(key);
        }

        public Object getObjectByKey(Integer key) {
            return sectionObjects.get(key);
        }

        public Collection<SectionButtons> getAllSectionButtons() {
            return sectionButtons.values();
        }

        public void putAttributeTable(ITestParameter testParam, TableViewer attributeTableViewer) {
            attributeTableViewers.put(getKeyFor(testParam), attributeTableViewer);
        }

        public TableViewer getAttributeTable(ITestParameter testParam) {
            return attributeTableViewers.get(getKeyFor(testParam));
        }

        public Collection<TableViewer> getAllAttributeTable() {
            return attributeTableViewers.values();
        }

        public void putAttributeDetails(ITestParameter testParam, AttributeDetails attributeDetails) {
            attributeDetailsMap.put(getKeyFor(testParam), attributeDetails);
        }

        public AttributeDetails getAttributeDetails(ITestParameter testParam) {
            return attributeDetailsMap.get(getKeyFor(testParam));
        }

        public Collection<AttributeDetails> getAllAttributeDetails() {
            return attributeDetailsMap.values();
        }
    }

    /**
     * State class contains the enable state of all actions (for buttons and context menu)
     */
    private static class TreeActionEnableState {
        private boolean addEnable = false;
        private boolean removeEnable = false;
        private boolean upEnable = false;
        private boolean downEnable = false;

        public boolean isAddEnable() {
            return addEnable;
        }

        public void setAddEnable(boolean addEnable) {
            this.addEnable = addEnable;
        }

        public boolean isRemoveEnable() {
            return removeEnable;
        }

        public void setRemoveEnable(boolean removeEnable) {
            this.removeEnable = removeEnable;
        }

        public boolean isUpEnable() {
            return upEnable;
        }

        public void setUpEnable(boolean upEnable) {
            this.upEnable = upEnable;
        }

        public boolean isDownEnable() {
            return downEnable;
        }

        public void setDownEnable(boolean downEnable) {
            this.downEnable = downEnable;
        }
    }

    /**
     * Class used to store button objects for each section
     */
    private class SectionButtons {
        private Composite buttons;
        private Button addAtributeButton;
        private Button removeAttributeButton;
        private Button moveAttributeUp;
        private Button moveAttributeDown;
        private TableViewer attributeTableViewer;
        private ITestPolicyCmptTypeParameter testParameter;

        /**
         * Helper class to handle common button functionality
         */
        private class ButtonSelectionListener extends SelectionAdapter {

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    ISelection selection = attributeTableViewer.getSelection();
                    if (selection instanceof IStructuredSelection) {
                        IIpsObjectPart selectedDetailObject = (IIpsObjectPart)((IStructuredSelection)selection)
                                .getFirstElement();
                        if (selectedDetailObject != null) {
                            currSelectedDetailObject = selectedDetailObject;
                        }
                        clickedEvent(e);
                    }
                    // CSOFF: IllegalCatch
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
                // CSON: IllegalCatch
            }

            /**
             * @param e The selection event
             */
            protected void clickedEvent(SelectionEvent e) {
                // Empty default implementation
            }

        }

        public void createButtons(Composite structureComposite,
                ITestPolicyCmptTypeParameter testParameter,
                TableViewer attributeTableViewer) {

            this.testParameter = testParameter;
            this.attributeTableViewer = attributeTableViewer;

            buttons = toolkit.getFormToolkit().createComposite(structureComposite);
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

            removeAttributeButton.setEnabled(false);
            moveAttributeUp.setEnabled(false);
            moveAttributeDown.setEnabled(false);

            hookButtonListenersDetail();
        }

        public void updateDetailButtonStatus(IIpsObjectPart mainSectionObject,
                IIpsObjectPart currSelectedDetailObject) {
            if (!isDataChangeable()) {
                toolkit.setDataChangeable(addAtributeButton, false);
                toolkit.setDataChangeable(removeAttributeButton, false);
                toolkit.setDataChangeable(moveAttributeUp, false);
                toolkit.setDataChangeable(moveAttributeDown, false);
                return;
            }

            if (testParameter != mainSectionObject) {
                removeAttributeButton.setEnabled(false);
                moveAttributeUp.setEnabled(false);
                moveAttributeDown.setEnabled(false);
                return;
            }

            if (currSelectedDetailObject instanceof ITestPolicyCmptTypeParameter) {
                addAtributeButton.setEnabled(true);
                removeAttributeButton.setEnabled(false);
                moveAttributeUp.setEnabled(false);
                moveAttributeDown.setEnabled(false);
            } else if (currSelectedDetailObject instanceof ITestAttribute) {
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
            addAtributeButton.addSelectionListener(new ButtonSelectionListener() {
                @Override
                protected void clickedEvent(SelectionEvent e) {
                    addAttributeClicked(testParameter);
                }
            });
            removeAttributeButton.addSelectionListener(new ButtonSelectionListener() {
                @Override
                protected void clickedEvent(SelectionEvent e) {
                    removeAttributeClicked(currSelectedDetailObject);
                }
            });
            moveAttributeUp.addSelectionListener(new ButtonSelectionListener() {
                @Override
                protected void clickedEvent(SelectionEvent e) {
                    moveUpAttributeClicked(currSelectedDetailObject);
                }
            });
            moveAttributeDown.addSelectionListener(new ButtonSelectionListener() {
                @Override
                protected void clickedEvent(SelectionEvent e) {
                    moveDownAttributeClicked(currSelectedDetailObject);
                }
            });
        }
    }

    /**
     * Mouse listener class to select the section if the mouse button is clicked
     */
    private class SectionSelectMouseListener implements MouseListener {

        private Section section;
        private IIpsObjectPart object;

        public SectionSelectMouseListener(Section section, IIpsObjectPart object) {
            this.section = section;
            this.object = object;
        }

        @Override
        public void mouseDown(MouseEvent e) {
            currSelectedDetailObject = object;
            if (section == null) {
                Section sectionFromCache = objectCache.getSection(object);
                if (sectionFromCache != null) {
                    selectSection(sectionFromCache);
                }
            } else {
                selectSection(section);
            }
        }

        @Override
        public void mouseDoubleClick(MouseEvent e) {
            // nothing to do
        }

        @Override
        public void mouseUp(MouseEvent e) {
            // nothing to do
        }

    }

    /**
     * Cell Modifier for the test attribute table
     */
    private class TestAttributeCellModifier implements ICellModifier {

        private TableViewer viewer;

        public TestAttributeCellModifier(TableViewer viewer) {
            this.viewer = viewer;
        }

        @Override
        public boolean canModify(Object element, String property) {
            ITestAttribute testAttributeFromObject = getTestAttributeFromObject(element);
            if (property.equals(ITestAttribute.PROPERTY_DATATYPE)) {
                if (!IpsStringUtils.isEmpty(testAttributeFromObject.getAttribute())) {
                    return false;
                }

            }
            return isDataChangeable();
        }

        @Override
        public Object getValue(Object element, String property) {
            if (property.equals(ITestAttribute.PROPERTY_TEST_ATTRIBUTE_TYPE)) {
                return getTestAttributeFromObject(element).getTestAttributeType().getIndex();
            } else if (property.equals(IIpsElement.PROPERTY_NAME)) {
                return getTestAttributeFromObject(element).getName();
            } else if (property.equals(ITestAttribute.PROPERTY_DATATYPE)) {
                return getTestAttributeFromObject(element).getDatatype();
            }
            return null;
        }

        @Override
        public void modify(Object element, String property, Object value) {
            TableItem tableItem = (TableItem)element;
            if (property.equals(ITestAttribute.PROPERTY_TEST_ATTRIBUTE_TYPE)) {
                TestParameterType selectedType = TestParameterType.getTestParameterType((Integer)value);
                if (selectedType == null) {
                    return;
                }
                getTestAttributeFromObject(tableItem.getData()).setTestAttributeType(selectedType);
                tableItem.setText(1, selectedType.getName());
            } else if (property.equals(IIpsElement.PROPERTY_NAME)) {
                getTestAttributeFromObject(tableItem.getData()).setName((String)value);
                tableItem.setText(0, (String)value);
            } else if (property.equals(ITestAttribute.PROPERTY_DATATYPE)) {
                getTestAttributeFromObject(tableItem.getData()).setDatatype((String)value);
                tableItem.setText(3, (String)value);
            }
            repackAttributeTable(viewer);
        }

        private ITestAttribute getTestAttributeFromObject(Object obj) {
            ArgumentCheck.isInstanceOf(obj, ITestAttribute.class);
            return (ITestAttribute)obj;
        }

    }

    /**
     * Label provider for the attribute table
     */
    private class TestAttributeTblLabelProvider extends DefaultLabelProvider implements ITableLabelProvider {

        private IIpsProject ipsProject;

        public TestAttributeTblLabelProvider() {
            ipsProject = testCaseType.getIpsProject();
        }

        @Override
        public void dispose() {
            super.dispose();
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            Image baseImage;
            try {
                ITestAttribute testAttribute = ((ITestAttribute)element);
                MessageList msgList = testAttribute.validate(ipsProject);
                switch (columnIndex) {
                    case 0:
                        // test attribute name (used in test case)
                        msgList = msgList.getMessagesFor(element, IIpsElement.PROPERTY_NAME);
                        if (!msgList.isEmpty()) {
                            return (Image)resourceManager.get(IpsProblemOverlayIcon.getOverlay(msgList.getSeverity()));
                        }
                        return null;
                    case 1:
                        // type input or expected
                        if (testAttribute.getTestAttributeType() == TestParameterType.EXPECTED_RESULT) {
                            baseImage = (Image)resourceManager
                                    .get(IpsUIPlugin.getImageHandling().createImageDescriptor("TestCaseExpResult.gif")); //$NON-NLS-1$
                        } else {
                            baseImage = (Image)resourceManager
                                    .get(IpsUIPlugin.getImageHandling().createImageDescriptor("TestCaseInput.gif")); //$NON-NLS-1$
                        }
                        msgList = msgList.getMessagesFor(element, ITestAttribute.PROPERTY_TEST_ATTRIBUTE_TYPE);
                        break;
                    case 2:
                        // attribute
                        if (IpsStringUtils.isEmpty(testAttribute.getAttribute())) {
                            return null;
                        }
                        baseImage = getImageForAttribute(element);
                        msgList = msgList.getMessagesFor(element);
                        break;
                    case 3:
                        // datatype
                        baseImage = (Image)resourceManager
                                .get(IIpsDecorators.getImageHandling().createImageDescriptor("Datatype.gif")); //$NON-NLS-1$
                        msgList = msgList.getMessagesFor(element, ITestAttribute.PROPERTY_DATATYPE);
                        break;
                    default:
                        return null;
                }
                return (Image)resourceManager
                        .get(IpsProblemOverlayIcon.createOverlayIcon(baseImage, msgList.getSeverity()));
            } catch (IpsException e) {
                IpsPlugin.logAndShowErrorDialog(e);
                return null;
            }
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof ITestAttribute) {
                ITestAttribute testAttribute = (ITestAttribute)element;
                switch (columnIndex) {
                    case 0:
                        // test attribute name (used in test case)
                        return testAttribute.getName();
                    case 1:
                        // // type input or expected
                        return testAttribute.getTestAttributeType().getName();
                    case 2:
                        // attribute
                        IPolicyCmptTypeAttribute attr = testAttribute.findAttribute(ipsProject);
                        if (attr != null) {
                            return (attr.isDerived() ? "/" : "") + attr.getName(); //$NON-NLS-1$ //$NON-NLS-2$
                        } else {
                            return testAttribute.getAttribute();
                        }
                    case 3:
                        // datatype
                        ValueDatatype datatype;
                        try {
                            datatype = testAttribute.findDatatype(ipsProject);
                            if (datatype != null) {
                                return super.getText(datatype);
                            } else {
                                return testAttribute.getDatatype();
                            }
                        } catch (IpsException e) {
                            // ignore exception, display datatype name stored in test parameter
                            // instead
                            return testAttribute.getDatatype();
                        }
                    default:
                        break;
                }
            }
            return null;
        }

        private Image getImageForAttribute(Object element) {
            return IpsUIPlugin.getImageHandling().getImage((ITestAttribute)element);
        }
    }

    /**
     * Expansion section listener class to select the section if expand or collapsed
     */
    private class SectionExpandListern implements IExpansionListener {

        private Section section;
        private IIpsObjectPart object;

        public SectionExpandListern(Section section, IIpsObjectPart object) {
            this.section = section;
            this.object = object;
        }

        @Override
        public void expansionStateChanged(ExpansionEvent e) {
            currSelectedDetailObject = object;
            selectSection(section);
            final ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter;
            if (object instanceof ITestAttribute) {
                testPolicyCmptTypeParameter = (ITestPolicyCmptTypeParameter)((ITestAttribute)object).getParent();
            } else if (object instanceof ITestPolicyCmptTypeParameter) {
                testPolicyCmptTypeParameter = (ITestPolicyCmptTypeParameter)object;
            } else {
                return;
            }

            try {
                if (!object.isValid(object.getIpsProject()) && !e.getState()) {
                    postAsyncRunnable(() -> {
                        if (isDisposed()) {
                            return;
                        }
                        redrawDetailArea(testPolicyCmptTypeParameter, object);
                    });
                }
                // CSOFF: IllegalCatch
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            // CSON: IllegalCatch
        }

        @Override
        public void expansionStateChanging(ExpansionEvent e) {
            // nothing to do
        }
    }

    private abstract static class ErrorLoggingAction extends IpsAction {
        public ErrorLoggingAction(ISelectionProvider selectionProvider) {
            super(selectionProvider);
        }

        @Override
        public void run(IStructuredSelection selection) {
            try {
                runWith(selection);
                // CSOFF: IllegalCatch
            } catch (Exception e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
            // CSON: IllegalCatch
        }

        protected abstract void runWith(IStructuredSelection selection);
    }

    /**
     * Action to add an element
     */
    private class AddAction extends ErrorLoggingAction {

        public AddAction() {
            super(treeViewer);
            setText(Messages.TestCaseTypeSection_Button_NewRootParameter);
        }

        @Override
        protected boolean computeEnabledProperty(IStructuredSelection selection) {
            TreeActionEnableState actionEnableState = evaluateTreeActionEnableState(selection.getFirstElement());
            return actionEnableState.isAddEnable();
        }

        @Override
        public void runWith(IStructuredSelection selection) {
            addParameterClicked();
        }
    }

    /**
     * Action to remove an element
     */
    private class RemoveAction extends ErrorLoggingAction {

        public RemoveAction() {
            super(treeViewer);
            setText(Messages.TestCaseTypeSection_Button_Remove);
        }

        @Override
        protected boolean computeEnabledProperty(IStructuredSelection selection) {
            TreeActionEnableState actionEnableState = evaluateTreeActionEnableState(selection.getFirstElement());
            return actionEnableState.isRemoveEnable();
        }

        @Override
        public void runWith(IStructuredSelection selection) {
            removeClicked();
        }
    }

    /**
     * Action to move up an element
     */
    private class MoveUpAction extends ErrorLoggingAction {

        public MoveUpAction() {
            super(treeViewer);
            setText(Messages.TestCaseTypeSection_Button_Up);
        }

        @Override
        protected boolean computeEnabledProperty(IStructuredSelection selection) {
            TreeActionEnableState actionEnableState = evaluateTreeActionEnableState(selection.getFirstElement());
            return actionEnableState.isUpEnable();
        }

        @Override
        public void runWith(IStructuredSelection selection) {
            moveUpClicked(selection);
        }
    }

    /**
     * Action to move down an element
     */
    private class MoveDownAction extends ErrorLoggingAction {

        public MoveDownAction() {
            super(treeViewer);
            setText(Messages.TestCaseTypeSection_Button_Down);
        }

        @Override
        protected boolean computeEnabledProperty(IStructuredSelection selection) {
            TreeActionEnableState actionEnableState = evaluateTreeActionEnableState(selection.getFirstElement());
            return actionEnableState.isDownEnable();
        }

        @Override
        public void runWith(IStructuredSelection selection) {
            moveDownClicked(selection);
        }
    }

    private class OpenInNewEditorAction extends IpsAction {

        public OpenInNewEditorAction() {
            super(treeViewer);
            setText(""); //$NON-NLS-1$
        }

        @Override
        public void run(IStructuredSelection selection) {
            Object firstElement = selection.getFirstElement();
            if (firstElement instanceof ITestPolicyCmptTypeParameter) {
                ITestPolicyCmptTypeParameter param = (ITestPolicyCmptTypeParameter)firstElement;
                if (IpsStringUtils.isNotEmpty(param.getPolicyCmptType())) {
                    IPolicyCmptType cmptType = param.findPolicyCmptType(param.getIpsProject());
                    IpsUIPlugin.getDefault().openEditor(cmptType);
                }
            }
        }
    }

    public TestCaseTypeSection(Composite parent, UIToolkit toolkit, final ITestCaseType testCaseType,
            final String title, String detailTitle, ScrolledForm form) {

        super(parent, ExpandableComposite.NO_TITLE, GridData.FILL_BOTH, toolkit);

        resourceManager = new LocalResourceManager(JFaceResources.getResources());
        addDisposeListener($ -> resourceManager.dispose());

        objectCache = new SectionDetailObjectCache();

        this.testCaseType = testCaseType;
        sectionTreeStructureTitle = title;
        sectionDetailsTitle = detailTitle;
        this.form = form;

        initControls();
        setText(title);
    }

    @Override
    protected void performRefresh() {
        postRefreshAll();
    }

    /**
     * Post the given runnable to the asynchronous executable list of the display
     */
    private void postAsyncRunnable(Runnable r) {
        if (!isDisposed()) {
            getDisplay().asyncExec(r);
        }
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        this.toolkit = toolkit;

        SashForm sashForm = new SashForm(client, SWT.NULL);
        toolkit.getFormToolkit().adapt(sashForm, false, false);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Tree structure section
        Section structureSection = toolkit.getFormToolkit().createSection(sashForm, ExpandableComposite.TITLE_BAR);
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
        MessageCueLabelProvider msgCueLabelProvider = new MessageCueLabelProvider(labelProvider,
                testCaseType.getIpsProject());
        treeViewer.setLabelProvider(msgCueLabelProvider);
        TestCaseTypeContentProvider contentProvider = new TestCaseTypeContentProvider(testCaseType);
        treeViewer.setContentProvider(contentProvider);
        treeViewer.setInput(testCaseType);
        treeViewer.expandAll();
        treeViewer.collapseAll();
        hookTreeListeners();
        treeViewer.expandToLevel(2);
        buildContextMenu();
        createToolbar(structureSection);

        // Buttons belongs to the tree structure
        Composite buttons = toolkit.getFormToolkit().createComposite(structureComposite);
        buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        GridLayout buttonLayout = new GridLayout(1, true);
        buttons.setLayout(buttonLayout);

        addParameterButton = toolkit.createButton(buttons, Messages.TestCaseTypeSection_Button_NewRootParameter);
        removeButton = toolkit.createButton(buttons, Messages.TestCaseTypeSection_Button_Remove);
        moveUpButton = toolkit.createButton(buttons, Messages.TestCaseTypeSection_Button_Up);
        moveDownButton = toolkit.createButton(buttons, Messages.TestCaseTypeSection_Button_Down);

        addParameterButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        moveUpButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        moveDownButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        updateTreeButtonStatus(null);
        hookButtonListeners();

        // Details section
        Section detailsSection = toolkit.getFormToolkit().createSection(sashForm, ExpandableComposite.TITLE_BAR);
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

        if (showAll) {
            createDetailsArea(null);
        }

        configureToolBar(detailsSection);

        sashForm.setWeights(new int[] { 50, 50 });
        redrawForm();
    }

    private void createToolbar(Section section) {
        ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
        ToolBar toolbar = toolBarManager.createControl(section);
        toolBarManager.add(new ExpandAllAction(treeViewer));
        toolBarManager.add(new CollapseAllAction(treeViewer));
        toolBarManager.update(true);
        section.setTextClient(toolbar);
    }

    /**
     * Configuration of toolbar
     */
    private void configureToolBar(Section detailsSection) {
        // Toolbar item show all
        Action actionAll = new Action("structureAll", IAction.AS_CHECK_BOX) { //$NON-NLS-1$
            @Override
            public void run() {
                showAllClicked();
            }
        };
        actionAll.setChecked(showAll);
        actionAll.setToolTipText(Messages.TestCaseTypeSection_Action_ShowAll_ToolTip);
        actionAll.setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("TestCase_flatView.gif")); //$NON-NLS-1$
        ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
        ToolBar toolbar = toolBarManager.createControl(detailsSection);
        toolBarManager.add(actionAll);
        toolBarManager.update(true);
        detailsSection.setTextClient(toolbar);
    }

    /**
     * Show all test parameter elements
     */
    private void showAllClicked() {
        showAll = !showAll;
        if (showAll) {
            treeViewer.expandAll();
        }
        createDetailsArea(getRootSectionObject(currSelectedDetailObject));
        setDataChangeable(isDataChangeable());
    }

    /**
     * Creates the test case type details area depending on the given object.
     */
    private void createDetailsArea(ITestParameter testParam) {
        if (isTreeRefreshing) {
            return;
        }

        clearDetailArea();

        if (testParam == null && !showAll) {
            updateTreeButtonStatus(getSelectedObjectInTree());
            prevSelectedTestParam = null;
            return;
        }

        ITestParameter firstTestParam = testParam;
        Section firstSection = null;
        if (showAll) {
            ITestParameter[] testParms = testCaseType.getTestParameters();
            if (testParms != null && testParms.length > 0) {
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

    /**
     * Creates the details all given test parameter
     */
    private void createDetailsForAllTestParams(ITestParameter[] testParams) {
        for (ITestParameter testParam : testParams) {
            createDetailsForTestParam(testParam);
            if (testParam instanceof ITestPolicyCmptTypeParameter) {
                ITestPolicyCmptTypeParameter testPolicyCmptTypeParam = (ITestPolicyCmptTypeParameter)testParam;
                createDetailsForAllTestParams(testPolicyCmptTypeParam.getTestPolicyCmptTypeParamChilds());
            }
        }
    }

    /**
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

        Composite details = createBorderComposite(detailComposite);
        Section section = toolkit.getFormToolkit().createSection(details, 0);
        section.setText(labelProvider.getText(testParam));
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        MessageList msgList = null;
        try {
            msgList = testParam.validate(testParam.getIpsProject());
        } catch (IpsException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return section;
        }

        showErrors(testParam, section, msgList);

        // create separator line
        toolkit.getFormToolkit().createCompositeSeparator(section);
        storeSection(section, testParam);

        BindingContext bindingContext = new BindingContext();

        // create common edit fields
        Composite editFieldsComposite = toolkit.createLabelEditColumnComposite(section);
        section.setClient(editFieldsComposite);

        createTestParamDetails(editFieldsComposite, testParam, bindingContext);

        // create details depending on the test parameter
        if (testParam instanceof ITestPolicyCmptTypeParameter) {
            ITestPolicyCmptTypeParameter testPolicyCmptTypeParam = (ITestPolicyCmptTypeParameter)testParam;

            createTestPolicyCmptTypeParamDetails(editFieldsComposite, testPolicyCmptTypeParam, bindingContext);

            // Spacer between test policy cmpt type param and test attribute
            toolkit.createVerticalSpacer(details, 10).setBackground(details.getBackground());

            createDetailComposite(testParam, structureLayout, details, testPolicyCmptTypeParam);
        } else if (testParam instanceof ITestValueParameter) {
            createTestValueParamDetails(editFieldsComposite, (ITestValueParameter)testParam, bindingContext);
        } else if (testParam instanceof ITestRuleParameter) {
            createTestRuleParamDetails(editFieldsComposite, testParam);
        }

        bindingContext.updateUI();

        return section;
    }

    private void createDetailComposite(ITestParameter testParam,
            GridLayout structureLayout,
            Composite details,
            ITestPolicyCmptTypeParameter testPolicyCmptTypeParam) {
        Composite composite = toolkit.getFormToolkit().createComposite(details);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        if (!isAssociation(testPolicyCmptTypeParam)) {
            GridLayout tableBtnLayout = new GridLayout(2, false);
            tableBtnLayout.horizontalSpacing = 0;
            tableBtnLayout.marginWidth = 0;
            tableBtnLayout.marginHeight = 3;
            composite.setLayout(structureLayout);

            // create the attribute table
            final TableViewer attributeTableViewer = createTestAttributeTable(composite);
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
            currSectionButtons.createButtons(composite, (ITestPolicyCmptTypeParameter)testParam,
                    attributeTableViewer);
            objectCache.putSectionButtons(testParam, currSectionButtons);

            // create detail area of selected attributes
            AttributeDetails attributeDetails = new AttributeDetails();
            attributeDetails.createAttributeDetailArea(composite);
            objectCache.putAttributeDetails(testParam, attributeDetails);
        } else {
            composite.setLayout(new GridLayout(1, true));

            FormText text = toolkit.getFormToolkit().createFormText(composite, false);
            GridData gridData = new GridData(GridData.FILL_BOTH);
            gridData.heightHint = 60;
            gridData.widthHint = 100;
            text.setLayoutData(gridData);
            text.setText(NLS.bind(
                    Messages.TestCaseTypeSection_FormText_InfoAssociation_1
                            + Messages.TestCaseTypeSection_FormText_InfoAssociation_2
                            + Messages.TestCaseTypeSection_FormText_InfoAssociation_3,
                    testPolicyCmptTypeParam.getAssociation()), true, false);
        }
    }

    private void showErrors(ITestParameter testParam, Section section, MessageList msgList) {
        // create error indicator in the description bar
        // show errors for property policy component type and property association,
        // because there are no edit fields which shows these errors
        String errorMessageText = ""; //$NON-NLS-1$
        MessageList msgListPolicyCmptType = msgList.getMessagesFor(testParam,
                ITestPolicyCmptTypeParameter.PROPERTY_POLICYCMPTTYPE);
        MessageList msgListPolicyCmptAssociation = msgList.getMessagesFor(testParam,
                ITestPolicyCmptTypeParameter.PROPERTY_ASSOCIATION);
        if (!msgListPolicyCmptType.isEmpty()) {
            String msgText = msgListPolicyCmptType.getText();
            errorMessageText += errorMessageText.length() > 0 ? "<br/>" : ""; //$NON-NLS-1$ //$NON-NLS-2$
            errorMessageText += "<p><img href=\"imagepccmpttype\"/> <span color=\"red\">" + msgText + "</span></p>"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (!msgListPolicyCmptAssociation.isEmpty()) {
            String msgText = msgListPolicyCmptAssociation.getText();
            errorMessageText += errorMessageText.length() > 0 ? "<br/>" : ""; //$NON-NLS-1$ //$NON-NLS-2$
            errorMessageText += "<p><img href=\"imageassociation\"/> <span color=\"red\">" + msgText + "</span></p>"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (errorMessageText.length() > 0) {
            FormText formText = toolkit.getFormToolkit().createFormText(section, false);
            if (testParam instanceof ITestPolicyCmptTypeParameter) {
                ITestPolicyCmptTypeParameter pcTypeParameter = (ITestPolicyCmptTypeParameter)testParam;
                IPolicyCmptType pcType = pcTypeParameter.findPolicyCmptType(pcTypeParameter.getIpsProject());
                Image baseImage = IpsUIPlugin.getImageHandling().getImage(pcType);
                ImageDescriptor overlayedImage = IpsProblemOverlayIcon.createOverlayIcon(baseImage,
                        msgList.getSeverity());
                formText.setImage("imagepccmpttype", (Image)resourceManager.get(overlayedImage)); //$NON-NLS-1$
            }
            Image baseImage = (Image)resourceManager
                    .get(IpsUIPlugin.getImageHandling().createImageDescriptor("Association.gif")); //$NON-NLS-1$
            ImageDescriptor imageassociationDescriptor = IpsProblemOverlayIcon.createOverlayIcon(baseImage,
                    msgList.getSeverity());
            formText.setImage("imageassociation", (Image)resourceManager.get(imageassociationDescriptor)); //$NON-NLS-1$
            formText.setColor("red", getDisplay().getSystemColor(SWT.COLOR_DARK_RED)); //$NON-NLS-1$
            formText.setText("<form>" + errorMessageText + "</form>", true, false); //$NON-NLS-1$ //$NON-NLS-2$
            section.setDescriptionControl(formText);
            addSectionSelectionListeners(null, formText, testParam);
        }
    }

    @Override
    public void setDataChangeable(boolean changeable) {
        super.setDataChangeable(changeable);
        updateDetailButtonStatus(null);
        Collection<AttributeDetails> allAttributeDetails = objectCache.getAllAttributeDetails();
        for (AttributeDetails details : allAttributeDetails) {
            toolkit.setEnabled(details.attributesPolicyCmptType, false);
        }
    }

    /**
     * Repacks the columns in the test attribute table
     */
    private void repackAttributeTable(TableViewer attributeTableViewer) {
        for (int i = 0, n = attributeTableViewer.getTable().getColumnCount(); i < n; i++) {
            attributeTableViewer.getTable().getColumn(i).pack();
        }
    }

    /**
     * Create the test attribute table
     */
    private TableViewer createTestAttributeTable(Composite details) {
        final Table table = new Table(details, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        TableColumn column = new TableColumn(table, SWT.LEFT);

        column.setText(Messages.TestCaseTypeSection_AttributeTable_ColumnTitleAttributeName);

        column = new TableColumn(table, SWT.LEFT);
        column.setText(Messages.TestCaseTypeSection_AttributeTable_ColumnTitleAttributeType);

        column = new TableColumn(table, SWT.LEFT);
        column.setText(Messages.TestCaseTypeSection_AttributeTable_ColumnTitleAttribute);

        column = new TableColumn(table, SWT.LEFT);
        column.setText(Messages.TestCaseTypeSection_AttributeTable_ColumnTitle_Datatyp);

        // Create the viewer and connect it to the view
        final TableViewer viewer = new TableViewer(table);
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new TestAttributeTblLabelProvider());

        TestParameterType[] values = TestParameterType.values();
        String[] valueNames = new String[values.length - 1];
        int idx = 0;
        for (TestParameterType value : values) {
            if (value != TestParameterType.COMBINED) {
                valueNames[idx++] = value.getName();
            }
        }

        // create the cell editors
        ComboBoxCellEditor cellEditorTestAttributeType = new ComboBoxCellEditor(table, valueNames, SWT.READ_ONLY);
        final TextCellEditor textCellEditorName = new TextCellEditor(table);

        viewer.setCellEditors(new CellEditor[] { textCellEditorName, cellEditorTestAttributeType, null, null });

        viewer.setCellModifier(new TestAttributeCellModifier(viewer));
        viewer.setColumnProperties(
                new String[] { IIpsElement.PROPERTY_NAME, ITestAttribute.PROPERTY_TEST_ATTRIBUTE_TYPE,
                        ITestAttribute.PROPERTY_ATTRIBUTE, ITestAttribute.PROPERTY_DATATYPE });

        // add listener to the table
        viewer.addSelectionChangedListener(event -> {
            if (event.getSelection() instanceof IStructuredSelection) {
                Object firstElement = ((IStructuredSelection)event.getSelection()).getFirstElement();
                if (firstElement instanceof ITestAttribute) {
                    currSelectedDetailObject = (ITestAttribute)firstElement;
                    updateDetailButtonStatus((ITestAttribute)firstElement);
                    selectSection(objectCache.getSection((ITestAttribute)firstElement));
                    AttributeDetails attributeDetails = objectCache
                            .getAttributeDetails(((ITestAttribute)firstElement).getTestPolicyCmptTypeParameter());
                    attributeDetails.updateDetailAttributeArea((ITestAttribute)firstElement);
                }
            }
        });
        new TableMessageHoverService(viewer) {
            @Override
            protected MessageList getMessagesFor(Object element) {
                if (element != null) {
                    return validateElement(element);
                } else {
                    return null;
                }
            }
        };
        return viewer;
    }

    /**
     * Add attribute to test policy cmpt type param
     */
    private void addAttributeClicked(IIpsObjectPart object) {
        ITestPolicyCmptTypeParameter testPolicyCmptTypeParam;
        if (object instanceof ITestAttribute) {
            testPolicyCmptTypeParam = (ITestPolicyCmptTypeParameter)((ITestAttribute)object).getParent();
        } else if (object instanceof ITestPolicyCmptTypeParameter) {
            testPolicyCmptTypeParam = (ITestPolicyCmptTypeParameter)object;
        } else {
            throw new RuntimeException(
                    NLS.bind(Messages.TestCaseTypeSection_Error_UnexpectedObjectClass, object.getClass().getName()));
        }

        // open a wizard to select an attribute of the policy cmpt
        // wich is related by the given test policy cmpt type param
        // or define a new test attribite without a policy cmpt type
        // attribute
        newTestAttributesByWizard(testPolicyCmptTypeParam);
    }

    /**
     * Add attribute to test policy cmpt type param
     */
    private void removeAttributeClicked(IIpsObjectPart object) {
        if (object == null) {
            return;
        }
        ArgumentCheck.isInstanceOf(object, ITestAttribute.class);
        ITestAttribute testAttribute = (ITestAttribute)object;
        ITestParameter param = (ITestParameter)object.getParent();

        for (ITestAttribute iTestAttribute : getSelectedAttributes(testAttribute)) {
            iTestAttribute.delete();
        }
        redrawDetailArea((ITestPolicyCmptTypeParameter)param, (ITestPolicyCmptTypeParameter)param);
    }

    /**
     * Move attribute up
     */
    private void moveUpAttributeClicked(IIpsObjectPart object) {
        if (object == null) {
            return;
        }
        ArgumentCheck.isInstanceOf(object, ITestAttribute.class);
        moveTestAttribute((ITestAttribute)object, true);
    }

    /**
     * Move attribute down
     */
    private void moveDownAttributeClicked(IIpsObjectPart object) {
        if (object == null) {
            return;
        }
        ArgumentCheck.isInstanceOf(object, ITestAttribute.class);
        moveTestAttribute((ITestAttribute)object, false);
    }

    /**
     * Creates the edit fields for the test parameter
     */
    private void createTestParamDetails(Composite editFieldsComposite,
            ITestParameter testParam,
            BindingContext bindingContext) {

        Label label = toolkit.createFormLabel(editFieldsComposite, Messages.TestCaseTypeSection_EditFieldLabel_Name);
        EditField<String> editFieldName = new TextField(toolkit.createText(editFieldsComposite));
        editFieldName.setText(testParam.getName());
        addSectionSelectionListeners(editFieldName, label, testParam);
        objectCache.putFirstEditFieldInSection(testParam, editFieldName);
        bindingContext.bindContent(editFieldName, testParam, IIpsElement.PROPERTY_NAME);

        label = toolkit.createFormLabel(editFieldsComposite,
                Messages.TestCaseTypeSection_EditFieldLabel_TestParameterType);
        TestParameterType[] allowedValues;
        if (testParam instanceof ITestValueParameter) {
            allowedValues = new TestParameterType[] { TestParameterType.INPUT, TestParameterType.EXPECTED_RESULT };
        } else if (testParam instanceof ITestRuleParameter) {
            allowedValues = new TestParameterType[] { TestParameterType.EXPECTED_RESULT };
        } else {
            allowedValues = TestParameterType.values();
        }

        EnumField<TestParameterType> editFieldType = new EnumField<>(
                toolkit.createCombo(editFieldsComposite), allowedValues);
        addSectionSelectionListeners(editFieldType, label, testParam);
        bindingContext.bindContent(editFieldType, testParam, ITestParameter.PROPERTY_TEST_PARAMETER_TYPE);
    }

    private boolean isAssociation(ITestPolicyCmptTypeParameter testParam) {
        try {
            IPolicyCmptTypeAssociation association = (testParam).findAssociation(testParam.getIpsProject());
            if (association != null) {
                if (association.isAssoziation()) {
                    return true;
                }
            }
        } catch (IpsException e) {
            // ignore exception, inconsitence are displayed as validation errors
        }
        return false;
    }

    /**
     * Adds a listener to mark the section as selected if the given edit field gets the focus
     */
    private void addSectionSelectionListeners(EditField<?> editField, Control label, final IIpsObjectPart object) {
        if (editField != null) {
            editField.getControl().addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    Section section = objectCache.getSection(object);
                    if (section != null) {
                        currSelectedDetailObject = objectCache.getObject(object);
                        selectSection(section);
                    }
                }
            });
            editField.getControl().addMouseListener(new SectionSelectMouseListener(null, object));
        }
        if (label != null) {
            label.addMouseListener(new SectionSelectMouseListener(null, object));
        }
    }

    /**
     * Select the given section, means change the color of the section and store the current object.
     * And select the corresponding tree item if withFocusChange is <code>true</code>. If the given
     * section is <code>null</code> do nothing.
     */
    private void selectSection(Section section, boolean withFocusChange) {
        resetSectionSelectedColor();
        if (section == null) {
            return;
        }
        section.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        if (withFocusChange) {
            // select the corresponding object in the tree
            ITestParameter testParam = getRootSectionObject(currSelectedDetailObject);
            treeViewer.setSelection(new StructuredSelection(testParam));
            updateTreeButtonStatus(testParam);
        }
    }

    /**
     * Select the given section, means change the color of the section and store the current object.
     * And select the corresponding tree item. If the given section is <code>null</code> do nothing.
     */
    private void selectSection(Section section) {
        selectSection(section, true);
    }

    /**
     * Update the enable status of the detail buttons
     */
    private void updateDetailButtonStatus(IIpsObjectPart currSelectedDetailObject) {
        for (SectionButtons buttons : objectCache.getAllSectionButtons()) {
            buttons.updateDetailButtonStatus(getRootSectionObject(currSelectedDetailObject), currSelectedDetailObject);
        }
    }

    /**
     * Returns the object which is the root of a section, the returned object is the object in the
     * tree
     */
    private ITestParameter getRootSectionObject(IIpsObjectPart partInMainSection) {
        if (partInMainSection == null) {
            return null;
        }
        if (partInMainSection instanceof ITestAttribute) {
            return (ITestPolicyCmptTypeParameter)partInMainSection.getParent();
        }
        return (ITestParameter)partInMainSection;
    }

    /**
     * Reset the selection color of all sections
     */
    private void resetSectionSelectedColor() {
        for (Section section : objectCache.getAllSections()) {
            section.setBackground(form.getBackground());
        }
    }

    /**
     * Creates the edit fields for the test value parameter
     */
    private void createTestValueParamDetails(Composite editFieldsComposite,
            ITestValueParameter parameter,
            BindingContext bindingContext) {

        Label label = toolkit.createFormLabel(editFieldsComposite,
                Messages.TestCaseTypeSection_EditFieldLabel_Datatype);
        EditField<String> editFieldDatatype = new TextField(toolkit.createText(editFieldsComposite));
        addSectionSelectionListeners(editFieldDatatype, label, parameter);
        editFieldDatatype.getControl().setEnabled(false);
        addSectionSelectionListeners(editFieldDatatype, label, parameter);

        toolkit.createLabel(editFieldsComposite, Messages.TestCaseTypeSection_labelDescription);
        toolkit.createVerticalSpacer(editFieldsComposite, 1);
        DescriptionEditComposite description = new DescriptionEditComposite(editFieldsComposite, parameter, toolkit);
        GridData gd = (GridData)description.getLayoutData();
        gd.horizontalSpan = 2;

        // connect to model
        bindingContext.bindContent(editFieldDatatype, parameter, ITestValueParameter.PROPERTY_VALUEDATATYPE);
    }

    /**
     * Creates the edit fields for the test rule parameter
     */
    private void createTestRuleParamDetails(Composite editFieldsComposite, ITestParameter testParam) {
        toolkit.createLabel(editFieldsComposite, Messages.TestCaseTypeSection_labelDescription);
        toolkit.createVerticalSpacer(editFieldsComposite, 1);
        DescriptionEditComposite description = new DescriptionEditComposite(editFieldsComposite, testParam, toolkit);
        GridData gd = (GridData)description.getLayoutData();
        gd.horizontalSpan = 2;
    }

    /**
     * Creates the edit fields for the test policy cmpt type parameter
     */
    private void createTestPolicyCmptTypeParamDetails(Composite editFieldsComposite,
            ITestPolicyCmptTypeParameter parameter,
            BindingContext bindingContext) {

        Label label = null;
        if (!(isAssociation(parameter))) {
            label = toolkit.createFormLabel(editFieldsComposite,
                    Messages.TestCaseTypeSection_EditFieldLabel_RequiresProduct);
            EditField<Boolean> editFieldReqProd = new CheckboxField(toolkit.createCheckbox(editFieldsComposite));
            addSectionSelectionListeners(editFieldReqProd, label, parameter);
            bindingContext.bindContent(editFieldReqProd, parameter,
                    ITestPolicyCmptTypeParameter.PROPERTY_REQUIRES_PRODUCTCMT);
        }

        // min and max instances only for child parameters
        label = toolkit.createFormLabel(editFieldsComposite, Messages.TestCaseTypeSection_EditFieldLabel_MinInstances);
        EditField<Integer> editFieldMin = new CardinalityField(toolkit.createText(editFieldsComposite));
        editFieldMin.setValue(parameter.getMinInstances());
        addSectionSelectionListeners(editFieldMin, label, parameter);
        bindingContext.bindContent(editFieldMin, parameter, ITestPolicyCmptTypeParameter.PROPERTY_MIN_INSTANCES);

        label = toolkit.createFormLabel(editFieldsComposite, Messages.TestCaseTypeSection_EditFieldLabel_MaxInstances);
        EditField<Integer> editFieldMax = new CardinalityField(toolkit.createText(editFieldsComposite));
        editFieldMax.setValue(parameter.getMaxInstances());
        addSectionSelectionListeners(editFieldMax, label, parameter);
        bindingContext.bindContent(editFieldMax, parameter, ITestPolicyCmptTypeParameter.PROPERTY_MAX_INSTANCES);

        // disable min and max for root parameter
        if (parameter.isRoot()) {
            editFieldMin.getControl().setEnabled(false);
            editFieldMax.getControl().setEnabled(false);
        }
    }

    /**
     * Clears the detail area.
     */
    public void clearDetailArea() {
        if (detailsArea != null) {
            detailsArea.dispose();
        }

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
    private MessageList validateElement(Object element) {
        MessageList messageList = new MessageList();
        // validate element
        if (element instanceof IIpsObjectPartContainer) {
            messageList.add(
                    ((IIpsObjectPartContainer)element).validate(((IIpsObjectPartContainer)element).getIpsProject()));
        }
        return messageList;
    }

    /**
     * Store the given section and the object which is displayed in the section
     */
    private void storeSection(Section section, IIpsObjectPart object) {
        objectCache.putSection(object, section);
        objectCache.putSectionObjects(object);

        section.getChildren()[0].addMouseListener(new SectionSelectMouseListener(section, object));
        section.addMouseListener(new SectionSelectMouseListener(section, object));
        section.addExpansionListener(new SectionExpandListern(section, object));
    }

    /**
     * Add the tree listener to the tree.
     */
    private void hookTreeListeners() {
        openInNewEditorAction = new OpenInNewEditorAction();

        treeViewer.addSelectionChangedListener(event -> {
            if (event.getSelection() instanceof IStructuredSelection) {
                IStructuredSelection selection = (IStructuredSelection)event.getSelection();
                selectionInTreeChanged(selection);
            }
        });
        new TreeMessageHoverService(treeViewer) {
            @Override
            protected MessageList getMessagesFor(Object element) {
                if (element != null) {
                    return validateElement(element);
                } else {
                    return null;
                }
            }
        };
        treeViewer.addDoubleClickListener(event -> {
            Object selection = event.getSelection();
            if (!(selection instanceof IStructuredSelection)) {
                return;
            }
            Object selectedObject = ((IStructuredSelection)selection).getFirstElement();
            if (!(selectedObject instanceof IIpsObjectPart)) {
                return;
            }
            currSelectedDetailObject = (IIpsObjectPart)selectedObject;
            selectFirstEditFieldInSection(selectedObject);
        });
        treeViewer.getTree().addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.DEL) {
                    removeClicked();
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
        treeViewer.getTree().addMouseListener(adapter);
    }

    private void buildContextMenu() {
        MenuManager manager = new MenuManager();
        manager.setRemoveAllWhenShown(false);
        manager.add(new AddAction());
        manager.add(new RemoveAction());
        manager.add(new MoveUpAction());
        manager.add(new MoveDownAction());
        Menu contextMenu = manager.createContextMenu(treeViewer.getControl());
        treeViewer.getControl().setMenu(contextMenu);
    }

    /**
     * Selects the first edit field in the section contains the given object
     */
    private void selectFirstEditFieldInSection(Object selected) {
        EditField<?> editFieldName = objectCache.getSectionFirstEditField(selected);
        if (editFieldName == null) {
            return;
        }
        editFieldName.getControl().setFocus();
    }

    private static SelectionListener onSelection(Runnable r) {
        return new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    r.run();
                    // CSOFF: IllegalCatch
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
                // CSON: IllegalCatch
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        };
    }

    /**
     * Adds the button listener for the tree area.
     */
    private void hookButtonListeners() {
        removeButton.addSelectionListener(onSelection(this::removeClicked));
        addParameterButton.addSelectionListener(onSelection(this::addParameterClicked));
        moveUpButton.addSelectionListener(onSelection(() -> moveUpClicked(getSelectedObjectInTree())));
        moveDownButton.addSelectionListener(onSelection(() -> moveDownClicked(getSelectedObjectInTree())));
    }

    /**
     * Redraws the form.
     */
    private void redrawForm() {
        // redraw the form
        try {
            setFormRedraw(false);
            pack();
            getParent().layout(true);
            form.reflow(true);
        } finally {
            setFormRedraw(true);
        }
    }

    /**
     * Add root or child parameter.
     */
    private void addParameterClicked() {
        Object selObject = getSelectedObjectInTree();
        boolean dirty = testCaseType.getIpsSrcFile().isDirty();
        ITestParameter newParam = null;
        if (selObject instanceof TestCaseTypeTreeRootElement) {
            // open wizard to add a new root test parameter
            Memento memento = testCaseType.newMemento();
            NewRootParameterWizard wizard = new NewRootParameterWizard(testCaseType);
            WizardDialog dialog = new WizardDialog(getShell(), wizard);
            dialog.open();
            if (dialog.getReturnCode() == Window.CANCEL) {
                testCaseType.setState(memento);
                if (!dirty) {
                    testCaseType.getIpsSrcFile().markAsClean();
                }
                refresh();
                return;
            }
            newParam = wizard.getNewCreatedTestParameter();
        } else if (selObject instanceof ITestPolicyCmptTypeParameter) {
            // open wizard to add a new child test parameter

            // check if the policy cmpt type exists and if not
            // open a error dialog and cancel
            if (((ITestPolicyCmptTypeParameter)selObject)
                    .findPolicyCmptType(((ITestPolicyCmptTypeParameter)selObject).getIpsProject()) == null) {
                MessageDialog.openInformation(getShell(), Messages.TestCaseTypeSection_ErrorDialog_AddParameterTitle,
                        Messages.TestCaseTypeSection_ErrorDialog_AddParameterPcTypeIsMissing);
                return;
            }

            Memento memento = testCaseType.newMemento();
            NewChildParameterWizard wizard = new NewChildParameterWizard(testCaseType,
                    (ITestPolicyCmptTypeParameter)selObject);
            WizardDialog dialog = new WizardDialog(getShell(), wizard);
            dialog.open();
            if (dialog.getReturnCode() == Window.CANCEL) {
                testCaseType.setState(memento);
                if (!dirty) {
                    testCaseType.getIpsSrcFile().markAsClean();
                }
                return;
            }
            newParam = wizard.getNewCreatedTestParameter();
        }
        refreshTreeAndDetails(newParam);
    }

    /**
     * Remove button was clicked.
     */
    private void removeClicked() {
        Object selObject = getSelectedObjectInTree();
        if (!(selObject instanceof ITestParameter)) {
            return;
        }

        ITestParameter testParameter = (ITestParameter)selObject;
        testParameter.delete();

        // try to obtain the previous tree item and if exists use this item as new selected object
        // after delete, if no previous found select the next item
        TreeItem[] selection = treeViewer.getTree().getSelection();
        TreeItem[] childs = treeViewer.getTree().getItems();
        TreeItem prevTreeItem = searchChilds(childs, selection[0], null);
        if (prevTreeItem != null && prevTreeItem.getData() instanceof ITestParameter) {
            refreshTreeAndDetails((ITestParameter)prevTreeItem.getData());
        } else {
            refreshTreeAndDetails(null);
        }
    }

    /**
     * Move the selected test parameter up
     */
    private void moveUpClicked(Object selectedObjInTree) {
        if (selectedObjInTree instanceof ITestParameter) {
            moveTestParameter((ITestParameter)selectedObjInTree, true);
        }
    }

    /**
     * Move the selected test parameter down
     */
    private void moveDownClicked(Object selectedObjInTree) {
        if (selectedObjInTree instanceof ITestParameter) {
            moveTestParameter((ITestParameter)selectedObjInTree, false);
        }
    }

    private TreeItem searchChilds(TreeItem[] childs, TreeItem selectedItem, TreeItem prevTreeItem) {
        for (TreeItem child : childs) {
            if (child.equals(selectedItem)) {
                return prevTreeItem;
            }

            TreeItem found = searchChilds(child.getItems(), selectedItem, child);
            if (found != null) {
                return found;
            }

        }
        return null;
    }

    /**
     * Gets the currently selected test parameter or null if no test parameter is selected in the
     * tree
     */
    private Object getSelectedObjectInTree() {
        ISelection selection = treeViewer.getSelection();
        if (!(selection instanceof IStructuredSelection)) {
            return null;
        }
        Object selectedElem = ((IStructuredSelection)selection).getFirstElement();
        if (!(selectedElem instanceof ITestParameter || selectedElem instanceof TestCaseTypeTreeRootElement)) {
            return null;
        }
        return selectedElem;
    }

    /**
     * Return the selected attributes in the attribute table
     */
    private Set<ITestAttribute> getSelectedAttributes(ITestAttribute object) {
        Set<ITestAttribute> testAttributesSelected = new HashSet<>(1);
        testAttributesSelected.add(object);

        ITestParameter param = (ITestParameter)object.getParent();
        TableViewer attributeTable = objectCache.getAttributeTable(param);
        if (attributeTable != null) {
            ISelection selection = attributeTable.getSelection();
            if (selection instanceof IStructuredSelection) {
                IStructuredSelection structuredSelection = (IStructuredSelection)selection;
                for (Object element : structuredSelection) {
                    if (element instanceof ITestAttribute) {
                        testAttributesSelected.add((ITestAttribute)element);
                    }
                }
            }
        }
        return testAttributesSelected;
    }

    /**
     * Moves the give test attribute up or down
     */
    private void moveTestAttribute(ITestAttribute testAttribute, boolean up) {
        Set<ITestAttribute> selectedAttributes = getSelectedAttributes(testAttribute);
        int[] selectedAttributesIndexes = new int[selectedAttributes.size()];
        int i = 0;
        for (ITestAttribute element : selectedAttributes) {
            Integer testAttributeIdx = objectCache.getIdxFromAttribute(element);
            if (testAttributeIdx == null) {
                throw new RuntimeException(Messages.TestCaseTypeSection_Error_WrongTestAttributeIndex);
            }
            selectedAttributesIndexes[i++] = testAttributeIdx.intValue();
        }

        ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter = (ITestPolicyCmptTypeParameter)testAttribute
                .getParent();
        int[] movedAttributesIndexes = testPolicyCmptTypeParameter.moveTestAttributes(selectedAttributesIndexes, up);
        redrawDetailArea(testPolicyCmptTypeParameter, testAttribute);

        TableViewer attrTable = objectCache.getAttributeTable(testPolicyCmptTypeParameter);
        List<ITestAttribute> newAttributeIdx = new ArrayList<>(selectedAttributes.size());
        if (attrTable != null) {
            for (int movedAttributesIndexe : movedAttributesIndexes) {
                ITestAttribute movedAttribute = objectCache.getAttributeByIndex(Integer.valueOf(movedAttributesIndexe));
                if (movedAttribute != null) {
                    newAttributeIdx.add(movedAttribute);
                }
            }
        }
    }

    /**
     * Moves the give test attribute up or down
     */
    private void moveTestParameter(ITestParameter testParameter, boolean up) {
        int[] selectedTestParamIndexes = null;
        if (testParameter.isRoot()) {
            ITestParameter[] testParams = testCaseType.getTestParameters();
            for (int i = 0; i < testParams.length; i++) {
                if (testParams[i].equals(testParameter)) {
                    selectedTestParamIndexes = new int[] { i };
                    break;
                }
            }
            if (selectedTestParamIndexes == null) {
                throw new RuntimeException("Unable to determine the index of the given test parameter!"); //$NON-NLS-1$
            }
            testCaseType.moveTestParameters(selectedTestParamIndexes, up);
        } else {
            ITestPolicyCmptTypeParameter parent = (ITestPolicyCmptTypeParameter)testParameter.getParent();
            List<ITestPolicyCmptTypeParameter> childs = Arrays.asList(parent.getTestPolicyCmptTypeParamChilds());
            selectedTestParamIndexes = new int[] { childs.indexOf(testParameter) };
            parent.moveTestPolicyCmptTypeChild(selectedTestParamIndexes, up);
        }
        if (showAll) {
            // recreate and select element after moving
            // off
            showAllClicked();
            // and on again
            showAllClicked();
            postSelectedTestParameterInTree(testParameter);
        }
    }

    /**
     * The selection in the tree changed the given object is selected.
     */
    private void selectionInTreeChanged(IStructuredSelection selection) {
        // skip if this method was called while the tree is refreshing
        if (isTreeRefreshing) {
            return;
        }

        if (selection != null) {
            Object selectedObj = (selection).getFirstElement();
            if (selectedObj != null && selectedObj instanceof ITestParameter) {
                // if the selected object is the previous rendered object
                // or all elements are visible return
                if (showAll) {
                    // select section for the tree entry
                    selectSection(objectCache.getSection((ITestParameter)selectedObj), false);
                } else if (selectedObj != prevSelectedTestParam) {
                    createDetailsArea((ITestParameter)selectedObj);
                    setDataChangeable(isDataChangeable());
                }
            } else {
                if (!showAll) {
                    // no details are created
                    createDetailsArea(null);
                }
                // select no section
                selectSection(null);
            }
            updateTreeButtonStatus(selectedObj);
        }
    }

    /**
     * Updates the enable state of the buttons which belongs to the tree
     */
    private void updateTreeButtonStatus(Object object) {
        if (!isDataChangeable()) {
            toolkit.setDataChangeable(removeButton, false);
            toolkit.setDataChangeable(addParameterButton, false);
            toolkit.setDataChangeable(moveUpButton, false);
            toolkit.setDataChangeable(moveDownButton, false);
            return;
        }
        TreeActionEnableState treeActionEnableState = evaluateTreeActionEnableState(object);
        removeButton.setEnabled(treeActionEnableState.isRemoveEnable());
        addParameterButton.setEnabled(treeActionEnableState.isAddEnable());
        moveUpButton.setEnabled(treeActionEnableState.isUpEnable());
        moveDownButton.setEnabled(treeActionEnableState.isDownEnable());
    }

    private TreeActionEnableState evaluateTreeActionEnableState(Object object) {
        Object obj = object;
        TreeActionEnableState treeActionEnableState = new TreeActionEnableState();
        if (obj == null) {
            obj = getSelectedObjectInTree();
        }

        if (obj == null || !isDataChangeable()) {
            return treeActionEnableState;
        }

        if (obj instanceof ITestPolicyCmptTypeParameter) {
            treeActionEnableState.setRemoveEnable(true);
            treeActionEnableState.setAddEnable(true);
            treeActionEnableState.setUpEnable(true);
            treeActionEnableState.setDownEnable(true);
        } else if (obj instanceof ITestParameter) {
            treeActionEnableState.setRemoveEnable(true);
            treeActionEnableState.setAddEnable(false);
            treeActionEnableState.setUpEnable(true);
            treeActionEnableState.setDownEnable(true);
        } else if (obj instanceof TestCaseTypeTreeRootElement) {
            treeActionEnableState.setRemoveEnable(false);
            treeActionEnableState.setAddEnable(true);
            treeActionEnableState.setUpEnable(false);
            treeActionEnableState.setDownEnable(false);
        } else {
            treeActionEnableState.setRemoveEnable(false);
            treeActionEnableState.setAddEnable(false);
            treeActionEnableState.setUpEnable(false);
            treeActionEnableState.setDownEnable(false);
        }
        return treeActionEnableState;
    }

    /**
     * Refreshs the tree and details
     */
    private void refreshTreeAndDetails(ITestParameter param) {
        if (param != null && !treeViewer.getExpandedState(param)) {
            // if the parameter isn't expanded then first expand the tree
            // otherwise it isn't possible to select the new parameter in the tree
            treeViewer.expandAll();
        }
        refreshTree();
        createDetailsArea(param);
        currSelectedDetailObject = param;
        selectSection(objectCache.getSection(param));
        selectFirstEditFieldInSection(param);
    }

    /**
     * Refresh the attribute table
     */
    private void refreshAttributeTable() {
        for (TableViewer table : objectCache.getAllAttributeTable()) {
            table.refresh();
        }
    }

    /**
     * Refresh the tree.
     */
    private void refreshTree() {
        if (treeViewer.getTree().isDisposed()) {
            return;
        }

        try {
            treeViewer.refresh();
        } finally {
            treeViewer.getTree().setRedraw(true);
            isTreeRefreshing = false;
        }
    }

    /**
     * Refreshs all section titles
     */
    private void refreshSectionTitles() {
        for (Integer key : objectCache.getAllSectionKeys()) {
            Section section = objectCache.getSectionByKey(key);
            if (section.isDisposed()) {
                continue;
            }
            section.setText(labelProvider.getText(objectCache.getObjectByKey(key)));
            section.getParent().setRedraw(false);
            section.pack();
            section.getParent().layout();
            section.getParent().setRedraw(true);
        }
    }

    /**
     * Redraw and select the section of the new attribute
     */
    private void redrawDetailArea(ITestPolicyCmptTypeParameter testPolicyCmptTypeParam, IIpsObjectPart selectedObject) {
        // store the expanded sections state
        List<Integer> expandedSectionKeys = new ArrayList<>();
        for (Integer key : objectCache.getAllSectionKeys()) {
            if (objectCache.getSectionByKey(key).isExpanded()) {
                expandedSectionKeys.add(key);
            }
        }

        try {
            setFormRedraw(false);
            treeViewer.refresh();
            prevSelectedTestParam = null;
            createDetailsArea(testPolicyCmptTypeParam);
            currSelectedDetailObject = selectedObject;
            selectSection(objectCache.getSection(selectedObject));

            // restore the expanded section states
            for (Integer integer : expandedSectionKeys) {
                Section section = objectCache.getSectionByKey(integer);
                if (section == null) {
                    return;
                }
                section.setExpanded(true);
            }

            if (selectedObject instanceof ITestAttribute) {
                TableViewer attrTable = objectCache.getAttributeTable(testPolicyCmptTypeParam);
                if (attrTable != null) {
                    attrTable.setSelection(new StructuredSelection(selectedObject));
                }
            }
        } finally {
            setFormRedraw(true);
        }
    }

    /**
     * Refresh the ui
     */
    private void postRefreshAll() {
        postAsyncRunnable(() -> {
            if (isDisposed()) {
                return;
            }
            try {
                setFormRedraw(false);
                refreshTree();
                refreshAttributeTable();
                refreshSectionTitles();
                // refresh attribute edit fields
                for (UIController controller : attributeControllers) {
                    controller.updateUI();
                }
                updateTreeButtonStatus(getRootSectionObject(currSelectedDetailObject));
            } finally {
                setFormRedraw(true);
            }
        });
    }

    /**
     * Select the given test parameter in the tree
     */
    private void postSelectedTestParameterInTree(final ITestParameter testParam) {
        postAsyncRunnable(() -> treeViewer.setSelection(new StructuredSelection(testParam)));
    }

    /**
     * Create a bordered composite
     */
    private Composite createBorderComposite(Composite parent) {
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
     * Sets the redraw state of the form
     */
    private void setFormRedraw(boolean redraw) {
        if (!form.isDisposed()) {
            form.setRedraw(redraw);
        }
    }

    /**
     * Displays a dialog to select attributes of a policy component.
     */
    private void newTestAttributesByWizard(ITestPolicyCmptTypeParameter testPolicyCmptTypeParam) {
        boolean dirty = testCaseType.getIpsSrcFile().isDirty();

        // open wizard to add new test attributes
        Memento memento = testCaseType.newMemento();
        NewTestAttributeWizard wizard = new NewTestAttributeWizard(testCaseType, testPolicyCmptTypeParam,
                showSubtypeAttributes);
        WizardDialog dialog = new WizardDialog(getShell(), wizard);
        dialog.open();
        if (dialog.getReturnCode() == Window.CANCEL) {
            testCaseType.setState(memento);
            if (!dirty) {
                testCaseType.getIpsSrcFile().markAsClean();
            }
        } else {
            redrawDetailArea(testPolicyCmptTypeParam, wizard.getNewlyCreatedTestAttribute());
            showSubtypeAttributes = wizard.getShowSubtypeAttributes();
        }
    }

    public void refreshTreeAndDetailArea() {
        refreshTreeAndDetails(getRootSectionObject(currSelectedDetailObject));
    }
}
// CSON: InnerTypeLastCheck
// CSON: FileLengthCheck
