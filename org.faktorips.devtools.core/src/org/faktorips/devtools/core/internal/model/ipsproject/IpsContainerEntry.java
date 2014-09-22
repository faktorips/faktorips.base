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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
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
 * The implementation of {@link IIpsContainerEntry}
 * 
 * @author Jan Ortmann
 */
public class IpsContainerEntry extends IpsObjectPathEntry implements IIpsContainerEntry {

    private static final String XML_ATTRIBUTE_PATH = "path"; //$NON-NLS-1$
    private static final String XML_ATTRIBUTE_CONTAINER = "container"; //$NON-NLS-1$
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

    public void setContainerTypeId(String containerTypeId) {
        this.containerTypeId = containerTypeId;
    }

    @Override
    public String getOptionalPath() {
        return optionalPath;
    }

    public void setOptionalPath(String optionalPath) {
        this.optionalPath = optionalPath;
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
        IIpsModel ipsModel = getIpsObjectPath().getIpsProject().getIpsModel();
        return ipsModel.getIpsObjectPathContainer(getIpsProject(), containerTypeId, optionalPath);
    }

    @Override
    public List<IIpsObjectPathEntry> resolveEntries() {
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
    protected IIpsSrcFile findIpsSrcFileInternal(QualifiedNameType nameType, IpsObjectPathSearchContext searchContext) {
        List<IIpsObjectPathEntry> entries = resolveEntries();
        for (IIpsObjectPathEntry entry : entries) {
            IIpsSrcFile file = ((IpsObjectPathEntry)entry).findIpsSrcFileInternal(nameType, searchContext);
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
     * 
     * For {@link IpsContainerEntry} there is no single {@link IIpsPackageFragmentRoot} hence this
     * method always return <code>null</code>.
     */
    @Override
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot() {
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * For {@link IpsContainerEntry} there is no single {@link IIpsPackageFragmentRoot} hence this
     * method always return <code>null</code>.
     */
    @Override
    public String getIpsPackageFragmentRootName() {
        return null;
    }

    @Override
    public boolean containsResource(String path) {
        List<IIpsObjectPathEntry> entries = resolveEntries();
        for (IIpsObjectPathEntry entry : entries) {
            if (entry.containsResource(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getResourceAsStream(String resourcePath, IpsObjectPathSearchContext searchContext) {
        List<IIpsObjectPathEntry> entries = resolveEntries();
        for (IIpsObjectPathEntry entry : entries) {
            if (entry.containsResource(resourcePath)) {
                InputStream stream = entry.getResourceAsStream(resourcePath, searchContext);
                return stream;
            }
        }
        throw new CoreRuntimeException("Resource " + resourcePath + " was not found in container " + getName()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initFromXml(Element element, IProject project) {
        super.initFromXml(element, project);
        containerTypeId = element.getAttribute(XML_ATTRIBUTE_CONTAINER);
        optionalPath = element.getAttribute(XML_ATTRIBUTE_PATH);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element toXml(Document doc) {
        Element element = super.toXml(doc);
        element.setAttribute(XML_ATTRIBUTE_TYPE, TYPE_CONTAINER);
        element.setAttribute(XML_ATTRIBUTE_CONTAINER, containerTypeId);
        element.setAttribute(XML_ATTRIBUTE_PATH, optionalPath);
        return element;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageList validate() {
        IIpsObjectPathContainer container = getIpsObjectPathContainer();
        if (container == null) {
            MessageList result = new MessageList();
            result.add(new Message(MSG_CODE_INVALID_CONTAINER_ENTRY, NLS.bind(
                    Messages.IpsContainerEntry_err_invalidConainerEntry, containerTypeId), Message.ERROR, this));
            return result;
        }
        return container.validate();
    }

    @Override
    public IIpsObjectPathEntry getResolvedEntry(String rootName) {
        List<IIpsObjectPathEntry> entries = resolveEntries();
        for (IIpsObjectPathEntry entry : entries) {
            if (rootName.equals(entry.getIpsPackageFragmentRootName())) {
                return entry;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "ContainerEntry[" + getName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
