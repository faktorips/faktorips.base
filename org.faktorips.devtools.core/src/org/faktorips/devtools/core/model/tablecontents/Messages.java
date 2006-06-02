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

package org.faktorips.devtools.core.model.tablecontents;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.model.tablecontents.messages"; //$NON-NLS-1$

    private Messages() {
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    public static String AbstractXlsTableImportOperation_errRead;
    public static String AbstractXlsTableImportOperation_errNoSheets;
    public static String AbstractXlsTableImportOperation_errNoRows;
    public static String TableExportOperation_errNoGenerations;
    public static String TableExportOperation_labelMonitorTitle;
    public static String TableExportOperation_errStructureNotFound;
    public static String TableExportOperation_errWrite;
    public static String TableExportOperation_errStructureTooMuchColumns;
    public static String TableExportOperation_errContentsTooMuchColumns;
    public static String TableExportOperation_errTooMuchRows;
}
