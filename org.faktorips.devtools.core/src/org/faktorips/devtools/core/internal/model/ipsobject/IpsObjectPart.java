/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject;

import java.beans.PropertyChangeEvent;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.w3c.dom.Element;

public abstract class IpsObjectPart extends IpsObjectPartContainer implements IIpsObjectPart {

    private String id;

    private boolean deleted;

    protected IpsObjectPart(IIpsObjectPartContainer parent, String id) {
        super(parent, ""); //$NON-NLS-1$
        this.id = id;
    }

    /**
     * Constructor for testing purposes.
     */
    protected IpsObjectPart() {
        id = UUID.randomUUID().toString();
        // Provides default constructor for testing purposes
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public IIpsObject getIpsObject() {
        IpsObjectPartContainer container = getContainer();
        if (container == null) {
            return null;
        }
        return container.getIpsObject();
    }

    public IpsObjectPartContainer getContainer() {
        return (IpsObjectPartContainer)getParent();
    }

    @Override
    public void delete() {
        if (isDeleted()) {
            throw new RuntimeException("Object has already been deleted!"); //$NON-NLS-1$
        }

        deleted = true;
        if (getContainer() != null) {
            getContainer().removePart(this);
        }

        ContentChangeEvent event = ContentChangeEvent.newPartRemovedEvent(this);
        objectHasChanged(event);
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    @Override
    protected void objectHasChanged() {
        IpsModel model = (IpsModel)getIpsModel();
        ContentChangeEvent event = ContentChangeEvent.newPartChangedEvent(this);
        IpsSrcFileContent content = model.getIpsSrcFileContent(getIpsSrcFile());
        if (content != null) {
            content.ipsObjectChanged(event);
        }
    }

    @Override
    protected void objectHasChanged(PropertyChangeEvent propertyChangeEvent) {
        IpsModel model = (IpsModel)getIpsModel();
        ContentChangeEvent event = ContentChangeEvent.newPartChangedEvent(this, propertyChangeEvent);
        IpsSrcFileContent content = model.getIpsSrcFileContent(getIpsSrcFile());
        if (content != null) {
            content.ipsObjectChanged(event);
        }
    }

    @Override
    public IResource getCorrespondingResource() {
        return null;
    }

    @Deprecated
    @Override
    public boolean isValid() throws CoreException {
        return getValidationResultSeverity() != Message.ERROR;
    }

    @Override
    public boolean isValid(IIpsProject ipsProject) throws CoreException {
        return getValidationResultSeverity(ipsProject) != Message.ERROR;
    }

    @Deprecated
    @Override
    public int getValidationResultSeverity() throws CoreException {
        return validate(getIpsProject()).getSeverity();
    }

    @Override
    public int getValidationResultSeverity(IIpsProject ipsProject) throws CoreException {
        return validate(ipsProject).getSeverity();
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        if (id != null) {
            this.id = id;
        } else {
            String s = element.getAttribute(PROPERTY_ID);
            if (StringUtils.isNotEmpty(s)) {
                this.id = s;
            } // else keep the id set in the constructor. migration for old files without id!
        }
    }

    @Override
    protected void propertiesToXml(Element element) {
        element.setAttribute(PROPERTY_ID, "" + id); //$NON-NLS-1$
    }

    @Override
    public void copyFrom(IIpsObjectPartContainer source) {
        ArgumentCheck.isTrue(getClass().equals(source.getClass()));

        Element xmlElement = source.toXml(IpsPlugin.getDefault().getDocumentBuilder().newDocument());
        initFromXml(xmlElement, getId());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + ((id == null) ? 0 : id.hashCode());
        // result = 31 * result + ((parent == null) ? 0 : parent.hashCode());
        return result;
    }

    /**
     * Two parts are equal if they have the same parent and the same id.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof IpsObjectPart)) {
            return false;
        }
        IpsObjectPart other = (IpsObjectPart)obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (getParent() == null) {
            if (other.getParent() != null) {
                return false;
            }
        } else if (!getParent().equals(other.getParent())) {
            return false;
        }
        return true;
    }

}
