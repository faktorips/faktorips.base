/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
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
    
    private IFile archiveFile;
    private long modificationStamp;
    
    // package name as key, content as value. content stored as a set of qNameTypes
    private HashMap packs = null;

    // map with qNameTypes as keys and IpsObjectProperties as values.
    private HashMap qNameTypes = null;
    
    public IpsArchive(IFile file) {
        this.archiveFile = file;
    }

    /**
     * {@inheritDoc}
     */
    public IFile getArchiveFile() {
        return archiveFile;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean exists() {
        return archiveFile!=null && archiveFile.exists();
    }
    
    /**
     * {@inheritDoc}
     */
    public String[] getNoneEmptyPackages() throws CoreException {
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
    public Set getNoneEmptySubpackages(String parentPack) throws CoreException {
        if (parentPack==null) {
            return Collections.EMPTY_SET;
        }
        readArchiveContentIfNecessary();
        Set result = new HashSet();
        for (Iterator it=packs.keySet().iterator(); it.hasNext(); ) {
            String nonEmptyPack = (String)it.next();
            for (Iterator it2=getParentPackagesIncludingSelf(nonEmptyPack).iterator(); it2.hasNext(); ) {
               String pack = (String)it2.next();
               if (isChildPackageOf(pack, parentPack)) {
                   result.add(pack);
               }
            }
        }
        return result;
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
    
    /*
     * The specified path to the resource within the jar file needs to be relative to the
     * IIpsArchive.IPSOBJECTS_FOLDER. Additionally the path doesn't need to start with a path separator.
     */
//    TODO pk 2007-11-11 intermedia state
//    private InputStream getResource(String path) throws CoreException{
//        if (path == null) {
//            return null;
//        }
//        JarFile archive;
//        try {
//            archive = new JarFile(archiveFile.getLocation().toFile());
//        } catch (IOException e) {
//            throw new CoreException(new IpsStatus("Error opening jar for " + this, e)); //$NON-NLS-1$
//        }
//        JarEntry entry = archive.getJarEntry(IIpsArchive.IPSOBJECTS_FOLDER + IPath.SEPARATOR + path);
//        if (entry == null) {
//            return null;
//        }
//        try {
//            return StreamUtil.copy(archive.getInputStream(entry), 1024);
//        } catch (IOException e) {
//            throw new CoreException(new IpsStatus("Error reading data from archive for " + this, e)); //$NON-NLS-1$
//        } finally {
//            try {
//                archive.close();
//            } catch (Exception e) {
//                throw new CoreException(new IpsStatus("Error closing stream or archive for " + this, e)); //$NON-NLS-1$
//            }
//        }
//    }

//  TODO pk 2007-11-11 intermedia state
//    public InputStream getContent(QualifiedNameType qnt) throws CoreException {
//        if (qnt == null) {
//            return null;
//        }
//        readArchiveContentIfNecessary();
//        if (!qNameTypes.containsKey(qnt)) {
//            return null;
//        }
//        InputStream is = getResource(qnt.toPath().toString());
//        if (is == null) {
//            throw new CoreException(new IpsStatus("Entry not found in archive for " + this)); //$NON-NLS-1$
//        }
//        return is;
//    }
    
    
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
            archive = new JarFile(archiveFile.getLocation().toFile());
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
            if (archiveFile==null) {
                return;
            }
            if (packs==null) {
                readArchiveContent();
            }
            archiveFile.refreshLocal(0, null);
            if ((archiveFile.getModificationStamp()!=modificationStamp)) {
                readArchiveContent();
            }
        }
    }
    
    private void readArchiveContent() throws CoreException {
        if (!archiveFile.exists()) {
            throw new CoreException(new IpsStatus("IpsArchive file " + archiveFile + " does not exists!")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("Reading archive content from disk: " + this); //$NON-NLS-1$
        }
        packs = new HashMap(100);
        qNameTypes = new HashMap(100);
        modificationStamp = archiveFile.getModificationStamp();
        JarFile jar;
        try {
            jar = new JarFile(archiveFile.getLocation().toFile());
        } catch (IOException e) {
            throw new CoreException(new IpsStatus("Error reading ips archive " + archiveFile, e)); //$NON-NLS-1$
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
            qNameTypes.put(qNameType, props);
            Set content = (Set)packs.get(qNameType.getPackageName());
            if (content==null) {
                content = new HashSet();
                packs.put(qNameType.getPackageName(), content);
            }
            content.add(qNameType);
        }
        try {
            jar.close();
        } catch (IOException e) {
            throw new CoreException(new IpsStatus("Error closing ips archive " + archiveFile)); //$NON-NLS-1$
        }
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
        return "Archive " + archiveFile; //$NON-NLS-1$
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

    class IpsObjectProperties {
        
        String basePackage;
        String extensionPackage;

        public IpsObjectProperties(String basePackage, String extensionPackage) {
            this.basePackage = basePackage;
            this.extensionPackage = extensionPackage;
        }
     
    }
}
