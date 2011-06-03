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

package org.faktorips.devtools.core.internal.model.ipsobject;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
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
    public IResource getCorrespondingResource() {
        return null;
    }

    @Override
    public boolean isValid() throws CoreException {
        return getValidationResultSeverity() != Message.ERROR;
    }

    @Override
    public boolean isValid(IIpsProject ipsProject) throws CoreException {
        return getValidationResultSeverity(ipsProject) != Message.ERROR;
    }

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
            if (!StringUtils.isEmpty(s)) {
                this.id = s;
            } // else keep the id set in the constructor. migration for old files without id!
        }
    }

    @Override
    protected void propertiesToXml(Element element) {
        element.setAttribute(PROPERTY_ID, "" + id); //$NON-NLS-1$
    }

    @Override
    public void copy(IIpsObjectPartContainer target) {
        ArgumentCheck.isTrue(getClass().equals(target.getClass()));

        Element xmlElement = toXml(IpsPlugin.getDefault().getDocumentBuilder().newDocument());
        ((IpsObjectPartContainer)target).initFromXml(xmlElement, ((IIpsObjectPart)target).getId());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + ((id == null) ? 0 : id.hashCode());
        result = 31 * result + ((parent == null) ? 0 : parent.hashCode());
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
        if (parent == null) {
            if (other.parent != null) {
                return false;
            }
        } else if (!parent.equals(other.parent)) {
            return false;
        }
        return true;
    }

    // ok, to suppress unchecked warnings, because the method signature is defined by Eclipse
    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class adapterType) {
        if (IResource.class.isAssignableFrom(adapterType) | ResourceMapping.class.isAssignableFrom(adapterType)) {
            /*
             * This prevents the CVSLightweightDecorator from displaying decorations for
             * ipsobjectparts in ModelExplorer and ProductExplorer.
             */
            return null;
        }
        return super.getAdapter(adapterType);
    }

}
