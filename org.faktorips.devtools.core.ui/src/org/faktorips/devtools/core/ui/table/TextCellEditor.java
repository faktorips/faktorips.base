/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.table;

import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * 
 * @author Stefan Widmaier
 */
public class TextCellEditor extends IpsCellEditor {

    private Text textControl;

    public TextCellEditor(Text textControl) {
        super(textControl);
        this.textControl = textControl;
    }

    @Override
    protected Object doGetValue() {
        String returnValue = textControl.getText();
        if (IpsPlugin.getDefault().getIpsPreferences().getNullPresentation().equals(returnValue)) {
            return null;
        }
        return returnValue;
    }

    @Override
    protected void doSetFocus() {
        textControl.selectAll();
        textControl.setFocus();
    }

    @Override
    protected void doSetValue(Object value) {
        if (null == value) {
            textControl.setText((IpsPlugin.getDefault().getIpsPreferences().getNullPresentation()));
            return;
        }
        if (value instanceof String) {
            textControl.setText((String)value);
        }
    }

    protected String getText() {
        return textControl.getText();
    }

    protected void setText(String newText) {
        if (newText == null) {
            textControl.setText(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());
        }
        textControl.setText(newText);
    }

    @Override
    public boolean isMappedValue() {
        return false;
    }
}
