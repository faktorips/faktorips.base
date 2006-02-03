package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.text.DateFormat;
import java.util.Locale;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptGenerationPolicyCmptTypeDelta;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.ui.editors.DescriptionPage;
import org.faktorips.devtools.core.ui.editors.TimedIpsObjectEditor;


/**
 *
 */
public class ProductCmptEditor extends TimedIpsObjectEditor {
    
    private boolean dontCreateNewConfigElements = false;
    
    private PropertiesPage propertiesPage;
    private GenerationsPage generationsPage;
    private DescriptionPage descriptionPage;

    public ProductCmptEditor() {
        super();
        IpsPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(new MyPropertyChangeListener());
    }

    /** 
     * Overridden method.
     * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
     */
    protected void addPages() {
        try {
        	propertiesPage = new PropertiesPage(this);
        	generationsPage = new GenerationsPage(this);
        	descriptionPage = new DescriptionPage(this);
            addPage(propertiesPage);
            addPage(generationsPage);
            addPage(descriptionPage);
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }
    
    /** 
     * Overridden.
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
     * Overridden.
     */
    public void partActivated(IWorkbenchPart part) {
        if (part!=this) {
            return;
        }
        checkGeneration();
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
	        	msg.append(Messages.ProductCmptEditor_msgNotContainingAttributes);
	            msg.append(SystemUtils.LINE_SEPARATOR);
	            for (int i=0; i<newAttributes.length; i++) {
	                msg.append(" - "); //$NON-NLS-1$
	                msg.append(newAttributes[i].getName());
	                msg.append(SystemUtils.LINE_SEPARATOR);
	            }
	        }
	        IConfigElement[] elements = delta.getConfigElementsWithMissingAttributes();
	        if (elements.length > 0) {
	            if (msg.toString().length()>0) {
		            msg.append(SystemUtils.LINE_SEPARATOR);
	            }
	        	msg.append(Messages.ProductCmptEditor_msgAttributesNotFound);
	            msg.append(SystemUtils.LINE_SEPARATOR);
	            for (int i=0; i<elements.length; i++) {
	                msg.append(" - "); //$NON-NLS-1$
	                msg.append(elements[i].getName());
	                msg.append(SystemUtils.LINE_SEPARATOR);
	            }
	        }
	        elements = delta.getTypeMismatchElements();
	        if (elements.length > 0) {
	            if (msg.toString().length()>0) {
		            msg.append(SystemUtils.LINE_SEPARATOR);
	            }
	        	msg.append(Messages.ProductCmptEditor_msgTypeMismatch);
	            msg.append(SystemUtils.LINE_SEPARATOR);
	            for (int i=0; i<elements.length; i++) {
	                msg.append(" - "); //$NON-NLS-1$
	                msg.append(elements[i].getName());
	                msg.append(SystemUtils.LINE_SEPARATOR);
	            }
	        }
	        elements = delta.getElementsWithValueSetMismatch();
	        if (elements.length > 0) {
	            if (msg.toString().length()>0) {
		            msg.append(SystemUtils.LINE_SEPARATOR);
	            }
	        	msg.append(Messages.ProductCmptEditor_msgValueAttributeMismatch);
	            msg.append(SystemUtils.LINE_SEPARATOR);
	            for (int i=0; i<elements.length; i++) {
	                msg.append(" - "); //$NON-NLS-1$
	                msg.append(elements[i].getName());
	                msg.append(SystemUtils.LINE_SEPARATOR);
	            }
	        }
	        IProductCmptRelation[] relations = delta.getRelationsWithMissingPcTypeRelations();
	        if (relations.length > 0) {
	            if (msg.toString().length()>0) {
		            msg.append(SystemUtils.LINE_SEPARATOR);
	            }
	        	msg.append(Messages.ProductCmptEditor_msgNoRelationDefined);
	            msg.append(SystemUtils.LINE_SEPARATOR);
	            for (int i=0; i<relations.length; i++) {
	                msg.append(" - "); //$NON-NLS-1$
	                msg.append(relations[i].getName());
	                msg.append(SystemUtils.LINE_SEPARATOR);
	            }
	        }
	        
            msg.append(SystemUtils.LINE_SEPARATOR);
            msg.append(Messages.ProductCmptEditor_msgFixIt);
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
     * Overridden.
     */
    protected String getUniformPageTitle() {
        String title = Messages.ProductCmptEditor_productComponent + getProductCmpt().getName();
        IProductCmptGeneration generation = (IProductCmptGeneration)getActiveGeneration();
        if (generation==null) {
            return title;
        }
        DateFormat format = DateFormat.getDateInstance(DateFormat.DEFAULT);
        String generationConceptName = IpsPreferences.getChangesInTimeNamingConvention().getGenerationConceptNameSingular(Locale.getDefault());
        return title + ", " +  generationConceptName + " " + format.format(generation.getValidFrom().getTime()); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    private void checkGeneration() {
		IProductCmpt prod = getProductCmpt();
		IProductCmptGeneration generation  = (IProductCmptGeneration)prod.getGenerationByEffectiveDate(IpsPreferences.getWorkingDate());

		if (generation == null) {
			// no generation for the _exact_ current working date.
			boolean ok = MessageDialog.openConfirm(getContainer().getShell(), "Generation missmatch on " + getProductCmpt().getName()
					, "No generation available for the current set working date (" + IpsPreferences.getWorkingDate().getTime().toLocaleString() + ")." +
							"Create the missing generation?");
		
			if (ok) {
				// create a new generation and set it active
				getContainer().setEnabled(true);
				IProductCmptGeneration newGen = (IProductCmptGeneration)prod.newGeneration();
				newGen.setValidFrom(IpsPreferences.getWorkingDate());
				propertiesPage.setActiveGeneration(newGen);
			}
			else {
				// no new generation - disable editing
				propertiesPage.setEnabled(false);
			}
		}
		else if (!generation.equals(getActiveGeneration())) {
			propertiesPage.setEnabled(true);
			propertiesPage.setActiveGeneration(generation);
		}
		else {
			propertiesPage.setEnabled(true);
		}
		
    }
            
    private class MyPropertyChangeListener implements IPropertyChangeListener {

		public void propertyChange(PropertyChangeEvent event) {
			// if the this editor is the active on, check for correct generation immediatly


			IProductCmptGeneration generation  = (IProductCmptGeneration)getProductCmpt().getGenerationByEffectiveDate(IpsPreferences.getWorkingDate());
			if (!propertiesPage.getPartControl().isDisposed()) {
				propertiesPage.setEnabled(!(generation == null));
			}
			else {
				IpsPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
			}
		}
    	
    }
}
