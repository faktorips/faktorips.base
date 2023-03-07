/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsobject;

import java.beans.PropertyChangeEvent;
import java.util.Objects;
import java.util.UUID;

import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.Severity;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.ArgumentCheck;
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
        deleteInternal();
        ContentChangeEvent event = ContentChangeEvent.newPartRemovedEvent(this);
        objectHasChanged(event);
    }

    private void deleteInternal() {
        if (isDeleted()) {
            throw new RuntimeException("Object has already been deleted!"); //$NON-NLS-1$
        }

        markAsDeleted();

        if (getContainer() != null) {
            getContainer().removePart(this);
        }
    }

    void markAsDeleted() {
        deleted = true;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    @Override
    protected void objectHasChanged() {
        IIpsModel model = getIpsModel();
        ContentChangeEvent event = ContentChangeEvent.newPartChangedEvent(this);
        IpsSrcFileContent content = model.getIpsSrcFileContent(getIpsSrcFile());
        if (content != null) {
            content.ipsObjectChanged(event);
        }
    }

    @Override
    protected void objectHasChanged(PropertyChangeEvent propertyChangeEvent) {
        IIpsModel model = getIpsModel();
        ContentChangeEvent event = ContentChangeEvent.newPartChangedEvent(this, propertyChangeEvent);
        IpsSrcFileContent content = model.getIpsSrcFileContent(getIpsSrcFile());
        if (content != null) {
            content.ipsObjectChanged(event);
        }
    }

    @Override
    public AResource getCorrespondingResource() {
        return null;
    }

    @Override
    public boolean isValid(IIpsProject ipsProject) {
        return getValidationResultSeverity(ipsProject) != Severity.ERROR;
    }

    @Override
    public Severity getValidationResultSeverity(IIpsProject ipsProject) {
        return validate(ipsProject).getSeverity();
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        if (id != null) {
            this.id = id;
        } else {
            String s = element.getAttribute(PROPERTY_ID);
            if (IpsStringUtils.isNotEmpty(s)) {
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
        Element xmlElement = source.toXml(XmlUtil.getDefaultDocumentBuilder().newDocument());
        initFromXml(xmlElement, getId());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();

        // result = 31 * result + ((parent == null) ? 0 : parent.hashCode());
        return 31 * result + ((id == null) ? 0 : id.hashCode());
    }

    /**
     * Two parts are equal if they have the same parent and the same id.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof IpsObjectPart other)) {
            return false;
        }
        return Objects.equals(id, other.id)
                && Objects.equals(getParent(), other.getParent());
    }

}
