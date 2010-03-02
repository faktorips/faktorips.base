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

package org.faktorips.devtools.core.internal.model.type;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.type.messages"; //$NON-NLS-1$

    private Messages() {
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    public static String Association_msg_MaxCardinalityMustBeAtLeast1;
    public static String Association_msg_MaxCardinalityForDerivedUnionTooLow;
    public static String Association_msg_MinCardinalityGreaterThanMaxCardinality;
    public static String Association_msg_TargetRolePlural;
    public static String Association_msg_TargetRoleSingular;
    public static String Association_msg_TargetRoleSingularIlleaglySameAsTargetRolePlural;
    public static String Association_msg_DerivedUnionDoesNotExist;
    public static String Association_msg_NotMarkedAsDerivedUnion;
    public static String Association_msg_TargetOfDerivedUnionDoesNotExist;
    public static String Association_msg_TargetNotSubclass;
    public static String Association_msgDerivedUnionNotSubset;
    public static String Association_msgSubsetOfDerivedUnionSameMaxCardinality;

    public static String Attribute_msg_InvalidAttributeName;
    public static String Attribute_msg_DefaultNotParsable_UnknownDatatype;
    public static String Attribute_msg_DefaultValueIsEmptyString;
    public static String Attribute_msg_ValueTypeMismatch;
    public static String Attribute_msg_DefaultNotInValueset;

    public static String DuplicatePropertyNameValidator_msg;
    public static String Method_duplicateParamName;
    public static String Method_duplicateSignature;
    public static String Method_incompatbileReturnType;

    public static String Type_msg_AbstractMissmatch;
    public static String Type_msg_cycleInTypeHierarchy;
    public static String Type_msg_MustOverrideAbstractMethod;
    public static String Type_msg_MustImplementDerivedUnion;
    public static String Type_msg_MustImplementInverseDerivedUnion;
    public static String Type_msg_supertypeNotFound;
    public static String Type_msg_TypeHierarchyInconsistent;

    public static String Method_msg_NameEmpty;
    public static String Method_msg_InvalidMethodname;
    public static String Method_msg_abstractMethodError;
    public static String Parameter_msg_NameEmpty;
    public static String Parameter_msg_InvalidParameterName;
    public static String Type_msgOtherTypeWithSameQNameInDependentProject;
    public static String Type_msgOtherTypeWithSameQNameInSameProject;

}
