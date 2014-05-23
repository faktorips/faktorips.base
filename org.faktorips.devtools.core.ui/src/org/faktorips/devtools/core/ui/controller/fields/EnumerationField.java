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

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * A text control with a content proposal that provides the enumeration values. An arrow down icon
 * is drawn near the text's right side to emphasize the content proposal functionality.
 */
public class EnumerationField extends FormattingTextField<String> {

    public EnumerationField(Text text, EnumDatatype datatype, String nullRepresentation) {
        super(text, IpsUIPlugin.getDefault().getInputFormat(datatype, null), nullRepresentation);
        EnumPainter enumPainter = new EnumPainter();
        text.addPaintListener(enumPainter);
    }

    public class EnumPainter implements PaintListener {

        @Override
        public void paintControl(PaintEvent e) {
            GC gc = e.gc;
            paintDownArrow(gc);
        }

        private void paintDownArrow(GC gc) {
            Rectangle clipping = gc.getClipping();
            int x = (clipping.width - 16) - 2;
            int y = (clipping.height - 16) / 2;
            Image arrow = IpsUIPlugin.getImageHandling().getSharedImage("ArrowDown_grey.gif", true); //$NON-NLS-1$
            gc.drawImage(arrow, x, y);
        }

    }

}
