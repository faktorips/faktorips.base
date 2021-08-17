/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.preferencepages;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * This is a basic implementation for a simple composite that needs to implement
 * {@link IDataChangeableReadWriteAccess}. This class simply extends Composite and handles the
 * changeable state. The {@link #setDataChangeable(boolean)} method simply delegate to
 * {@link UIToolkit#setDataChangeable(Control, boolean)} for every child. However to implement
 * special data changeable behavior simply override {@link #setDataChangeable(boolean)} (and don't
 * forget to call super implementation or at least {@link #setChangeableProeprty(boolean)}!
 * 
 */
public abstract class DataChangeableComposite extends Composite implements IDataChangeableReadWriteAccess {

    private boolean changeable;

    public DataChangeableComposite(Composite parent, int style) {
        super(parent, style);
    }

    @Override
    public boolean isDataChangeable() {
        return changeable;
    }

    @Override
    public void setDataChangeable(boolean changeable) {
        setChangeableProeprty(changeable);
        Control[] children = getChildren();
        for (Control element : children) {
            new UIToolkit(null).setDataChangeable(element, changeable);
        }
    }

    protected void setChangeableProeprty(boolean changeable) {
        this.changeable = changeable;
    }

}
