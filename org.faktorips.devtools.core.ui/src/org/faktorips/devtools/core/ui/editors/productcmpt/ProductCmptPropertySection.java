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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
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
 * 
 * @see IPropertyValue
 * 
 * @author Alexander Weickmann
 */
public class ProductCmptPropertySection extends IpsSection {

    private final IProductCmptCategory category;

    private final List<IPropertyValue> propertyValues;

    /**
     * Pane which serves as parent for all controls created inside this section.
     */
    private Composite rootPane;

    public ProductCmptPropertySection(IProductCmptCategory category, List<IPropertyValue> propertyValues,
            Composite parent, UIToolkit toolkit) {

        super(category.getId(), parent, category.isAtLeftPosition() ? GridData.FILL_BOTH : GridData.FILL_HORIZONTAL
                | GridData.VERTICAL_ALIGN_FILL, toolkit);

        this.category = category;
        this.propertyValues = propertyValues;

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
        return IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(category);
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

        toolkit.addBorder(rootPane);
    }

    private void createEditControls() {
        if (hasContentToDisplay()) {
            // Create a label and an appropriate edit composite for each property value
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
        IProductCmptProperty property = findProperty(propertyValue);
        Control label = createLabel(propertyValue, property);
        EditPropertyValueComposite<?, ?> editComposite = createEditComposite(propertyValue, property);
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
        PropertyValueUIConfiguration propertyValueUI = PropertyValueUIConfiguration
                .getValueByPropertyType(propertyValue.getPropertyType());

        Control label = propertyValueUI.createLabel(propertyValue, rootPane, getToolkit());
        // Use description of property as tooltip if available
        if (property != null) {
            label.setToolTipText(IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(property));
        }
        return label;
    }

    private EditPropertyValueComposite<?, ?> createEditComposite(IPropertyValue propertyValue,
            IProductCmptProperty property) {

        PropertyValueUIConfiguration propertyValueUI = PropertyValueUIConfiguration
                .getValueByPropertyType(propertyValue.getPropertyType());

        EditPropertyValueComposite<?, ?> editComposite = null;
        if (property != null) {
            editComposite = propertyValueUI.createEditComposite(property, propertyValue, this, rootPane,
                    getBindingContext(), getToolkit());
        } else {
            createEmptyComposite();
        }
        return editComposite;
    }

    private void verticallyAlignLabel(Control label, EditPropertyValueComposite<?, ?> editComposite) {
        if (label != null && editComposite != null) {
            /*
             * Vertically indent the label so it does not stick at the very top of the composite.
             * The magnitude of the indentation depends on the height and margin of the edit
             * composite.
             */
            ((GridData)label.getLayoutData()).verticalAlignment = SWT.TOP;
            int topOfControlToLabelPixels = editComposite.getFirstControlHeight()
                    - label.computeSize(SWT.DEFAULT, SWT.DEFAULT).y + editComposite.getFirstControlMarginHeight();
            ((GridData)label.getLayoutData()).verticalIndent = ((GridLayout)editComposite.getLayout()).marginHeight
                    + topOfControlToLabelPixels;
        }
    }

    private void createEmptyComposite() {
        getToolkit().createComposite(rootPane);
    }

    @Override
    protected boolean hasContentToDisplay() {
        return !propertyValues.isEmpty();
    }

    /**
     * Configures the user interface for each kind of property value.
     * <p>
     * For each property value type, the following configurations are made:
     * <ul>
     * <li>Creation of the label
     * <li>Creation of the edit composite
     * </ul>
     * 
     * @see IPropertyValue
     */
    private static enum PropertyValueUIConfiguration {

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

        public static PropertyValueUIConfiguration getValueByPropertyType(ProductCmptPropertyType propertyType) {
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

        public Control createLabel(IPropertyValue propertyValue, Composite parent, UIToolkit toolkit) {
            String localizedCaption = IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(propertyValue);
            return toolkit.createLabel(parent, localizedCaption);
        }

        public abstract EditPropertyValueComposite<?, ?> createEditComposite(IProductCmptProperty property,
                IPropertyValue propertyValue,
                ProductCmptPropertySection propertySection,
                Composite parent,
                BindingContext bindingContext,
                UIToolkit toolkit);

    }

}
