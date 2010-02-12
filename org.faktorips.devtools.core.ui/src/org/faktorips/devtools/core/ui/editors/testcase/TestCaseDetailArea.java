/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.StringDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.core.model.testcase.ITestRule;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcase.TestCaseHierarchyPath;
import org.faktorips.devtools.core.model.testcase.TestRuleViolationType;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueField;

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
    private HashMap<String, Section> sectionControls = new HashMap<String, Section>();

    // Container holds all edit fields for test values and test attribute values
    private HashMap<String, EditField> allEditFieldsCache = new HashMap<String, EditField>();

    // Contains the first edit field of each test policy component in the edit area
    private HashMap<String, EditField> firstAttributeEditFields = new HashMap<String, EditField>();

    // Contains the failures of the last test run
    private HashMap<String, String> failureMessageCache = new HashMap<String, String>();
    private HashMap<String, String[]> failureDetailCache = new HashMap<String, String[]>();
    // Contains all fixed fields (actual value stored as expected value)
    private List<String> fixedFieldsCache = new ArrayList<String>();

    // Contains the mapping between the edit field and model objects
    private HashMap<EditField, IIpsObjectPart> editField2ModelObject = new HashMap<EditField, IIpsObjectPart>();

    // Contains all ui controller
    private List<Control> allBindedControls = new ArrayList<Control>();

    // The section this details belongs to
    private TestCaseSection testCaseSection;

    // Composites to change the UI
    // area which contains the dynamic detail controls
    private Composite detailsArea;
    // area which contains alls detail controls
    private Composite dynamicArea;

    private List<ITestCaseDetailAreaRedrawListener> testCaseDetailAreaRedrawListener = new ArrayList<ITestCaseDetailAreaRedrawListener>(
            1);

    private BindingContext bindingContext;

    /*
     * Mouse listener class to select the section if the mouse button is clicked
     */
    private class SectionSelectMouseListener implements MouseListener {
        private ITestPolicyCmpt testPolicyCmptType;
        private ITestPolicyCmptLink testPolicyCmptTypeLink;
        private ITestObject testObject;

        public SectionSelectMouseListener(IIpsObjectPart object) {
            if (object instanceof ITestPolicyCmpt) {
                testPolicyCmptType = (ITestPolicyCmpt)object;
            } else if (object instanceof ITestPolicyCmptLink) {
                testPolicyCmptTypeLink = (ITestPolicyCmptLink)object;
            } else if (object instanceof ITestObject) {
                testObject = (ITestObject)object;
            }
        }

        public SectionSelectMouseListener(ITestPolicyCmptLink testPolicyCmptTypeLink) {
            this.testPolicyCmptTypeLink = testPolicyCmptTypeLink;
        }

        public void mouseDown(MouseEvent e) {
            if (testPolicyCmptType != null) {
                testCaseSection.selectInTreeByObject(testPolicyCmptType, false);
                testCaseSection.selectInDetailArea(testPolicyCmptType, false);
            } else if (testPolicyCmptTypeLink != null) {
                testCaseSection.selectInTreeByObject(testPolicyCmptTypeLink, false);
                testCaseSection.selectInDetailArea(testPolicyCmptTypeLink, false);
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

    public TestCaseDetailArea(UIToolkit toolkit, TestCaseContentProvider contentProvider, TestCaseSection section,
            BindingContext bindingContext) {
        this.toolkit = toolkit;
        this.contentProvider = contentProvider;
        ipsProject = contentProvider.getTestCase().getIpsProject();
        testCaseSection = section;
        this.bindingContext = bindingContext;
    }

    /**
     * Resets the color of all detail sections.
     */
    public void resetSectionColors(ScrolledForm form) {
        Iterator<Section> iter = sectionControls.values().iterator();
        while (iter.hasNext()) {
            Section section = iter.next();
            section.setBackground(form.getBackground());
        }
    }

    /**
     * Returns the attribute edit fields given by the unique key.
     */
    public EditField getFirstAttributeEditField(String uniqueKey) {
        return firstAttributeEditFields.get(uniqueKey);
    }

    /**
     * Returns the test value edit fields given by the unique key.
     */
    public EditField getTestValueEditField(String uniqueKey) {
        return getEditField(uniqueKey);
    }

    /**
     * Returns the section given by the unique key.
     */
    public Section getSection(String uniqueKey) {
        return sectionControls.get(uniqueKey);
    }

    /**
     * Creates the main detail area.
     */
    public Section createInitialDetailArea(Composite parent, String title) {
        Section detailsSection = toolkit.getFormToolkit().createSection(parent, ExpandableComposite.TITLE_BAR);
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
        return detailsSection;
    }

    public void addDetailAreaRedrawListener(ITestCaseDetailAreaRedrawListener listener) {
        testCaseDetailAreaRedrawListener.add(listener);
    }

    public void removeDetailAreaRedrawListener(ITestCaseDetailAreaRedrawListener listener) {
        testCaseDetailAreaRedrawListener.remove(listener);
    }

    private void notifyListener(List<ITestObject> testObjects) {
        try {
            for (Iterator<ITestCaseDetailAreaRedrawListener> iter = testCaseDetailAreaRedrawListener.iterator(); iter
                    .hasNext();) {
                ITestCaseDetailAreaRedrawListener listener = iter.next();
                listener.visibleTestObjectsChanges(testObjects);
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /**
     * Creates the details for the given test objects.
     */
    public void createTestObjectSections(List<ITestObject> testObjects) {
        try {
            notifyListener(testObjects);

            for (Iterator<ITestObject> iter = testObjects.iterator(); iter.hasNext();) {
                ITestObject testObject = iter.next();
                if (testObject instanceof ITestValue) {
                    Composite borderedComosite = createBorderComposite(dynamicArea);
                    createTestValuesSection((ITestValue)testObject, borderedComosite);
                } else if (testObject instanceof ITestRule) {
                    Composite borderedComosite = createBorderComposite(dynamicArea);
                    createTestRuleSection((ITestRule)testObject, borderedComosite);
                } else if (testObject instanceof ITestPolicyCmpt) {
                    Composite borderedComosite = createBorderComposite(dynamicArea);
                    createPolicyCmptAndLinkSection((ITestPolicyCmpt)testObject, borderedComosite);
                }
            }

            if (!testCaseSection.isDataChangeable()) {
                toolkit.setDataChangeable(detailsArea, false);
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates the section with the test policy component object.<br>
     * If the element is a child then the link name could be given as input to display it in the
     * section title beside the test policy component.
     * 
     * @throws CoreException
     */
    private void createPolicyCmptSection(final ITestPolicyCmpt testPolicyCmpt, Composite details) throws CoreException {
        if (testPolicyCmpt == null || details.isDisposed()) {
            return;
        }
        String uniqueKey = testCaseSection.getUniqueKey(testPolicyCmpt);

        if (!((testCaseSection.getContentProvider().isExpectedResult() && testPolicyCmpt.isExpectedResult()) || (testCaseSection
                .getContentProvider().isInput() && testPolicyCmpt.isInput()))) {
            // check if the parameter wasn't found
            // if the parameter not exists then the type couldn't be determined, therefore display
            // the content in any case
            if (testPolicyCmpt.findTestPolicyCmptTypeParameter(ipsProject) != null) {
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
                    || (testCaseSection.getContentProvider().isInput() && testAttributeValues[i]
                            .isInputAttribute(ipsProject)) || testCaseSection.getContentProvider().isExpectedResult()
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
        EditField editField = null;

        // get the ctrlFactory to create the edit field
        ValueDatatype datatype = null;
        ValueDatatypeControlFactory ctrlFactory = null;

        try {
            ITestAttribute testAttribute = attributeValue.findTestAttribute(ipsProject);
            if (testAttribute != null && !testAttribute.isBasedOnModelAttribute()) {
                // the attribute is an extension attribute
                datatype = testAttribute.findDatatype(ipsProject);
                ctrlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(datatype);
            } else {
                IAttribute attribute = attributeValue.findAttribute(ipsProject);
                if (attribute != null) {
                    datatype = attribute.findDatatype(ipsProject);
                    ctrlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(datatype);
                } else {
                    if (StringUtils.isNotEmpty(attributeValue.getValue())) {
                        ctrlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(Datatype.STRING);
                    } else {
                        // if the attribute wasn't found and no value is stored then no controls
                        // will be displayed
                        // maybe this attributes are not available in subtype test policy cmpt's
                        return null;
                    }
                }
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }

        if (ctrlFactory == null) {
            ctrlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(Datatype.STRING);
        }

        Label label = toolkit.createFormLabel(attributeComposite, StringUtils.capitalize(attributeValue
                .getTestAttribute()));
        addSectionSelectionListeners(null, label, testPolicyCmptForSelection);

        try {
            editField = ctrlFactory.createEditField(toolkit, attributeComposite, datatype, null, ipsProject);
        } catch (Exception e) {
            // ignore exception
        }

        // store the edit field
        String testPolicyCmptTypeParamPath = TestCaseHierarchyPath.evalTestPolicyCmptParamPath(testPolicyCmpt);
        putEditField(testPolicyCmptTypeParamPath + attributeValue.getTestAttribute(), editField);
        editField2ModelObject.put(editField, attributeValue);
        addSectionSelectionListeners(editField, null, testPolicyCmptForSelection);

        // mark as expected result
        if (attributeValue.isExpextedResultAttribute(ipsProject)) {
            markAsExpected(editField);
        }
        // mark as failure
        String failureLastTestRun = failureMessageCache.get(testPolicyCmptTypeParamPath
                + attributeValue.getTestAttribute());
        if (failureLastTestRun != null) {
            if (!fixedFieldsCache.contains(testPolicyCmptTypeParamPath + attributeValue.getTestAttribute())) {
                testCaseSection.postSetFailureBackgroundAndToolTip(editField, failureLastTestRun);
                // create context menu
                String[] failureDetails = failureDetailCache.get(testPolicyCmptTypeParamPath
                        + attributeValue.getTestAttribute());
                if (failureDetails != null) {
                    testCaseSection.postAddExpectedResultContextMenu(editField.getControl(), failureDetails);
                }
            } else {
                testCaseSection.postSetOverriddenValueBackgroundAndToolTip(editField, failureLastTestRun, false);
            }
        }

        addBindingFor(editField, attributeValue, ITestAttributeValue.PROPERTY_VALUE);

        return editField;
    }

    /*
     * Marks the given edit field as expected result.
     */
    private void markAsExpected(final EditField editField) {
        editField.getControl().setBackground(testCaseSection.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
    }

    /*
     * Creates the section for a link of type association.<br> Create a hyperlink if the realtion
     * exists is in the current test case or create a label with the test link target.
     */
    private void createLinkSectionAssociation(final ITestPolicyCmptLink currLink, Composite details) {
        String uniquePath = testCaseSection.getUniqueKey(currLink);

        Section section = toolkit.getFormToolkit().createSection(details, 0);
        section.setText(testCaseSection.getLabelProvider().getTextForSection(currLink));
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        // create separator line
        toolkit.getFormToolkit().createCompositeSeparator(section);

        section.addMouseListener(new SectionSelectMouseListener(currLink));
        section.getChildren()[0].addMouseListener(new SectionSelectMouseListener(currLink));

        Composite hyperlinkArea = toolkit.createGridComposite(details, 2, false, true);
        sectionControls.put(uniquePath, section);

        // create a hyperlink to the target
        ITestPolicyCmpt target = null;
        try {
            target = contentProvider.getTestCase().findTestPolicyCmpt(currLink.getTarget());
        } catch (CoreException e2) {
            // ignore the exception, error searching for the target
        }
        if (target != null) {
            Hyperlink linkHyperlink = toolkit.getFormToolkit().createHyperlink(hyperlinkArea,
                    TestCaseHierarchyPath.unqualifiedName(currLink.getTarget()), SWT.WRAP);
            linkHyperlink.addHyperlinkListener(new HyperlinkAdapter() {
                @Override
                public void linkActivated(HyperlinkEvent e) {
                    try {
                        testCaseSection.selectInTreeByObject(currLink.findTarget(), true);
                    } catch (CoreException e1) {
                        throw new RuntimeException(e1);
                    }
                }
            });
            linkHyperlink.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {

                    testCaseSection.selectInTreeByObject(currLink, false);
                }

                public void focusLost(FocusEvent e) {
                }
            });
            String hyperLinkPath = " (" + testCaseSection.getLabelProvider().getAssoziationTargetLabel(currLink.getTarget()) + " ) "; //$NON-NLS-1$ //$NON-NLS-2$
            String hyperLinklabel = hyperLinkPath;
            if (hyperLinklabel.length() > 60) {
                hyperLinklabel = hyperLinkPath.substring(0, 27);
                hyperLinklabel += "..."; //$NON-NLS-1$
                hyperLinklabel += hyperLinkPath.substring(hyperLinkPath.length() - 30);
            }
            Label label = toolkit.createLabel(hyperlinkArea, hyperLinklabel);
            addSectionSelectionListeners(null, label, currLink);
        } else {
            // target not found in current test case
            Label label = toolkit.createLabel(hyperlinkArea, TestCaseHierarchyPath
                    .unqualifiedName(currLink.getTarget()));
            addSectionSelectionListeners(null, label, currLink);
            label = toolkit.createLabel(hyperlinkArea,
                    " (" + testCaseSection.getLabelProvider().getAssoziationTargetLabel(currLink.getTarget()) + " ) "); //$NON-NLS-1$ //$NON-NLS-2$
            addSectionSelectionListeners(null, label, currLink);
        }
    }

    /**
     * Recursive create the sections for the links and all their childs.
     * 
     * @throws CoreException
     */
    private void createPolicyCmptAndLinkSection(ITestPolicyCmpt currTestPolicyCmpt, Composite details)
            throws CoreException {
        createPolicyCmptSection(currTestPolicyCmpt, details);
        ITestPolicyCmptLink[] links = currTestPolicyCmpt.getTestPolicyCmptLinks();
        for (int i = 0; i < links.length; i++) {
            ITestPolicyCmptLink currLink = links[i];
            if (currLink.isComposition()) {
                try {
                    ITestPolicyCmpt policyCmpt = currLink.findTarget();
                    if (policyCmpt != null) {
                        createPolicyCmptAndLinkSection(policyCmpt, details);
                    }
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            } else {
                // link is an association
                createLinkSectionAssociation(currLink, details);
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
                ctrlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(datatype);
            } else {
                ctrlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(new StringDatatype());
            }
        } catch (CoreException e1) {
            throw new RuntimeException(e1);
        }

        Label label = toolkit.createFormLabel(composite, Messages.TestCaseDetailArea_Label_Value);
        final EditField editField = ctrlFactory.createEditField(toolkit, composite, datatype, null, ipsProject);
        addSectionSelectionListeners(editField, label, testValue);

        addBindingFor(editField, testValue, ITestValue.PROPERTY_VALUE);

        editField.getControl().addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                testCaseSection.selectTestObjectInTree(testValue);
            }
        });

        putEditField(uniquePath, editField);
        editField2ModelObject.put(editField, testValue);

        // mark as expected result
        if (testValue.isExpectedResult()) {
            markAsExpected(editField);
        }
        // mark as failure
        String failureLastTestRun = failureMessageCache.get(testValue.getTestValueParameter());
        if (failureLastTestRun != null) {
            testCaseSection.postSetFailureBackgroundAndToolTip(editField, failureLastTestRun);
        }
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

        editField.getControl().addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                testCaseSection.selectInTreeByObject(rule);
            }
        });

        putEditField(uniqueKey, editField);
        editField2ModelObject.put(editField, rule);

        // mark as expected result
        if (rule.isExpectedResult()) {
            markAsExpected(editField);
        }
        // mark as failure
        String failureLastTestRun = failureMessageCache.get(testCaseSection.getUniqueKey(rule));
        if (failureLastTestRun != null) {
            testCaseSection.postSetFailureBackgroundAndToolTip(editField, failureLastTestRun);
        }

        addBindingFor(editField, rule, ITestRule.PROPERTY_VIOLATED);
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
        if (parent.isDisposed()) {
            return null;
        }
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
     * Resets the containers containing the control references.
     */
    private void resetContainers() {
        unbindControls();
        allEditFieldsCache.clear();
        firstAttributeEditFields.clear();
        sectionControls.clear();
        editField2ModelObject.clear();
    }

    /**
     * Clears the detail area.
     */
    public void clearDetailArea() {
        if (detailsArea.isDisposed()) {
            return;
        }

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
    public void pack() {
        dynamicArea.pack();
    }

    /**
     * Mark the test attribute value field or test value field - which is identified by the given
     * key - as failure. Returns <code>true</code> if the field was found otherwise
     * <code>false</code>.
     * 
     * @param failureDetails2
     */
    boolean markEditFieldAsFailure(String editFieldUniqueKey, String failureMessage, String[] failureDetails2) {
        failureMessageCache.put(editFieldUniqueKey, failureMessage);
        failureDetailCache.put(editFieldUniqueKey, failureDetails2);
        EditField editField = getEditField(editFieldUniqueKey);
        if (editField == null) {
            // edit field not found, try to get the special edit value field
            editField = getEditField(TestCaseSection.VALUESECTION + editFieldUniqueKey);
        }
        if (editField != null) {
            if (fixedFieldsCache.contains(editFieldUniqueKey)) {
                testCaseSection.postSetOverriddenValueBackgroundAndToolTip(editField, failureMessage, false);
            } else {
                testCaseSection.postSetFailureBackgroundAndToolTip(editField, failureMessage);
            }
            return true;
        }
        return false;
    }

    void setFocusOnEditField(String editFieldUniqueKey) {
        EditField editField = getEditField(editFieldUniqueKey);
        if (editField == null) {
            // edit field not found, try to get the special edit value field
            editField = getEditField(TestCaseSection.VALUESECTION + editFieldUniqueKey);
        }
        if (editField != null) {
            editField.getControl().setFocus();
        }
    }

    /**
     * Stores the given actual value as expected result.
     */
    boolean storeActualValueInExpResult(String editFieldUniqueKey, String actualValue, String message) {
        if (!testCaseSection.isDataChangeable()) {
            return false;
        }
        EditField editField = getEditField(editFieldUniqueKey);
        if (editField == null) {
            // edit field not found, try to get the special edit value field
            editField = getEditField(TestCaseSection.VALUESECTION + editFieldUniqueKey);
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
        IIpsObjectPart object = editField2ModelObject.get(editField);
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
        if (clearFixedValueState) {
            fixedFieldsCache.clear();
        }
    }

    /*
     * Adds a listener to mark the section as selected if the given edit field gets the focus
     */
    private void addSectionSelectionListeners(EditField editField, Control label, final IIpsObjectPart object) {
        if (editField != null) {
            editField.getControl().addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (object instanceof ITestPolicyCmpt) {
                        testCaseSection.selectInTreeByObject((ITestPolicyCmpt)object, false);
                    } else if (object instanceof ITestPolicyCmptLink) {
                        testCaseSection.selectInTreeByObject((ITestPolicyCmptLink)object, false);
                    }
                }
            });
            editField.getControl().addMouseListener(new SectionSelectMouseListener(object));
        }
        if (label != null) {
            label.addMouseListener(new SectionSelectMouseListener(object));
        }
    }

    /*
     * Remove binding of all controls
     */
    private void unbindControls() {
        for (Iterator<Control> iter = allBindedControls.iterator(); iter.hasNext();) {
            Control control = iter.next();
            bindingContext.removeBindings(control);
        }
        allBindedControls.clear();
    }

    /*
     * Adds the given object to the binding context
     */
    private void addBindingFor(EditField editField, Object object, String property) {
        bindingContext.bindContent(editField, object, property);
        allBindedControls.add(editField.getControl());
    }

    /*
     * Stores the edit field which is identified by the given unique key.
     */
    private void putEditField(String key, EditField editField) {
        allEditFieldsCache.put(key.toUpperCase(), editField);
    }

    /**
     * Returns the edit field which is identified by the given unique key.
     */
    public EditField getEditField(String uniqueKey) {
        EditField editField = allEditFieldsCache.get(uniqueKey);
        if (editField == null) {
            // edit field not found, try to get the special edit value field
            editField = allEditFieldsCache.get((TestCaseSection.VALUESECTION + uniqueKey).toUpperCase());
        }
        return editField;
    }
}
