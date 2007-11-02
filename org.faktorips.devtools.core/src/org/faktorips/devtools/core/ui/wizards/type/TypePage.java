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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;

/**
 *
 */
public abstract class TypePage extends IpsObjectPage {
    
    private IpsObjectRefControl superTypeControl;
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
    
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        super.fillNameComposite(nameComposite, toolkit);
        toolkit.createFormLabel(nameComposite, Messages.TypePage_superclass);
        superTypeControl = createSupertypeControl(nameComposite, toolkit);
        TextButtonField supertypeField = new TextButtonField(superTypeControl);
        supertypeField.addChangeListener(this);
        
        // Composite options = toolkit.createGridComposite(nameComposite.getParent(), 1, false, false);
        toolkit.createLabel(nameComposite, Messages.TypePage_option);
        overrideCheckbox = toolkit.createCheckbox(nameComposite, Messages.TypePage_overrideAbstractMethods);
        overrideCheckbox.setChecked(true);
    }
    
    protected abstract IpsObjectRefControl createSupertypeControl(Composite container, UIToolkit toolkit);
    
    public String getQualifiedIpsObjectName(){
        StringBuffer buf = new StringBuffer();
        if(!StringUtils.isEmpty(getPackage())){
            buf.append(getPackage());
            buf.append('.');
        }
        buf.append(getIpsObjectName());
        return buf.toString();
    }
    
    /** 
     * {@inheritDoc}
     */
    protected void sourceFolderChanged() {
        super.sourceFolderChanged();
        IIpsPackageFragmentRoot root = getIpsPackageFragmentRoot();
        if (root!=null) {
            superTypeControl.setIpsProject(root.getIpsProject());
        } else {
            superTypeControl.setIpsProject(null);
        }
    }
    
    public String getSuperType() {
        return superTypeControl.getText();
    }
    
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
    
    
    protected void validatePageExtension() throws CoreException {
        //check for name conflicts of the product and policy component type name
        if (pageOfAssociatedType != null && getIpsObjectName() != null && pageOfAssociatedType.getIpsObjectName() != null
                && getIpsObjectName().equals(pageOfAssociatedType.getIpsObjectName())) {
            setErrorMessage("The name of the policy component type conflicts with the name of the product component type");
        }
    }
    
    public void pageEntered() throws CoreException{
        validatePage();
    }

}
