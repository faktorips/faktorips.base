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

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.MessageSeverity;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo.DateTimeMapping;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.binding.ControlPropertyBinding;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.EnumField;
import org.faktorips.devtools.core.ui.controller.fields.EnumTypeDatatypeField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.IntegerField;
import org.faktorips.devtools.core.ui.controller.fields.MessageCueController;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.core.ui.controls.valuesets.ValueSetControlEditMode;
import org.faktorips.devtools.core.ui.controls.valuesets.ValueSetSpecificationControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.core.ui.editors.productcmpttype.ProductCmptTypeMethodEditDialog;
import org.faktorips.devtools.core.util.PersistenceUtil;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.util.memento.Memento;
import org.faktorips.util.message.MessageList;

/**
 * Dialog to edit an attribute.
 * 
 * @author Jan Ortmann
 */
public class AttributeEditDialog extends IpsPartEditDialog2 {

    // the attribute beeing edited.
    private IPolicyCmptTypeAttribute attribute;

    private IIpsProject ipsProject;

    private IValidationRule rule;

    private Text nameText;

    private EditField defaultValueField;

    private ValueSetSpecificationControl valueSetSpecificationControl;
    private DatatypeRefControl datatypeControl;

    private Label labelDefaultValue;

    private ExtensionPropertyControlFactory extFactory;

    private Group configGroup;

    private Checkbox validationRuleAdded;

    /**
     * TextField to link the name input control with the rule name
     */
    private TextField ruleNameField;

    /**
     * TextField to link the message code input control with the rule name
     */
    private TextField msgCodeField;

    /**
     * TextField to link the message text input control with the rule name
     */
    private TextField msgTextField;

    /**
     * TextField to link the message severity input control with the rule name
     */
    private EnumValueField msgSeverityField;

    /**
     * Folder which contains the pages shown by this editor. Used to modify which page is shown.
     */
    private TabFolder tabFolder;

    /**
     * Flag that indicates whether this dialog should startUp with the rule page on top (
     * <code>true</code>) or not.
     */
    private boolean startWithRulePage = false;

    /**
     * Controller to link the part-related input fields to the rule. The default ui-controller can
     * not be used because the default ui-controller is for the attribute and not for the rule.
     */
    private IpsObjectUIController ruleUIController;

    private ValueDatatype currentDatatype;

    private AttributeType currentAttributeType;

    // placeholder for the default edit field, the edit field for the default value depends on
    // the attributes datatype
    private Composite defaultEditFieldPlaceholder;

    private Group ruleGroup;

    private IntegerField sizeField;

    private IntegerField precisionField;

    private IntegerField scaleField;

    private EnumField temporalMappingField;

    private Text sqlColumnDefinition;

    private Checkbox uniqueCheckbox;

    private Checkbox nullableCheckbox;

