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

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.CompositeUIController;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.util.message.ObjectProperty;

/**
 * Abstract base class for composites that allow to edit property values.
 * <p>
 * The default layout of the composite will be a grid layout with 1 column and a
 * <em>margin-width</em> of 1, as well as a <em>margin-height</em> of 2. The parent cell will be
 * filled horizontally by the composite. To change these settings, subclasses are allowed to
 * override {@link #setLayout()} and {@link #setLayoutData()}.
 * <p>
 * The methods {@link #getFirstControlHeight()} and {@link #getFirstControlMarginHeight()} are
 * intended to enable clients of this class to change the position of other UI elements. For
 * example, if a composite of this kind is used in a 2-column layout where the left column features
 * a label of the property value to be edited, it might be necessary to change the vertical position
 * of the label. The height of the first control is computed automatically, subclasses must override
 * {@link #getFirstControlMarginHeight()} if the first control they create features a
 * <em>margin-height</em> other than 0.
 * <p>
 * Finally, the method {@link #createEditFields(Map)} must be implemented to create the edit fields
 * of the composite. Each edit field must be registered to the provided map, associating an
 * {@link ObjectProperty} to each edit field.
 * <p>
 * Subclasses must invoke {@link #initControls()} right after all subclass-specific attributes have
 * been initialized.
 * 
 * @author Alexander Weickmann
 * 
 * @see IPropertyValue
 * @see EditField
 */
public abstract class EditPropertyValueComposite<P extends IProductCmptProperty, V extends IPropertyValue> extends
        Composite {

    private final P property;

    private final V propertyValue;

    private final IpsObjectUIController controller;

    private final UIToolkit toolkit;

    private final ProductCmptPropertySection propertySection;

    /**
     * The height of the first control contained in this composite.
     * <p>
     * The value -1 indicates that the height has not yet been computed or that this composite does
     * not contain any edit fields.
     */
    private int firstControlHeight = -1;

    protected EditPropertyValueComposite(P property, V propertyValue, ProductCmptPropertySection propertySection,
            Composite parent, CompositeUIController uiMasterController, UIToolkit toolkit) {

        super(parent, SWT.NONE);

        this.property = property;
        this.propertyValue = propertyValue;
        this.propertySection = propertySection;
        this.toolkit = toolkit;

        controller = new IpsObjectUIController(propertyValue);
        uiMasterController.add(controller);
    }

    protected final ProductCmptPropertySection getProductCmptPropertySection() {
        return propertySection;
    }

    protected final int getFirstControlHeight() {
        return firstControlHeight;
    }

    protected final P getProperty() {
        return property;
    }

    protected final V getPropertyValue() {
        return propertyValue;
    }

    protected final IpsObjectUIController getController() {
        return controller;
    }

    protected final UIToolkit getToolkit() {
        return toolkit;
    }

    /**
     * Returns the margin-height of the first control contained in this composite.
     * <p>
     * Subclasses should override this method if the first control they create features a
     * margin-height other than 0.
     * <p>
     * The default implementation always returns 0.
     */
    protected int getFirstControlMarginHeight() {
        return 0;
    }

    /**
     * Creates this composite and must be called by subclasses directly after subclass-specific
     * attributes have been initialized by the subclass constructor.
     * <p>
     * This implementation first calls {@link #setLayout()} and {@link #setLayoutData()}. Then,
     * {@link #createEditFields(Map)} is invoked.
     */
    protected final void initControls() {
        setLayout();
        setLayoutData();

        Map<EditField<?>, ObjectProperty> editFieldsToObjectProperties = new LinkedHashMap<EditField<?>, ObjectProperty>();
        try {
            createEditFields(editFieldsToObjectProperties);
        } catch (CoreException e) {
            // Log exception and do not add any edit fields
            IpsPlugin.log(e);
        }
        for (EditField<?> editField : editFieldsToObjectProperties.keySet()) {
            if (firstControlHeight == -1) {
                firstControlHeight = editField.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
            }
            ObjectProperty objectProperty = editFieldsToObjectProperties.get(editField);
            controller.add(editField, objectProperty.getObject(), objectProperty.getProperty());
            propertySection.addFocusControl(editField.getControl());
        }

        getToolkit().getFormToolkit().paintBordersFor(this);
    }

    /**
     * Creates and sets the layout of this composite.
     * <p>
     * Subclasses are allowed to override this method to set a specific layout.
     * <p>
     * The default implementation creates a grid layout with 1 column, a <em>margin-width</em> of 1
     * as well as a <em>margin-height</em> of 2. Then,
     * {@link #setLayout(org.eclipse.swt.widgets.Layout)} is invoked.
     */
    protected void setLayout() {
        GridLayout clientLayout = new GridLayout(1, false);
        clientLayout.marginWidth = 1;
        clientLayout.marginHeight = 2;
        setLayout(clientLayout);
    }

    /**
     * Creates and sets the layout data of this composite.
     * <p>
     * Subclasses are allowed to override this method to provide specific layout data.
     * <p>
     * The default implementation creates a {@link GridData} object with the flag
     * {@link GridData#FILL_HORIZONTAL} and invokes {@link #setLayoutData(Object)}.
     */
    protected void setLayoutData() {
        setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    /**
     * Subclasses must implement this operation to create the edit fields that constitute this edit
     * composite.
     * <p>
     * Every edit field must be added as key to the provided map with the associated value
     * identifying the edited object and property.
     * <p>
     * Each edit field will be automatically added to the controller of this composite. Furthermore,
     * each edit field control will be automatically registered as focus control to the parent
     * section.
     * 
     * @param editFieldsToObjectProperties Map to associate each created edit field with the object
     *            and the property it edits
     * 
     * @throws CoreException May throw this kind of exception at any time
     */
    protected abstract void createEditFields(Map<EditField<?>, ObjectProperty> editFieldsToObjectProperties)
            throws CoreException;

}
