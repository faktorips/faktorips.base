/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsContainerEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Jan Ortmann
 */
public class IpsContainerEntry extends IpsObjectPathEntry implements IIpsContainerEntry {

    private String containerTypeId;
    private String optionalPath;

    public IpsContainerEntry(IpsObjectPath path) {
        super(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return TYPE_CONTAINER;
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    @Override
    public String getContainerTypeId() {
        return containerTypeId;
    }

    @Override
    public String getOptionalPath() {
        return optionalPath;
    }

    @Override
    public String getName() {
        IIpsObjectPathContainer container = getIpsObjectPathContainer();
        if (container == null) {
            return "InvalidContainer: " + containerTypeId + '[' + optionalPath + ']'; //$NON-NLS-1$
        }
        return container.getName();
    }

    @Override
    public IIpsObjectPathContainer getIpsObjectPathContainer() {
        IIpsModel ipsModel = IpsPlugin.getDefault().getIpsModel();
        return ipsModel.getIpsObjectPathContainer(getIpsProject(), containerTypeId, optionalPath);
    }

    @Override
    public List<IIpsObjectPathEntry> resolveEntries() throws CoreException {
        IIpsObjectPathContainer container = getIpsObjectPathContainer();
        if (container == null) {
            return new ArrayList<IIpsObjectPathEntry>(0);
        }
        return container.resolveEntries();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(QualifiedNameType qnt) throws CoreException {
        List<IIpsObjectPathEntry> entries = resolveEntries();
        for (IIpsObjectPathEntry entry : entries) {
            if (((IpsObjectPathEntry)entry).exists(qnt)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IIpsSrcFile findIpsSrcFileInternal(QualifiedNameType nameType, Set<IIpsObjectPathEntry> visitedEntries)
            throws CoreException {

        List<IIpsObjectPathEntry> entries = resolveEntries();
        for (IIpsObjectPathEntry entry : entries) {
            IIpsSrcFile file = ((IpsObjectPathEntry)entry).findIpsSrcFileInternal(nameType, visitedEntries);
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void findIpsSrcFilesInternal(IpsObjectType type,
            String packageFragment,
            List<IIpsSrcFile> result,
            Set<IIpsObjectPathEntry> visitedEntries) throws CoreException {

        List<IIpsObjectPathEntry> entries = resolveEntries();
        for (IIpsObjectPathEntry entry : entries) {
            ((IpsObjectPathEntry)entry).findIpsSrcFilesInternal(type, packageFragment, result, visitedEntries);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void findIpsSrcFilesStartingWithInternal(IpsObjectType type,
            String prefix,
            boolean ignoreCase,
            List<IIpsSrcFile> result,
            Set<IIpsObjectPathEntry> visitedEntries) throws CoreException {

        List<IIpsObjectPathEntry> entries = resolveEntries();
        for (IIpsObjectPathEntry entry : entries) {
            ((IpsObjectPathEntry)entry).findIpsSrcFilesStartingWithInternal(type, prefix, ignoreCase, result,
                    visitedEntries);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot() {
        return null; // container entry hasn't got a root!
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIpsPackageFragmentRootName() {
        return null; // container entry hasn't got a root!
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getRessourceAsStream(String resourcePath) throws CoreException {
        List<IIpsObjectPathEntry> entries = resolveEntries();
        for (IIpsObjectPathEntry entry : entries) {
            InputStream stream = entry.getRessourceAsStream(resourcePath);
            if (stream != null) {
                return stream;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initFromXml(Element element, IProject project) {
        containerTypeId = element.getAttribute("container"); //$NON-NLS-1$
        optionalPath = element.getAttribute("path"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element toXml(Document doc) {
        Element element = doc.createElement(XML_ELEMENT);
        element.setAttribute("type", TYPE_CONTAINER); //$NON-NLS-1$
        element.setAttribute("container", containerTypeId); //$NON-NLS-1$
        element.setAttribute("path", optionalPath); //$NON-NLS-1$
        return element;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageList validate() throws CoreException {
        IIpsObjectPathContainer container = getIpsObjectPathContainer();
        if (container == null) {
            MessageList result = new MessageList();
            result.add(Message.newError("Invalid Container Entry", "No container of type " //$NON-NLS-1$ //$NON-NLS-2$
                    + containerTypeId + "found.")); //$NON-NLS-1$
            return result;
        }
        return container.validate();
    }

    @Override
    public IIpsObjectPathEntry getResolvedEntry(String rootName) {
        try {
            List<IIpsObjectPathEntry> entries = resolveEntries();
            for (IIpsObjectPathEntry entry : entries) {
                if (entry.getIpsPackageFragmentRootName().equals(rootName)) {
                    return entry;
                }
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return null;
    }
}
