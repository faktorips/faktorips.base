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

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.product.ProductCmpt;
import org.faktorips.devtools.core.model.IIpsModel;
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

	private PropertiesPage propertiesPage;

	private ProductCmptPropertiesPage productCmptPropertiesPage;

	private DescriptionPage descriptionPage;

	/**
	 * Flag that indicates whether this editor is currently active or not.
	 */
	private boolean active = false;

	/**
	 * Image used in editor titlebar if editor is enabled
	 */ 
	private Image enabledImage;

	/**
	 * Image used in editor titlebar if editor is disabled
	 */
	private Image disabledImage;

	private boolean generationManuallySet = false;
	
	private IPropertyChangeListener propertyChangeListener;

	/**
	 * Flag that is <code>true</code> if this editor is enabled (what means that the properties-page is editable).
	 */
	private boolean enabled = true;
	
    // The working date that is used in the editor. This has to be stored in the editor
    // as it can differ from the global working date when the user chnages the global working date.
    private GregorianCalendar workingDateUsedInEditor = null;
    
	/**
	 * Storage for the decision of the user not to fix differences existing between attributes
	 * and config elements to supress repeating questions to the user.
	 */
	private boolean dontFixDifferencesBetweenAttributeAndConfigElement = false;
	
	/**
	 * Flag indicating an open delta-dialog if <code>true</code>.
	 */
	private boolean deltasShowing = false;
	
	/**
	 * Creates a new editor for product components.
	 */
	public ProductCmptEditor() {
		super();
		propertyChangeListener = new MyPropertyChangeListener();
		IpsPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(propertyChangeListener);
		enabledImage = IpsPlugin.getDefault().getImage("ProductCmpt.gif"); //$NON-NLS-1$
		disabledImage = IpsPlugin.getDefault()
				.getImage("lockedProductCmpt.gif"); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	protected void addPages() {
		try {
			if (isSrcFileUsable()) {
				IProductCmpt cmpt = (ProductCmpt)getIpsObject();
                //TODO needs to be changed when the isHistorical() method is available on IpsSrcFile
				if (cmpt.findProductCmptType() == null && 
                    !IpsPlugin.getDefault().isTestMode() &&
                    !cmpt.getIpsSrcFile().isHistoric()) {
					String msg = NLS.bind(Messages.ProductCmptEditor_msgTemplateNotFound, cmpt.getPolicyCmptType());
					SetTemplateDialog dialog = new SetTemplateDialog(cmpt, getSite().getShell(), msg);
					int button = dialog.open();
					if (button != SetTemplateDialog.OK) {
						addPage(new FormPage(this, Messages.ProductCmptEditor_titleEmpty, "")); //$NON-NLS-1$
						this.close(false);
						return;
					} else {
						checkForInconsistenciesBetweenAttributeAndConfigElements(false);
					}
				}
				
				propertiesPage = new PropertiesPage(this);
                productCmptPropertiesPage = new ProductCmptPropertiesPage(this);
				descriptionPage = new DescriptionPage(this);
				
				addPage(propertiesPage);
				addPage(productCmptPropertiesPage);
				addPage(descriptionPage);
                setActiveGeneration(getPreferredGeneration(), false);
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
	 * {@inheritDoc}
	 */
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
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
	public void partActivated(IWorkbenchPartReference partRef) {
		super.partActivated(partRef);
		IWorkbenchPart part = partRef.getPart(false);
		if (part != this || !isSrcFileUsable()) {
			return;
		}
		active = true;
		checkGeneration();
		checkForInconsistenciesBetweenAttributeAndConfigElements(false);
	}

	/**
	 * {@inheritDoc}
	 */
	public void partDeactivated(IWorkbenchPartReference partRef) {
		super.partDeactivated(partRef);
		active = false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void partClosed(IWorkbenchPartReference partRef) {
		super.partClosed(partRef);
		IpsPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(propertyChangeListener);
	}

	/**
	 * Does what the methodname says :-)
	 */
	private void checkForInconsistenciesBetweenAttributeAndConfigElements(boolean force) {
		if (!force) {
			if (!this.enabled || !getIpsSrcFile().isMutable() || deltasShowing) {
	    		// no modifications for read-only-editors
				return;
			}
			
			if (dontFixDifferencesBetweenAttributeAndConfigElement) {
			    // user decided not to fix the differences some time ago...
				return;
			}			
		}
		
		if (getContainer() == null) {
			// do nothing, we will be called again later. This avoids that the user
			// is shown the differences-dialog twice if openening the editor...
			return;
		}
		
		IIpsObjectGeneration[] gen = this.getProductCmpt().getGenerations();
		IProductCmptGeneration[] generations = new IProductCmptGeneration[gen.length];
		for (int i = 0; i < generations.length; i++) {
			generations[i] = (IProductCmptGeneration)gen[i];
		}

		IProductCmptGenerationPolicyCmptTypeDelta[] deltas = new IProductCmptGenerationPolicyCmptTypeDelta[generations.length];
		boolean deltaFound = false;
		for (int i = 0; i < generations.length; i++) {			
			try {
				deltas[i] = ((IProductCmptGeneration)generations[i]).computeDeltaToPolicyCmptType();
				deltaFound = deltaFound || (deltas[i] != null && !deltas[i].isEmpty()); 
			} catch (CoreException e) {
				IpsPlugin.logAndShowErrorDialog(e);
				deltas[i] = null;
			}
		}

		if (!deltaFound) {
			return;
		}

		deltasShowing = true;
		Shell shell = getSite().getShell();
		ProductCmptDeltaDialog dialog = new ProductCmptDeltaDialog(generations, deltas, shell);
		dialog.setBlockOnOpen(true);
		int result = dialog.open();
		
		boolean fix = result == ProductCmptDeltaDialog.OK;
		if (fix) {
			IIpsModel model = getProductCmpt().getIpsModel();
            
            try {
                model.runAndQueueChangeEvents(new DifferenceFixer(generations, deltas), null);
                super.refresh();
            }
            catch (CoreException e) {
                IpsPlugin.log(e);
            }
		}
		else {
			dontFixDifferencesBetweenAttributeAndConfigElement = true;
		}
        deltasShowing = false;
	}

	/**
	 * Triggers a refresh for sturcturals changes. 
	 */
	private void refreshStructure() {
		
		if (this.propertiesPage != null) {
			this.propertiesPage.refreshStructure();
		}
	}
	
	public void refresh() {
		refreshInternal(false);
	}
	
	public void forceRefresh() {
		refreshInternal(true);
	}
	
	private void refreshInternal(boolean force) {
		checkForInconsistenciesBetweenAttributeAndConfigElements(force);
		super.refresh();
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
	 * working date. If not so, a search for a matching geneartion is started. If nothing found, the user
	 * is asked to create one. 
	 */
	private void checkGeneration() {

		if (!getIpsSrcFile().isMutable()) {
			setPropertiesEnabled(false);
			return;
		}

		IProductCmpt prod = getProductCmpt();
		GregorianCalendar workingDate = IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
		IProductCmptGeneration generation = (IProductCmptGeneration) prod
				.getGenerationByEffectiveDate(workingDate);

        if (generation!=null) {
            workingDate = workingDateUsedInEditor;
            if (!generation.equals(getActiveGeneration())) {
                // we found a generation matching the working date, but the found one is not active,
                // so make it active.
                this.setActiveGeneration(generation, false);
            } else {
                setPropertiesEnabled((IProductCmptGeneration) getActiveGeneration());
            }
            return;
        }
        // no generation for the _exact_ current working date.
		if (workingDate.equals(workingDateUsedInEditor)) {
			// check happned before and user decided not to create a new generation - dont bother 
			// the user with repeating questions.
			setPropertiesEnabled((IProductCmptGeneration) getActiveGeneration());
			return;
		}
		
        // TODO Mit Thorsten klaeren: Wieso wird hier 2x auf WorkingMode Browse geprueft. 
		if (IpsPlugin.getDefault().getIpsPreferences().isWorkingModeBrowse()) {
			// Editor is on browse-mode.
			setPropertiesEnabled(false);
			return;
		}
		
		IpsPreferences prefs = IpsPlugin.getDefault().getIpsPreferences();
		if (prefs.isWorkingModeBrowse()) {
			// just browsing - show the generation valid at working date
			if (!generationManuallySet) {
				showGenerationEffectiveOn(prefs.getWorkingDate());
			} 
			else {
				setPropertiesEnabled(false);
			}
			return;
		}
		
		handleWorkingDateMissmatch(getContainer().getShell());
	}

	/**
	 * Enable the properties page if the given generation is editable or disable the page 
	 * if not. 
	 */
	protected void setPropertiesEnabled(IProductCmptGeneration generation) {
		setPropertiesEnabled(isEditableGeneration(generation));
	}

	/**
	 * Set the enablement-state of the properties page.
	 */
	private void setPropertiesEnabled(boolean enabled) {
		this.enabled = enabled && !IpsPlugin.getDefault().getIpsPreferences().isWorkingModeBrowse();
		if (this.enabled) {
			this.setTitleImage(enabledImage);
			checkForInconsistenciesBetweenAttributeAndConfigElements(false);
		} else {
			this.setTitleImage(disabledImage);
		}
		if (propertiesPage != null) {
			propertiesPage.setEnabled(this.enabled);
		}
	}
	
	/**
	 * Listener to properties with effects on this editor. If changes occur, check if correct generation is displayed.
	 * 
	 * @author Thorsten Guenther
	 */
	private class MyPropertyChangeListener implements IPropertyChangeListener {

        /**
         * {@inheritDoc}
         */
		public void propertyChange(PropertyChangeEvent event) {
            if (!active) {
				return;
			}

			String property = event.getProperty();
			if (property.equals(IpsPreferences.WORKING_DATE)) {
				generationManuallySet = false;
				checkGeneration();
                productCmptPropertiesPage.refresh();
				refreshInternal(false);
			} else if (property
					.equals(IpsPreferences.EDIT_GENERATION_WITH_SUCCESSOR)
					|| property.equals(IpsPreferences.EDIT_RECENT_GENERATION)) {
				setPropertiesEnabled((IProductCmptGeneration) getActiveGeneration());
				refreshInternal(false);
			} else if (event.getProperty().equals(IpsPreferences.WORKING_MODE)) {
				generationManuallySet = false;
				setPropertiesEnabled((IProductCmptGeneration) getActiveGeneration());
				refreshInternal(false);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setActiveGeneration(IIpsObjectGeneration generation) {
		setActiveGeneration(generation, true);
	}

	private void setActiveGeneration(IIpsObjectGeneration generation, boolean rememberDecision) {
		if (generation == null) {
			return;
		}
		if (!generation.equals(getActiveGeneration())) {
			super.setActiveGeneration(generation);
            updateGenerationPropertiesPageTab();
			refreshStructure();
		}
		setPropertiesEnabled((IProductCmptGeneration) generation);
		generationManuallySet = generationManuallySet || rememberDecision;
	}

	/**
	 * Checks whether the given generation can be edited respecting the preferences 
	 */
	protected boolean isEditableGeneration(IProductCmptGeneration generation) {

		if (generation == null) {
			return false;
		}
		
		// if generation does not match the current set working date, no editing will ever
		// be possible, so return false immediate
		if (!generation.equals(this.getProductCmpt()
				.getGenerationByEffectiveDate(IpsPlugin.getDefault().getIpsPreferences().getWorkingDate()))) {
			return false;
		}

		GregorianCalendar validFrom = generation.getValidFrom();
		GregorianCalendar now = new GregorianCalendar();
        
        // because now contains the current time incliding hour, minute and second, but
        // validFrom does not, we have to set the fields for hour, minute, second and millisecond
        // to 0 to get an editable generation which is valid from today. The field AM_PM has to be 
        // set to AM, too. 
        now.set(GregorianCalendar.HOUR, 0);
        now.set(GregorianCalendar.AM_PM, GregorianCalendar.AM);
        now.set(GregorianCalendar.MINUTE, 0);
        now.set(GregorianCalendar.SECOND, 0);
        now.set(GregorianCalendar.MILLISECOND, 0);
        
		boolean editable = true;

		if (now.after(validFrom)) {
			editable = IpsPlugin.getDefault().getIpsPreferences()
					.canEditRecentGeneration();
		}

		IIpsObjectGeneration[] generations = this.getProductCmpt()
				.getGenerations();
		for (int i = 0; i < generations.length; i++) {
			if (generations[i].getValidFrom().after(validFrom)) {
				return IpsPlugin.getDefault().getIpsPreferences()
						.canEditGenerationsWithSuccesor()
						&& editable;
			}
		}

		return editable;
	}
	
	private void showGenerationEffectiveOn(GregorianCalendar date) {
		IIpsObjectGeneration generation = getProductCmpt().findGenerationEffectiveOn(date);
		if (generation == null) {
			generation = getProductCmpt().getFirstGeneration();
		}
		setActiveGeneration(generation, false);
	}
    
    /**
     * Update the tab-content (text and image) for the tab of the generation properties page.
     */
    protected void updateGenerationPropertiesPageTab() {
        propertiesPage.updateTabname();
    }
	
	private void handleWorkingDateMissmatch(Shell shell) {
		IProductCmpt cmpt = getProductCmpt();
		GenerationSelectionDialog dialog = new GenerationSelectionDialog(shell, cmpt);
		dialog.open();
	
		CloseHandler handler = new CloseHandler(dialog);
		if (!IpsPlugin.getDefault().isTestMode()) {
			dialog.getShell().addShellListener(handler);
			dialog.getShell().addDisposeListener(handler);
		}
		else {
			dialog.close();
			handler.widgetDisposed(null);
		}
	}
	
	private abstract class AbstractCloseHandler extends ShellAdapter implements DisposeListener {
		public void widgetDisposed(DisposeEvent e) {
			finish();
		}

		/**
		 * {@inheritDoc}
		 */
		public void shellClosed(ShellEvent e) {
			finish();
		}

		protected abstract void finish();
	}
	
	private class CloseHandler extends AbstractCloseHandler {
		private GenerationSelectionDialog dialog;
		
		public CloseHandler(GenerationSelectionDialog dialog) {
			this.dialog = dialog;
		}
		
		public void widgetDisposed(DisposeEvent e) {
			finish();
		}

		/**
		 * {@inheritDoc}
		 */
		public void shellClosed(ShellEvent e) {
			finish();
		}
		
		protected void finish() {
			boolean ok = true;
			int choice = GenerationSelectionDialog.CHOICE_BROWSE;
			if (IpsPlugin.getDefault().isTestMode()) {
				choice = IpsPlugin.getDefault().getTestAnswerProvider().getIntAnswer();
			}
			else {
				ok = dialog.getReturnCode() == GenerationSelectionDialog.OK;
				if (ok) {
					choice = dialog.getChoice();
				}
			}
			
			if (!ok) {
				close(false);
				return;
			}
		
			IProductCmpt cmpt = getProductCmpt();
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
				IpsPreferences prefs = IpsPlugin.getDefault()
						.getIpsPreferences();
				prefs.setWorkingDate(generation.getValidFrom());
				setPropertiesEnabled(true);
				break;

			default:
				IpsPlugin.log(new IpsStatus("Unknown choice: " //$NON-NLS-1$
						+ dialog.getChoice()));
				break;
			}
		}
	}
    
    private class DifferenceFixer implements IWorkspaceRunnable {
        IProductCmptGeneration[] generations;
        IProductCmptGenerationPolicyCmptTypeDelta[] deltas;
        
        public DifferenceFixer(IProductCmptGeneration[] generations, IProductCmptGenerationPolicyCmptTypeDelta[] deltas) {
            this.generations = generations;
            this.deltas = deltas;
        }
        
        /**
         * {@inheritDoc}
         */
        public void run(IProgressMonitor monitor) throws CoreException {
            for (int i = 0; i < generations.length; i++) {
                try {
                    generations[i].fixDifferences(deltas[i]);
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }
            setDirty(getIpsSrcFile().isDirty());
            refreshStructure();
            getContainer().update();
        }
        
    }
}
