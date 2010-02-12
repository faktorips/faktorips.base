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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.CompositeUIController;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.GregorianCalendarField;
import org.faktorips.devtools.core.ui.controller.fields.IpsObjectField;
import org.faktorips.devtools.core.ui.controls.ProductCmptType2RefControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Section to display and edit the product attributes
 * 
 * @author Thorsten Guenther
 */
public class ComponentPropertiesSection extends IpsSection {

    /**
     * Product component which holds the informations to display
     */
    private final IProductCmpt product;

    private final ExtensionPropertyControlFactory extFactory;

    /**
     * Pane which serves as parent for all controlls created inside this section.
     */
    private Composite rootPane;

    /**
     * List of controls displaying data (needed to enable/disable).
     */
    private final List<Text> editControls = new ArrayList<Text>();

    /**
     * Controller to handle update of ui and model automatically.
     */
    private CompositeUIController uiMasterController = null;

    private ProductCmptType2RefControl productCmptTypeControl;
    private MyModifyListener policyCmptTypeListener;

    private Text runtimeIdText;
    private Text validToText;
    private GregorianCalendarField validToField;
    private final ProductCmptEditor editor;

    /**
     * Creates a new attributes section.
     * 
     * @param generation The generation to get all informations to display from.
     * @param parent The parent to link the ui-items to.
     * @param toolkit The toolkit to use for easier ui-handling
     */
    public ComponentPropertiesSection(IProductCmpt product, Composite parent, UIToolkit toolkit, ProductCmptEditor editor) {
        super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);
        this.product = product;
        extFactory = new ExtensionPropertyControlFactory(product.getClass());
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

        // create label or hyperlink and text control for the policy component type
        // this product component is based on.
        if (IpsPlugin.getDefault().getIpsPreferences().canNavigateToModelOrSourceCode()) {
            Hyperlink link = toolkit.createHyperlink(rootPane, Messages.ProductAttributesSection_template);
            link.addHyperlinkListener(new HyperlinkAdapter() {
                public void linkActivated(HyperlinkEvent event) {
                    try {
                        if (!IpsPlugin.getDefault().getIpsPreferences().canNavigateToModelOrSourceCode()){
                            // if the property changed while the editor is open
                            return;
                        }
                        IProductCmptType productCmptType = product.findProductCmptType(product.getIpsProject());
                        if (productCmptType != null) {
                            IpsUIPlugin.getDefault().openEditor(productCmptType);
                        }
                    } catch (Exception e) {
                        IpsPlugin.logAndShowErrorDialog(e);
                    }
                }
            });
        } else {
            toolkit.createLabel(rootPane, Messages.ProductAttributesSection_template);
        }

        productCmptTypeControl = new ProductCmptType2RefControl(product.getIpsProject(), rootPane, toolkit, true);
        toolkit.setDataChangeable(productCmptTypeControl.getTextControl(), false);
        IpsObjectField field = new IpsObjectField(productCmptTypeControl);

        // create label and text control for the runtime id representing the displayed product component
        toolkit.createLabel(rootPane, Messages.ProductAttributesSection_labelRuntimeId);
        runtimeIdText = toolkit.createText(rootPane);
        editControls.add(runtimeIdText);

        // create label and text control for the valid-to date of the displayed product component
        toolkit.createLabel(rootPane, Messages.ProductAttributesSection_labelValidTo);
        validToText = toolkit.createText(rootPane);
        validToText.setText(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());
        editControls.add(validToText);

        IpsObjectUIController controller = new IpsObjectUIController(product);
        controller.add(field, product, IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE);
        controller.add(runtimeIdText, product, IProductCmpt.PROPERTY_RUNTIME_ID);
        validToField = new GregorianCalendarField(validToText);
        controller.add(validToField, product, IProductCmpt.PROPERTY_VALID_TO);

        // handle invalid values - the gregorian calendar field transforms all invalid values
        // to null, so invalid strings like "egon" can not be validated in the normal way by
        // validating the object itself
        validToText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                String value = validToText.getText();
                if (value.equals(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation())) {
                    return;
                }

                DateFormat format = IpsPlugin.getDefault().getIpsPreferences().getDateFormat();
                try {
                    String parsed = format.format(format.parse(value));
                    if (!parsed.equals(value)) {
                        throw new ParseException(value + " parsed to " + parsed, 0); //$NON-NLS-1$
                    }
                } catch (ParseException e1) {
                    MessageList list = new MessageList();
                    String msg = NLS.bind(Messages.ProductAttributesSection_msgInvalidDate, value);
                    list.add(new Message("", msg, Message.ERROR, product, IProductCmpt.PROPERTY_VALID_TO)); //$NON-NLS-1$
                    validToField.setMessages(list);
                }
            }
        });

        uiMasterController = new CompositeUIController();
        uiMasterController.add(controller);
        uiMasterController.updateUI();

        policyCmptTypeListener = new MyModifyListener();
        productCmptTypeControl.getTextControl().addModifyListener(policyCmptTypeListener);

        // update enablement state of runtime-id-input if preference changed
        IpsPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (!event.getProperty().equals(IpsPreferences.MODIFY_RUNTIME_ID)) {
                    return;
                }
                if (!runtimeIdText.isDisposed()) {
                    updateRuntimeIdEnableState();
                } else {
                    IpsPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
                }
            }
        });

        extFactory.createControls(rootPane,toolkit,product);
        extFactory.bind(bindingContext);
    }

    /**
     * {@inheritDoc}
     */
    protected void performRefresh() {
        if (uiMasterController != null) {
            uiMasterController.updateUI();
        }
        updateRuntimeIdEnableState();
        bindingContext.updateUI();
    }

    /**
     * {@inheritDoc}
     */
    public void setDataChangeable(boolean changeable) {
        super.setDataChangeable(changeable);
        updateRuntimeIdEnableState();
        if (changeable) {
            productCmptTypeControl.setButtonEnabled(true);
        } else {
            productCmptTypeControl.setButtonEnabled(editor.couldDateBeChangedIfProductCmptTypeWasntMissing());
        }
    }

    private void updateRuntimeIdEnableState() {
        getToolkit().setDataChangeable(runtimeIdText, isDataChangeable() & IpsPlugin.getDefault().getIpsPreferences().canModifyRuntimeId());
    }

    private class MyModifyListener implements ModifyListener {

        public void modifyText(ModifyEvent e) {
            productCmptTypeControl.getTextControl().removeModifyListener(this);
            uiMasterController.updateUI();
            uiMasterController.updateModel();
            editor.checkForInconsistenciesToModel();
            productCmptTypeControl.getTextControl().addModifyListener(this);
        }
    }

}
