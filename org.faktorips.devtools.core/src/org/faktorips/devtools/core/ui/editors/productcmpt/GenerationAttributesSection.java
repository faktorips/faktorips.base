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
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ValueSet;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.CompositeUIController;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;
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
	private List editControls = new ArrayList();
	
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
	public GenerationAttributesSection(IProductCmptGeneration generation,
			Composite parent, UIToolkit toolkit, ProductCmptEditor editor) {
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
		
        // create controls for config elements
		createEditControls();
		uiMasterController.updateUI();
	}

    protected void updateGenerationText() {
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
        String generationConceptName = IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNameSingular(); 
        setText(generationConceptName + ": " + validRange); //$NON-NLS-1$
    }
    
	/**
	 * {@inheritDoc}
	 */
	protected void performRefresh() {
		if (uiMasterController != null) {
			uiMasterController.updateUI();
		}
	}

	private void createEditControls() {
		uiMasterController = new CompositeUIController();

		// create a label and edit control for each config element
		IConfigElement[] elements = generation.getConfigElements(ConfigElementType.PRODUCT_ATTRIBUTE);
		Arrays.sort(elements, new ConfigElementComparator());
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
		
		if (isDisposed()) {
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
		if (toDisplay == null) {
			// this can happen if the config element has no corresponding attribute... 
			return;
		}
		
		toolkit.createLabel(rootPane, StringUtils.capitalise(toDisplay.getPcTypeAttribute()));	
		
		IpsPartUIController controller = new IpsPartUIController(toDisplay);
		uiMasterController.add(controller);
	
		try {
			IAttribute attr = toDisplay.findPcTypeAttribute();
			Datatype datatype = null;
			if (attr != null) {
				datatype = attr.findDatatype();
			}
			ValueDatatypeControlFactory ctrlFactory;
			if (datatype != null && datatype.isValueDatatype()) {
				ctrlFactory = IpsPlugin.getDefault().getValueDatatypeControlFactory((ValueDatatype)datatype);
			}
			else {
				ctrlFactory = IpsPlugin.getDefault().getValueDatatypeControlFactory(null);
			}
			
			EditField field = ctrlFactory.createEditField(toolkit, rootPane, (ValueDatatype)datatype, (ValueSet)toDisplay.getValueSet());
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
