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

package org.faktorips.devtools.core.ui.views.modelexplorer;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.views.modelexplorer.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String ModelExplorer_menuItemMove;
    public static String ModelExplorer_menuShowIpsProjectsOnly_Title;
    public static String ModelExplorer_menuShowIpsProjectsOnly_Tooltip;
    public static String ModelExplorer_menuGroupCategories_Title;
    public static String ModelExplorer_menuGroupCategories_Tooltip;
    public static String ModelExplorer_submenuNew;
    public static String ModelExplorer_submenuLayout;
    public static String ModelExplorer_actionFlatLayout;
    public static String ModelExplorer_actionHierarchicalLayout;
    public static String ModelExplorer_submenuRefactor;
    public static String ModelExplorer_errorTitle;
    public static String ModelExplorer_defaultPackageLabel;
    public static String ModelExplorer_nonIpsProjectLabel;
    public static String ModelLabelProvider_noProductDefinitionProjectLabel;
    public static String OpenActionGroup_openWithMenuLabel;

}
