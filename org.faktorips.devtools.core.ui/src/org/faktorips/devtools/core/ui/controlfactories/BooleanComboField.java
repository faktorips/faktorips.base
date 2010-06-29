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

package org.faktorips.devtools.core.ui.controlfactories;

import org.eclipse.swt.widgets.Combo;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;
import org.faktorips.util.ArgumentCheck;

/**
 * Field to edit values of type boolean (=instances of the BooleanDatatype). E.g. this field can be
 * used to edit an attributes default value if the attributes datatype is boolean. This is NOT a
 * general purpose field for booleans that are part the faktor ips meta model.
 * 
 * @author Joerg Ortmann
 */
public class BooleanComboField extends ComboField {

    private String trueRepresentation;
    private String falseRepresentation;

    public BooleanComboField(Combo combo, String trueRepresentation, String falseRepresentation) {
        super(combo);
        ArgumentCheck.notNull(trueRepresentation);
        ArgumentCheck.notNull(falseRepresentation);
        this.trueRepresentation = trueRepresentation;
        this.falseRepresentation = falseRepresentation;
    }

    @Override
    public Object parseContent() {
        String s = (String)super.parseContent();
        if (s == null) {
            return null;
        } else if (s.equals(trueRepresentation)) {
            return Boolean.TRUE.toString();
        } else if (s.equals(falseRepresentation)) {
            return Boolean.FALSE.toString();
        }
        throw new RuntimeException("Unknown value " + s); //$NON-NLS-1$
    }

    @Override
    public void setText(String newValue) {
        if (super.prepareObjectForGet(newValue) == null) {
            super.setText(newValue);
        } else if (newValue.equals(trueRepresentation) || newValue.equals(Boolean.TRUE.toString())) {
            super.setText(trueRepresentation);
        } else {
            super.setText(falseRepresentation);
        }
    }
}
