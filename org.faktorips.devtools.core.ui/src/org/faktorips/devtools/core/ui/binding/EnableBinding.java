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

import org.eclipse.swt.widgets.Control;

/**
 * Binding between the enable property of a SWT control and a boolean property of an abitrary
 * object, usually a domain model object or presentation model object.
 * 
 * @author Jan Ortmann
 */
public class EnableBinding extends ControlPropertyBinding {

    private Object expectedValue;

    public EnableBinding(Control control, Object object, String property, Object expectedValue) {
        super(control, object, property, null);
        this.expectedValue = expectedValue;
    }

    @Override
    public void updateUiIfNotDisposed(String nameOfChangedProperty) {
        try {
            Object value = getProperty().getReadMethod().invoke(getObject(), new Object[0]);
            boolean enabled = value != null && value.equals(expectedValue);
            getControl().setEnabled(enabled);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
