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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link DatatypeFieldEditGuard} directly against a bare {@link Text} on an unopened
 * {@link Shell}, rather than only indirectly through the two attribute edit dialogs that use it.
 * <p>
 * A {@link Text} on a shell that was never opened never reports focus, so
 * {@code isFocusControl()} is unconditionally {@code false} here — the two early-return branches of
 * {@code leaveIfFocusSettledElsewhere()} (disposed control, focus settled back on the field) are
 * therefore not reachable by any test in this class either, see that method's Javadoc, and neither is
 * the constructor's {@code ModifyListener} branch that sets the editing state while already focused
 * ({@link #testModifyWhileNotFocused_doesNotMarkAsEditing()} only exercises the always-false case).
 */
public class DatatypeFieldEditGuardTest extends AbstractIpsPluginTest {

    private Shell shell;
    private Text text;
    private int settledCount;
    private DatatypeFieldEditGuard guard;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        shell = new Shell(Display.getCurrent());
        text = new Text(shell, SWT.NONE);
        guard = new DatatypeFieldEditGuard(text, () -> settledCount++);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        shell.dispose();
        super.tearDown();
    }

    private void drainDisplayEvents() {
        while (shell.getDisplay().readAndDispatch()) {
            // dispatch until the queue is empty
        }
    }

    @Test
    public void testInitialState_isNotEditing() {
        assertThat(guard.isEditing(), is(false));
    }

    @Test
    public void testFocusGained_marksAsEditing() {
        text.notifyListeners(SWT.FocusIn, new Event());

        assertThat(guard.isEditing(), is(true));
    }

    @Test
    public void testModifyWhileNotFocused_doesNotMarkAsEditing() {
        text.notifyListeners(SWT.Modify, new Event());

        assertThat(guard.isEditing(), is(false));
    }

    @Test
    public void testFocusLost_appliesTheDeferredCallbackAndClearsEditingState() {
        text.notifyListeners(SWT.FocusIn, new Event());

        text.notifyListeners(SWT.FocusOut, new Event());
        drainDisplayEvents();

        assertThat(guard.isEditing(), is(false));
        assertThat(settledCount, is(1));
    }

    @Test
    public void testFocusLostWhileNeverEditing_stillAppliesTheCallback() {
        text.notifyListeners(SWT.FocusOut, new Event());
        drainDisplayEvents();

        assertThat(settledCount, is(1));
    }

    @Test
    public void testSettleNow_appliesTheCallbackAndClearsEditingState() {
        text.notifyListeners(SWT.FocusIn, new Event());

        guard.settleNow();

        assertThat(guard.isEditing(), is(false));
        assertThat(settledCount, is(1));
    }

    @Test
    public void testSettleNowWhileNotEditing_stillAppliesTheCallback() {
        guard.settleNow();

        assertThat(settledCount, is(1));
    }
}
