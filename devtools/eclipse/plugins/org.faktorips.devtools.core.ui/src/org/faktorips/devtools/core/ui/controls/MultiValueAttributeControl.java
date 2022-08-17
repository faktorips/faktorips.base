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

import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.dialogs.MultiValueDialog;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;

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
            IProductCmptTypeAttribute productCmptTypeAttribute, IAttributeValue attributeValue,
            ValueDatatype datatype) {
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
