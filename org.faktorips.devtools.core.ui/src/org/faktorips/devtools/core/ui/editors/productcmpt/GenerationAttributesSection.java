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

import java.text.DateFormat;
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
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.PropertyValueComparator;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.CompositeUIController;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * Section to display and edit the time-related attributes (attributes bound to a generation)
 * 
 * @author Thorsten Guenther
 */
public class GenerationAttributesSection extends IpsSection {

    /**
     * Generation which holds the informations to display
     */
    private IProductCmptGeneration generation;

    /**
     * Toolkit to handle common ui-operations
     */
    private UIToolkit toolkit;

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
    private CompositeUIController uiMasterController = null;

    /**
     * Creates a new attributes section.
     * 
     * @param generation The generation to get all informations to display from.
     * @param parent The parent to link the ui-items to.
     * @param toolkit The toolkit to use for easier ui-handling
     */
    public GenerationAttributesSection(IProductCmptGeneration generation, Composite parent, UIToolkit toolkit) {
        super(parent, ExpandableComposite.TITLE_BAR, GridData.FILL_BOTH, toolkit);
        this.generation = generation;
        initControls();
        setText(Messages.ProductAttributesSection_attribute);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        this.toolkit = toolkit;

        GridLayout layout = new GridLayout(1, true);
        layout.marginHeight = 2;
        layout.marginWidth = 1;
        client.setLayout(layout);

        rootPane = toolkit.createLabelEditColumnComposite(client);
        rootPane.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout workAreaLayout = (GridLayout)rootPane.getLayout();
        workAreaLayout.marginHeight = 5;
        workAreaLayout.marginWidth = 5;

        // following line forces the paint listener to draw a light grey border around
        // the text control. Can only be understood by looking at the
        // FormToolkit.PaintBorder class.
        rootPane.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
        toolkit.getFormToolkit().paintBordersFor(rootPane);

        // create controls for config elements
        createEditControls();
        uiMasterController.updateUI();
    }

    protected void updateGenerationText() {
        DateFormat format = IpsPlugin.getDefault().getIpsPreferences().getDateFormat();
        String validRange = format.format(generation.getValidFrom().getTime());

        GregorianCalendar date = generation.getValidTo();
        String validToString;
        if (date == null) {
            validToString = Messages.ProductAttributesSection_valueGenerationValidToUnlimited;
        } else {
            validToString = IpsPlugin.getDefault().getIpsPreferences().getDateFormat().format(date.getTime());
        }

        validRange += " - " + validToString; //$NON-NLS-1$
        String generationConceptName = IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention()
                .getGenerationConceptNameSingular();
        setText(generationConceptName + ": " + validRange); //$NON-NLS-1$
    }

    @Override
    protected void performRefresh() {
        if (uiMasterController != null) {
            uiMasterController.updateUI();
        }
    }

    private void createEditControls() {
        uiMasterController = new CompositeUIController();

        // create a label and edit control for each attribute value
        IAttributeValue[] elements = generation.getAttributeValues();
        Arrays.sort(
                elements,
                new PropertyValueComparator(generation.getProductCmpt().getProductCmptType(), generation
                        .getIpsProject()));
        for (IAttributeValue element : elements) {
            addAndRegister(element);
        }

        rootPane.layout(true);
        rootPane.redraw();
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (isEnabled() == enabled) {
            return;
        }

        if (isDisposed()) {
            return;
        }

        super.setEnabled(enabled);

        // to get the disabled look, we have to disable all the input-fields manually :-(
        for (Control element : editControls) {
            element.setEnabled(enabled);

        }

        rootPane.layout(true);
        rootPane.redraw();
    }

    /**
     * Creates a new label and input for the given config element and links the input with the
     * config element.
     */
    private void addAndRegister(IAttributeValue toDisplay) {
        if (toDisplay == null) {
            // this can happen if the config element has no corresponding attribute...
            return;
        }
        IIpsProject ipsProject = toDisplay.getIpsProject();
        Label label = toolkit.createLabel(rootPane, StringUtils.capitalize(toDisplay.getAttribute()));

        IpsObjectUIController controller = new IpsObjectUIController(toDisplay);
        uiMasterController.add(controller);

        try {
            IProductCmptTypeAttribute attr = toDisplay.findAttribute(ipsProject);
            ValueDatatype datatype = null;
            IValueSet valueset = null;
            // use description of attribute as tooltip
            if (attr != null) {
                label.setToolTipText(attr.getDescription());
                datatype = attr.findDatatype(ipsProject);
                valueset = attr.getValueSet();
            }
            ValueDatatypeControlFactory ctrlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(datatype);
            EditField field = ctrlFactory.createEditField(toolkit, rootPane, datatype, valueset,
                    generation.getIpsProject());
            Control ctrl = field.getControl();
            controller.add(field, toDisplay, IConfigElement.PROPERTY_VALUE);
            addFocusControl(ctrl);
            editControls.add(ctrl);

        } catch (CoreException e) {
            Text text = toolkit.createText(rootPane);
            addFocusControl(text);
            editControls.add(text);
            controller.add(text, toDisplay, IConfigElement.PROPERTY_VALUE);
        }
    }

}
