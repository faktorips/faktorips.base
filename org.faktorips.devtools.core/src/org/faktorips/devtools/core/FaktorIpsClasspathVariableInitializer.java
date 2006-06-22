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
 * ClasspathVariableInitializer that initializes the FaktorIPS libraries needed at runtime.
 * 
 * @author Jan Ortmann
 */
public class FaktorIpsClasspathVariableInitializer extends
		ClasspathVariableInitializer { 
	
	public final static String VARNAME_VALUETYPES_BIN = "FAKTORIPS_VALUETYPES"; //$NON-NLS-1$
	public final static String VARNAME_VALUETYPES_SRC = "FAKTORIPS_VALUETYPES_SRC"; //$NON-NLS-1$
	public final static String VARNAME_RUNTIME_BIN = "FAKTORIPS_RUNTIME"; //$NON-NLS-1$
	public final static String VARNAME_RUNTIME_SRC = "FAKTORIPS_RUNTIME_SRC"; //$NON-NLS-1$
	
	/**
	 * Classpath variables for the faktorips jars needed at runtime.
	 */
	public final static String[] IPS_VARIABLES_BIN = new String[] {
		VARNAME_VALUETYPES_BIN, VARNAME_RUNTIME_BIN };

	/**
	 * Classpath variables for the source attachements.
	 */
	public final static String[] IPS_VARIABLES_SRC = new String[] {
		VARNAME_VALUETYPES_SRC, VARNAME_RUNTIME_SRC }; //$NON-NLS-1$ //$NON-NLS-2$
	
	private HashMap varMapping = new HashMap();
	
	public FaktorIpsClasspathVariableInitializer() {
		add(new Mapping(VARNAME_VALUETYPES_BIN, "org.faktorips.valuetypes", "/faktorips-valuetypes.jar")); //$NON-NLS-1$ //$NON-NLS-2$
		add(new Mapping(VARNAME_VALUETYPES_SRC, "org.faktorips.valuetypes", "/faktorips-valuetypessrc.zip")); //$NON-NLS-1$ //$NON-NLS-2$
		add(new Mapping(VARNAME_RUNTIME_BIN, "org.faktorips.runtime", "/faktorips-runtime.jar")); //$NON-NLS-1$ //$NON-NLS-2$
		add(new Mapping(VARNAME_RUNTIME_SRC, "org.faktorips.runtime", "/faktorips-runtimesrc.zip")); //$NON-NLS-1$ //$NON-NLS-2$
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
			IpsPlugin.log(new IpsStatus("Error initializing classpath variable " + variable  //$NON-NLS-1$
					+ ". Bundle " + m.pluginId + "not found.")); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		
		URL installLocation= bundle.getEntry(m.jarName);
		if (installLocation==null) {
			return; // if source is not distributed we init the variable.
		}
		// installLocation is something like bundleentry://140/faktorips-util.jar
		URL local= null;
		try {
			local= Platform.asLocalURL(installLocation);
		} catch (Exception e) {
			IpsPlugin.log(new IpsStatus("Error initializing classpath variable " + variable  //$NON-NLS-1$
					+ ". Bundle install locaction: " + installLocation, e)); //$NON-NLS-1$
			return;
		}
		try {
			File file = new File(local.getPath());
			String fullPath= file.getAbsolutePath();
			if (file.exists()) {
				JavaCore.setClasspathVariable(variable, Path.fromOSString(fullPath), null);
			}
		} catch (JavaModelException e1) {
			IpsPlugin.log(new IpsStatus("Error initializing classpath variable " + variable, e1)); //$NON-NLS-1$
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
