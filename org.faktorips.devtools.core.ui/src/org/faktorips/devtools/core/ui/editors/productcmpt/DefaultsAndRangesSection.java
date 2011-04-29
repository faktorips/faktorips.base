/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.TimedEnumDatatypeUtil;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.PropertyValueComparator;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetFilter;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.CompositeUIController;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.PreviewTextButtonField;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.ArgumentCheck;

/**
 * Section to display and edit defaults and value sets of a product component generation.
 * 
 * @author Thorsten Guenther
 */
public class DefaultsAndRangesSection extends IpsSection {

    private static final String ID = "org.faktorips.devtools.core.ui.editors.productcmpt.DefaultsAndRangesSection"; //$NON-NLS-1$

    /**
     * Generation which holds the informations to display
     */
    private IProductCmptGeneration generation;

    /**
     * Pane which serves as parent for all controlls created inside this section.
     */
    private Composite rootPane;

    /**
     * List of controls displaying data (needed to enable/disable).
     */
    private List<Control> editControls = new ArrayList<Control>();

    /**
     * Controller to handle update of ui and model automatically.
     */
    private CompositeUIController uiMasterController;

    /**
     * Toolkit to handle common ui-operations
     */
    private UIToolkit toolkit;

    /**
     * Creates a new section to edit ranges and default-values.
     */
    public DefaultsAndRangesSection(IProductCmptGeneration generation, Composite parent, UIToolkit toolkit) {
        super(ID, parent, GridData.FILL_HORIZONTAL, toolkit);
        ArgumentCheck.notNull(generation);
        this.generation = generation;
        initControls();
        setText(Messages.PolicyAttributesSection_defaultsAndRanges);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        GridLayout layout = new GridLayout(1, true);
        layout.marginHeight = 2;
        layout.marginWidth = 1;
        client.setLayout(layout);
        rootPane = toolkit.createStructuredLabelEditColumnComposite(client);
        rootPane.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout workAreaLayout = (GridLayout)rootPane.getLayout();
        workAreaLayout.marginHeight = 5;
        workAreaLayout.marginWidth = 5;
        this.toolkit = toolkit;

        // following line forces the paint listener to draw a light grey border
        // around the text control. Can only be understood by looking at the
        // FormToolkit.PaintBorder class.
        rootPane.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
        toolkit.getFormToolkit().paintBordersFor(rootPane);

        createAllEditControls();
    }

    /**
     * Create the edit controls for all config elements.
     */
    private void createAllEditControls() {
        uiMasterController = new CompositeUIController();
        IConfigElement[] elements = getSortedConfigElements();
        if (elements.length == 0) {
            toolkit.createLabel(rootPane, Messages.PolicyAttributesSection_noDefaultsAndRangesDefined);
        } else {
            for (IConfigElement element : elements) {
                createEditControlsForConfigElement(element);
            }
        }

        rootPane.layout(true);
        rootPane.redraw();
        uiMasterController.updateUI();
    }

    private IConfigElement[] getSortedConfigElements() {
        IConfigElement[] elements = generation.getConfigElements();
        Arrays.sort(
                elements,
                new PropertyValueComparator(generation.getProductCmpt().getProductCmptType(), generation
                        .getIpsProject()));
        return elements;
    }

