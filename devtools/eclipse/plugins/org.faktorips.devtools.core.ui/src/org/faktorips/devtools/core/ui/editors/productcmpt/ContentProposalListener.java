/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
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
        Control control = contentProposalAdapter.getControl();
        IControlContentAdapter controlContentAdapter = contentProposalAdapter.getControlContentAdapter();
        int cursorPosition = controlContentAdapter.getCursorPosition(control);

        if (proposal instanceof ContentProposal) {
            ContentProposal contentProposal = (ContentProposal)proposal;
            int prefixLength = contentProposal.getPrefixLength();
            if (prefixLength > 0) {
                int newCursorPosition = cursorPosition - prefixLength;
                if (newCursorPosition > 0) {
                    controlContentAdapter.setCursorPosition(contentProposalAdapter.getControl(), newCursorPosition);
                    pos = newCursorPosition;
                }
                if (control instanceof Text) {
                    Text textControl = (Text)control;
                    String textBefore = textControl.getText().substring(0, newCursorPosition);
                    String textBehind = textControl.getText().substring(cursorPosition, textControl.getText().length());
                    controlContentAdapter.setControlContents(textControl, textBefore + textBehind, newCursorPosition);
                }
                if (control instanceof StyledText) {
                    StyledText textControl = (StyledText)control;
                    String textBefore = textControl.getText().substring(0, newCursorPosition);
                    String textBehind = textControl.getText().substring(cursorPosition, textControl.getText().length());
                    controlContentAdapter.setControlContents(textControl, textBefore + textBehind, newCursorPosition);
                }

            }
        }
        controlContentAdapter.insertControlContents(control, content, pos + content.length());
        if (proposal instanceof ContentProposal && control instanceof StyledText) {
            ((StyledText)control).setCaretOffset(cursorPosition + content.length());
        }
    }

}
