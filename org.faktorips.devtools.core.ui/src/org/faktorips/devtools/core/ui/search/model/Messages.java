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

package org.faktorips.devtools.core.ui.search.model;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.search.model.messages"; //$NON-NLS-1$
    public static String ModelSearchPage_8;
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
    public static String ModelSearchQuery_okStatus;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
