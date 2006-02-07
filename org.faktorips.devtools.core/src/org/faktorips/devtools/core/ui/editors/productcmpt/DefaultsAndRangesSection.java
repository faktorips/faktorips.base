package org.faktorips.devtools.core.ui.editors.productcmpt;

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
 *
 */
public class DefaultsAndRangesSection extends IpsSection {

    private IProductCmptGeneration generation;
    private Composite workArea;
    private CompositeUIController uiController;
    private UIToolkit toolkit;
	private boolean fGenerationDirty;
    
    public DefaultsAndRangesSection(
            IProductCmptGeneration generation,
            Composite parent,
            UIToolkit toolkit) {
        super(parent, Section.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);
        ArgumentCheck.notNull(generation);
        this.generation = generation;
		fGenerationDirty = true;
        initControls();
        setText(Messages.PolicyAttributesSection_defaultsAndRanges);
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.forms.IpsSection#initClientComposite(org.eclipse.swt.widgets.Composite, org.faktorips.devtools.core.ui.UIToolkit)
     */
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
    	GridLayout layout = new GridLayout(1, true);
    	layout.marginHeight = 2;
    	layout.marginWidth = 1;
    	client.setLayout(layout);
    	workArea = toolkit.createStructuredLabelEditColumnComposite(client);
    	workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
    	GridLayout workAreaLayout = (GridLayout) workArea.getLayout();
    	workAreaLayout.marginHeight = 5;
    	workAreaLayout.marginWidth = 5;
    	this.toolkit = toolkit;
    	  	
    	// following line forces the paint listener to draw a light grey border
    	// around
    	// the text control. Can only be understood by looking at the
    	// FormToolkit.PaintBorder class.
    	workArea.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
    	toolkit.getFormToolkit().paintBordersFor(workArea);
    }
    
    private void createEditControls() {
    	uiController = new CompositeUIController();
    	IpsObjectUIController ctrl = new IpsObjectUIController(generation.getIpsObject());
    	uiController.add(ctrl);
    	
    	IConfigElement[] elements = generation.getConfigElements(ConfigElementType.POLICY_ATTRIBUTE);
    	
    	if (elements.length == 0) {
    		toolkit.createLabel(workArea, Messages.PolicyAttributesSection_noDefaultsAndRangesDefined);
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
    		toolkit.createFormLabel(workArea, StringUtils.capitalise(elements[i].getName()));
    		toolkit.createFormLabel(workArea, Messages.PolicyAttributeEditDialog_defaultValue);
    		Text text = toolkit.createText(workArea);
    		TextField field = new TextField(text);
    		IpsPartUIController controller = new IpsPartUIController(elements[i]);
    		controller.add(field, elements[i], IConfigElement.PROPERTY_VALUE);
    		uiController.add(controller);
    		
    		if (valueSet.isEnum()) {
    			toolkit.createFormLabel(workArea, ""); //$NON-NLS-1$
    			toolkit.createFormLabel(workArea, Messages.PolicyAttributesSection_values);
    			EnumValuesControl evc = new EnumValuesControl(workArea, toolkit, elements[i], this.getShell());
    			evc.setText(valueSet.toString());
    		} else if (valueSet.isRange()) {
    			toolkit.createFormLabel(workArea, ""); //$NON-NLS-1$
    			toolkit.createFormLabel(workArea, Messages.PolicyAttributesSection_minimum);
    			text = toolkit.createText(workArea);
    			field = new TextField(text);
    			controller.add(field, (Range) valueSet, Range.PROPERTY_LOWERBOUND);
    			
    			toolkit.createFormLabel(workArea, ""); //$NON-NLS-1$
    			toolkit.createFormLabel(workArea, Messages.PolicyAttributesSection_maximum);
    			text = toolkit.createText(workArea);
    			field = new TextField(text);
    			controller.add(field, (Range) valueSet, Range.PROPERTY_UPPERBOUND);
    			
    			toolkit.createFormLabel(workArea, ""); //$NON-NLS-1$
    			toolkit.createFormLabel(workArea, Messages.PolicyAttributesSection_step);
    			text = toolkit.createText(workArea);
    			field = new TextField(text);
    			controller.add(field, (Range) valueSet, Range.PROPERTY_STEP);
    		}
    		toolkit.createVerticalSpacer(workArea, 3).setBackground(workArea.getBackground());
    		toolkit.createVerticalSpacer(workArea, 3).setBackground(workArea.getBackground());
    		toolkit.createVerticalSpacer(workArea, 3).setBackground(workArea.getBackground());
    	}
    	    	
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.forms.IpsSection#performRefresh()
     */
    protected void performRefresh() {
		if (fGenerationDirty) {
	    	createEditControls();
	    	uiController.updateUI();
		}
    }

	public void setActiveGeneration(IProductCmptGeneration generation) {
		if (this.generation.equals(generation)) {
			return;
		}
		
		if (generation instanceof IProductCmptGeneration) {
			this.generation = (IProductCmptGeneration)generation;
			fGenerationDirty = true;
			performRefresh();
		}
	}
}
