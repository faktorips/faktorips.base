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

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
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
    
    /**
     * @param pageName
     * @param selection
     * @throws JavaModelException
     */
    public TypePage(IStructuredSelection selection, String title) throws JavaModelException {
        super(selection, title);
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
}
