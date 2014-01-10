/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object doGetValue() {
        String returnValue = textControl.getText();
        if (IpsPlugin.getDefault().getIpsPreferences().getNullPresentation().equals(returnValue)) {
            return null;
        }
        return returnValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSetFocus() {
        textControl.selectAll();
        textControl.setFocus();
    }

    /**
     * {@inheritDoc}
     */
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
            newText = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        }
        textControl.setText(newText);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMappedValue() {
        return false;
    }
}
