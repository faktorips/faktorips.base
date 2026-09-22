/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Text;

/**
 * Defers a callback until a {@link Text} field the user is actively typing into loses focus, so
 * that not every intermediate keystroke is treated as "the user is done editing".
 * <p>
 * Used by attribute edit dialogs where recalculating the allowed value set types on every keystroke
 * of the datatype field would repeatedly discard the value set configured for a datatype that has
 * not been fully typed yet. See {@link #settleNow()} for the case that the dialog is confirmed
 * without ever leaving the field.
 * <p>
 * Registers listeners on {@code text} for as long as the instance is reachable; no explicit
 * disposal is needed since the listeners are torn down together with {@code text} itself.
 */
public class DatatypeFieldEditGuard {

    private final Text text;
    private final Runnable onSettled;

    private boolean editing;

    /**
     * @param text the field whose editing state is tracked
     * @param onSettled called once the field is left (or {@link #settleNow()} is called), to apply
     *            the value that was deferred while the field was being edited
     */
    public DatatypeFieldEditGuard(Text text, Runnable onSettled) {
        this.text = text;
        this.onSettled = onSettled;

        text.addModifyListener(e -> {
            if (text.isFocusControl()) {
                editing = true;
            }
        });
        text.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                editing = true;
            }

            @Override
            public void focusLost(FocusEvent e) {
                text.getDisplay().asyncExec(() -> leaveIfFocusSettledElsewhere());
            }
        });
    }

    /**
     * A content proposal popup can take the keyboard focus away from the field for a moment, which
     * would end the editing state in the middle of typing. Leaving the field is therefore only
     * accepted once the focus has settled somewhere else.
     */
    private void leaveIfFocusSettledElsewhere() {
        if (text.isDisposed() || text.isFocusControl()) {
            return;
        }
        settle();
    }

    private void settle() {
        editing = false;
        onSettled.run();
    }

    /**
     * Returns whether the field is currently being edited, i.e. whether the deferred adjustment is
     * still pending.
     *
     * @return {@code true} if the field is currently being edited
     */
    public boolean isEditing() {
        return editing;
    }

    /**
     * Settles the field immediately, regardless of whether it currently has focus. Callers
     * confirming a dialog without the field ever losing focus must use this instead of relying on
     * {@link #isEditing()}/the focus-lost handling, since neither of those is otherwise triggered by
     * confirming the dialog.
     */
    public void settleNow() {
        settle();
    }
}
