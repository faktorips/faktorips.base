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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.tools.ant.BuildException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.launching.AbstractVMInstall;
import org.eclipse.jdt.launching.AbstractVMInstallType;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMStandin;

/**
 * Implements a custom Ant task, which configures a JDK installation found in a given path in the
 * running Eclipse workspace so that projects relying on that JDK's version can be build against
 * that JDK instead of the JDK with which the Eclipse was started.
 */
public class ConfigureJdkTask extends AbstractIpsTask {

    /**
     * The path to the JDK directory.
     */
    private String jdkDir = "";

    public ConfigureJdkTask() {
        super("ConfigureJdkTask");
    }

    /**
     * Sets the Ant attribute which describes the location of the JDK to configure.
     * 
     * @param dir path to the JDK as String
     */
    public void setDir(String dir) {
        this.jdkDir = dir;
    }

    /**
     * Returns the path of the JDK to configure as String
     * 
     * @return Path as String
     */
    public String getDir() {
        return this.jdkDir;
    }

    /**
     * Executes the Ant task.
     */
    @Override
    public void executeInternal() throws Exception {
        checkDir();
        IVMInstall vm = findVM();
        System.out.println("Found VM " + toString(vm));
        if (vm instanceof VMStandin) {
            ((VMStandin)vm).convertToRealVM();
        }
    }

    private String toString(IVMInstall vm) {
        return vm.getName() + "(" + vm.getId() + ")[" + vm.getInstallLocation() + "]";
    }

    /**
     * Does some checks on the provided directory attribute.
     *
     * @return the directory as a {@link File}.
     * 
     * @throws BuildException
     */
    private File checkDir() {

        if (this.getDir() == null || "".equals(this.getDir())) {
            throw new BuildException("Please provide the 'dir' attribute.");
        }

        File dir = new File(this.getDir());
        if (!dir.exists()) {
            throw new BuildException("Directory " + this.getDir() + " doesn't exist.");
        }

        if (!dir.isDirectory()) {
            throw new BuildException("Provided 'dir' " + this.getDir() + " is not a Directory.");
        }

        if (!dir.canRead()) {
            throw new BuildException("Provided 'dir' " + this.getDir() + " is not readable.");
        }
        return dir;
    }

    private IVMInstall findVM() {
        // inspired by InstalledJREsBlock#search
        final File rootDir = checkDir();
        final List<File> locations = new ArrayList<>();
        final List<IVMInstallType> types = new ArrayList<>();
        final Set<File> exstingLocations = new HashSet<>();

        searchJDK(rootDir, locations, types, exstingLocations, new NullProgressMonitor());

        if (locations.isEmpty()) {
            throw new BuildException("Directory " + this.getDir() + " doesn't contain a JVM.");
        } else {
            Iterator<IVMInstallType> iter2 = types.iterator();
            for (File location : locations) {
                IVMInstallType type = iter2.next();
                AbstractVMInstall vm = new VMStandin(type, UUID.randomUUID().toString());
                String name = location.getName();
                vm.setName(name);
                vm.setInstallLocation(location);
                if (type instanceof AbstractVMInstallType) {
                    // set default java doc location
                    AbstractVMInstallType abs = (AbstractVMInstallType)type;
                    vm.setJavadocLocation(abs.getDefaultJavadocLocation(location));
                    vm.setVMArgs(abs.getDefaultVMArguments(location));
                }
                return vm;
            }
        }
        throw new BuildException("Directory " + this.getDir() + " doesn't contain a JVM.");
    }

    /**
     * Searches the specified directory recursively for installed VMs, adding each detected VM to
     * the <code>found</code> list. Any directories specified in the <code>ignore</code> are not
     * traversed.
     *
     * @param directory
     * @param found
     * @param types
     * @param ignore
     */
    // CSOFF: CyclomaticComplexity
    private static void searchJDK(File directory,
            List<File> found,
            List<IVMInstallType> types,
            Set<File> ignore,
            IProgressMonitor monitor) {
        String[] names = directory.list();
        if (names == null) {
            return;
        }
        List<File> subDirs = new ArrayList<>();
        for (String name : names) {
            if (monitor.isCanceled()) {
                return;
            }
            File file = new File(directory, name);
            IVMInstallType[] vmTypes = JavaRuntime.getVMInstallTypes();
            if (file.isDirectory()) {
                if (!ignore.contains(file) && !"jre".equalsIgnoreCase(name)) {
                    boolean validLocation = false;

                    // Take the first VM install type that claims the location as a
                    // valid VM install. VM install types should be smart enough to not
                    // claim another type's VM, but just in case...
                    for (IVMInstallType type : vmTypes) {
                        if (monitor.isCanceled()) {
                            return;
                        }
                        IStatus status = type.validateInstallLocation(file);
                        if (status.isOK()) {
                            String filePath = file.getPath();
                            int index = filePath.lastIndexOf(File.separatorChar);
                            File newFile = file;
                            // remove bin folder from install location as java executables are found
                            // only under bin for Java 9 and above
                            if (index > 0 && filePath.substring(index + 1).equals("bin")) { //$NON-NLS-1$
                                newFile = new File(filePath.substring(0, index));
                            }
                            found.add(newFile);
                            types.add(type);
                            validLocation = true;
                            break;
                        }
                    }
                    if (!validLocation) {
                        subDirs.add(file);
                    }
                }
            }
        }
        while (!subDirs.isEmpty()) {
            File subDir = subDirs.remove(0);
            searchJDK(subDir, found, types, ignore, monitor);
            if (monitor.isCanceled()) {
                return;
            }
        }

    }
    // CSON: CyclomaticComplexity

}
