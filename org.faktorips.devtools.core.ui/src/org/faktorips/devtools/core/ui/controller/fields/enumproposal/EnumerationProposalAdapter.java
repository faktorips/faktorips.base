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
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                /*
                 * Workaround to let the context proposal work like a combo control.
                 * 
                 * When a combo is opened it shows ALL available entries. This is not how the
                 * context proposal works. It normally only shows matching entries, depending on the
                 * text already entered in the text field. To simulate combo-behavior, set the
                 * cursor position to 0. Now there is no text to the left of the cursor. This forces
                 * the context proposal to show all values, as it has no text to use as filter.
                 */
                text.setSelection(0);
                openProposalPopup();
            }
        });
    }

}
