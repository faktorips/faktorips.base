/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.internal.ContentProposal;

/**
 * Listener for the use in {@link ContentProposalAdapter}. This aditional listener extends the
 * normal replace or insert. In case of using lower or upper case, then we must replace the prefix
 * and insert the whole word.
 * 
 * @author frank
 */
public class ContentProposalListener implements IContentProposalListener {

    private ContentProposalAdapter contentProposalAdapter;

    public ContentProposalListener(ContentProposalAdapter contentProposalAdapter) {
        this.contentProposalAdapter = contentProposalAdapter;
    }

    @Override
    public void proposalAccepted(IContentProposal proposal) {
        String content = proposal.getContent();
        int pos = proposal.getCursorPosition();
        Text control = (Text)contentProposalAdapter.getControl();
        IControlContentAdapter controlContentAdapter = contentProposalAdapter.getControlContentAdapter();

        if (proposal instanceof ContentProposal) {
            ContentProposal contentProposal = (ContentProposal)proposal;
            int cursorPosition = controlContentAdapter.getCursorPosition(control);
            int prefixLength = contentProposal.getPrefixLength();
            if (prefixLength > 0) {
                int newCursorPosition = cursorPosition - prefixLength;
                if (newCursorPosition > 0) {
                    controlContentAdapter.setCursorPosition(contentProposalAdapter.getControl(), newCursorPosition);
                    pos = newCursorPosition;
                }
                String textBefore = control.getText().substring(0, newCursorPosition);
                String textBehind = control.getText().substring(cursorPosition, control.getText().length());
                controlContentAdapter.setControlContents(control, textBefore + textBehind, newCursorPosition);
            }
        }
        controlContentAdapter.insertControlContents(control, content, pos + content.length());
    }

}
