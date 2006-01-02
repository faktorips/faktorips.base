package org.faktorips.devtools.core.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.faktorips.devtools.core.model.extproperties.ExtensionPropertyDefinition;

/**
 * The IPS model is the top of the IpsElement hierarchy (like the Java model is  the top of the Java element
 * hierarchy). One model instance exists per workspace. The model instance can be retrievedd via the plugin's
 * <code>getIpsModel()</code> method. 
 */
public interface IIpsModel extends IIpsElement {
    
    /**
     * Returns the workspace.
     */
    public IWorkspace getWorkspace();

    /**
     * Returns all IPS projects opened in the workspace or an empty array if none.
     */
    public IIpsProject[] getIpsProjects() throws CoreException;
    
    /**
     * Returns the IpsProject with the indicated name.
     */
    public IIpsProject getIpsProject(String name);
    
    /**
     * Returns the IpsProject that belongs to the indicated platform project.
     * 
     * @throws NullPointerException if project is null.
     */
    public IIpsProject getIpsProject(IProject project);
    
    /**
     * Returns the IpsElement that corresponds to the indicated resource.
     */
    public IIpsElement getIpsElement(IResource resource);
    
    /**
     * Adds a listener that is notified when something in the model was changed.
     * 
     * @throws IllegalArgumentException if listener is null.
     */
    public void addChangeListener(ContentsChangeListener listener);
    
    /**
     * Removes the change listener.
     * 
     * @throws IllegalArgumentException if listener is null.
     */
    public void removeChangeListener(ContentsChangeListener listener);

    /**
     * Returns all package fragment roots containing source files or
     * an empty array if none is found.
     * 
     * @throws CoreException
     */
    public IIpsPackageFragmentRoot[] getSourcePackageFragmentRoots() throws CoreException;
    
    /**
     * Returns the IPS source file that correspond to the given compilation unit
     * or <code>null</code> if no such file can be determined. Note that the returned
     * file might not exists.
     *  
     * @throws CoreException if an error occurs while searching for the file.
     * @throws NullPointerException if cu is null.
     */
    public IIpsSrcFile findIpsSrcFile(ICompilationUnit cu) throws CoreException;
    
    /**
     * Returns all IpsArtefactBuilderSets that have been assigned to this model. 
     */
    public IIpsArtefactBuilderSet[] getAvailableArtefactBuilderSets();
    
    /**
     * Sets the available artefact builder sets of this model.
     */
    public void setAvailableArtefactBuilderSets(IIpsArtefactBuilderSet[] sets);
    
    /**
     * Returns the extension properties for the given type. Returns an empty array
     * if no extension property is defined.
     * 
     * @param type The published interface of the ips object or part 
     * e.g. <code>org.faktorips.plugin.model.pctype.IAttribute</code>
     * @param <code>true</code> if not only the extension properties defined for for the type itself
     * should be returned, but also the ones registered for it's supertype(s) and it's interfaces.
     */
    public ExtensionPropertyDefinition[] getExtensionPropertyDefinitions(Class type, boolean includeSupertypesAndInterfaces);
    
    /**
     * Returns the extension property with the given id that belongs to the given type. Returns <code>null</code>
     * if no such extension property is defined.
     * 
     * @param type The published interface of the ips object or part 
     * e.g. <code>or.faktorips.plugin.model.pctype.Attribute</code>
     * @parma propertyId the extension property id
     * @param <code>true</code> if not only the extension properties defined for for the type itself
     * should be returned, but also the ones registered for it's supertype(s) and it's interfaces.
     */
    public IExtensionPropertyDefinition getExtensionPropertyDefinition(
            Class type, 
            String propertyId, 
            boolean includeSupertypesAndInterfaces);
    
    
}
