/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.swt.widgets.Combo;
import org.faktorips.util.ArgumentCheck;

/**
 * Field to edit values of type boolean (=instances of the BooleanDatatype). E.g. this field can be
 * used to edit an attributes default value if the attributes datatype is boolean. This is NOT a
 * general purpose field for booleans that are part the faktor ips meta model.
 * 
 * @author Joerg Ortmann
 */
public class BooleanComboField extends StringValueComboField {

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
    public String parseContent() {
        String s = super.parseContent();
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
