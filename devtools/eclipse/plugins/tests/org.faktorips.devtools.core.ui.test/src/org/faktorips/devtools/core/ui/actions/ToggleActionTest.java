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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.core.databinding.observable.value.WritableValue;
import org.junit.jupiter.api.Test;

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

        assertEquals(startValue, action.isChecked(), WRONG_ACTION_CHECKED_STATE);
        assertEquals(startValue, observable.getValue(), WRONG_OBSERVABLE_STATE);
        action.run();
        assertEquals(!startValue, observable.getValue(), WRONG_OBSERVABLE_STATE);
        action.run();
        assertEquals(startValue, observable.getValue(), WRONG_OBSERVABLE_STATE);
    }

    /**
     * Verifies that the action toggles its state if the model changes.
     */
    @Test
    public void testActionStateUpdateFromModel() {
        WritableValue<Boolean> observable = new WritableValue<>(true, Boolean.class);
        ToggleAction action = new ToggleAction("description", "icon", observable);

        assertTrue(action.isChecked(), WRONG_ACTION_CHECKED_STATE);
        observable.setValue(false);
        assertFalse(action.isChecked(), WRONG_ACTION_CHECKED_STATE);
    }
}
