/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.io.InputStream;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.ipsobject.LibraryIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsStorage;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Ips object path entry for an archive.
 * 
 * @author Jan Ortmann
 */
public class IpsArchiveEntry extends IpsLibraryEntry implements IIpsArchiveEntry {

    private IIpsArchive archive;

    public IpsArchiveEntry(IpsObjectPath ipsObjectPath) {
        super(ipsObjectPath);

    }

    /**
     * Returns a description of the xml format.
     */
    public static final String getXmlFormatDescription() {
        return "Archive:" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "  <" + XML_ELEMENT + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "     type=\"archive\"" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "     file=\"base." + IIpsArchiveEntry.FILE_EXTENSION + "\">      The archive file." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$ //$NON-NLS-2$
                + "  </" + XML_ELEMENT + ">" + SystemUtils.LINE_SEPARATOR; //$NON-NLS-1$ //$NON-NLS-2$
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
    public void initStorage(IPath newArchivePath) {
        if (newArchivePath == null) {
            archive = null;
            return;
        }
        if (archive != null && newArchivePath.equals(archive.getArchivePath())) {
            return;
        }
        archive = new IpsArchive(getIpsProject(), newArchivePath);
        setIpsPackageFragmentRoot(new LibraryIpsPackageFragmentRoot(getIpsProject(), archive));
    }

    @Override
    public String getIpsPackageFragmentRootName() {
        return getIpsArchive().getArchivePath().lastSegment();
    }

    @Override
    public boolean exists(QualifiedNameType qnt) throws CoreException {
        if (archive == null || qnt == null) {
            return false;
        }
        return archive.contains(qnt.toPath());
    }

    @Override
    protected IIpsSrcFile getIpsSrcFile(QualifiedNameType qNameType) {
        LibraryIpsPackageFragment pack = new LibraryIpsPackageFragment(getIpsPackageFragmentRoot(),
                qNameType.getPackageName());
        return new LibraryIpsSrcFile(pack, qNameType.getFileName());
    }

    @Override
    public String getType() {
        return TYPE_ARCHIVE;
    }

    @Override
    public MessageList validate() {
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
    public boolean isAffectedBy(IResourceDelta delta) {
        return archive.isAffectedBy(delta);
    }

    @Override
    public boolean containsResource(String resourcePath) {
        return archive.contains(new Path(resourcePath));
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        return archive.getResourceAsStream(path);
    }

    @Override
    protected IIpsStorage getIpsStorage() {
        return archive;
    }

    @Override
    protected String getXmlAttributePathName() {
        return "file"; //$NON-NLS-1$
    }

    @Override
    protected String getXmlPathRepresentation() {
        return archive.getArchivePath().toString();
    }

    @Override
    public IPath getPath() {
        return archive.getArchivePath();
    }

}
