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

package org.faktorips.devtools.core.ui.binding;

import java.beans.PropertyDescriptor;

import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.util.BeanUtil;

/**
 * Abstract base class for a binding between a property of a SWT control and a property of an
 * arbitrary object, usually a domain model object or presentation model object.
 * 
 */
public abstract class ControlPropertyBinding {

    private Control control;
    private Object object;

    private PropertyDescriptor property;

    public ControlPropertyBinding(Control control, Object object, String propertyName, Class<?> exptectedType) {
        super();
        this.control = control;
        this.object = object;
        property = BeanUtil.getPropertyDescriptor(object.getClass(), propertyName);
        if (exptectedType != null && !exptectedType.equals(property.getPropertyType())) {
            throw new IllegalArgumentException(
                    "Property " + propertyName + " of type " + object.getClass() + " is not of type " + exptectedType); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    public Control getControl() {
        return control;
    }

    public Object getObject() {
        return object;
    }

    public PropertyDescriptor getProperty() {
        return property;
    }

    public final void updateUI() {
        if (!control.isDisposed()) {
            updateUiIfNotDisposed();
        }
    }

    public abstract void updateUiIfNotDisposed();

    @Override
    public String toString() {
        return "Binding " + object.toString() + "#" + property.getName() + " to control " + control; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
