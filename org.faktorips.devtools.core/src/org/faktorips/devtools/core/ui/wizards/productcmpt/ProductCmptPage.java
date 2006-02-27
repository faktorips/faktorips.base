package org.faktorips.devtools.core.ui.wizards.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.PcTypeRefControl;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;


/**
 *
 */
public class ProductCmptPage extends IpsObjectPage {
    
    private PcTypeRefControl pcTypeControl;
    
    /**
     * @param pageName
     * @param selection
     * @throws JavaModelException
     */
    public ProductCmptPage(IStructuredSelection selection) throws JavaModelException {
        super(selection, Messages.ProductCmptPage_title);
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.wizards.IpsObjectPage#fillNameComposite(org.eclipse.swt.widgets.Composite, UIToolkit)
     */
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        addNameLabelField(toolkit);
        toolkit.createFormLabel(nameComposite, Messages.ProductCmptPage_labelName);
        pcTypeControl = toolkit.createPcTypeRefControl(null, nameComposite);
        TextButtonField pcTypeField = new TextButtonField(pcTypeControl);
        pcTypeField.addChangeListener(this);
    }
    
    String getPolicyCmptType() {
        return pcTypeControl.getText();
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.wizards.IpsObjectPage#sourceFolderChanged()
     */
    protected void sourceFolderChanged() {
        super.sourceFolderChanged();
        IIpsPackageFragmentRoot root = getPdPackageFragmentRoot();
        if (root!=null) {
            pcTypeControl.setPdProject(root.getIpsProject());
        } else {
            pcTypeControl.setPdProject(null);
        }
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.ui.wizards.IpsObjectPage#validatePage()
     */
    protected void validatePage() throws CoreException {
        super.validatePage();
        if (getErrorMessage()!=null) {
            return;
        }
        IPolicyCmptType pcType = pcTypeControl.findPcType();
        if (pcType==null) {
			setErrorMessage(NLS.bind(Messages.ProductCmptPage_msgPolicyClassMissing, pcTypeControl.getText()));
			return;
        }
        updatePageComplete();
    }
}
