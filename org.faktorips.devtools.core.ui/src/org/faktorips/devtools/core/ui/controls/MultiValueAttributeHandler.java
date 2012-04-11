/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.dialogs.MultiValueDialog;

public class MultiValueAttributeHandler {
    private final IAttributeValue attributeValue;
    private final IValueSet valueSet;
    private final Shell shell;

    public MultiValueAttributeHandler(Shell shell, IAttributeValue attributeValue, IValueSet valueSet) {
        this.shell = shell;
        this.attributeValue = attributeValue;
        this.valueSet = valueSet;
    }

    private boolean isEnumValueSet() {
        return valueSet instanceof IEnumValueSet;
    }

    public void editValues() {
        // if (isEnumValueSet()) {
        // // TODO subsetChooser
        // } else {
        MultiValueDialog multiValueDialog = new MultiValueDialog(shell, attributeValue);
        multiValueDialog.open();
        // values are applied in the dialog's okPressed() method
        // }
    }
}