/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import java.text.MessageFormat;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.model.internal.ipsproject.messages"; //$NON-NLS-1$

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
    public static String IpsProject_msgUnparsableDotIpsprojectFile;
    public static String IpsProject_msgNoFeatureManager;
    public static String IpsProject_msgVersionTooLow;
    public static String IpsProject_msgIncompatibleVersions;
    public static String IpsProject_msgInvalidMigrationInformation;
    public static String IpsProject_msgDuplicateTocFilePath;
    public static String IpsProject_msgRuntimeIDCollision;
    public static String IpsProject_msgMissingDotIpsprojectFile;

    public static String IpsProjectProperties_msgMissingMinFeatureId;

    public static String IpsArchiveEntry_archiveDoesNotExist;
    public static String IpsArchiveEntry_archiveIsInvalid;

    public static String IpsArtefactBuilderSetInfo_propertyInompatibleJDK;
    public static String IpsArtefactBuilderSetInfo_propertyNotSupported;
    public static String IpsBuilderSetPropertyDef_NonParsableValue;
    public static String IpsContainer4JdtClasspathContainer_err_invalidClasspathContainer;
    public static String IpsContainerEntry_err_invalidConainerEntry;
    public static String IpsProjectRefEntry_noReferencedProjectSpecified;
    public static String IpsProjectRefEntry_msgMissingReferencedProject;

    public static String IpsSrcFolderEntry_error_duplicateDerivedBasePackage;
    public static String IpsSrcFolderEntry_error_duplicateMergableBasePackage;
    public static String IpsSrcFolderEntry_outputfolderdoesntexist;
    public static String IpsSrcFolderEntry_outputfoldermergablesrcmissing;
    public static String IpsSrcFolderEntry_outputfoldersrcderivedmissing;
    public static String IpsSrcFolderEntry_srcFolderMustBeADirectChildOfTheProject;
    public static String IpsSrcFolderEntry_msgMissingFolder;

    public static String IpsTestRunner_validationErrorInvalidName;

    public static String IpsProjectProperties_err_versionOrVersionProvider;
    public static String IpsProjectProperties_error_persistenceAndSharedAssociationNotAllowed;
    public static String IpsProjectProperties_msgUnknownDatatype;
    public static String IpsProjectProperties_msgUnknownBuilderSetId;
    public static String IpsProjectProperties_msgSupportedLanguageUnknownLocale;
    public static String IpsProjectProperties_msgMoreThanOneDefaultLanguage;
    public static String IpsProjectProperties_unknownNamingStrategy;
    public static String IpsProjectProperties_unknownMarkerEnums;
    public static String IpsProjectProperties_msgExtensibleMarkerEnumsNotAllowed;
    public static String IpsProjectProperties_msgAbstractMarkerEnumsNotAllowed;
    public static String IpsProjectProperties_msgUniqueMarkerIds;
    public static String IpsProjectProperties_msgUnknownFeatureIdForConfiguration;
    public static String VersionProviderExtensionPoint_error_invalidVersionProvider;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String bind(String message, String... bindings) {
        return MessageFormat.format(message, (Object[])bindings);
    }

}
