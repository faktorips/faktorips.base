/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

    public void keyTraversed(TraverseEvent e) {
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    public void focusGained(FocusEvent e) {
    }

    public void focusLost(FocusEvent e) {
    }

    protected void fireApplyEditorValue() {
        getCellEditor().fireApplyEditorValue();
    }
}
