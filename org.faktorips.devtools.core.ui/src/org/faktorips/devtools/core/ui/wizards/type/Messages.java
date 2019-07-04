/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.type;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.type.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String NewTypePage_check_abstract;
    public static String NewTypePage_msgNameConflicts;
    public static String NewTypePage_msgSupertypeDoesNotExist;
    public static String NewTypePage_superclass;
    public static String ConstrainableAssociationSelectionPage_Message_NoOverridableAssociationsAvailable;
    public static String ConstrainableAssociationWizard_title;
    public static String ConstrainableAssociationWizard_labelNoAssociation;
    public static String ConstrainableAssociationWizard_labelSelectAssociation;
    public static String ConstrainableAssociationWizard_labelSelectionTarget;
    public static String CreateConstrainingAssociationOperation_argumentsMustNotBeNull;
    public static String CreateConstrainingAssociationOperation_sourceAndTargetTypeMustBeOfSameClass;
    public static String CreateConstrainingAssociationOperation_sourceTypeAndAssociationClassMustMatch;
    public static String CreateConstrainingAssociationOperation_targetTypeMustBeSubclassOfTheConstrainedAssociationTarget;

}
