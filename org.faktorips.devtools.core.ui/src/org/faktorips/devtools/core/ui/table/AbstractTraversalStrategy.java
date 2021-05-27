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

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.TraverseEvent;

/**
 * Empty implementation of all methods.
 * 
 * @author widmaier
 */
public abstract class AbstractTraversalStrategy implements TraversalStrategy {

    private IpsCellEditor cellEditor;

    protected AbstractTraversalStrategy(IpsCellEditor cellEditor) {
        this.cellEditor = cellEditor;
    }

    public IpsCellEditor getCellEditor() {
        return cellEditor;
    }

    @Override
    public void keyTraversed(TraverseEvent e) {
        // Nothing to do
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Nothing to do
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Nothing to do
    }

    @Override
    public void focusGained(FocusEvent e) {
        // Nothing to do
    }

    @Override
    public void focusLost(FocusEvent e) {
        // Nothing to do
    }

    protected void fireApplyEditorValue() {
        getCellEditor().fireApplyEditorValue();
    }

}
