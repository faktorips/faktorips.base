package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
import org.faktorips.devtools.core.ui.controller.fields.ValueTextField;
import org.faktorips.devtools.core.ui.controls.FormulaEditControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.ArgumentCheck;

/**
 * Section to display and edit the formulas of a product
 * 
 * @author Thorsten Guenther
 */
public class FormulasSection extends IpsSection {

	/**
	 * Generation which holds the informations to display
	 */
	private IProductCmptGeneration generation;

	/**
	 * Pane which serves as parent for all controlls created inside this
	 * section.
	 */
	private Composite rootPane;

	/**
	 * Toolkit to handle common ui-operations
	 */
	private UIToolkit toolkit;

	/**
	 * List of controls displaying data (needed to enable/disable).
	 */
	private List editControls = new ArrayList();

	/**
	 * Controller to handle update of ui and model automatically.
	 */
	private CompositeUIController uiMasterController;

	/**
	 * Label which is displayed if no formulas are defined.
	 */
	private Label noFormulasLabel;

	public FormulasSection(IProductCmptGeneration generation, Composite parent,
			UIToolkit toolkit) {
		super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);
		ArgumentCheck.notNull(generation);

		this.generation = generation;
		initControls();
		setText(Messages.FormulasSection_calculationFormulas);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void initClientComposite(Composite client, UIToolkit toolkit) {
		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 2;
		layout.marginWidth = 1;
		client.setLayout(layout);
		rootPane = toolkit.createLabelEditColumnComposite(client);
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
	 * {@inheritDoc}
	 */
	protected void performRefresh() {
		uiMasterController.updateUI();
	}

	/**
	 * Create the ui-elements
	 */
	private void createEditControls() {
		uiMasterController = new CompositeUIController();
		IpsObjectUIController ctrl = new IpsObjectUIController(generation
				.getIpsObject());
		uiMasterController.add(ctrl);

		IConfigElement[] elements = generation
				.getConfigElements(ConfigElementType.FORMULA);

		if (elements.length == 0 && noFormulasLabel == null) {
			noFormulasLabel = toolkit.createLabel(rootPane,
					Messages.FormulasSection_noFormulasDefined);
		} else if (elements.length > 0 && noFormulasLabel != null) {
			noFormulasLabel.dispose();
			noFormulasLabel = null;
		}

		for (int i = 0; i < elements.length; i++) {

			toolkit.createFormLabel(rootPane, StringUtils
					.capitalise(elements[i].getName()));
			FormulaEditControl evc = new FormulaEditControl(rootPane, toolkit,
					elements[i], this.getShell());
			ctrl.add(new ValueTextField(evc.getTextControl()), elements[i],
					ConfigElement.PROPERTY_VALUE);
			this.editControls.add(evc);

			try {
				FormulaCompletionProcessor completionProcessor = new FormulaCompletionProcessor(
						elements[i].findPcTypeAttribute(), elements[i]
								.getIpsProject(), elements[i].getExprCompiler());
				ContentAssistHandler.createHandlerForText(evc.getTextControl(),
						CompletionUtil
								.createContentAssistant(completionProcessor));
			} catch (CoreException e) {
				IpsPlugin.logAndShowErrorDialog(e);
			}
		}

		rootPane.layout(true);
		rootPane.redraw();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEnabled(boolean enabled) {
//		super.setEnabled(enabled);

		// to get the disabled look, we have to disable all the input-fields
		// manually :-(
		for (Iterator iter = editControls.iterator(); iter.hasNext();) {
			Control element = (Control) iter.next();
			element.setEnabled(enabled);

		}
		rootPane.layout(true);
		rootPane.redraw();
	}

}
