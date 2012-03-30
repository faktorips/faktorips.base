/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
