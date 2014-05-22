/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controlfactories;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Control;

/**
 * Opens the proposal-popup on mouse up and thus also on double click.
 */
public class OpenOnMouseClickProposalAdapter extends ContentProposalAdapter {

    public OpenOnMouseClickProposalAdapter(Control control, IControlContentAdapter controlContentAdapter,
            IContentProposalProvider proposalProvider, KeyStroke keyStroke, char[] activationChars) {
        super(control, controlContentAdapter, proposalProvider, keyStroke, activationChars);
        addListenerForOpenOnMouseUp(control);
    }

    public static OpenOnMouseClickProposalAdapter createAndActivateOnAnyKey(Control control,
            IControlContentAdapter controlContentAdapter,
            IContentProposalProvider proposalProvider) {
        return new OpenOnMouseClickProposalAdapter(control, controlContentAdapter, proposalProvider, null, null);
    }

    private void addListenerForOpenOnMouseUp(Control control) {
        control.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                openProposalPopup();
            }
        });
    }

}
