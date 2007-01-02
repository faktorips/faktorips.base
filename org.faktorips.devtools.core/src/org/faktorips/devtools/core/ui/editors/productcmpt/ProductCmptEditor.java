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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.product.ProductCmpt;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptGenerationPolicyCmptTypeDelta;
import org.faktorips.devtools.core.ui.editors.DescriptionPage;
import org.faktorips.devtools.core.ui.editors.TimedIpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.productcmpt.deltapresentation.ProductCmptDeltaDialog;

/**
 * Editor to a edit a product component.
 * 
 * @author Jan Ortmann
 * @author Thorsten Guenther
 */
public class ProductCmptEditor extends TimedIpsObjectEditor {

	private GenerationPropertiesPage generationPropertiesPage;

	private ProductCmptPropertiesPage productCmptPropertiesPage;

	private DescriptionPage descriptionPage;

	// flag is true if the user has manually chosen the active generation
    private boolean activeGenerationManuallySet = false;
	
    // The working date that is used in the editor. This has to be stored in the editor
    // as it can differ from the global working date when the user changes the global working date.
    private GregorianCalendar workingDateUsedInEditor = null;
    
    private boolean isHandlingWorkingDateMismatch = false;
    
	/**
	 * Creates a new editor for product components.
	 */
	public ProductCmptEditor() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void addPages() {
		try {
			if (isSrcFileUsable()) {
				IProductCmpt cmpt = (ProductCmpt)getIpsObject();
				if (getIpsSrcFile().isMutable() && cmpt.findProductCmptType() == null && !IpsPlugin.getDefault().isTestMode()) {
					String msg = NLS.bind(Messages.ProductCmptEditor_msgTemplateNotFound, cmpt.getPolicyCmptType());
					SetTemplateDialog dialog = new SetTemplateDialog(cmpt, getSite().getShell(), msg);
					dialog.open();
				}
				
				generationPropertiesPage = new GenerationPropertiesPage(this);
                productCmptPropertiesPage = new ProductCmptPropertiesPage(this);
				descriptionPage = new DescriptionPage(this);
				
				addPage(generationPropertiesPage);
				addPage(productCmptPropertiesPage);
				addPage(descriptionPage);
                IIpsObjectGeneration gen = getGenerationEffectiveOnCurrentEffectiveDate();
                if (gen==null) {
                    gen = cmpt.getGenerations()[cmpt.getNumOfGenerations()-1];            
                }
                setActiveGeneration(gen, false);
			}
			else {
				MissingResourcePage page = new MissingResourcePage(this, getIpsSrcFile());
				addPage(page);
			}
		} catch (Exception e) {
			IpsPlugin.logAndShowErrorDialog(e);
		}
	}

