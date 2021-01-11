/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.controls.AbstractDateTimeControl;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;

public class DateControlField<T> extends FormattingTextField<T> {

    private final AbstractDateTimeControl dateControl;

    public DateControlField(AbstractDateTimeControl dateControl, IInputFormat<T> format) {
        this(dateControl, format, true);
    }

    public DateControlField(AbstractDateTimeControl dateControl, IInputFormat<T> format, boolean formatOnFocusLost) {
        super(dateControl.getTextControl(), format, formatOnFocusLost);
        this.dateControl = dateControl;
    }

    @Override
    public Control getControl() {
        return dateControl;
    }

}
