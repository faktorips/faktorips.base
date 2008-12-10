/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelexplorer;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.views.modelexplorer.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String ModelExplorer_menuItemMove;
    public static String ModelExplorer_menuShowIpsProjectsOnly_Title;
    public static String ModelExplorer_menuShowIpsProjectsOnly_Tooltip;
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
