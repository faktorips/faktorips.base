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

package org.faktorips.devtools.core.ui.wizards.type;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;

/**
 * An abstract superclass that implements common behaviour of the policy and product component type
 * pages.
 */
public abstract class TypePage extends IpsObjectPage {

    /** The checkbox field to set the abstract property. */
    protected CheckboxField abstractField;

    /** The wizard type page of the associated (product or policy) type. */
    protected TypePage pageOfAssociatedType;

    // The text field to choose the supertype
    private TextButtonField supertypeField;

    // Flag inidcating whether this type page has already been entered
    private boolean alreadyBeenEntered;

    /**
     * @param pageName
     * @param selection
     * @throws JavaModelException
     */
    public TypePage(IpsObjectType ipsObjectType, IStructuredSelection selection, String pageName)
            throws JavaModelException {

        super(ipsObjectType, selection, pageName);
        alreadyBeenEntered = false;
        setTitle(pageName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        super.fillNameComposite(nameComposite, toolkit);

        toolkit.createFormLabel(nameComposite, Messages.TypePage_superclass);
        IpsObjectRefControl superTypeControl = createSupertypeControl(nameComposite, toolkit);
        supertypeField = new TextButtonField(superTypeControl);
        /*
         * SW 9.4.2010: Super-type field should not return "null", even if the null-presentation is
         * matched. Prior to this fix opening the Wizard would cause an NPE in the
         * QualifiedNameType's constructor, if the null-presentation was set to "" (empty String).
         * The empty TextField then matched the null-presentation and thus findPCType() was called
         * with null argument.
         */
        supertypeField.setSupportsNull(false);
        supertypeField.addChangeListener(this);
    }

    /**
     * 
     * 
     * @param nameComposite
     * @param toolkit
     */
    protected void addAbstractField(Composite nameComposite, UIToolkit toolkit) {
        toolkit.createLabel(nameComposite, ""); //$NON-NLS-1$
        abstractField = new CheckboxField(toolkit.createCheckbox(nameComposite, "Abstract"));
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
     * @throws CoreException Subclasses may throw at any time.
     */
    protected void supertypeChanged(TextButtonField supertypeField) throws CoreException {

    }

    /**
     * The type specific control must be instantiated and returned by this method.
     * 
     * @param container
     * @param toolkit
     * 
     * @return
     */
    protected abstract IpsObjectRefControl createSupertypeControl(Composite container, UIToolkit toolkit);

    /**
     * {@inheritDoc}
     */
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
        return ((Boolean)abstractField.getValue()).booleanValue();
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
        return (String)supertypeField.getValue();
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void finishIpsObjects(IIpsObject newIpsObject, List<IIpsObject> modifiedIpsObjects) throws CoreException {
        IType type = (IType)newIpsObject;
        String supertypeName = getSuperType();
        type.setSupertype(supertypeName);
        type.setAbstract(getAbstract());
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Checks if the name of the policy component type and product component type of the associated
     * product component type page are not equal.
     * </p>
     */
    @Override
    protected void validatePageExtension() throws CoreException {
        // Check for name conflicts of the product and policy component type name
        if (pageOfAssociatedType != null && getIpsObjectName() != null
                && pageOfAssociatedType.getIpsObjectName() != null && !StringUtils.isEmpty(getIpsObjectName())
                && !StringUtils.isEmpty(pageOfAssociatedType.getIpsObjectName())
                && getIpsObjectName().equals(pageOfAssociatedType.getIpsObjectName())) {
            setErrorMessage(Messages.TypePage_msgNameConflicts);
        }

        // Check if selected super type exists
        if (!StringUtils.isEmpty(getSuperType())) {
            IIpsSrcFile ipsSrcFile = getIpsProject().findIpsSrcFile(getIpsObjectType(), getSuperType());
            if (ipsSrcFile == null) {
                setErrorMessage(Messages.TypePage_msgSupertypeDoesNotExist);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
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

}
