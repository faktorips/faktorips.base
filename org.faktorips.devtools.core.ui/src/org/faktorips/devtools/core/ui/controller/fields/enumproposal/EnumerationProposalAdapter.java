/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;
import org.faktorips.devtools.model.valueset.IValueSetOwner;

/**
 * Show the proposal-popup on key type, button click and mouse click. Always shows the popup on key
 * events. Per default it does also show the popup button click but not on mouse click. However, it
 * can be configured to do so using {@link #setOpenOnMouseClick(boolean)}, as well as
 * {@link #setOpenOnButtonClick(boolean)}
 */
public class EnumerationProposalAdapter extends ContentProposalAdapter {

    private boolean openOnMouseClick = false;
    private boolean openOnButtonClick = true;

    public EnumerationProposalAdapter(Text text, IContentProposalProvider proposalProvider, KeyStroke keyStroke,
            char[] activationChar) {
        super(text, new TextContentAdapter(), proposalProvider, keyStroke, activationChar);
    }

    /**
     * Configures this proposal adapter whether or not to show the proposals when the text control
     * is clicked. Default is <code>false</code>.
     * 
     * @param open whether the proposals are shown when the text field is clicked.
     */
    public void setOpenOnMouseClick(boolean open) {
        openOnMouseClick = open;
    }

    /**
     * Configures this proposal adapter whether or not to show the proposals when the button is
     * clicked. Default is <code>true</code>.
     * 
     * @param open whether the proposals are shown when the button is clicked.
     */
    public void setOpenOnButtonClick(boolean open) {
        openOnButtonClick = open;
    }

    /**
     * Creates a proposal adapter. Always shows the popup on key events. Per default it does also
     * show the popup button click but not on mouse click. However, it can be configured to do so
     * using {@link #setOpenOnMouseClick(boolean)}, as well as
     * {@link #setOpenOnButtonClick(boolean)}.
     * 
     * @param text the text control to add context proposal to. Must <em>not</em> be
     *            <code>null</code>.
     * @param button the button added in most cases. Can be <code>null</code> if no button is
     *            created (e.g. in tables).
     * @param valueDatatype The datatype of the edit field
     * @param owner the value set owner
     * @param inputFormat the input format of the edit field.
     * @return an {@link EnumerationProposalAdapter}
     */
    public static EnumerationProposalAdapter createAndActivateOnAnyKey(Text text,
            Button button,
            ValueDatatype valueDatatype,
            IValueSetOwner owner,
            IInputFormat<String> inputFormat) {
        IContentProposalProvider proposalProvider = new EnumerationProposalProvider(valueDatatype, owner, inputFormat);
        EnumerationProposalAdapter proposalAdapter = new EnumerationProposalAdapter(text, proposalProvider, null, null);
        proposalAdapter.setProposalAcceptanceStyle(PROPOSAL_REPLACE);
        proposalAdapter.addListenerForOpenOnClick(text, button);
        return proposalAdapter;
    }

    private void addListenerForOpenOnClick(final Text text, final Button button) {
        if (button != null) {
            button.addSelectionListener(new ClickSelectionListener());
        }
        text.addMouseListener(new MouseClickListener());
    }

    /**
     * Workaround to let the context proposal work like a combo control.
     * <p>
     * When a combo is opened it shows ALL available entries. To get all available proposals the
     * cursor must be set to the first position. To make clear that the whole text will be replaced
     * it is also nice to have the whole text selected. To achieve both, the selection selection is
     * set from last position to first position.
     * <p>
     * The focus is set to the text control which also leads the focus to the proposal pop-up to get
     * the proper navigation and closing behavior of the pop-up.
     */
    private void openProposals() {
        if (getControl() instanceof Text) {
            ((Text)getControl()).setSelection(0);
        }
        getControl().setFocus();
        openProposalPopup();
    }

    private final class ClickSelectionListener extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            if (openOnButtonClick) {
                openProposals();
            }
        }

    }

    private final class MouseClickListener extends MouseAdapter {
        @Override
        public void mouseUp(MouseEvent e) {
            if (openOnMouseClick) {
                openProposals();
            }
        }
    }

}
