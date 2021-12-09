/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.valuesets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.ButtonTextBinding;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.StringValueComboField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.ControlComposite;
import org.faktorips.devtools.core.ui.controls.Messages;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IConfigElement;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.MessageList;

/**
 * A control to specify the value set belonging to a {@link IValueSetOwner} . The control also
 * allows to change the type of the value set. Which value set types are allowed are defined by
 * {@link IValueSetOwner#getAllowedValueSetTypes(org.faktorips.devtools.model.ipsproject.IIpsProject)}
 */
public class ValueSetSpecificationControl extends ControlComposite implements IDataChangeableReadWriteAccess {

    private final IValueSetOwner valueSetOwner;

    private ValueSetControlEditMode editMode = ValueSetControlEditMode.ALL_KIND_OF_SETS;

    // Label, Combo & Field for the allowed value set types
    private Label valueSetTypeLabel;
    private Combo valueSetTypesCombo;
    private StringValueComboField valueSetTypeField;
    private List<ValueSetType> allowedValueSetTypes = new ArrayList<>();

    private Checkbox concreteValueSetCheckbox = null;
    private Checkbox containsNullCheckbox;
    private CheckboxField concreteValueSetField = null;
    private CheckboxField containsNullField;

    // control showing the value set
    private IValueSetEditControl valueSetEditControl;
    // can be safely casted to IValueSetEditControl

    // area around the value set, used to change the layout
    private Composite valueSetArea;

    // The last selected value set that is not an unrestricted value set.
    private IValueSet lastRestrictedValueSet;

    private UIToolkit toolkit;
    private BindingContext bindingContext;

    private boolean dataChangeable;
    private Label concreteValueSetLabel;

    private final ValueSetPmo valueSetPmo;

    /**
     * Creates a new control which contains a combo box and depending on the value of the box a
     * {@link EnumValueSetEditControl} or a {@link RangeEditControl}. The following general layout
     * is used: the main layout is a grid layout with one column. In the first row a composite with
     * a 2 column grid layout is created. In the second row a stack layout is used to swap the
     * EnumValueSetEditControl and RangeEditControl dynamically.
     */
    public ValueSetSpecificationControl(Composite parent, UIToolkit toolkit, BindingContext bindingContext,
            IValueSetOwner valueSetOwner, List<ValueSetType> allowedValueSetTypes, ValueSetControlEditMode editMode) {
        super(parent, SWT.NONE);
        this.valueSetOwner = valueSetOwner;
        this.toolkit = toolkit;
        this.bindingContext = bindingContext;
        this.editMode = editMode;

        valueSetPmo = new ValueSetPmo(valueSetOwner);
        initControls(toolkit);
        setAllowedValueSetTypes(allowedValueSetTypes);
    }

    private void initControls(UIToolkit toolkit) {
        initLayout();

        Composite twoColumnArea = toolkit.createLabelEditColumnComposite(this);
        createValueSetTypesCombo(toolkit, twoColumnArea);
        createConcreteValueSetCheckbox(toolkit, twoColumnArea);
        createContainsNullCheckbox(toolkit, twoColumnArea);

        toolkit.createVerticalSpacer(this, 5);
        createValueSetArea(toolkit, this);
        showControlForValueSet();
    }

