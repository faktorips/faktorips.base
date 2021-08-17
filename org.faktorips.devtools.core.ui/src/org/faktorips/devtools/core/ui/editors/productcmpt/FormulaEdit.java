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

import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.swt.custom.StyledText;
import org.faktorips.devtools.core.ui.controller.fields.StyledTextModifyListener;
import org.faktorips.devtools.model.productcmpt.IFormula;

public final class FormulaEdit {

    private static final KeyStroke CTRL_SPACE = getKeyStroke(IKeyLookup.CTRL_NAME + '+' + IKeyLookup.SPACE_NAME);
    private static final KeyStroke ENTER = getKeyStroke(IKeyLookup.ENTER_NAME);

    private FormulaEdit() {
        // util
    }

    private static KeyStroke getKeyStroke(String keyStroke) {
        try {
            return KeyStroke.getInstance(keyStroke);
        } catch (ParseException e) {
            throw new IllegalArgumentException("KeyStroke \"" + keyStroke + "\" could not be parsed.", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    static ContentProposalAdapter createContentProposalAdapter(StyledText formulaText, IFormula formula) {
        ContentProposalAdapter contentProposalAdapter = new ContentProposalAdapter(formulaText,
                new StyledTextContentAdapter(),
                new ExpressionProposalProvider(formula), CTRL_SPACE, new char[] { '.' });

        contentProposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_IGNORE);

        formulaText.addExtendedModifyListener(new StyledTextModifyListener());
        formulaText.addVerifyKeyListener(e -> {
            if (FormulaEdit.ENTER.getNaturalKey() == e.keyCode && contentProposalAdapter.isProposalPopupOpen()) {
                e.doit = false;
            }
        });
        return contentProposalAdapter;
    }
}
