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
import java.util.List;
import java.util.function.Function;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.core.ui.views.producttemplate.ShowTemplatePropertyUsageViewAction;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.devtools.model.util.TemplatedValueUtil;

/**
 * Abstract base class for composites that allow the user to edit property values.
 * <p>
 * <strong>Subclassing:</strong><br>
 * The default layout of the composite is a grid layout with 1 column and a <em>margin-width</em> of
 * 1, as well as a <em>margin-height</em> of 2. The parent cell is filled horizontally by the
 * composite. To change these settings, subclasses are allowed to override {@link #setLayout()} and
 * {@link #setLayoutData()}.
 * <p>
 * The method {@link #getFirstControlHeight()} is intended to enable clients of this class to change
 * the position of other UI elements. For example, if a composite of this kind is used in a 2-column
 * layout where the left column features a label representing the property value's caption, it might
 * be necessary to change the vertical position of the label. The height of the first control is
 * computed automatically, subclasses may override this method.
 * <p>
 * Finally, the method {@link #createEditFields(List)} must be implemented to create the edit fields
 * of the composite.
 * <p>
 * Subclasses must invoke {@link #initControls()} in the subclass constructor (usually this should
 * be the last invocation of the subclass constructor).
 * 
 * @since 3.6
 * 
 * @see IPropertyValue
 * @see EditField
 */
