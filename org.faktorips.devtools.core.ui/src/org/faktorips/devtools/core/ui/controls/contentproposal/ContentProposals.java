/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.controls.contentproposal;

import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * Helper class to create {@link ContentProposalAdapter ContentProposalAdapters}.
 */
public class ContentProposals {

    /**
     * The key stroke to trigger auto completion, {@code [Ctrl]+[Space]}.
     */
    public static final KeyStroke AUTO_COMPLETION_KEY_STROKE = getAutoCompletionKeyStroke();

    private ContentProposals() {
        // Utility class
    }

    /**
     * Attaches a {@link ContentProposalAdapter} to the given {@link Text} control, using the given
     * {@link IContentProposalProvider proposal provider}, using a {@link TextContentAdapter} and
     * the {@link ContentProposals#AUTO_COMPLETION_KEY_STROKE} {@code [Ctrl]+[Space]}.
     *
     * @param text a {@link Text} control
     * @param proposalProvider the provider for proposals based on the text's contents
     * @return the created {@link IContentProposalProvider}
     */
    public static ContentProposalAdapter forText(Text text, IContentProposalProvider proposalProvider) {
        return addContentProposalAdapter(text, new TextContentAdapter(), proposalProvider);
    }

    /**
     * Attaches a {@link ContentProposalAdapter} to the given {@link Combo} control, using the given
     * {@link IContentProposalProvider proposal provider}, using a {@link ComboContentAdapter} and
     * the {@link ContentProposals#AUTO_COMPLETION_KEY_STROKE} {@code [Ctrl]+[Space]}.
     *
     * @param combo a {@link Combo} control
     * @param proposalProvider the provider for proposals based on the combo's contents
     * @return the created {@link IContentProposalProvider}
     */
    public static ContentProposalAdapter forCombobox(Combo combo, IContentProposalProvider proposalProvider) {
        return addContentProposalAdapter(combo, new ComboContentAdapter(), proposalProvider);
    }

    /**
     * Attaches a {@link ContentProposalAdapter} to the given {@link Control}, using the given
     * {@link IContentProposalProvider proposal provider}, {@link IControlContentAdapter} and the
     * {@link ContentProposals#AUTO_COMPLETION_KEY_STROKE} {@code [Ctrl]+[Space]}.
     *
     * @param control a {@link Control}
     * @param contentAdapter an adapter matching the control
     * @param proposalProvider the provider for proposals based on the combo's contents
     * @return the created {@link IContentProposalProvider}
     */
    public static ContentProposalAdapter addContentProposalAdapter(Control control,
            IControlContentAdapter contentAdapter,
            IContentProposalProvider proposalProvider) {
        ContentProposalAdapter contentProposalAdapter = new ContentProposalAdapter(control, contentAdapter,
                proposalProvider, ContentProposals.AUTO_COMPLETION_KEY_STROKE, null);
        contentProposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
        return contentProposalAdapter;
    }

    private static KeyStroke getAutoCompletionKeyStroke() {
        try {
            return KeyStroke.getInstance(IKeyLookup.CTRL_NAME + KeyStroke.KEY_DELIMITER + IKeyLookup.SPACE_NAME);
        } catch (final ParseException e) {
            throw new IllegalArgumentException("KeyStroke \"Ctrl+Space\" could not be parsed.", e); //$NON-NLS-1$
        }
    }

}
