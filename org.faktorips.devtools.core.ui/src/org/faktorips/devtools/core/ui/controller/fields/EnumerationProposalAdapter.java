/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;

/**
 * Opens the proposal-popup on mouse up and thus also on double click.
 */
public class EnumerationProposalAdapter extends ContentProposalAdapter {

    private static final int CLICK_AREA_WIDTH = 24;

    public EnumerationProposalAdapter(Text control, IContentProposalProvider proposalProvider, KeyStroke keyStroke,
            char[] activationChar) {
        super(control, new TextContentAdapter(), proposalProvider, keyStroke, activationChar);
        addListenerForOpenOnMouseUp(control);
    }

    public static EnumerationProposalAdapter createAndActivateOnAnyKey(Text control,
            ValueDatatype valueDatatype,
            IValueSetOwner owner,
            IInputFormat<String> inputFormat) {
        IContentProposalProvider proposalProvider = new EnumerationProposalProvider(valueDatatype, owner, inputFormat);
        return new EnumerationProposalAdapter(control, proposalProvider, null, null);
    }

    private void addListenerForOpenOnMouseUp(final Text control) {
        control.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                Rectangle clickRegion = control.getClientArea();
                if (!isLeftAligned(control)) {
                    clickRegion.x = clickRegion.width - CLICK_AREA_WIDTH;
                }
                clickRegion.width = CLICK_AREA_WIDTH;
                if (clickRegion.contains(e.x, e.y)) {
                    control.setSelection(control.getText().length(), 0);
                    openProposalPopup();
                }
            }
        });
    }

    private boolean isLeftAligned(final Text control) {
        return (control.getStyle() & SWT.RIGHT) != 0;
    }
}
