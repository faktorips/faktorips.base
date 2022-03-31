/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsContainerEntry;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
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
            return new ArrayList<>(0);
        }
        return container.resolveEntries();
    }

    @Override
    public boolean exists(QualifiedNameType qnt) {
        List<IIpsObjectPathEntry> entries = resolveEntries();
        for (IIpsObjectPathEntry entry : entries) {
            if (((IpsObjectPathEntry)entry).exists(qnt)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<IIpsSrcFile> findIpsSrcFiles(IpsObjectType type) {
        return Collections.emptyList();
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
    public boolean containsResource(String resourcePath) {
        return false;
    }

    @Override
    public InputStream getResourceAsStream(String resourcePath) {
        return null;
    }

    @Override
    public void initFromXml(Element element, AProject project) {
        super.initFromXml(element, project);
        containerTypeId = element.getAttribute(XML_ATTRIBUTE_CONTAINER);
        optionalPath = element.getAttribute(XML_ATTRIBUTE_PATH);
    }

    @Override
    public Element toXml(Document doc) {
        Element element = super.toXml(doc);
        element.setAttribute(XML_ATTRIBUTE_TYPE, TYPE_CONTAINER);
        element.setAttribute(XML_ATTRIBUTE_CONTAINER, containerTypeId);
        element.setAttribute(XML_ATTRIBUTE_PATH, optionalPath);
        return element;
    }

    @Override
    public MessageList validate() {
        IIpsObjectPathContainer container = getIpsObjectPathContainer();
        if (container == null) {
            MessageList result = new MessageList();
            result.add(new Message(MSG_CODE_INVALID_CONTAINER_ENTRY, MessageFormat.format(
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
