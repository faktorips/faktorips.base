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

package org.faktorips.devtools.core.internal.model.testcasetype;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.testcasetype.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String TestAttribute_Error_AttributeAndDatatypeGiven;
    public static String TestAttribute_TestAttribute_Error_InvalidTestAttributeName;
    public static String TestAttribute_TestAttribute_Error_NameIsEmpty;
    public static String TestAttribute_ValidationWarning_DerivedOnTheFlyAttributesAreNotSupported;
    public static String TestAttribute_Error_TypeNotAllowed;
    public static String TestAttribute_Error_TypeNotAllowedIfParent;
    public static String TestAttribute_Error_AttributeNotFound;
    public static String TestAttribute_Error_DuplicateName;
    public static String TestAttribute_ValidationError_DuplicateAttributeAndType;

    public static String TestPolicyCmptTypeParameter_ValidationError_FlagRequiresIsTrueButPolicyCmptTypeIsNotConfByProduct;
    public static String TestPolicyCmptTypeParameter_ValidationWarning_AccosiationTargetNotInTestCaseType;
    public static String TestPolicyCmptTypeParameter_ValidationError_PolicyCmptTypeNotExists;
    public static String TestPolicyCmptTypeParameter_ValidationError_MinGreaterThanMax;
    public static String TestPolicyCmptTypeParameter_ValidationError_MaxLessThanMin;
    public static String TestPolicyCmptTypeParameter_ValidationError_TypeNotAllowed;
    public static String TestPolicyCmptTypeParameter_ValidationError_AssociationNotExists;
    public static String TestPolicyCmptTypeParameter_ValidationError_TargetOfAssociationNotExists;
    public static String TestPolicyCmptTypeParameter_ValidationError_PolicyCmptNotAllowedForAssociation;
    public static String TestPolicyCmptTypeParameter_ValidationError_MustRequireProdCmptIfRootAndAbstract;

    public static String TestValueParameter_ValidateError_ValueDatatypeNotFound;
    public static String TestValueParameter_ValidationError_TypeNotAllowed;

    public static String TestParameter_ValidationError_DuplicateName;
    public static String TestParameter_ValidateError_InvalidTestParamName;

    public static String TestCaseType_Error_MoreThanOneValueParamWithTypeAndName;
    public static String TestCaseType_Error_MoreThanOnePolicyParamWithTypeAndName;
    public static String TestCaseType_Error_MoreThanOneParamWithTypeAndName;

    public static String TestRuleParameter_ValidationError_WrongParameterType;

}
