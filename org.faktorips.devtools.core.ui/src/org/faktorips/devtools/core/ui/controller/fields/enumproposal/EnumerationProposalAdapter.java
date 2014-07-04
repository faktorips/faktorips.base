/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields.enumproposal;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;

/**
 * Opens the proposal-popup on mouse up and thus also on double click.
 */
public class EnumerationProposalAdapter extends ContentProposalAdapter {

    public EnumerationProposalAdapter(Text textControl, IContentProposalProvider proposalProvider, KeyStroke keyStroke,
            char[] activationChar) {
        super(textControl, new TextContentAdapter(), proposalProvider, keyStroke, activationChar);
    }

    public static EnumerationProposalAdapter createAndActivateOnAnyKey(Text text,
            Button button,
            ValueDatatype valueDatatype,
            IValueSetOwner owner,
            IInputFormat<String> inputFormat) {
        IContentProposalProvider proposalProvider = new EnumerationProposalProvider(valueDatatype, owner, inputFormat,
                PROPOSAL_REPLACE);
        EnumerationProposalAdapter proposalAdapter = new EnumerationProposalAdapter(text, proposalProvider, null, null);
        proposalAdapter.setProposalAcceptanceStyle(PROPOSAL_REPLACE);
        proposalAdapter.addListenerForOpenOnClick(text, button);
        return proposalAdapter;
    }

    private void addListenerForOpenOnClick(final Text text, final Button button) {
        button.addSelectionListener(new ClickSelectionListener(text));
    }

    private final class ClickSelectionListener extends SelectionAdapter {
        private final Text text;

        private ClickSelectionListener(Text text) {
            this.text = text;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            openProposals(text);
        }

        /**
         * Workaround to let the context proposal work like a combo control.
         * <p>
         * When a combo is opened it shows ALL available entries. To get all available proposals the
         * cursor must be set to the first position. To make clear that the whole text will be
         * replaced it is also nice to have the whole text selected. To achieve both, the selection
         * selection is set from last position to first position.
         * <p>
         * The focus is set to the text control which also leads the focus to the proposal pop-up to
         * get the proper navigation and closing behavior of the pop-up.
         */
        private void openProposals(Text text) {
            text.setSelection(0);
            text.setFocus();
            openProposalPopup();
        }
    }

}
