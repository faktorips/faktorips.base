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

import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.IControlContentAdapter2;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

public class StyledTextContentAdapter implements IControlContentAdapter, IControlContentAdapter2 {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.taskassistance.IControlContentAdapter#getControlContents(org.
     * eclipse.swt.widgets.Control)
     */
    @Override
    public String getControlContents(Control control) {
        return ((StyledText)control).getText();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.fieldassist.IControlContentAdapter#setControlContents(org.eclipse.swt.
     * widgets.Control, java.lang.String, int)
     */
    @Override
    public void setControlContents(Control control,
            String text,
            int cursorPosition) {
        ((StyledText)control).setText(text);
        ((StyledText)control).setSelection(cursorPosition, cursorPosition);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.fieldassist.IControlContentAdapter#insertControlContents(org.eclipse.swt.
     * widgets.Control, java.lang.String, int)
     */
    @Override
    public void insertControlContents(Control control,
            String text,
            int cursorPosition) {
        Point selection = ((StyledText)control).getSelection();
        ((StyledText)control).insert(text);
        // Insert will leave the cursor at the end of the inserted text. If this
        // is not what we wanted, reset the selection.
        if (cursorPosition < text.length()) {
            ((StyledText)control).setSelection(selection.x + cursorPosition,
                    selection.x + cursorPosition);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.fieldassist.IControlContentAdapter#getCursorPosition(org.eclipse.swt.
     * widgets.Control)
     */
    @Override
    public int getCursorPosition(Control control) {
        return ((StyledText)control).getCaretOffset();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.fieldassist.IControlContentAdapter#getInsertionBounds(org.eclipse.swt.
     * widgets.Control)
     */
    @Override
    public Rectangle getInsertionBounds(Control control) {
        StyledText text = (StyledText)control;
        Point caretOrigin = text.getLocationAtOffset(text.getCaretOffset());
        return new Rectangle(caretOrigin.x + text.getClientArea().x,
                caretOrigin.y + text.getClientArea().y + 3, 1, text.getLineHeight());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.fieldassist.IControlContentAdapter#setCursorPosition(org.eclipse.swt.
     * widgets.Control, int)
     */
    @Override
    public void setCursorPosition(Control control, int position) {
        ((StyledText)control).setSelection(new Point(position, position));
    }

    /**
     * @see org.eclipse.jface.fieldassist.IControlContentAdapter2#getSelection(org.eclipse.swt.widgets.Control)
     * 
     * @since 3.4
     */
    @Override
    public Point getSelection(Control control) {
        return ((StyledText)control).getSelection();
    }

    /**
     * @see org.eclipse.jface.fieldassist.IControlContentAdapter2#setSelection(org.eclipse.swt.widgets.Control,
     *      org.eclipse.swt.graphics.Point)
     * 
     * @since 3.4
     */
    @Override
    public void setSelection(Control control, Point range) {
        ((StyledText)control).setSelection(range);
    }
}
