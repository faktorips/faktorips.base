/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.ArchiveIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Ips object path entry for an archive.
 * 
 * @author Jan Ortmann
 */
public class IpsArchiveEntry extends IpsObjectPathEntry implements IIpsArchiveEntry {

    /**
     * Returns a description of the xml format.
     */
    public final static String getXmlFormatDescription() {
        return "Archive:" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "  <" + XML_ELEMENT + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "     type=\"archive\"" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "     file=\"base." + IIpsArchiveEntry.FILE_EXTENSION + "\">      The archive file." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$ //$NON-NLS-2$
                + "  </" + XML_ELEMENT + ">" + SystemUtils.LINE_SEPARATOR; //$NON-NLS-1$ //$NON-NLS-2$
    }

    private IIpsArchive archive;

    public IpsArchiveEntry(IpsObjectPath path) {
        super(path);
    }

    @Override
    public IIpsArchive getIpsArchive() {
        return archive;
    }

    @Override
    public IPath getArchiveLocation() {
        if (archive == null) {
            return null;
        }
        return archive.getLocation();
    }

    @Override
    public void setArchivePath(IIpsProject ipsProject, IPath newArchivePath) {
        if (newArchivePath == null) {
            archive = null;
            return;
        }
        if (archive != null && newArchivePath.equals(archive.getArchivePath())) {
            return;
        }
        archive = new IpsArchive(ipsProject, newArchivePath);
    }

    @Override
    public String getType() {
        return IIpsObjectPathEntry.TYPE_ARCHIVE;
    }

    @Override
    public String getIpsPackageFragmentRootName() {
        return getIpsArchive().getArchivePath().lastSegment();
    }

    @Override
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot() {
        return archive.getRoot();
    }

    @Override
    public boolean exists(QualifiedNameType qnt) throws CoreException {
        if (archive == null) {
            return false;
        }
        return archive.contains(qnt);
    }

    @Override
    public void findIpsSrcFilesInternal(IpsObjectType type,
            String packageFragment,
            List<IIpsSrcFile> result,
            Set<IIpsObjectPathEntry> visitedEntries) throws CoreException {

        ((ArchiveIpsPackageFragmentRoot)getIpsPackageFragmentRoot()).findIpsSourceFiles(type, packageFragment, result);
    }

    @Override
    protected IIpsSrcFile findIpsSrcFileInternal(QualifiedNameType qnt, Set<IIpsObjectPathEntry> visitedEntries)
            throws CoreException {

        IIpsSrcFile file = getIpsSrcFile(qnt);
        if (file.exists()) {
            return file;
        }
        return null;
    }

    @Override
    public void findIpsSrcFilesStartingWithInternal(IpsObjectType type,
            String prefix,
            boolean ignoreCase,
            List<IIpsSrcFile> result,
            Set<IIpsObjectPathEntry> visitedEntries) throws CoreException {

        if (ignoreCase) {
            prefix = prefix.toLowerCase();
        }

        for (QualifiedNameType qnt : archive.getQNameTypes()) {
            String name = qnt.getUnqualifiedName();
            if (ignoreCase) {
                name = name.toLowerCase();
            }
            if (name.startsWith(prefix)) {
                IIpsSrcFile file = getIpsSrcFile(qnt);
                if (file.exists()) {
                    result.add(getIpsSrcFile(qnt));
                }
            }
        }
    }

    private IIpsSrcFile getIpsSrcFile(QualifiedNameType qNameType) {
        ArchiveIpsPackageFragment pack = new ArchiveIpsPackageFragment(
                (ArchiveIpsPackageFragmentRoot)getIpsPackageFragmentRoot(), qNameType.getPackageName());
        return new ArchiveIpsSrcFile(pack, qNameType.getFileName());
    }

    @Override
    public void initFromXml(Element element, IProject project) {
        String path = element.getAttribute("file"); //$NON-NLS-1$
        IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(project);
        if (StringUtils.isEmpty(path)) {
            setArchivePath(ipsProject, null);
        } else {
            setArchivePath(ipsProject, new Path(path));
        }
    }

    @Override
    public Element toXml(Document doc) {
        Element element = doc.createElement(XML_ELEMENT);
        element.setAttribute("type", TYPE_ARCHIVE); //$NON-NLS-1$
        element.setAttribute("file", archive.getArchivePath().toString()); //$NON-NLS-1$
        return element;
    }

    @Override
    public MessageList validate() throws CoreException {
        MessageList result = new MessageList();
        if (archive == null || !archive.exists()) {
            String text = NLS.bind(Messages.IpsArchiveEntry_archiveDoesNotExist, archive == null ? null : archive
                    .getArchivePath().toString());
            Message msg = new Message(IIpsObjectPathEntry.MSGCODE_MISSING_ARCHVE, text, Message.ERROR, this);
            result.add(msg);
        } else if (archive != null && !archive.isValid()) {
            String text = NLS.bind(Messages.IpsArchiveEntry_archiveIsInvalid, archive == null ? null : archive
                    .getArchivePath().toString());
            Message msg = new Message(IIpsObjectPathEntry.MSGCODE_INVALID_ARCHVE, text, Message.ERROR, this);
            result.add(msg);
        }
        return result;
    }

    @Override
    public String toString() {
        return "ArchiveEntry[" + getArchiveLocation().toString() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public boolean isContained(IResourceDelta delta) {
        return ((IpsArchive)archive).isContained(delta);
    }

    @Override
    public InputStream getRessourceAsStream(String path) throws CoreException {
        return archive.getResourceAsStream(path);
    }

}
