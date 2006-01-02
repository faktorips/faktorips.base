package org.faktorips.devtools.core.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;

/**
 * A handle for files containing sourcecode for ips objects.
 */
public interface IIpsSrcFile extends IIpsElement {
    
    /**
     * Returns the package fragment the file belongs to.
     */
    public IIpsPackageFragment getIpsPackageFragment();
    
    /**
     * Returns all Java compilation units that correspond to this IPS source file.
     * Note that any Java compilation unit might not exists. 
     */
    public ICompilationUnit[] getAllJavaCompilationUnits() throws CoreException;
    
    /**
     * Returns the corresponding platform file.
     */
    public IFile getCorrespondingFile();
    
    /**
     * Returns true if the file contains unsaved changes to it's contents. 
     * @throws CoreException
     */
    public boolean isDirty();
    
    /**
     * Marks the file as containing no unsaved changed.
     */
    public void markAsClean();
    
    /**
     * Discard all changes that haven't been saved yet. If the file
     * is not dirty, nothing happends.
     */
    public void discardChanges();
    
    /**
     * Returns a new memento with the file's content and dirty state.
     */
    public IIpsSrcFileMemento newMemento() throws CoreException;
    
    /**
     * Update the file's state with the informaiton from the memento.
     * 
     * @throws CoreException if the memento wasn't taken from this file.
     */
    public void setMemento(IIpsSrcFileMemento memento) throws CoreException;
    
    /**
     * Saves the file's content to the file system.
     *  
     * @throws CoreException
     */
    public void save(boolean force, IProgressMonitor monitor) throws CoreException;
    
    /**
     * Returns the file's contents.
     */
    public String getContents() throws CoreException;
    
    /**
     * Sets the file's new contents. Afterwards the method <code>isDirty()</code>
     * returns true until the object is saved.
     */
    public void setContents(String newContents) throws CoreException;
    
    /**
     * Returns true if the content can be parsed and a IpsObject can be created
     * based on the content. Returns false, if the contents can't be parsed
     * (e.g. the XML isn't properly formatted).
     * 
     * @throws CoreException if an error occurs while reading the contents.
     */
    public boolean isContentParsable() throws CoreException;
    
    /**
     * Returns the IPS object stored in the file.
     * 
     * @throws CoreException if the file can't be read or it's contents can't be
     * parsed.
     */
    public IIpsObject getIpsObject() throws CoreException;
    
    /**
     * Returns the IpsObjectType that is contain in this IpsSrcFile. 
     */
    public IpsObjectType getIpsObjectType();
    
    /**
     * Returns the qualified name type of the ips object contained within this IpsSrcFile. 
     */
    public QualifiedNameType getQualifiedNameType();
    
    
}
