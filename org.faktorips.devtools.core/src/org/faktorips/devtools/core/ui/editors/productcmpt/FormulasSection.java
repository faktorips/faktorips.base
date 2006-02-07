package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.product.ConfigElement;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.ui.controller.CompositeUIController;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controls.FormulaEditControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.ArgumentCheck;

/**
 * 
 */
public class FormulasSection extends IpsSection {
	
	private IProductCmptGeneration generation;
	private Composite workArea;
	private UIToolkit toolkit;
	private CompositeUIController uiController;
	private boolean fGenerationDirty;
	private Label noFormulasLabel;
	private ArrayList labels;
	
	public FormulasSection(IProductCmptGeneration generation, Composite parent, UIToolkit toolkit) {
		super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);
		ArgumentCheck.notNull(generation);
		
		this.generation = generation;
		fGenerationDirty = true;
		initControls();
		setText(Messages.FormulasSection_calculationFormulas);
	}
	
	/**
	 * Overridden
	 */
	protected void initClientComposite(Composite client, UIToolkit toolkit) {
    	GridLayout layout = new GridLayout(1, true);
    	layout.marginHeight = 2;
    	layout.marginWidth = 1;
    	client.setLayout(layout);
    	workArea = toolkit.createLabelEditColumnComposite(client);
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
	
	/**
	 * Overridden method.
	 */
	protected void performRefresh() {
		if (fGenerationDirty || structureChanged()) {
			createEditControls();
		}
	}
	
	/**
	 * Determines whether the structure of the underlying data has changed
	 */
	private boolean structureChanged() {
		return true;
	}
	
    private void createEditControls() {
    	if (labels == null) {
    		labels = new ArrayList();
    	}
    	
    	uiController = new CompositeUIController();
    	IpsObjectUIController ctrl = new IpsObjectUIController(generation.getIpsObject());
    	uiController.add(ctrl);
    	
    	IConfigElement[] elements = generation.getConfigElements(ConfigElementType.FORMULA);
    	
    	if (elements.length == 0 && noFormulasLabel == null) {
    		noFormulasLabel = toolkit.createLabel(workArea, Messages.FormulasSection_noFormulasDefined);
    	}
    	else if (elements.length > 0 && noFormulasLabel != null) {
			noFormulasLabel.dispose();
			noFormulasLabel = null;
    	}
    		    	
    	for (int i = 0; i < elements.length; i++) {
    		
    		if (i < labels.size()) {
    			((Label)labels.get(i)).setText(StringUtils.capitalise(elements[i].getName()));
    		}
    		else {
    			Label label = toolkit.createFormLabel(workArea, StringUtils.capitalise(elements[i].getName()));
    			labels.add(label);
    			FormulaEditControl evc = new FormulaEditControl(workArea, toolkit, elements[i], this.getShell());
    			evc.setText(elements[i].getValue());
    			ctrl.add(evc.getTextControl(), elements[i], ConfigElement.PROPERTY_VALUE);
    			try {
    				FormulaCompletionProcessor completionProcessor = new FormulaCompletionProcessor(elements[i].getIpsProject(), elements[i].getExprCompiler());
    				ContentAssistHandler.createHandlerForText(evc.getTextControl(), CompletionUtil.createContentAssistant(completionProcessor));
    			} catch (CoreException e) {
    				IpsPlugin.logAndShowErrorDialog(e);
    			}
    		}    		
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
