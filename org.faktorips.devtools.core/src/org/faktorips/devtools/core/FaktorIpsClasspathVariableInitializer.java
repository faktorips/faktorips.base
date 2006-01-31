package org.faktorips.devtools.core;

import java.util.HashMap;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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
	
	public final static String VARNAME_UTIL = "FAKTORIPS_UTIL";
	public final static String VARNAME_VALUETYPES = "FAKTORIPS_VALUETYPES";
	public final static String VARNAME_RUNTIME = "FAKTORIPS_RUNTIME";
	public final static String VARNAME_DTFLCOMMON = "FAKTORIPS_DTFLCOMMON";
	
	public final static String[] IPS_VARIABLES = new String[] {
		VARNAME_UTIL , VARNAME_VALUETYPES, VARNAME_RUNTIME, VARNAME_DTFLCOMMON };

	private HashMap varMapping = new HashMap();
	
	public FaktorIpsClasspathVariableInitializer() {
		add(new Mapping(VARNAME_UTIL, "org.faktorips.util", "faktorips-util.jar"));
		add(new Mapping(VARNAME_VALUETYPES, "org.faktorips.valuetypes", "faktorips-valuetypes.jar"));
		add(new Mapping(VARNAME_RUNTIME, "org.faktorips.runtime", "faktorips-runtime.jar"));
		add(new Mapping(VARNAME_DTFLCOMMON, "org.faktorips.dtflcommon", "faktorips-dtflcommon.jar"));
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
		Bundle bundle = InternalPlatform.getDefault().getBundle(m.getPluginId());
		IPath path = new Path(bundle.getLocation());
		try {
			JavaCore.setClasspathVariable(variable, path, null);
		} catch (JavaModelException e) {
			IpsPlugin.log(e);
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
