/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search.product.conditions.types;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.search.product.conditions.types.messages"; //$NON-NLS-1$
    public static String AllowanceSearchOperatorType_allowed;
    public static String AllowanceSearchOperatorType_notAllowed;
    public static String ComparableSearchOperatorType_labelGreater;
    public static String ComparableSearchOperatorType_labelGreaterOrEqual;
    public static String ComparableSearchOperatorType_labelLess;
    public static String ComparableSearchOperatorType_labelLessOrEqual;
    public static String EqualitySearchOperatorType_equals;
    public static String EqualitySearchOperatorType_notEquals;
    public static String LikeSearchOperatorType_labelDoesNotLike;
    public static String LikeSearchOperatorType_labelLike;
    public static String PolicyAttributeCondition_conditionName;
    public static String PolicyAttributeCondition_noSearchableElementMessage;
    public static String ProductAttributeCondition_conditionName;
    public static String ProductAttributeCondition_noSearchableElementMessage;
    public static String ProductComponentAssociationCondition_association;
    public static String ProductComponentAssociationCondition_messageNoElement;
    public static String ReferenceSearchOperatorType_labelDoesNotReference;
    public static String ReferenceSearchOperatorType_labelReferences;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        //
    }
}
