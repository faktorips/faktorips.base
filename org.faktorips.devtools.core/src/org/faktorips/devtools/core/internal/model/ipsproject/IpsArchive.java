/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StreamUtil;

/**
 * An ips archive is an archive for ips objects. It is physically stored in a jar file.
 * This class gives read-only access to the archive's content.
 * A new archive can be created with the CreateIpsArchiveOperation.
 * 
 * @see org.faktorips.devtools.core.model.CreateIpsArchiveOperation
 * 
 * @author Jan Ortmann
 */
public class IpsArchive implements IIpsArchive {

    private final static int IPSOBJECT_FOLDER_NAME_LENGTH = IIpsArchive.IPSOBJECTS_FOLDER.length(); 
    
    private IPath archivePath;
    private IPath location;
    private long modificationStamp;
    private ArchiveIpsPackageFragmentRoot root;

    // package name as key, content as value. content stored as a set of qNameTypes
    private HashMap packs = null;

    // map with qNameTypes as keys and IpsObjectProperties as values.
    private LinkedHashMap qNameTypes = null;
    
    public IpsArchive(IIpsProject ipsProject, IPath path) {
        ArgumentCheck.notNull(ipsProject, "The parameter ipsproject cannot be null.");
        this.archivePath = path;
        this.root = new ArchiveIpsPackageFragmentRoot(ipsProject, this);
        determineLocation();
    }

    private void determineLocation(){
        if(archivePath == null){
            return;
        }
        //try to find the file assuming a path relative to the workspace
        IWorkspaceRoot wsRoot = root.getIpsProject().getProject().getWorkspace().getRoot();
        IFile file = wsRoot.getFile(archivePath);
        if(file != null && file.exists()){
            location = file.getLocation();
            return;
        }
        
        //try to find the file assuming a path relative to the project
        IProject project = root.getIpsProject().getProject();
        file = project.getFile(archivePath);
        if(file != null && file.exists()){
            location = file.getLocation();
            return;
        }
        //try to find the file outside the workspace in the file system
        File extFile = archivePath.toFile();
        if(extFile.exists()){
            location = Path.fromOSString(extFile.getAbsolutePath());
        }
        
        //TODO pk: support for path variables is missing 26-09-2008
        //location is null if it could not be determined through the above algorithm 
    }

