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
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.value.IValue;
import org.faktorips.devtools.core.ui.dialogs.MultilingualValueDialog;

public class MultilingualValueAttributeHandler {
    private final IAttributeValue attributeValue;
    private final Shell shell;
    private final IIpsProject ipsProject;
    private final IValueHolder<IValue<?>> valueHolder;

    public MultilingualValueAttributeHandler(Shell shell, IAttributeValue attributeValue, IIpsProject ipsProject,
            IValueHolder<IValue<?>> valueHolder) {
        this.shell = shell;
        this.attributeValue = attributeValue;
        this.ipsProject = ipsProject;
        this.valueHolder = valueHolder;
    }

    public void editValues() {
        openInternationalValueDialog();
    }

    protected void openInternationalValueDialog() {
        MultilingualValueDialog internationalValueDialog = new MultilingualValueDialog(shell, attributeValue,
                ipsProject, valueHolder);
        internationalValueDialog.open();
        // values are applied in the dialog's okPressed() method
    }
}