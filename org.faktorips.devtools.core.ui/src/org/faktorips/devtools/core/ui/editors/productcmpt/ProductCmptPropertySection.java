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
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.faktorips.devtools.core.IpsPlugin;
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
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.CompositeUIController;
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
        layout.marginWidth = 1;
        layout.marginHeight = 2;
        client.setLayout(layout);

        rootPane = toolkit.createLabelEditColumnComposite(client);
        rootPane.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL | GridData.GRAB_HORIZONTAL));
        GridLayout workAreaLayout = (GridLayout)rootPane.getLayout();
        workAreaLayout.marginHeight = 5;
        workAreaLayout.marginWidth = 5;

        /*
         * Following line forces the paint listener to draw a light grey border around the control.
         * Can only be understood by looking at the FormToolkit$PaintBorder class.
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
        IProductCmptProperty property = null;
        try {
            property = propertyValue.findProperty(propertyValue.getIpsProject());
        } catch (CoreException e) {
            // Log exception, property remains null
            IpsPlugin.log(e);
        }

        Control label = null;
        PropertyValueUI propertyValueUI = PropertyValueUI.getValueByPropertyType(propertyValue.getPropertyType());
        if (propertyValueUI.isLabelRequired()) {
            label = createLabel(propertyValue);
            // Use description of property as tooltip if available
            if (property != null) {
                label.setToolTipText(IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(property));
            }
        }

        EditPropertyValueComposite<?, ?> editComposite = null;
        if (property != null) {
            editComposite = propertyValueUI.createEditComposite(property, propertyValue, this, rootPane,
                    uiMasterController, getToolkit());
        } else {
            createEmptyComposite();
        }

        if (label != null && editComposite != null) {
            /*
             * Vertically indent the label so it does not stick at the very top of the composite.
             * The magnitude of the indentation depends on the height and margin of the edit
             * composite.
             */
            ((GridData)label.getLayoutData()).verticalAlignment = SWT.TOP;
            int topOfControlToLabelPixels = editComposite.getFirstControlHeight()
                    - label.computeSize(SWT.DEFAULT, SWT.DEFAULT).y + editComposite.getFirstControlMargin();
            ((GridData)label.getLayoutData()).verticalIndent = ((GridLayout)editComposite.getLayout()).marginHeight
                    + topOfControlToLabelPixels;
        }
    }

    private Control createLabel(IPropertyValue propertyValue) {
        if (propertyValue instanceof ITableContentUsage) {
            return createHyperlink((ITableContentUsage)propertyValue);
        }
        String localizedCaption = IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(propertyValue);
        return getToolkit().createLabel(rootPane, localizedCaption);
    }

    private Control createHyperlink(final ITableContentUsage tableContentUsage) {
        String localizedCaption = IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(tableContentUsage);
        Hyperlink hyperlink = getToolkit().createHyperlink(rootPane, localizedCaption);
        hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            public void linkActivated(HyperlinkEvent event) {
                try {
                    ITableContents tableContents = tableContentUsage.findTableContents(generation.getIpsProject());
                    if (tableContents != null) {
                        IpsUIPlugin.getDefault().openEditor(tableContents.getIpsSrcFile());
                    }
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        });
        return hyperlink;
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

    private static enum PropertyValueUI {

        ATTRIBUTE_VALUE(true) {
            @Override
            public EditPropertyValueComposite<?, ?> createEditComposite(IProductCmptProperty property,
                    IPropertyValue propertyValue,
                    ProductCmptPropertySection propertySection,
                    Composite parent,
                    CompositeUIController uiMasterController,
                    UIToolkit toolkit) {

                return new AttributeValueEditComposite((IProductCmptTypeAttribute)property,
                        (IAttributeValue)propertyValue, propertySection, parent, uiMasterController, toolkit);
            }
        },

        TABLE_CONTENT_USAGE(true) {
            @Override
            public EditPropertyValueComposite<?, ?> createEditComposite(IProductCmptProperty property,
                    IPropertyValue propertyValue,
                    ProductCmptPropertySection propertySection,
                    Composite parent,
                    CompositeUIController uiMasterController,
                    UIToolkit toolkit) {

                return new TableContentUsageEditComposite((ITableStructureUsage)property,
                        (ITableContentUsage)propertyValue, propertySection, parent, uiMasterController, toolkit);
            }
        },

        VALIDATION_RULE_CONFIG(false) {
            @Override
            public EditPropertyValueComposite<?, ?> createEditComposite(IProductCmptProperty property,
                    IPropertyValue propertyValue,
                    ProductCmptPropertySection propertySection,
                    Composite parent,
                    CompositeUIController uiMasterController,
                    UIToolkit toolkit) {

                return new ValidationRuleConfigEditComposite((IValidationRule)property,
                        (IValidationRuleConfig)propertyValue, propertySection, parent, uiMasterController, toolkit);
            }
        },

        FORMULA(true) {
            @Override
            public EditPropertyValueComposite<?, ?> createEditComposite(IProductCmptProperty property,
                    IPropertyValue propertyValue,
                    ProductCmptPropertySection propertySection,
                    Composite parent,
                    CompositeUIController uiMasterController,
                    UIToolkit toolkit) {

                return new FormulaEditComposite((IProductCmptTypeMethod)property, (IFormula)propertyValue,
                        propertySection, parent, uiMasterController, toolkit);
            }
        },

        CONFIG_ELEMENT(true) {
            @Override
            public EditPropertyValueComposite<?, ?> createEditComposite(IProductCmptProperty property,
                    IPropertyValue propertyValue,
                    ProductCmptPropertySection propertySection,
                    Composite parent,
                    CompositeUIController uiMasterController,
                    UIToolkit toolkit) {

                return new ConfigElementEditComposite((IPolicyCmptTypeAttribute)property,
                        (IConfigElement)propertyValue, propertySection, parent, uiMasterController, toolkit);
            }
        };

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

        private final boolean labelRequired;

        private PropertyValueUI(boolean labelRequired) {
            this.labelRequired = labelRequired;
        }

        public boolean isLabelRequired() {
            return labelRequired;
        }

        public abstract EditPropertyValueComposite<?, ?> createEditComposite(IProductCmptProperty property,
                IPropertyValue propertyValue,
                ProductCmptPropertySection propertySection,
                Composite parent,
                CompositeUIController uiMasterController,
                UIToolkit toolkit);

    }

}
