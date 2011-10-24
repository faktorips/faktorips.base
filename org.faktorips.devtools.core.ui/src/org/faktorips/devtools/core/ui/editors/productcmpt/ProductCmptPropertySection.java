/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.TimedEnumDatatypeUtil;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetFilter;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.OverlayIcons;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.CompositeUIController;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.PreviewTextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.FormulaEditControl;
import org.faktorips.devtools.core.ui.controls.TableContentsUsageRefControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.message.ObjectProperty;

/**
 * Provides a generic section for all kinds of {@link IPropertyValue}s.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 */
public class ProductCmptPropertySection extends IpsSection {

    private final IProductCmptCategory category;

    private final IProductCmptGeneration generation;

    private final List<IPropertyValue> propertyValues;

    /**
     * Controller to handle update of UI and model automatically.
     */
    private final CompositeUIController uiMasterController;

    /**
     * Pane which serves as parent for all controls created inside this section.
     */
    private Composite rootPane;

    public ProductCmptPropertySection(IProductCmptCategory category, IProductCmptGeneration generation,
            List<IPropertyValue> propertyValues, Composite parent, UIToolkit toolkit) {

        super(category.getId(), parent, category.isAtLeftPosition() ? GridData.FILL_BOTH : GridData.FILL_HORIZONTAL
                | GridData.VERTICAL_ALIGN_FILL, toolkit);

        this.category = category;
        this.generation = generation;
        this.propertyValues = propertyValues;

        uiMasterController = new CompositeUIController();

        /*
         * The following call is necessary in addition to the above layout data constants because of
         * the relayoutSection(boolean) method.
         */
        if (category.isAtRightPosition()) {
            setGrabVerticalSpace(false);
        }

        setInitCollapsedIfNoContent(true);
        initControls();
    }

    @Override
    protected String getSectionTitle() {
        return category.getName();
    }

