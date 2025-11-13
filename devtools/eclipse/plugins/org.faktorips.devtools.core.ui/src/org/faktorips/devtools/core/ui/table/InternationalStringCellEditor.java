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
import org.faktorips.devtools.core.ui.controls.InternationalStringControl;
import org.faktorips.devtools.model.internal.InternationalString;

/**
 * A cell editor using the {@link InternationalStringControl} to enter values in different
 * languages.
 */
public class InternationalStringCellEditor extends AbstractLocalizedStringCellEditor {

    public InternationalStringCellEditor(InternationalStringControl control) {
        super(control);
    }

    @Override
    public InternationalStringControl getControl() {
        return (InternationalStringControl)super.getControl();
    }

    @Override
    protected Text getTextControl() {
        return getControl().getTextControl();
    }

    @Override
    protected void addStrategyAsListener(TraversalStrategy strategy) {
        if (strategy != null) {
            getTextControl().addKeyListener(strategy);
            getTextControl().addTraverseListener(strategy);
            getTextControl().addFocusListener(strategy);
        }
    }

    /**
     * Removes the given {@link TraversalStrategy} as listener from the text control.
     * Overrides the behavior of the superclass to register on the text control instead of the
     * composite control to allow proper "tab" use on {@link InternationalString} cells. 
     */
    private void removeStrategyAsListener(TraversalStrategy strategy) {
        if (strategy != null && !getTextControl().isDisposed()) {
            getTextControl().removeKeyListener(strategy);
            getTextControl().removeTraverseListener(strategy);
            getTextControl().removeFocusListener(strategy);
        }
    }

    @Override
    public void setTraversalStrategy(TraversalStrategy strategy) {
        removeStrategyAsListener(getTraversalStrategy());
        super.setTraversalStrategy(strategy);
    }
}
