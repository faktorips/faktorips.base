package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPartContainer;
import org.faktorips.devtools.core.model.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ValueSet;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
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

    private final static String PROPERTY_PM_PROPERTY_NAME_ID = "de.bbv.faktorips.attribute.pmPropertyName";
    	
    private IAttribute attribute;

    // edit fields
    private TextField nameField;

    private TextButtonField datatypeField;

    private EnumValueField modifierField;

    private EnumValueField attributeTypeField;

    private CheckboxField productRelevantField;

    private TextField defaultValueField;
    
    private ValueSetEditControl valueSetEditControl;
    private DatatypeRefControl datatypeControl;
    // control to edit the formula parameters
    private ChangeParametersControl parametersControl;
    
    private EditField pmPropertyNameField; // TODO remove from core
    
    /**
     * @param parentShell
     * @param title
     */
    public AttributeEditDialog(IAttribute attribute, Shell parentShell) {
        super(attribute, parentShell, "Edit IAttribute", true);
        this.attribute = attribute;
    }
    
    /**
     * Overridden method.
     */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        TabFolder folder = (TabFolder)parent;

        TabItem page = new TabItem(folder, SWT.NONE);
        page.setText("General");
        page.setControl(createGeneralPage(folder));

        page = new TabItem(folder, SWT.NONE);
        page.setText("Value Set");
        page.setControl(createValueSetPage(folder));

        page = new TabItem(folder, SWT.NONE);
        page.setText("Calculation Parameters");
        page.setControl(createFormulaParametersPage(folder));

        createDescriptionTabItem(folder);
        return folder;
    }

    private Control createGeneralPage(TabFolder folder) {

        Composite c = createTabItemComposite(folder, 1, false);
        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        
        // TODO: remove pm extension from core
        IExtensionPropertyDefinition extProp = attribute.getIpsModel().getExtensionPropertyDefinition(IAttribute.class, PROPERTY_PM_PROPERTY_NAME_ID, true);
        if (extProp!=null) {
            uiToolkit.createFormLabel(workArea, extProp.getDisplayName() + ":");
            pmPropertyNameField = extProp.newEditField((IpsObjectPartContainer)attribute, workArea, uiToolkit);
        }

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

        uiToolkit.createFormLabel(workArea, "IAttribute Type:");
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
        defaultValueField = new TextField(defaultValueText);
        
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
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsPartEditDialog#connectToModel()
     */
    protected void connectToModel() {
        super.connectToModel();
        uiController.add(nameField, IAttribute.PROPERTY_NAME);
        uiController.add(datatypeField, IAttribute.PROPERTY_DATATYPE);
        uiController.add(modifierField, IAttribute.PROPERTY_MODIFIER);
        uiController.add(attributeTypeField, IAttribute.PROPERTY_ATTRIBUTE_TYPE);
        uiController.add(defaultValueField, IAttribute.PROPERTY_DEFAULT_VALUE);
        uiController.add(productRelevantField, IAttribute.PROPERTY_PRODUCT_RELEVANT);
        if (pmPropertyNameField!=null) {
            uiController.add(pmPropertyNameField, PROPERTY_PM_PROPERTY_NAME_ID);
        }
        List infos = ParameterInfo.createInfosAsList(attribute.getFormulaParameters());
        parametersControl.setInput(infos);
        parametersControl.setParameterListChangeListener(this);
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.pctype.ParameterListChangeListener#parameterChanged(org.faktorips.devtools.core.ui.editors.pctype.ParameterInfo)
     */
    public void parameterChanged(ParameterInfo parameter) {
        parameterListChanged();
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.pctype.ParameterListChangeListener#parameterAdded(org.faktorips.devtools.core.ui.editors.pctype.ParameterInfo)
     */
    public void parameterAdded(ParameterInfo parameter) {
        parameterListChanged();
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.pctype.ParameterListChangeListener#parameterListChanged()
     */
    public void parameterListChanged() {
        Parameter[] params = ParameterInfo.createParameters(parametersControl.getInput());
        attribute.setFormulaParameters(params);
    }

    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.ui.controls.ValueSetChangeListener#valueSetChanged(org.faktorips.devtools.core.model.ValueSet)
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
