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

package org.faktorips.devtools.core.internal.model.enums.refactor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.enums.refactor.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String RenameEnumAttributeProcessor_processorName;
    public static String RenameEnumLiteralNameAttributeValueProcessor_processorName;

    public static String PullUpEnumAttributeProcessor_processorName;
    public static String PullUpEnumAttributeProcessor_msgEnumTypeHasNoSuperEnumType;
    public static String PullUpEnumAttributeProcessor_msgSuperEnumTypeCannotBeFound;
    public static String PullUpEnumAttributeProcessor_msgLiteralNameAttributeCannotBePulledUp;
    public static String PullUpEnumAttributeProcessor_msgTargetEnumTypeMustBeASupertype;
    public static String PullUpEnumAttributeProcessor_msgEnumAttributeAlreadyExistsInTarget;
    public static String PullUpEnumAttributeProcessor_msgEnumAttributeBaseOfInheritedAttributeNotFound;

}
