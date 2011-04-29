/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.CompositeUIController;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.DateControlField;
import org.faktorips.devtools.core.ui.controller.fields.GregorianCalendarFormat;
import org.faktorips.devtools.core.ui.controller.fields.IpsObjectField;
import org.faktorips.devtools.core.ui.controls.DateControl;
import org.faktorips.devtools.core.ui.controls.ProductCmptType2RefControl;
import org.faktorips.devtools.core.ui.controls.TextButtonControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * Section to display and edit the product attributes
 * 
 * @author Thorsten Guenther
 */
public class ComponentPropertiesSection extends IpsSection {

    private static final String ID = "org.faktorips.devtools.core.ui.editors.productcmpt.ComponentPropertiesSection"; //$NON-NLS-1$

    /** Product component which holds the informations to display. */
    private final IProductCmpt product;

    private final ExtensionPropertyControlFactory extFactory;

    /** Pane which serves as parent for all controls created inside this section. */
    private Composite rootPane;

    /** List of controls displaying data (needed to enable / disable). */
    private final List<Text> editControls = new ArrayList<Text>();

    /** Controller to handle update of UI and model automatically. */
    private CompositeUIController uiMasterController;

    private ProductCmptType2RefControl productCmptTypeControl;

    private TextButtonControl runtimeIdControl;

    private EditField<GregorianCalendar> validToField;

    private final ProductCmptEditor editor;

    public ComponentPropertiesSection(IProductCmpt product, Composite parent, UIToolkit toolkit,
            ProductCmptEditor editor) {

        super(ID, parent, GridData.FILL_BOTH, toolkit);

        this.product = product;
        this.editor = editor;
        extFactory = new ExtensionPropertyControlFactory(product.getClass());

        initControls();
        setText(Messages.ProductAttributesSection_attribute);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        // Initialize layout stuff
        initLayout(client);
        initRootPane(client, toolkit);

        // Initialize the individual rows of the section
        initProductCmptTypeRow(toolkit);
        initRuntimeIdRow(toolkit);
        initValidToRow(toolkit);

        // Initialize controllers
        IpsObjectUIController controller = initUiController();
        initUiMasterController(controller);

        extFactory.createControls(rootPane, toolkit, product);
        extFactory.bind(bindingContext);
    }

    private void initUiMasterController(IpsObjectUIController controller) {
        uiMasterController = new CompositeUIController();
        uiMasterController.add(controller);
        uiMasterController.updateUI();
    }

    private IpsObjectUIController initUiController() {
        IpsObjectUIController controller = new IpsObjectUIController(product);
        controller.add(new IpsObjectField(productCmptTypeControl), product, IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE);
        controller.add(runtimeIdControl.getTextControl(), product, IProductCmpt.PROPERTY_RUNTIME_ID);
        controller.add(validToField, product, IProductCmpt.PROPERTY_VALID_TO);
        return controller;
    }

    /**
     * Create label and text control for the valid-to date of the displayed {@link IProductCmpt}.
     */
    private void initValidToRow(UIToolkit toolkit) {
        toolkit.createLabel(rootPane, Messages.ProductAttributesSection_labelValidTo);
        DateControl dateControl = new DateControl(rootPane, toolkit);
        dateControl.setText(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());
        validToField = new DateControlField<GregorianCalendar>(dateControl, GregorianCalendarFormat.newInstance());
        editControls.add(dateControl.getTextControl());
    }

