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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.ScrolledComposite;
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
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.EnumField;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.testcase.TestCaseHierarchyPath;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.model.testcase.ITestObject;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.model.testcase.ITestRule;
import org.faktorips.devtools.model.testcase.ITestValue;
import org.faktorips.devtools.model.testcase.TestRuleViolationType;
import org.faktorips.devtools.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.model.testcasetype.ITestRuleParameter;
import org.faktorips.devtools.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Detail section class of the test case editor. Supports dynamic creation of detail edit controls.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseDetailArea {

    /** UI toolkit for creating the controls */
    private UIToolkit toolkit;

    /** Contains the content provider of the test policy component object */
    private TestCaseContentProvider contentProvider;

    private IIpsProject ipsProject;

    /** Contains all edit sections the key is the name of the corresponding test parameter */
    private HashMap<String, Section> sectionControls = new HashMap<>();

    /** Container holds all edit fields for test values and test attribute values */
    private HashMap<String, EditField<?>> allEditFieldsCache = new HashMap<>();

    /** Contains the first edit field of each test policy component in the edit area */
    private HashMap<String, EditField<?>> firstAttributeEditFields = new HashMap<>();

    private HashMap<String, String> failureMessageCache = new HashMap<>();
    private HashMap<String, String[]> failureDetailCache = new HashMap<>();

    /** Contains all fixed fields (actual value stored as expected value) */
    private List<String> fixedFieldsCache = new ArrayList<>();

    /** Contains the mapping between the edit field and model objects */
    private HashMap<EditField<?>, IIpsObjectPart> editField2ModelObject = new HashMap<>();

    /** Contains all ui controller */
    private List<Control> allBindedControls = new ArrayList<>();

    /** The section this details belongs to */
    private TestCaseSection testCaseSection;

    private final String nullRepresentation = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();

    /**
     * Composites to change the UI area which contains the dynamic detail controls
     */
    private Composite detailsArea;

    /** area which contains all detail controls */
    private Composite dynamicArea;

    private List<ITestCaseDetailAreaRedrawListener> testCaseDetailAreaRedrawListener = new ArrayList<>(
            1);

    private BindingContext bindingContext;

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
        for (Section section : sectionControls.values()) {
            try {
                if (!section.isDisposed()) {
                    section.setBackground(form.getBackground());
                }
            } catch (SWTException e) {
                // ignore
            }

        }
    }

    /**
     * Returns the attribute edit fields given by the unique key.
     */
    public EditField<?> getFirstAttributeEditField(String uniqueKey) {
        return firstAttributeEditFields.get(uniqueKey);
    }

    /**
     * Returns the test value edit fields given by the unique key.
     */
    public EditField<?> getTestValueEditField(String uniqueKey) {
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
        detailsArea = toolkit.createGridComposite(detailsSection, 1, false, true);
        detailsSection.setClient(detailsArea);
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
            for (ITestCaseDetailAreaRedrawListener listener : testCaseDetailAreaRedrawListener) {
                listener.visibleTestObjectsChanges(testObjects);
            }
        } catch (IpsException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /**
     * Creates the details for the given test objects.
     */
    public void createTestObjectSections(List<ITestObject> testObjects) {
        notifyListener(testObjects);

        ScrolledComposite scrolledComposite = createScrolledComposite(dynamicArea);
        Composite container = new Composite(scrolledComposite, SWT.NONE);
        container.setBackground(detailsArea.getBackground());
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout(1, false);
        container.setLayout(layout);
        for (ITestObject testObject : testObjects) {
            if (testObject instanceof ITestValue) {
                Composite borderedComosite = createBorderComposite(container);
                createTestValuesSection((ITestValue)testObject, borderedComosite);
            } else if (testObject instanceof ITestRule) {
                Composite borderedComosite = createBorderComposite(container);
                createTestRuleSection((ITestRule)testObject, borderedComosite);
            } else if (testObject instanceof ITestPolicyCmpt) {
                Composite borderedComosite = createBorderComposite(container);
                createPolicyCmptAndLinkSection((ITestPolicyCmpt)testObject, borderedComosite);
            }
        }
        scrolledComposite.setContent(container);
        scrolledComposite.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        if (!testCaseSection.isDataChangeable()) {
            toolkit.setDataChangeable(detailsArea, false);
        }
    }

    private ScrolledComposite createScrolledComposite(Composite parent) {
        if (parent.isDisposed()) {
            return null;
        }
        final ScrolledComposite scrolledComposite = new ScrolledComposite(parent,
                SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        scrolledComposite.setShowFocusedControl(true);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        applyHeight(scrolledComposite);
        testCaseSection.getTreeViewer().getTree().addListener(SWT.Resize, $ -> {
            if (!scrolledComposite.isDisposed()) {
                applyHeight(scrolledComposite);
                testCaseSection.layout();
            }
        });

        return scrolledComposite;
    }

    private void applyHeight(ScrolledComposite scrolledComposite) {
        GridDataFactory dataFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL)
                .hint(SWT.DEFAULT, computeScrolledCompositeHeight()).grab(true, true);
        dataFactory.applyTo(scrolledComposite);
    }

    private int computeScrolledCompositeHeight() {
        // TODO AW 04-01-2012: I don't know where the 25 comes from
        return testCaseSection.getTreeViewer().getTree().getSize().y - 25;
    }

    /**
     * Creates the section with the test policy component object.<br>
     * If the element is a child then the link name could be given as input to display it in the
     * section title beside the test policy component.
     */
    private void createPolicyCmptSection(final ITestPolicyCmpt testPolicyCmpt, Composite details) {
        if (testPolicyCmpt == null || details.isDisposed()) {
            return;
        }
        String uniqueKey = testCaseSection.getUniqueKey(testPolicyCmpt);

        if (!((testCaseSection.getContentProvider().isExpectedResult() && testPolicyCmpt.isExpectedResult())
                || (testCaseSection.getContentProvider().isInput() && testPolicyCmpt.isInput()))) {
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
        createAttributeEditFields(testPolicyCmpt, uniqueKey, attributeComposite);
        section.setClient(attributeComposite);

        toolkit.createVerticalSpacer(details, 10).setBackground(details.getBackground());
    }

    private void createAttributeEditFields(final ITestPolicyCmpt testPolicyCmpt,
            String uniqueKey,
            Composite attributeComposite) {
        ITestAttributeValue[] testAttributeValues = testPolicyCmpt.getTestAttributeValues();
        boolean firstEditField = true;
        for (final ITestAttributeValue attributeValue : testAttributeValues) {
            // Create the edit field only if the content provider provides the type of the test
            // attribute object
            if (isResultAndInputAttribute(attributeValue.isInputAttribute(ipsProject),
                    attributeValue.isExpectedResultAttribute(ipsProject))) {
                EditField<?> editField = createAttributeEditField(testPolicyCmpt, testPolicyCmpt, attributeComposite,
                        attributeValue);

                // store the first attribute of each policy cmpt for fast focus setting
                if (editField != null && firstEditField) {
                    firstAttributeEditFields.put(uniqueKey, editField);
                    firstEditField = false;
                }
            }
        }
    }

    private boolean isResultAndInputAttribute(boolean isInputAttribute, boolean isExpectedResultAttribute) {
        TestCaseContentProvider testCaseContentProvider = testCaseSection.getContentProvider();
        boolean isInput = testCaseContentProvider.isInput() && isInputAttribute;
        boolean isExpectedResult = testCaseContentProvider.isExpectedResult() && isExpectedResultAttribute;
        return testCaseContentProvider.isCombined() || isInput || isExpectedResult;
    }

    private EditField<?> createAttributeEditField(final ITestPolicyCmpt testPolicyCmpt,
            final ITestPolicyCmpt testPolicyCmptForSelection,
            Composite attributeComposite,
            final ITestAttributeValue attributeValue) {

        // get the ctrlFactory to create the edit field
        ITestAttribute testAttribute = attributeValue.findTestAttribute(ipsProject);
        IAttribute attribute = attributeValue.findAttribute(ipsProject);

        if (testAttribute == null || testAttribute.isBasedOnModelAttribute()) {
            if (attribute == null && IpsStringUtils.isEmpty(attributeValue.getValue())) {
                return null;
            }
        }

        ValueDatatype datatype = findDatatype(attributeValue, testAttribute);
        ValueDatatypeControlFactory ctrlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(datatype);

        Label label = toolkit.createFormLabel(attributeComposite,
                StringUtils.capitalize(attributeValue.getTestAttribute()));

        if (testAttribute != null) {
            String localizedDescription = IIpsModel.get().getMultiLanguageSupport()
                    .getLocalizedDescription(testAttribute);
            label.setToolTipText(localizedDescription);
        }

        addSectionSelectionListeners(null, label, testPolicyCmptForSelection);

        EditField<?> editField = createEditField(attributeComposite, ctrlFactory, datatype, attribute);
        storeAndMarkEditField(testPolicyCmpt, testPolicyCmptForSelection, attributeValue, editField);
        addBindingFor(editField, attributeValue, ITestAttributeValue.PROPERTY_VALUE);

        return editField;
    }

    private ValueDatatype findDatatype(final ITestAttributeValue attributeValue, ITestAttribute testAttribute) {
        if (testAttribute != null && !testAttribute.isBasedOnModelAttribute()) {
            // the attribute is an extension attribute
            return testAttribute.findDatatype(ipsProject);
        } else {
            IAttribute attribute = attributeValue.findAttribute(ipsProject);
            if (attribute != null) {
                return attribute.findDatatype(ipsProject);
            } else {
                if (IpsStringUtils.isNotEmpty(attributeValue.getValue())) {
                    return Datatype.STRING;
                }
            }
        }
        return null;
    }

    private EditField<?> createEditField(Composite attributeComposite,
            ValueDatatypeControlFactory ctrlFactory,
            ValueDatatype datatype,
            IAttribute attribute) {
        EditField<?> editField = null;
        if (attribute != null) {
            IValueSet valueSet = attribute.getValueSet();
            editField = ctrlFactory.createEditField(toolkit, attributeComposite, datatype, valueSet, ipsProject);
        } else {
            editField = ctrlFactory.createEditField(toolkit, attributeComposite, datatype, null, ipsProject);
        }
        return editField;
    }

    private void storeAndMarkEditField(final ITestPolicyCmpt testPolicyCmpt,
            final ITestPolicyCmpt testPolicyCmptForSelection,
            final ITestAttributeValue attributeValue,
            EditField<?> editField) {
        String testPolicyCmptTypeParamPath = TestCaseHierarchyPath.evalTestPolicyCmptParamPath(testPolicyCmpt);
        // store the edit field
        putEditField(testPolicyCmptTypeParamPath + attributeValue.getTestAttribute(), editField);
        editField2ModelObject.put(editField, attributeValue);
        addSectionSelectionListeners(editField, null, testPolicyCmptForSelection);

        // mark as expected result
        if (attributeValue.isExpectedResultAttribute(ipsProject)) {
            markAsExpected(editField);
        }
        // mark as failure
        markAsFailure(attributeValue, editField, testPolicyCmptTypeParamPath);
    }

    private void markAsFailure(final ITestAttributeValue attributeValue,
            EditField<?> editField,
            String testPolicyCmptTypeParamPath) {
        String failureLastTestRun = failureMessageCache
                .get(testPolicyCmptTypeParamPath + attributeValue.getTestAttribute());
        if (failureLastTestRun != null) {
            if (!fixedFieldsCache.contains(testPolicyCmptTypeParamPath + attributeValue.getTestAttribute())) {
                testCaseSection.postSetFailureBackgroundAndToolTip(editField, failureLastTestRun);
                // create context menu
                String[] failureDetails = failureDetailCache
                        .get(testPolicyCmptTypeParamPath + attributeValue.getTestAttribute());
                if (failureDetails != null) {
                    testCaseSection.postAddExpectedResultContextMenu(editField.getControl(), failureDetails);
                }
            } else {
                testCaseSection.postSetOverriddenValueBackgroundAndToolTip(editField, failureLastTestRun, false);
            }
        }
    }

    /**
     * Marks the given edit field as expected result.
     */
    private void markAsExpected(final EditField<?> editField) {
        editField.getControl().setBackground(testCaseSection.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
    }

    /**
     * Creates the section for a link of type association.<br>
     * Create a hyperlink if the realtion exists is in the current test case or create a label with
     * the test link target.
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
        ITestPolicyCmpt target = contentProvider.getTestCase().findTestPolicyCmpt(currLink.getTarget());
        if (target != null) {
            Hyperlink linkHyperlink = toolkit.getFormToolkit().createHyperlink(hyperlinkArea,
                    TestCaseHierarchyPath.unqualifiedName(currLink.getTarget()), SWT.WRAP);
            linkHyperlink.addHyperlinkListener(new HyperlinkAdapter() {
                @Override
                public void linkActivated(HyperlinkEvent e) {
                    testCaseSection.selectInTreeByObject(currLink.findTarget(), true);
                }
            });
            linkHyperlink.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {

                    testCaseSection.selectInTreeByObject(currLink, false);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    // Nothing to do
                }
            });
            String hyperLinkPath = " (" //$NON-NLS-1$
                    + testCaseSection.getLabelProvider().getAssoziationTargetLabel(currLink.getTarget()) + " ) "; //$NON-NLS-1$
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
            Label label = toolkit.createLabel(hyperlinkArea,
                    TestCaseHierarchyPath.unqualifiedName(currLink.getTarget()));
            addSectionSelectionListeners(null, label, currLink);
            label = toolkit.createLabel(hyperlinkArea,
                    " (" + testCaseSection.getLabelProvider().getAssoziationTargetLabel(currLink.getTarget()) + " ) "); //$NON-NLS-1$ //$NON-NLS-2$
            addSectionSelectionListeners(null, label, currLink);
        }
    }

    /**
     * Recursive create the sections for the links and all their childs.
     */
    private void createPolicyCmptAndLinkSection(ITestPolicyCmpt currTestPolicyCmpt, Composite details) {

        createPolicyCmptSection(currTestPolicyCmpt, details);
        ITestPolicyCmptLink[] links = currTestPolicyCmpt.getTestPolicyCmptLinks();
        for (ITestPolicyCmptLink currLink : links) {
            if (currLink.isComposition()) {
                try {
                    ITestPolicyCmpt policyCmpt = currLink.findTarget();
                    if (policyCmpt != null) {
                        createPolicyCmptAndLinkSection(policyCmpt, details);
                    }
                } catch (IpsException e) {
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
        if (!isVisibleForContentFilter(testValue)) {
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
        ITestValueParameter param = testValue.findTestValueParameter(ipsProject);
        if (param != null) {
            datatype = param.findValueDatatype(ipsProject);
            ctrlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(datatype);
        } else {
            ctrlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(new StringDatatype());
        }

        Label label = toolkit.createFormLabel(composite, Messages.TestCaseDetailArea_Label_Value);
        if (param != null) {
            // use description of parameter as tooltip
            String localizedDescription = IIpsModel.get().getMultiLanguageSupport().getLocalizedDescription(param);
            label.setToolTipText(localizedDescription);
            section.getChildren()[0].setToolTipText(localizedDescription);
        }

        final EditField<String> editField = ctrlFactory.createEditField(toolkit, composite, datatype, null, ipsProject);
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
        if (!isVisibleForContentFilter(rule)) {
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

        ITestRuleParameter testRuleParameter = null;
        try {
            testRuleParameter = rule.findTestRuleParameter(ipsProject);
        } catch (IpsException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }

        Label label = toolkit.createFormLabel(composite, Messages.TestCaseDetailArea_Label_Violation);
        if (testRuleParameter != null) {
            // use description of parameter as tooltip
            String localizedDescription = IIpsModel.get().getMultiLanguageSupport()
                    .getLocalizedDescription(testRuleParameter);
            label.setToolTipText(localizedDescription);
            section.getChildren()[0].setToolTipText(localizedDescription);
        }

        @SuppressWarnings("deprecation")
        final EditField<?> editField = new EnumField<>(
                toolkit.createCombo(composite, TestRuleViolationType.class),
                TestRuleViolationType.class);
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

    /**
     * Return <code>true</code> if the given test object is visible or not <code>false</code>.
     */
    private boolean isVisibleForContentFilter(ITestObject testObject) {
        return isResultAndInputAttribute(testObject.isInput(), testObject.isExpectedResult());
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
        dynamicArea.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        GridLayout detailLayout = new GridLayout(1, true);
        detailLayout.horizontalSpacing = 0;
        detailLayout.marginWidth = 0;
        detailLayout.marginHeight = 0;
        dynamicArea.setLayout(detailLayout);

        resetContainers();
    }

    /**
     * Mark the test attribute value field or test value field - which is identified by the given
     * key - as failure. Returns <code>true</code> if the field was found otherwise
     * <code>false</code>.
     */
    boolean markEditFieldAsFailure(String editFieldUniqueKey, String failureMessage, String[] failureDetails2) {
        failureMessageCache.put(editFieldUniqueKey, failureMessage);
        failureDetailCache.put(editFieldUniqueKey, failureDetails2);
        EditField<?> editField = getEditField(editFieldUniqueKey);
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
        EditField<?> editField = getEditField(editFieldUniqueKey);
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
        EditField<?> editField = getEditField(editFieldUniqueKey);
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

    private void updateValue(EditField<?> editField, String actualValue) {
        IIpsObjectPart object = editField2ModelObject.get(editField);
        String actualValueToSet = nullIfNullRepresentation(actualValue);
        if (object != null) {
            if (object instanceof ITestValue) {
                ((ITestValue)object).setValue(actualValueToSet);
            } else if (object instanceof ITestAttributeValue) {
                ((ITestAttributeValue)object).setValue(actualValueToSet);
            } else if (object instanceof ITestRule) {
                ((ITestRule)object).setViolationType(
                        TestRuleViolationType.getTestRuleViolationType(actualValue));
            }
        }
    }

    private String nullIfNullRepresentation(String actualValue) {
        return nullRepresentation != null && nullRepresentation.equals(actualValue) ? null : actualValue;
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

    /**
     * Adds a listener to mark the section as selected if the given edit field gets the focus
     */
    private void addSectionSelectionListeners(EditField<?> editField, Control label, final IIpsObjectPart object) {
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

    /**
     * Remove binding of all controls
     */
    private void unbindControls() {
        for (Control control : allBindedControls) {
            bindingContext.removeBindings(control);
        }
        allBindedControls.clear();
    }

    /**
     * Adds the given object to the binding context
     */
    private void addBindingFor(EditField<?> editField, Object object, String property) {
        bindingContext.bindContent(editField, object, property);
        allBindedControls.add(editField.getControl());
    }

    /**
     * Stores the edit field which is identified by the given unique key.
     */
    private void putEditField(String key, EditField<?> editField) {
        allEditFieldsCache.put(key.toUpperCase(), editField);
    }

    /**
     * Returns the edit field which is identified by the given unique key.
     */
    public EditField<?> getEditField(String uniqueKey) {
        EditField<?> editField = allEditFieldsCache.get(uniqueKey);
        if (editField == null) {
            // edit field not found, try to get the special edit value field
            editField = allEditFieldsCache.get((TestCaseSection.VALUESECTION + uniqueKey).toUpperCase());
            if (editField == null && uniqueKey != null) {
                // fallback
                editField = allEditFieldsCache.get(uniqueKey.toUpperCase());
            }
        }
        return editField;
    }

    public void selectSection(String uniquePath) {
        Section sectionCtrl = getSection(uniquePath);
        if (sectionCtrl != null && !sectionCtrl.isDisposed()) {
            try {
                sectionCtrl.setBackground(testCaseSection.getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
            } catch (SWTException e) {
                // ignore
            }
        }
    }

    /**
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

        @Override
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

        @Override
        public void mouseDoubleClick(MouseEvent e) {
            // nothing to do
        }

        @Override
        public void mouseUp(MouseEvent e) {
            // nothing to do
        }
    }
}
