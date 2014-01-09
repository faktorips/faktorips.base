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
