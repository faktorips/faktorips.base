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

package org.faktorips.devtools.core;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.messages"; //$NON-NLS-1$

    private Messages() {
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    public static String DatatypeFormatter_booleanFalse;

    public static String DatatypeFormatter_booleanTrue;

    public static String EnumTypeDisplay_id;

    public static String EnumTypeDisplay_name;

    public static String EnumTypeDisplay_nameAndId;

    public static String IpsPlugin_infoDefaultTextEditorWasOpened;

    public static String IpsPlugin_titleErrorDialog;

    public static String IpsPlugin_msgUnexpectedError;

    public static String IpsPlugin_languagePackLanguage;

    public static String IpsPlugin_languagePackCountry;

    public static String IpsPlugin_languagePackVariant;

    public static String IpsPlugin_errorNoDatatypeControlFactoryFound;

    public static String IpsPlugin_dialogSaveDirtyEditorMessageMany;

    public static String IpsPlugin_dialogSaveDirtyEditorMessageSimple;

    public static String IpsPlugin_dialogSaveDirtyEditorTitle;

}