    public AttributeEditDialog(IPolicyCmptTypeAttribute attribute, Shell parentShell) {
        super(attribute, parentShell, Messages.AttributeEditDialog_title, true);
        this.attribute = attribute;
        ipsProject = attribute.getIpsProject();
        rule = attribute.findValueSetRule(ipsProject);
        try {
            currentDatatype = attribute.findDatatype(ipsProject);
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        currentAttributeType = attribute.getAttributeType();
        extFactory = new ExtensionPropertyControlFactory(attribute.getClass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Composite createWorkArea(Composite parent) throws CoreException {
        tabFolder = (TabFolder)parent;

        TabItem page = new TabItem(tabFolder, SWT.NONE);
        page.setText(Messages.AttributeEditDialog_generalTitle);
        page.setControl(createGeneralPage(tabFolder));

        page = new TabItem(tabFolder, SWT.NONE);
        page.setText(Messages.AttributeEditDialog_valuesetTitle);
        page.setControl(createValueSetPage(tabFolder));

        final TabItem validationRulePage = new TabItem(tabFolder, SWT.NONE);
        validationRulePage.setText(Messages.AttributeEditDialog_validationRuleTitle);
        validationRulePage.setControl(createValidationRulePage(tabFolder));
        if (startWithRulePage) {
            tabFolder.setSelection(2);
            ruleNameField.getControl().setFocus();
        } else {
            tabFolder.setSelection(0);
            nameText.setFocus();
        }

        createPersistenceTabItemIfNecessary(tabFolder);
        createDescriptionTabItem(tabFolder);

        tabFolder.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            public void widgetSelected(SelectionEvent e) {
                if (ruleUIController != null) {
                    ruleUIController.updateUI();
                }
            }
        });

        final ContentsChangeListenerForWidget listener = new ContentsChangeListenerForWidget() {
            @Override
            public void contentsChangedAndWidgetIsNotDisposed(ContentChangeEvent event) {
                if (!event.getIpsSrcFile().exists()) {
                    return;
                }
                if (event.getIpsSrcFile().equals(attribute.getIpsObject().getIpsSrcFile())) {
                    updateFieldValidationRuleAdded();
                }
            }
        };
        listener.setWidget(parent);
        attribute.getIpsModel().addChangeListener(listener);

        // initial update of the checkBox "validationRuleAdded"
        updateFieldValidationRuleAdded();

        return tabFolder;
    }

    private void updateFieldValidationRuleAdded() {
        if (rule != null) {
            MessageList msgList;
            try {
                if (rule.isDeleted()) {
                    MessageCueController.setMessageCue(validationRuleAdded, null);
                    return;
                }
                msgList = rule.validate(rule.getIpsProject());
                msgList = msgList.getMessagesFor(rule, IValidationRule.PROPERTY_CHECK_AGAINST_VALUE_SET_RULE);
                MessageCueController.setMessageCue(validationRuleAdded, msgList);
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
            return;
        }
        MessageCueController.setMessageCue(validationRuleAdded, null);
    }

    /**
     * Brings the page for the validation rule to front.
     */
    protected void showValidationRulePage() {
        startWithRulePage = true;
        if (tabFolder != null) {
            tabFolder.setSelection(3);
        }
    }

    private Control createGeneralPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);
        Group generelGroup = uiToolkit.createGroup(c, Messages.AttributeEditDialog_generalGroup);
        createGenerelGroupContent(generelGroup);
        if (attribute.isProductRelevant() || attribute.getPolicyCmptType().isConfigurableByProductCmptType()) {
            configGroup = uiToolkit.createGroup(c, Messages.AttributeEditDialog_ConfigurationGroup);
            createConfigGroupContent();
        }

        return c;
    }

    private void createGenerelGroupContent(Composite c) {
        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        extFactory.createControls(workArea, uiToolkit, attribute, IExtensionPropertyDefinition.POSITION_TOP);

        uiToolkit.createFormLabel(workArea, Messages.AttributeEditDialog_labelName);
        nameText = uiToolkit.createText(workArea);
        bindingContext.bindContent(nameText, attribute, IIpsElement.PROPERTY_NAME);

        uiToolkit.createFormLabel(workArea, Messages.AttributeEditDialog_lableOverwrites);
        final Checkbox cb = new Checkbox(workArea, uiToolkit);
        cb.setText(Messages.AttributeEditDialog_overwritesNote);
        EditField overwrittenField = bindingContext.bindContent(cb, attribute,
                IPolicyCmptTypeAttribute.PROPERTY_OVERWRITES);
        overwrittenField.addChangeListener(new ValueChangeListener() {

            public void valueChanged(FieldValueChangedEvent event) {
                if (cb.isChecked()) {
                    try {
                        IPolicyCmptTypeAttribute overwrittenAttribute = attribute.findOverwrittenAttribute(ipsProject);
                        if (overwrittenAttribute != null) {
                            attribute.setDatatype(overwrittenAttribute.getDatatype());
                            attribute.setModifier(overwrittenAttribute.getModifier());
                            attribute.setProductRelevant(overwrittenAttribute.isProductRelevant());
                            attribute.setAttributeType(overwrittenAttribute.getAttributeType());
                            attribute.setValueSetCopy(overwrittenAttribute.getValueSet());
                        }
                    } catch (CoreException e) {
                        IpsPlugin.log(e);
                    }
                }

            }

        });

        uiToolkit.createFormLabel(workArea, Messages.AttributeEditDialog_labelDatatype);
        datatypeControl = uiToolkit.createDatatypeRefEdit(attribute.getIpsProject(), workArea);
        datatypeControl.setVoidAllowed(false);
        datatypeControl.setOnlyValueDatatypesAllowed(true);
        bindingContext.bindContent(datatypeControl, attribute, IAttribute.PROPERTY_DATATYPE);

        uiToolkit.createFormLabel(workArea, Messages.AttributeEditDialog_labelModifier);
        Combo modifierCombo = uiToolkit.createCombo(workArea, Modifier.getEnumType());
        bindingContext.bindContent(modifierCombo, attribute, IAttribute.PROPERTY_MODIFIER, Modifier.getEnumType());

        uiToolkit.createFormLabel(workArea, Messages.AttributeEditDialog_labelAttrType);
        Combo typeCombo = uiToolkit.createCombo(workArea, AttributeType.getEnumType());
        bindingContext.bindContent(typeCombo, attribute, IPolicyCmptTypeAttribute.PROPERTY_ATTRIBUTE_TYPE,
                AttributeType.getEnumType());

        extFactory.createControls(workArea, uiToolkit, attribute, IExtensionPropertyDefinition.POSITION_BOTTOM);
        extFactory.bind(bindingContext);
    }

