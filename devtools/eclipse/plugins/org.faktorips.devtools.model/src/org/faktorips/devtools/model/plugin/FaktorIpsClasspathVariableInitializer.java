/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.plugin;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ClasspathVariableInitializer;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.framework.Bundle;

/**
 * ClasspathVariableInitializer that initializes the Faktor-IPS libraries needed at runtime.
 * 
 * @author Jan Ortmann
 */
public class FaktorIpsClasspathVariableInitializer extends ClasspathVariableInitializer {

    public static final String VARNAME_VALUETYPES_JAVA5_BIN = "FAKTORIPS_VALUETYPES_JAVA5"; //$NON-NLS-1$
    public static final String VARNAME_VALUETYPES_JAVA5_SRC = "FAKTORIPS_VALUETYPES_JAVA5_SRC"; //$NON-NLS-1$
    public static final String VARNAME_RUNTIME_JAVA5_BIN = "FAKTORIPS_RUNTIME_JAVA5"; //$NON-NLS-1$
    public static final String VARNAME_RUNTIME_JAVA5_SRC = "FAKTORIPS_RUNTIME_JAVA5_SRC"; //$NON-NLS-1$

    public static final String[] IPS_VARIABLES_JAVA5_BIN = { VARNAME_VALUETYPES_JAVA5_BIN,
            VARNAME_RUNTIME_JAVA5_BIN };

    public static final String[] IPS_VARIABLES_JAVA5_SRC = { VARNAME_VALUETYPES_JAVA5_SRC,
            VARNAME_RUNTIME_JAVA5_SRC };

    private final HashMap<String, Mapping> varMapping = new HashMap<>();

    public FaktorIpsClasspathVariableInitializer() {
        add(new Mapping(VARNAME_VALUETYPES_JAVA5_BIN, "org.faktorips.valuetypes", "/")); //$NON-NLS-1$ //$NON-NLS-2$
        add(new Mapping(VARNAME_VALUETYPES_JAVA5_SRC, "org.faktorips.valuetypes", "/src.zip")); //$NON-NLS-1$ //$NON-NLS-2$
        add(new Mapping(VARNAME_RUNTIME_JAVA5_BIN, "org.faktorips.runtime", "/")); //$NON-NLS-1$ //$NON-NLS-2$
        add(new Mapping(VARNAME_RUNTIME_JAVA5_SRC, "org.faktorips.runtime", "/src.zip")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    protected void add(Mapping m) {
        varMapping.put(m.getVarName(), m);
    }

    private Mapping getMapping(String varName) {
        return varMapping.get(varName);
    }

    @Override
    public void initialize(String variable) {
        Mapping m = getMapping(variable);
        if (m == null) {
            return;
        }
        Bundle bundle = Platform.getBundle(m.getPluginId());
        if (bundle == null) {
            IpsLog.log(new IpsStatus("Error initializing classpath variable " + variable //$NON-NLS-1$
                    + ". Bundle " + m.pluginId + "not found.")); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        URL installLocation = bundle.getEntry(m.jarName);
        if (installLocation == null) {
            /*
             * Jar not installed - maybe the plug-in is running in a Runtime-Eclipse from source. In
             * this case, keep the old value if there is one.
             */
            IPath previous = JavaCore.getClasspathVariable(variable);
            if (previous == null) {
                return;
            }
            try {
                JavaCore.setClasspathVariable(variable, previous, null);
            } catch (JavaModelException e) {
                IpsLog.log(new IpsStatus("Error initializing classpath variable " + variable, e)); //$NON-NLS-1$
            }
            return;
        }
        // Install location is something like bundleentry://140/faktorips-util.jar
        URL local = null;
        try {
            local = FileLocator.toFileURL(installLocation);
            // CSOFF: IllegalCatchCheck
        } catch (Exception e) {
            IpsLog.log(new IpsStatus("Error initializing classpath variable " + variable //$NON-NLS-1$
                    + ". Bundle install locaction: " + installLocation, e)); //$NON-NLS-1$
            return;
        }
        // CSON: IllegalCatchCheck
        try {
            File file = new File(local.getPath());
            String fullPath = file.getAbsolutePath();
            if (file.exists()) {
                JavaCore.setClasspathVariable(variable, Path.fromOSString(fullPath), null);
            }
        } catch (JavaModelException e1) {
            IpsLog.log(new IpsStatus("Error initializing classpath variable " + variable, e1)); //$NON-NLS-1$
        }
    }

    protected class Mapping {

        private final String varName;
        private final String pluginId;
        private final String jarName;

        public Mapping(String varName, String pluginId, String jarName) {
            this.varName = varName;
            this.pluginId = pluginId;
            this.jarName = jarName;
        }

        public String getJarName() {
            return jarName;
        }

        public String getPluginId() {
            return pluginId;
        }

        public String getVarName() {
            return varName;
        }

    }
}
