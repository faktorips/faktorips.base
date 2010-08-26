/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

    private boolean enabledIfObjectPropertyIsTrue;

    public EnableBinding(Control control, Object object, String propertyName, boolean enabledIfTrue) {
        super(control, object, propertyName, Boolean.TYPE);
        this.enabledIfObjectPropertyIsTrue = enabledIfTrue;
    }

    @Override
    public void updateUiIfNotDisposed() {
        try {
            Boolean value = (Boolean)getProperty().getReadMethod().invoke(getObject(), new Object[0]);
            boolean enabled = value.booleanValue() == enabledIfObjectPropertyIsTrue;
            getControl().setEnabled(enabled);
        } catch (Exception e) {
            // TODO catch Exception needs to be documented properly or specialized
            throw new RuntimeException(e);
        }
    }

}
