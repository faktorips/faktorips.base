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
        this.spanningControl = controlToSpan;
    }

    @Override
    public void updateUiIfNotDisposed(String nameOfChangedProperty) {
        try {
            Boolean value = (Boolean)getProperty().getReadMethod().invoke(getObject(), new Object[0]);
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
