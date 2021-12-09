/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsproject;

import org.faktorips.devtools.model.abstraction.AFolder;

/**
 * An object path entry that defines a folder containing Faktor-IPS source files.
 * 
 * @author Jan Ortmann
 */
public interface IIpsSrcFolderEntry extends IIpsObjectPathEntry {

    public static final String MSGCODE_SRCFOLDER_MUST_BE_A_DIRECT_CHILD_OF_THE_PROHECT = "SourceFolder must be a direct child of the project."; //$NON-NLS-1$

    public static final String MSGCODE_OUTPUT_FOLDER_MERGABLE_MISSING = "OutputFolderMergableMissing"; //$NON-NLS-1$

    public static final String MSGCODE_OUTPUT_FOLDER_MERGABLE_DOESNT_EXIST = "OutputFolderMergableDoesntExist"; //$NON-NLS-1$

    public static final String MSGCODE_OUTPUT_FOLDER_DERIVED_MISSING = "OutputFolderDerivedMissing"; //$NON-NLS-1$

    public static final String MSGCODE_OUTPUT_FOLDER_DERIVED_DOESNT_EXIST = "OutputFolderDeriveddDoesntExist"; //$NON-NLS-1$

    public static final String MSGCODE_DUPLICATE_BASE_PACKAGE = "DuplicateBasePackage"; //$NON-NLS-1$

    /**
     * Returns the folder containing the IPS source files.
     */
    public AFolder getSourceFolder();

    /**
     * Returns the output folder for generated but mergeable Java source files. The content of the
     * java files in this folder are supposed to be merged with the newly generated content during
     * every build cycle . If a specific output folder is set for this entry, the specific output
     * folder is returned, otherwise the output folder defined in the object path is returned.
     */
    public AFolder getOutputFolderForMergableJavaFiles();

    /**
     * Returns the entry's own output folder for generated but mergeable Java source files. This
     * output folder is used only for this entry.
     */
    public AFolder getSpecificOutputFolderForMergableJavaFiles();

    /**
     * Sets the entry's output folder for generated but mergeable Java source files.
     */
    public void setSpecificOutputFolderForMergableJavaFiles(AFolder outputFolder);

    /**
     * Returns the name of the base package for generated but mergeable Java source files. If a
     * specific base package name is set for this entry, the specific base package name is returned,
     * otherwise the base package name defined in the object path is returned.
     */
    public String getBasePackageNameForMergableJavaClasses();

    /**
     * Returns a unique base package name for mergeable artifacts. Unique means that this base
     * package name should not be used by any other {@link IIpsObjectPathEntry} in this or any
     * dependent project. The unique base package is the package name returned by
     * {@link #getBasePackageNameForMergableJavaClasses()} concatenated with the unique qualifier. A
     * unique qualifier may be empty if the base package is already unique.
     */
    public String getUniqueBasePackageNameForMergableArtifacts();

    /**
     * Returns partial TOC resource name. The fully qualified TOC resource name is obtained by
     * adding this partial name to the base package name for generated Java classes.
     * 
     * @see #getBasePackageNameForMergableJavaClasses()
     */
    public String getBasePackageRelativeTocPath();

    /**
     * Sets the partial TOC resource name.
     * 
     * @see #getBasePackageRelativeTocPath()
     */
    public void setBasePackageRelativeTocPath(String newName);

    /**
     * Returns the name of the entry's own base package for generated but mergeable Java source
     * files.
     */
    public String getSpecificBasePackageNameForMergableJavaClasses();

    /**
     * Sets the name of entry's own base package for generated but mergeable Java source files.
     */
    public void setSpecificBasePackageNameForMergableJavaClasses(String name);

    /**
     * Returns the output folder containing generated derived Java source files. If a specific
     * output folder is set for this entry, the specific output folder is returned, otherwise the
     * output folder defined in the object path is returned.
     */
    public AFolder getOutputFolderForDerivedJavaFiles();

    /**
     * Returns the entry's own output folder containing generated derived Java source files. This
     * output folder is used only for this entry.
     */
    public AFolder getSpecificOutputFolderForDerivedJavaFiles();

    /**
     * Sets the entry's output folder containing generated derived Java source files. This output
     * folder is used only for this entry.
     */
    public void setSpecificOutputFolderForDerivedJavaFiles(AFolder outputFolder);

    /**
     * Returns the name of the base package for the generated derived Java source files. If a
     * specific base package name is set for this entry, the specific base package name is returned,
     * otherwise the base package name defined in the object path is returned.
     */
    public String getBasePackageNameForDerivedJavaClasses();

    /**
     * Returns the name of the entry's own base package for generated derived Java source files. All
     * generated Java types are contained in this package or one of the child packages.
     */
    public String getSpecificBasePackageNameForDerivedJavaClasses();

    /**
     * Sets the name of the entry's own base package for the generated derived Java source files.
     * All generated Java types are contained in this package or one of the child packages.
     */
    public void setSpecificBasePackageNameForDerivedJavaClasses(String name);

    /**
     * Returns a unique base package name for derived artifacts. Unique means that this base package
     * name should not be used by any other {@link IIpsObjectPathEntry} in this or any dependent
     * project. The unique base package is the package name returned by
     * {@link #getBasePackageNameForDerivedJavaClasses()} concatenated with the unique qualifier. A
     * unique qualifier may be empty if the base package is already unique.
     */
    public String getUniqueBasePackageNameForDerivedArtifacts();

    /**
     * @param validationMessagesBundle The validationMessagesBundle to set.
     */
    public void setValidationMessagesBundle(String validationMessagesBundle);

    /**
     * @return Returns the validationMessagesBundle.
     */
    public String getValidationMessagesBundle();

    /**
     * Returns the unique qualifier that is used to get unique base packages for source entries with
     * same base package.
     * 
     * @return The specified unique qualifier name
     * @see #getUniqueBasePackageNameForMergableArtifacts()
     * @see #getUniqueBasePackageNameForDerivedArtifacts()
     */
    public String getUniqueQualifier();

    /**
     * Specify the unique qualifier to get unique base packages for source entries with same base
     * package.
     * 
     * @param uniqueQualifier A unique qualifier
     * @see #getUniqueBasePackageNameForMergableArtifacts()
     * @see #getUniqueBasePackageNameForDerivedArtifacts()
     */
    public void setUniqueQualifier(String uniqueQualifier);

}
