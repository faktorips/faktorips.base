/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.StringDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcase.ITestRule;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcase.TestRuleViolationType;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.UIController;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;

/**
 * Detail section class of the test case editor. Supports dynamic creation of detail edit controls.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseDetailArea {
    // UI toolkit for creating the controls
    private UIToolkit toolkit;
    
    // Contains the content provider of the test policy component object
    private TestCaseContentProvider contentProvider;

    private IIpsProject ipsProject;
    
    // Contains all edit sections the key is the name of the correspondin test parameter
    private HashMap sectionControls = new HashMap();

    // Container holds all edit fields for test values and test attribute values
    private HashMap allEditFields = new HashMap();

    // Contains the first edit field of each test policy component in the edit area
    private HashMap firstAttributeEditFields = new HashMap();

    // Contains the failures of the last test run
    private HashMap failureMessageCache = new HashMap();
    private HashMap failureDetailCache = new HashMap();
    // Contains all fixed fields (actual value stored as expected value)
    private List fixedFieldsCache = new ArrayList();
    
    // Contains the mapping between the edit field and model objects
    private HashMap editField2ModelObject = new HashMap();

    // Contains all ui controller
    private ArrayList uiControllers = new ArrayList();

    // The section this details belongs to
    private TestCaseSection testCaseSection;

    // Composites to change the UI
    // area which contains the dynamic detail controls
    private Composite detailsArea;
    // area which contains alls detail controls
    private Composite dynamicArea;

    private List previousDisplayedTestObjects;
    private List testCaseDetailAreaRedrawListener = new ArrayList(1);
    
    /*
     * Mouse listener class to select the section if the mouse button is clicked
     */
    private class SectionSelectMouseListener implements MouseListener {
        private ITestPolicyCmpt testPolicyCmptType;
        private ITestPolicyCmptRelation testPolicyCmptTypeRelation;
        private ITestObject testObject;

        public SectionSelectMouseListener(IIpsObjectPart object) {
            if (object instanceof ITestPolicyCmpt) {
                testPolicyCmptType = (ITestPolicyCmpt)object;
            } else if (object instanceof ITestPolicyCmptRelation) {
                testPolicyCmptTypeRelation = (ITestPolicyCmptRelation)object;
            } else if (object instanceof ITestObject) {
                testObject = (ITestObject)object;
            }
        }

        public SectionSelectMouseListener(ITestPolicyCmptRelation testPolicyCmptTypeRelation) {
            this.testPolicyCmptTypeRelation = testPolicyCmptTypeRelation;
        }

        public void mouseDown(MouseEvent e) {
            if (testPolicyCmptType != null) {
                testCaseSection.selectInTreeByObject(testPolicyCmptType, false);
                testCaseSection.selectInDetailArea(testPolicyCmptType, false);
            } else if (testPolicyCmptTypeRelation != null) {
                testCaseSection.selectInTreeByObject(testPolicyCmptTypeRelation, false);
                testCaseSection.selectInDetailArea(testPolicyCmptTypeRelation, false);
            } else if (testObject != null) {
                testCaseSection.selectTestObjectInTree(testObject);
                testCaseSection.selectInDetailArea(testObject, false);
            }
        }

        public void mouseDoubleClick(MouseEvent e) {
            // nothing to do
        }

        public void mouseUp(MouseEvent e) {
            // nothing to do
        }
    }

    public TestCaseDetailArea(UIToolkit toolkit, TestCaseContentProvider contentProvider,
            TestCaseSection testCaseSection) {
        this.toolkit = toolkit;
        this.contentProvider = contentProvider;
        this.ipsProject = contentProvider.getTestCase().getIpsProject();
        this.testCaseSection = testCaseSection;
    }

    /**
     * Resets the color of all detail sections.
     */
    public void resetSectionColors(ScrolledForm form) {
        Iterator iter = sectionControls.values().iterator();
        while (iter.hasNext()) {
            Section section = (Section)iter.next();
            section.setBackground(form.getBackground());
        }
    }

    /**
     * Returns the attribute edit fields given by the unique key.
     */
    public EditField getFirstAttributeEditField(String uniqueKey) {
        return (EditField)firstAttributeEditFields.get(uniqueKey);
    }

    /**
     * Returns the test value edit fields given by the unique key.
     */
    public EditField getTestValueEditField(String uniqueKey) {
        return (EditField)allEditFields.get(uniqueKey);
    }

    /**
     * Returns the section given by the unique key.
     */
    public Section getSection(String uniqueKey) {
        return (Section)sectionControls.get(uniqueKey);
    }

    /**
     * Returns the edit field which is identified by the given unique key.
     */
    public EditField getEditField(String uniqueKey) {
        EditField editField = (EditField)allEditFields.get(uniqueKey);
        if (editField == null) {
            // edit field not found, try to get the special edit value field
            editField = (EditField)allEditFields.get(TestCaseSection.VALUESECTION + uniqueKey);
        }
        return editField;
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

    public void addDetailAreaRedrawListener(ITestCaseDetailAreaRedrawListener listener){
        testCaseDetailAreaRedrawListener.add(listener);
    }
    
    public void removeDetailAreaRedrawListener(ITestCaseDetailAreaRedrawListener listener){
        testCaseDetailAreaRedrawListener.remove(listener);
    }
    
    private void notifyListener(List testObjects) {
        try {
            for (Iterator iter = testCaseDetailAreaRedrawListener.iterator(); iter.hasNext();) {
                ITestCaseDetailAreaRedrawListener listener = (ITestCaseDetailAreaRedrawListener)iter.next();
                listener.visibleTestObjectsChanges(testObjects);
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }
    
    private boolean visibleTestObjectsChanged(List testObjects) {
        boolean changed = false;
        if (previousDisplayedTestObjects == null){
            changed = true;
        } 
        if (!changed && testObjects.size() != previousDisplayedTestObjects.size()){
            changed = true;
        }
        if (!changed){
            for (int i = 0; i < testObjects.size(); i++) {
                if (previousDisplayedTestObjects.get(i) != testObjects.get(i)){
                    changed = true;
                    break;
                }
            }
        }
        if (changed){
            previousDisplayedTestObjects = testObjects;
        }
        return changed;
    }
    
    /**
     * Creates the details for the given test objects.
     */
    public void createTestObjectSections(List testObjects) {
        try {
            if (!visibleTestObjectsChanged(testObjects)){
                return;
            }
            notifyListener(testObjects);
            
            for (Iterator iter = testObjects.iterator(); iter.hasNext();) {
                ITestObject testObject = (ITestObject)iter.next();
                if (testObject instanceof ITestValue) {
                    Composite borderedComosite = createBorderComposite(dynamicArea);
                    createTestValuesSection((ITestValue)testObject, borderedComosite);
                } else if (testObject instanceof ITestRule) {
                    Composite borderedComosite = createBorderComposite(dynamicArea);
                    createTestRuleSection((ITestRule)testObject, borderedComosite);
                } else if (testObject instanceof ITestPolicyCmpt) {
                    Composite borderedComosite = createBorderComposite(dynamicArea);
                    createPolicyCmptAndRelationSection((ITestPolicyCmpt)testObject, borderedComosite);
                }
            }
            
            if (!testCaseSection.isDataChangeable()){
                toolkit.setDataChangeable(detailsArea, false);
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates the section with the test policy component object.<br>
     * If the element is a child then the relation name could be given as input to display it in the
     * section title beside the test policy component.
     * 
     * @throws CoreException
     */
    private void createPolicyCmptSection(final ITestPolicyCmpt testPolicyCmpt, Composite details) throws CoreException {
        if (testPolicyCmpt == null) {
            return;
        }
        String uniqueKey = testCaseSection.getUniqueKey(testPolicyCmpt);

        if (!((testCaseSection.getContentProvider().isExpectedResult() && testPolicyCmpt.isExpectedResult()) || (testCaseSection
                .getContentProvider().isInput() && testPolicyCmpt.isInput()))) {
            // check if the parameter wasn't found
            // if the parameter not exists then the type couldn't be determined, therefore display the content in any case
            if (testPolicyCmpt.findTestPolicyCmptTypeParameter(ipsProject) != null){
                return;
            }
        }

        Section section = toolkit.getFormToolkit().createSection(details, 0);
        section.setText(testCaseSection.getLabelProvider().getTextForSection(testPolicyCmpt));
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        // create separator line
        toolkit.getFormToolkit().createCompositeSeparator(section);
        section.addMouseListener(new SectionSelectMouseListener(testPolicyCmpt));
        section.getChildren()[0].addMouseListener(new SectionSelectMouseListener(testPolicyCmpt));

        sectionControls.put(uniqueKey, section);

        Composite attributeComposite = toolkit.createLabelEditColumnComposite(section);

        // create text edit fields for each attribute
        ITestAttributeValue[] testAttributeValues = testPolicyCmpt.getTestAttributeValues();
        boolean firstEditField = true; 
        for (int i = 0; i < testAttributeValues.length; i++) {
            final ITestAttributeValue attributeValue = testAttributeValues[i];
            // Create the edit field only if the content provider provides the type of the test
            // attribute object
            if (testCaseSection.getContentProvider().isCombined()
                    || (testCaseSection.getContentProvider().isInput() && testAttributeValues[i].isInputAttribute(ipsProject))
                    || testCaseSection.getContentProvider().isExpectedResult()
                    && testAttributeValues[i].isExpextedResultAttribute(ipsProject)) {
                EditField editField = createAttributeEditField(testPolicyCmpt, testPolicyCmpt, attributeComposite,
                        attributeValue);

                // store the first attribute of each policy cmpt for fast focus setting
                if (editField != null && firstEditField) {
                    firstAttributeEditFields.put(uniqueKey, editField);
                    firstEditField = false;
                }
            }
        }
        section.setClient(attributeComposite);

        toolkit.createVerticalSpacer(details, 10).setBackground(details.getBackground());
    }

    private EditField createAttributeEditField(final ITestPolicyCmpt testPolicyCmpt,
            final ITestPolicyCmpt testPolicyCmptForSelection,
            Composite attributeComposite,
            final ITestAttributeValue attributeValue) throws CoreException {
        IpsObjectUIController uiController = createUIController(attributeValue);
        EditField editField = null;

        // get the ctrlFactory to create the edit field
        ValueDatatype datatype = null;
        ValueDatatypeControlFactory ctrlFactory = null;
        
        try {
            IAttribute attribute = attributeValue.findAttribute(ipsProject);
            if (attribute != null){
                datatype = attribute.findDatatype(ipsProject);
                ctrlFactory = IpsPlugin.getDefault().getValueDatatypeControlFactory(datatype);
            } else { 
                if (StringUtils.isNotEmpty(attributeValue.getValue())){
                    ctrlFactory = IpsPlugin.getDefault().getValueDatatypeControlFactory(ValueDatatype.STRING);
                } else {
                    // if the attribute wasn't found and no value is stored then no controls will be displayed
                    // maybe this attributes are not available in subtype test policy cmpt's
                    return null;
                }
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
            ctrlFactory = IpsPlugin.getDefault().getValueDatatypeControlFactory(ValueDatatype.STRING);
        }
        
        Label label = toolkit.createFormLabel(attributeComposite, StringUtils.capitalize(attributeValue
                .getTestAttribute()));
        addSectionSelectionListeners(null, label, testPolicyCmptForSelection);

        try {
            editField = ctrlFactory.createEditField(toolkit, attributeComposite, datatype, null);
        } catch (Exception e) {
            // ignore exception
        }
        
        uiController.add(editField, ITestAttributeValue.PROPERTY_VALUE);
        // store the edit field
        String testPolicyCmptTypeParamPath = TestCaseHierarchyPath.evalTestPolicyCmptParamPath(testPolicyCmpt);
        allEditFields.put(testPolicyCmptTypeParamPath + attributeValue.getTestAttribute(), editField);
        editField2ModelObject.put(editField, attributeValue);
        addSectionSelectionListeners(editField, null, testPolicyCmptForSelection);

        // mark as expected result
        if (attributeValue.isExpextedResultAttribute(ipsProject)) {
            markAsExpected(editField);
        }
        // mark as failure
        String failureLastTestRun = (String)failureMessageCache.get(testPolicyCmptTypeParamPath
                + attributeValue.getTestAttribute());
        if (failureLastTestRun != null) {
            if (!fixedFieldsCache.contains(testPolicyCmptTypeParamPath + attributeValue.getTestAttribute())) {
                testCaseSection.postSetFailureBackgroundAndToolTip(editField, failureLastTestRun);
                // create context menu
                String[] failureDetails = (String[])failureDetailCache.get(testPolicyCmptTypeParamPath
                        + attributeValue.getTestAttribute());
                if (failureDetails != null) {
                    testCaseSection.postAddExpectedResultContextMenu(editField.getControl(), failureDetails);
                }
            } else {
                testCaseSection.postSetOverriddenValueBackgroundAndToolTip(editField, failureLastTestRun, false);
            }
        }
        uiController.updateUI();

        return editField;
    }

    /*
     * Marks the given edit field as expected result.
     */
    private void markAsExpected(final EditField editField) {
        editField.getControl().setBackground(testCaseSection.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
    }

    /*
     * Creates the section for a relation of type association.<br> Create a hyperlink if the
     * realtion exists is in the current test case or create a label with the test relation target.
     */
    private void createRelationSectionAssociation(final ITestPolicyCmptRelation currRelation, Composite details) {
        String uniquePath = testCaseSection.getUniqueKey(currRelation);

        Section section = toolkit.getFormToolkit().createSection(details, 0);
        section.setText(testCaseSection.getLabelProvider().getTextForSection(currRelation));
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        // create separator line
        toolkit.getFormToolkit().createCompositeSeparator(section);

        section.addMouseListener(new SectionSelectMouseListener(currRelation));
        section.getChildren()[0].addMouseListener(new SectionSelectMouseListener(currRelation));

        Composite hyperlinkArea = toolkit.createGridComposite(details, 2, false, true);
        sectionControls.put(uniquePath, section);

        // create a hyperlink to the target
        ITestPolicyCmpt target = null;
        try {
            target = contentProvider.getTestCase().findTestPolicyCmpt(currRelation.getTarget());
        } catch (CoreException e2) {
            // ignore the exception, error searching for the target
        }
        if (target != null) {
            Hyperlink relationHyperlink = toolkit.getFormToolkit().createHyperlink(hyperlinkArea,
                    TestCaseHierarchyPath.unqualifiedName(currRelation.getTarget()), SWT.WRAP);
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
            String hyperLinkPath = " (" + testCaseSection.getLabelProvider().getAssoziationTargetLabel(currRelation.getTarget()) + " ) "; //$NON-NLS-1$ //$NON-NLS-2$
            String hyperLinklabel = hyperLinkPath; //$NON-NLS-1$
            if (hyperLinklabel.length() > 60) {
                hyperLinklabel = hyperLinkPath.substring(0, 27);
                hyperLinklabel += "..."; //$NON-NLS-1$
                hyperLinklabel += hyperLinkPath.substring(hyperLinkPath.length() - 30);
            }
            Label label = toolkit.createLabel(hyperlinkArea, hyperLinklabel);
            addSectionSelectionListeners(null, label, currRelation);
        } else {
            // target not found in current test case
            Label label = toolkit.createLabel(hyperlinkArea, TestCaseHierarchyPath.unqualifiedName(currRelation
                    .getTarget()));
            addSectionSelectionListeners(null, label, currRelation);
            label = toolkit
                    .createLabel(
                            hyperlinkArea,
                            " ("    + testCaseSection.getLabelProvider().getAssoziationTargetLabel(currRelation.getTarget()) + " ) "); //$NON-NLS-1$ //$NON-NLS-2$
            addSectionSelectionListeners(null, label, currRelation);
        }
    }

    /**
     * Recursive create the sections for the relations and all their childs.
     * 
     * @throws CoreException
     */
    private void createPolicyCmptAndRelationSection(ITestPolicyCmpt currTestPolicyCmpt, Composite details)
            throws CoreException {
        createPolicyCmptSection(currTestPolicyCmpt, details);
        ITestPolicyCmptRelation[] relations = currTestPolicyCmpt.getTestPolicyCmptRelations();
        for (int i = 0; i < relations.length; i++) {
            ITestPolicyCmptRelation currRelation = relations[i];
            if (currRelation.isComposition()) {
                try {
                    ITestPolicyCmpt policyCmpt = currRelation.findTarget();
                    if (policyCmpt != null) {
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
     * Creates the section for the given test value object.
     */
    private void createTestValuesSection(final ITestValue testValue, Composite details) {
        // Create the edit field only if the content provider provides the type of the test value
        // object
        String uniquePath = testCaseSection.getUniqueKey(testValue);
        if (!isVisibleForContentFilter(testCaseSection.getContentProvider(), testValue)) {
            return;
        }

        IpsObjectUIController uiController = createUIController(testValue);

        Section section = toolkit.getFormToolkit().createSection(details, 0);
        section.setText(testCaseSection.getLabelProvider().getTextForSection(testValue));
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        // create separator line
        toolkit.getFormToolkit().createCompositeSeparator(section);
        sectionControls.put(uniquePath, section);

        section.addMouseListener(new SectionSelectMouseListener(testValue));
        section.getChildren()[0].addMouseListener(new SectionSelectMouseListener(testValue));

        Composite composite = toolkit.createLabelEditColumnComposite(section);
        section.setClient(composite);

        ValueDatatype datatype = null;
        ValueDatatypeControlFactory ctrlFactory = null;
        try {
            ITestValueParameter param = testValue.findTestValueParameter(ipsProject);
            if (param != null) {
                datatype = param.findValueDatatype(ipsProject);
                ctrlFactory = IpsPlugin.getDefault().getValueDatatypeControlFactory(datatype);
            } else {
                ctrlFactory = IpsPlugin.getDefault().getValueDatatypeControlFactory(new StringDatatype());
            }
        } catch (CoreException e1) {
            throw new RuntimeException(e1);
        }

        Label label = toolkit.createFormLabel(composite, Messages.TestCaseDetailArea_Label_Value);
        final EditField editField = ctrlFactory.createEditField(toolkit, composite, datatype, null);
        addSectionSelectionListeners(editField, label, testValue);
        uiController.add(editField, ITestValue.PROPERTY_VALUE);

        editField.getControl().addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                testCaseSection.selectTestObjectInTree(testValue);
            }
        });

        allEditFields.put(uniquePath, editField);
        editField2ModelObject.put(editField, testValue);

        // mark as expected result
        if (testValue.isExpectedResult()) {
            markAsExpected(editField);
        }
        // mark as failure
        String failureLastTestRun = (String)failureMessageCache.get(testValue.getTestValueParameter());
        if (failureLastTestRun != null) {
            testCaseSection.postSetFailureBackgroundAndToolTip(editField, failureLastTestRun);
        }

        uiController.updateUI();
    }

    /**
     * Creates the section for the given test rule objects.
     */
    private void createTestRuleSection(final ITestRule rule, Composite borderedComosite) {
        // Create the edit field only if the content provider provides the type of the test value
        // object
        String uniqueKey = testCaseSection.getUniqueKey(rule);
        if (!isVisibleForContentFilter(testCaseSection.getContentProvider(), rule)) {
            return;
        }

        IpsObjectUIController uiController = createUIController(rule);

        Section section = toolkit.getFormToolkit().createSection(borderedComosite, 0);
        section.setText(testCaseSection.getLabelProvider().getTextForSection(rule));
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        // create separator line
        toolkit.getFormToolkit().createCompositeSeparator(section);
        sectionControls.put(uniqueKey, section);

        section.addMouseListener(new SectionSelectMouseListener(rule));
        section.getChildren()[0].addMouseListener(new SectionSelectMouseListener(rule));

        Composite composite = toolkit.createLabelEditColumnComposite(section);
        section.setClient(composite);

        Label label = toolkit.createFormLabel(composite, Messages.TestCaseDetailArea_Label_Violation);
        final EditField editField = new EnumValueField(toolkit.createCombo(composite, TestRuleViolationType
                .getEnumType()), TestRuleViolationType.getEnumType());
        addSectionSelectionListeners(editField, label, rule);
        uiController.add(editField, ITestRule.PROPERTY_VIOLATED);

        editField.getControl().addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                testCaseSection.selectInTreeByObject(rule);
            }
        });

        allEditFields.put(uniqueKey, editField);
        editField2ModelObject.put(editField, rule);

        // mark as expected result
        if (rule.isExpectedResult()) {
            markAsExpected(editField);
        }
        // mark as failure
        String failureLastTestRun = (String)failureMessageCache.get(testCaseSection.getUniqueKey(rule));
        if (failureLastTestRun != null) {
            testCaseSection.postSetFailureBackgroundAndToolTip(editField, failureLastTestRun);
        }

        uiController.updateUI();

    }

    /*
     * Return <code>true</code> if the given test object is visible or not <code>false</code>.
     */
    private boolean isVisibleForContentFilter(TestCaseContentProvider contentProvider, ITestObject testObject) {
        try {
            if (!((testCaseSection.getContentProvider().isInput() && testObject.isInput()) || (testCaseSection
                    .getContentProvider().isExpectedResult() && testObject.isExpectedResult()))
                    && !testCaseSection.getContentProvider().isCombined()) {
                return false;
            }
        } catch (Exception e) {
            // ignore exception, display the testObject
        }
        return true;
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
     * Creates a new ui controller for the given object.
     */
    private IpsObjectUIController createUIController(IIpsObjectPart part) {
        IpsObjectUIController controller = new IpsObjectUIController(part) {
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
        allEditFields.clear();
        firstAttributeEditFields.clear();
        sectionControls.clear();
        uiControllers.clear();
        editField2ModelObject.clear();
    }

    /**
     * Clears the detail area.
     */
    public void clearDetailArea() {
        if (dynamicArea != null) {
            dynamicArea.dispose();
        }

        dynamicArea = toolkit.getFormToolkit().createComposite(detailsArea);
        dynamicArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout detailLayout = new GridLayout(1, true);
        detailLayout.horizontalSpacing = 0;
        detailLayout.marginWidth = 0;
        detailLayout.marginHeight = 0;
        dynamicArea.setLayout(detailLayout);

        resetContainers();
    }

    /**
     * Packs the detail area.
     */
    public void pack(){
        dynamicArea.pack();
    }
    
    /**
     * Mark the test attribute value field or test value field - which is identified by the given
     * key - as failure.
     * Returns <code>true</code> if the field was found otherwise <code>false</code>.
     * 
     * @param failureDetails2
     */
    boolean markEditFieldAsFailure(String editFieldUniqueKey, String failureMessage, String[] failureDetails2) {
        failureMessageCache.put(editFieldUniqueKey, failureMessage);
        failureDetailCache.put(editFieldUniqueKey, failureDetails2);
        EditField editField = (EditField)allEditFields.get(editFieldUniqueKey);
        if (editField == null) {
            // edit field not found, try to get the special edit value field
            editField = (EditField)allEditFields.get(TestCaseSection.VALUESECTION + editFieldUniqueKey);
        }
        if (editField != null) {
            if (fixedFieldsCache.contains(editFieldUniqueKey)){
                testCaseSection.postSetOverriddenValueBackgroundAndToolTip(editField, failureMessage, false);
            } else {
                testCaseSection.postSetFailureBackgroundAndToolTip(editField, failureMessage);
            }
            return true;
        }
        return false;
    }

    void setFocusOnEditField(String editFieldUniqueKey){
        EditField editField = (EditField)allEditFields.get(editFieldUniqueKey);
        if (editField == null) {
            // edit field not found, try to get the special edit value field
            editField = (EditField)allEditFields.get(TestCaseSection.VALUESECTION + editFieldUniqueKey);
        }
        if (editField != null) {
            editField.getControl().setFocus();
        }
    }
    
    
    /**
     * Stores the given actual value as expected result.
     */
    boolean storeActualValueInExpResult(String editFieldUniqueKey, String actualValue, String message) {
        if (! testCaseSection.isDataChangeable()){
            return false;
        }
        EditField editField = (EditField)allEditFields.get(editFieldUniqueKey);
        if (editField == null) {
            // edit field not found, try to get the special edit value field
            editField = (EditField)allEditFields.get(TestCaseSection.VALUESECTION + editFieldUniqueKey);
        }
        if (editField != null) {
            fixedFieldsCache.add(editFieldUniqueKey);
            updateValue(editField, actualValue);
            testCaseSection.postSetOverriddenValueBackgroundAndToolTip(editField, message, true);
            return true;
        }
        return false;
    }

    private void updateValue(EditField editField, String actualValue) {
        IIpsObjectPart object = (IIpsObjectPart)editField2ModelObject.get(editField);
        if (object != null) {
            if (object instanceof ITestValue) {
                ((ITestValue)object).setValue(actualValue);
            } else if (object instanceof ITestAttributeValue) {
                ((ITestAttributeValue)object).setValue(actualValue);
            } else if (object instanceof ITestRule) {
                ((ITestRule)object).setViolationType((TestRuleViolationType)TestRuleViolationType.getEnumType()
                        .getEnumValue(actualValue));
            }
        }
    }

    /**
     * Resets the test run, clear failure cache.
     */
    public void resetTestRun(boolean clearFixedValueState) {
        failureMessageCache.clear();
        failureDetailCache.clear();
        if (clearFixedValueState){
            fixedFieldsCache.clear();
        }
    }

    /*
     * Adds a listener to mark the section as selected if the given edit field gets the focus
     */
    private void addSectionSelectionListeners(EditField editField, Control label, final IIpsObjectPart object) {
        if (editField != null) {
            editField.getControl().addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    if (object instanceof ITestPolicyCmpt)
                        testCaseSection.selectInTreeByObject((ITestPolicyCmpt)object, false);
                    else if (object instanceof ITestPolicyCmptRelation)
                        testCaseSection.selectInTreeByObject((ITestPolicyCmptRelation)object, false);
                }
            });
            editField.getControl().addMouseListener(new SectionSelectMouseListener(object));
        }
        if (label != null) {
            label.addMouseListener(new SectionSelectMouseListener(object));
        }
    }

    /**
     * Updates the ui for all ui controller
     */
    void updateUi() {
        for (Iterator iter = uiControllers.iterator(); iter.hasNext();) {
            UIController controller = (UIController)iter.next();
            controller.updateUI();
        }
    }
}
