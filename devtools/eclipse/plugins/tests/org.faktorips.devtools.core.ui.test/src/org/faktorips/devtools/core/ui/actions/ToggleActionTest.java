/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.databinding.observable.value.WritableValue;
import org.junit.Test;

public class ToggleActionTest {

    private static final String WRONG_OBSERVABLE_STATE = "Wrong observable state";

    private static final String WRONG_ACTION_CHECKED_STATE = "Wrong action checked state";

    /**
     * Tests whether the action works as expected when starting with <code>true</code> or
     * <code>false</code>.
     */
    @Test
    public void testActionStartingWithTrue() {
        checkAction(true);
        checkAction(false);
    }

    private void checkAction(final boolean startValue) {
        WritableValue<Boolean> observable = new WritableValue<>(startValue, Boolean.class);
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
    @Test
    public void testActionStateUpdateFromModel() {
        WritableValue<Boolean> observable = new WritableValue<>(true, Boolean.class);
        ToggleAction action = new ToggleAction("description", "icon", observable);

        assertTrue(WRONG_ACTION_CHECKED_STATE, action.isChecked());
        observable.setValue(false);
        assertFalse(WRONG_ACTION_CHECKED_STATE, action.isChecked());
    }
}
