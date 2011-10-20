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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.faktorips.datatype.ValueDatatype;
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
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.OverlayIcons;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.CompositeUIController;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.FormulaEditControl;
import org.faktorips.devtools.core.ui.controls.TableContentsUsageRefControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * Provides a generic section for all kinds of {@link IPropertyValue}s.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 */
public class ProductCmptPropertySection extends IpsSection {

    private final List<Control> editControls = new ArrayList<Control>();

    private final IProductCmptGeneration generation;

    private final List<IPropertyValue> propertyValues;

    /**
     * Pane which serves as parent for all controls created inside this section.
     */
    private Composite rootPane;

    /**
     * Controller to handle update of UI and model automatically.
     */
    private CompositeUIController uiMasterController;

    public ProductCmptPropertySection(IProductCmptCategory category, IProductCmptGeneration generation,
            Composite parent, UIToolkit toolkit, List<IPropertyValue> propertyValues) {

        super(parent, ExpandableComposite.TITLE_BAR, GridData.FILL_BOTH, toolkit);

        this.generation = generation;
        this.propertyValues = propertyValues;

        initControls();
        setText(category.getName());
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
        uiMasterController = new CompositeUIController();

        // Create a label and an edit control for each property
        for (IPropertyValue propertyValue : propertyValues) {
            createEditControl(propertyValue);
        }

        rootPane.layout(true);
        rootPane.redraw();
    }

    private void createEditControl(IPropertyValue propertyValue) {
        Control label = createLabel(propertyValue);

        // TODO AW
        if (propertyValue instanceof IConfigElement || propertyValue instanceof IValidationRuleConfig) {
            return;
        }

        IpsObjectUIController controller = new IpsObjectUIController(propertyValue);
        uiMasterController.add(controller);

        try {
            IProductCmptProperty property = propertyValue.findProperty(propertyValue.getIpsProject());
            // Use description of property as tooltip
            if (property != null) {
                label.setToolTipText(IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(property));
            }
            EditField<?> editField = createEditField(property, propertyValue);
            controller.add(editField, propertyValue, getEditedPropertyName(propertyValue));
            addFocusControl(editField.getControl());
            editControls.add(editField.getControl());

            if (propertyValue instanceof IAttributeValue) {
                addChangeOverTimeControlDecoration(editField);
            }

        } catch (CoreException e) {
            Text text = getToolkit().createText(rootPane);
            addFocusControl(text);
            editControls.add(text);
            controller.add(text, propertyValue, getEditedPropertyName(propertyValue));
        }
    }

    private Control createLabel(IPropertyValue propertyValue) {
        String localizedCaption = IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(propertyValue);
        if (propertyValue instanceof ITableContentUsage) {
            return createLabel((ITableContentUsage)propertyValue, localizedCaption);
        }
        return getToolkit().createLabel(rootPane, localizedCaption);
    }

    private Control createLabel(final ITableContentUsage tcu, String localizedCaption) {
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

    private String getEditedPropertyName(IPropertyValue propertyValue) {
        if (propertyValue instanceof IAttributeValue) {
            return IAttributeValue.PROPERTY_VALUE;
        }
        if (propertyValue instanceof IConfigElement) {
            return IConfigElement.PROPERTY_VALUE;
        }
        if (propertyValue instanceof IFormula) {
            return IFormula.PROPERTY_EXPRESSION;
        }
        if (propertyValue instanceof ITableContentUsage) {
            return ITableContentUsage.PROPERTY_TABLE_CONTENT;
        }
        if (propertyValue instanceof IValidationRuleConfig) {
            return IValidationRuleConfig.PROPERTY_ACTIVE;
        }
        throw new RuntimeException();
    }

    private void addChangeOverTimeControlDecoration(EditField<?> editField) {
        ControlDecoration controlDecoration = new ControlDecoration(editField.getControl(), SWT.LEFT | SWT.TOP);
        controlDecoration.setDescriptionText(NLS.bind(
                Messages.AttributeValuesSection_attributeNotChangingOverTimeDescription, IpsPlugin.getDefault()
                        .getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNamePlural()));
        controlDecoration.setImage(IpsUIPlugin.getImageHandling().getImage(OverlayIcons.NOT_CHANGEOVERTIME_OVR_DESC));
        controlDecoration.setMarginWidth(1);
    }

    private EditField<?> createEditField(IProductCmptProperty property, IPropertyValue propertyValue)
            throws CoreException {

        EditField<?> editField = null;
        switch (property.getProductCmptPropertyType()) {
            case PRODUCT_CMPT_TYPE_ATTRIBUTE:
                editField = createEditField((IProductCmptTypeAttribute)property);
                break;
            case TABLE_STRUCTURE_USAGE:
                editField = createEditField((ITableStructureUsage)property);
                break;
            case FORMULA_SIGNATURE_DEFINITION:
                editField = createEditField((IFormula)propertyValue);
                break;
            case POLICY_CMPT_TYPE_ATTRIBUTE:
                editField = createEditField((IPolicyCmptTypeAttribute)property);
                break;
            case VALIDATION_RULE:
                editField = createEditField((IValidationRule)property);
                break;
        }
        return editField;
    }

    private EditField<?> createEditField(IProductCmptTypeAttribute productCmptTypeAttribute) throws CoreException {
        ValueDatatype datatype = productCmptTypeAttribute.findDatatype(productCmptTypeAttribute.getIpsProject());
        ValueDatatypeControlFactory controlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(datatype);
        return controlFactory.createEditField(getToolkit(), rootPane, datatype, productCmptTypeAttribute.getValueSet(),
                productCmptTypeAttribute.getIpsProject());
    }

    private EditField<?> createEditField(IFormula formula) throws CoreException {
        FormulaEditControl formulaEditControl = new FormulaEditControl(rootPane, getToolkit(), formula, getShell(),
                this);
        FormulaCompletionProcessor completionProcessor = new FormulaCompletionProcessor(formula);
        ContentAssistHandler.createHandlerForText(formulaEditControl.getTextControl(),
                CompletionUtil.createContentAssistant(completionProcessor));
        return new TextButtonField(formulaEditControl);
    }

    // TODO AW
    private EditField<?> createEditField(IValidationRule validationRule) {
        return null;
    }

    // TODO AW
    private EditField<?> createEditField(IPolicyCmptTypeAttribute policyCmptTypeAttribute) {
        return null;
    }

    private EditField<?> createEditField(ITableStructureUsage tableStructureUsage) {
        TableContentsUsageRefControl tcuControl = new TableContentsUsageRefControl(tableStructureUsage.getIpsProject(),
                rootPane, getToolkit(), tableStructureUsage);
        return new TextButtonField(tcuControl);
    }

    @Override
    protected void performRefresh() {
        if (uiMasterController != null) {
            uiMasterController.updateUI();
        }
    }

}
