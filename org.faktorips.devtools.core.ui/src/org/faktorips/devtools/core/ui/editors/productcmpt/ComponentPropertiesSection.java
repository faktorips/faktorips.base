/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
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
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.binding.PropertyChangeBinding;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.DateControlField;
import org.faktorips.devtools.core.ui.controller.fields.IpsObjectField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.DateControl;
import org.faktorips.devtools.core.ui.controls.ProductCmptRefControl;
import org.faktorips.devtools.core.ui.controls.ProductCmptType2RefControl;
import org.faktorips.devtools.core.ui.controls.TextButtonControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.core.ui.inputformat.GregorianCalendarFormat;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.internal.IpsStringUtils;

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
    private final List<Text> editControls = new ArrayList<>();

    private ProductCmptType2RefControl productCmptTypeControl;

    /**
     * Control to edit the runtime id. Will be {@code null} for product templates (as they do not
     * have a runtime id). If the control exists it is enabled/disabled depending on the preferences
     * (see {@link #updateRuntimeIdEnableState()}.
     */
    private TextButtonControl runtimeIdControl;

    private EditField<GregorianCalendar> validFromField;

    private EditField<GregorianCalendar> validToField;

    private ProductCmptRefControl templateControl;

    private final ProductCmptEditor editor;

    private ComponentPropertiesPMO componentPropertiesPMO;

    public ComponentPropertiesSection(IProductCmpt product, Composite parent, UIToolkit toolkit,
            ProductCmptEditor editor) {

        super(ID, parent, GridData.FILL_BOTH, toolkit);

        this.product = product;
        this.editor = editor;
        extFactory = new ExtensionPropertyControlFactory(product);
        componentPropertiesPMO = new ComponentPropertiesPMO(product);

        initControls();
        setText(Messages.ComponentPropertiesSection_title);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        // Initialize layout stuff
        initLayout(client);
        initRootPane(client, toolkit);

        // Initialize the individual rows of the section
        initProductCmptTypeRow(toolkit);
        initTemplateRow(toolkit);
        initRuntimeIdRow(toolkit);
        initValidFromRow(toolkit);
        initValidToRow(toolkit);

        bind();

        extFactory.createControls(rootPane, toolkit, product);
        extFactory.bind(getBindingContext());

        getBindingContext().updateUI();
    }

    private void bind() {
        getBindingContext().bindContent(new IpsObjectField(productCmptTypeControl), product,
                IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE);

        // Templates do not have a runtime id
        if (!product.isProductTemplate()) {
            getBindingContext().bindContent(new TextButtonField(runtimeIdControl), product,
                    IProductCmpt.PROPERTY_RUNTIME_ID);
        }

        getBindingContext().bindContent(validFromField, componentPropertiesPMO,
                ComponentPropertiesPMO.PROPERTY_VALID_FROM);
        getBindingContext().bindEnabled(validFromField.getControl(), componentPropertiesPMO,
                ComponentPropertiesPMO.PROPERTY_VALID_FROM_ENABLED);

        getBindingContext().bindContent(validToField, product, IProductCmpt.PROPERTY_VALID_TO);

        getBindingContext().bindContent(new IpsObjectField(templateControl), product, IProductCmpt.PROPERTY_TEMPLATE);
    }

    /**
     * Create label and text control for the valid-to date of the displayed {@link IProductCmpt}.
     */
    private void initValidFromRow(UIToolkit toolkit) {
        toolkit.createLabel(rootPane, Messages.ComponentPropertiesSection_labelValidFrom);
        DateControl dateControl = new DateControl(rootPane, toolkit);
        dateControl.setText(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());
        validFromField = new DateControlField<>(dateControl, GregorianCalendarFormat.newInstance());
        editControls.add(dateControl.getTextControl());
    }

    /**
     * Create label and text control for the valid-to date of the displayed {@link IProductCmpt}.
     */
    private void initValidToRow(UIToolkit toolkit) {
        toolkit.createLabel(rootPane, Messages.ProductAttributesSection_labelValidTo);
        DateControl dateControl = new DateControl(rootPane, toolkit);
        dateControl.setText(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());
        validToField = new DateControlField<>(dateControl, GregorianCalendarFormat.newInstance());
        editControls.add(dateControl.getTextControl());
    }

    /**
     * Create label and text control for the runtime id representing the displayed
     * {@link IProductCmpt}.
     */
    private void initRuntimeIdRow(UIToolkit toolkit) {
        // Templates do not have a runtime id
        if (product.isProductTemplate()) {
            return;
        }
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
        createLabelOrHyperlink(toolkit, Messages.ProductAttributesSection_type,
                () -> product.findProductCmptType(product.getIpsProject()));

        productCmptTypeControl = new ProductCmptType2RefControl(product.getIpsProject(), rootPane, toolkit,
                !product.isProductTemplate());
        toolkit.setEnabled(productCmptTypeControl.getTextControl(), false);
        getBindingContext().add(new PropertyChangeBinding<>(productCmptTypeControl, product,
                IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE, String.class) {

            @Override
            protected void propertyChanged(String oldValue, String newValue) {
                editor.checkForInconsistenciesToModel();
            }
        });
    }

    private void initTemplateRow(UIToolkit toolkit) {
        createLabelOrHyperlink(toolkit, Messages.ComponentPropertiesSection_TemplateName,
                () -> product.findTemplate(product.getIpsProject()), true);

        templateControl = new ProductCmptRefControl(product.getIpsProject(), rootPane, toolkit, true);
        templateControl.setProductCmptType(product.findProductCmptType(product.getIpsProject()), true);
        templateControl.setSearchTemplates(true);
        templateControl.setProductCmptsToExclude(new IProductCmpt[] { product });
        toolkit.setEnabled(templateControl.getTextControl(), false);
        getBindingContext().add(new PropertyChangeBinding<>(templateControl, product,
                IProductCmpt.PROPERTY_TEMPLATE, String.class) {

            @Override
            protected void propertyChanged(String oldValue, String newValue) {
                if (!Objects.equals(oldValue, newValue)) {
                    editor.refreshIncludingStructuralChanges();
                }
                editor.checkForInconsistenciesToModel();
            }
        });
    }

    private void createLabelOrHyperlink(UIToolkit toolkit,
            String labelText,
            final IpsObjectFinder ipsObjectFinder,
            boolean navigationAllowed) {
        if (navigationAllowed && ipsObjectFinder.findIpsObject() != null) {
            Hyperlink link = toolkit.createHyperlink(rootPane, labelText);
            link.addHyperlinkListener(new HyperlinkAdapter() {
                @Override
                public void linkActivated(HyperlinkEvent event) {
                    IIpsObject ipsObject = ipsObjectFinder.findIpsObject();
                    if (ipsObject != null) {
                        IpsUIPlugin.getDefault().openEditor(ipsObject);
                    }
                }
            });
        } else {
            toolkit.createLabel(rootPane, labelText);
        }
    }

    private void createLabelOrHyperlink(UIToolkit toolkit, String labelText, final IpsObjectFinder ipsObjectFinder) {
        createLabelOrHyperlink(toolkit, labelText, ipsObjectFinder,
                IpsPlugin.getDefault().getIpsPreferences().canNavigateToModelOrSourceCode());
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
        getBindingContext().updateUI();
        updateRuntimeIdEnableState();
    }

    @Override
    public void setDataChangeable(boolean changeable) {
        super.setDataChangeable(changeable);
        updateRuntimeIdEnableState();
        if (changeable) {
            productCmptTypeControl.setButtonEnabled(true);
        } else {
            productCmptTypeControl.setButtonEnabled(IpsUIPlugin.isEditable(product.getIpsSrcFile()));
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
            /*
             * First set the runtime ID to "" so that in fact nothing happens when the button is
             * clicked while the runtime ID equals the runtime ID that would be generated. If the
             * old value remains here, the naming strategy would find it and create a new name with
             * added 1.
             */
            product.setRuntimeId(IpsStringUtils.EMPTY);
            String generatedRuntimeId = namingStrategy.getUniqueRuntimeId(ipsProject, product.getName());
            product.setRuntimeId(generatedRuntimeId);
        }

    }

    public static class ComponentPropertiesPMO extends IpsObjectPartPmo {

        public static final String PROPERTY_VALID_FROM_ENABLED = "validFromEnabled"; //$NON-NLS-1$

        public static final String PROPERTY_VALID_FROM = "validFrom"; //$NON-NLS-1$

        private IProductCmpt productCmpt;

        private boolean productCmptTypeChangingOverTime;

        public ComponentPropertiesPMO(IProductCmpt productCmpt) {
            super(productCmpt);
            this.productCmpt = productCmpt;
            productCmptTypeChangingOverTime = productCmpt.allowGenerations();
            mapValidationMessagesFor(new ObjectProperty(productCmpt, IIpsObjectGeneration.PROPERTY_VALID_FROM))
                    .to(new ObjectProperty(this, PROPERTY_VALID_FROM));
        }

        public GregorianCalendar getValidFrom() {
            return productCmpt.getValidFrom();
        }

        /**
         * Sets the validFrom date to the given date. It is expected that the valid from will be set
         * to a meaningful date, so this method simply doesn't accept the value if the user provides
         * null as input
         */
        public void setValidFrom(GregorianCalendar validFrom) {
            if (validFrom != null) {
                productCmpt.setValidFrom(validFrom);
            }
        }

        public boolean isValidFromEnabled() {
            return !productCmptTypeChangingOverTime;
        }

    }

    /**
     * Finds an IpsElement to be opened in an editor. Intended to be used in hyperlinks for
     * navigating to the model. Introduced for lack of a Java 8 Producer&lt;IIpsObject&gt;
     * 
     * .
     */
    private interface IpsObjectFinder {
        /**
         * Finds an IpsObject to open.
         */
        public IIpsObject findIpsObject();

    }

}
