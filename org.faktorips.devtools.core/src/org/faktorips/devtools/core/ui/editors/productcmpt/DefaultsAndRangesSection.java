package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.Range;
import org.faktorips.devtools.core.model.ValueSet;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.CompositeUIController;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.EnumValuesControl;
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
    	IpsObjectUIController ctrl = new IpsObjectUIController(generation.getIpsObject());
    	uiMasterController.add(ctrl);
    	
    	IConfigElement[] elements = generation.getConfigElements(ConfigElementType.POLICY_ATTRIBUTE);
    	
    	if (elements.length == 0) {
    		toolkit.createLabel(rootPane, Messages.PolicyAttributesSection_noDefaultsAndRangesDefined);
    	}
    	
    	for (int i = 0; i < elements.length; i++) {
    		IAttribute attribute = null; 
    		try {
				attribute = elements[i].findPcTypeAttribute();
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
    		ValueSet valueSet = elements[i].getValueSet();
    		if (attribute != null) {
    			if (valueSet.isAllValues()) {
    				valueSet = attribute.getValueSet().copy();
    			}
    		}
    		toolkit.createFormLabel(rootPane, StringUtils.capitalise(elements[i].getName()));
    		toolkit.createFormLabel(rootPane, Messages.PolicyAttributeEditDialog_defaultValue);
    		Text text = toolkit.createText(rootPane);
			this.editControls.add(text);
    		TextField field = new TextField(text);
    		IpsPartUIController controller = new IpsPartUIController(elements[i]);
    		controller.add(field, elements[i], IConfigElement.PROPERTY_VALUE);
    		uiMasterController.add(controller);
    		
    		if (valueSet.isEnum()) {
    			toolkit.createFormLabel(rootPane, ""); //$NON-NLS-1$
    			toolkit.createFormLabel(rootPane, Messages.PolicyAttributesSection_values);
    			EnumValuesControl evc = new EnumValuesControl(rootPane, toolkit, elements[i], this.getShell());
    			evc.setText(valueSet.toString());
    			this.editControls.add(evc.getTextControl());
    		} else if (valueSet.isRange()) {
    			toolkit.createFormLabel(rootPane, ""); //$NON-NLS-1$
    			toolkit.createFormLabel(rootPane, Messages.PolicyAttributesSection_minimum);
    			text = toolkit.createText(rootPane);
    			this.editControls.add(text);
    			field = new TextField(text);
    			controller.add(field, (Range) valueSet, Range.PROPERTY_LOWERBOUND);
    			
    			toolkit.createFormLabel(rootPane, ""); //$NON-NLS-1$
    			toolkit.createFormLabel(rootPane, Messages.PolicyAttributesSection_maximum);
    			text = toolkit.createText(rootPane);
    			field = new TextField(text);
    			this.editControls.add(text);
    			controller.add(field, (Range) valueSet, Range.PROPERTY_UPPERBOUND);
    			
    			toolkit.createFormLabel(rootPane, ""); //$NON-NLS-1$
    			toolkit.createFormLabel(rootPane, Messages.PolicyAttributesSection_step);
    			text = toolkit.createText(rootPane);
    			field = new TextField(text);
    			this.editControls.add(text);
    			controller.add(field, (Range) valueSet, Range.PROPERTY_STEP);
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
		super.setEnabled(enabled);
		
		// to get the disabled look, we have to disable all the input-fields manually :-(
		for (Iterator iter = editControls.iterator(); iter.hasNext();) {
			Text element = (Text) iter.next();
			element.setEnabled(enabled);
			
		}
		rootPane.layout(true);
		rootPane.redraw();
	}

}
