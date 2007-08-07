/*
 * Copyright der Gesellschaften der KarstadtQuelle Versicherungen
 * Projekt:     
 * Dateiname:   EnableBinding.java
 * Erzeugt:     07.03.2007
 *
 * Beschreibung:
 *
 *
 ************************************************************************
 * Modification History:
 *
 ************************************************************************
 *
 */
package org.faktorips.devtools.core.ui.binding;

import org.eclipse.swt.widgets.Control;

/**
 * Binding between the visible property of a SWT control and a boolean
 * property of an abitrary object, usually a domain model object or presentation model
 * object.
 * 
 * @author Jan Ortmann
 */
public class VisibleBinding extends ControlPropertyBinding {

    
    public VisibleBinding(Control control, Object object, String propertyName) {
        super(control, object, propertyName, Boolean.TYPE);
    }
    
    public void updateUI() {
        try {
            Boolean value = (Boolean)getProperty().getReadMethod().invoke(getObject(), new Object[0]);
            getControl().setVisible(value.booleanValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
