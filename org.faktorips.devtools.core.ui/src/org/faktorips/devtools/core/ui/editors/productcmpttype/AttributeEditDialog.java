/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.ButtonField;
import org.faktorips.devtools.core.ui.controller.fields.ComboViewerField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.core.ui.controls.valuesets.ValueSetControlEditMode;
import org.faktorips.devtools.core.ui.controls.valuesets.ValueSetSpecificationControl;
import org.faktorips.devtools.core.ui.editors.CategoryPmo;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.core.ui.refactor.IpsRefactoringOperation;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.SingleEventModification;
import org.faktorips.devtools.model.ipsobject.Modifier;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.valueset.ValueSetType;

/**
 * Dialog to edit a product cmpt type attribute.
 * 
 * @author Jan Ortmann
 */
public class AttributeEditDialog extends IpsPartEditDialog2 {

    /**
     * Folder which contains the pages shown by this dialog. Used to modify which page is shown.
     */
    private TabFolder folder;

    /**
     * Keep track of the content of the name field to be able to determine whether it has changed.
     */
    private final String initialName;

    private IIpsProject ipsProject;
    private IProductCmptTypeAttribute attribute;

    /**
     * placeholder for the default edit field, the edit field for the default value depends on the
     * attributes datatype
     */
    private Composite defaultEditFieldPlaceholder;
    private EditField<String> defaultValueField;

    private ValueSetSpecificationControl valueSetEditControl;

    private ValueDatatype currentDatatype;
    private ValueSetType currentValueSetType;

    private ExtensionPropertyControlFactory extFactory;
    private ProductCmptTypeAttributePmo attributePmo;

