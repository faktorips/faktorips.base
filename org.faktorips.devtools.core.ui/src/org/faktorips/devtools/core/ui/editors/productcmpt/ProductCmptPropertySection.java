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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.forms.IpsSection;

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
 * @author Alexander Weickmann, Faktor Zehn AG
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
            int layoutData, UIToolkit toolkit) {

        super(id, parent, layoutData, toolkit);

        this.propertyValues = propertyValues;

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
            int layoutData, UIToolkit toolkit) {

        super(parent, style, layoutData, toolkit);

        this.propertyValues = propertyValues;

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
            addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(DisposeEvent e) {
                    IpsUIPlugin.getDefault().getPropertyVisibleController()
                            .removePropertyControlMapping(ProductCmptPropertySection.this);
                }
            });
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
            IpsUIPlugin.getDefault().getPropertyVisibleController()
                    .addPropertyControlMapping(this, property, label, editComposite);
        }

        verticallyAlignLabel(label, editComposite);
    }

    private IProductCmptProperty findProperty(IPropertyValue propertyValue) {
        IProductCmptProperty property = null;
        try {
            property = propertyValue.findProperty(propertyValue.getIpsProject());
        } catch (CoreException e) {
            // Log exception, property remains null
            IpsPlugin.log(e);
        }
        return property;
    }

    private Control createLabel(IPropertyValue propertyValue, IProductCmptProperty property) {
        PropertyValueUI propertyValueUI = PropertyValueUI.getValueByPropertyType(propertyValue.getPropertyType());

        Control label = propertyValueUI.createLabel(propertyValue, rootPane, getToolkit());
        // Use description of property as tooltip if available
        if (property != null) {
            label.setToolTipText(IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(property));
        }
        return label;
    }

    private EditPropertyValueComposite<?, ?> createEditComposite(IPropertyValue propertyValue,
            IProductCmptProperty property) {

        PropertyValueUI propertyValueUI = PropertyValueUI.getValueByPropertyType(propertyValue.getPropertyType());
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
                    - label.computeSize(SWT.DEFAULT, SWT.DEFAULT).y + editComposite.getFirstControlMarginHeight();
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
                Hyperlink hyperlink = toolkit.createHyperlink(parent, IpsPlugin.getMultiLanguageSupport()
                        .getLocalizedCaption(propertyValue));
                addOpenEditorHyperlinkListener(propertyValue, hyperlink);
                return hyperlink;
            }

            private void addOpenEditorHyperlinkListener(final IPropertyValue propertyValue, Hyperlink hyperlink) {
                hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
                    @Override
                    public void linkActivated(HyperlinkEvent event) {
                        try {
                            ITableContents tableContents = ((ITableContentUsage)propertyValue)
                                    .findTableContents(propertyValue.getIpsProject());
                            if (tableContents != null) {
                                IpsUIPlugin.getDefault().openEditor(tableContents.getIpsSrcFile());
                            }
                        } catch (CoreException e) {
                            throw new RuntimeException(e);
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

        CONFIG_ELEMENT() {
            @Override
            public EditPropertyValueComposite<?, ?> createEditComposite(IProductCmptProperty property,
                    IPropertyValue propertyValue,
                    ProductCmptPropertySection propertySection,
                    Composite parent,
                    BindingContext bindingContext,
                    UIToolkit toolkit) {

                return new ConfigElementEditComposite((IPolicyCmptTypeAttribute)property,
                        (IConfigElement)propertyValue, propertySection, parent, bindingContext, toolkit);
            }
        };

        /**
         * Returns the {@link PropertyValueUI} corresponding to the provided
         * {@link ProductCmptPropertyType} or null if no {@link PropertyValueUI} exists for the
         * provided {@link ProductCmptPropertyType}.
         */
        public static PropertyValueUI getValueByPropertyType(ProductCmptPropertyType propertyType) {
            switch (propertyType) {
                case PRODUCT_CMPT_TYPE_ATTRIBUTE:
                    return ATTRIBUTE_VALUE;
                case VALIDATION_RULE:
                    return VALIDATION_RULE_CONFIG;
                case FORMULA_SIGNATURE_DEFINITION:
                    return FORMULA;
                case TABLE_STRUCTURE_USAGE:
                    return TABLE_CONTENT_USAGE;
                case POLICY_CMPT_TYPE_ATTRIBUTE:
                    return CONFIG_ELEMENT;
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
            String localizedCaption = IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(propertyValue);
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
