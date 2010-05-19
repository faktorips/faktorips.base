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

package org.faktorips.devtools.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.CreateIpsArchiveOperation;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * This is an ant task that can only be used with the eclipse ant runner with an eclipse
 * installation that contains the faktor ips plugin. It create an IPS-Archive file for the specified
 * parameters.
 * 
 * @author Peter Erzberger
 */
public class CreateIpsArchiveTask extends AbstractIpsTask {

    private boolean inclJavaSources = false;
    private boolean inclJavaBinaries = false;
    private File archiveFile;
    private String ipsProjectName;
    private List fragmentRootRepresentations = new ArrayList();

    public CreateIpsArchiveTask() {
        super("CreateIpsArchiveTask");
    }

    /**
     * Starts the CreateIpsArchiveOperation configured with the parameters provided to this ant
     * task.
     * 
     * {@inheritDoc}
     */
    public void executeInternal() throws Exception {

        if (ipsProjectName == null) {
            throw new BuildException("The ipsProjectName needs to be specified.");
        }

        IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(ipsProjectName);
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
            List ipsPackageFragmentRoots = new ArrayList();
            for (Iterator it = fragmentRootRepresentations.iterator(); it.hasNext();) {
                IpsPackageFragmentRoot fragmentRoot = (IpsPackageFragmentRoot)it.next();
                IIpsPackageFragmentRoot root = ipsProject.getIpsPackageFragmentRoot(fragmentRoot.getName());
                if (root == null) {
                    throw new BuildException("The IpsPackageFragmentRoot: " + root.getName() + " of the IpsProject: "
                            + ipsProjectName + " could not be found.");
                }
                ipsPackageFragmentRoots.add(root);
            }
            IIpsPackageFragmentRoot[] roots = (IIpsPackageFragmentRoot[])ipsPackageFragmentRoots
                    .toArray(new IIpsPackageFragmentRoot[fragmentRootRepresentations.size()]);
            CreateIpsArchiveOperation operation = new CreateIpsArchiveOperation(roots, archiveFile);
            operation.setInclJavaBinaries(inclJavaBinaries);
            operation.setInclJavaSources(inclJavaSources);
            operation.run(null);
        }
    }

    /**
     * The file handle to the file that contains the ips archive.
     */
    public File getArchiveFile() {
        return archiveFile;
    }

    /**
     * Sets the file handle to the ips archive file.
     */
    public void setArchiveFile(File archiveFile) {
        this.archiveFile = archiveFile;
    }

    /**
     * Adds a representation for an ips package fragment root. This repesentation contains the name
     * of an ips package fragment root of the ips project specified by the ips project name property
     * of this task.
     */
    public void addFragmentRoot(IpsPackageFragmentRoot fragmentRoot) {
        fragmentRootRepresentations.add(fragmentRoot);
    }

    /**
     * Returns if the java binaries should be included into the ips archive.
     */
    public boolean isInclJavaBinaries() {
        return inclJavaBinaries;
    }

    /**
     * Sets if the java binaries should be included into the ips archive.
     */
    public void setInclJavaBinaries(boolean inclJavaBinaries) {
        this.inclJavaBinaries = inclJavaBinaries;
    }

    /**
     * Returns if the java source files should be included into the ips archive.
     */
    public boolean isInclJavaSources() {
        return inclJavaSources;
    }

    /**
     * Sets if the java source files should be included into the ips archive.
     */
    public void setInclJavaSources(boolean inclJavaSources) {
        this.inclJavaSources = inclJavaSources;
    }

    /**
     * Returns the ips project name for which the ips archive will be created.
     */
    public String getIpsProjectName() {
        return ipsProjectName;
    }

    /**
     * Sets the ips project name for which the ips archive will be created.
     */
    public void setIpsProjectName(String ipsProjectName) {
        this.ipsProjectName = ipsProjectName;
    }

    /**
     * This inner class is used as a data structure by means of which mutliple ips package fragment
     * roots of the ips project can be specified.
     * 
     * @author Peter Erzberger
     */
    public static class IpsPackageFragmentRoot {

        private String name;

        /**
         * Returns the name of the ips package fragment root relative to the ips project.
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the name of the ips package fragment root relative to the ips project.
         */
        public void setName(String name) {
            this.name = name;
        }
    }
}
