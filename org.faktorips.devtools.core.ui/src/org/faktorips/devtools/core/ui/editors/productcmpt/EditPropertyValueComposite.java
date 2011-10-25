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
 * TODO AW
 * 
 * @author Alexander Weickmann
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
     * Returns The margin of the first control contained in this composite.
     * <p>
     * Subclasses should override this method if the first control they create features a margin.
     * <p>
     * The default implementation always returns 0.
     */
    protected int getFirstControlMargin() {
        return 0;
    }

    /**
     * Creates this composite and must be called by subclasses directly after subclass-specific data
     * has been initialized by the subclass constructor.
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

    protected void setLayout() {
        GridLayout clientLayout = new GridLayout(1, false);
        clientLayout.marginWidth = 1;
        clientLayout.marginHeight = 2;
        setLayout(clientLayout);
    }

    protected void setLayoutData() {
        setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    /**
     * Subclasses must create the edit fields that constitute this edit composite.
     * <p>
     * Every edit field must be added as key to the provided map with the associated value being the
     * edited property.
     * 
     * @param editFieldsToObjectProperties Map to associate each created edit field with the object
     *            and the property it edits
     * 
     * @throws CoreException May throw this kind of exception at any time
     */
    protected abstract void createEditFields(Map<EditField<?>, ObjectProperty> editFieldsToObjectProperties)
            throws CoreException;

}
