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

package org.faktorips.devtools.core.internal.model.tablecontents;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.tablecontents.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String Row_FromValueGreaterThanToValue;
    public static String Row_MissingValueForUniqueKey;
    public static String Row_NameMustBeValidJavaIdentifier;
    public static String Row_ValueNotParsable;

    public static String TableContents_msgMissingTablestructure;
    public static String TableContents_msgColumncountMismatch;
    public static String TableContents_msgNameStructureAndContentsNotSameWhenEnum;
    public static String TableContentsGeneration_dublicateEnumId;

    public static String UniqueKeyValidator_msgUniqueKeyViolation;
    public static String UniqueKeyValidatorRange_msgToManyUniqueKeyViolations;

}
