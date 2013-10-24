/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
