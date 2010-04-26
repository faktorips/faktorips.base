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

package org.faktorips.devtools.core.ui.wizards.ipspackage;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.ipspackage.messages"; //$NON-NLS-1$

    private Messages() {
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    public static String IpsPackagePage_msgNew;
    public static String IpsPackagePage_labelSrcFolder;
    public static String IpsPackagePage_labelName;
    public static String IpsPackagePage_msgRootMissing;
    public static String IpsPackagePage_msgRootNoIPSSrcFolder;
    public static String IpsPackagePage_msgPackageMissing;
    public static String IpsPackagePage_msgEmptyName;
    public static String IpsPackagePage_msgRootRequired;
    public static String IpsPackagePage_msgSelectSourceFolder;
    public static String IpsPackagePage_title;
    public static String IpsPackagePage_PackageNameMustNotContainBlanks;
    public static String IpsPackagePage_InvalidPackageName;
    public static String IpsPackagePage_PackageAllreadyExists;
}
