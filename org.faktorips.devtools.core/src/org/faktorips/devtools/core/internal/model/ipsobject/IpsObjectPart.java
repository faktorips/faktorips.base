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

package org.faktorips.devtools.core.internal.model.ipsobject;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.w3c.dom.Element;

/**
 * 
 */
public abstract class IpsObjectPart extends IpsObjectPartContainer implements IIpsObjectPart {

    private String description = ""; //$NON-NLS-1$
    private int id;
    private boolean deleted = false;
    protected boolean descriptionChangable = true;

    protected IpsObjectPart(IIpsObject parent, int id) {
        super(parent, ""); //$NON-NLS-1$
        this.id = id;
    }

    protected IpsObjectPart(IIpsObjectPart parent, int id) {
        super(parent, ""); //$NON-NLS-1$
        this.id = id;
    }

    /**
     * Constructor for testing purposes.
     */
    protected IpsObjectPart() {

    }

    /**
     * {@inheritDoc}
     */
    public int getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return description;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDescriptionChangable() {
        return descriptionChangable;
    }

    /**
     * {@inheritDoc}
     */
    public void setDescription(String newDescription) {
        ArgumentCheck.notNull(description, this);
        ArgumentCheck.isTrue(isDescriptionChangable(), "The description attribute of this object is marked " + //$NON-NLS-1$
                "as not changeable. See the method isDescriptionChangable() for explanation"); //$NON-NLS-1$

        String oldDescription = description;
        description = newDescription;

        valueChanged(oldDescription, newDescription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void objectHasChanged() {
        IpsModel model = (IpsModel)getIpsModel();
        ContentChangeEvent event = ContentChangeEvent.newPartChangedEvent(this);
        model.getIpsSrcFileContent(getIpsSrcFile()).ipsObjectChanged(event);
    }

    /**
     * {@inheritDoc}
     */
    public IResource getCorrespondingResource() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isValid() throws CoreException {
        return getValidationResultSeverity() != Message.ERROR;
    }

    /**
     * {@inheritDoc}
     */
    public int getValidationResultSeverity() throws CoreException {
        return validate(getIpsProject()).getSeverity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initPropertiesFromXml(Element element, Integer id) {
        if (id != null) {
            this.id = id.intValue();
        } else {
            String s = element.getAttribute(PROPERTY_ID);
            if (!StringUtils.isEmpty(s)) {
                this.id = Integer.parseInt(s);
            } // else keep the id set in the constructor. migration for old files without id!
        }

        if (isDescriptionChangable()) {
            description = DescriptionHelper.getDescription(element);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void propertiesToXml(Element element) {
        element.setAttribute(PROPERTY_ID, "" + id); //$NON-NLS-1$
        if (isDescriptionChangable()) {
            DescriptionHelper.setDescription(element, description);
        }
    }

    /**
     * {@inheritDoc} Two parts are equal if the have the same parent and the same id.
     */
    // TODO XXX CD missing hashCode override
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IIpsObjectPart)) {
            return false;
        }

        IIpsObjectPart other = (IIpsObjectPart)o;

        return other.getId() == getId()
                && ((parent == null && other.getParent() == null) || (parent != null && parent
                        .equals(other.getParent())));

    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class adapterType) {
        // ok, to suppress unchecked warnings, as the method signature is defined by Eclipse
        if (IResource.class.isAssignableFrom(adapterType)) {
            /*
             * This prevents the CVSDecorator from displaying decorations for ipsobjectparts in
             * ModelExplorer and ProductExplorer.
             */
            return null;
        } else {
            return super.getAdapter(adapterType);
        }
    }
}