    @Override
    protected int getNumberOfElementsToDisplayInSectionTitle() {
        return propertyValues.size();
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        GridLayout layout = new GridLayout(1, true);
        layout.marginHeight = 2;
        layout.marginWidth = 1;
        client.setLayout(layout);

        rootPane = toolkit.createLabelEditColumnComposite(client);
        rootPane.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL | GridData.GRAB_HORIZONTAL));
        GridLayout workAreaLayout = (GridLayout)rootPane.getLayout();
        workAreaLayout.marginHeight = 5;
        workAreaLayout.marginWidth = 5;

        /*
         * Following line forces the paint listener to draw a light grey border around the text
         * control. Can only be understood by looking at the FormToolkit$PaintBorder class.
         */
        rootPane.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
        toolkit.getFormToolkit().paintBordersFor(rootPane);

        createEditControls();
        uiMasterController.updateUI();
    }

    private void createEditControls() {
        // Create a label and an edit composite for each property
        if (hasContentToDisplay()) {
            for (IPropertyValue propertyValue : propertyValues) {
                createLabelAndEditComposite(propertyValue);
            }
        } else {
            createLabelForEmptySection();
        }

        rootPane.layout(true);
        rootPane.redraw();
    }

    private void createLabelForEmptySection() {
        getToolkit().createLabel(rootPane, Messages.ProductCmptPropertySection_NoContentToDisplay);
    }

    private void createLabelAndEditComposite(IPropertyValue propertyValue) {
        Control label = createLabel(propertyValue);

        IpsObjectUIController controller = new IpsObjectUIController(propertyValue);
        uiMasterController.add(controller);

        try {
            IProductCmptProperty property = propertyValue.findProperty(propertyValue.getIpsProject());
            if (property != null) {
                // Use description of property as tooltip
                label.setToolTipText(IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(property));

                EditPropertyValueComposite<?, ?> editComposite = createEditComposite(property, propertyValue);

                /*
                 * Vertically indent the label so it does not stick at the very top of the
                 * composite. The magnitude of the indentation depends on the height of the edit
                 * composite.
                 */
                ((GridData)label.getLayoutData()).verticalAlignment = SWT.TOP;
                int topOfControlToLabelPixels = editComposite.controlHeight
                        - label.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
                ((GridData)label.getLayoutData()).verticalIndent = ((GridLayout)editComposite.getLayout()).marginHeight
                        + topOfControlToLabelPixels + 4;
            } else {
                createEmptyComposite();
            }

        } catch (CoreException e) {
            // Log exception and create an empty composite for this property value
            IpsPlugin.log(e);
            createEmptyComposite();
        }
    }

    private Control createLabel(IPropertyValue propertyValue) {
        if (propertyValue instanceof ITableContentUsage) {
            return createHyperlink((ITableContentUsage)propertyValue);
        }
        String localizedCaption = IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(propertyValue);
        return getToolkit().createLabel(rootPane, localizedCaption);
    }

    private Control createHyperlink(final ITableContentUsage tcu) {
        String localizedCaption = IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(tcu);
        Hyperlink hyperlink = getToolkit().createHyperlink(rootPane, localizedCaption);
        hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            public void linkActivated(HyperlinkEvent event) {
                try {
                    ITableContents tc = tcu.findTableContents(generation.getIpsProject());
                    if (tc != null) {
                        IpsUIPlugin.getDefault().openEditor(tc.getIpsSrcFile());
                    }
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        });
        return hyperlink;
    }

    private EditPropertyValueComposite<?, ?> createEditComposite(IProductCmptProperty property,
            IPropertyValue propertyValue) {

        EditPropertyValueComposite<?, ?> editComposite = null;
        switch (property.getProductCmptPropertyType()) {
            case PRODUCT_CMPT_TYPE_ATTRIBUTE:
                editComposite = new AttributeValueEditComposite((IProductCmptTypeAttribute)property,
                        (IAttributeValue)propertyValue);
                break;
            case TABLE_STRUCTURE_USAGE:
                editComposite = new TableContentUsageEditComposite((ITableStructureUsage)property,
                        (ITableContentUsage)propertyValue);
                break;
            case FORMULA_SIGNATURE_DEFINITION:
                editComposite = new FormulaEditComposite((IProductCmptTypeMethod)property, (IFormula)propertyValue);
                break;
            case POLICY_CMPT_TYPE_ATTRIBUTE:
                editComposite = new ConfigElementEditComposite((IPolicyCmptTypeAttribute)property,
                        (IConfigElement)propertyValue);
                break;
            case VALIDATION_RULE:
                editComposite = new ValidationRuleConfigEditComposite((IValidationRule)property,
                        (IValidationRuleConfig)propertyValue);
                break;
        }
        return editComposite;
    }

    private void createEmptyComposite() {
        getToolkit().createComposite(rootPane);
    }

    @Override
    protected void performRefresh() {
        uiMasterController.updateUI();
    }

    @Override
    protected boolean hasContentToDisplay() {
        return !propertyValues.isEmpty();
    }

    private abstract class EditPropertyValueComposite<P extends IProductCmptProperty, V extends IPropertyValue> extends
            Composite {

        protected final P property;

        protected final V propertyValue;

        protected final IpsObjectUIController controller;

        protected int controlHeight = -1;

        public EditPropertyValueComposite(P property, V propertyValue) {
            super(rootPane, SWT.NONE);

            this.property = property;
            this.propertyValue = propertyValue;

            controller = new IpsObjectUIController(propertyValue);
            uiMasterController.add(controller);
        }

        /**
         * Creates this composite and must be called by subclasses directly after subclass-specific
         * data has been initialized by the subclass constructor.
         */
        protected final void initControls() {
            setLayout();
            setLayoutData();

            Map<EditField<?>, ObjectProperty> editFieldsToObjectProperties = new LinkedHashMap<EditField<?>, ObjectProperty>();
            try {
                createEditFields(editFieldsToObjectProperties);
            } catch (CoreException e) {
                // Log exception and do not add any edit fields
                IpsPlugin.log(e);
            }
            for (EditField<?> editField : editFieldsToObjectProperties.keySet()) {
                if (controlHeight == -1) {
                    controlHeight = editField.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
                }
                ObjectProperty objectProperty = editFieldsToObjectProperties.get(editField);
                controller.add(editField, objectProperty.getObject(), objectProperty.getProperty());
                addFocusControl(editField.getControl());
                addEditFieldDecorators(editField);
            }
        }

        protected void setLayout() {
            GridLayout clientLayout = new GridLayout(1, false);
            clientLayout.marginHeight = 2;
            clientLayout.marginWidth = 1;
            setLayout(clientLayout);
        }

        private void setLayoutData() {
            setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        }

        /**
         * Subclasses must create the edit fields that constitute this edit composite.
         * <p>
         * Every edit field must be added as key to the provided map with the associated value being
         * the edited property.
         * 
         * @param editFieldsToObjectProperties Map to associate each created edit field with the
         *            object and the property it edits
         * 
         * @throws CoreException May throw this kind of exception at any time
         */
        protected abstract void createEditFields(Map<EditField<?>, ObjectProperty> editFieldsToObjectProperties)
                throws CoreException;

        /**
         * Subclasses may override this method to add decorators to the given edit field.
         * <p>
         * The default implementation does nothing.
         * 
         * @param editField The edit field to decorate
         */
        protected void addEditFieldDecorators(EditField<?> editField) {
            // Empty default implementation
        }

    }

    private final class AttributeValueEditComposite extends
            EditPropertyValueComposite<IProductCmptTypeAttribute, IAttributeValue> {

        public AttributeValueEditComposite(IProductCmptTypeAttribute property, IAttributeValue propertyValue) {
            super(property, propertyValue);
            initControls();
        }

        @Override
        protected void createEditFields(Map<EditField<?>, ObjectProperty> editFieldsToObjectProperties)
                throws CoreException {

            createValueEditField(editFieldsToObjectProperties);
        }

        private void createValueEditField(Map<EditField<?>, ObjectProperty> editFieldsToObjectProperties)
                throws CoreException {

            ValueDatatype datatype = property.findDatatype(property.getIpsProject());
            ValueDatatypeControlFactory controlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(
                    datatype);
            EditField<String> editField = controlFactory.createEditField(getToolkit(), this, datatype,
                    property.getValueSet(), property.getIpsProject());

            editFieldsToObjectProperties.put(editField, new ObjectProperty(propertyValue,
                    IAttributeValue.PROPERTY_VALUE));
        }

        @Override
        protected void addEditFieldDecorators(EditField<?> editField) {
            addChangeOverTimeControlDecoration(editField);
        }

        private void addChangeOverTimeControlDecoration(EditField<?> editField) {
            ControlDecoration controlDecoration = new ControlDecoration(editField.getControl(), SWT.LEFT | SWT.TOP);
            controlDecoration
                    .setDescriptionText(NLS.bind(
                            Messages.AttributeValuesSection_attributeNotChangingOverTimeDescription, IpsPlugin
                                    .getDefault().getIpsPreferences().getChangesOverTimeNamingConvention()
                                    .getGenerationConceptNamePlural()));
            controlDecoration.setImage(IpsUIPlugin.getImageHandling()
                    .getImage(OverlayIcons.NOT_CHANGEOVERTIME_OVR_DESC));
            controlDecoration.setMarginWidth(1);
        }

    }

    private final class TableContentUsageEditComposite extends
            EditPropertyValueComposite<ITableStructureUsage, ITableContentUsage> {

        public TableContentUsageEditComposite(ITableStructureUsage property, ITableContentUsage propertyValue) {
            super(property, propertyValue);
            initControls();
        }

        @Override
        protected void createEditFields(Map<EditField<?>, ObjectProperty> editFieldsToEditedProperties) {
            createTableContentEditField(editFieldsToEditedProperties);
        }

        private void createTableContentEditField(Map<EditField<?>, ObjectProperty> editFieldsToEditedProperties) {
            TableContentsUsageRefControl tcuControl = new TableContentsUsageRefControl(property.getIpsProject(), this,
                    getToolkit(), property);
            TextButtonField editField = new TextButtonField(tcuControl);

            editFieldsToEditedProperties.put(editField, new ObjectProperty(propertyValue,
                    ITableContentUsage.PROPERTY_TABLE_CONTENT));
        }

    }

    private final class ValidationRuleConfigEditComposite extends
            EditPropertyValueComposite<IValidationRule, IValidationRuleConfig> {

        public ValidationRuleConfigEditComposite(IValidationRule property, IValidationRuleConfig propertyValue) {
            super(property, propertyValue);
            initControls();
        }

        @Override
        protected void createEditFields(Map<EditField<?>, ObjectProperty> editFieldsToEditedProperties) {
            createActiveEditField(editFieldsToEditedProperties);
        }

        private void createActiveEditField(Map<EditField<?>, ObjectProperty> editFieldsToEditedProperties) {
            Checkbox checkbox = getToolkit().createCheckbox(this);
            checkbox.setChecked(propertyValue.isActive());
            CheckboxField editField = new CheckboxField(checkbox);

            editFieldsToEditedProperties.put(editField, new ObjectProperty(propertyValue,
                    IValidationRuleConfig.PROPERTY_ACTIVE));
        }

    }

    private final class FormulaEditComposite extends EditPropertyValueComposite<IProductCmptTypeMethod, IFormula> {

        public FormulaEditComposite(IProductCmptTypeMethod property, IFormula propertyValue) {
            super(property, propertyValue);
            initControls();
        }

        @Override
        protected void createEditFields(Map<EditField<?>, ObjectProperty> editFieldsToEditedProperties)
                throws CoreException {

            createExpressionEditField(editFieldsToEditedProperties);
        }

        private void createExpressionEditField(Map<EditField<?>, ObjectProperty> editFieldsToEditedProperties)
                throws CoreException {

            FormulaEditControl formulaEditControl = new FormulaEditControl(this, getToolkit(), propertyValue,
                    getShell(), ProductCmptPropertySection.this);
            FormulaCompletionProcessor completionProcessor = new FormulaCompletionProcessor(propertyValue);
            ContentAssistHandler.createHandlerForText(formulaEditControl.getTextControl(),
                    CompletionUtil.createContentAssistant(completionProcessor));
            TextButtonField editField = new TextButtonField(formulaEditControl);

            editFieldsToEditedProperties
                    .put(editField, new ObjectProperty(propertyValue, IFormula.PROPERTY_EXPRESSION));
        }

    }

    private final class ConfigElementEditComposite extends
            EditPropertyValueComposite<IPolicyCmptTypeAttribute, IConfigElement> {

        public ConfigElementEditComposite(IPolicyCmptTypeAttribute property, IConfigElement propertyValue) {
            super(property, propertyValue);
            initControls();
        }

        @Override
        protected void setLayout() {
            GridLayout clientLayout = new GridLayout(2, false);
            clientLayout.marginHeight = 2;
            clientLayout.marginWidth = 1;
            setLayout(clientLayout);
        }

        @Override
        protected void createEditFields(Map<EditField<?>, ObjectProperty> editFieldsToObjectProperties) {
            createValueSetEditField(editFieldsToObjectProperties);
            createDefaultValueEditField(editFieldsToObjectProperties);
        }

        private void createDefaultValueEditField(Map<EditField<?>, ObjectProperty> editFieldsToObjectProperties) {
            getToolkit().createLabel(this, Messages.PolicyAttributeEditDialog_defaultValue);
            ValueDatatype datatype = null;
            try {
                datatype = property.findDatatype(propertyValue.getIpsProject());
            } catch (CoreException e) {
                // Exception while searching for datatype, log exception and use String as default
                IpsPlugin.log(e);
                datatype = Datatype.STRING;
            }
            if (datatype == null) {
                // No datatype found - use String as default
                datatype = Datatype.STRING;
            }

            IValueSet sourceSet = ValueSetFilter.filterValueSet(propertyValue.getValueSet(), datatype, getGeneration()
                    .getValidFrom(), getGeneration().getValidTo(),
                    TimedEnumDatatypeUtil.ValidityCheck.SOME_TIME_OF_THE_PERIOD);
            ValueDatatypeControlFactory controlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(
                    datatype);
            EditField<String> editField = controlFactory.createEditField(getToolkit(), this, datatype, sourceSet,
                    getGeneration().getIpsProject());

            editFieldsToObjectProperties.put(editField,
                    new ObjectProperty(propertyValue, IConfigElement.PROPERTY_VALUE));
        }

        private void createValueSetEditField(Map<EditField<?>, ObjectProperty> editFieldsToObjectProperties) {
            if (areRangeValueEditFieldsRequired()) {
                createValueSetEditFieldForRange(editFieldsToObjectProperties);
            } else {
                createValueSetEditFieldForOtherThanRange(editFieldsToObjectProperties);
            }
        }

        private boolean areRangeValueEditFieldsRequired() {
            return property.getValueSet() != null ? property.getValueSet().isRange() : propertyValue.getValueSet()
                    .isRange();
        }

        private void createValueSetEditFieldForRange(Map<EditField<?>, ObjectProperty> editFieldsToObjectProperties) {
            RangeValueSet range = (RangeValueSet)propertyValue.getValueSet();
            ValueDatatypeControlFactory controlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(
                    range.getValueDatatype());

            getToolkit().createLabel(this, Messages.DefaultsAndRangesSection_minMaxStepLabel);
            Composite rangeComposite = getToolkit().createGridComposite(this, 3, false, false);

            // Need to see borders
            ((GridLayout)rangeComposite.getLayout()).marginWidth = 1;
            ((GridLayout)rangeComposite.getLayout()).marginHeight = 2;

            EditField<String> lowerField = controlFactory.createEditField(getToolkit(), rangeComposite,
                    range.getValueDatatype(), range, range.getIpsProject());
            initTextField(lowerField.getControl(), 50);

            EditField<String> upperField = controlFactory.createEditField(getToolkit(), rangeComposite,
                    range.getValueDatatype(), range, range.getIpsProject());
            initTextField(upperField.getControl(), 50);

            EditField<String> stepField = controlFactory.createEditField(getToolkit(), rangeComposite,
                    range.getValueDatatype(), range, range.getIpsProject());
            initTextField(stepField.getControl(), 50);

            getToolkit().getFormToolkit().paintBordersFor(rangeComposite);

            editFieldsToObjectProperties.put(upperField, new ObjectProperty(range, IRangeValueSet.PROPERTY_UPPERBOUND));
            editFieldsToObjectProperties.put(lowerField, new ObjectProperty(range, IRangeValueSet.PROPERTY_LOWERBOUND));
            editFieldsToObjectProperties.put(stepField, new ObjectProperty(range, IRangeValueSet.PROPERTY_STEP));
        }

        private void createValueSetEditFieldForOtherThanRange(Map<EditField<?>, ObjectProperty> editFieldsToObjectProperties) {
            getToolkit().createFormLabel(this, Messages.PolicyAttributesSection_valueSet);
            AnyValueSetControl valueSetControl = new AnyValueSetControl(this, getToolkit(), propertyValue, getShell(),
                    controller);
            valueSetControl.setDataChangeable(isDataChangeable());
            valueSetControl.setText(IpsUIPlugin.getDefault().getDatatypeFormatter()
                    .formatValueSet(propertyValue.getValueSet()));
            ((GridData)valueSetControl.getLayoutData()).widthHint = UIToolkit.DEFAULT_WIDTH;
            PreviewTextButtonField editField = new PreviewTextButtonField(valueSetControl);

            editFieldsToObjectProperties.put(editField, new ObjectProperty(propertyValue,
                    IConfigElement.PROPERTY_VALUE_SET));
        }

        private void initTextField(Control control, int widthHint) {
            if (control.getLayoutData() instanceof GridData) {
                GridData gd = (GridData)control.getLayoutData();
                gd.widthHint = widthHint;
                control.setLayoutData(gd);
            }
        }

        private IProductCmptGeneration getGeneration() {
            return propertyValue.getProductCmptGeneration();
        }

    }

}
