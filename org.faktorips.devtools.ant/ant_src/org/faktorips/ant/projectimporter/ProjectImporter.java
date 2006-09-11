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

package org.faktorips.ant.projectimporter;

import java.util.Enumeration;

import org.apache.tools.ant.BuildException;
import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Implements a custom Ant-Task, which imports a given Directory 
 * to a running Eclipse Workspace as Project.
 * @author Marcel Senf - FaktorZehn GmbH
 */
public class ProjectImporter extends org.apache.tools.ant.Task {

	/** Directory from which to import Data */
	private String baseDir = "";
	/** Name of the Project */
	private String projectName = "";
	  
	
	public ProjectImporter(){
		super();
	}
	
	public void setDir(String dir){
		this.baseDir = dir; 
	}
	
	public void setName(String name){
		this.projectName = name;
	}
	
	public void execute() throws BuildException{
		
		//check if properties are set
		if (this.baseDir == "" || this.projectName == ""){
			throw new BuildException("Please provide both attributes, 'dir' AND 'name'");
		}
		
		
		Object runtime = getProject().getReferences().get(AntCorePlugin.ECLIPSE_RUNTIME);
		
		Enumeration references = getProject().getReferences().keys();
		
		while(references.hasMoreElements()){
			System.out.println(references.nextElement().toString());
		}
		
		IWorkspace workspace =  ResourcesPlugin.getWorkspace();
		System.out.println("workspache: " + workspace);
		
		
		System.out.println("Hello World. Runtime: "+runtime);
	}
}
