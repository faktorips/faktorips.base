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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.internal.type.TypeValidations;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;
import org.faktorips.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Abstract super type that all IPS objects should extend.
 * 
 * @author unascribed
 */
public abstract class IpsObject extends IpsObjectPartContainer implements IIpsObject {

    /**
     * Flag indicating whether this <code>IpsObject</code> was created from a parsable file content.
     */
    private boolean fromParsableFile = false;

    /**
     * Creates a new <code>IpsObject</code>.
     * 
     * @param file The <code>IIpsSrcFile</code> in which this <code>IpsObject</code> will be stored
     *            in.
     */
    protected IpsObject(IIpsSrcFile file) {
        super(file, (file == null) ? "" : StringUtil.getFilenameWithoutExtension(file.getName())); //$NON-NLS-1$
    }

    /** Constructor for testing purposes. */
    protected IpsObject() {
        // Constructor for testing purposes.
    }

    @Override
    public boolean isFromParsableFile() {
        return fromParsableFile;
    }

    /**
     * Marks the <code>IpsObject</code> as originating from an <code>IIpsSrcFile</code> with an
     * invalid file format.
     */
    void markAsFromUnparsableFile() {
        fromParsableFile = false;
        reinitPartCollections();
    }

    @Override
    public IIpsObject getIpsObject() {
        return this;
    }

    @Override
    public IIpsPackageFragment getIpsPackageFragment() {
        return getIpsSrcFile().getIpsPackageFragment();
    }

    @Override
    public QualifiedNameType getQualifiedNameType() {
        return new QualifiedNameType(getQualifiedName(), getIpsObjectType());
    }

    @Override
    public String getQualifiedName() {
        String folderName = getParent().getParent().getName();
        if (folderName.isEmpty()) {
            return getName();
        }

        return folderName + '.' + getName();
    }

    @Override
    public String getUnqualifiedName() {
        return getName();
    }

    @Override
    public IResource getCorrespondingResource() {
        return null;
    }

    @Override
    public IIpsSrcFile getIpsSrcFile() {
        if (getParent() instanceof IIpsSrcFile) {
            return (IIpsSrcFile)getParent();
        }
        return null;
    }

    /** Notifies the model that this <code>IpsObject</code> has changed. */
    @Override
    protected void objectHasChanged() {
        ContentChangeEvent event = ContentChangeEvent.newWholeContentChangedEvent(getIpsSrcFile());
        objectHasChanged(event);
    }

    @Override
    protected void objectHasChanged(PropertyChangeEvent propertyChangeEvent) {
        ContentChangeEvent event = ContentChangeEvent.newPartChangedEvent(this, propertyChangeEvent);
        objectHasChanged(event);
    }

    @Override
    public boolean isValid(IIpsProject ipsProject) throws CoreException {
        return getValidationResultSeverity(ipsProject) != Severity.ERROR;
    }

    @Override
    public Severity getValidationResultSeverity(IIpsProject ipsProject) throws CoreException {
        return validate(ipsProject).getSeverity();
    }

    @Override
    public IDependency[] dependsOn() {
        return dependsOn(null);
    }

    @Override
    public List<IDependencyDetail> getDependencyDetails(IDependency dependency) throws CoreException {
        if (dependency == null) {
            throw new NullPointerException("Can not get dependency details for null as dependency."); //$NON-NLS-1$
        }

        Map<IDependency, List<IDependencyDetail>> detailMap = new HashMap<>();
        dependsOn(detailMap);

        List<IDependencyDetail> result = detailMap.get(dependency);

        if (result == null) {
            result = new ArrayList<>();
        }

        return result;
    }

    /**
     * Hook for subclasses to implement their own dependency examination. This method returns always
     * an empty array.
     * 
     * @param details The map to collect dependency details for all found dependencies. Can be
     *            <code>null</code> - no details have to be collected in this case.
     * 
     * @return An array of found dependencies. If no dependencies are found, this method has to
     *         return an empty array.
     * 
     */
    protected IDependency[] dependsOn(Map<IDependency, List<IDependencyDetail>> details) {
        return new IDependency[0];
    }

    @Override
    protected final Element createElement(Document doc) {
        return doc.createElement(getIpsObjectType().getXmlElementName());
    }

