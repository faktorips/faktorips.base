/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
    public static String ConstrainableAssociationWizard_title;
    public static String ConstrainableAssociationWizard_labelNoAssociation;
    public static String ConstrainableAssociationWizard_labelSelectAssociation;
    public static String ConstrainableAssociationWizard_labelSelectionTarget;
    public static String CreateConstrainingAssociationOperation_argumentsMustNotBeNull;
    public static String CreateConstrainingAssociationOperation_sourceAndTargetTypeMustBeOfSameClass;
    public static String CreateConstrainingAssociationOperation_sourceTypeAndAssociationClassMustMatch;
    public static String CreateConstrainingAssociationOperation_targetTypeMustBeSubclassOfTheConstrainedAssociationTarget;

}
