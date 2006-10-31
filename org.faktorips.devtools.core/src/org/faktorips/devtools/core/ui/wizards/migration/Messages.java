/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.migration;

import org.eclipse.osgi.util.NLS;

/**
 * @author Joerg Ortmann
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.migration.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

    public static String ProjectSelectionPage_titleSelectProjects;
    public static String MigrationPage_titleMigrationOperations;
    public static String MigrationPage_labelHeader;
    public static String MigrationPage_labelError;
    public static String MigrationPage_labelMissingProjects;
    public static String MigrationWizard_title;
    public static String MigrationWizard_titleAbortion;
    public static String MigrationWizard_msgAbortion;
    public static String MigrationWizard_titleError;
    public static String MigrationWizard_msgError;
    public static String ProjectSelectionPage_msgNoProjects;
    public static String ProjectSelectionPage_msgSelectProjects;
    public static String OpenMigrationWizardAction_titleCantMigrate;
    public static String OpenMigrationWizardAction_msgCantMigrate;
    public static String MigrationPage_msgShortDescription;
    public static String MigrationPage_titleProject;

}
