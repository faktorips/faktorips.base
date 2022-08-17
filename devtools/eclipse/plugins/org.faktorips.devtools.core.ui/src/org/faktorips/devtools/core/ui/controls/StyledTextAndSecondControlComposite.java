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
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.UIToolkit;

public abstract class StyledTextAndSecondControlComposite extends InputAndSecondControlComposite<StyledText> {

    public StyledTextAndSecondControlComposite(Composite parent, UIToolkit toolkit, boolean smallMargins,
            int buttonHeightHint, int style) {
        super(parent, toolkit, smallMargins, buttonHeightHint, style);
    }

    @Override
    protected StyledText createTextControl(UIToolkit toolkit, int style) {
        StyledText styledText;
        if (toolkit.getFormToolkit() == null) {
            styledText = toolkit.createStyledTextAppendStyle(this, SWT.SINGLE | style);
        } else {
            styledText = toolkit.createStyledText(getInnerComposite(), style);
        }
        return styledText;
    }

    @Override
    protected abstract void addListeners();

    @Override
    protected abstract Control createSecondControl(UIToolkit toolkit);

    @Override
    public void setTextInternal(String newText) {
        StyledText styledText = getTextControl();
        styledText.setText(newText);
        setTextControl(styledText);
    }

    @Override
    public String getText() {
        return getTextControl().getText();
    }
}
