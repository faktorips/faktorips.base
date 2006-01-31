package org.faktorips.devtools.core.builder;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IPackageFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.Parameter;

/**
 * A collection of static helper methods.
 * 
 * @author Jan Ortmann
 */
public class BuilderHelper {
    
    public final static String[] transformParameterTypesToJavaClassNames(
    		IIpsProject ipsProject, 
    		Parameter[] params) throws CoreException {
	    String[] javaClasses = new String[params.length];
	    for (int i=0; i<params.length; i++) {
	        Datatype paramDataype = ipsProject.findDatatype(params[i].getDatatype());
		    javaClasses[i] = paramDataype.getJavaClassName();
	    }
        return javaClasses;
    }

    public final static String[] extractParameterNames(Parameter[] params) {
	    String[] paramNames = new String[params.length];
	    for (int i=0; i<params.length; i++) {
	        paramNames[i] = params[i].getName();
	    }
        return paramNames;
    }
    
    public final static String[] extractMessageParameters(String message) {
    	ArrayList al = new ArrayList();
		while(true) {
			int start = message.indexOf('{');
			if(start > -1) {
				int end = message.indexOf('}', start + 2); // param darf kein Leerstring sein
				if(end > -1) {
					String param = message.substring(start + 1, end);
					param = param.trim();
					param = param.replace(' ', '_');	
					param = StringUtils.uncapitalise(param); 
					al.add(param);
					message = message.substring(end + 1); 
				}
				else {
					break;
				}
			}
			else {
				break;
			}
		}
		return (String[]) al.toArray(new String[al.size()]);    	
    }
    
    public final static String transformMessage(String message) {
    	int count = 0;
    	String transformedMessage = "";
		while(true) {
			int start = message.indexOf('{');
			if(start > -1) {
				int end = message.indexOf('}', start + 2); // param darf kein Leerstring sein
				if(end > -1) {
					transformedMessage += message.substring(0,start);
					transformedMessage += "{";
					transformedMessage += count;
					transformedMessage += "}";
					message = message.substring(end + 1); 
					count++;
				}
				else {
					transformedMessage += message;
					break;
				}
			}
			else {
				transformedMessage += message;
				break;
			}
		}
		return transformedMessage;    	
    }    

    private BuilderHelper() {
        super();
    }

    /**
     * find the datatype for the attribute in the project configuration
     */
    public final static Datatype findAttributeDatatype(IAttribute a) 
    throws CoreException {
        Datatype datatype = a.getIpsProject().findDatatype(a.getDatatype());
        if (datatype == null) {
            throw new CoreException(new IpsStatus(
                    "No datatype found for attribute " + a.getName() + "and datatype " + a.getDatatype()));                       
        }
        return datatype;
    }
    
    /**
     * Returns the name of the provided file without its extension.
     */
    public final static String getFileNameWithoutExtension(IFile file){
        String name = file.getName(); 
        return file.getFileExtension() == null ? name : 
            name.substring(0, name.length() - 1 - file.getFileExtension().length());
    }
    
    /**
     * Copies the file of the provided IpsObject to the corresponding folder of the generated files.
     * In addition to that it changes the file extension to .xml. 
     */
    public final static void copyIpsObjectResource(IIpsObject ipsObject, MultiStatus buildStatus){
        IFile file = (IFile)ipsObject.getEnclosingResource();
        InputStream is;
        try {
            is = file.getContents(true);
        } catch (Exception e) {
            buildStatus.add(new IpsStatus("Can't read xml file contents for " + file, e));
            return;
        }
        IFolder folder = null;
        try {
            IIpsSrcFolderEntry srcEntry = (IIpsSrcFolderEntry)ipsObject.getIpsPackageFragment().getRoot().getIpsObjectPathEntry();
            IPackageFragment javaPack = ipsObject.getIpsPackageFragment().getJavaPackageFragment(IIpsPackageFragment.JAVA_PACK_PUBLISHED_INTERFACE);
            IPath pathToPack = new Path(javaPack.getElementName().replace('.', '/'));
            folder = srcEntry.getOutputFolderForGeneratedJavaFiles().getFolder(pathToPack);
            if (!folder.exists()) {
                createFolder(folder);
            }
        } catch (Exception e) {
            buildStatus.add(new IpsStatus("Can't create folder " + folder, e));
            return;
        }
        IFile copy = null;
        try {
            copy = folder.getFile(getFileNameWithoutExtension(file) + ".xml");
            if (copy.exists()) {
                copy.setContents(is, true, true, null);
            } else {
                copy.create(is, true, null);
            }
        } catch (Exception e) {
            buildStatus.add(new IpsStatus("Error copying contents of file " + file + " to " + copy, e));
            return;
        }
    }
    
    private final static void createFolder(IFolder folder) throws CoreException {
        while (!folder.getParent().exists()) {
            createFolder((IFolder)folder.getParent());
        }
        folder.create(true, true, null);
    }


}
