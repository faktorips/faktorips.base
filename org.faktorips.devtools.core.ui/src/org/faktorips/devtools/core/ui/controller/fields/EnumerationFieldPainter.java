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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class EnumerationFieldPainter implements PaintListener {

    private static final int PADDING_X = 2;
    private static final int IMAGE_X = 16;
    private static final int IMAGE_Y = 16;

    private final Text text;
    private final IValueSetOwner valueSetOwner;
    private final ValueDatatype valueDatatype;

    public EnumerationFieldPainter(Text text, ValueDatatype valueDatatype, IValueSetOwner valueSetOwner) {
        this.text = text;
        this.valueDatatype = valueDatatype;
        this.valueSetOwner = valueSetOwner;
    }

    public static void addPainterTo(Text text, ValueDatatype valueDatatype, IValueSetOwner valueSetOwner) {
        EnumerationFieldPainter enumPainter = new EnumerationFieldPainter(text, valueDatatype, valueSetOwner);
        text.addPaintListener(enumPainter);
    }

    @Override
    public void paintControl(PaintEvent e) {
        if (isPaintingEnumSign()) {
            GC gc = e.gc;
            Rectangle clipping = gc.getClipping();
            int x;
            if (isRightAligned()) {
                x = clipping.x + PADDING_X;
                clipping.x += IMAGE_X + (PADDING_X * 2);
            } else {
                x = (clipping.width - IMAGE_X) - PADDING_X;
                clipping.width -= IMAGE_X + (PADDING_X * 2);
            }
            int y = (clipping.height - IMAGE_Y) / 2;
            Image image = IpsUIPlugin.getImageHandling().getSharedImage("ArrowDown_grey.gif", true); //$NON-NLS-1$
            gc.drawImage(image, x, y);
            gc.setClipping(clipping);
        }
    }

    private boolean isRightAligned() {
        return (text.getStyle() & SWT.RIGHT) != 0;
    }

    private boolean isPaintingEnumSign() {
        return valueDatatype.isEnum()
                || (valueSetOwner != null && valueSetOwner.getValueSet() != null && valueSetOwner.getValueSet()
                        .canBeUsedAsSupersetForAnotherEnumValueSet());
    }

}