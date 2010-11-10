/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.pages.elements.types;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.htmlexport.pages.elements.types.messages"; //$NON-NLS-1$
    public static String AssociationTablePageElement_headlineAggregationKind;
    public static String AssociationTablePageElement_headlineAssociationType;
    public static String AssociationTablePageElement_headlineDerivedUnion;
    public static String AssociationTablePageElement_headlineDescription;
    public static String AssociationTablePageElement_headlineLabel;
    public static String AssociationTablePageElement_headlineMaxCardinality;
    public static String AssociationTablePageElement_headlineMinCardinality;
    public static String AssociationTablePageElement_headlineName;
    public static String AssociationTablePageElement_headlineQualified;
    public static String AssociationTablePageElement_headlineSubsettedDerivedUnion;
    public static String AssociationTablePageElement_headlineTarget;
    public static String AssociationTablePageElement_headlineTargetRolePlural;
    public static String AssociationTablePageElement_headlineTargetRoleSingular;
    public static String AttributesTablePageElement_headlineDatatype;
    public static String AttributesTablePageElement_headlineDefaultValue;
    public static String AttributesTablePageElement_headlineDescription;
    public static String AttributesTablePageElement_headlineLabel;
    public static String AttributesTablePageElement_headlineModifier;
    public static String AttributesTablePageElement_headlineName;
    public static String IpsObjectListPageElement_allObjects;
    public static String IpsObjectListPageElement_objects;
    public static String IpsPackagesListPageElement_allPackages;
    public static String IpsPackagesListPageElement_packages;
    public static String KeyValueTablePageElement_headlineProperty;
    public static String KeyValueTablePageElement_headlineValue;
    public static String KeyValueTablePageElement_justEvenNumberOfPageElementsAllowed;
    public static String MessageListTablePageElement_error;
    public static String MessageListTablePageElement_headlineCode;
    public static String MessageListTablePageElement_headlineMessage;
    public static String MessageListTablePageElement_headlineProperties;
    public static String MessageListTablePageElement_headlineSeverity;
    public static String MessageListTablePageElement_info;
    public static String MessageListTablePageElement_severity;
    public static String MessageListTablePageElement_warning;
    public static String MethodsTablePageElement_headlineAbstract;
    public static String MethodsTablePageElement_headlineDatatype;
    public static String MethodsTablePageElement_headlineDescription;
    public static String MethodsTablePageElement_headlineModifier;
    public static String MethodsTablePageElement_headlineName;
    public static String MethodsTablePageElement_headlineSignature;
    public static String ProductGenerationAttributeTable_associatedComponents;
    public static String ProductGenerationAttributeTable_generationFrom;
    public static String ProductGenerationAttributeTable_undefined;
    public static String ValidationRuleTablePageElement_headlineDescription;
    public static String ValidationRuleTablePageElement_headlineMessageCode;
    public static String ValidationRuleTablePageElement_headlineMessageSeverity;
    public static String ValidationRuleTablePageElement_headlineMessageText;
    public static String ValidationRuleTablePageElement_headlineName;
    public static String ValidationRuleTablePageElement_headlineValidatedAttributes;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        //
    }
}
