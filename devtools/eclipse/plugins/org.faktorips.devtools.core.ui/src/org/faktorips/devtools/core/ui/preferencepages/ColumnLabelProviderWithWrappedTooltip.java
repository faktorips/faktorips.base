/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.preferencepages;

import java.util.function.Function;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Point;
import org.faktorips.devtools.model.internal.util.StringUtils;

/**
 * A {@link ColumnLabelProvider} that displays text labels and tooltips for longer texts. If the
 * text contains line breaks, only the first line is used for the label. For the tooltip, the text
 * is wrapped at a width of {@value #TOOLTIP_LINE_LENGTH} characters.
 */
public class ColumnLabelProviderWithWrappedTooltip extends ColumnLabelProvider {
    private static final int TOOLTIP_LINE_LENGTH = 75;

    private final Function<Object, String> textGetter;

    public ColumnLabelProviderWithWrappedTooltip(Function<Object, String> textGetter) {
        this.textGetter = textGetter;
    }

    @Override
    public String getText(Object element) {
        String text = textGetter.apply(element);
        int lineBreak = text.indexOf('\n');
        if (lineBreak > 0) {
            return text.substring(0, lineBreak);
        }
        return text;
    }

    @Override
    public String getToolTipText(Object element) {
        String text = textGetter.apply(element);
        return StringUtils.wrapText(text, TOOLTIP_LINE_LENGTH, "\n");
    }

    @Override
    public Point getToolTipShift(Object object) {
        return new Point(5, 5);
    }

    @Override
    public int getToolTipDisplayDelayTime(Object object) {
        return 200;
    }

    @Override
    public int getToolTipTimeDisplayed(Object object) {
        return 10000;
    }
}
