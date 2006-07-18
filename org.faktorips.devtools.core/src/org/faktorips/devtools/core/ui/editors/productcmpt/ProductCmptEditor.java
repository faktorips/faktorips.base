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

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptGenerationPolicyCmptTypeDelta;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.ui.editors.DescriptionPage;
import org.faktorips.devtools.core.ui.editors.TimedIpsObjectEditor;

/**
 * Editor to a edit a product component.
 * 
 * @author Jan Ortmann
 * @author Thorsten Guenther
 */
public class ProductCmptEditor extends TimedIpsObjectEditor {

	private PropertiesPage propertiesPage;

	private GenerationsPage generationsPage;

	private RulesPage rulesPage;

	private DescriptionPage descriptionPage;

	private boolean browseOnly = false;

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
				if (cmpt.findProductCmptType() == null && !IpsPlugin.getDefault().isTestMode()) {
					String msg = NLS.bind(Messages.ProductCmptEditor_msgTemplateNotFound, cmpt.getPolicyCmptType());
					SetTemplateDialog dialog = new SetTemplateDialog(cmpt, getSite().getShell(), msg);
					int button = dialog.open();
					if (button != SetTemplateDialog.OK) {
						addPage(new FormPage(this, Messages.ProductCmptEditor_titleEmpty, "")); //$NON-NLS-1$
						this.close(false);
						return;
					} else {
						checkForInconsistenciesBetweenAttributeAndConfigElements();
					}
				}
				
				propertiesPage = new PropertiesPage(this);
				generationsPage = new GenerationsPage(this);
				descriptionPage = new DescriptionPage(this);
				rulesPage = new RulesPage(this);
				
				addPage(propertiesPage);
				addPage(generationsPage);
				addPage(rulesPage);
				addPage(descriptionPage);
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

		if (isSrcFileUsable()) {
			setActiveGeneration(getPreferredGeneration(), false);
		} 

		browseOnly = IpsPlugin.getDefault().getIpsPreferences().isWorkingModeBrowse();
		
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

