/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.application;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.application.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String IpsActionBarAdvisor_file;
    public static String IpsActionBarAdvisor_new;
    public static String IpsActionBarAdvisor_edit;
    public static String IpsActionBarAdvisor_navigate;
    public static String IpsActionBarAdvisor_goto;
    public static String IpsActionBarAdvisor_showIn;
    public static String IpsActionBarAdvisor_project;
    public static String IpsActionBarAdvisor_Window;
    public static String IpsActionBarAdvisor_shortcuts;
    public static String IpsActionBarAdvisor_help;

    public static String IpsWorkbenchAdvisor_title;

    public static String ProblemsSavingWorkspace;

}
