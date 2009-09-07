/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsproject;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.ipsproject.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String DefaultIpsProjectNamingConventions_error;

    public static String DefaultIpsProjectNamingConventions_msgMissingName;

    public static String DefaultIpsProjectNamingConventions_msgMissingNameForProductCmpt;

    public static String DefaultIpsProjectNamingConventions_msgNameIdDiscouraged;

    public static String DefaultIpsProjectNamingConventions_msgNameMustNotBeQualified;

    public static String DefaultIpsProjectNamingConventions_msgNameNotValid;

    public static String DefaultIpsProjectNamingConventions_msgNameNotValidForProductCmpt;

    public static String DefaultIpsProjectNamingConventions_warning;

    public static String IpsObjectPath_msgOutputFolderDerivedMissing;

    public static String IpsObjectPath_msgOutputFolderMergableMissing;

    public static String IpsObjectPath_srcfolderentrymissing;

    public static String IpsPackageFragmentArbitrarySortDefinition_CommentLine;

    public static String IpsProject_javaProjectHasInvalidBuildPath;

    public static String IpsProject_msgCycleInIpsObjectPath;

    public static String IpsProjectRefEntry_noReferencedProjectSpecified;

    public static String IpsSrcFolderEntry_outputfolderdoesntexist;

    public static String IpsSrcFolderEntry_outputfoldermergablesrcmissing;

    public static String IpsSrcFolderEntry_outputfoldersrcderivedmissing;

    public static String IpsSrcFolderEntry_srcFolderMustBeADirectChildOfTheProject;

	public static String IpsProjectProperties_ENUM_QUESTION_ASSIGNED_USERGROUP_BUSINESS;

    public static String IpsProjectProperties_ENUM_QUESTION_ASSIGNED_USERGROUP_COOPERATE;

    public static String IpsProjectProperties_ENUM_QUESTION_ASSIGNED_USERGROUP_IMPLEMENTATION;

    public static String IpsProjectProperties_ENUM_QUESTION_ASSIGNED_USERGROUP_UNDEFINED;

    public static String IpsProjectProperties_ENUM_QUESTION_STATUS_CLOSED;

    public static String IpsProjectProperties_ENUM_QUESTION_STATUS_DEFERRED;

    public static String IpsProjectProperties_ENUM_QUESTION_STATUS_OPEN;

    public static String IpsProjectProperties_msgUnknownDatatype;

	public static String IpsProjectProperties_msgUnknownBuilderSetId;

	public static String IpsProject_msgMissingDotIpsprojectFile;

	public static String IpsProject_msgUnparsableDotIpsprojectFile;

    public static String IpsProject_msgNoFeatureManager;

    public static String IpsProject_msgVersionTooLow;

    public static String IpsProject_msgIncompatibleVersions;

    public static String IpsProject_msgInvalidMigrationInformation;

    public static String IpsProjectRefEntry_msgMissingReferencedProject;

    public static String IpsSrcFolderEntry_msgMissingFolder;

    public static String IpsProject_msgDuplicateTocFilePath;

    public static String IpsArchiveEntry_archiveDoesNotExist;

    public static String IpsProjectProperties_msgMissingMinFeatureId;

    public static String IpsProject_msgRuntimeIDCollision;
    
}
