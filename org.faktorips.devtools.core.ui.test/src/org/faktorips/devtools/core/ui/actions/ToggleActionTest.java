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

package org.faktorips.devtools.core.ui.actions;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.value.WritableValue;

/**
 * Tests the class {@link ToggleAction}.
 */
@SuppressWarnings("nls")
public class ToggleActionTest extends TestCase {
    /**
     * Tests whether the action works as expected when starting with <code>true</code> or
     * <code>false</code>.
     */
    public void testActionStartingWithTrue() {
        checkAction(true);
        checkAction(false);
    }

    private void checkAction(final boolean startValue) {
        WritableValue observable = new WritableValue(startValue, Boolean.class);
        ToggleAction action = new ToggleAction("description", "icon", observable);

        assertEquals("Wrong action checked state", startValue, action.isChecked());
        assertEquals("Wrong observable state", startValue, observable.getValue());
        action.run();
        assertEquals("Wrong observable state", !startValue, observable.getValue());
        action.run();
        assertEquals("Wrong observable state", startValue, observable.getValue());
    }
}