    /**
     * Creates the labels and controls to edit the default value and the set of allowed values for
     * the given config element.
     */
    private void createEditControlsForConfigElement(IConfigElement element) {
        try {
            IPolicyCmptTypeAttribute attribute = element.findPcTypeAttribute(element.getIpsProject());
            ValueDatatype datatype = (attribute == null) ? null : attribute.findDatatype(element.getIpsProject());
            if (datatype == null) {
                // no datatype found - use string as default
                datatype = Datatype.STRING;
            }
            IpsObjectUIController controller = new IpsObjectUIController(element);
            uiMasterController.add(controller);
            createConfigElementNameLabel(element, attribute);
            boolean controlCreated = createEditControlForValueSet(element, controller);
            if (controlCreated) {
                // "indent the next line"
                toolkit.createFormLabel(rootPane, ""); //$NON-NLS-1$
            }
            createEditControlForDefaultValue(element, datatype, controller);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private void createConfigElementNameLabel(IConfigElement element, IPolicyCmptTypeAttribute attribute) {
        Label label = toolkit.createFormLabel(rootPane, IpsPlugin.getMultiLanguageSupport()
                .getLocalizedCaption(element));
        // use the description of the attribute as tooltip
        if (attribute != null) {
            String localizedDescription = IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(attribute);
            label.setToolTipText(localizedDescription);
        }
    }

    private void createEditControlForDefaultValue(IConfigElement element,
            ValueDatatype datatype,
            IpsObjectUIController controller) {

        toolkit.createFormLabel(rootPane, Messages.PolicyAttributeEditDialog_defaultValue);
        EditField<String> field = createEditField(element, datatype);
        addFocusControl(field.getControl());
        editControls.add(field.getControl());
        controller.add(field, element, IConfigElement.PROPERTY_VALUE);
    }

    /**
     * Creates the edit control(s) for the config element's value set.
     * 
     * @return <code>true</code> if the control(s) have bean created, otherwise <code>false</code>.
     */
    private boolean createEditControlForValueSet(IConfigElement element, IpsObjectUIController controller) {
        IValueSet valueSet = element.getValueSet();
        if (valueSet == null) {
            return false;
        }
        if (valueSet.isRange()) {
            createEditControlsForRange((IRangeValueSet)valueSet, controller);
        } else {
            createEditControlsForOtherThanRange(element, controller);
        }
        return true;
    }

    private void createEditControlsForOtherThanRange(IConfigElement element, IpsObjectUIController controller) {
        toolkit.createFormLabel(rootPane, Messages.PolicyAttributesSection_valueSet);
        AnyValueSetControl valueSetCtrl = new AnyValueSetControl(rootPane, toolkit, element, getShell(), controller);
        valueSetCtrl.setDataChangeable(isDataChangeable());
        valueSetCtrl.setText(IpsUIPlugin.getDefault().getDatatypeFormatter().formatValueSet(element.getValueSet()));
        PreviewTextButtonField ptbf = new PreviewTextButtonField(valueSetCtrl);
        controller.add(ptbf, element, IConfigElement.PROPERTY_VALUE_SET);
        GridData data = (GridData)valueSetCtrl.getLayoutData();
        data.widthHint = UIToolkit.DEFAULT_WIDTH;
        addFocusControl(valueSetCtrl.getTextControl());
        editControls.add(valueSetCtrl);
    }

    private void createEditControlsForRange(IRangeValueSet range, IpsObjectUIController controller) {
        EditField<String> lowerField;
        EditField<String> upperField;
        EditField<String> stepField;

        ValueDatatype datatype = ((RangeValueSet)range).getValueDatatype();
        ValueDatatypeControlFactory controlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(datatype);
        if (!IpsPlugin.getDefault().getIpsPreferences().isRangeEditFieldsInOneRow()) {
            toolkit.createFormLabel(rootPane, Messages.PolicyAttributesSection_minimum);
            lowerField = controlFactory.createEditField(toolkit, rootPane, datatype, range, range.getIpsProject());
            addFocusControl(lowerField.getControl());

            toolkit.createFormLabel(rootPane, ""); //$NON-NLS-1$
            toolkit.createFormLabel(rootPane, Messages.PolicyAttributesSection_maximum);
            upperField = controlFactory.createEditField(toolkit, rootPane, datatype, range, range.getIpsProject());
            addFocusControl(upperField.getControl());

            toolkit.createFormLabel(rootPane, ""); //$NON-NLS-1$
            toolkit.createFormLabel(rootPane, Messages.PolicyAttributesSection_step);
            stepField = controlFactory.createEditField(toolkit, rootPane, datatype, range, range.getIpsProject());
            addFocusControl(stepField.getControl());
        } else {
            toolkit.createFormLabel(rootPane, Messages.DefaultsAndRangesSection_minMaxStepLabel);
            Composite rangeComposite = toolkit.createGridComposite(rootPane, 3, false, false);
            // need to see borders
            ((GridLayout)rangeComposite.getLayout()).marginWidth = 1;
            ((GridLayout)rangeComposite.getLayout()).marginHeight = 2;

            lowerField = controlFactory
                    .createEditField(toolkit, rangeComposite, datatype, range, range.getIpsProject());
            initTextField(lowerField.getControl(), 50);

            upperField = controlFactory
                    .createEditField(toolkit, rangeComposite, datatype, range, range.getIpsProject());
            initTextField(upperField.getControl(), 50);

            stepField = controlFactory.createEditField(toolkit, rangeComposite, datatype, range, range.getIpsProject());
            initTextField(stepField.getControl(), 50);

            toolkit.getFormToolkit().paintBordersFor(rangeComposite);
        }

        editControls.add(lowerField.getControl());
        editControls.add(upperField.getControl());
        editControls.add(stepField.getControl());

        controller.add(upperField, range, IRangeValueSet.PROPERTY_UPPERBOUND);
        controller.add(lowerField, range, IRangeValueSet.PROPERTY_LOWERBOUND);
        controller.add(stepField, range, IRangeValueSet.PROPERTY_STEP);
    }

    private void initTextField(Control control, int widthHint) {
        if (control.getLayoutData() instanceof GridData) {
            GridData gd = (GridData)control.getLayoutData();
            gd.widthHint = widthHint;
            control.setLayoutData(gd);
        }

        addFocusControl(control);
        editControls.add(control);
    }

    @Override
    protected void performRefresh() {
        uiMasterController.updateUI();
    }

    /**
     * Creates the edit field for values of the config elemnt (default value oder values in the
     * value set).
     */
    private EditField<String> createEditField(IConfigElement configElement, ValueDatatype datatype) {
        GregorianCalendar genFrom = configElement.getProductCmptGeneration().getValidFrom();
        GregorianCalendar genTo = configElement.getProductCmptGeneration().getValidTo();
        IValueSet sourceSet = ValueSetFilter.filterValueSet(configElement.getValueSet(), datatype, genFrom, genTo,
                TimedEnumDatatypeUtil.ValidityCheck.SOME_TIME_OF_THE_PERIOD);
        ValueDatatypeControlFactory ctrlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(datatype);
        return ctrlFactory.createEditField(toolkit, rootPane, datatype, sourceSet, generation.getIpsProject());
    }
}
