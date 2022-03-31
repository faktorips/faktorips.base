/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.filter.IPropertyVisibleController;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.devtools.model.type.ProductCmptPropertyType;

/**
 * Provides a generic section for all kinds of property values.
 * <p>
 * A section of this type features a two-column layout displaying a label (usually representing a
 * property value's caption) on the left hand side and an {@link EditPropertyValueComposite} on the
 * right hand side.
 * <p>
 * The property values to display are provided to the section via the constructor.
 * <p>
 * <strong>Subclassing:</strong><br>
 * The only reason this class is abstract is to allow concrete classes to provide the section title.
 * This should be done by overriding {@link #getSectionTitle()}. Calling {@link #setText(String)} is
 * not safe, as the section title is dynamic in the sense that it might be refreshed by the
 * superclass from time to time.
 * <p>
 * Subclasses must not forget to invoke {@link #initControls()} within the subclass constructor
 * (usually this is the subclass constructor's last statement).
 * 
 * @since 3.6
 * 
 * @see EditPropertyValueComposite
 * @see IPropertyValue
 */
public abstract class ProductCmptPropertySection extends IpsSection {

    private final List<IPropertyValue> propertyValues;

    /**
     * Pane which serves as parent for all controls created inside this section.
     */
    private Composite rootPane;

    private final IPropertyVisibleController visibilityController;

    /**
     * Creates a {@link ProductCmptPropertySection} that can be expanded and collapsed by the user.
     * <p>
     * <strong>Subclassing:</strong><br>
     * This constructor calls {@link #setInitCollapsedIfNoContent(boolean)} with true as argument.
     * 
     * @param id a unique {@link String} that is used as key to store the section's expanded state
     *            in the preference store
     * @param propertyValues list containing the property values to be displayed by this section
     */
    protected ProductCmptPropertySection(String id, List<IPropertyValue> propertyValues, Composite parent,
            int layoutData, UIToolkit toolkit, IPropertyVisibleController visibilityController) {

        super(id, parent, layoutData, toolkit);

        this.propertyValues = propertyValues;
        this.visibilityController = visibilityController;

        setInitCollapsedIfNoContent(true);
    }

    /**
     * Creates a {@link ProductCmptPropertySection} that cannot be expanded and collapsed by the
     * user.
     * <p>
     * <strong>Subclassing:</strong><br>
     * This constructor calls {@link #setInitCollapsedIfNoContent(boolean)} with true as argument.
     * 
     * @param propertyValues list containing the property values to be displayed by this section
     */
    protected ProductCmptPropertySection(List<IPropertyValue> propertyValues, Composite parent, int style,
            int layoutData, UIToolkit toolkit, IPropertyVisibleController visibilityController) {

        super(parent, style, layoutData, toolkit);

        this.propertyValues = propertyValues;
        this.visibilityController = visibilityController;

        setInitCollapsedIfNoContent(true);
    }

    @Override
    protected void initClientComposite(Composite parent, UIToolkit toolkit) {
        setLayout(parent);
        createRootPane(parent, toolkit);
        createEditControls();
    }

    private void setLayout(Composite composite) {
        GridLayout layout = new GridLayout(1, true);
        layout.marginWidth = 1;
        layout.marginHeight = 2;
        composite.setLayout(layout);
    }

    private void createRootPane(Composite parent, UIToolkit toolkit) {
        rootPane = toolkit.createLabelEditColumnComposite(parent);

        GridLayout rootPaneLayout = (GridLayout)rootPane.getLayout();
        rootPaneLayout.marginHeight = 5;
        rootPaneLayout.marginWidth = 5;
        rootPane.setLayoutData(new GridData(GridData.FILL_BOTH));

        toolkit.paintBordersForComposite(rootPane);
    }

