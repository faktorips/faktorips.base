/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
    public static String ProductAttributeCondition_conditionName;
    public static String ProductComponentAssociationCondition_association;
    public static String ReferenceSearchOperatorType_labelDoesNotReference;
    public static String ReferenceSearchOperatorType_labelReferences;
    public static String ContainsSearchOperatorType__labelContains;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        //
    }
}
