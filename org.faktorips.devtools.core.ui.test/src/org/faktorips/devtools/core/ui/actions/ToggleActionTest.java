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
    private static final String WRONG_OBSERVABLE_STATE = "Wrong observable state";
    private static final String WRONG_ACTION_CHECKED_STATE = "Wrong action checked state";

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

        assertEquals(WRONG_ACTION_CHECKED_STATE, startValue, action.isChecked());
        assertEquals(WRONG_OBSERVABLE_STATE, startValue, observable.getValue());
        action.run();
        assertEquals(WRONG_OBSERVABLE_STATE, !startValue, observable.getValue());
        action.run();
        assertEquals(WRONG_OBSERVABLE_STATE, startValue, observable.getValue());
    }

    /**
     * Verifies that the action toggles its state if the model changes.
     */
    public void testActionStateUpdateFromModel() {
        WritableValue observable = new WritableValue(true, Boolean.class);
        ToggleAction action = new ToggleAction("description", "icon", observable);

        assertTrue(WRONG_ACTION_CHECKED_STATE, action.isChecked());
        observable.setValue(false);
        assertFalse(WRONG_ACTION_CHECKED_STATE, action.isChecked());
    }
}