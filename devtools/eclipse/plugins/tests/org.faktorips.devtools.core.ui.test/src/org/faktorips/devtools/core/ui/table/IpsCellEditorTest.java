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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.junit.Test;

public class IpsCellEditorTest {

    @Test
    public void removeOldTraversalStrategyAsListener() {
        Text textControl = mock(Text.class);
        IpsCellEditor editor = spy(new TextCellEditor(textControl));
        Control control = mock(Control.class);
        when(editor.getControl()).thenReturn(control);

        TraversalStrategy strat1 = mock(TraversalStrategy.class);
        editor.setTraversalStrategy(strat1);
        verify(control).addKeyListener(strat1);
        verify(control).addTraverseListener(strat1);
        verify(control).addFocusListener(strat1);

        TraversalStrategy strat2 = mock(TraversalStrategy.class);
        editor.setTraversalStrategy(strat2);
        verify(control).addKeyListener(strat2);
        verify(control).addTraverseListener(strat2);
        verify(control).addFocusListener(strat2);
        verify(control).removeKeyListener(strat1);
        verify(control).removeTraverseListener(strat1);
        verify(control).removeFocusListener(strat1);
    }

}
