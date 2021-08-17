/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;

public class StyledTextModifyListener implements ExtendedModifyListener {

    private static final String OPENING_PARENTHESIS = "{("; //$NON-NLS-1$
    private static final String CLOSING_PARENTHESIS = ")}"; //$NON-NLS-1$
    private final int[] colorsParenthesis = { SWT.COLOR_BLACK, SWT.COLOR_RED, SWT.COLOR_DARK_MAGENTA,
            SWT.COLOR_DARK_GREEN, SWT.COLOR_MAGENTA, SWT.COLOR_DARK_YELLOW, SWT.COLOR_CYAN, SWT.COLOR_BLUE };

    @Override
    public void modifyText(ExtendedModifyEvent event) {
        StyledText styledText = (StyledText)event.getSource();

        String text = styledText.getText();
        List<StyleRange> ranges = new ArrayList<>();
        int color = 0;

        for (int i = 0, n = text.length(); i < n; i++) {
            if (OPENING_PARENTHESIS.indexOf(text.charAt(i)) > -1) {
                ranges.add(new StyleRange(i, 1,
                        Display.getCurrent()
                                .getSystemColor(colorsParenthesis[color % (colorsParenthesis.length - 1)]),
                        null, SWT.NORMAL));
                color++;
            }
            if (CLOSING_PARENTHESIS.indexOf(text.charAt(i)) > -1) {
                if (color > 0) {
                    color--;
                }
                ranges.add(new StyleRange(i, 1,
                        Display.getCurrent()
                                .getSystemColor(colorsParenthesis[color % (colorsParenthesis.length - 1)]),
                        null, SWT.NORMAL));
            }
        }
        if (!ranges.isEmpty()) {
            styledText.replaceStyleRanges(event.start, event.length, ranges
                    .toArray(new StyleRange[0]));
        }
    }
}
