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

package org.faktorips.devtools.tableconversion.ui;

import org.eclipse.osgi.util.NLS;

/**
 * @author Roman Grutza
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.tableconversion.ui.messages"; //$NON-NLS-1$

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    public static String CSVPropertyCompositeFactory_dateFormatHelp1;
    public static String CSVPropertyCompositeFactory_dateFormatLabel;
    public static String CSVPropertyCompositeFactory_errMsgDecimalGroupingLength;
    public static String CSVPropertyCompositeFactory_errMsgDecimalSeparatorAndGroupingCharsAreEqual;
    public static String CSVPropertyCompositeFactory_errMsgDecimalSeparatorLength;
    public static String CSVPropertyCompositeFactory_errMsgInvalidDateFormat;
    public static String CSVPropertyCompositeFactory_errMsgFieldDelimiterLength;
    public static String CSVPropertyCompositeFactory_fieldDelimiterLabel;
    public static String CSVPropertyCompositeFactory_labelDecimalGrouping;
    public static String CSVPropertyCompositeFactory_labelDecimalSeparator;
}