    private void createEditControls() {
        if (hasContentToDisplay()) {
            // Create a label and an appropriate edit composite for each property value
            for (IPropertyValue propertyValue : propertyValues) {
                createLabelAndEditComposite(propertyValue);
            }
            // Remove all visible bindings as soon as the section is disposed
            addDisposeListener($ -> visibilityController.removePropertyControlMapping(ProductCmptPropertySection.this));
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
        final IProductCmptProperty property = findProperty(propertyValue);

        Control label = createLabel(propertyValue, property);
        EditPropertyValueComposite<?, ?> editComposite = createEditComposite(propertyValue, property);

        if (property != null) {
            visibilityController.addPropertyControlMapping(this, property, label, editComposite);
        }

        verticallyAlignLabel(label, editComposite);
    }

    private IProductCmptProperty findProperty(IPropertyValue propertyValue) {
        IProductCmptProperty property = null;
        try {
            property = propertyValue.findProperty(propertyValue.getIpsProject());
        } catch (IpsException e) {
            // Log exception, property remains null
            IpsPlugin.log(e);
        }
        return property;
    }

    private Control createLabel(IPropertyValue propertyValue, IProductCmptProperty property) {
        PropertyValueUI propertyValueUI = PropertyValueUI.getValueByPropertyType(propertyValue.getPropertyValueType());

        Control label = propertyValueUI.createLabel(propertyValue, rootPane, getToolkit());
        // Use description of property as tooltip if available
        if (property != null) {
            label.setToolTipText(IIpsModel.get().getMultiLanguageSupport().getLocalizedDescription(property));
        }
        return label;
    }

    private EditPropertyValueComposite<?, ?> createEditComposite(IPropertyValue propertyValue,
            IProductCmptProperty property) {

        PropertyValueUI propertyValueUI = PropertyValueUI.getValueByPropertyType(propertyValue.getPropertyValueType());
        return propertyValueUI.createEditComposite(property, propertyValue, this, rootPane, getBindingContext(),
                getToolkit());
    }

    private void verticallyAlignLabel(Control label, EditPropertyValueComposite<?, ?> editComposite) {
        if (label != null && editComposite != null) {
            /*
             * Vertically indent the label so it is of the same height as the first control of the
             * edit composite. The magnitude of the indentation depends on the height and margin of
             * the first control within the edit composite.
             */
            ((GridData)label.getLayoutData()).verticalAlignment = SWT.TOP;
            int topOfControlToLabelPixels = editComposite.getFirstControlHeight()
                    - label.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
            ((GridData)label.getLayoutData()).verticalIndent = ((GridLayout)editComposite.getLayout()).marginHeight
                    + topOfControlToLabelPixels / 2;
        }
    }

    /**
     * <strong>Subclassing:</strong><br>
     * This implementation returns whether the list of property values given to the section via the
     * constructor is not empty.
     */
    @Override
    protected boolean hasContentToDisplay() {
        return !propertyValues.isEmpty();
    }

    /**
     * Provides the user interface for each kind of {@link IPropertyValue}, that is an appropriate
     * label and {@link EditPropertyValueComposite}.
     * 
     * @see IPropertyValue
     * @see EditPropertyValueComposite
     */
    private static enum PropertyValueUI {

        ATTRIBUTE_VALUE() {
            @Override
            public EditPropertyValueComposite<?, ?> createEditComposite(IProductCmptProperty property,
                    IPropertyValue propertyValue,
                    ProductCmptPropertySection propertySection,
                    Composite parent,
                    BindingContext bindingContext,
                    UIToolkit toolkit) {

                return new AttributeValueEditComposite((IProductCmptTypeAttribute)property,
                        (IAttributeValue)propertyValue, propertySection, parent, bindingContext, toolkit);
            }
        },

        TABLE_CONTENT_USAGE() {
            @Override
            public Control createLabel(IPropertyValue propertyValue, Composite parent, UIToolkit toolkit) {
                Hyperlink hyperlink = toolkit.createHyperlink(parent,
                        IIpsModel.get().getMultiLanguageSupport().getLocalizedCaption(propertyValue));
                addOpenEditorHyperlinkListener(propertyValue, hyperlink);
                return hyperlink;
            }

            private void addOpenEditorHyperlinkListener(final IPropertyValue propertyValue, Hyperlink hyperlink) {
                hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
                    @Override
                    public void linkActivated(HyperlinkEvent event) {
                        ITableContents tableContents = ((ITableContentUsage)propertyValue)
                                .findTableContents(propertyValue.getIpsProject());
                        if (tableContents != null) {
                            IpsUIPlugin.getDefault().openEditor(tableContents.getIpsSrcFile());
                        }
                    }
                });
            }

            @Override
            public EditPropertyValueComposite<?, ?> createEditComposite(IProductCmptProperty property,
                    IPropertyValue propertyValue,
                    ProductCmptPropertySection propertySection,
                    Composite parent,
                    BindingContext bindingContext,
                    UIToolkit toolkit) {

                return new TableContentUsageEditComposite((ITableStructureUsage)property,
                        (ITableContentUsage)propertyValue, propertySection, parent, bindingContext, toolkit);
            }
        },

