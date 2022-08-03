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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.UIToolkit;

public abstract class TextAndSecondControlComposite extends InputAndSecondControlComposite<Text> {

    public TextAndSecondControlComposite(Composite parent, UIToolkit toolkit, boolean smallMargins,
            int buttonHeightHint, int style) {
        super(parent, toolkit, smallMargins, buttonHeightHint, style);
    }

    @Override
    protected Text createTextControl(UIToolkit toolkit, int style) {
        Text text;
        if (toolkit.getFormToolkit() == null) {
            text = toolkit.createTextAppendStyle(this, SWT.SINGLE | style);
        } else {
            text = toolkit.createText(getInnerComposite(), style);
        }
        return text;
    }

    @Override
    protected abstract void addListeners();

    @Override
    protected abstract Control createSecondControl(UIToolkit toolkit);

    @Override
    protected void setTextInternal(String newText) {
        Text text = getTextControl();
        text.setText(newText);
        setTextControl(text);
    }

    @Override
    public String getText() {
        return getTextControl().getText();
    }
}
