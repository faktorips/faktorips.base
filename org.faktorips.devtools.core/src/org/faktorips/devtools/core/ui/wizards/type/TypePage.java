/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.type;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;

/**
 * A abstract superclass that implements common behaviour of the policy and product component type pages. 
 */
public abstract class TypePage extends IpsObjectPage {
    
    private TextButtonField supertypeField;
    private Checkbox overrideCheckbox;
    protected TypePage pageOfAssociatedType;
    
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
        
        // Composite options = toolkit.createGridComposite(nameComposite.getParent(), 1, false, false);
        toolkit.createLabel(nameComposite, Messages.TypePage_option);
        overrideCheckbox = toolkit.createCheckbox(nameComposite, Messages.TypePage_overrideAbstractMethods);
        overrideCheckbox.setChecked(true);
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
     * Returns the value of the override field.
     */
    public boolean overrideAbstractMethods() {
        return overrideCheckbox.isChecked();
    }
    
    /** 
     * {@inheritDoc}
     */
    protected void finishIpsObject(IIpsObject ipsObject) throws CoreException {
        IType type = (IType)ipsObject;
        String supertypeName = getSuperType(); 
        type.setSupertype(supertypeName);
        if (overrideAbstractMethods()) {
            IMethod[] abstractMethods = type.findOverrideMethodCandidates(true, ipsObject.getIpsProject());
            type.overrideMethods(abstractMethods);
        }
    }
    
    /**
     * Checks if the name of the policy component type and product component type of the associated product component type page
     * are not equal. 
     * {@inheritDoc}
     */
    protected void validatePageExtension() throws CoreException {
        //check for name conflicts of the product and policy component type name
        if (pageOfAssociatedType != null && getIpsObjectName() != null && pageOfAssociatedType.getIpsObjectName() != null
                && getIpsObjectName().equals(pageOfAssociatedType.getIpsObjectName())) {
            setErrorMessage(Messages.TypePage_msgNameConflicts);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void pageEntered() throws CoreException{
        super.pageEntered();
        validatePage();
    }

}
