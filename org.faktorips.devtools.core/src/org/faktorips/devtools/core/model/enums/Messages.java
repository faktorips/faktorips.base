/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.enums;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.model.enums.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String EnumType_SupertypeDoesNotExist;
    public static String EnumType_SupertypeIsNotAbstract;
    public static String EnumType_cycleDetected;
    public static String EnumType_inconsistentHierarchy;
    public static String EnumType_EnumContentNameEmpty;

    public static String EnumContent_EnumTypeMissing;
    public static String EnumContent_EnumTypeDoesNotExist;
    public static String EnumContent_EnumTypeIsAbstract;
    public static String EnumContent_ValuesArePartOfType;
    public static String EnumContent_EnumContentNameNotCorrect;

}