    public AttributeEditDialog(IProductCmptTypeAttribute productCmptTypeAttribute, Shell parentShell) {
        super(productCmptTypeAttribute, parentShell, Messages.AttributeEditDialog_title, true);

        attribute = productCmptTypeAttribute;
        initialName = attribute.getName();
        ipsProject = attribute.getIpsProject();

        currentDatatype = productCmptTypeAttribute.findDatatype(ipsProject);

        currentValueSetType = productCmptTypeAttribute.getValueSet().getValueSetType();
        extFactory = new ExtensionPropertyControlFactory(attribute);
        attributePmo = new ProductCmptTypeAttributePmo(attribute);
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        folder = (TabFolder)parent;

        TabItem generalItem = new TabItem(folder, SWT.NONE);
        generalItem.setText(Messages.AttributeEditDialog_properties);
        generalItem.setControl(createGeneralPage(folder));

        TabItem defaultAndValuesItem = new TabItem(folder, SWT.NONE);

        defaultAndValuesItem.setText(Messages.AttributeEditDialog_defaultAndValuesGroup);
        try {
            defaultAndValuesItem.setControl(createDefaultAndValuesPage(folder));
        } catch (CoreRuntimeException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return folder;
    }

    private void createGeneralGroupContent(Composite c) {
        Composite workArea = getToolkit().createLabelEditColumnComposite(c);
        if (extFactory.needsToCreateControlsFor(IExtensionPropertyDefinition.POSITION_TOP)) {
            extFactory.createControls(workArea, getToolkit(), attribute, IExtensionPropertyDefinition.POSITION_TOP);
        }

        getToolkit().createFormLabel(workArea, Messages.AttributeEditDialog_nameLabel);
        Text nameText = getToolkit().createText(workArea);
        nameText.setFocus();
        getBindingContext().bindContent(nameText, attribute, IIpsElement.PROPERTY_NAME);

        getToolkit().createFormLabel(workArea, ""); //$NON-NLS-1$
        final Button checkbox = getToolkit().createButton(workArea, Messages.AttributeEditDialog_overwritesNote,
                SWT.CHECK);
        getBindingContext().bindContent(checkbox, attribute, IAttribute.PROPERTY_OVERWRITES);

        getToolkit().createFormLabel(workArea, Messages.AttributeEditDialog_datatypeLabel);
        DatatypeRefControl datatypeControl = getToolkit().createDatatypeRefEdit(attribute.getIpsProject(), workArea);
        datatypeControl.setAbstractAllowed(true);
        datatypeControl.setVoidAllowed(false);
        datatypeControl.setOnlyValueDatatypesAllowed(true);
        getBindingContext().bindContent(datatypeControl, attribute, IAttribute.PROPERTY_DATATYPE);

        getToolkit().createVerticalSpacer(workArea, 0);
        Composite radioComposite = getToolkit().createGridComposite(workArea, 2, false, false);
        ButtonField singleValueRadioButtonField = new ButtonField(getToolkit().createButton(radioComposite,
                Messages.AttributeEditDialog_SingleValueRadioButton_Label, SWT.RADIO), false);
        ButtonField multiValueRadioButtonField = new ButtonField(getToolkit().createButton(radioComposite,
                Messages.AttributeEditDialog_MultiValueRadioButton_Label, SWT.RADIO), true);
        getBindingContext().bindContent(singleValueRadioButtonField, attribute,
                IProductCmptTypeAttribute.PROPERTY_MULTI_VALUE_ATTRIBUTE);
        getBindingContext().bindContent(multiValueRadioButtonField, attribute,
                IProductCmptTypeAttribute.PROPERTY_MULTI_VALUE_ATTRIBUTE);

        getToolkit().createVerticalSpacer(workArea, 0);
        Button multilingualCheckbox = getToolkit().createButton(workArea,
                Messages.AttributeEditDialog_multiLingualCheckbox, SWT.CHECK);
        getBindingContext().bindContent(multilingualCheckbox, attribute,
                IProductCmptTypeAttribute.PROPERTY_MULTILINGUAL);
        getBindingContext().bindEnabled(multilingualCheckbox, attribute,
                IProductCmptTypeAttribute.PROPERTY_MULTILINGUAL_SUPPORTED);
        multilingualCheckbox.setToolTipText(Messages.AttributeEditDialog_multiLingualCheckboxTooltip);

        getToolkit().createFormLabel(workArea, Messages.AttributeEditDialog_modifierLabel);
        Combo modifierCombo = getToolkit().createCombo(workArea);
        getBindingContext().bindContent(modifierCombo, attribute, IAttribute.PROPERTY_MODIFIER, Modifier.class);

        getToolkit().createFormLabel(workArea, ""); //$NON-NLS-1$
        Button changeOverTimeCheckbox = getToolkit().createButton(
                workArea,
                NLS.bind(Messages.AttributeEditDialog_changeOverTimeCheckbox, IpsPlugin.getDefault()
                        .getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNamePlural()),
                SWT.CHECK);
        getBindingContext().bindContent(changeOverTimeCheckbox, attribute,
                IProductCmptTypeAttribute.PROPERTY_CHANGING_OVER_TIME);

        if (extFactory.needsToCreateControlsFor(IExtensionPropertyDefinition.POSITION_BOTTOM)) {
            extFactory.createControls(workArea, getToolkit(), attribute, IExtensionPropertyDefinition.POSITION_BOTTOM);
        }
        extFactory.bind(getBindingContext());
    }

    private void createDisplayGroupContent(Composite c) {
        Composite workArea = getToolkit().createLabelEditColumnComposite(c);

        getToolkit().createFormLabel(workArea, ""); //$NON-NLS-1$
        final Button checkbox = getToolkit().createButton(workArea, Messages.AttributeEditDialog_visibilityNote,
                SWT.CHECK);
        ButtonField buttonField = new ButtonField(checkbox, false);
        getBindingContext().bindContent(buttonField, attribute, IProductCmptTypeAttribute.PROPERTY_VISIBLE);

        getToolkit().createFormLabel(workArea, Messages.AttributeEditDialog_categoryLabel);
        createCategoryCombo(workArea);
    }

    private Control createDefaultAndValuesPage(TabFolder folder) throws CoreRuntimeException {
        Composite c = createTabItemComposite(folder, 1, false);
        Composite workArea = getToolkit().createLabelEditColumnComposite(c);

        getToolkit().createFormLabel(workArea, Messages.AttributeEditDialog_defaultvalueLabel);
        defaultEditFieldPlaceholder = getToolkit().createComposite(workArea);
        defaultEditFieldPlaceholder.setLayout(getToolkit().createNoMarginGridLayout(1, true));
        defaultEditFieldPlaceholder.setLayoutData(new GridData(GridData.FILL_BOTH));
        createDefaultValueEditField();

        Composite temp = getToolkit().createGridComposite(c, 1, true, false);
        getToolkit().createLabel(temp, Messages.AttributeEditDialog_valueSetSection);
        getToolkit().createVerticalSpacer(temp, 8);
        List<ValueSetType> valueSetTypes = attribute.getAllowedValueSetTypes(attribute.getIpsProject());
        valueSetEditControl = new ValueSetSpecificationControl(temp, getToolkit(), getBindingContext(), attribute,
                valueSetTypes, ValueSetControlEditMode.ONLY_NONE_ABSTRACT_SETS);
        updateValueSetTypes();

        getBindingContext().bindEnabled(valueSetEditControl, attributePmo,
                ProductCmptTypeAttributePmo.PROPERTY_ENABLED_VALUE);

        Object layoutData = valueSetEditControl.getLayoutData();
        if (layoutData instanceof GridData) {
            /*
             * set the minimum height to show at least the maximum size of the selected
             * <code>ValueSetEditControl</code>
             */
            GridData gd = (GridData)layoutData;
            gd.heightHint = 260;
        }

        return c;
    }

    private Control createGeneralPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);

