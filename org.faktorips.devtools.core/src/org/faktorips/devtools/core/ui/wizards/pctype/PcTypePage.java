package org.faktorips.devtools.core.ui.wizards.pctype;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.PcTypeRefControl;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;


/**
 *
 */
public class PcTypePage extends IpsObjectPage implements ValueChangeListener {
    
    private PcTypeRefControl superTypeControl;
    private Checkbox overrideCheckbox;
    
    /**
     * @param pageName
     * @param selection
     * @throws JavaModelException
     */
    public PcTypePage(IStructuredSelection selection) throws JavaModelException {
        super(selection, Messages.PcTypePage_title);
    }
    
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        super.fillNameComposite(nameComposite, toolkit);
        toolkit.createFormLabel(nameComposite, Messages.PcTypePage_labelSuperclass);
        superTypeControl = toolkit.createPcTypeRefControl(null, nameComposite);
        TextButtonField supertypeField = new TextButtonField(superTypeControl);
        supertypeField.addChangeListener(this);
        
        // Composite options = toolkit.createGridComposite(nameComposite.getParent(), 1, false, false);
        toolkit.createLabel(nameComposite, Messages.PcTypePage_labelOption);
        overrideCheckbox = toolkit.createCheckbox(nameComposite, Messages.PcTypePage_labelOverride);
        overrideCheckbox.setChecked(true);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.wizards.IpsObjectPage#sourceFolderChanged()
     */
    protected void sourceFolderChanged() {
        super.sourceFolderChanged();
        IIpsPackageFragmentRoot root = getPdPackageFragmentRoot();
        if (root!=null) {
            superTypeControl.setPdProject(root.getIpsProject());
        } else {
            superTypeControl.setPdProject(null);
        }
    }
    
    String getSuperType() {
        return superTypeControl.getText();
    }
    
    public boolean overrideAbstractMethods() {
        return overrideCheckbox.isChecked();
    }
}
