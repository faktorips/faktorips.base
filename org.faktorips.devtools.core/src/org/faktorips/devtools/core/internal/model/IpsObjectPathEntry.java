package org.faktorips.devtools.core.internal.model;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IpsObjectPathEntry.
 *  
 * @author Jan Ortmann
 */
public abstract class IpsObjectPathEntry implements IIpsObjectPathEntry {
    
    // name of xml elements representing path entries.
    final static String XML_ELEMENT = "Entry"; //$NON-NLS-1$

    private IpsObjectPath path;
    
    public IpsObjectPathEntry(IpsObjectPath path) {
        ArgumentCheck.notNull(path);
        this.path = path;
    }

    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.IIpsObjectPathEntry#getIpsObjectPath()
     */
    public IIpsObjectPath getIpsObjectPath() {
        return path;
    }
    
    /**
     * Returns the first object with the indicated type and qualified name found in the path entry.
     */
    public abstract IIpsObject findIpsObject(IIpsProject ipsProject, IpsObjectType type, String qualifiedName) throws CoreException;    

    /**
     * Returns the first object with the indicated qualified name type found in the path entry.
     */
    public abstract IIpsObject findIpsObject(IIpsProject ipsProject, QualifiedNameType nameType) throws CoreException;    

    /**
     * Adds all objects of the given type found in the path entry to the result list. 
     */
    public abstract void findIpsObjects(IIpsProject IIpsProject, IpsObjectType type, List result) throws CoreException;
    
    /**
     * Returns all objects of the given type starting with the given prefix found on the path.
     * 
     * @param ignoreCase <code>true</code> if case differences should be ignored during the search.
     * 
     * @throws CoreException if an error occurs while searching for the objects. 
     */
    protected abstract void findIpsObjectsStartingWith(
    		IIpsProject IIpsProject, 
    		IpsObjectType type, 
    		String prefix, 
    		boolean ignoreCase, 
    		List result ) throws CoreException;

    /**
     * Initializes the entry with the data stored in the xml element.
     */
    public abstract void initFromXml(Element element, IProject project);
    
    /**
     * Transforms the entry to an xml element.
     * @param doc The xml document used to created the element.
     */
    public abstract Element toXml(Document doc);
    
    /**
     * Returns the object path entry stored in the xml element.     
     */
    public final static IIpsObjectPathEntry createFromXml(IpsObjectPath path, Element element, IProject project) {
        IpsObjectPathEntry entry;
        String type = element.getAttribute("type"); //$NON-NLS-1$
        if (type.equals(TYPE_SRC_FOLDER)) {
            entry = new IpsSrcFolderEntry(path);
            entry.initFromXml(element, project);
            return entry;
        }
        if (type.equals(TYPE_PROJECT_REFERENCE)) {
            entry = new IpsProjectRefEntry(path);
            entry.initFromXml(element, project);
            return entry;
        }
        throw new RuntimeException("Unknown entry type " + type); //$NON-NLS-1$
    }
    

}
