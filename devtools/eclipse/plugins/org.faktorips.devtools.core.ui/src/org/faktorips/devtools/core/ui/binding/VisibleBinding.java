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

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Binding between the visible property of a SWT control and a boolean property of an abitrary
 * object, usually a domain model object or presentation model object.
 * 
 * @author Jan Ortmann
 */
public class VisibleBinding extends ControlPropertyBinding {

    private final boolean excludeWhenInvisible;
    private final Control spanningControl;

    /**
     * Setting the control's visible state depending on the property's values
     * 
     * @param excludeWhenInvisible setting the {@link GridData#exclude} so this control would be
     *            ignored in the layout if it is invisible.
     * @param controlToSpan An optional control which {@link GridData#horizontalSpan} is increased
     *            when the control is invisible
     */
    public VisibleBinding(Control control, Object object, String propertyName, boolean excludeWhenInvisible,
            Control controlToSpan) {
        super(control, object, propertyName, Boolean.TYPE);
        this.excludeWhenInvisible = excludeWhenInvisible;
        spanningControl = controlToSpan;
    }

    @Override
    public void updateUiIfNotDisposed(String nameOfChangedProperty) {
        try {
            Boolean value = (Boolean)getProperty().getReadMethod().invoke(getObject());
            getControl().setVisible(value.booleanValue());
            Object layoutData = getControl().getLayoutData();
            if (excludeWhenInvisible && layoutData instanceof GridData) {
                GridData gridData = (GridData)layoutData;
                gridData.exclude = !value;
            }
            if (spanningControl != null && !spanningControl.isDisposed()
                    && spanningControl.getLayoutData() instanceof GridData) {
                GridData gridDate = (GridData)spanningControl.getLayoutData();
                if (value) {
                    gridDate.horizontalSpan--;
                } else {
                    gridDate.horizontalSpan++;
                }
            }
            Composite parent = getControl().getParent();
            if (parent != null && !parent.isDisposed()) {
                parent.layout();

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
