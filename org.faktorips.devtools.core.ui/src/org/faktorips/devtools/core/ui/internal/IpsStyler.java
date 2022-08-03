/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.internal;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.TextStyle;
import org.faktorips.devtools.core.ui.IpsColor;

/** A simple styler for colored text that may be stricken out. */
public class IpsStyler extends Styler {

    public static final Styler TEMPLATE_STYLER = new IpsStyler(IpsColor.TEMPLATE_BLUE);

    public static final Styler OVERWRITE_TEMPLATE_STYLER = new IpsStyler(IpsColor.TEMPLATE_BLUE, true);

    public static final Styler DISABLED_STYLER = new IpsStyler(IpsColor.DISABLED_GREY, true);

    public static final Styler DEACTIVATED_STYLER = new IpsStyler(IpsColor.DISABLED_GREY);

    public static final Styler PRODUCT_STYLER = new IpsStyler(IpsColor.PRODUCT_GREEN);

    public static final Styler DEFAULT_STYLER = new IpsStyler(IpsColor.DEFAULT);

    public static final Styler QUALIFIER_STYLER = StyledString.QUALIFIER_STYLER;

    public static final Styler DEFAULT_CARDINALITY_STYLER = StyledString.DECORATIONS_STYLER;

    public static final Styler MODEL_CARDINALITY_STYLER = new IpsStyler(IpsColor.MODEL_PURPLE);

    public static final Styler ROLENAME_STYLER = new IpsStyler(IpsColor.MODEL_PURPLE);

    private final IpsColor foregroundColor;

    private final boolean strikeout;

    private IpsStyler(IpsColor foregroundColor, boolean strikeout) {
        super();
        this.foregroundColor = foregroundColor;
        this.strikeout = strikeout;
    }

    public IpsStyler(IpsColor foregroundColor) {
        this(foregroundColor, false);
    }

    @Override
    public void applyStyles(TextStyle textStyle) {
        textStyle.foreground = foregroundColor.getColor();
        textStyle.strikeout = strikeout;
    }

}
