/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.bf;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.model.bf.messages"; //$NON-NLS-1$

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    public static String BFElementType_bfCallAction;
    public static String BFElementType_decision;
    public static String BFElementType_end;
    public static String BFElementType_inlineAction;
    public static String BFElementType_merge;
    public static String BFElementType_methodCallAction;
    public static String BFElementType_methodCallDecision;
    public static String BFElementType_parameter;
    public static String BFElementType_start;
    public static String BusinessFunctionIpsObjectType_displayName;
    public static String BusinessFunctionIpsObjectType_displayNamePlural;

}
