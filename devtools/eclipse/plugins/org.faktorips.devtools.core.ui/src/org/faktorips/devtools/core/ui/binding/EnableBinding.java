/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.binding;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Predicate;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Binding between the enable property of a SWT control and a boolean property of an arbitrary
 * object, usually a domain model object or presentation model object.
 * 
 * @author Jan Ortmann
 */
public class EnableBinding extends ControlPropertyBinding {

    private Predicate<Object> enabledFunction;

    private UIToolkit uiToolkit = new UIToolkit(null);

    public EnableBinding(Control control, Object object, String property, final Object expectedValue) {
        this(control, object, property, Predicate.isEqual(expectedValue));
    }

    public EnableBinding(Control control, Object object, String property, Predicate<Object> enabledFunction) {
        super(control, object, property, null);
        this.enabledFunction = enabledFunction;
    }

    /**
     * This method updates the enabled state of the control referenced by {@link #getControl()}. The
     * disabled state of a control should be overwritten and the data changeable state that may be
     * already set by {@link UIToolkit#setDataChangeable(Control, boolean)}. But the data changeable
     * state should not be over controlled by an enabled state. This behavior is important because
     * some controls have different behavior in read-only and disabled state. For example a
     * {@link Text} control is set by {@link Text#setEditable(boolean)} to read-only state but
     * {@link Text#setEnabled(boolean)} in disabled state. That leads to following logic:
     * <ul>
     * <li>Control is read-only, enabled-binding set enabled: control stays read-only</li>
     * <li>Control is read-write, enabled-binding set enabled: control stays enabled</li>
     * <li>Control is read-only, enabled-binding set disabled: control set disabled</li>
     * <li>Control is read-write, enabled-binding set disabled: control set disabled</li>
     * </ul>
     */
    @Override
    public void updateUiIfNotDisposed(String nameOfChangedProperty) {
        try {
            Object value = getProperty().getReadMethod().invoke(getObject());
            boolean enabled = value != null && enabledFunction.test(value);
            if (isDataChangeable() || !enabled) {
                uiToolkit.setEnabled(getControl(), enabled);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isDataChangeable() {
        return uiToolkit.isDataChangeable(getControl());
    }

}
