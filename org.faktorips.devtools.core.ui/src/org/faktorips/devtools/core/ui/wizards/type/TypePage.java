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
 * A abstract superclass that implements common behaviour of the policy and product component type pages. 
 */
public abstract class TypePage extends IpsObjectPage {
    
    private TextButtonField supertypeField;
    protected CheckboxField abstractField;
    protected TypePage pageOfAssociatedType;
    private boolean alreadyBeenEntered = false;
    
    /**
     * @param pageName
     * @param selection
     * @throws JavaModelException
     */
    public TypePage(IpsObjectType ipsObjectType, IStructuredSelection selection, String pageName) throws JavaModelException {
        super(ipsObjectType, selection, pageName);
        setTitle(pageName);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        super.fillNameComposite(nameComposite, toolkit);
        toolkit.createFormLabel(nameComposite, Messages.TypePage_superclass);
        IpsObjectRefControl superTypeControl = createSupertypeControl(nameComposite, toolkit);
        supertypeField = new TextButtonField(superTypeControl);
        supertypeField.addChangeListener(this);
    }

    protected void addAbstractField(Composite nameComposite, UIToolkit toolkit){
        toolkit.createLabel(nameComposite, ""); //$NON-NLS-1$
        abstractField = new CheckboxField(toolkit.createCheckbox(nameComposite, "Abstract"));
        abstractField.addChangeListener(this);
    }
    
    /**
     * Delegates to supertypeChanged() if the supertype control has been edited.
     */
    protected void valueChangedExtension(FieldValueChangedEvent e) throws CoreException{
        if(e.field == supertypeField){
            supertypeChanged(supertypeField);
        }
    }

    /**
     * Empty by default. Subclasses can override it to react input changes of the super type control.
     * 
     * @throws CoreException are logged
     */
    protected void supertypeChanged(TextButtonField supertypeField) throws CoreException {
    }
    
    /**
     * The type specific control must be instantiation and returned by this method. 
     */
    protected abstract IpsObjectRefControl createSupertypeControl(Composite container, UIToolkit toolkit);

    /** 
     * {@inheritDoc}
     */
    protected void sourceFolderChanged() {
        super.sourceFolderChanged();
        IIpsPackageFragmentRoot root = getIpsPackageFragmentRoot();
        if (root!=null) {
            ((IpsObjectRefControl)supertypeField.getControl()).setIpsProject(root.getIpsProject());
        } else {
            ((IpsObjectRefControl)supertypeField.getControl()).setIpsProject(null);
        }
    }
    
    public boolean getAbstract(){
        return ((Boolean)abstractField.getValue()).booleanValue();
    }
    
    public void setAbstract(boolean value){
        abstractField.setValue(new Boolean(value));
    }
    
    /**
     * Returns the value of the super type field.
     */
    public String getSuperType() {
        return (String)supertypeField.getValue();
    }

    /**
     * Sets the value of the super type field.
     */
    public void setSuperType(String superTypeQualifiedName){
        supertypeField.setValue(superTypeQualifiedName);
    }
    
    /** 
     * {@inheritDoc}
     */
    protected void finishIpsObjects(IIpsObject newIpsObject, List modifiedIpsObjects) throws CoreException {
        IType type = (IType)newIpsObject;
        String supertypeName = getSuperType(); 
        type.setSupertype(supertypeName);
        type.setAbstract(getAbstract());
    }
    
    /**
     * Checks if the name of the policy component type and product component type of the associated product component type page
     * are not equal. 
     * {@inheritDoc}
     */
    protected void validatePageExtension() throws CoreException {
        //check for name conflicts of the product and policy component type name
        if (pageOfAssociatedType != null && 
            getIpsObjectName() != null && 
            pageOfAssociatedType.getIpsObjectName() != null && 
            !StringUtils.isEmpty(getIpsObjectName()) && 
            !StringUtils.isEmpty(pageOfAssociatedType.getIpsObjectName()) && 
            getIpsObjectName().equals(pageOfAssociatedType.getIpsObjectName())) {
                setErrorMessage(Messages.TypePage_msgNameConflicts);
        }
        //check if selected super type exists
        if(!StringUtils.isEmpty(getSuperType())){
            IIpsSrcFile ipsSrcFile = getIpsProject().findIpsSrcFile(getIpsObjectType(), getSuperType());
            if(ipsSrcFile == null){
                setErrorMessage(Messages.TypePage_msgSupertypeDoesNotExist);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void pageEntered() throws CoreException{
        super.pageEntered();
        validatePage();
        alreadyBeenEntered = true;
    }

    public boolean isAlreadyBeenEntered() {
        return alreadyBeenEntered;
    }

}
