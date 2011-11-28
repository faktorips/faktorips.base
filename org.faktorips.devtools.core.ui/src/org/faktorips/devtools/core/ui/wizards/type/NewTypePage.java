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

package org.faktorips.devtools.core.ui.wizards.type;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsValidation;
import org.faktorips.devtools.core.IpsValidationTask;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * An abstract superclass that implements common behavior of the policy and product component type
 * pages.
 */
public abstract class NewTypePage extends IpsObjectPage {

    /** The checkbox field to set the abstract property. */
    protected CheckboxField abstractField;

    /** The wizard type page of the associated (product or policy) type. */
    private NewTypePage pageOfAssociatedType;

    /** The text field to choose the supertype */
    private TextButtonField supertypeField;

    /** Flag indicating whether this type page has already been entered */
    private boolean alreadyBeenEntered;

    public NewTypePage(IpsObjectType ipsObjectType, IStructuredSelection selection, String pageName) {
        super(ipsObjectType, selection, pageName);
        alreadyBeenEntered = false;
        setTitle(pageName);
    }

    @Override
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        super.fillNameComposite(nameComposite, toolkit);

        toolkit.createFormLabel(nameComposite, Messages.NewTypePage_superclass);
        IpsObjectRefControl superTypeControl = createSupertypeControl(nameComposite, toolkit);
        supertypeField = new TextButtonField(superTypeControl);
        supertypeField.addChangeListener(this);
    }

    protected void addAbstractField(Composite nameComposite, UIToolkit toolkit) {
        toolkit.createLabel(nameComposite, ""); //$NON-NLS-1$
        abstractField = new CheckboxField(toolkit.createCheckbox(nameComposite, "Abstract")); //$NON-NLS-1$
        abstractField.addChangeListener(this);
    }

    /**
     * Delegates to <code>supertypeChanged()</code> if the supertype control has been edited.
     */
    @Override
    protected void valueChangedExtension(FieldValueChangedEvent e) throws CoreException {
        if (e.field == supertypeField) {
            supertypeChanged(supertypeField);
        }
    }

    /**
     * Empty by default. Subclasses can override it to react input changes of the super type
     * control.
     * 
     * @param supertypeField The text button field used to select the supertype.
     * 
     * @throws CoreException Subclasses may throw at any time.
     */
    protected void supertypeChanged(TextButtonField supertypeField) throws CoreException {
        // Empty default implementation
    }

    /**
     * The type specific control must be instantiated and returned by this method.
     */
    protected abstract IpsObjectRefControl createSupertypeControl(Composite container, UIToolkit toolkit);

    @Override
    protected void sourceFolderChanged() {
        super.sourceFolderChanged();

        IIpsPackageFragmentRoot root = getIpsPackageFragmentRoot();
        if (root != null) {
            ((IpsObjectRefControl)supertypeField.getControl()).setIpsProject(root.getIpsProject());
        } else {
            ((IpsObjectRefControl)supertypeField.getControl()).setIpsProject(null);
        }
    }

    /**
     * Returns the value of the abstract checkbox field.
     * 
     * @return A flag indicating whether the abstract checkbox field is checked (<code>true</code>)
     *         or not ( <code>false</code>).
     */
    public boolean getAbstract() {
        return (abstractField.getValue()).booleanValue();
    }

    /**
     * Sets the value of the abstract checkbox field.
     * 
     * @param value If <code>true</code> the abstract checkbox field will be checked, if
     *            <code>false</code> it won't be checked.
     */
    public void setAbstract(boolean value) {
        abstractField.setValue(new Boolean(value));
    }

    /**
     * Returns the value of the super type field.
     * 
     * @return A <code>String</code> representing the current value of the super type field.
     */
    public String getSuperType() {
        return supertypeField.getValue();
    }

    /**
     * Sets the value of the super type field.
     * 
     * @param superTypeQualifiedName The qualified name of the super type that will be written into
     *            the super type field.
     */
    public void setSuperType(String superTypeQualifiedName) {
        supertypeField.setValue(superTypeQualifiedName);
    }

    @Override
    protected void finishIpsObjectsExtension(IIpsObject newIpsObject, Set<IIpsObject> modifiedIpsObjects)
            throws CoreException {

        IType type = (IType)newIpsObject;
        String supertypeName = getSuperType();
        type.setSupertype(supertypeName);
        type.setAbstract(getAbstract());
    }

    @Override
    public void pageEntered() throws CoreException {
        super.pageEntered();

        validatePage();
        alreadyBeenEntered = true;
    }

    /**
     * Returns whether this wizard page has already been entered.
     * 
     * @return A flag indicating whether this wizard page has already been entered.
     */
    public boolean isAlreadyBeenEntered() {
        return alreadyBeenEntered;
    }

    public void setPageOfAssociatedType(NewTypePage pageOfAssociatedType) {
        this.pageOfAssociatedType = pageOfAssociatedType;
    }

    public NewTypePage getPageOfAssociatedType() {
        return pageOfAssociatedType;
    }

    /**
     * Checks if the name of the policy component type and product component type of the associated
     * product component type page are not equal.
     */
    @Override
    protected void validatePageExtension() throws CoreException {
        if (!isCurrentPage()) {
            return;
        }

        IpsValidation validation = new IpsValidation();
        validation.addTask(new ValidateNameConflicts());
        validation.addTask(new ValidateSupertypeExists());

        validatePageExtensionThis(validation);

        MessageList result = validation.validate(getIpsProject());
        if (!result.isEmpty()) {
            setMessage(result.getMessageWithHighestSeverity());
        }
    }

    protected abstract void validatePageExtensionThis(IpsValidation validation) throws CoreException;

    /**
     * Check for name conflicts of the product and policy component type name.
     */
    private class ValidateNameConflicts extends IpsValidationTask {

        @Override
        public Message execute(IIpsProject ipsProject) throws CoreException {
            if (pageOfAssociatedType != null && getIpsObjectName() != null
                    && pageOfAssociatedType.getIpsObjectName() != null && !StringUtils.isEmpty(getIpsObjectName())
                    && !StringUtils.isEmpty(pageOfAssociatedType.getIpsObjectName())
                    && getIpsObjectName().equals(pageOfAssociatedType.getIpsObjectName())) {
                return new Message("", Messages.NewTypePage_msgNameConflicts, Message.ERROR); //$NON-NLS-1$
            }
            return null;
        }

    }

    /**
     * Check if selected super type exists.
     */
    private class ValidateSupertypeExists extends IpsValidationTask {

        @Override
        public Message execute(IIpsProject ipsProject) throws CoreException {
            if (!StringUtils.isEmpty(getSuperType())) {
                IIpsSrcFile ipsSrcFile = ipsProject.findIpsSrcFile(getIpsObjectType(), getSuperType());
                if (ipsSrcFile == null) {
                    return new Message("", Messages.NewTypePage_msgSupertypeDoesNotExist, Message.ERROR); //$NON-NLS-1$
                }
            }
            return null;
        }

    }

}
