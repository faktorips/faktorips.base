/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.extsystems.excel;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.extsystems.excel.messages"; //$NON-NLS-1$

    private Messages() {
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    public static String DateValueConverter_msgConversionErrorExtern;
    public static String DateValueConverter_msgConversionErrorIntern;
    public static String IntegerValueConverter_msgLoosingData;
    public static String IntegerValueConverter_msgConversionErrorIntern;
    public static String IntegerValueConverter_msgConversionErrorExtern;
    public static String LongValueConverter_msgLoosingData;
    public static String LongValueConverter_msgConversionErrorExternal;
    public static String LongValueConverter_msgConversionErrorInternal;
    public static String BooleanValueConverter_msgConverisonErrorExternal;
    public static String GregorianCalendarValueConverter_msgConversionErrorExternal;
    public static String GregorianCalendarValueConverter_msgConversionErrorInternal;
    public static String MoneyValueConverter_msgConversionErrorExtern;
    public static String MoneyValueConverter_msgAnotherConversionErrorExtern;
    public static String MoneyValueConverter_msgConversionErrorIntern;
    public static String DoubleValueConverter_msgConversionErrorExtern;
    public static String DoubleValueConverter_msgConversionErrorIntern;
    public static String DecimalValueConverter_msgConversionErrorExtern;
    public static String DecimalValueConverter_msgAnotherConversionErrorExtern;
    public static String DecimalValueConverter_msgConversionErrorIntern;
}
