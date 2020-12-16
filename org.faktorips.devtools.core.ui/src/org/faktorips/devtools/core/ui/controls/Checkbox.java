/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.ButtonField;

public class Checkbox extends AbstractCheckbox {

    public Checkbox(Composite parent, UIToolkit toolkit) {
        super(parent, toolkit, SWT.CHECK);
    }

    /**
     * Use {@link Button} and {@link ButtonField} instead. {@link ButtonField} also allows inverting
     * the checked state of a check box.
     * 
     * @deprecated as of FIPS 3.11
     */
    @Deprecated
    public Checkbox(Composite parent, UIToolkit toolkit, boolean invertValue) {
        super(parent, toolkit, SWT.CHECK, invertValue);
    }

}
