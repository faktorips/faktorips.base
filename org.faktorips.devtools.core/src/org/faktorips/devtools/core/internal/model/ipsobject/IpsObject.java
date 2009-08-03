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

import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is the abstract super type that all ips objects should extend.
 */
public abstract class IpsObject extends IpsObjectPartContainer implements IIpsObject {

    // The description currently attached to this ips object
    private String description = ""; //$NON-NLS-1$

    // Flag indicating whether this object was created from a parsable file content
    private boolean fromParsableFile = false;

    /**
     * Creates a new ips object.
     * 
     * @param file The ips source file in which this ips object will be stored in.
     */
    protected IpsObject(IIpsSrcFile file) {
        super(file, ""); //$NON-NLS-1$
    }

    /**
     * Constructor for testing purposes.
     */
    protected IpsObject() {

    }

    /**
     * {@inheritDoc}
     */
    public boolean isFromParsableFile() {
        return fromParsableFile;
    }

    /**
     * Marks the ips object as originating from an ips src file with an invalid file format.
     */
    void markAsFromUnparsableFile() {
        fromParsableFile = false;
        this.reinitPartCollections();
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObject getIpsObject() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragment getIpsPackageFragment() {
        return getIpsSrcFile().getIpsPackageFragment();
    }

    /**
     * {@inheritDoc}
     */
    public QualifiedNameType getQualifiedNameType() {
        return new QualifiedNameType(getQualifiedName(), getIpsObjectType());
    }

    /**
     * {@inheritDoc}
     */
    public String getQualifiedName() {
        String folderName = getParent().getParent().getName();
        if (folderName.equals("")) { //$NON-NLS-1$
            return getName();
        }

        return folderName + '.' + getName();
    }

    /**
     * {@inheritDoc}
     */
    public String getUnqualifiedName() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        String filename = getParent().getName();
        int index = filename.indexOf('.');
        if (index == -1) {
            throw new RuntimeException("filename has no extension: " + filename); //$NON-NLS-1$
        }

        return filename.substring(0, index);
    }

    /**
     * {@inheritDoc}
     */
    /*
     * TODO AW: Seems not to be consistent with the JavaDoc, causes bug FS #1513 together with
     * IpsProblemLabelDecorator computeAdornmentFlags().
     */
    public IResource getCorrespondingResource() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IIpsSrcFile getIpsSrcFile() {
        if (getParent() instanceof IIpsSrcFile) {
            return (IIpsSrcFile)getParent();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        if (getIpsSrcFile().exists()) {
            return getIpsObjectType().getEnabledImage();
        } else {
            /*
             * The IPS source file doesn't exists, thus the IPS object couldn't be linked to an IPS
             * source file in the workspace, return the image of the IPS source file to decide
             * between valid and invalid IPS objects.
             */
            return IpsObjectType.IPS_SOURCE_FILE.getEnabledImage();
        }
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isDescriptionChangable() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void setDescription(String newDescription) {
        description = newDescription;
        objectHasChanged();
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return description;
    }

    /**
     * Notifies the model that the object has changed.
     */
    protected void objectHasChanged() {
        ContentChangeEvent event = ContentChangeEvent.newWholeContentChangedEvent(getIpsSrcFile());
        objectHasChanged(event);
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
    public IDependency[] dependsOn() throws CoreException {
        return new IDependency[0];
    }

    /**
     * {@inheritDoc}
     */
    protected final Element createElement(Document doc) {
        return doc.createElement(getIpsObjectType().getXmlElementName());
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        DescriptionHelper.setDescription(element, description);
    }

    /**
     * {@inheritDoc}
     */
    public void initFromXml(Element element) {
        fromParsableFile = true;
        super.initFromXml(element);
    }

    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        description = DescriptionHelper.getDescription(element);
    }

    public String toString() {
        if (getParent() == null) {
            return "unnamed object"; // can only happen in test cases.  //$NON-NLS-1$
        }

        // ips object's name is the same as the file name, so use the
        // parent's to string method
        return getParent().toString();
    }

    /**
     * This operation is extended by <code>IpsObject</code> to perform validations on the name
     * property.
     * 
     * @see #validateNamingConventions(MessageList, String, String)
     */
    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);

        validateNamingConventions(list, getName(), PROPERTY_NAME);
        validateSecondIpsObjectWithSameNameTypeInIpsObjectPath(list, ipsProject);
    }

    // Validates whether there is another type in the object path with the same name
    private void validateSecondIpsObjectWithSameNameTypeInIpsObjectPath(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        IIpsObject otherIpsObject = ipsProject.findIpsObject(getQualifiedNameType());
        if (otherIpsObject != this) {
            list.add(new Message(MSGCODE_SAME_IPSOBJECT_IN_IPSOBEJECTPATH_AHEAD, NLS.bind(
                    Messages.IpsObject_msg_OtherIpsObjectAlreadyInPathAhead, getIpsProject()), Message.WARNING, this));
        }
    }

    /**
     * Validate the naming conventions for the given name.
     * 
     * @param list The list the message will be added if the name is invalid.
     * @param nameToValidate The name which will be validated against the naming conventions.
     * @param property The property which contains the name, the message will be related to this
     *            property.
     */
    @SuppressWarnings("unchecked")
    protected void validateNamingConventions(MessageList list, String nameToValidate, String property)
            throws CoreException {

        MessageList mlForNameValidation = new MessageList();
        mlForNameValidation.add(getIpsProject().getNamingConventions().validateUnqualifiedIpsObjectName(
                getIpsObjectType(), nameToValidate));
        for (Iterator<Message> iter = mlForNameValidation.iterator(); iter.hasNext();) {
            // Create new messages related to this object and the given property
            Message msg = (Message)iter.next();
            Message newMsg = new Message(msg.getCode(), msg.getText(), msg.getSeverity(), this, property);
            list.add(newMsg);
        }
    }

}
