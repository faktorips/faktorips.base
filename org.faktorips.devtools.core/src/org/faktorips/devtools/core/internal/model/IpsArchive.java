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

package org.faktorips.devtools.core.internal.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsArchive;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.util.StringUtil;

/**
 * An ips archive is an archive for ips objects. It is physically stored in a jar file.
 * 
 * @author Jan Ortmann
 */
public class IpsArchive implements IIpsArchive {

    private IFile archiveFile;
    
    // package name as key, content as value. content stored as a set of qNameTypes
    private HashMap packs = new HashMap(100);

    // list of qNameTypes 
    private HashSet ipsObjects = new HashSet(100);
    
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
        readArchiveContentIfNeccessary();
        String[] packNames = new String[packs.size()];
        int i = 0;
        for (Iterator it=packs.keySet().iterator(); it.hasNext(); i++) {
            packNames[i] = (String)it.next();
        }
        return packNames;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean containsPackage(String name) throws CoreException {
        if (name==null) {
            return false;
        }
        if (name.equals("")) {
            return true; // default package is always contained.
        }
        readArchiveContentIfNeccessary();
        if (packs.containsKey(name)) {
            return true;
        }
        String prefix = name + ".";
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
        readArchiveContentIfNeccessary();
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
        if (parentPack.equals("")) { // default package
            return candidate.indexOf('.')==-1;
        } else {
            if (!candidate.startsWith(parentPack + '.')) {
                return false;
            }
            return StringUtils.countMatches(parentPack, ".") == StringUtils.countMatches(candidate, ".") - 1;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean contains(QualifiedNameType qnt) throws CoreException {
        readArchiveContentIfNeccessary();
        return ipsObjects.contains(qnt);
    }

    /**
     * {@inheritDoc}
     */
    public Set getQNameTypes() throws CoreException {
        readArchiveContentIfNeccessary();
        return ipsObjects;
    }
    
    /**
     * {@inheritDoc}
     */
    public Set getQNameTypes(String packName) throws CoreException {
        readArchiveContentIfNeccessary();
        Set qnts = (Set)packs.get(packName);
        if (qnts==null) {
            return Collections.EMPTY_SET;
        }
        Set packContent = new HashSet(qnts);
        return packContent;
    }

    /**
     * {@inheritDoc}
     */
    public String getContent(QualifiedNameType qnt, String encoding) throws CoreException {
        if (qnt==null) {
            return null;
        }
        readArchiveContentIfNeccessary();
        if (!ipsObjects.contains(qnt)) {
            return null;
        }
        JarFile archive;
        try {
            archive = new JarFile(archiveFile.getLocation().toFile());
        } catch (IOException e) {
            throw new CoreException(new IpsStatus("Error opening jar for " + this, e));
        }
        String entryName = qnt.toPath().toString();
        JarEntry entry = archive.getJarEntry(entryName);
        if (entry==null) {
            throw new CoreException(new IpsStatus("Entry not found in archive for " + this));
        }
        InputStream is = null;
        try {
            is = archive.getInputStream(entry);
            return StringUtil.readFromInputStream(is, encoding);
        } catch (IOException e) {
            throw new CoreException(new IpsStatus("Error reading data from archive for " + this, e));
        } finally {
            try {
                if (is!=null) {
                    is.close();
                }
                archive.close();
            } catch (Exception e) {
                throw new CoreException(new IpsStatus("Error closing stream or archive for " + this, e));
            }
        }
        
    }
    
    private void readArchiveContentIfNeccessary() throws CoreException {
        synchronized (this) {
            if (packs==null) {
                return;
            }
            packs = new HashMap(100);
            ipsObjects = new HashSet(100);
            readArchiveContent();
        }
    }
    
    private void readArchiveContent() throws CoreException {
        if (!archiveFile.exists()) {
            throw new CoreException(new IpsStatus("IpsArchive file " + archiveFile + " does not exists!"));
        }
        JarFile jar;
        try {
            jar = new JarFile(archiveFile.getLocation().toFile());
        } catch (IOException e) {
            throw new CoreException(new IpsStatus("Error reading ips archive " + archiveFile, e));
        }
        for (Enumeration e=jar.entries(); e.hasMoreElements(); ) {
            JarEntry entry = (JarEntry)e.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            QualifiedNameType qNameType = QualifiedNameType.newQualifedNameType(entry.getName());
            ipsObjects.add(qNameType);
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
            throw new CoreException(new IpsStatus("Error closing ips archive " + archiveFile));
        }
    }
    
    public String toString() {
        return "Archive " + archiveFile;
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
    
}
