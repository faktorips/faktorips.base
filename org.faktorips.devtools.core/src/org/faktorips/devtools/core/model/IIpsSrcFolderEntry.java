package org.faktorips.devtools.core.model;

import org.eclipse.core.resources.IFolder;

/**
 * An object path entry that defines a folder containing FaktorIPS source files.
 * 
 * @author Jan Ortmann
 */
public interface IIpsSrcFolderEntry extends IIpsObjectPathEntry {

    /**
     * Returns the folder containing the ips source files.
     */
    public IFolder getSourceFolder();
    
    /**
     * Returns the package fragment root this source folder entry defines. Note that is not guaranteed
     * that the returned package fragment root exists.
     */
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot();
    
    /**
     * Returns the output folder where the Java source files are generated into. If a specific output folder is set 
     * for this entry, the specific output folder is returned, otherwise the ouput folder defined in the object path
     * is returned.
     */
    public IFolder getOutputFolderForGeneratedJavaFiles();
    
    /**
     * Returns the entry's own output folder where the Java source files are generated. This ouptput folder is used only
     * for this entry.
     */
    public IFolder getSpecificOutputFolderForGeneratedJavaFiles();
    
    /**
     * Sets the entry's output folder where the Java source files are generated into.
     */
    public void setSpecificOutputFolderForGeneratedJavaFiles(IFolder outputFolder);
    
    /**
     * Returns the name of the base package for the generated Java source files. If a specific base package name is set 
     * for this entry, the specific base package name is returned, otherwise the base package name defined in the object path
     * is returned.
     */
    public String getBasePackageNameForGeneratedJavaClasses();
    
    /**
     * Returns the name of the entry's own base package for the generated Java source files. All generated Java types
     * are contained in this package or one of the child packages.
     */
    public String getSpecificBasePackageNameForGeneratedJavaClasses();

    /**
     * Sets the name of entry's own base package for the generated Java source files. All generated Java types
     * are contained in this package or one of the child packages.
     */
    public void setSpecificBasePackageNameForGeneratedJavaClasses(String name);
    
    /**
     * Returns the output folder containting the Java source files where the developer adds it's own code.
     * If a specific output folder is set for this entry, the specific output folder is returned, 
     * otherwise the ouput folder defined in the object path is returned.
     */
    public IFolder getOutputFolderForExtensionJavaFiles();
    
    /**
     * Returns the entry's own output folder containing the Java source files where the developer adds it's
     * own code. This ouptput folder is used only for this entry.
     */
    public IFolder getSpecificOutputFolderForExtensionJavaFiles();
    
    /**
     * Sets the entry's output folder containg the Java source files where the developer adds it's own code.
     * This ouptput folder is used only for this entry.
     */
    public void setSpecificOutputFolderForExtensionJavaFiles(IFolder outputFolder);
    
    /**
     * Returns the name of the base package for the Java source files where the developer adds it's own code. 
     * If a specific base package name is set for this entry, the specific base package name is returned, 
     * otherwise the base package name defined in the object path is returned.
     */
    public String getBasePackageNameForExtensionJavaClasses();
    
    /**
     * Returns the name of the entry's own base package for the Java source files where the developer adds it's 
     * own code. All generated Java types are contained in this package or one of the child packages.
     */
    public String getSpecificBasePackageNameForExtensionJavaClasses();

    /**
     * Sets the name of the entry's own base package for the Java source files where the developer adds it's own code. 
     * All generated Java types are contained in this package or one of the child packages.
     */
    public void setSpecificBasePackageNameForExtensionJavaClasses(String name);
    
}
