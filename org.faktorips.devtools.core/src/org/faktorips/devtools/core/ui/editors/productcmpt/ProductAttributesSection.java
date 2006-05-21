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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
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
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.CompositeUIController;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;
import org.faktorips.devtools.core.ui.controller.fields.EnumDatatypeField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.ArgumentCheck;

/**
 * Section to display and edit the product attributes
 * 
 * @author Thorsten Guenther
 */
public class ProductAttributesSection extends IpsSection {

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
	private List editControls = new ArrayList();
	
	// Text field showing the policy component type
	private Text pcTypeText;
	private Text generationText;

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
	public ProductAttributesSection(IProductCmptGeneration generation,
			Composite parent, UIToolkit toolkit) {
		super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);
		this.generation = generation;
		initControls();
		setText(Messages.ProductAttributesSection_attribute);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void initClientComposite(Composite client, UIToolkit toolkit) {
		this.toolkit = toolkit;

		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 2;
		layout.marginWidth = 1;
		client.setLayout(layout);

		rootPane = toolkit.createLabelEditColumnComposite(client);
		rootPane.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout workAreaLayout = (GridLayout) rootPane.getLayout();
		workAreaLayout.marginHeight = 5;
		workAreaLayout.marginWidth = 5;

		// following line forces the paint listener to draw a light grey border around
		// the text control. Can only be understood by looking at the
		// FormToolkit.PaintBorder class.
		rootPane.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
		toolkit.getFormToolkit().paintBordersFor(rootPane);
		
		// create label and text for the currently displayed generation
		String generationConceptName = IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNameSingular(); 
		toolkit.createLabel(rootPane, generationConceptName);
		
		DateFormat format = IpsPlugin.getDefault().getIpsPreferences().getValidFromFormat();
		String validRange = format.format(this.generation.getValidFrom().getTime());

		GregorianCalendar date = generation.getValidTo();
		String validToString;
		if (date == null) {
			validToString = Messages.ProductAttributesSection_valueGenerationValidToUnlimited;
		}
		else {
			validToString = IpsPlugin.getDefault().getIpsPreferences().getValidFromFormat().format(date.getTime());
		}

		validRange += " - " + validToString; //$NON-NLS-1$
		this.generationText = toolkit.createText(rootPane);
		this.generationText.setText(validRange);
		this.generationText.setEnabled(false);
		toolkit.createVerticalSpacer(rootPane, 2).setBackground(rootPane.getBackground());
		toolkit.createVerticalSpacer(rootPane, 2).setBackground(rootPane.getBackground());

		// create label and text control for the policy component type
		// this product component is based on.
		toolkit.createLabel(rootPane, Messages.ProductAttributesSection_template);
		this.pcTypeText = toolkit.createText(rootPane);
		this.pcTypeText.setEnabled(false);
		toolkit.createVerticalSpacer(rootPane, 2).setBackground(rootPane.getBackground());
		toolkit.createVerticalSpacer(rootPane, 2).setBackground(rootPane.getBackground());

		createEditControls();
		
		IpsObjectUIController controller = new IpsObjectUIController(generation.getIpsObject());
		try {
			IProductCmptType type = generation.getProductCmpt().findProductCmptType();
			if (type != null) {
				controller.add(new TextField(pcTypeText), type, IProductCmptType.PROPERTY_NAME);
			}
		} catch (CoreException e) {
			pcTypeText.setText(Messages.ProductAttributesSection_noProductCmptType);
		}
		uiMasterController.add(controller);
		uiMasterController.updateUI();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void performRefresh() {
		uiMasterController.updateUI();
	}

	private void createEditControls() {
		uiMasterController = new CompositeUIController();

		// create a label and edit control for each config element
		IConfigElement[] elements = generation.getConfigElements(ConfigElementType.PRODUCT_ATTRIBUTE);
		for (int i = 0; i < elements.length; i++) {
			addAndRegister(elements[i]);
		}

		rootPane.layout(true);
		rootPane.redraw();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEnabled(boolean enabled) {
		if (isEnabled() == enabled) {
			return;
		}
		
		super.setEnabled(enabled);
		
		// to get the disabled look, we have to disable all the input-fields manually :-(
		for (Iterator iter = editControls.iterator(); iter.hasNext();) {
			Control element = (Control) iter.next();
			element.setEnabled(enabled);
			
		}
		rootPane.layout(true);
		rootPane.redraw();
	}
	
	/**
	 * Creates a new label and input for the given config element and links the input with the config element.
	 */
	private void addAndRegister(IConfigElement toDisplay) {
		toolkit.createLabel(rootPane, StringUtils.capitalise(toDisplay.getPcTypeAttribute()));	
		
		IpsPartUIController controller = new IpsPartUIController(toDisplay);
		uiMasterController.add(controller);
	
		try {
			IAttribute attr = toDisplay.findPcTypeAttribute();
			Datatype datatype = null;
			if (attr != null) {
				datatype = attr.findDatatype();
			}
			
			if (datatype != null && datatype.equals(Datatype.BOOLEAN)) {
				Combo combo = toolkit.createComboForBoolean(rootPane, true, Messages.ProductAttributesSection_true, Messages.ProductAttributesSection_false);
				ComboField field = new BooleanComboField(combo);
				controller.add(field, toDisplay, IConfigElement.PROPERTY_VALUE);		
				addFocusControl(combo);
				editControls.add(combo);
			}
			else if (datatype != null && datatype.equals(Datatype.PRIMITIVE_BOOLEAN)) {
				Combo combo = toolkit.createComboForBoolean(rootPane, false, Messages.ProductAttributesSection_true, Messages.ProductAttributesSection_false);
				ComboField field = new BooleanComboField(combo);
				controller.add(field, toDisplay, IConfigElement.PROPERTY_VALUE);		
				addFocusControl(combo);
				editControls.add(combo);
			}
			else if (datatype != null && datatype instanceof EnumDatatype) {
				Combo combo = toolkit.createCombo(rootPane);
				EnumDatatypeField field = new EnumDatatypeField(combo, (EnumDatatype)datatype);
				controller.add(field, toDisplay, IConfigElement.PROPERTY_VALUE);		
				addFocusControl(combo);
				editControls.add(combo);
			}
			else {
				Text text = toolkit.createText(rootPane);
				addFocusControl(text);
				editControls.add(text);
				controller.add(new TextField(text), toDisplay, IConfigElement.PROPERTY_VALUE);		
			}
		} catch (CoreException e) {
			Text text = toolkit.createText(rootPane);
			addFocusControl(text);
			editControls.add(text);
			controller.add(text, toDisplay, IConfigElement.PROPERTY_VALUE);		
		}
		
		toolkit.createVerticalSpacer(rootPane, 3).setBackground(rootPane.getBackground());
		toolkit.createVerticalSpacer(rootPane, 3).setBackground(rootPane.getBackground());
	}
	
	class BooleanComboField extends ComboField {
		
		private String trueRepresentation;
		private String falseRepresentation;

		public BooleanComboField(Combo combo) {
			this(combo, Messages.ProductAttributesSection_true, Messages.ProductAttributesSection_false);
		}
		
		/**
		 * @param combo
		 */
		public BooleanComboField(Combo combo, String trueRepresentation, String falseRepresentation) {
			super(combo);
			ArgumentCheck.notNull(trueRepresentation);
			ArgumentCheck.notNull(falseRepresentation);
			this.trueRepresentation = trueRepresentation;
			this.falseRepresentation = falseRepresentation;
		}

		/**
		 * {@inheritDoc}
		 */
		public Object getValue() {
			String s = (String)super.getValue();
			if (s==null) {
				return null;
			} else if (s.equals(trueRepresentation)) {
				return Boolean.TRUE.toString();
			} else if (s.equals(falseRepresentation)) {
					return Boolean.FALSE.toString();
			}
			throw new RuntimeException("Unknown value " + s);
		}

		/**
		 * {@inheritDoc}
		 */
		public void setValue(Object newValue) {
			if (newValue==null) {
				super.setValue(newValue);
				return;
			}
			boolean bool = Boolean.valueOf((String)newValue).booleanValue();
			if (bool) {
				super.setValue(trueRepresentation);
			} else {
				super.setValue(falseRepresentation);
			}
		}
		
		
	}
}
