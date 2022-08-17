/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.model;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.search.model.messages"; //$NON-NLS-1$
    public static String ModelSearchPage_groupLabelSearchFor;
    public static String ModelSearchPage_labelAssociations;
    public static String ModelSearchPage_labelAttributes;
    public static String ModelSearchPage_labelMethodsAndFormulas;
    public static String ModelSearchPage_labelRules;
    public static String ModelSearchPage_labelSearchTerm;
    public static String ModelSearchPage_labelTableStructureUsage;
    public static String ModelSearchPage_labelTypeName;
    public static String ModelSearchQuery_faktorIpsModelSearchLabel;
    public static String ModelSearchQuery_labelHitSearchTerm;
    public static String ModelSearchQuery_labelHitSearchTermAndTypeName;
    public static String ModelSearchQuery_labelHitsSearchTerm;
    public static String ModelSearchQuery_labelHitsSearchTermAndTypeName;
    public static String ModelSearchQuery_labelHitsTypeName;
    public static String ModelSearchQuery_labelHitTypeName;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        //
    }
}
