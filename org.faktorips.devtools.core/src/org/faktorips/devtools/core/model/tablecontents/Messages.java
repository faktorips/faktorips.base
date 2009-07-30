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
    public static String TableExportOperation_errNoGenerations;
    public static String TableExportOperation_labelMonitorTitle;
    public static String TableExportOperation_errStructureNotFound;
    public static String TableExportOperation_errWrite;
    public static String TableExportOperation_errStructureTooMuchColumns;
    public static String ExcelTableImportOperation_labelImportFile;
    public static String ExcelTableImportOperation_msgImportEscapevalue;

}
