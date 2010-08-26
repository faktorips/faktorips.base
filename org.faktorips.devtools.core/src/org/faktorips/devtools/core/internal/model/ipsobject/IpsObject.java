/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.ipsobject.refactor.MoveIpsObjectProcessor;
import org.faktorips.devtools.core.internal.model.ipsobject.refactor.RenameIpsObjectProcessor;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IDependencyDetail;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Abstract super type that all IPS objects should extend.
 * 
 * @author unascribed
 */
public abstract class IpsObject extends IpsObjectPartContainer implements IIpsObject {

    /** Flag indicating whether this <tt>IpsObject</tt> was created from a parsable file content. */
    private boolean fromParsableFile = false;

    /**
     * Creates a new <tt>IpsObject</tt>.
     * 
     * @param file The <tt>IIpsSrcFile</tt> in which this <tt>IpsObject</tt> will be stored in.
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
     * Marks the <tt>IpsObject</tt> as originating from an <tt>IIpsSrcFile</tt> with an invalid file
     * format.
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
        if (folderName.equals("")) { //$NON-NLS-1$
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

    /** Notifies the model that this <tt>IpsObject</tt> has changed. */
    @Override
    protected void objectHasChanged() {
        ContentChangeEvent event = ContentChangeEvent.newWholeContentChangedEvent(getIpsSrcFile());
        objectHasChanged(event);
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
    public IDependency[] dependsOn() throws CoreException {
        return dependsOn(null);
    }

    @Override
    public List<IDependencyDetail> getDependencyDetails(IDependency dependency) throws CoreException {
        if (dependency == null) {
            throw new NullPointerException("Can not get dependency details for null as dependency."); //$NON-NLS-1$
        }

        Map<IDependency, List<IDependencyDetail>> detailMap = new HashMap<IDependency, List<IDependencyDetail>>();
        dependsOn(detailMap);

        List<IDependencyDetail> result = detailMap.get(dependency);

        if (result == null) {
            result = new ArrayList<IDependencyDetail>();
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
     * @throws CoreException exceptions can be wrapped into CoreExceptions and propagated by this
     *             method
     */
    protected IDependency[] dependsOn(Map<IDependency, List<IDependencyDetail>> details) throws CoreException {
        return new IDependency[0];
    }

    @Override
    protected final Element createElement(Document doc) {
        return doc.createElement(getIpsObjectType().getXmlElementName());
    }

    @Override
    protected void propertiesToXml(Element element) {
        // Empty implementation now, not removed because of subclasses calling super implementation.
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
            return "unnamed object"; // Can only happen in test cases.  //$NON-NLS-1$
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
    }

    /** Validates whether there is another type in the object path with the same name. */
    private void validateSecondIpsObjectWithSameNameTypeInIpsObjectPath(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        IIpsObject otherIpsObject = ipsProject.findIpsObject(getQualifiedNameType());
        if (otherIpsObject != null && otherIpsObject != this) {
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
    protected void validateNamingConventions(MessageList list, String nameToValidate, String property)
            throws CoreException {

        MessageList mlForNameValidation = new MessageList();
        mlForNameValidation.add(getIpsProject().getNamingConventions().validateUnqualifiedIpsObjectName(
                getIpsObjectType(), nameToValidate));
        for (Message msg : mlForNameValidation) {
            Message newMsg = new Message(msg.getCode(), msg.getText(), msg.getSeverity(), this, property);
            list.add(newMsg);
        }
    }

    @Override
    public ProcessorBasedRefactoring getRenameRefactoring() {
        return new ProcessorBasedRefactoring(new RenameIpsObjectProcessor(this));
    }

    @Override
    public ProcessorBasedRefactoring getMoveRefactoring() {
        return new ProcessorBasedRefactoring(new MoveIpsObjectProcessor(this));
    }

}
