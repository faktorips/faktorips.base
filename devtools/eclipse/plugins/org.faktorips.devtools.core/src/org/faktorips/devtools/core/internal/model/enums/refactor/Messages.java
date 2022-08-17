/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
