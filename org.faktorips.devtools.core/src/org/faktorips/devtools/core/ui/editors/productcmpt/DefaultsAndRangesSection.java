/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IEnumValueSet;
import org.faktorips.devtools.core.model.IRangeValueSet;
import org.faktorips.devtools.core.model.IValueSet;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.CompositeUIController;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;
import org.faktorips.devtools.core.ui.controller.fields.EnumDatatypeField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueSetField;
import org.faktorips.devtools.core.ui.controller.fields.ValueTextField;
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
    	
    	if (elements.length == 0) {
    		toolkit.createLabel(rootPane, Messages.PolicyAttributesSection_noDefaultsAndRangesDefined);
    	}
    	
    	for (int i = 0; i < elements.length; i++) {
    		IAttribute attribute = null; 
    		Datatype dataType = null;
    		try {
				attribute = elements[i].findPcTypeAttribute();
				if (attribute != null) {
					dataType = attribute.findDatatype();
				}
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
			
    		IValueSet valueSet = elements[i].getValueSet();
    		
    		// if the config element has an all values valueset, the valueset
    		// is changed to a copy of the underlying attribute valueset. This is
    		// because all value valuesets onyl apply on datatypes, not on other
    		// valuesets.
    		if (attribute != null && valueSet.getValueSetType() == ValueSetType.ALL_VALUES) {
    			elements[i].setValueSetCopy(attribute.getValueSet());
    			valueSet = elements[i].getValueSet();
    		}

    		toolkit.createFormLabel(rootPane, StringUtils.capitalise(elements[i].getName()));
    		toolkit.createFormLabel(rootPane, Messages.PolicyAttributeEditDialog_defaultValue);

    		if ((valueSet.getValueSetType() == ValueSetType.ALL_VALUES && dataType instanceof EnumDatatype) || valueSet.getValueSetType() == ValueSetType.ENUM) {
    			
    			Combo combo = toolkit.createCombo(rootPane);
    			EditField defaultField;
    			if (valueSet.getValueSetType() == ValueSetType.ENUM) {
    				defaultField = new EnumValueSetField(combo, (IEnumValueSet)valueSet, (EnumDatatype)dataType);
    			}
    			else {
    				defaultField = new EnumDatatypeField(combo, (EnumDatatype)dataType);
    			}
    			
    			this.editControls.add(combo);
        		
    			IpsPartUIController controller = new IpsPartUIController(elements[i]);
        		uiMasterController.add(controller);

        		controller.add(defaultField, elements[i], IConfigElement.PROPERTY_VALUE);
        		
        		if (valueSet.getValueSetType() != ValueSetType.ALL_VALUES) {
        			// only if the value set defined in the model is not an all values value set
        			// we can modify the content of the value set.
	    			toolkit.createFormLabel(rootPane, ""); //$NON-NLS-1$
	    			toolkit.createFormLabel(rootPane, Messages.PolicyAttributesSection_values);
	    			EnumValueSetControl evc = new EnumValueSetControl(rootPane, toolkit, elements[i], this.getShell(), controller);
	    			evc.setText(valueSet.toShortString());
        		}
    		}
    		else if (valueSet.getValueSetType() == ValueSetType.RANGE || valueSet.getValueSetType() == ValueSetType.ALL_VALUES) {
        		IpsPartUIController controller = new IpsPartUIController(elements[i]);

    			Text text = toolkit.createText(rootPane);    			
    			this.editControls.add(text);
        		ValueTextField field = new ValueTextField(text);
        		controller.add(field, elements[i], IConfigElement.PROPERTY_VALUE);
        		uiMasterController.add(controller);

        		if (valueSet.getValueSetType() != ValueSetType.ALL_VALUES && !attribute.getDatatype().equals(Datatype.STRING.getName())) {
        			// only if the value set defined in the model is not an all values value set
        			// and the datatype is not a string we can modify the ranges of the value set.
        			toolkit.createFormLabel(rootPane, ""); //$NON-NLS-1$
        			toolkit.createFormLabel(rootPane, Messages.PolicyAttributesSection_minimum);
        			Text lower = toolkit.createText(rootPane);
        			this.editControls.add(lower);
        			
        			toolkit.createFormLabel(rootPane, ""); //$NON-NLS-1$
        			toolkit.createFormLabel(rootPane, Messages.PolicyAttributesSection_maximum);
        			Text upper = toolkit.createText(rootPane);
        			this.editControls.add(upper);
        			
        			toolkit.createFormLabel(rootPane, ""); //$NON-NLS-1$
        			toolkit.createFormLabel(rootPane, Messages.PolicyAttributesSection_step);
        			Text step = toolkit.createText(rootPane);
        			this.editControls.add(step);
        			
        			controller.add(upper, (IRangeValueSet) valueSet, IRangeValueSet.PROPERTY_UPPERBOUND);
        			controller.add(lower, (IRangeValueSet) valueSet, IRangeValueSet.PROPERTY_LOWERBOUND);
        			controller.add(step, (IRangeValueSet) valueSet, IRangeValueSet.PROPERTY_STEP);
        		}
        		
    		}
    		toolkit.createVerticalSpacer(rootPane, 3).setBackground(rootPane.getBackground());
    		toolkit.createVerticalSpacer(rootPane, 3).setBackground(rootPane.getBackground());
    		toolkit.createVerticalSpacer(rootPane, 3).setBackground(rootPane.getBackground());
    	}
    	    	
		rootPane.layout(true);
		rootPane.redraw();
    }
    
	/**
	 * {@inheritDoc}
	 */
    protected void performRefresh() {
    	uiMasterController.updateUI();
    }

	/**
	 * {@inheritDoc}
	 */
	public void setEnabled(boolean enabled) {
		// to get the disabled look, we have to disable all the input-fields manually :-(
		for (Iterator iter = editControls.iterator(); iter.hasNext();) {
			Control element = (Control) iter.next();
			element.setEnabled(enabled);
			
		}
		rootPane.layout(true);
		rootPane.redraw();
	}
}