    public boolean isContained(IResourceDelta delta) {
        //see javadoc IIpsArchiveEntry#isContained(IResourceDelta)
        IWorkspaceRoot wsRoot = root.getIpsProject().getProject().getWorkspace().getRoot();
        IFile file = wsRoot.getFileForLocation(getLocation());
        if (delta.findMember(file.getProjectRelativePath()) != null) {
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public IPath getLocation(){
        return location;
    }
    
    /**
     * {@inheritDoc}
     */
    public IPath getArchivePath() {
        return archivePath;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean exists() {
    	if (archivePath == null) {
    		return false;
    	}
    	if(getLocation() != null){
    	    return true;
    	}
    	return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public String[] getNonEmptyPackages() throws CoreException {
        readArchiveContentIfNecessary();
        String[] packNames = new String[packs.size()];
        int i = 0;
        for (Iterator it=packs.keySet().iterator(); it.hasNext(); i++) {
            packNames[i] = (String)it.next();
        }
        Arrays.sort(packNames);
        return packNames;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean containsPackage(String name) throws CoreException {
        if (name==null) {
            return false;
        }
        if (name.equals("")) { //$NON-NLS-1$
            return true; // default package is always contained.
        }
        readArchiveContentIfNecessary();
        if (packs.containsKey(name)) {
            return true;
        }
        String prefix = name + "."; //$NON-NLS-1$
        for (Iterator it=packs.keySet().iterator(); it.hasNext(); ) {
            String pack = (String)it.next();
            if (pack.startsWith(prefix)) {
                return true; // given pack name is an empty parent package
            }
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public String [] getNonEmptySubpackages(String parentPack) throws CoreException {
        if (parentPack == null) {
            return new String[0];
        }
        readArchiveContentIfNecessary();
        Set result = new HashSet();
        for (Iterator it = packs.keySet().iterator(); it.hasNext();) {
            String nonEmptyPack = (String)it.next();
            for (Iterator it2 = getParentPackagesIncludingSelf(nonEmptyPack).iterator(); it2.hasNext();) {
                String pack = (String)it2.next();
                if (isChildPackageOf(pack, parentPack)) {
                    result.add(pack);
                }
            }
        }

        String[]packNames = (String[])result.toArray(new String [result.size()]);
        Arrays.sort(packNames);
        
        return packNames;
    }
    
    private boolean isChildPackageOf(String candidate, String parentPack) {
        if (parentPack.equals("")) { // default package //$NON-NLS-1$
            return candidate.indexOf('.')==-1;
        } else {
            if (!candidate.startsWith(parentPack + '.')) {
                return false;
            }
            return StringUtils.countMatches(parentPack, ".") == StringUtils.countMatches(candidate, ".") - 1; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean contains(QualifiedNameType qnt) throws CoreException {
        readArchiveContentIfNecessary();
        return qNameTypes.containsKey(qnt);
    }

    /**
     * {@inheritDoc}
     */
    public Set getQNameTypes() throws CoreException {
        readArchiveContentIfNecessary();
        return qNameTypes.keySet();
    }
    
    /**
     * {@inheritDoc}
     */
    public Set getQNameTypes(String packName) throws CoreException {
        readArchiveContentIfNecessary();
        Set qnts = (Set)packs.get(packName);
        if (qnts==null) {
            return Collections.EMPTY_SET;
        }
        Set packContent = new HashSet(qnts);
        return packContent;
    }

    public InputStream getSortDefinitionContent(String packName) throws CoreException{
        if(packName == null){
            return null;
        }
        if(!containsPackage(packName)){
            return null;
        }
        StringBuffer buf = new StringBuffer();
        buf.append(packName);
        buf.append('.');
        buf.append(IIpsPackageFragment.SORT_ORDER_FILE_NAME);
//      TODO pk 2007-11-11 intermedia state
//        return getResource(buf.toString());
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    public InputStream getContent(QualifiedNameType qnt) throws CoreException {
        if (qnt == null) {
            return null;
        }
        readArchiveContentIfNecessary();
        if (!qNameTypes.containsKey(qnt)) {
            return null;
        }
        JarFile archive;
        try {
        	File archiveFile = getFileFromPath();
            archive = new JarFile(archiveFile);
        } catch (IOException e) {
            throw new CoreException(new IpsStatus("Error opening jar for " + this, e)); //$NON-NLS-1$
        }
        String entryName = IIpsArchive.IPSOBJECTS_FOLDER + IPath.SEPARATOR + qnt.toPath().toString();
        JarEntry entry = archive.getJarEntry(entryName);
        if (entry == null) {
            throw new CoreException(new IpsStatus("Entry not found in archive for " + this)); //$NON-NLS-1$
        }
        try {
            return StreamUtil.copy(archive.getInputStream(entry), 1024);
        } catch (IOException e) {
            throw new CoreException(new IpsStatus("Error reading data from archive for " + this, e)); //$NON-NLS-1$
        } finally {
            try {
                archive.close();
            } catch (Exception e) {
                throw new CoreException(new IpsStatus("Error closing stream or archive for " + this, e)); //$NON-NLS-1$
            }
        }
    }
    
    private void readArchiveContentIfNecessary() throws CoreException {
        synchronized (this) {
            if (getLocation() == null) {
                return;
            }
            if (packs==null || qNameTypes == null) {
                readArchiveContent();
            }
            if ((getLocation().toFile().lastModified()!=modificationStamp)) {
                readArchiveContent();
            }
        }
    }
    
    private void readArchiveContent() throws CoreException {
        if (!exists()) {
            throw new CoreException(new IpsStatus("IpsArchive file " + getLocation() + " does not exist!")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("Reading archive content from disk: " + this); //$NON-NLS-1$
        }
        packs = new HashMap(200);
        SortedMap qntTemp = new TreeMap();
        
        File file = getFileFromPath();        
        
		modificationStamp = file.lastModified();
        JarFile jar;
        try {
            jar = new JarFile(file);
        } catch (IOException e) {
            throw new CoreException(new IpsStatus("Error reading ips archive " + getLocation(), e)); //$NON-NLS-1$
        }
        Properties ipsObjectProperties = readIpsObjectsProperties(jar);
        for (Enumeration e=jar.entries(); e.hasMoreElements(); ) {
            JarEntry entry = (JarEntry)e.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            QualifiedNameType qNameType = getQualifiedNameType(entry);
            if (qNameType==null) {
                continue;
            }
            IpsObjectProperties props = new IpsObjectProperties(
                    getPropertyValue(ipsObjectProperties, qNameType, IIpsArchive.PROPERTY_POSTFIX_BASE_PACKAGE),
                    getPropertyValue(ipsObjectProperties, qNameType, IIpsArchive.PROPERTY_POSTFIX_EXTENSION_PACKAGE));
            qntTemp.put(qNameType, props);
            Set content = (Set)packs.get(qNameType.getPackageName());
            if (content==null) {
                content = new HashSet();
                packs.put(qNameType.getPackageName(), content);
            }
            content.add(qNameType);
        }
        qNameTypes = new LinkedHashMap(qntTemp);
        try {
            jar.close();
        } catch (IOException e) {
            throw new CoreException(new IpsStatus("Error closing ips archive " + getLocation())); //$NON-NLS-1$
        }
    }

	private File getFileFromPath() {
        return getLocation().toFile();
	}
    
    private Properties readIpsObjectsProperties(JarFile archive) throws CoreException {
        JarEntry entry = archive.getJarEntry(IIpsArchive.JAVA_MAPPING_ENTRY_NAME);
        if (entry==null) {
            throw new CoreException(new IpsStatus("Entry " + IIpsArchive.JAVA_MAPPING_ENTRY_NAME + " not found in archive for " + this)); //$NON-NLS-1$ //$NON-NLS-2$
        }
        InputStream is = null;
        try {
            is = archive.getInputStream(entry);
            Properties props = new Properties();
            props.load(is);
            return props;
        } catch (IOException e) {
            throw new CoreException(new IpsStatus("Error reading ipsobjects.properties from archive for " + this, e)); //$NON-NLS-1$
        } 
    }
    
    private QualifiedNameType getQualifiedNameType(JarEntry jarEntry) {
        try {
            String path = jarEntry.getName().substring(IPSOBJECT_FOLDER_NAME_LENGTH+1); // qName path begins after "ipsobject/"
            return QualifiedNameType.newQualifedNameType(path);
        } catch (Exception e) {
            return null; // the entry does not contain an ips object
        }
    }
    
    private String getPropertyValue(Properties properties, QualifiedNameType qnt, String postfix) {
        String key = qnt.toPath().toString() + IIpsArchive.QNT_PROPERTY_POSTFIX_SEPARATOR + postfix;
        return properties.getProperty(key);
    }
    
    public String toString() {
        return "Archive " + getLocation(); //$NON-NLS-1$
    }
    
    private List getParentPackagesIncludingSelf(String pack) {
        ArrayList result = new ArrayList();
        result.add(pack);
        getParentPackages(pack, result);
        return result;
    }

    private void getParentPackages(String pack, List result) {
        int index = pack.lastIndexOf('.');
        if (index==-1) {
            return;
        }
        String parentPack = pack.substring(0, index);
        result.add(parentPack);
        getParentPackages(parentPack, result);
    }

    /**
     * {@inheritDoc}
     */
    public String getBasePackageNameForGeneratedJavaClass(QualifiedNameType qnt) throws CoreException {
        readArchiveContentIfNecessary();
        IpsObjectProperties props = (IpsObjectProperties )qNameTypes.get(qnt);
        if (props==null) {
            return null;
        }
        return props.basePackage;
    }

    /**
     * {@inheritDoc}
     */
    public String getBasePackageNameForExtensionJavaClass(QualifiedNameType qnt) throws CoreException {
        readArchiveContentIfNecessary();
        IpsObjectProperties props = (IpsObjectProperties )qNameTypes.get(qnt);
        if (props==null) {
            return null;
        }
        return props.extensionPackage;
    }

    /**
     * Returns an IResource only if the resource can be located by the {@link IWorkspaceRoot#getFileForLocation(IPath)} method. 
     */
    IResource getCorrespondingResource() {
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IFile resource = workspaceRoot.getFileForLocation(getLocation());
        if (resource == null) {
            resource = workspaceRoot.getFile(getLocation());
        }
        return resource;
    }


    public IIpsPackageFragmentRoot getRoot() {
        return root;
    }
    
    class IpsObjectProperties {
        
        String basePackage;
        String extensionPackage;

        public IpsObjectProperties(String basePackage, String extensionPackage) {
            this.basePackage = basePackage;
            this.extensionPackage = extensionPackage;
        }
     
    }
}
