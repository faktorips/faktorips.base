package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPartContainer;
import org.faktorips.devtools.core.model.ValueSet;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.MessageSeverity;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueTextField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.core.ui.controls.TableElementValidator;
import org.faktorips.devtools.core.ui.controls.ValueSetChangeListener;
import org.faktorips.devtools.core.ui.controls.ValueSetEditControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;

/**
 *  
 */
public class AttributeEditDialog extends IpsPartEditDialog implements ParameterListChangeListener,
        ValueSetChangeListener {

    //private final static String PROPERTY_PM_PROPERTY_NAME_ID = "de.bbv.faktorips.attribute.pmPropertyName";
    	
    private IAttribute attribute;
    private IValidationRule rule;

    // edit fields
    private TextField nameField;

    private TextButtonField datatypeField;

    private EnumValueField modifierField;

    private EnumValueField attributeTypeField;

    private CheckboxField productRelevantField;

    private ValueTextField defaultValueField;
    
    private ValueSetEditControl valueSetEditControl;
    private DatatypeRefControl datatypeControl;
    // control to edit the formula parameters
    private ChangeParametersControl parametersControl;
    
    private ExtensionPropertyControlFactory extFactory;

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
     * Collection of all controlls depending on the CheckValueAgainstValueSetRule.
     */
    private ArrayList ruleDependendControls = new ArrayList();

    /**
     * Folder which contains the pages shown by this editor. Used to modify which page
     * is shown.
     */
    private TabFolder folder;
    
    /**
     * Flag that indicates whether this dialog should startUp with the rule page 
     * on top (<code>true</code>) or not.
     */
    private boolean startWithRulePage = false;
    
    /**
     * Controller to link the part-related input fields to the rule. The default ui-controller can not
     * be used because the default ui-controller is for the attribute and not for the rule. 
     */
    private IpsPartUIController ruleUIController;
    
    /**
     * @param parentShell
     * @param title
     */
    public AttributeEditDialog(IAttribute attribute, Shell parentShell) {
        super(attribute, parentShell, "Edit Attribute", true);
        this.attribute = attribute;
        extFactory = new ExtensionPropertyControlFactory(attribute.getClass());
        
        this.rule = findValidationRule();
    }
    
    /**
     * Returns the default validation rule bound to this attribute or null if no such rule is found.
     */
    private IValidationRule findValidationRule() {
    	IPolicyCmptType type = (IPolicyCmptType)attribute.getParent();
    	IValidationRule[] rules = type.getRules();
    	
    	for (int i = 0; i < rules.length; i++) {
			String[] attributes = rules[i].getValidatedAttributes();
			for (int j = 0; j < attributes.length; j++) {
				if (attributes[j].equals(attribute.getName())) {
					return rules[i];
				}
			}
		}
    	
    	return null;
    }
    
	/**
	 * {@inheritDoc}
	 */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        folder = (TabFolder)parent;

        TabItem page = new TabItem(folder, SWT.NONE);
        page.setText("General");
        page.setControl(createGeneralPage(folder));

        page = new TabItem(folder, SWT.NONE);
        page.setText("Value Set");
        page.setControl(createValueSetPage(folder));

        page = new TabItem(folder, SWT.NONE);
        page.setText("Calculation Parameters");
        page.setControl(createFormulaParametersPage(folder));

        page = new TabItem(folder, SWT.NONE);
        page.setText("Validation Rule");
        page.setControl(createValidationRulePage(folder));
        if (startWithRulePage) {
        	folder.setSelection(3);
        }

        createDescriptionTabItem(folder);
        return folder;
    }
    
    /**
     * Brings the page for the validation rule to front.
     */
    protected void showValidationRulePage() {
    	startWithRulePage = true;
    	if (folder != null) {
    		folder.setSelection(3);
    	}
    }

    private Control createGeneralPage(TabFolder folder) {

        Composite c = createTabItemComposite(folder, 1, false);
        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        extFactory.createControls(workArea,uiToolkit,(IpsObjectPartContainer)attribute);

        uiToolkit.createFormLabel(workArea, "Name:");
        Text nameText = uiToolkit.createText(workArea);
        nameText.setFocus();

        uiToolkit.createFormLabel(workArea, "Datatype:");
        datatypeControl = uiToolkit.createDatatypeRefEdit(attribute.getIpsProject(), workArea);
        datatypeControl.setVoidAllowed(false);
        datatypeControl.setOnlyValueDatatypesAllowed(true);
        datatypeControl.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                updateValueSetTypes();
            }
        });

        uiToolkit.createFormLabel(workArea, "Modifier:");
        Combo modifierCombo = uiToolkit.createCombo(workArea, Modifier.getEnumType());

        uiToolkit.createFormLabel(workArea, "Attribute Type:");
        Combo typeCombo = uiToolkit.createCombo(workArea, AttributeType.getEnumType());

        uiToolkit.createFormLabel(workArea, "Product Relevant:");
        Checkbox checkbox = uiToolkit.createCheckbox(workArea);

        uiToolkit.createFormLabel(workArea, "Default Value:");
        Text defaultValueText = uiToolkit.createText(workArea);
        
        // create fields
        nameField = new TextField(nameText);
        datatypeField = new TextButtonField(datatypeControl);
        modifierField = new EnumValueField(modifierCombo, Modifier.getEnumType());
        attributeTypeField = new EnumValueField(typeCombo, AttributeType.getEnumType());
        productRelevantField = new CheckboxField(checkbox);
        defaultValueField = new ValueTextField(defaultValueText);
        
        return c;
    }

    private Control createValueSetPage(TabFolder folder) {
        Composite pageControl = createTabItemComposite(folder, 1, false);
        valueSetEditControl = new ValueSetEditControl(pageControl, uiToolkit, uiController, attribute.getValueSet(), new PcTypeValidator());
        valueSetEditControl.setValueSetChangelistener(this);
        return pageControl;
    }

    private Control createFormulaParametersPage(TabFolder folder) {

        Composite pageControl = createTabItemComposite(folder, 1, false);
        Composite workArea = uiToolkit.createLabelEditColumnComposite(pageControl);

        parametersControl = new ChangeParametersControl(workArea, SWT.NONE, "Parameters", attribute.getIpsProject()) {

            public MessageList validate(int paramIndex) throws CoreException {
                MessageList result = new MessageList();
                MessageList list = attribute.validate();
                for (int i = 0; i < list.getNoOfMessages(); i++) {
                    if (isMessageForParameter(list.getMessage(i), paramIndex)) {
                        result.add(list.getMessage(i));
                    }
                }
                return result;
            }

            private boolean isMessageForParameter(Message msg, int paramIndex) {
                ObjectProperty[] op = msg.getInvalidObjectProperties();
                for (int j = 0; j < op.length; j++) {
                    if (op[j].getObject() instanceof Parameter) {
                        if (((Parameter)op[j].getObject()).getIndex() == paramIndex) {
                            return true;
                        }
                    }
                }
                return false;
            }

        };
        parametersControl.initControl();
        parametersControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        return pageControl;
    }

    
    private Control createValidationRulePage(TabFolder folder) {
        Composite workArea = createTabItemComposite(folder,1, false);

        ((GridLayout)workArea.getLayout()).verticalSpacing = 20;

        Composite checkComposite = uiToolkit.createGridComposite(workArea, 1, true, false);
        Checkbox active = uiToolkit.createCheckbox(checkComposite, "Activate validation rule");
        active.setToolTipText("If activated, a rule is created which checks the value of this attribute against its value set.");
        active.getButton().addSelectionListener(new SelectionListener() {
		
			public void widgetDefaultSelected(SelectionEvent e) {
				System.out.println("defSel");
				
			}
		
			public void widgetSelected(SelectionEvent e) {
				
				enableCheckValueAgainstValueSetRule(((Button)e.getSource()).getSelection());
			}
		
		});
        
        Group ruleGroup = uiToolkit.createGroup(checkComposite, "Rule");
        Composite nameComposite = uiToolkit.createLabelEditColumnComposite(ruleGroup);
        Label nameLabel = uiToolkit.createFormLabel(nameComposite, "Name:");
        Text nameText = uiToolkit.createText(nameComposite);
        nameText.setFocus();
        nameText.setText("validate" + attribute.getName());

        // message group
        Group msgGroup = uiToolkit.createGroup(ruleGroup, "Message");
        Composite msgComposite = uiToolkit.createLabelEditColumnComposite(msgGroup);
        msgComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        Label codeLabel = uiToolkit.createFormLabel(msgComposite, "Code:");
        Text codeText = uiToolkit.createText(msgComposite);
        Label severityLabel = uiToolkit.createFormLabel(msgComposite, "Severity:");
        Combo severityCombo = uiToolkit.createCombo(msgComposite, MessageSeverity.getEnumType());
        Label label = uiToolkit.createFormLabel(msgComposite, "Text:");
        label.getParent().setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING));
        Text msgText = uiToolkit.createMultilineText(msgComposite);
        
        // create fields
        ruleNameField = new TextField(nameText);
        msgCodeField = new TextField(codeText);
        msgTextField = new TextField(msgText);
        msgSeverityField = new EnumValueField(severityCombo, MessageSeverity.getEnumType());;
        
        ruleDependendControls.add(ruleGroup);
        ruleDependendControls.add(nameLabel);
        ruleDependendControls.add(nameText);
        ruleDependendControls.add(msgGroup);
        ruleDependendControls.add(codeLabel);
        ruleDependendControls.add(codeText);
        ruleDependendControls.add(severityCombo);
        ruleDependendControls.add(severityLabel);
        ruleDependendControls.add(label);
        ruleDependendControls.add(msgText);
        if (rule != null) {
        	active.setChecked(true);
        	enableCheckValueAgainstValueSetRule(true);
        }
        else {
        	active.setChecked(false);
        	enableCheckValueAgainstValueSetRule(false);
        }

        return workArea;
    }

    private void enableCheckValueAgainstValueSetRule(boolean enabled) {
    	for (Iterator iter = ruleDependendControls.iterator(); iter.hasNext();) {
			Control control = (Control) iter.next();
			control.setEnabled(enabled);
		}
    	
    	if (enabled) {
    		if (rule != null) {
    			// we allready have a rule, so dont create one. this could happen
    			// at creation time, for example.
    			return;
    		}
    		rule = ((IPolicyCmptType)attribute.getParent()).newRule();
    		rule.addValidatedAttribute(attribute.getName());
    		rule.setAppliedInAllBusinessFunctions(true);
    		rule.setCheckValueAgainstValueSetRule(true);
    		rule.setValidatedAttrSpecifiedInSrc(false);
    		rule.setDescription("Automatically created rule to check the value of one attribute against the valueset of this attribute.");

    		initRuleUIController();
    		
    		ruleUIController.add(ruleNameField, rule, IValidationRule.PROPERTY_NAME);
    		ruleUIController.add(msgCodeField, rule, IValidationRule.PROPERTY_MESSAGE_CODE);
    		ruleUIController.add(msgTextField, rule, IValidationRule.PROPERTY_MESSAGE_TEXT);
    		ruleUIController.add(msgSeverityField, rule, IValidationRule.PROPERTY_MESSAGE_SEVERITY);
    	}
    	else if (rule != null){
    		ruleUIController.remove(ruleNameField);
    		ruleUIController.remove(msgCodeField);
    		ruleUIController.remove(msgTextField);
    		ruleUIController.remove(msgSeverityField);
    		ruleUIController = null;
    		rule.delete();
    		rule = null;
    	}
    }
    
    private void initRuleUIController() {
    	if (ruleUIController == null && rule != null) {
    		ruleUIController = new IpsPartUIController(rule);
    	}
    }
    
    private void updateValueSetTypes() {
        ValueDatatype datatype;
        try {
            datatype = attribute.getIpsProject().findValueDatatype(datatypeControl.getText());
            if (datatype != null) {
                ValueSetType[] types = attribute.getIpsProject().getValueSetTypes(datatype);
                valueSetEditControl.setTypes(types, datatype);
            } else {
                valueSetEditControl.setTypes(new ValueSetType[]{ValueSetType.ALL_VALUES}, null);
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }

	/**
	 * {@inheritDoc}
	 */
    protected void connectToModel() {
        super.connectToModel();
        uiController.add(nameField, IAttribute.PROPERTY_NAME);
        uiController.add(datatypeField, IAttribute.PROPERTY_DATATYPE);
        uiController.add(modifierField, IAttribute.PROPERTY_MODIFIER);
        uiController.add(attributeTypeField, IAttribute.PROPERTY_ATTRIBUTE_TYPE);
        uiController.add(defaultValueField, IAttribute.PROPERTY_DEFAULT_VALUE);
        uiController.add(productRelevantField, IAttribute.PROPERTY_PRODUCT_RELEVANT);

        if (rule != null) {
        	initRuleUIController();
        	ruleUIController.add(ruleNameField, IValidationRule.PROPERTY_NAME);
        	ruleUIController.add(msgCodeField, IValidationRule.PROPERTY_MESSAGE_CODE);
        	ruleUIController.add(msgTextField, IValidationRule.PROPERTY_MESSAGE_TEXT);
        	ruleUIController.add(msgSeverityField, IValidationRule.PROPERTY_MESSAGE_SEVERITY);
        }
        
        extFactory.connectToModel(uiController);
        List infos = ParameterInfo.createInfosAsList(attribute.getFormulaParameters());
        parametersControl.setInput(infos);
        parametersControl.setParameterListChangeListener(this);
    }

	/**
	 * {@inheritDoc}
	 */
    public void parameterChanged(ParameterInfo parameter) {
        parameterListChanged();
    }

	/**
	 * {@inheritDoc}
	 */
    public void parameterAdded(ParameterInfo parameter) {
        parameterListChanged();
    }

	/**
	 * {@inheritDoc}
	 */
    public void parameterListChanged() {
        Parameter[] params = ParameterInfo.createParameters(parametersControl.getInput());
        attribute.setFormulaParameters(params);
    }

	/**
	 * {@inheritDoc}
	 */
    public void valueSetChanged(ValueSet valueSet) {
        FieldValueChangedEvent e = new FieldValueChangedEvent(null);
        attribute.setValueSet(valueSet);
        uiController.valueChanged(e);
    }
    
    private class PcTypeValidator implements TableElementValidator {

        public MessageList validate(String element) {
            MessageList list;
            try {
                list = attribute.validate();
                return list.getMessagesFor(element);
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return new MessageList();
            }
        }
    }
    
}