        VALIDATION_RULE_CONFIG() {
            @Override
            public EditPropertyValueComposite<?, ?> createEditComposite(IProductCmptProperty property,
                    IPropertyValue propertyValue,
                    ProductCmptPropertySection propertySection,
                    Composite parent,
                    BindingContext bindingContext,
                    UIToolkit toolkit) {

                return new ValidationRuleConfigEditComposite((IValidationRule)property,
                        (IValidationRuleConfig)propertyValue, propertySection, parent, bindingContext, toolkit);
            }

            @Override
            public Control createLabel(IPropertyValue propertyValue, Composite parent, UIToolkit toolkit) {
                // We do not show any label for rules, the label of the rule is shown in the
                // checkbox
                return toolkit.createVerticalSpacer(parent, 0);
            }
        },

        FORMULA() {
            @Override
            public EditPropertyValueComposite<?, ?> createEditComposite(IProductCmptProperty property,
                    IPropertyValue propertyValue,
                    ProductCmptPropertySection propertySection,
                    Composite parent,
                    BindingContext bindingContext,
                    UIToolkit toolkit) {

                return new FormulaEditComposite((IProductCmptTypeMethod)property, (IFormula)propertyValue,
                        propertySection, parent, bindingContext, toolkit);
            }
        },

        CONFIGURED_VALUESET() {
            @Override
            public EditPropertyValueComposite<?, ?> createEditComposite(IProductCmptProperty property,
                    IPropertyValue propertyValue,
                    ProductCmptPropertySection propertySection,
                    Composite parent,
                    BindingContext bindingContext,
                    UIToolkit toolkit) {

                return new ConfiguredValueSetEditComposite((IPolicyCmptTypeAttribute)property,
                        (IConfiguredValueSet)propertyValue, propertySection, parent, bindingContext, toolkit);
            }

            @Override
            public Control createLabel(IPropertyValue propertyValue, Composite parent, UIToolkit toolkit) {
                IProductCmptProperty property = propertyValue.findProperty(propertyValue.getIpsProject());
                String label;
                if (property != null) {
                    label = IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(property);
                } else {
                    label = propertyValue.getPropertyName();
                }
                return toolkit.createLabel(parent, label);
            }

        },

        CONFIGURED_DEFAULT() {
            @Override
            public EditPropertyValueComposite<?, ?> createEditComposite(IProductCmptProperty property,
                    IPropertyValue propertyValue,
                    ProductCmptPropertySection propertySection,
                    Composite parent,
                    BindingContext bindingContext,
                    UIToolkit toolkit) {

                return new ConfiguredDefaultEditComposite((IPolicyCmptTypeAttribute)property,
                        (IConfiguredDefault)propertyValue, propertySection, parent, bindingContext, toolkit);
            }

            @Override
            public Control createLabel(IPropertyValue propertyValue, Composite parent, UIToolkit toolkit) {
                // We do not show any label for default values, the label is already set by the
                // value set
                return toolkit.createVerticalSpacer(parent, 0);
            }

        };

        /**
         * Returns the {@link PropertyValueUI} corresponding to the provided
         * {@link ProductCmptPropertyType} or null if no {@link PropertyValueUI} exists for the
         * provided {@link ProductCmptPropertyType}.
         */
        public static PropertyValueUI getValueByPropertyType(PropertyValueType propertyType) {
            switch (propertyType) {
                case ATTRIBUTE_VALUE:
                    return ATTRIBUTE_VALUE;
                case VALIDATION_RULE_CONFIG:
                    return VALIDATION_RULE_CONFIG;
                case FORMULA:
                    return FORMULA;
                case TABLE_CONTENT_USAGE:
                    return TABLE_CONTENT_USAGE;
                case CONFIGURED_VALUESET:
                    return CONFIGURED_VALUESET;
                case CONFIGURED_DEFAULT:
                    return CONFIGURED_DEFAULT;
            }
            return null;
        }

        /**
         * Creates and returns an appropriate label for this kind of {@link IPropertyValue}.
         * <p>
         * Note that this method returns a {@link Control} and not a {@link Label} in order to
         * support hyperlinks as well.
         */
        public Control createLabel(IPropertyValue propertyValue, Composite parent, UIToolkit toolkit) {
            String localizedCaption = IIpsModel.get().getMultiLanguageSupport().getLocalizedCaption(propertyValue);
            return toolkit.createLabel(parent, localizedCaption);
        }

        /**
         * Creates and returns an appropriate {@link EditPropertyValueComposite} for this kind of
         * {@link IPropertyValue}.
         */
        public abstract EditPropertyValueComposite<?, ?> createEditComposite(IProductCmptProperty property,
                IPropertyValue propertyValue,
                ProductCmptPropertySection propertySection,
                Composite parent,
                BindingContext bindingContext,
                UIToolkit toolkit);

    }

}
