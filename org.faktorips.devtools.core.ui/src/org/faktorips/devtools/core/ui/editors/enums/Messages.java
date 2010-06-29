/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
    public static String EnumValuesSection_labelResetLiteralNames;
    public static String EnumValuesSection_tooltipResetLiteralNames;

}
