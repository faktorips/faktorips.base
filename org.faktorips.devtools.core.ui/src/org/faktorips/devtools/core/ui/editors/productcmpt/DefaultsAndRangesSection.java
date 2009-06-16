/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.enums.EnumType;
import org.faktorips.devtools.core.internal.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.ConfigElementType;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.PropertyValueComparator;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
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
 * Section to display and edit defaults and ranges of a product
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
	private List editControls = new ArrayList();

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
    public DefaultsAndRangesSection(
            IProductCmptGeneration generation,
            Composite parent,
            UIToolkit toolkit) {
        super(parent, Section.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);
        ArgumentCheck.notNull(generation);
        this.generation = generation;
        initControls();
        setText(Messages.PolicyAttributesSection_defaultsAndRanges);
    }

	/**
	 * {@inheritDoc}
	 */
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
    	GridLayout layout = new GridLayout(1, true);
    	layout.marginHeight = 2;
    	layout.marginWidth = 1;
    	client.setLayout(layout);
    	rootPane = toolkit.createStructuredLabelEditColumnComposite(client);
    	rootPane.setLayoutData(new GridData(GridData.FILL_BOTH));
    	GridLayout workAreaLayout = (GridLayout) rootPane.getLayout();
    	workAreaLayout.marginHeight = 5;
    	workAreaLayout.marginWidth = 5;
    	this.toolkit = toolkit;
    	  	
    	// following line forces the paint listener to draw a light grey border
    	// around
    	// the text control. Can only be understood by looking at the
    	// FormToolkit.PaintBorder class.
    	rootPane.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
    	toolkit.getFormToolkit().paintBordersFor(rootPane);

    	createEditControls();
    }
    
    /**
	 * Create the controls...
	 */
	private void createEditControls() {
		uiMasterController = new CompositeUIController();
		IConfigElement[] elements = generation.getConfigElements(ConfigElementType.POLICY_ATTRIBUTE);
		Arrays.sort(elements, new PropertyValueComparator(generation.getProductCmpt().getProductCmptType(), generation.getIpsProject()));
		if (elements.length == 0) {
			toolkit.createLabel(rootPane, Messages.PolicyAttributesSection_noDefaultsAndRangesDefined);
		}

        for (int i = 0; i < elements.length; i++) {
            createEditControlsForElement(elements[i]);
		}
		
        rootPane.layout(true);
		rootPane.redraw();
	}
    
    private void createEditControlsForElement(IConfigElement element) {
        try {
            ValueDatatype datatype = element.findValueDatatype(element.getIpsProject());
            if (datatype == null) {
                // no datatype found - use string as default
                datatype = Datatype.STRING;
            }
            IpsObjectUIController controller = new IpsObjectUIController(element);
            uiMasterController.add(controller);
            createEditControlForDefaultValue(element, datatype, controller);      
            IValueSet valueSet = element.getValueSet();
            if (valueSet.getValueSetType() == ValueSetType.ENUM) {
                createEditControlsForEnumeration(element, valueSet, controller);
            } else if (valueSet.getValueSetType() == ValueSetType.RANGE) {
                createEditControlsForRange(valueSet, controller);
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private void createEditControlForDefaultValue(IConfigElement element, ValueDatatype dataType, IpsObjectUIController controller) throws CoreException {
        Label label = toolkit.createFormLabel(rootPane, StringUtils.capitalize(element.getName()));
        // use the description of the attribute as tooltip 
        IPolicyCmptTypeAttribute attribute = element.findPcTypeAttribute(element.getIpsProject());
        if (attribute != null) {
            label.setToolTipText(attribute.getDescription());
        }
        
        toolkit.createFormLabel(rootPane, Messages.PolicyAttributeEditDialog_defaultValue);

        ValueDatatypeControlFactory ctrlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(dataType);
        EditField field = null;
        //TODO pk 15-06-2009: as long as faktor ips doesn't support attribute value constraints the value set of
        //enum types with separate enum contents cannot be restricted  
        if(dataType instanceof EnumTypeDatatypeAdapter && ((EnumTypeDatatypeAdapter)dataType).hasEnumContent()){
            EnumTypeDatatypeAdapter adapter = (EnumTypeDatatypeAdapter)dataType;
            field = ctrlFactory.createEditField(toolkit, rootPane, new EnumTypeDatatypeAdapter(adapter.getEnumType(), null), element.getValueSet(), generation.getIpsProject());
        } else {
            field = ctrlFactory.createEditField(toolkit, rootPane, dataType, element.getValueSet(), generation.getIpsProject());
        }
        addFocusControl(field.getControl());
        editControls.add(field.getControl());
        controller.add(field, element, IConfigElement.PROPERTY_VALUE);
    }

    private void createEditControlsForEnumeration(IConfigElement element, IValueSet valueSet, IpsObjectUIController controller) {
        // only if the value set defined in the model is not an all values value set
        // we can modify the content of the value set.
        toolkit.createFormLabel(rootPane, ""); //$NON-NLS-1$
        toolkit.createFormLabel(rootPane, Messages.PolicyAttributesSection_values);
        EnumValueSetControl evc = new EnumValueSetControl(rootPane, toolkit, element, this.getShell(), controller);
        evc.setDataChangeable(isDataChangeable());
        evc.setText(valueSet.toShortString());
        PreviewTextButtonField ptbf = new PreviewTextButtonField(evc);
        controller.add(ptbf, valueSet, IEnumValueSet.PROPERTY_VALUES);
        GridData data = (GridData)evc.getLayoutData();
        data.widthHint = UIToolkit.DEFAULT_WIDTH;
        addFocusControl(evc.getTextControl());
        this.editControls.add(evc);
    }

    private void createEditControlsForRange(IValueSet valueSet, IpsObjectUIController controller) {
        Text lower;
        Text upper;
        Text step;
        if (!IpsPlugin.getDefault().getIpsPreferences().isRangeEditFieldsInOneRow()){
            toolkit.createFormLabel(rootPane, ""); //$NON-NLS-1$
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
            toolkit.createFormLabel(rootPane, ""); //$NON-NLS-1$
            
            toolkit.createFormLabel(rootPane, Messages.DefaultsAndRangesSection_minMaxStepLabel);
            
            Composite rangeComposite = toolkit.createGridComposite(rootPane, 3, false, false);
            
            lower = toolkit.createText(rangeComposite);
            initTextField(lower, 50);
            
            upper = toolkit.createText(rangeComposite);
            initTextField(upper, 50);
            
            step = toolkit.createText(rangeComposite);
            initTextField(step, 50);
        }
        
        this.editControls.add(lower);
        this.editControls.add(upper);
        this.editControls.add(step);

        controller.add(upper, (IRangeValueSet) valueSet, IRangeValueSet.PROPERTY_UPPERBOUND);
        controller.add(lower, (IRangeValueSet) valueSet, IRangeValueSet.PROPERTY_LOWERBOUND);
        controller.add(step, (IRangeValueSet) valueSet, IRangeValueSet.PROPERTY_STEP);
    }
    
    private void initTextField(Text text, int widthHint){
        GridData gd = (GridData)text.getLayoutData();
        gd.widthHint = widthHint;
        text.setLayoutData(gd);  
        
        addFocusControl(text);
        this.editControls.add(text);
    }
    
	/**
	 * {@inheritDoc}
	 */
    protected void performRefresh() {
    	uiMasterController.updateUI();
    }
}