    /**
     * Create label and text control for the runtime id representing the displayed
     * {@link IProductCmpt}.
     */
    private void initRuntimeIdRow(UIToolkit toolkit) {
        toolkit.createLabel(rootPane, Messages.ProductAttributesSection_labelRuntimeId);
        runtimeIdControl = new RuntimeIdControl(rootPane, toolkit);
        editControls.add(runtimeIdControl.getTextControl());

        // Update the enabled state of the runtime-id-input field if the global preference changed
        IpsPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if (!event.getProperty().equals(IpsPreferences.MODIFY_RUNTIME_ID)) {
                    return;
                }
                if (!runtimeIdControl.isDisposed()) {
                    updateRuntimeIdEnableState();
                } else {
                    IpsPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
                }
            }
        });
    }

    private void updateRuntimeIdEnableState() {
        getToolkit().setDataChangeable(runtimeIdControl,
                isDataChangeable() & IpsPlugin.getDefault().getIpsPreferences().canModifyRuntimeId());
    }

    /**
     * Create label or hyperlink and text control for the {@link IProductCmptType} the displayed
     * {@link IProductCmpt} is based on.
     */
    private void initProductCmptTypeRow(UIToolkit toolkit) {
        if (IpsPlugin.getDefault().getIpsPreferences().canNavigateToModelOrSourceCode()) {
            Hyperlink link = toolkit.createHyperlink(rootPane, Messages.ProductAttributesSection_template);
            link.addHyperlinkListener(new HyperlinkAdapter() {
                @Override
                public void linkActivated(HyperlinkEvent event) {
                    if (!IpsPlugin.getDefault().getIpsPreferences().canNavigateToModelOrSourceCode()) {
                        // if the property changed while the editor is open
                        return;
                    }
                    try {
                        IProductCmptType productCmptType = product.findProductCmptType(product.getIpsProject());
                        if (productCmptType != null) {
                            IpsUIPlugin.getDefault().openEditor(productCmptType);
                        }
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } else {
            toolkit.createLabel(rootPane, Messages.ProductAttributesSection_template);
        }

        productCmptTypeControl = new ProductCmptType2RefControl(product.getIpsProject(), rootPane, toolkit, true);
        toolkit.setDataChangeable(productCmptTypeControl.getTextControl(), false);
        productCmptTypeControl.getTextControl().addModifyListener(new MyModifyListener());
    }

    private void initLayout(Composite client) {
        GridLayout layout = new GridLayout(1, true);
        layout.marginHeight = 2;
        layout.marginWidth = 1;
        client.setLayout(layout);
    }

    private void initRootPane(Composite client, UIToolkit toolkit) {
        rootPane = toolkit.createLabelEditColumnComposite(client);
        rootPane.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout workAreaLayout = (GridLayout)rootPane.getLayout();
        workAreaLayout.marginHeight = 5;
        workAreaLayout.marginWidth = 5;

        /*
         * following line forces the paint listener to draw a light grey border around the text
         * control. Can only be understood by looking at the FormToolkit.PaintBorder class.
         */
        rootPane.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
        toolkit.getFormToolkit().paintBordersFor(rootPane);
    }

    @Override
    protected void performRefresh() {
        if (uiMasterController != null) {
            uiMasterController.updateUI();
        }
        updateRuntimeIdEnableState();
        bindingContext.updateUI();
    }

    @Override
    public void setDataChangeable(boolean changeable) {
        super.setDataChangeable(changeable);
        updateRuntimeIdEnableState();
        if (changeable) {
            productCmptTypeControl.setButtonEnabled(true);
        } else {
            productCmptTypeControl.setButtonEnabled(editor.couldDateBeChangedIfProductCmptTypeWasntMissing());
        }
    }

    private class MyModifyListener implements ModifyListener {

        @Override
        public void modifyText(ModifyEvent e) {
            productCmptTypeControl.getTextControl().removeModifyListener(this);
            uiMasterController.updateUI();
            uiMasterController.updateModel();
            editor.checkForInconsistenciesToModel();
            productCmptTypeControl.getTextControl().addModifyListener(this);
        }

    }

    /**
     * Needed so a "Generate" button can be attached to the runtime-id input field.
     * <p>
     * The "Generate" button enables the user to automatically generate a runtime-id based on the
     * name of the {@link IProductCmpt} and the {@link IProductCmptNamingStrategy} of the associated
     * {@link IIpsProject}.
     */
    private class RuntimeIdControl extends TextButtonControl {

        public RuntimeIdControl(Composite parent, UIToolkit toolkit) {
            super(parent, toolkit, Messages.ProductAttributesSection_ButtonLabel_GenerateRuntimeId);
        }

        @Override
        protected void buttonClicked() {
            IIpsProject ipsProject = product.getIpsProject();
            IProductCmptNamingStrategy namingStrategy = ipsProject.getProductCmptNamingStrategy();
            try {
                /*
                 * First set the runtime ID to null so that in fact nothing happens when the button
                 * is clicked while the runtime ID equals the runtime ID that would be generated.
                 */
                product.setRuntimeId(null);
                String generatedRuntimeId = namingStrategy.getUniqueRuntimeId(ipsProject, product.getName());
                product.setRuntimeId(generatedRuntimeId);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