    private void recreateConfigGroupContent(Group group, AttributeType attrType) {
        if (configGroup == null) {
            return;
        }
        Control[] children = configGroup.getChildren();
        for (int i = 0; i < children.length; i++) {
            children[i].dispose();
        }
        createConfigGroupContent();
        configGroup.layout();
        bindingContext.updateUI();
    }

    private void createConfigGroupContent() {
        Composite area = uiToolkit.createGridComposite(configGroup, 1, true, false);
        GridData gridData = (GridData)area.getLayoutData();
        gridData.heightHint = 100;

        if (attribute.isChangeable()) {
            Checkbox checkbox = uiToolkit.createCheckbox(area, Messages.AttributeEditDialog_defaultValueConfigured);
            bindingContext.bindContent(checkbox, attribute, IPolicyCmptTypeAttribute.PROPERTY_PRODUCT_RELEVANT);
            return;
        }

        if (attribute.getAttributeType() == AttributeType.CONSTANT) {
            uiToolkit.createFormLabel(area, Messages.AttributeEditDialog_ConstantAttributesCantBeConfigured);
            return;
        }

        String productCmptType = QNameUtil.getUnqualifiedName(attribute.getPolicyCmptType().getProductCmptType());
        String checkboxText = NLS.bind(Messages.AttributeEditDialog_attributeComputed, productCmptType);
        Checkbox checkbox = uiToolkit.createCheckbox(area, checkboxText);
        bindingContext.bindContent(checkbox, attribute, IPolicyCmptTypeAttribute.PROPERTY_PRODUCT_RELEVANT);
        uiToolkit.createLabel(area, Messages.AttributeEditDialog_methodNote);

        Composite temp = uiToolkit.createLabelEditColumnComposite(area);
        Link label = new Link(temp, SWT.NONE);
        label.setText(Messages.AttributeEditDialog_methodLink);
        label.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            public void widgetSelected(SelectionEvent e) {
                editMethodInDialog();
            }
        });
        Text compuationMethodText = uiToolkit.createText(temp);
        MethodSignatureCompletionProcessor processor = new MethodSignatureCompletionProcessor(getProductCmptType());
        CompletionUtil.createHandlerForText(compuationMethodText, processor);

        bindingContext.bindContent(compuationMethodText, attribute,
                IPolicyCmptTypeAttribute.PROPERTY_COMPUTATION_METHOD_SIGNATURE);
        bindingContext.bindEnabled(compuationMethodText, attribute, IPolicyCmptTypeAttribute.PROPERTY_PRODUCT_RELEVANT);

        Link link = new Link(area, SWT.NONE);
        link.setText(Messages.AttributeEditDialog_createNewMethod);
        link.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            public void widgetSelected(SelectionEvent e) {
                createMethodAndOpenDialog();
            }

        });

    }

    private IProductCmptType getProductCmptType() {
        try {
            return attribute.getPolicyCmptType().findProductCmptType(ipsProject);
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return null;
        }
    }

    private void createMethodAndOpenDialog() {
        IProductCmptType productCmptType = findProductCmptTypeAndInformUserIfNotExists();
        if (productCmptType == null) {
            return;
        }
        Memento productCmptTypeMemento = productCmptType.newMemento();
        boolean productCmptTypeDirty = productCmptType.getIpsSrcFile().isDirty();
        Memento policyCmptTypeMemento = attribute.getPolicyCmptType().newMemento();
        IProductCmptTypeMethod newMethod = productCmptType.newFormulaSignature(attribute.getName());
        String qName = attribute.getPolicyCmptType().getQualifiedName();
        newMethod.newParameter(qName, StringUtils.uncapitalize(QNameUtil.getUnqualifiedName(qName)));
        attribute.setComputationMethodSignature(newMethod.getSignatureString());
        newMethod.setDatatype(attribute.getDatatype());
        int rc = openEditMethodDialog(newMethod, productCmptTypeMemento, productCmptTypeDirty);
        if (rc == Window.CANCEL) {
            attribute.getPolicyCmptType().setState(policyCmptTypeMemento);
        }
    }

    private void editMethodInDialog() {
        IProductCmptType productCmptType = findProductCmptTypeAndInformUserIfNotExists();
        if (productCmptType == null) {
            return;
        }
        IProductCmptTypeMethod method = null;
        try {
            method = attribute.findComputationMethod(ipsProject);
            if (method == null) {
                String signature = attribute.getComputationMethodSignature();
                if (StringUtils.isEmpty(signature)) {
                    signature = Messages.AttributeEditDialog_emptyString;
                }
                String text = NLS.bind(Messages.AttributeEditDialog_questionCreateMethod, productCmptType
                        .getQualifiedName(), signature);
                if (MessageDialog.openQuestion(getShell(), Messages.AttributeEditDialog_MethodDoesNotExist, text)) {
                    createMethodAndOpenDialog();
                }
                return;
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return;
        }
        Memento memento = productCmptType.newMemento();
        openEditMethodDialog(method, memento, method.getIpsSrcFile().isDirty());
    }

    private int openEditMethodDialog(IProductCmptTypeMethod method,
            Memento memento,
            boolean productCmptTypeDirtyBeforeDialog) {
        IProductCmptType productCmptType = method.getProductCmptType();
        IIpsSrcFile file = productCmptType.getIpsSrcFile();
        ProductCmptTypeMethodEditDialog dialog = new ProductCmptTypeMethodEditDialog(method, getShell());
        dialog.open();
        if (dialog.getReturnCode() == Window.CANCEL) {
            productCmptType.setState(memento);
            if (!productCmptTypeDirtyBeforeDialog) {
                file.markAsClean();
            }
        } else {
            attribute.setComputationMethodSignature(method.getSignatureString());
            if (!productCmptTypeDirtyBeforeDialog) {
                try {
                    file.save(true, null);
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        }
        return dialog.getReturnCode();
    }

    private IProductCmptType findProductCmptTypeAndInformUserIfNotExists() {
        IPolicyCmptType policyCmptType = attribute.getPolicyCmptType();
        IProductCmptType productCmptType = null;
        try {
            productCmptType = policyCmptType.findProductCmptType(ipsProject);
            if (productCmptType == null) {
                String text = NLS.bind(Messages.AttributeEditDialog_TypeCantBeFound, policyCmptType
                        .getProductCmptType());
                MessageDialog.openInformation(getShell(), Messages.AttributeEditDialog_Info, text);
            }
        } catch (CoreException e) {
            String text = NLS
                    .bind(
                            "An error occurred while searching for the product component type ''{0}''.", policyCmptType.getProductCmptType()); //$NON-NLS-1$
            MessageDialog.openInformation(getShell(), Messages.AttributeEditDialog_Info, text);
        }
        return productCmptType;
    }

    private Control createValueSetPage(TabFolder folder) throws CoreException {
        Composite pageControl = createTabItemComposite(folder, 1, false);

        Composite workArea = uiToolkit.createLabelEditColumnComposite(pageControl);
        labelDefaultValue = uiToolkit.createLabel(workArea, Messages.AttributeEditDialog_labelDefaultValue);

        defaultEditFieldPlaceholder = uiToolkit.createComposite(workArea);
        defaultEditFieldPlaceholder.setLayout(uiToolkit.createNoMarginGridLayout(1, true));
        defaultEditFieldPlaceholder.setLayoutData(new GridData(GridData.FILL_BOTH));
        createDefaultValueEditField();

        IpsObjectUIController uiController = new IpsObjectUIController(attribute);
        List<ValueSetType> valueSetTypes = attribute.getAllowedValueSetTypes(attribute.getIpsProject());;
        valueSetSpecificationControl = new ValueSetSpecificationControl(pageControl, uiToolkit, uiController,
                attribute, valueSetTypes, ValueSetControlEditMode.ALL_KIND_OF_SETS);
        updateAllowedValueSetTypes();

        Object layoutData = valueSetSpecificationControl.getLayoutData();
        if (layoutData instanceof GridData) {
            // set the minimum height to show at least the maximum size of the selected
            // ValueSetEditControl
            GridData gd = (GridData)layoutData;
            gd.heightHint = 300;
        }

        adjustLabelWidth();
        return pageControl;
    }

    private void adjustLabelWidth() {
        if (valueSetSpecificationControl == null) {
            return;
        }
        // sets the label width of the value set control label, so the control will be horizontal
        // aligned to the default value text
        // the offset of 7 is calculated by the corresponding composites horizontal spacing and
        // margins
        int widthDefaultLabel = labelDefaultValue.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        int widthValueSetTypeLabel = valueSetSpecificationControl.getPreferredLabelWidth();
        if (widthDefaultLabel > widthValueSetTypeLabel) {
            valueSetSpecificationControl.setLabelWidthHint(widthDefaultLabel);
        } else {
            Object ld = labelDefaultValue.getLayoutData();
            if (ld instanceof GridData) {
                ((GridData)ld).widthHint = widthValueSetTypeLabel;
                labelDefaultValue.setLayoutData(ld);
            }
        }
    }

    private void createDefaultValueEditField() {
        ValueDatatypeControlFactory datatypeCtrlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(
                currentDatatype);
        defaultValueField = datatypeCtrlFactory.createEditField(uiToolkit, defaultEditFieldPlaceholder,
                currentDatatype, null, ipsProject);
        if (defaultValueField instanceof EnumTypeDatatypeField) {
            ((EnumTypeDatatypeField)defaultValueField).setEnableEnumContentDisplay(false);
        }
        defaultValueField.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        adjustLabelWidth();

        defaultEditFieldPlaceholder.layout();
        defaultEditFieldPlaceholder.getParent().getParent().layout();
        bindingContext.bindContent(defaultValueField, attribute, IAttribute.PROPERTY_DEFAULT_VALUE);
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        super.contentsChanged(event);
        if (attribute.getAttributeType() != currentAttributeType) {
            currentAttributeType = attribute.getAttributeType();
            recreateConfigGroupContent(configGroup, currentAttributeType);
        }
        ValueDatatype newDatatype = null;
        try {
            newDatatype = attribute.findDatatype(ipsProject);
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        boolean enabled = newDatatype != null;
        if (defaultValueField != null) {
            defaultValueField.getControl().setEnabled(enabled);
        }
        if (valueSetSpecificationControl != null) {
            valueSetSpecificationControl
                    .setEditMode(attribute.isProductRelevant() ? ValueSetControlEditMode.ALL_KIND_OF_SETS
                            : ValueSetControlEditMode.ONLY_NONE_ABSTRACT_SETS);
            valueSetSpecificationControl.setDataChangeable(enabled);
        }
        if (newDatatype == null || newDatatype.equals(currentDatatype)) {
            return;
        }
        currentDatatype = newDatatype;
        if (defaultValueField != null) {
            bindingContext.removeBindings(defaultValueField.getControl());
            defaultValueField.getControl().dispose();
        }
        createDefaultValueEditField();
        updateAllowedValueSetTypes();
    }

    private void updateAllowedValueSetTypes() {
        ValueSetType currentValueSetType = valueSetSpecificationControl.getValueSetType();
        try {
            valueSetSpecificationControl.setAllowedValueSetTypes(attribute.getAllowedValueSetTypes(attribute
                    .getIpsProject()));
        } catch (CoreException e) {
            IpsPlugin.log(e);
            valueSetSpecificationControl.setAllowedValueSetTypes(new ArrayList<ValueSetType>());
        }
        if (currentValueSetType != null) {
            // if the previous selction was a valid selection use this one as new selection in drop
            // down, otherwise the default (first one) is selected
            valueSetSpecificationControl.setValueSetType(currentValueSetType);
        }
    }

    private Control createValidationRulePage(TabFolder folder) {
        Composite workArea = createTabItemComposite(folder, 1, false);

        ((GridLayout)workArea.getLayout()).verticalSpacing = 20;

        Composite checkComposite = uiToolkit.createGridComposite(workArea, 1, true, false);
        validationRuleAdded = uiToolkit.createCheckbox(checkComposite,
                Messages.AttributeEditDialog_labelActivateValidationRule);
        validationRuleAdded.setToolTipText(Messages.AttributeEditDialog_tooltipActivateValidationRule);
        validationRuleAdded.getButton().addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing to do
            }

            public void widgetSelected(SelectionEvent e) {
                enableCheckValueAgainstValueSetRule(((Button)e.getSource()).getSelection());
            }

        });

        ruleGroup = uiToolkit.createGroup(checkComposite, Messages.AttributeEditDialog_ruleTitle);
        Composite nameComposite = uiToolkit.createLabelEditColumnComposite(ruleGroup);
        uiToolkit.createFormLabel(nameComposite, Messages.AttributeEditDialog_labelName);
        Text nameText = uiToolkit.createText(nameComposite);
        nameText.setFocus();

        // message group
        Group msgGroup = uiToolkit.createGroup(ruleGroup, Messages.AttributeEditDialog_messageTitle);
        Composite msgComposite = uiToolkit.createLabelEditColumnComposite(msgGroup);
        msgComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        uiToolkit.createFormLabel(msgComposite, Messages.AttributeEditDialog_labelCode);
        Text codeText = uiToolkit.createText(msgComposite);
        uiToolkit.createFormLabel(msgComposite, Messages.AttributeEditDialog_labelSeverity);
        Combo severityCombo = uiToolkit.createCombo(msgComposite, MessageSeverity.getEnumType());
        Label label = uiToolkit.createFormLabel(msgComposite, Messages.AttributeEditDialog_labelText);
        label.getParent().setLayoutData(
                new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING));
        Text msgText = uiToolkit.createMultilineText(msgComposite);

        // create fields
        ruleNameField = new TextField(nameText);
        msgCodeField = new TextField(codeText);
        msgTextField = new TextField(msgText);
        msgSeverityField = new EnumValueField(severityCombo, MessageSeverity.getEnumType());;

        if (rule != null) {
            validationRuleAdded.setChecked(true);
            enableCheckValueAgainstValueSetRule(true);
        } else {
            validationRuleAdded.setChecked(false);
            enableCheckValueAgainstValueSetRule(false);
        }
        if (rule != null) {
            createRuleUIController();
            ruleUIController.updateUI();
        }

        return workArea;
    }

    private void enableCheckValueAgainstValueSetRule(boolean enabled) {
        uiToolkit.setDataChangeable(ruleGroup, enabled);
        if (enabled) {
            if (rule != null) {
                // we already have a rule, so dont create one. this could happen
                // at creation time, for example.
                return;
            }
            rule = attribute.createValueSetRule();
            rule.setDescription(Messages.AttributeEditDialog_descriptionContent);

            if (ruleUIController == null) {
                createRuleUIController();
            }
            ruleUIController.updateUI();
        } else if (rule != null) {
            deleteRuleUIController();
            rule.delete();
            rule = null;
        }
    }

    private void deleteRuleUIController() {
        ruleUIController.remove(ruleNameField);
        ruleUIController.remove(msgCodeField);
        ruleUIController.remove(msgTextField);
        ruleUIController.remove(msgSeverityField);
        ruleUIController = null;
    }

    private void createRuleUIController() {
        ruleUIController = new IpsObjectUIController(rule);
        ruleUIController.add(ruleNameField, IIpsElement.PROPERTY_NAME);
        ruleUIController.add(msgCodeField, IValidationRule.PROPERTY_MESSAGE_CODE);
        ruleUIController.add(msgTextField, IValidationRule.PROPERTY_MESSAGE_TEXT);
        ruleUIController.add(msgSeverityField, IValidationRule.PROPERTY_MESSAGE_SEVERITY);
    }

    private void createPersistenceTabItemIfNecessary(TabFolder tabFolder) {
        if (!ipsProject.getProperties().isPersistenceSupportEnabled()) {
            return;
        }
        final TabItem persistencePage = new TabItem(tabFolder, SWT.NONE);
        persistencePage.setText("Persistence");

        Composite c = createTabItemComposite(tabFolder, 1, false);
        persistencePage.setControl(c);

        Composite checkComposite = uiToolkit.createGridComposite(c, 1, true, false);
        Checkbox checkTransient = uiToolkit.createCheckbox(checkComposite, "The attribute is transient");
        bindingContext.bindContent(checkTransient, attribute.getPersistenceAttributeInfo(),
                IPersistentAttributeInfo.PROPERTY_TRANSIENT);

        final Group group = uiToolkit.createGroup(checkComposite, "Persistent Properties");
        Composite workArea = uiToolkit.createLabelEditColumnComposite(group);

        uiToolkit.createFormLabel(workArea, "Column name:");
        Text columnNameText = uiToolkit.createText(workArea);
        bindingContext.bindContent(columnNameText, attribute.getPersistenceAttributeInfo(),
                IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_NAME);

        uiToolkit.createFormLabel(workArea, "Unique:");
        uniqueCheckbox = uiToolkit.createCheckbox(workArea);
        bindingContext.bindContent(uniqueCheckbox, attribute.getPersistenceAttributeInfo(),
                IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_UNIQE);

        uiToolkit.createFormLabel(workArea, "Nullable:");
        nullableCheckbox = uiToolkit.createCheckbox(workArea);
        bindingContext.bindContent(nullableCheckbox, attribute.getPersistenceAttributeInfo(),
                IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_NULLABLE);

        uiToolkit.createFormLabel(workArea, "Column size:");
        sizeField = new IntegerField(uiToolkit.createText(workArea));
        bindingContext.bindContent(sizeField, attribute.getPersistenceAttributeInfo(),
                IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_SIZE);

        uiToolkit.createFormLabel(workArea, "Column precision:");
        precisionField = new IntegerField(uiToolkit.createText(workArea));
        bindingContext.bindContent(precisionField, attribute.getPersistenceAttributeInfo(),
                IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_PRECISION);

        uiToolkit.createFormLabel(workArea, "Column scale:");
        scaleField = new IntegerField(uiToolkit.createText(workArea));
        bindingContext.bindContent(scaleField, attribute.getPersistenceAttributeInfo(),
                IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_SCALE);

        uiToolkit.createFormLabel(workArea, "Temporal type:");
        Combo temporalMappingCombo = uiToolkit.createCombo(workArea);
        setComboItemsForEnum(temporalMappingCombo, DateTimeMapping.class);
        temporalMappingField = new EnumField(temporalMappingCombo, DateTimeMapping.class);
        bindingContext.bindContent(temporalMappingField, attribute.getPersistenceAttributeInfo(),
                IPersistentAttributeInfo.PROPERTY_TEMPORAL_MAPPING);

        uiToolkit.createFormLabel(workArea, "SQL column definition:");
        sqlColumnDefinition = uiToolkit.createText(workArea);
        bindingContext.bindContent(sqlColumnDefinition, attribute.getPersistenceAttributeInfo(),
                IPersistentAttributeInfo.PROPERTY_SQL_COLUMN_DEFINITION);

        uiToolkit.createFormLabel(workArea, "Datatype converter class:");
        if (ipsProject.getIpsArtefactBuilderSet().isPersistentProviderSupportConverter()) {
            final Text converterQualifiedName = uiToolkit.createText(workArea);
            bindingContext.bindContent(converterQualifiedName, attribute.getPersistenceAttributeInfo(),
                    IPersistentAttributeInfo.PROPERTY_CONVERTER_QUALIFIED_CLASS_NAME);
        } else {
            final Text converterQualifiedName = uiToolkit.createText(workArea);
            converterQualifiedName.setEnabled(false);
            converterQualifiedName.setText("Not supported by persistence provider.");
        }

        // disable all tab page controls if policy component type shouldn't persist
        bindingContext.add(new ControlPropertyBinding(c, attribute.getPolicyCmptType().getPersistenceTypeInfo(),
                "enabled", Boolean.TYPE) { //$NON-NLS-1$
                    @Override
                    public void updateUiIfNotDisposed() {
                        if (!isPersistentEnabled()) {
                            uiToolkit.setDataChangeable(persistencePage.getControl(), false);
                            return;
                        }
                        if (attribute.getPersistenceAttributeInfo().isTransient()) {
                            uiToolkit.setDataChangeable(group, false);
                            return;
                        }
                        uiToolkit.setDataChangeable(persistencePage.getControl(), ((IPersistentTypeInfo)getObject())
                                .isEnabled());
                    }
                });

        // disable property controls if attribute is marked as transient
        bindingContext.add(new ControlPropertyBinding(c, attribute.getPersistenceAttributeInfo(),
                IPersistentAttributeInfo.PROPERTY_TRANSIENT, Boolean.TYPE) {
            @Override
            public void updateUiIfNotDisposed() {
                if (!isPersistentEnabled()) {
                    uiToolkit.setDataChangeable(group, false);
                    return;
                }
                if (attribute.getPersistenceAttributeInfo().isTransient()) {
                    uiToolkit.setDataChangeable(group, false);
                    return;
                }
                enableOrDisableDatatypeDependingControls();
            }

        });
        // datatype depending enabled or disabled controls
        bindingContext.add(new ControlPropertyBinding(c, attribute, IAttribute.PROPERTY_DATATYPE, String.class) {
            @Override
            public void updateUiIfNotDisposed() {
                boolean enabled = isPersistentEnabled();
                if (!enabled) {
                    uiToolkit.setDataChangeable(group, false);
                    return;
                }
                if (attribute.getPersistenceAttributeInfo().isTransient()) {
                    uiToolkit.setDataChangeable(group, false);
                    return;
                }
                if (enabled) {
                    enableOrDisableDatatypeDependingControls();
                }
            }
        });
    }

    private boolean isPersistentEnabled() {
        if (!isDataChangeable()) {
            return false;
        }
        return attribute.getPolicyCmptType().isPersistentEnabled();
    }

    private void enableOrDisableDatatypeDependingControls() {
        try {
            ValueDatatype datatype = attribute.findDatatype(ipsProject);

            boolean hasDecimalPlaces = PersistenceUtil.isSupportingDecimalPlaces(datatype);
            boolean hasLength = PersistenceUtil.isSupportingLenght(datatype);
            boolean needsTemporalType = PersistenceUtil.isSupportingTemporalType(datatype);
            boolean canBeNullable = true;
            boolean canBeUnique = true;

            // if a column definition is given, then all properties are specified using the sql
            // definition
            if (StringUtils.isNotEmpty(sqlColumnDefinition.getText())) {
                hasDecimalPlaces = false;
                hasLength = false;
                needsTemporalType = false;
                canBeNullable = false;
                canBeUnique = false;
            }

            uiToolkit.setDataChangeable(nullableCheckbox, canBeNullable);
            uiToolkit.setDataChangeable(uniqueCheckbox, canBeUnique);
            uiToolkit.setDataChangeable(precisionField.getControl(), hasDecimalPlaces);
            uiToolkit.setDataChangeable(scaleField.getControl(), hasDecimalPlaces);
            uiToolkit.setDataChangeable(sizeField.getControl(), hasLength);
            uiToolkit.setDataChangeable(temporalMappingField.getControl(), needsTemporalType);
        } catch (CoreException e) {
            // validation error, displayed in dialog message area
        }
    }

    class MethodSignatureCompletionProcessor extends AbstractCompletionProcessor {

        private IType type;

        public MethodSignatureCompletionProcessor(IType type) {
            super(type == null ? null : type.getIpsProject());
            this.type = type;
            setComputeProposalForEmptyPrefix(true);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void doComputeCompletionProposals(String prefix, int documentOffset, List<ICompletionProposal> result)
                throws Exception {
            if (type == null) {
                return;
            }
            IMethod[] methods = type.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getSignatureString().startsWith(prefix)) {
                    addToResult(result, methods[i], documentOffset);
                }
            }
        }

        private void addToResult(List<ICompletionProposal> result, IMethod method, int documentOffset) {
            String name = method.getSignatureString();
            CompletionProposal proposal = new CompletionProposal(name, 0, documentOffset, name.length(), IpsUIPlugin
                    .getImageHandling().getImage(method), name, null, method.getDescription());
            result.add(proposal);
        }

    }
}