	/**
	 * Returns the product component for the sourcefile edited with this editor.
	 */
	IProductCmpt getProductCmpt() {
		try {
			return (IProductCmpt) getIpsSrcFile().getIpsObject();
		} catch (Exception e) {
			IpsPlugin.logAndShowErrorDialog(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void editorActivated() {
        updateChosenActiveGeneration();
		super.editorActivated();
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getUniformPageTitle() {
		if (!isSrcFileUsable()) {
			String filename = getIpsSrcFile()==null?"null":getIpsSrcFile().getName(); //$NON-NLS-1$
			return NLS.bind(Messages.ProductCmptEditor_msgFileOutOfSync, filename);
		}
		return Messages.ProductCmptEditor_productComponent
				+ getProductCmpt().getName();
	}

	/**
	 * Checks if the currently active generations valid-from-date matches exactly the currently set
	 * working date. If not so, a search for a matching generation is started. If nothing is found, the user
	 * is asked to create a new one. 
	 */
	private void updateChosenActiveGeneration() {
		IProductCmpt prod = getProductCmpt();
		GregorianCalendar workingDate = IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
		IProductCmptGeneration generation = (IProductCmptGeneration)prod.getGenerationByEffectiveDate(workingDate);

        if (generation!=null) {
            workingDateUsedInEditor = workingDate;
            if (!generation.equals(getActiveGeneration())) {
                // we found a generation matching the working date, but the found one is not active,
                // so make it active.
                this.setActiveGeneration(generation, false);
            }
            return;
        }
        // no generation for the _exact_ current working date.
		if (workingDate.equals(workingDateUsedInEditor)) {
			// check happned before and user decided not to create a new generation - dont bother 
			// the user with repeating questions.
            return;
		}
		IpsPreferences prefs = IpsPlugin.getDefault().getIpsPreferences();
		if (prefs.isWorkingModeBrowse()) {
			// just browsing - show the generation valid at working date
			if (!activeGenerationManuallySet) {
				showGenerationEffectiveOn(prefs.getWorkingDate());
			}
			return;
		}
        handleWorkingDateMissmatch();
	}

    /**
     * {@inheritDoc}
     */
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (property.equals(IpsPreferences.WORKING_DATE)) {
			activeGenerationManuallySet = false;
			updateChosenActiveGeneration();
		} else if (property.equals(IpsPreferences.EDIT_RECENT_GENERATION)) {
            refresh();
		} else if (event.getProperty().equals(IpsPreferences.WORKING_MODE)) {
			activeGenerationManuallySet = false;
            // refresh is done in superclass
		}
        super.propertyChange(event);
	}

	public void setActiveGeneration(IIpsObjectGeneration generation, boolean manuallySet) {
		if (generation == null) {
			return;
		}
		if (!generation.equals(getActiveGeneration())) {
			super.setActiveGeneration(generation);
            if (generationPropertiesPage!=null) {
                generationPropertiesPage.rebuildInclStructuralChanges();
            }
		}
        refresh();
        activeGenerationManuallySet = manuallySet;
	}
    
    protected boolean computeDataChangeableState() {
        if (!super.computeDataChangeableState()) {
            return false;
        }
        try {
            return getProductCmpt().findProductCmptType()!=null;
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return false;
        }
    }
    
    boolean couldDateBeChangedIfProductCmptTypeWasntMissing() {
        return super.computeDataChangeableState();
    }
    
    /**
     * Returns <code>true</code> if the active generation is editable, otherwise <code>false</code>.
     */
    public boolean isActiveGenerationEditable() {
        return isGenerationEditable((IProductCmptGeneration)getActiveGeneration());
    }

    /**
     * Returns <code>true</code> if the given generation is editable, otherwise <code>false</code>.
     */
    public boolean isGenerationEditable(IProductCmptGeneration gen) {
        if (gen==null) {
            return false;
        }
		// if generation is not effective in the current effective date, no editing is possible
		if (!gen.equals(getGenerationEffectiveOnCurrentEffectiveDate())) {
			return false;
		}
        if (!gen.getIpsSrcFile().isMutable()) {
            return false;
        }
        if (gen.isValidFromInPast()!=null && gen.isValidFromInPast().booleanValue()) {
            IpsPreferences pref = IpsPlugin.getDefault().getIpsPreferences();
            return pref.canEditRecentGeneration();
		}
		return true;
	}
	
	private void showGenerationEffectiveOn(GregorianCalendar date) {
		IIpsObjectGeneration generation = getProductCmpt().findGenerationEffectiveOn(date);
		if (generation == null) {
			generation = getProductCmpt().getFirstGeneration();
		}
		setActiveGeneration(generation, false);
	}
	
	private void handleWorkingDateMissmatch() {
        // following if statement is there as closing the dialog triggers a window activated event
        // and handling in the evant calls this method.  
        if (isHandlingWorkingDateMismatch) {
            return;
        }
        isHandlingWorkingDateMismatch = true;
		IProductCmpt cmpt = getProductCmpt();
		GenerationSelectionDialog dialog = new GenerationSelectionDialog(getContainer().getShell(), cmpt);
		dialog.open(); // closing the dialog triggers an window activation event
        isHandlingWorkingDateMismatch = false;
        int choice = GenerationSelectionDialog.CHOICE_BROWSE;
        if (IpsPlugin.getDefault().isTestMode()) {
            choice = IpsPlugin.getDefault().getTestAnswerProvider().getIntAnswer();
        }
        else {
            if (dialog.getReturnCode() == GenerationSelectionDialog.OK) {
                choice = dialog.getChoice();
            }
        }
        GregorianCalendar workingDate = IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
        workingDateUsedInEditor = workingDate;
        switch (choice) {
            case GenerationSelectionDialog.CHOICE_BROWSE:
                setActiveGeneration(cmpt.findGenerationEffectiveOn(workingDate), false);
                break;
    
            case GenerationSelectionDialog.CHOICE_CREATE:
                setActiveGeneration(cmpt.newGeneration(workingDate), false);
                break;
    
            case GenerationSelectionDialog.CHOICE_SWITCH:
                IProductCmptGeneration generation = dialog.getSelectedGeneration();
                if (generation == null) {
                    generation = (IProductCmptGeneration) getProductCmpt()
                            .getFirstGeneration();
                }
                setActiveGeneration(generation, true);
                IpsPreferences prefs = IpsPlugin.getDefault().getIpsPreferences();
                prefs.setWorkingDate(generation.getValidFrom());
                break;
    
            default:
                IpsPlugin.log(new IpsStatus("Unknown choice: " //$NON-NLS-1$
                        + dialog.getChoice()));
                break;
        }
	}
	
    /**
     * {@inheritDoc}
     * @throws CoreException 
     */
    protected Dialog createDialogToFixDifferencesToModel() throws CoreException {

        IIpsObjectGeneration[] gen = this.getProductCmpt().getGenerations();
        IProductCmptGeneration[] generations = new IProductCmptGeneration[gen.length];
        for (int i = 0; i < generations.length; i++) {
            generations[i] = (IProductCmptGeneration)gen[i];
        }
        IProductCmptGenerationPolicyCmptTypeDelta[] deltas = new IProductCmptGenerationPolicyCmptTypeDelta[generations.length];
        for (int i = 0; i < generations.length; i++) {          
                deltas[i] = ((IProductCmptGeneration)generations[i]).computeDeltaToPolicyCmptType();
        }
        
        return new ProductCmptDeltaDialog(generations, deltas, getSite().getShell());
    }
    
    /**
     * {@inheritDoc}
     */
    public void contentsChanged(final ContentChangeEvent event) {
        if (event.getIpsSrcFile().equals(getIpsSrcFile())) {
            if (event.getEventType()==ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED) {
                this.workingDateUsedInEditor = null;
                this.activeGenerationManuallySet = false;
            }
        }
        super.contentsChanged(event);
    }

    /**
     * {@inheritDoc}
     */
    protected void refreshInclStructuralChanges() {
        try {
            getIpsSrcFile().getIpsObject();
            updateChosenActiveGeneration();
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        if (generationPropertiesPage!=null) {
            generationPropertiesPage.rebuildInclStructuralChanges();
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void setDataChangeable(boolean changeable) {
        if (changeable){
            changeable = isActiveGenerationEditable();
        }
        super.setDataChangeable(changeable);
    }
}