		checkForInconsistenciesBetweenAttributeAndConfigElements();
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
	private void checkForInconsistenciesBetweenAttributeAndConfigElements() {
		
		if (!getIpsSrcFile().isMutable()) {
			return;
		}
		
		IProductCmptGeneration generation = (IProductCmptGeneration) getActiveGeneration();
		if (generation == null) {
			return;
		}
		IProductCmptGenerationPolicyCmptTypeDelta delta;
		try {
			delta = generation.computeDeltaToPolicyCmptType();
		} catch (CoreException e) {
			IpsPlugin.logAndShowErrorDialog(e);
			return;
		}
		if (delta == null || delta.isEmpty()) {
			return;
		}

		try {
			StringBuffer msg = new StringBuffer();
			IAttribute[] newAttributes = delta
					.getAttributesWithMissingConfigElements();
			if (newAttributes.length > 0) {
				msg
						.append(Messages.ProductCmptEditor_msgNotContainingAttributes);
				msg.append(SystemUtils.LINE_SEPARATOR);
				for (int i = 0; i < newAttributes.length; i++) {
					msg.append(" - "); //$NON-NLS-1$
					msg.append(newAttributes[i].getName());
					msg.append(SystemUtils.LINE_SEPARATOR);
				}
			}
			IConfigElement[] elements = delta
					.getConfigElementsWithMissingAttributes();
			if (elements.length > 0) {
				if (msg.toString().length() > 0) {
					msg.append(SystemUtils.LINE_SEPARATOR);
				}
				msg.append(Messages.ProductCmptEditor_msgAttributesNotFound);
				msg.append(SystemUtils.LINE_SEPARATOR);
				for (int i = 0; i < elements.length; i++) {
					msg.append(" - "); //$NON-NLS-1$
					msg.append(elements[i].getName());
					msg.append(SystemUtils.LINE_SEPARATOR);
				}
			}
			elements = delta.getTypeMismatchElements();
			if (elements.length > 0) {
				if (msg.toString().length() > 0) {
					msg.append(SystemUtils.LINE_SEPARATOR);
				}
				msg.append(Messages.ProductCmptEditor_msgTypeMismatch);
				msg.append(SystemUtils.LINE_SEPARATOR);
				for (int i = 0; i < elements.length; i++) {
					msg.append(" - "); //$NON-NLS-1$
					msg.append(elements[i].getName());
					msg.append(SystemUtils.LINE_SEPARATOR);
				}
			}
			elements = delta.getElementsWithValueSetMismatch();
			if (elements.length > 0) {
				if (msg.toString().length() > 0) {
					msg.append(SystemUtils.LINE_SEPARATOR);
				}
				msg
						.append(Messages.ProductCmptEditor_msgValueAttributeMismatch);
				msg.append(SystemUtils.LINE_SEPARATOR);
				for (int i = 0; i < elements.length; i++) {
					msg.append(" - "); //$NON-NLS-1$
					msg.append(elements[i].getName());
					msg.append(SystemUtils.LINE_SEPARATOR);
				}
			}
			IProductCmptRelation[] relations = delta
					.getRelationsWithMissingPcTypeRelations();
			if (relations.length > 0) {
				if (msg.toString().length() > 0) {
					msg.append(SystemUtils.LINE_SEPARATOR);
				}
				msg.append(Messages.ProductCmptEditor_msgNoRelationDefined);
				msg.append(SystemUtils.LINE_SEPARATOR);
				for (int i = 0; i < relations.length; i++) {
					msg.append(" - "); //$NON-NLS-1$
					msg.append(relations[i].getName());
					msg.append(SystemUtils.LINE_SEPARATOR);
				}
			}

			msg.append(SystemUtils.LINE_SEPARATOR);
			msg.append(Messages.ProductCmptEditor_msgFixIt);
			boolean fix = MessageDialog.openConfirm(getContainer().getShell(),
					getPartName(), msg.toString());
			if (fix) {
				IIpsModel model = getProductCmpt().getIpsModel();
				model.removeChangeListener(this);
				try {
					generation.fixDifferences(delta);
					setDirty(getIpsSrcFile().isDirty());
					refreshStructure();
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
	 * Triggers a refresh for sturcturals changes. 
	 */
	private void refreshStructure() {
		if (this.propertiesPage != null) {
			this.propertiesPage.refreshStructure();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getUniformPageTitle() {
		if (!isSrcFileUsable()) {
			String filename = getIpsSrcFile()==null?"null":getIpsSrcFile().getName(); //$NON-NLS-1$
			return NLS.bind(Messages.ProductCmptEditor_msgFileOutOfSync, filename);
		}
//		checkGeneration();
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

		if (generation == null) {
			// no generation for the _exact_ current working date.

			if (browseOnly) {
				// check happned before and user decided not to create a new generation - dont bother 
				// the user with repeating questions.
				setPropertiesEnabled((IProductCmptGeneration) getActiveGeneration());
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
		} else if (!generation.equals(getActiveGeneration())) {
			// we found a generation matching the working date, but the found one is not active,
			// so make it active.
			this.setActiveGeneration(generation, false);
		} else {
			setPropertiesEnabled((IProductCmptGeneration) getActiveGeneration());
		}
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
		enabled = enabled && !browseOnly;
		if (enabled) {
			this.setTitleImage(enabledImage);
		} else {
			this.setTitleImage(disabledImage);
		}
		if (propertiesPage != null) {
			propertiesPage.setEnabled(enabled);
		}
	}
	
	/**
	 * Listener to properties with effects on this editor. If changes occur, check if correct generation is displayed.
	 * 
	 * @author Thorsten Guenther
	 */
	private class MyPropertyChangeListener implements IPropertyChangeListener {

		public void propertyChange(PropertyChangeEvent event) {
			if (!active) {
				return;
			}

			String property = event.getProperty();
			if (property.equals(IpsPreferences.WORKING_DATE)) {
				generationManuallySet = false;
				browseOnly = IpsPlugin.getDefault().getIpsPreferences().isWorkingModeBrowse();
				checkGeneration();
				generationsPage.refresh();
				refresh();
			} else if (property
					.equals(IpsPreferences.EDIT_GENERATION_WITH_SUCCESSOR)
					|| property.equals(IpsPreferences.EDIT_RECENT_GENERATION)) {
				setPropertiesEnabled((IProductCmptGeneration) getActiveGeneration());
				refresh();
			} else if (event.getProperty().equals(IpsPreferences.WORKING_MODE)) {
				generationManuallySet = false;
				browseOnly = ((String)event.getNewValue()).equals(IpsPreferences.WORKING_MODE_BROWSE);
				setPropertiesEnabled((IProductCmptGeneration) getActiveGeneration());
				refresh();
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

		if (getActiveGeneration() == null
				|| !getActiveGeneration().equals(generation)) {
			super.setActiveGeneration(generation);
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
	
	
	
	private class CloseHandler extends ShellAdapter implements DisposeListener {
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
		
		private void finish() {
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
				break;

			default:
				IpsPlugin.log(new IpsStatus("Unknown choice: " //$NON-NLS-1$
						+ dialog.getChoice()));
				break;
			}
		}
	}
}