    private void initLayout() {
        GridLayout mainAreaLayout = new GridLayout(1, false);
        mainAreaLayout.marginHeight = 0;
        mainAreaLayout.marginWidth = 0;
        setLayout(mainAreaLayout);
        setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_BOTH));
    }

    private void createValueSetArea(UIToolkit toolkit, Composite parent) {
        valueSetArea = toolkit.createComposite(parent);
        GridData stackData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_BOTH);
        valueSetArea.setLayoutData(stackData);
        valueSetArea.setLayout(new StackLayout());
    }

    /**
     * Returns the value set being edited.
     */
    public IValueSet getValueSet() {
        return valueSetOwner.getValueSet();
    }

    /**
     * Returns the type of the value set being edited.
     */
    public ValueSetType getValueSetType() {
        return valueSetOwner.getValueSet().getValueSetType();
    }

    /**
     * Selects the given value set type. Or the first item if the value set type is not in the list
     * of available item.
     */
    public void setValueSetType(ValueSetType valueSetType) {
        valueSetTypeField.setText(valueSetType.getName());
        // value set in the valueSetOwner is updated through event handling
    }

    /**
     * Returns the edit mode being used.
     */
    public ValueSetControlEditMode getEditMode() {
        return editMode;
    }

    /**
     * Sets the new edit mode. If the new edit mode does not allow to edit abstract value sets, but
     * the current value set being edited is abstract, it is set to not abstract.
     */
    public void setEditMode(ValueSetControlEditMode newEditMode) {
        if (editMode == newEditMode) {
            return;
        }
        editMode = newEditMode;
        IValueSet valueSet = getValueSet();
        if (!editMode.canDefineAbstractSets() && valueSet.isAbstract() && !valueSet.isUnrestricted()) {
            valueSet.setAbstract(false);
        }
    }

    private Control showControlForValueSet() {
        StackLayout layout = (StackLayout)valueSetArea.getLayout();
        layout.topControl = updateControlWithCurrentValueSetOrCreateNewIfNeccessary(valueSetArea);
        setDataChangeable(isDataChangeable());
        return layout.topControl;
    }

    private Control updateControlWithCurrentValueSetOrCreateNewIfNeccessary(Composite parent) {
        IValueSet valueSet = getValueSet();
        if (!isValueSetEditingAllowed(valueSet)) {
            // no further editing possible, return empty composite
            return toolkit.createComposite(parent);
        }
        ValueDatatype valueDatatype = valueSetPmo.getValueDatatype();
        if (getValueSetEditControl() != null && getValueSetEditControl().canEdit(valueSet, valueDatatype)) {
            // the current composite can be reused to edit the current value set
            getValueSetEditControl().setValueSet(valueSet, valueDatatype);
            // have to return the parent
            // here, as there is a
            // group control (see below) around the edit control. There has to be a better way to do
            // this!
            return valueSetEditControl.getComposite().getParent();
        }
        // Creates a new composite to edit the current value set
        Group group = createGroupAroundValueSet(parent, valueSet.getValueSetType().getName());
        ValueSetEditControlFactory factory = new ValueSetEditControlFactory();
        valueSetEditControl = factory.newControl(valueSet, valueDatatype, group, toolkit, bindingContext,
                valueSetOwner.getIpsProject(), valueSetPmo.sourceSet);
        valueSetEditControl.getComposite()
                .setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_BOTH));
        return group;
    }

    private boolean isValueSetEditingAllowed(IValueSet valueSet) {
        return !(valueSet.isAbstract() || valueSet.isUnrestricted() || valueSet.isDerived());
    }

    /**
     * Returns the value set control casted to {@link IValueSetEditControl}. Necessary as the SWT
     * type Control is a class and not an interface.
     */
    private IValueSetEditControl getValueSetEditControl() {
        return valueSetEditControl;
    }

    private Group createGroupAroundValueSet(Composite parent, String title) {
        Group group = toolkit.createGroup(parent, title);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 3;
        group.setLayout(layout);
        GridData labelGridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_BOTH);
        group.setLayoutData(labelGridData);
        return group;
    }

    public int getPreferredLabelWidth() {
        int width1 = valueSetTypeLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        if (concreteValueSetLabel == null) {
            return width1;
        }
        int width2 = concreteValueSetLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        return Math.max(width1, width2);
    }

    /**
     * Sets the width of the type label. The method could be used to align the control in the second
     * column with a control in one position (row) above.
     */
    public void setLabelWidthHint(int widthHint) {
        Object layoutData = valueSetTypeLabel.getLayoutData();
        if (layoutData instanceof GridData) {
            ((GridData)layoutData).widthHint = widthHint;
        }
        if (concreteValueSetLabel != null) {
            layoutData = concreteValueSetLabel.getLayoutData();
            if (layoutData instanceof GridData) {
                ((GridData)layoutData).widthHint = widthHint;
            }
        }
    }

    public Label getValueSetTypeLabel() {
        return valueSetTypeLabel;
    }

    private void createValueSetTypesCombo(UIToolkit toolkit, Composite parentArea) {
        valueSetTypeLabel = toolkit.createLabel(parentArea, Messages.ValueSetEditControl_labelType);

        valueSetTypesCombo = toolkit.createCombo(parentArea);
        valueSetTypesCombo.setText(getValueSetType().getName());
        valueSetTypeField = new StringValueComboField(valueSetTypesCombo);
        valueSetTypeField.addChangeListener(e -> {
            String selectedText = e.field.getText();
            ValueSetType newValueSetType = ValueSetType.getValueSetTypeByName(selectedText);
            changeValueSetType(newValueSetType);
        });

        toolkit.setDataChangeable(valueSetTypesCombo, isDataChangeable());
    }

    private void createConcreteValueSetCheckbox(UIToolkit toolkit, Composite parent) {
        if (!editMode.canDefineAbstractSets()) {
            // the user has no choice, so need to create a checkbox
            return;
        }
        concreteValueSetLabel = toolkit.createLabel(parent, Messages.ValueSetSpecificationControl_specifyBoundsValues);
        concreteValueSetCheckbox = toolkit.createCheckbox(parent);
        concreteValueSetField = new CheckboxField(concreteValueSetCheckbox);
        updateConcreteValueSetCheckbox();
        concreteValueSetField.addChangeListener(e -> {
            boolean checked = ((Boolean)e.field.getValue());
            getValueSet().setAbstract(!checked);
            updateUI();
        });
    }

    private void createContainsNullCheckbox(UIToolkit toolkit, Composite parent) {
        toolkit.createLabel(parent, NLS.bind(Messages.ValueSetSpecificationControl_containsNull,
                IpsPlugin.getDefault().getIpsPreferences().getNullPresentation()));
        containsNullCheckbox = toolkit.createCheckbox(parent, valueSetPmo.getRelevanceText());
        containsNullField = new CheckboxField(containsNullCheckbox);
        bindingContext.bindContent(containsNullField, valueSetPmo, ValueSetPmo.PROPERTY_CONTAINS_NULL);
        bindingContext.bindEnabled(containsNullCheckbox, valueSetPmo, ValueSetPmo.PROPERTY_CONTAINS_NULL_ENABLED);
        bindingContext.bindProblemMarker(containsNullField, valueSetPmo, ValueSetPmo.PROPERTY_CONTAINS_NULL);
        bindingContext
                .add(new ButtonTextBinding(containsNullCheckbox, valueSetPmo, ValueSetPmo.PROPERTY_RELEVANCE_TEXT));
    }

    @Override
    public boolean setFocus() {
        return valueSetTypesCombo.setFocus();
    }

    /**
     * Sets the list of value set types the user can select in the Combo box.
     */
    public void setAllowedValueSetTypes(List<ValueSetType> valueSetTypes) {
        allowedValueSetTypes.clear();
        allowedValueSetTypes.addAll(valueSetTypes);
        ValueSetType oldType = getValueSetType();
        ValueSetType newType = valueSetTypes.get(0);

        valueSetTypesCombo.removeAll();
        for (ValueSetType type : valueSetTypes) {
            valueSetTypesCombo.add(type.getName());
            if (oldType == type) {
                newType = oldType;
            }
        }
        valueSetTypeField.setText(newType.getName());
        valueSetTypesCombo.setEnabled(valueSetTypes.size() > 1);
    }

    /**
     * Returns the list of value set types the user can select in the Combo box.
     */
    public List<ValueSetType> getAllowedValueSetTypes() {
        List<ValueSetType> types = new ArrayList<>();
        types.addAll(allowedValueSetTypes);
        return types;
    }

    private void changeValueSetType(ValueSetType newValueSetType) {
        IValueSet oldValueSet = valueSetOwner.getValueSet();
        if (!oldValueSet.isUnrestricted()) {
            lastRestrictedValueSet = oldValueSet;
        }
        if (oldValueSet.getValueSetType().equals(newValueSetType)) {
            // unchanged
            return;
        }
        valueSetOwner.setValueSetType(newValueSetType);
        IValueSet newValueSet = valueSetOwner.getValueSet();
        if (lastRestrictedValueSet == null) {
            newValueSet.setAbstract(getDefaultForAbstractProperty());
        } else {
            newValueSet.setValuesOf(lastRestrictedValueSet);
        }
        updateUI();
    }

    private void updateUI() {
        showControlForValueSet();
        valueSetArea.layout();
        valueSetArea.getParent().layout();
        valueSetArea.getParent().getParent().layout();

        bindingContext.updateUI();
        updateConcreteValueSetCheckbox();
    }

    private void updateConcreteValueSetCheckbox() {
        if (concreteValueSetCheckbox != null) {
            IValueSet valueSet = valueSetOwner.getValueSet();
            boolean checked = !valueSet.isAbstract() && !valueSet.isUnrestricted();
            concreteValueSetCheckbox.setChecked(checked);
            updateConcreteValueSetCheckboxDataChangeableState();
        }
    }

    @Override
    public void setDataChangeable(boolean changeable) {
        dataChangeable = changeable && findDatatype() != null;
        toolkit.setDataChangeable(valueSetTypesCombo, dataChangeable);
        if (valueSetEditControl != null) {
            toolkit.setDataChangeable(valueSetEditControl.getComposite(), dataChangeable);
        }
        updateConcreteValueSetCheckboxDataChangeableState();
        toolkit.setDataChangeable(containsNullCheckbox, dataChangeable);
    }

    private ValueDatatype findDatatype() {
        return valueSetOwner.findValueDatatype(valueSetOwner.getIpsProject());
    }

    private void updateConcreteValueSetCheckboxDataChangeableState() {
        if (!isDataChangeable()) {
            toolkit.setDataChangeable(concreteValueSetCheckbox, false);
            return;
        }
        boolean enabled = !getValueSet().isUnrestricted() && editMode.canDefineAbstractSets();
        toolkit.setDataChangeable(concreteValueSetCheckbox, enabled);
    }

    @Override
    public boolean isDataChangeable() {
        return dataChangeable;
    }

    @Override
    public void setEnabled(boolean enable) {
        setEnabledIfExistent(valueSetTypeLabel, enable);
        setEnabledIfExistent(valueSetTypesCombo, enable);
        setEnabledIfExistent(concreteValueSetCheckbox, enable);
        if (valueSetEditControl != null) {
            setEnabledIfExistent(valueSetEditControl.getComposite(), enable);
        }
    }

    private void setEnabledIfExistent(Control control, boolean enable) {
        if (control != null && !control.isDisposed()) {
            control.setEnabled(enable);
        }
    }

    private boolean getDefaultForAbstractProperty() {
        return editMode.canDefineAbstractSets();
    }

    /**
     * An implementation of {@link IpsObjectPartPmo}. It is used for binding the state of a checkbox
     * according to the selected {@link IValueSet}.
     */
    public static class ValueSetPmo extends IpsObjectPartPmo {

        /**
         * Prefix for all message codes of this class.
         */
        public static final String MSGCODE_PREFIX = "ValueSetPmo-"; //$NON-NLS-1$
        public static final String MSG_CODE_NULL_NOT_ALLOWED = MSGCODE_PREFIX + "nullNotAllowed"; //$NON-NLS-1$
        public static final String PROPERTY_CONTAINS_NULL_ENABLED = "containsNullEnabled"; //$NON-NLS-1$
        public static final String PROPERTY_CONTAINS_NULL = IValueSet.PROPERTY_CONTAINS_NULL;
        public static final String PROPERTY_RELEVANCE_TEXT = "relevanceText"; //$NON-NLS-1$
        private IValueSet sourceSet;

        public ValueSetPmo(IValueSetOwner valueSetOwner) {
            super(valueSetOwner);
            if (valueSetOwner instanceof IConfigElement) {
                IConfigElement configElement = (IConfigElement)valueSetOwner;
                try {
                    IPolicyCmptTypeAttribute attribute = configElement
                            .findPcTypeAttribute(configElement.getIpsProject());
                    sourceSet = attribute.getValueSet();
                } catch (CoreException e) {
                    throw new CoreRuntimeException(e);
                }
            }
        }

        public boolean isContainsNullEnabled() {
            if ((getValueSetOwner().findValueDatatype(getIpsProject()) == null) || (!getValueSet().isDerived()
                    && getValueSet().isRange() && getValueSet().isEmpty())) {
                return false;
            }
            // If the value set contains null the checkbox must remain enabled even if the source
            // set no longer contains null so that the error can be corrected by deactivating the
            // checkbox.
            boolean sourceSetAllowsNullOrValueSetContainsNull = sourceSet == null || sourceSet.isContainsNull()
                    || isContainsNull();
            return !getValueSet().isDerived() && sourceSetAllowsNullOrValueSetContainsNull
                    && (!getValueDatatype().isPrimitive() || getValueSet().isEnum());
        }

        public boolean isContainsNull() {
            return getValueSet().isContainsNull();
        }

        public void setContainsNull(boolean containsNull) {
            getValueSet().setContainsNull(containsNull);
        }

        private IValueSet getValueSet() {
            return getValueSetOwner().getValueSet();
        }

        private IValueSetOwner getValueSetOwner() {
            return (IValueSetOwner)super.getIpsObjectPartContainer();
        }

        public ValueDatatype getValueDatatype() {
            ValueDatatype datatype = getValueSetOwner().findValueDatatype(getIpsProject());
            if (datatype == null) {
                return Datatype.STRING;
            }
            return datatype;
        }

        @Override
        public MessageList validate(IIpsProject ipsProject) throws CoreRuntimeException {
            MessageList messageList = super.validate(ipsProject);
            addContainsNullMessagesIfApplicable(messageList);
            return messageList;
        }

        private void addContainsNullMessagesIfApplicable(MessageList modelMessages) {
            if (isNullIncompatible(modelMessages)) {
                addContainsNullErrorMessage(modelMessages);
            }
        }

        private boolean isNullIncompatible(MessageList modelMessages) {
            return !modelMessages.getMessagesFor(getValueSet(), IEnumValueSet.PROPERTY_CONTAINS_NULL).isEmpty();
        }

        private void addContainsNullErrorMessage(MessageList messageList) {
            String text = Messages.ValueSetSpecificationControl_Msg_NullNotAllowed;
            messageList.newError(MSG_CODE_NULL_NOT_ALLOWED, text, this, PROPERTY_CONTAINS_NULL);
        }

        public String getRelevanceText() {
            return isContainsNull() ? Messages.ValueSetSpecificationControl_RelevanceOptional
                    : Messages.ValueSetSpecificationControl_RelevanceMandatory;
        }
    }

}
