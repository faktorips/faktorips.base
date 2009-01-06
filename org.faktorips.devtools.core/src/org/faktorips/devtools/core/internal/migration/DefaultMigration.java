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

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException {
        MessageList messages = new MessageList();
        IIpsPackageFragmentRoot[] roots = getIpsProject().getSourceIpsPackageFragmentRoots();
        try {
            monitor.beginTask("Migrate project: " + getIpsProject().getName(), countPackages() * 10);
            for (int i = 0; i < roots.length; i++) {
                IIpsPackageFragment[] packs = roots[i].getIpsPackageFragments();
                for (int j = 0; j < packs.length; j++) {
                    migrate(packs[j], messages, new SubProgressMonitor(monitor, 10));
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
        for (int i = 0; i < roots.length; i++) {
            packs = packs+ roots[i].getIpsPackageFragments().length;
        }
        return packs;
    }

    protected void migrate(IIpsPackageFragment pack, MessageList list, IProgressMonitor monitor) throws CoreException {
        IFolder folder = (IFolder)pack.getCorrespondingResource();
        IResource[] members = folder.members();
        monitor.beginTask("Migrate package " + pack.getName(), members.length);
        for (int i = 0; i < members.length; i++) {
            try {
                if (members[i] instanceof IFile) {
                    IFile file = (IFile)members[i];
                    boolean wasMigrated = migrate(file);
                    if (!wasMigrated) {
                        IIpsSrcFile srcFile = pack.getIpsSrcFile(file.getName());
                        if (srcFile!=null) {
                            migrate(srcFile);
                        }
                        if (monitor.isCanceled()) {
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                String text = "An error occured while migrating file " + members[i];
                System.err.println(text);
                list.add(Message.newError("", text));
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
     * @param oldContent The old content which will be transformed as new content for ips source file
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
}
