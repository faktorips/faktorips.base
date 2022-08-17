/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.faktorips.devtools.model.CreateIpsArchiveOperation;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * This is an Ant task that can only be used with the Eclipse Ant runner with an Eclipse
 * installation that contains the Faktor-IPS plugin. It create an IPS-Archive file for the specified
 * parameters.
 */
public class CreateIpsArchiveTask extends AbstractIpsTask {

    private boolean inclJavaSources = false;
    private boolean inclJavaBinaries = false;
    private File archiveFile;
    private String ipsProjectName;
    private List<IIpsPackageFragmentRoot> fragmentRootRepresentations = new ArrayList<>();

    public CreateIpsArchiveTask() {
        super("CreateIpsArchiveTask");
    }

    /**
     * Starts the {@link CreateIpsArchiveOperation} configured with the parameters provided to this
     * Ant task.
     */
    @Override
    public void executeInternal() throws Exception {

        if (ipsProjectName == null) {
            throw new BuildException("The ipsProjectName needs to be specified.");
        }

        IIpsProject ipsProject = IIpsModel.get().getIpsProject(ipsProjectName);
        if (ipsProject == null) {
            throw new BuildException("No IpsProject found for the specified name: " + ipsProjectName);
        }
        if (fragmentRootRepresentations.isEmpty()) {
            System.out.println("creating archive for project: " + ipsProject.getName());
            CreateIpsArchiveOperation operation = new CreateIpsArchiveOperation(ipsProject, archiveFile);
            operation.setInclJavaBinaries(inclJavaBinaries);
            operation.setInclJavaSources(inclJavaSources);
            operation.run(null);
        } else {
            List<IIpsPackageFragmentRoot> ipsPackageFragmentRoots = new ArrayList<>();
            for (IIpsPackageFragmentRoot fragmentRoot : fragmentRootRepresentations) {
                IIpsPackageFragmentRoot root = ipsProject.getIpsPackageFragmentRoot(fragmentRoot.getName());
                if (root == null) {
                    throw new BuildException("The IpsPackageFragmentRoot: " + fragmentRoot.getName()
                            + " of the IpsProject: " + ipsProjectName + " could not be found.");
                }
                ipsPackageFragmentRoots.add(root);
            }
            IIpsPackageFragmentRoot[] roots = ipsPackageFragmentRoots
                    .toArray(new IIpsPackageFragmentRoot[fragmentRootRepresentations.size()]);
            CreateIpsArchiveOperation operation = new CreateIpsArchiveOperation(roots, archiveFile);
            operation.setInclJavaBinaries(inclJavaBinaries);
            operation.setInclJavaSources(inclJavaSources);
            operation.run(null);
        }
    }

    /**
     * The file handle to the file that contains the IPS archive.
     */
    public File getArchiveFile() {
        return archiveFile;
    }

    /**
     * Sets the file handle to the IPS archive file.
     */
    public void setArchiveFile(File archiveFile) {
        this.archiveFile = archiveFile;
    }

    /**
     * Adds a representation for an IPS package fragment root. This representation contains the name
     * of an IPS package fragment root of the IPS project specified by the IPS project name property
     * of this task.
     */
    public void addFragmentRoot(IIpsPackageFragmentRoot fragmentRoot) {
        fragmentRootRepresentations.add(fragmentRoot);
    }

    /**
     * Returns whether the Java binaries should be included into the IPS archive.
     */
    public boolean isInclJavaBinaries() {
        return inclJavaBinaries;
    }

    /**
     * Sets whether the java binaries should be included into the IPS archive.
     */
    public void setInclJavaBinaries(boolean inclJavaBinaries) {
        this.inclJavaBinaries = inclJavaBinaries;
    }

    /**
     * Returns whether the Java source files should be included into the IPS archive.
     */
    public boolean isInclJavaSources() {
        return inclJavaSources;
    }

    /**
     * Sets whether the Java source files should be included into the IPS archive.
     */
    public void setInclJavaSources(boolean inclJavaSources) {
        this.inclJavaSources = inclJavaSources;
    }

    /**
     * Returns the IPS project name for which the IPS archive will be created.
     */
    public String getIpsProjectName() {
        return ipsProjectName;
    }

    /**
     * Sets the IPS project name for which the IPS archive will be created.
     */
    public void setIpsProjectName(String ipsProjectName) {
        this.ipsProjectName = ipsProjectName;
    }

    /**
     * This inner class is used as a data structure by means of which mutliple IPS package fragment
     * roots of the IPS project can be specified.
     */
    public static class IpsPackageFragmentRoot {

        private String name;

        /**
         * Returns the name of the IPS package fragment root relative to the IPS project.
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the name of the IPS package fragment root relative to the IPS project.
         */
        public void setName(String name) {
            this.name = name;
        }
    }
}
