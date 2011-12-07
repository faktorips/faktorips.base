/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.CellEditor.LayoutData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * Abstract base class for composites that allow the user to edit property values.
 * <p>
 * <strong>Subclassing:</strong><br>
 * The default layout of the composite is a grid layout with 1 column and a <em>margin-width</em> of
 * 1, as well as a <em>margin-height</em> of 2. The parent cell is filled horizontally by the
 * composite. To change these settings, subclasses are allowed to override {@link #setLayout()} and
 * {@link #setLayoutData()}.
 * <p>
 * The methods {@link #getFirstControlHeight()} and {@link #getFirstControlMarginHeight()} are
 * intended to enable clients of this class to change the position of other UI elements. For
 * example, if a composite of this kind is used in a 2-column layout where the left column features
 * a label representing the property value's caption, it might be necessary to change the vertical
 * position of the label. The height of the first control is computed automatically, subclasses must
 * override {@link #getFirstControlMarginHeight()} if the first control they create features a
 * <em>margin-height</em> other than 0.
 * <p>
 * Finally, the method {@link #createEditFields(List)} must be implemented to create the edit fields
 * of the composite.
 * <p>
 * Subclasses must invoke {@link #initControls()} in the subclass constructor (usually this should
 * be the last invocation of the subclass constructor).
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann, Faktor Zehn AG
 * 
 * @see IPropertyValue
 * @see EditField
 */
public abstract class EditPropertyValueComposite<P extends IProductCmptProperty, V extends IPropertyValue> extends
        Composite {

    private final P property;

    private final V propertyValue;

    private final BindingContext bindingContext;

    private final UIToolkit toolkit;

    private final IpsSection parentSection;

    private final List<EditField<?>> editFields = new ArrayList<EditField<?>>();

    protected EditPropertyValueComposite(P property, V propertyValue, IpsSection parentSection, Composite parent,
            BindingContext bindingContext, UIToolkit toolkit) {

        super(parent, SWT.NONE);

        this.property = property;
        this.propertyValue = propertyValue;
        this.parentSection = parentSection;
        this.bindingContext = bindingContext;
        this.toolkit = toolkit;
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
     * Returns the margin-height of the first control contained in this composite.
     * <p>
     * <strong>Subclassing:</strong><br>
     * The default implementation always returns 0. Subclasses should override this method if the
     * first control they create features a margin-height other than 0.
     */
    protected int getFirstControlMarginHeight() {
        return 0;
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
        setLayoutData();

        try {
            createEditFields(editFields);
        } catch (CoreException e) {
            // Log exception and do not add any edit fields
            IpsPlugin.log(e);
        }

        getToolkit().getFormToolkit().paintBordersFor(this);
    }

    /**
     * Creates and sets the {@link Layout} of this composite.
     * <p>
     * <strong>Subclassing:</strong><br>
     * The default implementation creates a grid layout with 1 column, a <em>margin-width</em> of 1
     * as well as a <em>margin-height</em> of 2 and a <em>horizontal-spacing</em> of 7. Then,
     * {@link #setLayout(Layout)} is invoked. Subclasses are allowed to override this method if the
     * default implementation is inappropriate.
     */
    protected void setLayout() {
        GridLayout clientLayout = new GridLayout(1, false);
        clientLayout.marginWidth = 1;
        clientLayout.marginHeight = 2;
        clientLayout.horizontalSpacing = 7;
        setLayout(clientLayout);
    }

    /**
     * Creates and sets the {@link LayoutData} of this composite.
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
     * @throws CoreException if an error occurs while creating the edit fields
     */
    protected abstract void createEditFields(List<EditField<?>> editFields) throws CoreException;

}
