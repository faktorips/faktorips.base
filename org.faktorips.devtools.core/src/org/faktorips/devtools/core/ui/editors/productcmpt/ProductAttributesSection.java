package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.CompositeUIController;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * 
 */
public class ProductAttributesSection extends IpsSection {

	private IProductCmptGeneration generation;

	private UIToolkit toolkit;

	private Composite workArea;

	// edit controls and fields
	private List editControls = new ArrayList();

	private List labels = new ArrayList();

	// Text field showing the policy component type
	private Text pcTypeText;

	// UIController
	private CompositeUIController uiController = null;

	public ProductAttributesSection(IProductCmptGeneration generation,
			Composite parent, UIToolkit toolkit) {
		super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);
		this.generation = generation;
		initControls();
		setText(Messages.ProductAttributesSection_attribute);
	}

	/**
	 * Overridden.
	 */
	protected void initClientComposite(Composite client, UIToolkit toolkit) {
		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 2;
		layout.marginWidth = 1;
		client.setLayout(layout);
		this.toolkit = toolkit;
		workArea = toolkit.createStructuredLabelEditColumnComposite(client);
		workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout workAreaLayout = (GridLayout) workArea.getLayout();
		workAreaLayout.marginHeight = 5;
		workAreaLayout.marginWidth = 5;

		// following line forces the paint listener to draw a light grey border
		// around
		// the text control. Can only be understood by looking at the
		// FormToolkit.PaintBorder class.
		workArea.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
		toolkit.getFormToolkit().paintBordersFor(workArea);
		// create label and text control for the policy component type
		// this product component is based on.
		toolkit.createLabel(workArea,
				Messages.ProductAttributesSection_template);
		toolkit.createLabel(workArea, ""); //$NON-NLS-1$
		pcTypeText = toolkit.createText(workArea);
		pcTypeText.setEnabled(false);
	}

	/**
	 * Overridden method.
	 * 
	 * @see org.faktorips.devtools.core.ui.forms.IpsSection#performRefresh()
	 */
	protected void performRefresh() {
		try {
			if (structureChanged()) {
				createEditControls();
				IpsObjectUIController controller = new IpsObjectUIController(
						generation.getIpsObject());
				controller.add(new TextField(pcTypeText),
						IProductCmpt.PROPERTY_POLICY_CMPT_TYPE);
				uiController.add(controller);
			}
			uiController.updateUI();
		} catch (CoreException e) {
			IpsPlugin.log(e);
		}
	}

	private boolean structureChanged() {
		if (uiController == null) {
			// page hasn't been initialized, consider that as a change so that
			// the controls are created.
			return true;
		}
		IConfigElement[] elements = generation
				.getConfigElements(ConfigElementType.PRODUCT_ATTRIBUTE);
		if (labels.size() != elements.length) {
			return true;
		}
		for (int i = 0; i < elements.length; i++) {
			Label label = (Label) labels.get(i);
			if (!label.getText().equals(
					StringUtils.capitalise(elements[i].getPcTypeAttribute()))) {
				return true;
			}
		}
		return false;
	}

	private void createEditControls() throws CoreException {

		uiController = new CompositeUIController();
		// create a label and edit control for each config element
		IConfigElement[] elements = generation
				.getConfigElements(ConfigElementType.PRODUCT_ATTRIBUTE);
		for (int i = 0; i < elements.length; i++) {
			if (i < labels.size()) {
				((Label) labels.get(i)).setText(elements[i]
						.getPcTypeAttribute());
			} else {
				Label label = toolkit.createLabel(workArea, StringUtils
						.capitalise(elements[i].getPcTypeAttribute()));
				labels.add(label);
				toolkit.createLabel(workArea, ""); //$NON-NLS-1$
				Text text = toolkit.createText(workArea);
				editControls.add(text);
				TextField field = new TextField(text);
				IpsPartUIController controller = new IpsPartUIController(
						elements[i]);
				controller.add(field, elements[i],
						IConfigElement.PROPERTY_VALUE);
				uiController.add(controller);
				toolkit.createVerticalSpacer(workArea, 3).setBackground(
						workArea.getBackground());
				toolkit.createVerticalSpacer(workArea, 3).setBackground(
						workArea.getBackground());
				toolkit.createVerticalSpacer(workArea, 3).setBackground(
						workArea.getBackground());
			}
		}

		for (int i = elements.length + 1; i < labels.size(); i++) {
			((Label) labels.get(i)).dispose();
			((Control) editControls.get(i)).dispose();
		}
		workArea.layout(true);
		workArea.redraw();
	}
}
