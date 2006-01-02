package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.text.DateFormat;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptGenerationPolicyCmptTypeDelta;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.ui.editors.DescriptionPage;
import org.faktorips.devtools.core.ui.editors.TimedPdoEditor;


/**
 *
 */
public class ProductCmptEditor extends TimedPdoEditor {
    
    private boolean dontCreateNewConfigElements = false;

    public ProductCmptEditor() {
        super();
    }

    /** 
     * Overridden method.
     * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
     */
    protected void addPages() {
        try {
            addPage(new PropertiesPage(this));
            addPage(new GenerationsPage(this));
            addPage(new DescriptionPage(this));
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }
    
    /** 
     * Overridden method.
     * @see org.eclipse.ui.IEditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
     */
    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        super.init(site, input);
    }
    
    IProductCmpt getProductCmpt() {
        try {
            return (IProductCmpt)getPdSrcFile().getIpsObject();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            throw new RuntimeException(e);
        }
    }
    
    /** 
     * Overridden method.
     * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
     */
    public void partActivated(IWorkbenchPart part) {
        if (part!=this) {
            return;
        }
        if (dontCreateNewConfigElements) {
            return;
        }
        checkForInconsistenciesBetweenAttributeAndConfigElements();
        // dontCreateNewConfigElements = true;
    }
    
    private void checkForInconsistenciesBetweenAttributeAndConfigElements() {
        IProductCmptGeneration generation = (IProductCmptGeneration)getActiveGeneration();
        if (generation==null) {
            return;
        }
        IProductCmptGenerationPolicyCmptTypeDelta delta;
        try {
            delta = generation.computeDeltaToPolicyCmptType();
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return;
        }
        if (delta.isEmpty()) {
            return;
        }
        
        try {
            StringBuffer msg = new StringBuffer();
	        IAttribute[] newAttributes = delta.getAttributesWithMissingConfigElements();
	        if (newAttributes.length > 0) {
	        	msg.append("The product component does not contain the following attributes/formulas:");
	            msg.append(SystemUtils.LINE_SEPARATOR);
	            for (int i=0; i<newAttributes.length; i++) {
	                msg.append(" - ");
	                msg.append(newAttributes[i].getName());
	                msg.append(SystemUtils.LINE_SEPARATOR);
	            }
	        }
	        IConfigElement[] elements = delta.getConfigElementsWithMissingAttributes();
	        if (elements.length > 0) {
	            if (msg.toString().length()>0) {
		            msg.append(SystemUtils.LINE_SEPARATOR);
	            }
	        	msg.append("The following attributes/formulas can't be found in the template:");
	            msg.append(SystemUtils.LINE_SEPARATOR);
	            for (int i=0; i<elements.length; i++) {
	                msg.append(" - ");
	                msg.append(elements[i].getName());
	                msg.append(SystemUtils.LINE_SEPARATOR);
	            }
	        }
	        elements = delta.getTypeMismatchElements();
	        if (elements.length > 0) {
	            if (msg.toString().length()>0) {
		            msg.append(SystemUtils.LINE_SEPARATOR);
	            }
	        	msg.append("There is a type mismatch in the following attributes/formulas:");
	            msg.append(SystemUtils.LINE_SEPARATOR);
	            for (int i=0; i<elements.length; i++) {
	                msg.append(" - ");
	                msg.append(elements[i].getName());
	                msg.append(SystemUtils.LINE_SEPARATOR);
	            }
	        }
	        elements = delta.getElementsWithValueSetMismatch();
	        if (elements.length > 0) {
	            if (msg.toString().length()>0) {
		            msg.append(SystemUtils.LINE_SEPARATOR);
	            }
	        	msg.append("There is a mismatch between the value sets of the following attributes:");
	            msg.append(SystemUtils.LINE_SEPARATOR);
	            for (int i=0; i<elements.length; i++) {
	                msg.append(" - ");
	                msg.append(elements[i].getName());
	                msg.append(SystemUtils.LINE_SEPARATOR);
	            }
	        }
	        IProductCmptRelation[] relations = delta.getRelationsWithMissingPcTypeRelations();
	        if (relations.length > 0) {
	            if (msg.toString().length()>0) {
		            msg.append(SystemUtils.LINE_SEPARATOR);
	            }
	        	msg.append("For the following relations no corresponding relation can be found in the template:");
	            msg.append(SystemUtils.LINE_SEPARATOR);
	            for (int i=0; i<relations.length; i++) {
	                msg.append(" - ");
	                msg.append(relations[i].getName());
	                msg.append(SystemUtils.LINE_SEPARATOR);
	            }
	        }
	        
            msg.append(SystemUtils.LINE_SEPARATOR);
            msg.append("Do you wan't to fix the differences?");
            boolean fix = MessageDialog.openConfirm(getContainer().getShell(), getPartName(), msg.toString());
            if (fix) {
                IIpsModel model = getProductCmpt().getIpsModel();
                model.removeChangeListener(this);
                try {
                    generation.fixDifferences(delta);
                    setDirty(getPdSrcFile().isDirty());
                    refresh();
                    getContainer().update();
                } finally {
                    model.addChangeListener(this);
                }
            }
	        
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsObjectEditor#getUniformPageTitle()
     */
    protected String getUniformPageTitle() {
        IProductCmptGeneration generation = (IProductCmptGeneration)getActiveGeneration();
        String title = "Product Component: " + getProductCmpt().getName();
        if (generation==null) {
            return title;
        }
        DateFormat format = DateFormat.getDateInstance(DateFormat.DEFAULT);
        return title + ", Generation " + format.format(generation.getValidFrom().getTime());
    }
        
}
