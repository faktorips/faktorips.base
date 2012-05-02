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

import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.dialogs.MultiValueDialog;

/**
 * {@link TextButtonControl} for multi-value attributes. The text control cannot be edited. The
 * button opens the {@link MultiValueDialog} to allow users to change the attribute's list of
 * values.
 * 
 * @author Stefan Widmaier
 */
public class MultiValueAttributeControl extends TextButtonControl {

    private MultiValueAttributeHandler handler;

    public MultiValueAttributeControl(Composite parent, UIToolkit toolkit,
            IProductCmptTypeAttribute productCmptTypeAttribute, IAttributeValue attributeValue, ValueDatatype datatype) {
        super(parent, toolkit, ""); //$NON-NLS-1$
        handler = new MultiValueAttributeHandler(parent.getShell(), productCmptTypeAttribute, attributeValue, datatype);
        setButtonImage(IpsUIPlugin.getImageHandling().getSharedImage("MultiValueAttribute.gif", true)); //$NON-NLS-1$
        getTextControl().setEditable(false);
    }

    @Override
    protected void buttonClicked() {
        handler.editValues();
    }

}
