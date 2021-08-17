/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public enum IpsColor {

    /** The purple to use for model artifacts. */
    MODEL_PURPLE("modelPurple", new RGB(84, 67, 147)), //$NON-NLS-1$

    /** The blue to use for templates. */
    TEMPLATE_BLUE("templateBlue", new RGB(133, 187, 214)), //$NON-NLS-1$

    /** The grey to use for disabled items. */
    DISABLED_GREY("disabledGrey", new RGB(128, 128, 128)), //$NON-NLS-1$

    /** The green to use for product components. */
    PRODUCT_GREEN("productGreen", new RGB(47, 122, 68)), //$NON-NLS-1$

    DEFAULT(null, null);

    private String symbolicName;
    private RGB defaultRgb;

    private IpsColor(String symbolicName, RGB defaultRgb) {
        this.symbolicName = symbolicName;
        this.defaultRgb = defaultRgb;
    }

    public Color getColor() {
        if (symbolicName == null) {
            return null;
        }
        return IpsUIPlugin.getDefault().getColor(symbolicName, defaultRgb);
    }

}
