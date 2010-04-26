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

package org.faktorips.devtools.core.model.ipsobject;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.model.ipsobject.messages"; //$NON-NLS-1$

    private Messages() {

    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    public static String IpsObjectType_nameEnumContent;

    public static String IpsObjectType_nameEnumType;

    public static String IpsObjectType_nameBusinessFunction;

    public static String IpsObjectType_namePolicyClass;

    public static String IpsObjectType_nameProductClass;

    public static String IpsObjectType_nameTableStructure;

    public static String IpsObjectType_nameProductComponent;

    public static String IpsObjectType_nameTableContents;

    public static String IpsObjectType_nameTestCaseType;

    public static String IpsObjectType_nameTestCase;

    public static String IpsObjectType_nameEnumContentPlural;

    public static String IpsObjectType_nameEnumTypePlural;

    public static String IpsObjectType_nameBusinessFunctionPlural;

    public static String IpsObjectType_namePolicyClassPlural;

    public static String IpsObjectType_nameProductClassPlural;

    public static String IpsObjectType_nameTableStructurePlural;

    public static String IpsObjectType_nameProductComponentPlural;

    public static String IpsObjectType_nameTableContentsPlural;

    public static String IpsObjectType_nameTestCaseTypePlural;

    public static String IpsObjectType_nameTestCasePlural;

}
