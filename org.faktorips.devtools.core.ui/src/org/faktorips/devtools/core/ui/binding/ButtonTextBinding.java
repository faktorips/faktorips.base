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

import org.eclipse.swt.widgets.Button;
import org.faktorips.devtools.core.ui.controls.Checkbox;

/**
 * Binding between the text property of a button or checkbox and a property of an abitrary object,
 * usually a domain model object or presentation model object.
 * 
 * @see Button
 * @see Checkbox
 * 
 * @author Jan Ortmann
 */
public class ButtonTextBinding extends ControlPropertyBinding {

    public ButtonTextBinding(Checkbox checkbox, Object object, String propertyName) {
        super(checkbox, object, propertyName, String.class);
    }

    public ButtonTextBinding(Button button, Object object, String propertyName) {
        super(button, object, propertyName, String.class);
    }

    @Override
    public void updateUiIfNotDisposed(String nameOfChangedProperty) {
        try {
            String value = (String)getProperty().getReadMethod().invoke(getObject(), new Object[0]);
            if (getControl() instanceof Checkbox) {
                ((Checkbox)getControl()).setText(value);
            } else {
                ((Button)getControl()).setText(value);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
