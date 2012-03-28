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

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.dialogs.MultiValueDialog;

public class MultiValueAttributeControl extends TextButtonControl {

    private IAttributeValue attributeValue;

    public MultiValueAttributeControl(Composite parent, UIToolkit toolkit, IAttributeValue attributeValue) {
        super(parent, toolkit, ""); //$NON-NLS-1$
        setButtonImage(IpsUIPlugin.getImageHandling().getSharedImage("MultiValueAttribute.gif", true)); //$NON-NLS-1$
        this.attributeValue = attributeValue;
    }

    @Override
    protected void buttonClicked() {
        MultiValueDialog multiValueDialog = new MultiValueDialog(getParent().getShell(), attributeValue);
        if (multiValueDialog.open() == Dialog.OK) {
            List<SingleValueHolder> values = multiValueDialog.getValues();
            attributeValue.setValueHolder(new MultiValueHolder(attributeValue, values));
        }
    }
}
