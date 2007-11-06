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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.ArchiveIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentSortDefinition;
import org.faktorips.util.StringUtil;

/**
 *
 * @author Jan Ortmann
 */
public class ArchiveIpsPackageFragment extends AbstractIpsPackageFragment implements IIpsPackageFragment {

    public ArchiveIpsPackageFragment(ArchiveIpsPackageFragmentRoot root, String name) {
        super(root, name);
    }

    /**
     * {@inheritDoc}
     */
    public boolean exists() {
        ArchiveIpsPackageFragmentRoot root = (ArchiveIpsPackageFragmentRoot)getRoot();
        if (!root.exists()) {
            return false;
        }
        try {
            IIpsArchive archive = root.getIpsArchive();
            if (archive==null) {
                return false;
            }
            return archive.containsPackage(getName());
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragment[] getChildIpsPackageFragments() throws CoreException {

        List list = getChildIpsPackageFragmentsAsList();

        return (IIpsPackageFragment[])list.toArray(new IIpsPackageFragment[list.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsSrcFile[] getIpsSrcFiles() throws CoreException {
        ArchiveIpsPackageFragmentRoot root = (ArchiveIpsPackageFragmentRoot)getParent();
        IIpsArchive archive = root.getIpsArchive();
        if (archive==null) {
            return new IIpsSrcFile[0];
        }
        Set set = archive.getQNameTypes(getName());
        IIpsSrcFile[] srcFiles = new IIpsSrcFile[set.size()];
        int i = 0;
        for (Iterator it = set.iterator(); it.hasNext(); i++) {
            QualifiedNameType qnt = (QualifiedNameType)it.next();
            srcFiles[i] = new ArchiveIpsSrcFile(this, qnt.getFileName());
        }
        return srcFiles;
    }


    /**
     * {@inheritDoc}
     */
    public IResource[] getNonIpsResources() throws CoreException {
        return new IResource[0];
    }

    /**
     * {@inheritDoc}
     */
    public IIpsSrcFile getIpsSrcFile(String name) {
        return new ArchiveIpsSrcFile(this, name);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsSrcFile createIpsFile(String name, InputStream source, boolean force, IProgressMonitor monitor)
            throws CoreException {

        throw newCantModifyPackageStoredInArchive();
    }

    /**
     * {@inheritDoc}
     */
    public IIpsSrcFile createIpsFile(String name, String content, boolean force, IProgressMonitor monitor)
            throws CoreException {

        throw newCantModifyPackageStoredInArchive();
    }

    /**
     * {@inheritDoc}
     */
    public IIpsSrcFile createIpsFile(IpsObjectType type, String ipsObjectName, boolean force, IProgressMonitor monitor)
            throws CoreException {

        throw newCantModifyPackageStoredInArchive();
    }

    /**
     * {@inheritDoc}
     */
    public IIpsSrcFile createIpsFileFromTemplate(String name,
            IIpsObject template,
            GregorianCalendar date,
            boolean force,
            IProgressMonitor monitor) throws CoreException {

        throw newCantModifyPackageStoredInArchive();
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragment createSubPackage(String name, boolean force, IProgressMonitor monitor)
            throws CoreException {
        throw new CoreException(new IpsStatus("Can't modifiy package stored in an archive.")); //$NON-NLS-1$
    }

    private CoreException newCantModifyPackageStoredInArchive() {
        return new CoreException(new IpsStatus("Can't modifiy package stored in an archive.")); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public IResource getCorrespondingResource() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void findIpsObjects(IpsObjectType type, List result) throws CoreException {
        findIpsSourceFilesInternal(type, result, true); 
    }
    
    /**
     * {@inheritDoc}
     */
    public void findIpsSourceFiles(IpsObjectType type, List result) throws CoreException {
        findIpsSourceFilesInternal(type, result, false);    
    }

    private void findIpsSourceFilesInternal(IpsObjectType type, List result, boolean asIpsObject) throws CoreException {
        ArchiveIpsPackageFragmentRoot root = (ArchiveIpsPackageFragmentRoot)getParent();
        IIpsArchive archive = root.getIpsArchive();
        if (archive==null) {
            return;
        }
        Set set = archive.getQNameTypes(getName());
        for (Iterator it = set.iterator(); it.hasNext();) {
            QualifiedNameType qnt = (QualifiedNameType)it.next();
            if (qnt.getIpsObjectType()==type) {
                IIpsSrcFile ipsSrcFile = getIpsSrcFile(qnt.getFileName());
                if (asIpsObject){
                    result.add(ipsSrcFile.getIpsObject());
                } else {
                    result.add(ipsSrcFile);
                }
            }
        }        
    }
    
    /**
     * {@inheritDoc}
     */
    public void findIpsObjects(List result) throws CoreException {
        ArchiveIpsPackageFragmentRoot root = (ArchiveIpsPackageFragmentRoot)getParent();
        IIpsArchive archive = root.getIpsArchive();
        if (archive == null) {
            return;
        }
        Set set = archive.getQNameTypes(getName());
        for (Iterator it = set.iterator(); it.hasNext();) {
            QualifiedNameType qnt = (QualifiedNameType)it.next();
            result.add(getIpsSrcFile(qnt.getFileName()).getIpsObject());
        }
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragmentSortDefinition getSortDefinition() {
        // TODO Cache sort definition

        IIpsPackageFragmentSortDefinition sortDef =  new IpsPackageFragmentArbitrarySortDefinition();

        try {
            String content = getSortDefinitionContent();
            sortDef.initPersistenceContent(content);
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return null;
        }
        return sortDef;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragment[] getSortedChildIpsPackageFragments() throws CoreException {

        // TODO Sort IpsPackageFragments by IpsPackageFragment.SORT_ORDER_FILE_NAME
        List sortedPacks = getChildIpsPackageFragmentsAsList();

        return (IIpsPackageFragment[])sortedPacks.toArray(new IIpsPackageFragment[sortedPacks.size()]);
    }

    /**
     * Get all chidren of type IIpsPackageFragment.
     *
     * @return IpsPackageFragments as List.
     * @throws CoreException
     */
    private List getChildIpsPackageFragmentsAsList() throws CoreException {

        ArchiveIpsPackageFragmentRoot root = (ArchiveIpsPackageFragmentRoot)getParent();
        Set packNames = root.getIpsArchive().getNoneEmptySubpackages(getName());

        List list = new ArrayList(packNames.size());

        for (Iterator iter = packNames.iterator(); iter.hasNext();) {
            String element = (String)iter.next();
            list.add(new ArchiveIpsPackageFragment(root, element));
        }

        return list;
    }

    /**
     * This method is not supported for archives and throws a {@link RuntimeException} if called. 
     */
    public void setSortDefinition(IIpsPackageFragmentSortDefinition newDefinition) throws CoreException {
        throw new RuntimeException("Can't set the sort definition in archives!");
    }

    /**
     * This method is not supported for archives and throws a {@link RuntimeException} if called. 
     */
    public IFile getSortOrderFile() {
        throw new RuntimeException("This method is not supported for archives.");
    }

    /**
     * @return sort definition as String.
     * @throws CoreException
     */
    private String getSortDefinitionContent() throws CoreException {
        ArchiveIpsPackageFragmentRoot root = (ArchiveIpsPackageFragmentRoot)getParent();
        IIpsArchive archive = root.getIpsArchive();
        QualifiedNameType qnt = QualifiedNameType.newQualifedNameType(this.getName().concat( StringUtil.getSystemLineSeparator()  + IIpsPackageFragment.SORT_ORDER_FILE_NAME) );

        return archive.getContent(qnt, this.getRoot().getIpsProject().getPlainTextFileCharset() );
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasChildIpsPackageFragments()  throws CoreException {
        ArchiveIpsPackageFragmentRoot root = (ArchiveIpsPackageFragmentRoot)getParent();
        Set packNames = root.getIpsArchive().getNoneEmptySubpackages(getName());

        return (packNames.size() > 0 ? true : false);
    }

}
