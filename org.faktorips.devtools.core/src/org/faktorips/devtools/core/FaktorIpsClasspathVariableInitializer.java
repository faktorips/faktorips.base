package org.faktorips.devtools.core;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ClasspathVariableInitializer;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.framework.Bundle;

/**
 * 
 * @author Jan Ortmann
 */
public class FaktorIpsClasspathVariableInitializer extends
		ClasspathVariableInitializer {
	
	public final static String VARNAME_UTIL_BIN = "FAKTORIPS_UTIL";
	public final static String VARNAME_UTIL_SRC = "FAKTORIPS_UTIL_SRC";
	public final static String VARNAME_VALUETYPES_BIN = "FAKTORIPS_VALUETYPES";
	public final static String VARNAME_VALUETYPES_SRC = "FAKTORIPS_VALUETYPES_SRC";
	public final static String VARNAME_RUNTIME_BIN = "FAKTORIPS_RUNTIME";
	public final static String VARNAME_RUNTIME_SRC = "FAKTORIPS_RUNTIME_SRC";
	public final static String VARNAME_DTFLCOMMON = "FAKTORIPS_DTFLCOMMON";
	public final static String VARNAME_COMMONS_LANG_BIN = "FAKTORIPS_INCLUDED_COMMONS_LANG";
	
	/**
	 * Classpath variables for the faktorips jars needed at runtime.
	 */
	public final static String[] IPS_VARIABLES_BIN = new String[] {
		VARNAME_UTIL_BIN, VARNAME_VALUETYPES_BIN, VARNAME_RUNTIME_BIN, VARNAME_DTFLCOMMON, VARNAME_COMMONS_LANG_BIN };

	/**
	 * Classpath variables for the source attachements.
	 */
	public final static String[] IPS_VARIABLES_SRC = new String[] {
		VARNAME_UTIL_SRC, VARNAME_VALUETYPES_SRC, VARNAME_RUNTIME_SRC, "", ""};
	
	private HashMap varMapping = new HashMap();
	
	public FaktorIpsClasspathVariableInitializer() {
		add(new Mapping(VARNAME_UTIL_BIN, "org.faktorips.util", "/faktorips-util.jar"));
		add(new Mapping(VARNAME_UTIL_SRC, "org.faktorips.util", "/faktorips-utilsrc.zip"));
		add(new Mapping(VARNAME_COMMONS_LANG_BIN, "org.faktorips.util", "/lib/commons-lang-1.0.1.jar"));
		add(new Mapping(VARNAME_VALUETYPES_BIN, "org.faktorips.valuetypes", "/faktorips-valuetypes.jar"));
		add(new Mapping(VARNAME_VALUETYPES_SRC, "org.faktorips.valuetypes", "/faktorips-valuetypessrc.zip"));
		add(new Mapping(VARNAME_RUNTIME_BIN, "org.faktorips.runtime", "/faktorips-runtime.jar"));
		add(new Mapping(VARNAME_RUNTIME_SRC, "org.faktorips.runtime", "/faktorips-runtimesrc.zip"));
		add(new Mapping(VARNAME_DTFLCOMMON, "org.faktorips.dtflcommon", "/faktorips-dtflcommon.jar"));
	}
	
	private void add(Mapping m) {
		varMapping.put(m.getVarName(), m);
	}
	
	private Mapping getMapping(String varName) {
		return (Mapping)varMapping.get(varName);
	}

	/**
	 * {@inheritDoc}
	 */
	public void initialize(String variable) {
		Mapping m = getMapping(variable);
		if (m==null) {
			return;
		}
		Bundle bundle = Platform.getBundle(m.getPluginId());
		if (bundle == null) {
			JavaCore.removeClasspathVariable(variable, null);
			return;
		}
		
		URL installLocation= bundle.getEntry(m.jarName); //$NON-NLS-1$
		// installLocation is something like bundleentry://140/faktorips-util.jar
		URL local= null;
		try {
			local= Platform.asLocalURL(installLocation);
		} catch (Exception e) {
			JavaCore.removeClasspathVariable(variable, null);
			return;
		}
		try {
			File file = new File(local.getPath());
			String fullPath= file.getAbsolutePath();
			if (file.exists()) {
				JavaCore.setClasspathVariable(variable, Path.fromOSString(fullPath), null);
			}
		} catch (JavaModelException e1) {
			JavaCore.removeClasspathVariable(variable, null);
		}
	}
	
	private class Mapping {
		
		private String varName;
		private String pluginId;
		private String jarName;
		
		Mapping(String varName, String pluginId, String jarName) {
			this.varName = varName;
			this.pluginId = pluginId;
			this.jarName = jarName;
		}

		/**
		 * @return Returns the jarName.
		 */
		public String getJarName() {
			return jarName;
		}

		/**
		 * @return Returns the pluginId.
		 */
		public String getPluginId() {
			return pluginId;
		}

		/**
		 * @return Returns the varName.
		 */
		public String getVarName() {
			return varName;
		}
		
		
	}
}