public abstract class EditPropertyValueComposite<P extends IProductCmptProperty, V extends IPropertyValue>
        extends Composite {

    private final P property;

    private final V propertyValue;

    private final BindingContext bindingContext;

    private final UIToolkit toolkit;

    private final IpsSection parentSection;

    private final List<EditField<?>> editFields = new ArrayList<>();

    private final ExtensionPropertyControlFactory extPropControlFactory;

    protected EditPropertyValueComposite(P property, V propertyValue, IpsSection parentSection, Composite parent,
            BindingContext bindingContext, UIToolkit toolkit) {

        super(parent, SWT.NONE);

        this.property = property;
        this.propertyValue = propertyValue;
        this.parentSection = parentSection;
        this.bindingContext = bindingContext;
        this.toolkit = toolkit;
        extPropControlFactory = new ExtensionPropertyControlFactory(propertyValue);
    }

    /**
     * Returns the parent {@link IpsSection} this {@link EditPropertyValueComposite} belongs to.
     */
    protected final IpsSection getProductCmptPropertySection() {
        return parentSection;
    }

    /**
     * Returns the height of the first control contained within this composite.
     * <p>
     * The value -1 indicates that the height has not yet been computed or that this composite does
     * not contain any edit fields.
     */
    protected final int getFirstControlHeight() {
        if (editFields.isEmpty()) {
            return -1;
        } else {
            EditField<?> firstEditField = editFields.get(0);
            return firstEditField.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        }
    }

    /**
     * Returns the {@link IProductCmptProperty} the {@link IPropertyValue} to be edited corresponds
     * to.
     */
    protected final P getProperty() {
        return property;
    }

    /**
     * Returns the {@link IPropertyValue} to be edited by this {@link EditPropertyValueComposite}.
     */
    protected final V getPropertyValue() {
        return propertyValue;
    }

    /**
     * Returns the {@link BindingContext} used to bind UI controls to the underlying model.
     */
    protected final BindingContext getBindingContext() {
        return bindingContext;
    }

    /**
     * Returns the {@link UIToolkit} used to create UI controls.
     */
    protected final UIToolkit getToolkit() {
        return toolkit;
    }

    /**
     * Creates this composite and must be called by subclasses directly after subclass-specific
     * attributes have been initialized by the subclass constructor.
     * <p>
     * <strong>Subclassing:</strong><br>
     * This implementation first calls {@link #setLayout()} and {@link #setLayoutData()}. Then,
     * {@link #createEditFields(List)} is invoked.
     */
    protected final void initControls() {
        setLayout();
        updateLayoutForTemplateButton();
        setLayoutData();

        try {
            createEditFields(editFields);
        } catch (IpsException e) {
            // Log exception and do not add any edit fields
            IpsPlugin.log(e);
        }

        getToolkit().getFormToolkit().paintBordersFor(this);
    }

    /**
     * Creates and sets the {@link Layout} of this composite.
     * <p>
     * <strong>Subclassing:</strong><br>
     * The default implementation creates a grid layout with 1 column, a <em>margin-width</em> of 0,
     * a <em>margin-height</em> of 0 and a <em>horizontal-spacing</em> of 0, to save some space.
     * Grants error markers enough space by setting marginLeft to 8. Then,
     * {@link #setLayout(Layout)} is invoked. Subclasses are allowed to override this method if the
     * default implementation is inappropriate.
     */
    protected void setLayout() {
        GridLayout clientLayout = new GridLayout(1, false);
        clientLayout.marginWidth = 0;
        clientLayout.marginHeight = 0;
        // needed to grant problem markers enough space
        clientLayout.marginLeft = 8;
        clientLayout.horizontalSpacing = 0;
        setLayout(clientLayout);
    }

    protected void updateLayoutForTemplateButton() {
        if (showTemplateButton()) {
            ((GridLayout)getLayout()).numColumns++;
        }
    }

    protected boolean showTemplateButton() {
        return getPropertyValue().isPartOfTemplateHierarchy();
    }

    /**
     * Creates and sets the layout data of this composite.
     * <p>
     * <strong>Subclassing:</strong><br>
     * The default implementation creates a {@link GridData} object with the flag
     * {@link GridData#FILL_HORIZONTAL} and invokes {@link #setLayoutData(Object)}. Subclasses are
     * allowed to override this method if the default implementation is inappropriate.
     */
    protected void setLayoutData() {
        setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    /**
     * Creates the edit fields that constitute this {@link EditPropertyValueComposite}.
     * <p>
     * <strong>Subclassing:</strong><br>
     * Subclasses must create the edit fields that constitute this composite and bind them to the
     * {@link BindingContext} obtained via {@link #getBindingContext()}. Furthermore, each created
     * {@link EditField} must be added to the provided list.
     * 
     * @param editFields the {@link List} to which each created {@link EditField} should be added to
     * 
     * @throws IpsException if an error occurs while creating the edit fields
     */
    protected abstract void createEditFields(List<EditField<?>> editFields) throws IpsException;

    /**
     * Adds a "S"-decoration to the editcomposite's field if the propertyValue is <em>not</em>
     * changing over time.
     * 
     * @param editField the edit field whose control should be decorated with an "S".
     */
    protected void addChangingOverTimeDecorationIfRequired(EditField<?> editField) {
        addChangingOverTimeDecorationIfRequired(editField, 0);
    }

    /**
     * Adds a "S"-decoration to the editcomposite's field if the propertyValue is <em>not</em>
     * changing over time. The decoration moves 7 pixels to the left, if the field has focus.
     * <p>
     * Use this method for edit composites that provide context proposals. On gaining focus, the "S"
     * decoration is moved to the left to prevent overlay with the context proposal icon.
     * 
     * @see #addChangingOverTimeDecorationIfRequired(EditField)
     * @param editField the edit field whose control should be decorated with an "S".
     */
    protected void addMovingChangingOverTimeDecorationIfRequired(EditField<?> editField) {
        addChangingOverTimeDecorationIfRequired(editField, 7);
    }

    private void addChangingOverTimeDecorationIfRequired(EditField<?> editField, int pixelsToLeftUponControlFocus) {
        if (isProductCmptTypeChangingOverTime() && propertyIsNotChangingOverTime()) {
            addChangingOverTimeDecoration(editField, pixelsToLeftUponControlFocus);
        }
    }

    private boolean isProductCmptTypeChangingOverTime() {
        IProductCmptType productCmptType = findProductCmptType();
        return productCmptType != null && productCmptType.isChangingOverTime();
    }

    private IProductCmptType findProductCmptType() {
        IProductCmptType productCmptType = null;
        if (getProperty() != null) {
            productCmptType = getProperty().findProductCmptType(propertyValue.getIpsProject());
        }
        return productCmptType;
    }

    private boolean propertyIsNotChangingOverTime() {
        return getProperty() != null && !getProperty().isChangingOverTime();
    }

    private void addChangingOverTimeDecoration(EditField<?> editField, int pixelsToLeftUponControlFocus) {
        final ControlDecoration controlDecoration = new ControlDecoration(editField.getControl(), SWT.LEFT | SWT.TOP,
                this.getParent());
        controlDecoration.setDescriptionText(NLS.bind(
                Messages.AttributeValueEditComposite_attributeNotChangingOverTimeDescription, IpsPlugin.getDefault()
                        .getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNamePlural()));
        controlDecoration.setImage(IpsUIPlugin.getImageHandling().getImage(OverlayIcons.NOT_CHANGEOVERTIME_OVR_DESC));
        controlDecoration.setMarginWidth(1);

        addFocusListenerIfRequired(editField, controlDecoration, pixelsToLeftUponControlFocus);
    }

    private void addFocusListenerIfRequired(EditField<?> editField,
            final ControlDecoration controlDecoration,
            int pixelsToLeftUponControlFocus) {
        if (pixelsToLeftUponControlFocus > 0) {
            editField.getControl()
                    .addFocusListener(new MoveDecorationFocusListener(controlDecoration, pixelsToLeftUponControlFocus));
        }
    }

    protected void createTemplateStatusButton(final EditField<?> editField) {
        if (showTemplateButton()) {
            final TemplateValuePmo<V> pmo = new TemplateValuePmo<>(getPropertyValue(), getToolTipFormatter());
            final ToolBar toolBar = new ToolBar(this, SWT.FLAT);
            ToolItem toolItem = TemplateValueUiUtil.setUpStatusToolItem(toolBar, bindingContext, pmo);

            focusOnTemplateStatusClick(editField.getControl(), toolItem);
            bindTemplateDependentEnabled(editField.getControl());
            bindProblemMarker(editField);
            toolBar.setMenu(new TemplateToolBarMenuBuilder(toolBar).createTemplateMenue());
        }
    }

    protected abstract Function<V, String> getToolTipFormatter();

    private void focusOnTemplateStatusClick(final Control control, final ToolItem toolItem) {
        toolItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (getToolkit().isEnabled(control) && getToolkit().isDataChangeable(control)) {
                    control.setFocus();
                }
            }
        });
    }

    private IIpsProject getIpsProject() {
        return getPropertyValue().getIpsProject();
    }

    private void bindTemplateDependentEnabled(Control control) {
        Control controlToEnable = control;
        if (control.getParent() != this) {
            controlToEnable = control.getParent();
        }
        getBindingContext().bindEnabled(controlToEnable, getPropertyValue(),
                IPropertyValue.PROPERTY_TEMPLATE_VALUE_STATUS, TemplateValueStatus.DEFINED);
    }

    private void bindProblemMarker(EditField<?> editField) {
        getBindingContext().bindProblemMarker(editField, getPropertyValue(),
                IPropertyValue.PROPERTY_TEMPLATE_VALUE_STATUS);
    }

    protected void createEditFieldsForExtensionProperties() {
        extPropControlFactory.createControls(this, getToolkit(), getPropertyValue(),
                IExtensionPropertyDefinition.POSITION_TOP);
        extPropControlFactory.createControls(this, getToolkit(), getPropertyValue(),
                IExtensionPropertyDefinition.POSITION_BOTTOM);
        extPropControlFactory.bind(getBindingContext());
    }

    private static class MoveDecorationFocusListener implements FocusListener {

        private final ControlDecoration controlDecoration;
        private final int pixelsToLeftUponControlFocus;

        public MoveDecorationFocusListener(ControlDecoration controlDecoration, int pixelsToLeftUponControlFocus) {
            this.controlDecoration = controlDecoration;
            this.pixelsToLeftUponControlFocus = pixelsToLeftUponControlFocus;
        }

        @Override
        public void focusGained(FocusEvent e) {
            controlDecoration.setMarginWidth(pixelsToLeftUponControlFocus);
        }

        @Override
        public void focusLost(FocusEvent e) {
            controlDecoration.setMarginWidth(0);
        }

    }

    private class TemplateToolBarMenuBuilder extends AbstractTemplateToolBarMenuBuilder {

        public TemplateToolBarMenuBuilder(ToolBar toolBar) {
            super(toolBar);
        }

        @Override
        protected void addOpenTemplateAction(IMenuManager manager) {
            IPropertyValue templateValue = getPropertyValue().findTemplateProperty(getIpsProject());
            if (templateValue != null) {
                String text = getOpenTemplateText(templateValue);
                IAction openTemplateAction = new SimpleOpenIpsObjectPartAction<>(templateValue, text);
                manager.add(openTemplateAction);
            }

        }

        @Override
        protected void addShowTemplatePropertyUsageAction(IMenuManager manager) {
            String text = null;
            ITemplatedValue templateValue;
            if (TemplatedValueUtil.isTemplateValue(getPropertyValue())) {
                text = Messages.AttributeValueEditComposite_MenuItem_showPropertyUsage;
                templateValue = getPropertyValue();
            } else {
                templateValue = getPropertyValue().findTemplateProperty(getIpsProject());
                if (templateValue == null) {
                    templateValue = TemplatedValueUtil.findNextTemplateValue(getPropertyValue());
                }
                text = getOpenTemplatePropertyUsageText(templateValue);
            }
            if (templateValue != null) {
                manager.add(new ShowTemplatePropertyUsageViewAction(templateValue, text));
            }
        }

        private String getOpenTemplateText(final IPropertyValue templateValue) {
            return NLS.bind(Messages.AttributeValueEditComposite_MenuItem_openTemplate,
                    templateValue.getPropertyValueContainer().getProductCmpt().getName());
        }

        private String getOpenTemplatePropertyUsageText(final ITemplatedValue templateValue) {
            return NLS.bind(Messages.AttributeValueEditComposite_MenuItem_showTemplatePropertyUsage,
                    templateValue.getTemplatedValueContainer().getProductCmpt().getName());
        }

    }

}
