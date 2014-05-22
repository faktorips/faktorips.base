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
 * An implementation of <code>AbstractEnumDatatypeBasedField</code> that displays the values of an
 * <code>EnumDatatype</code>. If the <code>EnumDatatype</code> supports value names these will be
 * displayed instead of the value ids.
 * 
 * @author Peter Kuntz
 * 
 */
public class EnumumerationField extends FormattingTextField<String> {

    public EnumumerationField(Text text, EnumDatatype datatype, String nullRepresentation) {
        super(text, IpsUIPlugin.getDefault().getInputFormat(datatype, null), nullRepresentation);
        EnumPainter enumPainter = new EnumPainter();
        text.addPaintListener(enumPainter);
    }

    public class EnumPainter implements PaintListener {

        @Override
        public void paintControl(PaintEvent e) {
            GC gc = e.gc;
            Rectangle clipping = gc.getClipping();
            int x = (clipping.width - 16) - 2;
            int y = (clipping.height - 16) / 2;
            Image arrow = IpsUIPlugin.getImageHandling().getSharedImage("ArrowDown_grey.gif", true); //$NON-NLS-1$
            System.out.println(arrow.getBounds());
            gc.drawImage(arrow, x, y);
        }

    }

}
