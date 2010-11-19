/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.TimedEnumDatatypeUtil;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
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
        super(parent, ExpandableComposite.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);
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
        Label label = toolkit.createFormLabel(rootPane, StringUtils.capitalize(element.getName()));
        // use the description of the attribute as tooltip
        if (attribute != null) {
            label.setToolTipText(attribute.getDescription());
        }
    }

    private void createEditControlForDefaultValue(IConfigElement element,
            ValueDatatype datatype,
            IpsObjectUIController controller) {

        toolkit.createFormLabel(rootPane, Messages.PolicyAttributeEditDialog_defaultValue);
        EditField field = createEditField(element, datatype);
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
        valueSetCtrl.setText(element.getValueSet().toShortString());
        PreviewTextButtonField ptbf = new PreviewTextButtonField(valueSetCtrl);
        controller.add(ptbf, element, IConfigElement.PROPERTY_VALUE_SET);
        GridData data = (GridData)valueSetCtrl.getLayoutData();
        data.widthHint = UIToolkit.DEFAULT_WIDTH;
        addFocusControl(valueSetCtrl.getTextControl());
        editControls.add(valueSetCtrl);
    }

    private void createEditControlsForRange(IRangeValueSet range, IpsObjectUIController controller) {
        Text lower;
        Text upper;
        Text step;
        if (!IpsPlugin.getDefault().getIpsPreferences().isRangeEditFieldsInOneRow()) {
            toolkit.createFormLabel(rootPane, Messages.PolicyAttributesSection_minimum);
            lower = toolkit.createText(rootPane);
            addFocusControl(lower);

            toolkit.createFormLabel(rootPane, ""); //$NON-NLS-1$
            toolkit.createFormLabel(rootPane, Messages.PolicyAttributesSection_maximum);
            upper = toolkit.createText(rootPane);
            addFocusControl(upper);

            toolkit.createFormLabel(rootPane, ""); //$NON-NLS-1$
            toolkit.createFormLabel(rootPane, Messages.PolicyAttributesSection_step);
            step = toolkit.createText(rootPane);
            addFocusControl(step);
        } else {
            toolkit.createFormLabel(rootPane, Messages.DefaultsAndRangesSection_minMaxStepLabel);
            Composite rangeComposite = toolkit.createGridComposite(rootPane, 3, false, false);

            lower = toolkit.createText(rangeComposite);
            initTextField(lower, 50);

            upper = toolkit.createText(rangeComposite);
            initTextField(upper, 50);

            step = toolkit.createText(rangeComposite);
            initTextField(step, 50);
        }

        editControls.add(lower);
        editControls.add(upper);
        editControls.add(step);

        controller.add(upper, range, IRangeValueSet.PROPERTY_UPPERBOUND);
        controller.add(lower, range, IRangeValueSet.PROPERTY_LOWERBOUND);
        controller.add(step, range, IRangeValueSet.PROPERTY_STEP);
    }

    private void initTextField(Text text, int widthHint) {
        GridData gd = (GridData)text.getLayoutData();
        gd.widthHint = widthHint;
        text.setLayoutData(gd);

        addFocusControl(text);
        editControls.add(text);
    }

    @Override
    protected void performRefresh() {
        uiMasterController.updateUI();
    }

    /**
     * Creates the edit field for values of the config elemnt (default value oder values in the
     * value set).
     */
    private EditField createEditField(IConfigElement configElement, ValueDatatype datatype) {
        GregorianCalendar genFrom = configElement.getProductCmptGeneration().getValidFrom();
        GregorianCalendar genTo = configElement.getProductCmptGeneration().getValidTo();
        IValueSet sourceSet = ValueSetFilter.filterValueSet(configElement.getValueSet(), datatype, genFrom, genTo,
                TimedEnumDatatypeUtil.ValidityCheck.SOME_TIME_OF_THE_PERIOD);
        ValueDatatypeControlFactory ctrlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(datatype);
        return ctrlFactory.createEditField(toolkit, rootPane, datatype, sourceSet, generation.getIpsProject());
    }
}
