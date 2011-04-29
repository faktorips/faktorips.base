/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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

    public IpsCellEditor getCellEditor() {
        return cellEditor;
    }

    protected AbstractTraversalStrategy(IpsCellEditor cellEditor) {
        this.cellEditor = cellEditor;
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
