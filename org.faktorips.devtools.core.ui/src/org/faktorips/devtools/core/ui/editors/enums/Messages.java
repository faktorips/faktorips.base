/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.enums;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.enums.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String EnumValuesSection_title;
    public static String EnumValuesSection_labelNewValue;
    public static String EnumValuesSection_tooltipNewValue;
    public static String EnumValuesSection_labelDeleteValue;
    public static String EnumValuesSection_tooltipDeleteValue;
    public static String EnumValuesSection_labelMoveEnumValueUp;
    public static String EnumValuesSection_tooltipMoveEnumValueUp;
    public static String EnumValuesSection_labelMoveEnumValueDown;
    public static String EnumValuesSection_tooltipMoveEnumValueDown;
    public static String EnumValuesSection_labelLockAndSync;
    public static String EnumValuesSection_tooltipLockAndSync;
    public static String EnumValuesSection_labelSubmenuRefactor;
    public static String EnumValuesSection_labelRenameLiteralName;
    public static String EnumValuesSection_labelResetLiteralNames;
    public static String EnumValuesSection_tooltipResetLiteralNames;
    public static String EnumValuesSection_column;

}
