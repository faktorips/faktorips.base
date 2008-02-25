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

package org.faktorips.devtools.core.internal.application;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.application.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
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
