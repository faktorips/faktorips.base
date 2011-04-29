/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.migration;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFileMemento;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * Default migration that loads every ips object in the source folders of the given project and call
 * a template method called migrate() to allow subclasses to modify the object.
 * 
 * @author Jan Ortmann
 */
public abstract class DefaultMigration extends AbstractIpsProjectMigrationOperation {

    public DefaultMigration(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException {
        MessageList messages = new MessageList();
        IIpsPackageFragmentRoot[] roots = getIpsProject().getSourceIpsPackageFragmentRoots();
        try {
            monitor.beginTask("Migrate project: " + getIpsProject().getName(), countPackages() * 10); //$NON-NLS-1$
            beforeFileMigration();
            for (IIpsPackageFragmentRoot root : roots) {
                IIpsPackageFragment[] packs = root.getIpsPackageFragments();
                for (IIpsPackageFragment pack : packs) {
                    migrate(pack, messages, new SubProgressMonitor(monitor, 10));
                    if (monitor.isCanceled()) {
                        return messages;
                    }
                }
            }
        } finally {
            monitor.done();
        }
        return messages;
    }

    private int countPackages() throws CoreException {
        int packs = 0;
        IIpsPackageFragmentRoot[] roots = getIpsProject().getSourceIpsPackageFragmentRoots();
        for (IIpsPackageFragmentRoot root : roots) {
            packs = packs + root.getIpsPackageFragments().length;
        }
        return packs;
    }

    protected void migrate(IIpsPackageFragment pack, MessageList list, IProgressMonitor monitor) throws CoreException {
        IFolder folder = (IFolder)pack.getCorrespondingResource();
        IResource[] members = folder.members();
        monitor.beginTask("Migrate package " + pack.getName(), members.length); //$NON-NLS-1$
        for (IResource member : members) {
            try {
                if (member instanceof IFile) {
                    IFile file = (IFile)member;
                    boolean wasMigrated = migrate(file);
                    if (!wasMigrated) {
                        IIpsSrcFile srcFile = pack.getIpsSrcFile(file.getName());
                        if (srcFile != null) {
                            migrate(srcFile);
                        }
                        if (monitor.isCanceled()) {
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                String text = "An error occured while migrating file " + member; //$NON-NLS-1$
                System.err.println(text);
                list.add(Message.newError("", text)); //$NON-NLS-1$
                IpsPlugin.log(new IpsStatus(text, e));
            }
            monitor.worked(1);
        }
        monitor.done();
    }

    /**
     * Migrate the ips source file using XSLT.
     * 
     * @param srcFile The ips source file to migrate
     * @param oldContent The old content which will be transformed as new content for ips source
     *            file
     * @param xslFile The stylesheet which is used to process the given content via XSLT
     * 
     * @throws CoreException If there was an exception during the transformation
     */
    protected void migrateUsingTransformation(IIpsSrcFile srcFile, InputStream oldContent, String xslFile)
            throws CoreException {
        TransformerFactory factory = TransformerFactory.newInstance();
        try {
            DOMResult result = new DOMResult();
            Transformer transformer = factory.newTransformer(new StreamSource(getClass().getClassLoader()
                    .getResourceAsStream(xslFile)));
            transformer.setOutputProperty(OutputKeys.ENCODING, getIpsProject().getXmlFileCharset());
            transformer.transform(new StreamSource(oldContent), result);
            Element firstElement = XmlUtil.getFirstElement(result.getNode());
            firstElement.normalize();
            IIpsSrcFileMemento memento = new IIpsSrcFileMemento(srcFile, firstElement, true);
            srcFile.setMemento(memento);
            srcFile.markAsDirty();
        } catch (Exception e) {
            throw new CoreException(new IpsStatus(e));
        }
    }

    /**
     * This template method is called for all files in an ips package fragments. Subclasses must
     * implement their migration logic here.
     * 
     * @see IIpsSrcFile#markAsDirty()
     */
    protected abstract boolean migrate(IFile file) throws CoreException;

    /**
     * This template method is called after the ips object is loaded. Subclasses must implement
     * their migration logic here. Note that an object is only saved physically to disk, if it was
     * either changed or it's enclosing file is marked as dirty.
     * 
     * @see IIpsSrcFile#markAsDirty()
     */
    protected abstract void migrate(IIpsSrcFile srcFile) throws CoreException;

    /**
     * Hook method for subclasses to do stuff that is done once before any file is migrated.
     * 
     * @throws CoreException This method may throw this exception at any time.
     */
    protected void beforeFileMigration() throws CoreException {
        // Empty default implementation
    }
}
