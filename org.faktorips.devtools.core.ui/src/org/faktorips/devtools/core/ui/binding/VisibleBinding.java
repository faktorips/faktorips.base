/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

/*
 * Copyright der Gesellschaften der KarstadtQuelle Versicherungen Projekt: Dateiname:
 * EnableBinding.java Erzeugt: 07.03.2007
 * 
 * Beschreibung:
 * 
 * 
 * *********************************************************************** Modification History:
 * 
 * ***********************************************************************
 */
package org.faktorips.devtools.core.ui.binding;

import org.eclipse.swt.widgets.Control;

/**
 * Binding between the visible property of a SWT control and a boolean property of an abitrary
 * object, usually a domain model object or presentation model object.
 * 
 * @author Jan Ortmann
 */
public class VisibleBinding extends ControlPropertyBinding {

    public VisibleBinding(Control control, Object object, String propertyName) {
        super(control, object, propertyName, Boolean.TYPE);
    }

    @Override
    public void updateUiIfNotDisposed() {
        try {
            Boolean value = (Boolean)getProperty().getReadMethod().invoke(getObject(), new Object[0]);
            getControl().setVisible(value.booleanValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