    @Override
    protected void propertiesToXml(Element element) {
        // @see FIPS-80 why we need to set this attribute
        element.setAttribute(XmlUtil.XML_ATTRIBUTE_SPACE, XmlUtil.XML_ATTRIBUTE_SPACE_VALUE);

        if (getIpsProject().getReadOnlyProperties().isValidateIpsSchema()
                && supportsXmlSchema()) {
            String xmlNamespace = getXmlNamespace();
            element.setAttribute(XMLConstants.XMLNS_ATTRIBUTE, xmlNamespace);
            element.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
                    "xsi:schemaLocation", //$NON-NLS-1$
                    xmlNamespace + " " //$NON-NLS-1$
                            + getXmlSchemaLocation());
        }
    }

    /**
     * Returns whether the XML for this {@link IpsObject} uses an XML schema.
     * 
     * <p>
     * If this method returns {@code true}, {@link #getXmlNamespace()} and
     * {@link #getXmlSchemaLocation()} must also be implemented.
     * </p>
     */
    protected boolean supportsXmlSchema() {
        return isNotBusinessFunction();
    }

    /**
     * Returns the namespace of this object's XML schema.
     *
     * @see #supportsXmlSchema()
     */
    protected String getXmlNamespace() {
        return XmlUtil.XML_IPS_DEFAULT_NAMESPACE;
    }

    /**
     * Returns the location of this object's XML schema.
     *
     * @see #supportsXmlSchema()
     */
    protected String getXmlSchemaLocation() {
        return XmlUtil.getSchemaLocation(getIpsObjectType());
    }

    @SuppressWarnings("deprecation")
    private boolean isNotBusinessFunction() {
        return !IpsObjectType.BUSINESS_FUNCTION.getXmlElementName().equals(getIpsObjectType().getXmlElementName());
    }

    @Override
    public void initFromXml(Element element) {
        fromParsableFile = true;
        super.initFromXml(element);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        // Empty implementation now, not removed because of subclasses calling super implementation.
    }

    @Override
    public String toString() {
        if (getParent() == null) {
            return "unnamed object"; // Can only happen in test cases. //$NON-NLS-1$
        }
        return super.toString();
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
        list.add(TypeValidations.validateUniqueQualifiedName(this));
    }

    /** Validates whether there is another type in the object path with the same name. */
    private void validateSecondIpsObjectWithSameNameTypeInIpsObjectPath(MessageList list, IIpsProject ipsProject) {

        if (ipsProject.findDuplicateIpsSrcFile(getQualifiedNameType())) {
            list.add(new Message(MSGCODE_SAME_IPSOBJECT_IN_IPSOBEJECTPATH_AHEAD,
                    NLS.bind(Messages.IpsObject_msg_OtherIpsObjectAlreadyInPathAhead, getIpsProject()), Message.WARNING,
                    this));
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
    protected void validateNamingConventions(MessageList list, String nameToValidate, String property)
            throws CoreException {

        MessageList mlForNameValidation = new MessageList();
        mlForNameValidation.add(getIpsProject().getNamingConventions()
                .validateUnqualifiedIpsObjectName(getIpsObjectType(), nameToValidate));
        for (Message msg : mlForNameValidation) {
            Message newMsg = new Message(msg.getCode(), msg.getText(), msg.getSeverity(), this, property);
            list.add(newMsg);
        }
    }

    @Override
    public void delete() throws CoreException {
        getIpsSrcFile().delete();
    }

    @Override
    public IIpsSrcFile createCopy(IIpsPackageFragment targetFragment,
            String name,
            boolean force,
            IProgressMonitor monitor) {
        IpsObjectType type = getIpsObjectType();
        String filename = type.getFileName(name);
        Document doc = XmlUtil.getDefaultDocumentBuilder().newDocument();
        Element element;

        IIpsSrcFile ipsSrcFile;
        element = toXml(doc);
        try {
            String encoding = getIpsProject().getXmlFileCharset();
            String contents = XmlUtil.nodeToString(element, encoding);
            ipsSrcFile = targetFragment.createIpsFile(filename, contents, force, monitor);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        return ipsSrcFile;
    }
}
