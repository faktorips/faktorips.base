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

package org.faktorips.devtools.core.ui.views.productdefinitionexplorer;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.views.productdefinitionexplorer.messages"; //$NON-NLS-1$

    private Messages(){}

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    
    public static String ProductExplorer_submenuTeam;
    public static String ProductExplorer_actionCommit;
    public static String ProductExplorer_actionUpdate;
    public static String ProductExplorer_actionReplace;
    public static String ProductExplorer_actionAdd;
    public static String ProductExplorer_actionShowHistory;
    public static String ProductExplorer_CompareWithMenu_CompareWith;
    public static String ProductExplorer_CompareWithMenu_Repository;
    public static String ProductExplorer_CompareWithMenu_Revision;


}