        Group generalPropertiesGroup = getToolkit().createGroup(c, Messages.AttributeEditDialog_generalGroup);
        createGeneralGroupContent(generalPropertiesGroup);

        Group displayGroup = getToolkit().createGroup(c, Messages.EditDialog_displayGroup);
        createDisplayGroupContent(displayGroup);
        return c;
    }

    private void createCategoryCombo(Composite workArea) {
        Combo categoryCombo = getToolkit().createCombo(workArea);
        ComboViewerField<IProductCmptCategory> comboViewerField = new ComboViewerField<>(
                categoryCombo, IProductCmptCategory.class);

        CategoryPmo pmo = new CategoryPmo(attribute);
        comboViewerField.setInput(pmo.getCategories());
        comboViewerField.setAllowEmptySelection(true);
        comboViewerField.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                IProductCmptCategory category = (IProductCmptCategory)element;
                return IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(category);
            }
        });

        getBindingContext().bindContent(comboViewerField, pmo, CategoryPmo.PROPERTY_CATEGORY);
        getBindingContext().bindEnabled(categoryCombo, attribute, IProductCmptTypeAttribute.PROPERTY_VISIBLE);
    }

    private void createDefaultValueEditField() {
        setDefaultValueField();
        defaultEditFieldPlaceholder.layout();
        defaultEditFieldPlaceholder.getParent().getParent().layout();
        getBindingContext().bindContent(defaultValueField, attribute, IAttribute.PROPERTY_DEFAULT_VALUE);
        getBindingContext().bindEnabled(defaultValueField.getControl(), attributePmo,
                ProductCmptTypeAttributePmo.PROPERTY_ENABLED_VALUE);
    }

    private void setDefaultValueField() {
        if (attribute.isMultiValueAttribute()) {
            createMultiValueDefaultField();
        } else {
            ValueDatatypeControlFactory datatypeCtrlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(
                    currentDatatype);
            defaultValueField = datatypeCtrlFactory.createEditField(getToolkit(), defaultEditFieldPlaceholder,
                    currentDatatype, null, ipsProject);
        }
    }

    /**
     * For multi values we allow multiple default values by separate single values with <en>"|"</en>
     */
    private void createMultiValueDefaultField() {
        Text multiValueText = getToolkit().createText(defaultEditFieldPlaceholder);
        defaultValueField = new TextField(multiValueText);
        multiValueText.setToolTipText(Messages.AttributeEditDialog_hint_multiValueDefault);
    }

    @Override
    protected void contentsChangedInternal(ContentChangeEvent event) {
        super.contentsChangedInternal(event);
        if (isOverwriteEvent(event)) {
            final IProductCmptTypeAttribute overwrittenAttribute = (IProductCmptTypeAttribute)attribute
                    .findOverwrittenAttribute(ipsProject);
            if (overwrittenAttribute != null) {
                ((IpsModel)IIpsModel.get())
                        .executeModificationsWithSingleEvent(
                                new SingleEventModification<>(attribute.getIpsSrcFile()) {

                                    @Override
                                    protected boolean execute() throws CoreRuntimeException {
                                        attribute.setDatatype(overwrittenAttribute.getDatatype());
                                        attribute.setModifier(overwrittenAttribute.getModifier());
                                        attribute.setValueSetCopy(overwrittenAttribute.getValueSet());
                                        attribute.setMultiValueAttribute(overwrittenAttribute
                                                .isMultiValueAttribute());
                                        attribute.setCategory(overwrittenAttribute.getCategory());
                                        return true;
                                    }
                                });
            }
        }
        ValueDatatype newDatatype = attribute.findDatatype(ipsProject);
        if (defaultValueField != null) {
            if (newDatatype == null || newDatatype.equals(currentDatatype)) {
                return;
            }

            currentDatatype = newDatatype;
            getBindingContext().removeBindings(defaultValueField.getControl());
            disposeChildrenOf(defaultEditFieldPlaceholder);
        }

        createDefaultValueEditField();
        updateValueSetTypes();

    }

    private void disposeChildrenOf(Composite composite) {
        for (Control control : composite.getChildren()) {
            control.dispose();
        }
    }

    private boolean isOverwriteEvent(ContentChangeEvent event) {
        return event.isAffected(attribute) && attribute.isOverwrite()
                && event.isPropertyAffected(IAttribute.PROPERTY_OVERWRITES);
    }

    private void updateValueSetTypes() throws CoreRuntimeException {
        if (valueSetEditControl == null) {
            return;
        }
        currentValueSetType = valueSetEditControl.getValueSetType();
        valueSetEditControl.setAllowedValueSetTypes(attribute.getAllowedValueSetTypes(ipsProject));
        if (currentValueSetType != null) {
            /*
             * If the previous selection was a valid selection use this one as new selection in drop
             * down, otherwise the default (first one) is selected.
             */
            valueSetEditControl.setValueSetType(currentValueSetType);
        }
        valueSetEditControl.setDataChangeable(true);
    }

    @Override
    protected void okPressed() {
        if (IpsPlugin.getDefault().getIpsPreferences().isRefactoringModeDirect()) {
            String newName = attribute.getName();
            if (!(newName.equals(initialName))) {
                applyRenameRefactoring(newName);
            }
        }
        super.okPressed();
    }

    private void applyRenameRefactoring(String newName) {
        // First, reset the initial name as otherwise the error 'names must not equal' will occur
        attribute.setName(initialName);

        IIpsRefactoring ipsRenameRefactoring = IpsPlugin.getIpsRefactoringFactory().createRenameRefactoring(attribute,
                newName, null, false);
        IpsRefactoringOperation refactoringOperation = new IpsRefactoringOperation(ipsRenameRefactoring, getShell());
        refactoringOperation.runDirectExecution();
    }

    /**
     * Presentation model object for the {@link IProductCmptTypeAttribute}. Provides some properties
     * that are not directly accessible in the model but may be derived from the model. It is used
     * for example for binding the enable state of a control to multiple properties of the
     * attribute.
     * 
     * @author frank
     * @since 3.9
     */
    public static class ProductCmptTypeAttributePmo extends IpsObjectPartPmo {

        public static final String PROPERTY_ENABLED_VALUE = "enabledDefaultAndValueset"; //$NON-NLS-1$

        public ProductCmptTypeAttributePmo(IProductCmptTypeAttribute attribute) {
            super(attribute);
        }

        @Override
        public IProductCmptTypeAttribute getIpsObjectPartContainer() {
            return (IProductCmptTypeAttribute)super.getIpsObjectPartContainer();
        }

        /**
         * Returns the enabled state for the default and valueset controls. Returns
         * <code>true</code> if datatype is correct and the multilingual is not set. Otherwise
         * returns <code>false</code>.
         */
        public boolean isEnabledDefaultAndValueset() {
            boolean enabled = true;
            ValueDatatype newDatatype = this.getIpsObjectPartContainer().findDatatype(
                    getIpsObjectPartContainer().getIpsProject());
            enabled = newDatatype != null && !this.getIpsObjectPartContainer().isMultilingual();
            return enabled;
        }
    }

}
