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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.internal.ContentProposal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ContentProposalListenerTest {

    @Mock
    ContentProposalAdapter contentProposalAdapter;

    @Mock
    ContentProposal proposal;

    @Mock
    Text textControl;

    @Mock
    TextContentAdapter controlContentAdapter;

    ContentProposalListener listener;

    @Before
    public void initMocks() {
        when(contentProposalAdapter.getControl()).thenReturn(textControl);
        when(contentProposalAdapter.getControlContentAdapter()).thenReturn(controlContentAdapter);
        listener = new ContentProposalListener(contentProposalAdapter);
    }

    @Test
    public void testProposalAcceptedStart() {
        when(proposal.getContent()).thenReturn("WENN");
        when(proposal.getCursorPosition()).thenReturn(4);
        when(proposal.getPrefixLength()).thenReturn(2);
        when(controlContentAdapter.getCursorPosition(textControl)).thenReturn(2);
        when(textControl.getText()).thenReturn("We");

        listener.proposalAccepted(proposal);

        verify(controlContentAdapter).setControlContents(textControl, "", 0);
        verify(controlContentAdapter).insertControlContents(textControl, "WENN", 8);
    }

    @Test
    public void testProposalAcceptedInside() {
        when(proposal.getContent()).thenReturn("WENN");
        when(proposal.getCursorPosition()).thenReturn(4);
        when(proposal.getPrefixLength()).thenReturn(2);
        when(controlContentAdapter.getCursorPosition(textControl)).thenReturn(13);
        when(textControl.getText()).thenReturn("MAX(1;2) + We + MAX(3;4)");

        listener.proposalAccepted(proposal);

        verify(controlContentAdapter).setControlContents(textControl, "MAX(1;2) +  + MAX(3;4)", 11);
        verify(controlContentAdapter).insertControlContents(textControl, "WENN", 15);
    }

    @Test
    public void testProposalAcceptedEnd() {
        when(proposal.getContent()).thenReturn("WENN");
        when(proposal.getCursorPosition()).thenReturn(4);
        when(proposal.getPrefixLength()).thenReturn(2);
        when(controlContentAdapter.getCursorPosition(textControl)).thenReturn(13);
        when(textControl.getText()).thenReturn("MAX(1;2) + We");

        listener.proposalAccepted(proposal);

        verify(controlContentAdapter).setControlContents(textControl, "MAX(1;2) + ", 11);
        verify(controlContentAdapter).insertControlContents(textControl, "WENN", 15);
    }

}
