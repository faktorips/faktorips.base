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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.CompositeUIController;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.IpsObjectField;
import org.faktorips.devtools.core.ui.controls.ProductCmptTypeRefControl;
import org.faktorips.devtools.core.ui.controls.TextButtonControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * Section to display and edit the product attributes
 * 
 * @author Thorsten Guenther
 */
public class ProductAttributesSection extends IpsSection {

	/**
	 * Product component which holds the informations to display
	 */
	private IProductCmpt product;
	
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
	
	private ProductCmptTypeRefControl policyCmptType;
    private MyModifyListener policyCmptTypeListener;
	
	private Text runtimeId;
	
	
	private ProductCmptEditor editor;
	
	/**
	 * Creates a new attributes section.
	 * 
	 * @param generation The generation to get all informations to display from.
	 * @param parent The parent to link the ui-items to.
	 * @param toolkit The toolkit to use for easier ui-handling
	 */
	public ProductAttributesSection(IProductCmpt product, Composite parent, UIToolkit toolkit, ProductCmptEditor editor) {
		super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);
        this.product = product;
		this.editor = editor;
		initControls();
		setText(Messages.ProductAttributesSection_attribute);
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

		// following line forces the paint listener to draw a light grey border around
		// the text control. Can only be understood by looking at the
		// FormToolkit.PaintBorder class.
		rootPane.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
		toolkit.getFormToolkit().paintBordersFor(rootPane);
		
		// create label and text control for the policy component type
		// this product component is based on.
		toolkit.createLabel(rootPane, Messages.ProductAttributesSection_template);

		policyCmptType = new ProductCmptTypeRefControl(product.getIpsProject(), rootPane, toolkit);
		policyCmptType.getTextControl().setEnabled(false);
		ProductCmptTypeField field = new ProductCmptTypeField(policyCmptType);
		
		// create label and text control for the runtime id representing the displayed product component
		toolkit.createLabel(rootPane, Messages.ProductAttributesSection_labelRuntimeId);
		runtimeId = toolkit.createText(rootPane);
		editControls.add(runtimeId);

        // create label and text control for the valid-to date of the displayed product component
//       pk: disabled for version 0.9.35        
//        toolkit.createLabel(rootPane, Messages.ProductAttributesSection_labelValidTo);
//        Text validTo = toolkit.createText(rootPane);
//        editControls.add(validTo);

		IpsObjectUIController controller = new IpsObjectUIController(product);
		controller.add(field, product, IProductCmpt.PROPERTY_POLICY_CMPT_TYPE);
		controller.add(runtimeId, product, IProductCmpt.PROPERTY_RUNTIME_ID);
//       pk: disabled for version 0.9.35        
//        GregorianCalendarField validToField = new GregorianCalendarField(validTo);
//        controller.add(validToField, generation.getProductCmpt(), IProductCmpt.PROPERTY_VALID_TO);
//
//        validToField.addChangeListener(new ValueChangeListener() {
//            public void valueChanged(FieldValueChangedEvent e) {
//                updateGenerationText();
//            }
//        });
//        
        uiMasterController = new CompositeUIController();
		uiMasterController.add(controller);
		uiMasterController.updateUI();
		
        policyCmptTypeListener = new MyModifyListener();
        policyCmptType.getTextControl().addModifyListener(policyCmptTypeListener);
        
		// update enablement state of runtime-id-input if preference changed
		IpsPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (!event.getProperty().equals(IpsPreferences.MODIFY_RUNTIME_ID)) {
					return;
				}
				if (!runtimeId.isDisposed()) {
				    runtimeId.setEnabled(isEnabled() && IpsPlugin.getDefault().getIpsPreferences().canModifyRuntimeId());
				    layout();
				}
                else {
                    IpsPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
                }
			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	protected void performRefresh() {
		if (uiMasterController != null) {
			uiMasterController.updateUI();
		}
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
		
		policyCmptType.setButtonEnabled(enabled);
		runtimeId.setEnabled(enabled && IpsPlugin.getDefault().getIpsPreferences().canModifyRuntimeId());
		
		rootPane.layout(true);
		rootPane.redraw();
	}
	
	
	private class ProductCmptTypeField extends IpsObjectField {

		/**
		 * @param control
		 */
		public ProductCmptTypeField(TextButtonControl control) {
			super(control);
		}

		public String getText() {
			try {
				IProductCmptType type = product.getIpsProject().findProductCmptType(super.getText());
				if (type != null) {
					return type.getPolicyCmptyType();
				}
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
			return super.getText();
		}

		public Object getValue() {
			return getText();
		}

		public void insertText(String text) {
			super.insertText(text);
		}

		public void setText(String newText) {
			try {
				IPolicyCmptType type = product.getIpsProject().findPolicyCmptType(newText);
				if (type != null) {
					super.setText(type.getProductCmptType());
				}
				return;
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
			super.setText(newText);
		}

		public void setValue(Object newValue) {
			setText((String)newValue);
		}
		
	}

	private class MyModifyListener implements ModifyListener {
		
		public void modifyText(ModifyEvent e) {
            policyCmptType.getTextControl().removeModifyListener(this);
			uiMasterController.updateUI();
			uiMasterController.updateModel();
			editor.forceRefresh();
            policyCmptType.getTextControl().addModifyListener(this);
		}
	};
}
