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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    public static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.productcmpttype.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String ProductCmptType_DuplicateFormulaName;
    public static String ProductCmptType_iconFileCannotBeResolved;
    public static String ProductCmptType_InconsistentTypeHierarchies;
    public static String ProductCmptType_msgDuplicateFormulasNotAllowedInSameType;
    public static String ProductCmptType_msgOverloadedFormulaMethodCannotBeOverridden;
    public static String ProductCmptType_msgProductCmptTypeAbstractWhenPolicyCmptTypeAbstract;
    public static String ProductCmptType_multiplePropertyNames;
    public static String ProductCmptType_MustInheritFromASupertype;
    public static String ProductCmptType_notMarkedAsConfigurable;
    public static String ProductCmptType_PolicyCmptTypeDoesNotExist;
    public static String ProductCmptType_policyCmptTypeDoesNotSpecifyThisType;
    public static String ProductCmptType_TypeMustConfigureAPolicyCmptTypeIfSupertypeDoes;
    public static String ProductCmptType_caption;
    public static String ProductCmptTypeAssociation_error_MatchingAssociationDoesNotReferenceThis;
    public static String ProductCmptTypeAssociation_error_MatchingAssociationDuplicateName;
    public static String ProductCmptTypeAssociation_error_MatchingAssociationInvalid;
    public static String ProductCmptTypeAssociation_error_matchingAssociationNotFound;

    public static String ProductCmptTypeMethod_FormulaNameIsMissing;
    public static String ProductCmptTypeMethod_FormulaSignatureDatatypeMustBeAValueDatatype;
    public static String ProductCmptTypeMethod_FormulaSignatureMustntBeAbstract;
    public static String ProductCmptTypeMethod_msgNoOverloadableFormulaInSupertypeHierarchy;

    public static String TableStructureUsage_msgAtLeastOneStructureMustBeReferenced;
    public static String TableStructureUsage_msgInvalidRoleName;
    public static String TableStructureUsage_msgRoleNameAlreadyInSupertype;
    public static String TableStructureUsage_msgSameRoleName;
    public static String TableStructureUsage_msgTableStructureNotExists;

    public static String ProductCmptPropertyExternalReference_msgReferencedPropertyCouldNotBeFound;

}
