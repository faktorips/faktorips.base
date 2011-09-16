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

package org.faktorips.devtools.core.ui.search.product;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.search.product.messages"; //$NON-NLS-1$
    public static String ProductSearchConditionsTableViewerProvider_argument;
    public static String ProductSearchConditionsTableViewerProvider_conditionType;
    public static String ProductSearchConditionsTableViewerProvider_element;
    public static String ProductSearchConditionsTableViewerProvider_operator;
    public static String ProductSearchPage_labelAddConditionButton;
    public static String ProductSearchPage_labelChooseProductComponentType;
    public static String ProductSearchPage_labelProductComponent;
    public static String ProductSearchPage_labelProductComponentType;
    public static String ProductSearchPage_labelSearchConditions;
    public static String ProductSearchQuery_1;
    public static String ProductSearchQuery_faktorIpsProductSearchLabel;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        //
    }
}
