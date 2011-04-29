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

package org.faktorips.devtools.core.ui.editors.pctype.associationwizard;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.pctype.associationwizard.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String AssociationTargetPage_labelTarget;

    public static String AssociationTargetPage_labelType;

    public static String AssociationTargetPage_pageDescription;

    public static String AssociationTargetPage_pageName;

    public static String AssociationTargetPage_pageTitle;

    public static String ConfigureProductCmptTypePage_labelCreateNew;

    public static String ConfigureProductCmptTypePage_labelCreateNone;

    public static String ConfigureProductCmptTypePage_pageDescription;

    public static String ConfigureProductCmptTypePage_pageName;

    public static String ConfigureProductCmptTypePage_pageTitle;

    public static String ConfProdCmptTypePropertyPage_groupProperties;

    public static String ConfProdCmptTypePropertyPage_labelMaximumCardinality;

    public static String ConfProdCmptTypePropertyPage_labelMinimumCardinality;

    public static String ConfProdCmptTypePropertyPage_labelSource;

    public static String ConfProdCmptTypePropertyPage_labelTarget;

    public static String ConfProdCmptTypePropertyPage_labelTargetRolePlural;

    public static String ConfProdCmptTypePropertyPage_labelTargetRoleSingular;

    public static String ConfProdCmptTypePropertyPage_labelType;

    public static String ConfProdCmptTypePropertyPage_pageDescription;

    public static String ConfProdCmptTypePropertyPage_pageName;

    public static String ConfProdCmptTypePropertyPage_pageTitle;

    public static String ErrorPage_pageDescription;

    public static String ErrorPage_pageTitle;

    public static String InverseAssociationPage_labelNewInverseAssociation;

    public static String InverseAssociationPage_labelNoInverseAssociation;

    public static String InverseAssociationPage_labelUseExistiongAssociation;

    public static String InverseAssociationPage_pageDescription;

    public static String InverseAssociationPage_pageName;

    public static String InverseAssociationPage_pageTitle;

    public static String InverseAssociationPropertyPage_labelExistingAssociation;

    public static String InverseAssociationPropertyPage_labelMaximumCardinality;

    public static String InverseAssociationPropertyPage_labelMinimumCardinality;

    public static String InverseAssociationPropertyPage_labelProperties;

    public static String InverseAssociationPropertyPage_labelTarget;

    public static String InverseAssociationPropertyPage_labelTargetRolePlural;

    public static String InverseAssociationPropertyPage_labelTargetRoleSingular;

    public static String InverseAssociationPropertyPage_labelType;

    public static String InverseAssociationPropertyPage_pageDescription;

    public static String InverseAssociationPropertyPage_pageName;

    public static String InverseAssociationPropertyPage_pageTitle;

    public static String NewPcTypeAssociationWizard_wizardTitle;
    public static String NewPcTypeAssociationWizard_warningNoExistingAssociationFound;
    public static String NewPcTypeAssociationWizard_descriptionSelectExistingInverseAssociation;
    public static String NewPcTypeAssociationWizard_descriptionDefineNewInverseAssociation;
    public static String NewPcTypeAssociationWizard_dialogMessageTargetIsDirty;
    public static String NewPcTypeAssociationWizard_dialogTitleTargetIsDirty;
    public static String NewPcTypeAssociationWizard_dialogMessageProductComponentTypeIsDirty;
    public static String NewPcTypeAssociationWizard_dialogTitleProductComponentTypeIsDirty;
    public static String NewPcTypeAssociationWizard_labelDescription;

    public static String PropertyPage_groupProperties;

    public static String PropertyPage_labelMaximumCardinality;

    public static String PropertyPage_labelMinimumCardinality;

    public static String PropertyPage_labelTargetRolePlural;

    public static String PropertyPage_labelTargetRoleSingular;

    public static String PropertyPage_pageDescription;

    public static String PropertyPage_pageName;

    public static String PropertyPage_pageTitle;

}
