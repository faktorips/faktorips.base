/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
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
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.SingleEventModification;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.ButtonField;
import org.faktorips.devtools.core.ui.controller.fields.ComboViewerField;
import org.faktorips.devtools.core.ui.controller.fields.EnumTypeDatatypeField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.core.ui.controls.valuesets.ValueSetControlEditMode;
import org.faktorips.devtools.core.ui.controls.valuesets.ValueSetSpecificationControl;
import org.faktorips.devtools.core.ui.editors.CategoryPmo;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.core.ui.refactor.IpsRefactoringOperation;

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

    /** Keep track of the content of the name field to be able to determine whether it has changed. */
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

        try {
            currentDatatype = productCmptTypeAttribute.findDatatype(ipsProject);
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }

        currentValueSetType = productCmptTypeAttribute.getValueSet().getValueSetType();
        extFactory = new ExtensionPropertyControlFactory(attribute.getClass());
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
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return folder;
    }

    private void createGeneralGroupContent(Composite c) {
        Composite workArea = getToolkit().createLabelEditColumnComposite(c);
        if (extFactory.needsToCreateControlsFor(attribute, IExtensionPropertyDefinition.POSITION_TOP)) {
            extFactory.createControls(workArea, getToolkit(), attribute, IExtensionPropertyDefinition.POSITION_TOP);
        }

        getToolkit().createFormLabel(workArea, Messages.AttributeEditDialog_nameLabel);
        Text nameText = getToolkit().createText(workArea);
        nameText.setFocus();
        getBindingContext().bindContent(nameText, attribute, IIpsElement.PROPERTY_NAME);

        getToolkit().createFormLabel(workArea, ""); //$NON-NLS-1$
        final Checkbox cb = new Checkbox(workArea, getToolkit());
        cb.setText(Messages.AttributeEditDialog_overwritesNote);
        getBindingContext().bindContent(cb, attribute, IAttribute.PROPERTY_OVERWRITES);

        getToolkit().createFormLabel(workArea, Messages.AttributeEditDialog_datatypeLabel);
        DatatypeRefControl datatypeControl = getToolkit().createDatatypeRefEdit(attribute.getIpsProject(), workArea);
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
        Checkbox multilingualCheckbox = getToolkit().createCheckbox(workArea,
                Messages.AttributeEditDialog_multiLingualCheckbox);
        getBindingContext().bindContent(multilingualCheckbox, attribute,
                IProductCmptTypeAttribute.PROPERTY_MULTILINGUAL);
        getBindingContext().bindEnabled(multilingualCheckbox, attribute,
                IProductCmptTypeAttribute.PROPERTY_MULTILINGUAL_SUPPORTED);
        multilingualCheckbox.setToolTipText(Messages.AttributeEditDialog_multiLingualCheckboxTooltip);

        getToolkit().createFormLabel(workArea, Messages.AttributeEditDialog_modifierLabel);
        Combo modifierCombo = getToolkit().createCombo(workArea);
        getBindingContext().bindContent(modifierCombo, attribute, IAttribute.PROPERTY_MODIFIER, Modifier.class);

        getToolkit().createFormLabel(workArea, ""); //$NON-NLS-1$
        Checkbox changeOverTimeCheckbox = getToolkit().createCheckbox(
                workArea,
                NLS.bind(Messages.AttributeEditDialog_changeOverTimeCheckbox, IpsPlugin.getDefault()
                        .getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNamePlural()));
        getBindingContext().bindContent(changeOverTimeCheckbox, attribute,
                IProductCmptTypeAttribute.PROPERTY_CHANGING_OVER_TIME);

        if (extFactory.needsToCreateControlsFor(attribute, IExtensionPropertyDefinition.POSITION_BOTTOM)) {
            extFactory.createControls(workArea, getToolkit(), attribute, IExtensionPropertyDefinition.POSITION_BOTTOM);
        }
        extFactory.bind(getBindingContext());
    }

    private void createDisplayGroupContent(Composite c) {
        Composite workArea = getToolkit().createLabelEditColumnComposite(c);

        getToolkit().createFormLabel(workArea, ""); //$NON-NLS-1$
        final Checkbox cb = getToolkit().createCheckbox(workArea, true);
        cb.setText(Messages.AttributeEditDialog_visibilityNote);
        getBindingContext().bindContent(cb, attribute, IProductCmptTypeAttribute.PROPERTY_VISIBLE);

        getToolkit().createFormLabel(workArea, Messages.AttributeEditDialog_categoryLabel);
        createCategoryCombo(workArea);
    }

    private Control createDefaultAndValuesPage(TabFolder folder) throws CoreException {
        Composite c = createTabItemComposite(folder, 1, false);
        Composite workArea = getToolkit().createLabelEditColumnComposite(c);

        getToolkit().createFormLabel(workArea, Messages.AttributeEditDialog_defaultvalueLabel);
        defaultEditFieldPlaceholder = getToolkit().createComposite(workArea);
        defaultEditFieldPlaceholder.setLayout(getToolkit().createNoMarginGridLayout(1, true));
        defaultEditFieldPlaceholder.setLayoutData(new GridData(GridData.FILL_BOTH));
        createDefaultValueEditField();

        IpsObjectUIController uiController = new IpsObjectUIController(attribute);
        Composite temp = getToolkit().createGridComposite(c, 1, true, false);
        getToolkit().createLabel(temp, Messages.AttributeEditDialog_valueSetSection);
        getToolkit().createVerticalSpacer(temp, 8);
        List<ValueSetType> valueSetTypes = attribute.getAllowedValueSetTypes(attribute.getIpsProject());
        valueSetEditControl = new ValueSetSpecificationControl(temp, getToolkit(), uiController, attribute,
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

        Group displayGroup = getToolkit().createGroup(c, Messages.AttributeEditDialog_displayGroup);
        createDisplayGroupContent(displayGroup);
        return c;
    }

    private void createCategoryCombo(Composite workArea) {
        Combo categoryCombo = getToolkit().createCombo(workArea);
        ComboViewerField<IProductCmptCategory> comboViewerField = new ComboViewerField<IProductCmptCategory>(
                categoryCombo, IProductCmptCategory.class);

        CategoryPmo pmo = new CategoryPmo(attribute);
        comboViewerField.setInput(pmo.getCategories());
        comboViewerField.setAllowEmptySelection(true);
        comboViewerField.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                IProductCmptCategory category = (IProductCmptCategory)element;
                return IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(category);
            }
        });

        getBindingContext().bindContent(comboViewerField, pmo, CategoryPmo.PROPERTY_CATEGORY);
        getBindingContext().bindEnabled(categoryCombo, attribute, IProductCmptTypeAttribute.PROPERTY_VISIBLE);
    }

    private void createDefaultValueEditField() {
        ValueDatatypeControlFactory datatypeCtrlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(
                currentDatatype);
        defaultValueField = datatypeCtrlFactory.createEditField(getToolkit(), defaultEditFieldPlaceholder,
                currentDatatype, null, ipsProject);
        if (defaultValueField instanceof EnumTypeDatatypeField) {
            ((EnumTypeDatatypeField)defaultValueField).setEnableEnumContentDisplay(false);
        }
        defaultEditFieldPlaceholder.layout();
        defaultEditFieldPlaceholder.getParent().getParent().layout();
        defaultEditFieldPlaceholder.getParent().getParent().layout(true);
        getBindingContext().bindContent(defaultValueField, attribute, IAttribute.PROPERTY_DEFAULT_VALUE);
        getBindingContext().bindEnabled(defaultValueField.getControl(), attributePmo,
                ProductCmptTypeAttributePmo.PROPERTY_ENABLED_VALUE);
    }

    @Override
    protected void contentsChangedInternal(ContentChangeEvent event) {
        super.contentsChangedInternal(event);
        try {
            if (event.getPropertyChangeEvent() != null && event.getPart().equals(attribute) && attribute.isOverwrite()
                    && IAttribute.PROPERTY_OVERWRITES.equals(event.getPropertyChangeEvent().getPropertyName())) {
                final IProductCmptTypeAttribute overwrittenAttribute = (IProductCmptTypeAttribute)attribute
                        .findOverwrittenAttribute(ipsProject);
                if (overwrittenAttribute != null) {
                    IpsPlugin
                            .getDefault()
                            .getIpsModel()
                            .executeModificationsWithSingleEvent(
                                    new SingleEventModification<Object>(attribute.getIpsSrcFile()) {

                                        @Override
                                        protected boolean execute() throws CoreException {
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
            if (defaultValueField != null && valueSetEditControl != null) {
                if (newDatatype == null || newDatatype.equals(currentDatatype)) {
                    return;
                }

                currentDatatype = newDatatype;
                getBindingContext().removeBindings(defaultValueField.getControl());
                defaultValueField.getControl().dispose();
            }

            createDefaultValueEditField();
            updateValueSetTypes();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

    }

    private void updateValueSetTypes() throws CoreException {
        currentValueSetType = valueSetEditControl.getValueSetType();
        valueSetEditControl.setAllowedValueSetTypes(attribute.getAllowedValueSetTypes(ipsProject));
        if (currentValueSetType != null) {
            /*
             * If the previous selection was a valid selection use this one as new selection in drop
             * down, otherwise the default (first one) is selected.
             */
            valueSetEditControl.setValueSetType(currentValueSetType);
        }
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
    public class ProductCmptTypeAttributePmo extends IpsObjectPartPmo {

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
            try {
                ValueDatatype newDatatype = this.getIpsObjectPartContainer().findDatatype(ipsProject);
                enabled = newDatatype != null && !this.getIpsObjectPartContainer().isMultilingual();
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
            return enabled;
        }
    }

}
