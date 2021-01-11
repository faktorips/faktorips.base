/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.type.refactor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.type.refactor.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String RenameAttributeProcessor_processorName;

    public static String RenameAssociationProcessor_processorName;
    public static String RenameAssociationProcessor_msgNewNameMustNotBeEmpty;
    public static String RenameAssociationProcessor_msgEitherNameOrPluralNameMustBeChanged;
    public static String RenameAssociationProcessor_msgNewPluralNameMustNotBeEmptyForToManyAssociations;
    public static String RenameValidationRuleProcessor_processorName;

    public static String PullUpAttributeProcessor_processorName;
    public static String PullUpAttributeProcessor_msgTypeHasNoSupertype;
    public static String PullUpAttributeProcessor_msgSupertypeCouldNotBeFound;
    public static String PullUpAttributeProcessor_msgTargetTypeMustBeSupertype;
    public static String PullUpAttributeProcessor_msgAttributeAlreadyExistingInTargetType;
    public static String PullUpAttributeProcessor_msgBaseOfOverwrittenAttributeNotFound;

}
