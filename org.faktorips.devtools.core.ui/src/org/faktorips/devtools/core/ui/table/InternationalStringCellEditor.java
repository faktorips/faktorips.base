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

package org.faktorips.devtools.core.ui.table;

import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.controls.InternationalStringControl;

/**
 * A cell editor using the {@link InternationalStringControl} to enter values in different
 * languages.
 */
public class InternationalStringCellEditor extends AbstractLocalizedStringCellEditor {

    public InternationalStringCellEditor(InternationalStringControl control) {
        super(control);
    }

    @Override
    public InternationalStringControl getControl() {
        return (InternationalStringControl)super.getControl();
    }

    @Override
    protected Text getTextControl() {
        return getControl().getTextControl();
    }

}
