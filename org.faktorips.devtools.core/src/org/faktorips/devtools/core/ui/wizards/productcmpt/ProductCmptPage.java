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

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.ProductCmptTypeRefControl;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;
import org.faktorips.util.message.MessageList;


/**
 *
 */
public class ProductCmptPage extends IpsObjectPage {
    
    private ProductCmptTypeRefControl typeRefControl;
    private Text versionId;
    private Text constName;
    private Text fullName;
    
    public ProductCmptPage(IStructuredSelection selection) throws JavaModelException {
        super(selection, Messages.ProductCmptPage_title);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {

        toolkit.createFormLabel(nameComposite, Messages.ProductCmptPage_labelTemplate);
        
        typeRefControl = new ProductCmptTypeRefControl(null, nameComposite, toolkit);
        TextButtonField pcTypeField = new TextButtonField(typeRefControl);
        pcTypeField.addChangeListener(this);

        toolkit.createLabel(nameComposite, Messages.ProductCmptPage_labelConstNamePart);
        constName = toolkit.createText(nameComposite);
        String label = NLS.bind(Messages.ProductCmptPage_labelVersionId, IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention().getVersionConceptNameSingular());
        toolkit.createLabel(nameComposite, label);
        versionId = toolkit.createText(nameComposite);

        toolkit.createFormLabel(nameComposite, Messages.ProductCmptPage_labelFullName); 
        
        fullName = addNameField(toolkit);

        updateEnableState();
        
        versionId.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				IProductCmptNamingStrategy ns = getNamingStrategy();
				showMessage(ns.validateVersionId(versionId.getText()));
				updateFullName();
			}
		});
        
        constName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				IProductCmptNamingStrategy ns = getNamingStrategy();
				showMessage(ns.validateKindId(constName.getText()));
				updateFullName();
			}
		});
    }
    
    /**
	 * {@inheritDoc}
     * @throws CoreException 
	 */
	protected void setDefaults(IResource selectedResource) throws CoreException {
		super.setDefaults(selectedResource);
		IIpsObject obj = getSelectedIpsObject();
		IProductCmptNamingStrategy namingStrategy = getNamingStrategy();
		if (!(obj instanceof IProductCmpt)) {
			if (namingStrategy != null && namingStrategy.supportsVersionId()) {
				versionId.setText(namingStrategy.getNextVersionId(null));
				typeRefControl.setFocus();
			}
			return;
		}
		IProductCmpt productCmpt = (IProductCmpt)obj;
		if (namingStrategy != null) {
			if (namingStrategy.supportsVersionId()) {
				versionId.setText(namingStrategy.getNextVersionId(productCmpt));
				constName.setText(namingStrategy.getKindId(namingStrategy.getNextName(productCmpt)));
				constName.setFocus();
			}
		} else {
			setIpsObjectName(productCmpt.getName());
		}
		typeRefControl.setText(productCmpt.findProductCmptType().getQualifiedName());
	}

	String getPolicyCmptType() {
    	String policyCmptTypeName = ""; //$NON-NLS-1$
    	try {
        	String productCmptTypeName = typeRefControl.getText();
			IProductCmptType type = typeRefControl.getIpsProject().findProductCmptType(productCmptTypeName);
			policyCmptTypeName = type.getPolicyCmptyType();
		} catch (CoreException e) {
			IpsPlugin.log(e);
		}
    	return policyCmptTypeName;
    }
    
    /** 
     * {@inheritDoc}
     */
    protected void sourceFolderChanged() {
        super.sourceFolderChanged();
        IIpsPackageFragmentRoot root = getPdPackageFragmentRoot();
        if (root!=null) {
        	typeRefControl.setPdProject(root.getIpsProject());
        } else {
        	typeRefControl.setPdProject(null);
        }
        updateEnableState();
    }
    
    protected void validatePage() throws CoreException {
        super.validatePage();
        if (getErrorMessage()!=null) {
            return;
        }
	    if (typeRefControl.findProductCmptType()==null) {
	        setErrorMessage(NLS.bind(Messages.ProductCmptPage_msgTemplateDoesNotExist, typeRefControl.getText()));
	    }
        updatePageComplete();
    }
    

	/**
	 * {@inheritDoc}
	 */
	protected void validateName() {
		super.validateName();
		if (getErrorMessage() != null) {
			return;
		}
		IProductCmptNamingStrategy ns = getNamingStrategy();
		if (ns != null) {
			MessageList list = ns.validate(getIpsObjectName());
			if (!list.isEmpty()) {
				setErrorMessage(list.getMessage(0).getText());
			}
		}
	}    

	/**
	 * Returns the currentyl active naming strategy. 
	 */
	private IProductCmptNamingStrategy getNamingStrategy()  {
    	IIpsProject project = getIpsProject();
    	if (project != null) {
    		try {
				return project.getProductCmptNamingStratgey();
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
    	}
    	return null;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void packageChanged() {
		super.packageChanged();
		updateEnableState();
	}
	
	private void updateEnableState() {
		IProductCmptNamingStrategy namingStrategy = getNamingStrategy();
		boolean enabled = namingStrategy != null && namingStrategy.supportsVersionId();
		this.constName.setEnabled(enabled);
		this.versionId.setEnabled(enabled);
		this.fullName.setEnabled(!enabled);
		
		if (enabled) {
			if (namingStrategy.validate(fullName.getText()).isEmpty()) {
				versionId.setText(namingStrategy.getVersionId(fullName.getText()));
				constName.setText(namingStrategy.getKindId(fullName.getText()));
			}
		}
	}
	
	private void showMessage(MessageList list) {
		if (!list.isEmpty()) {
			setErrorMessage(list.getMessage(0).getText());
		} else {
			setErrorMessage(null);
		}
	}
	
	private void updateFullName() {
		fullName.setText(getNamingStrategy().getProductCmptName(constName.getText(), versionId.getText()));
	}
}
