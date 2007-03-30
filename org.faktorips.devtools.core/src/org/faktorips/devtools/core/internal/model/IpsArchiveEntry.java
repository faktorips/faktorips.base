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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsArchive;
import org.faktorips.devtools.core.model.IIpsArchiveEntry;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsArchiveEntry extends IpsObjectPathEntry implements IIpsArchiveEntry {

    /**
     * Returns a description of the xml format.
     */
    public final static String getXmlFormatDescription() {
        return "Archive:" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
            +  "  <" + XML_ELEMENT + SystemUtils.LINE_SEPARATOR  //$NON-NLS-1$
            +  "     type=\"archive\"" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
            +  "     file=\"base." + IIpsArchiveEntry.FILE_EXTENSION + "\">      The archive file." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
            +  "  </" + XML_ELEMENT + ">" + SystemUtils.LINE_SEPARATOR;  //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    private IIpsArchive archive;
    
    /**
     * @param path
     */
    public IpsArchiveEntry(IpsObjectPath path) {
        super(path);
    }
    
    /**
     * {@inheritDoc}
     */
    public IIpsArchive getIpsArchive() {
        return archive;
    }

    /**
     * {@inheritDoc}
     */
    public IFile getArchiveFile() {
        if (archive==null) {
            return null;
        }
        return archive.getArchiveFile();
    }
    
    /**
     * {@inheritDoc}
     */
    public void setArchiveFile(IFile archiveFile) {
        if (archiveFile==null) {
            archive = null;
            return;
        }
        if (archive!=null && archiveFile.equals(archive.getArchiveFile())) {
            return;
        }
        archive = new IpsArchive(archiveFile);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getType() {
        return IIpsObjectPathEntry.TYPE_ARCHIVE;
    }
    
    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot(IIpsProject ipsProject) throws CoreException {
        return new ArchiveIpsPackageFragmentRoot(getIpsArchive().getArchiveFile());
    }
    
    /**
     * {@inheritDoc}
     */
    public IIpsObject findIpsObjectInternal(IIpsProject ipsProject, QualifiedNameType qnt, Set visitedEntries) throws CoreException {
        return getIpsPackageFragmentRoot(ipsProject).findIpsObject(qnt);
    }

    /**
     * {@inheritDoc}
     */
    public void findIpsObjectsInternal(IIpsProject ipsProject, IpsObjectType type, List result, Set visitedEntries) throws CoreException {
        ((ArchiveIpsPackageFragmentRoot)getIpsPackageFragmentRoot(ipsProject)).findIpsObjects(type, result);
    }

    /**
     * {@inheritDoc}
     */
    public void findIpsObjectsStartingWithInternal(IIpsProject ipsProject,
            IpsObjectType type,
            String prefix,
            boolean ignoreCase,
            List result,
            Set visitedEntries) throws CoreException {
        
        if (ignoreCase) {
            prefix = prefix.toLowerCase();
        }
        
        for (Iterator it=archive.getQNameTypes().iterator(); it.hasNext();) {
            QualifiedNameType qnt = (QualifiedNameType)it.next();
            String name = qnt.getUnqualifiedName();
            if (ignoreCase) {
                name = name.toLowerCase();
            }
            if (name.startsWith(prefix)) {
                result.add(getIpsObject(qnt, ipsProject));
            }
        }
    }
    
    private IIpsObject getIpsObject(QualifiedNameType qNameType, IIpsProject project) throws CoreException {
        IIpsPackageFragmentRoot root = getIpsPackageFragmentRoot(project);
        IIpsObject object = root.findIpsObject(qNameType);
        if (object!=null) {
            return object;
        }
        throw new CoreException(new IpsStatus("IpsObject not found for qNameType " + qNameType + " (but was expectedt to be in the archive!")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * {@inheritDoc}
     */
    public void initFromXml(Element element, IProject project) {
        String path = element.getAttribute("file"); //$NON-NLS-1$
        if (StringUtils.isEmpty(path)) {
            setArchiveFile(null);
        } else {
            setArchiveFile(project.getFile(new Path(path)));
        }
    }

    /**
     * {@inheritDoc}
     */
    public Element toXml(Document doc) {
        Element element = doc.createElement(XML_ELEMENT);
        element.setAttribute("type", TYPE_ARCHIVE); //$NON-NLS-1$
        element.setAttribute("file", archive.getArchiveFile().getProjectRelativePath().toString()); //$NON-NLS-1$
        return element;
    }

    /**
     * {@inheritDoc}
     */
    public MessageList validate() throws CoreException {
        MessageList result = new MessageList();
        if (!archive.exists()){
            String text = NLS.bind(Messages.IpsArchiveEntry_archiveDoesNotExist, archive.getArchiveFile().toString());
            Message msg = new Message(IIpsObjectPathEntry.MSGCODE_MISSING_ARCHVE, text, Message.ERROR, this);
            result.add(msg);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "ArchiveEntry[" + getArchiveFile().getProjectRelativePath().toString() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
