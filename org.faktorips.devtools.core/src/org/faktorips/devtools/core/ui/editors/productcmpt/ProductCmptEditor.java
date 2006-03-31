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
import java.util.Locale;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
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
 *
 */
public class ProductCmptEditor extends TimedIpsObjectEditor {

	private PropertiesPage propertiesPage;

	private GenerationsPage generationsPage;

	private RulesPage rulesPage;

	private DescriptionPage descriptionPage;

	private GregorianCalendar referenceDate;

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

	/**
	 * Creates a new editor for product components.
	 */
	public ProductCmptEditor() {
		super();
		IpsPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(
				new WorkingDateChangeListener());
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
			setActiveGeneration(getPreferredGeneration());
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
	 * Does what the methodname says :-)
	 */
	private void checkForInconsistenciesBetweenAttributeAndConfigElements() {
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
		checkGeneration();
		return Messages.ProductCmptEditor_productComponent
				+ getProductCmpt().getName();
	}

	/**
	 * Checks if the currently active generations valid-from-date matches exactly the currently set
	 * working date. If not so, a search for a matching geneartion is started. If nothing found, the user
	 * is asked to create one. 
	 */
	private void checkGeneration() {

		IProductCmpt prod = getProductCmpt();
		IProductCmptGeneration generation = (IProductCmptGeneration) prod
				.getGenerationByEffectiveDate(IpsPreferences.getWorkingDate());

		if (generation == null) {
			if (this.referenceDate != null
					&& this.referenceDate.equals(IpsPreferences
							.getWorkingDate())) {
				// check happned before and user decided not to create a new generation - dont bother 
				// the user with repeating questions.
				setPropertiesEnabled((IProductCmptGeneration) getActiveGeneration());

				return;
			}

			this.referenceDate = IpsPreferences.getWorkingDate();
			// no generation for the _exact_ current working date.
			IpsPreferences prefs = IpsPlugin.getDefault().getIpsPreferences();
			String gen = prefs.getChangesOverTimeNamingConvention()
					.getGenerationConceptNameSingular(Locale.getDefault());
			String message = Messages.bind(
					Messages.ProductCmptEditor_msg_GenerationMissmatch, prefs
							.getFormattedWorkingDate(), gen);
			String title = Messages.bind(
					Messages.ProductCmptEditor_title_GenerationMissmatch,
					getProductCmpt().getName(), gen);
			
			boolean ok;
			if (IpsPlugin.getDefault().isTestMode()) {
				ok = IpsPlugin.getDefault().getTestAnswerProvider().getBooleanAnswer();
			}
			else {
				ok = MessageDialog.openConfirm(getContainer().getShell(),
						title, message);
			}

			if (ok) {
				// create a new generation and set it active
				IProductCmptGeneration newGen = (IProductCmptGeneration) prod
						.newGeneration(IpsPreferences.getWorkingDate());
				this.setActiveGeneration(newGen);
			} else {
				// no new generation - disable editing
				this.setActiveGeneration(this.getPreferredGeneration());
			}
		} else if (!generation.equals(getActiveGeneration())) {
			// we found a generation matching the working date, but the found one is not active,
			// so make it active.
			this.setActiveGeneration(generation);
		} else {
			setPropertiesEnabled((IProductCmptGeneration) getActiveGeneration());
		}
	}

	/**
	 * Enable or disable the properties page.
	 */
	private void setPropertiesEnabled(IProductCmptGeneration generation) {
		boolean enabled = isEditableGeneration(generation);
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
	 * Listener to the working-date-property. If changes occur, check if correct generation is displayed.
	 * 
	 * @author Thorsten Guenther
	 */
	private class WorkingDateChangeListener implements IPropertyChangeListener {

		public void propertyChange(PropertyChangeEvent event) {
			if (!active) {
				return;
			}

			String property = event.getProperty();
			if (property.equals(IpsPreferences.WORKING_DATE)) {
				checkGeneration();
				generationsPage.refresh();
			} else if (property
					.equals(IpsPreferences.EDIT_GENERATION_WITH_SUCCESSOR)
					|| property.equals(IpsPreferences.EDIT_RECENT_GENERATION)) {
				setPropertiesEnabled((IProductCmptGeneration) getActiveGeneration());
			}

		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setActiveGeneration(IIpsObjectGeneration generation) {
		if (generation == null) {
			return;
		}

		if (getActiveGeneration() == null
				|| !getActiveGeneration().equals(generation)) {
			super.setActiveGeneration(generation);
			refreshStructure();
		}

		setPropertiesEnabled((IProductCmptGeneration) generation);
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
				.getGenerationByEffectiveDate(IpsPreferences.getWorkingDate()))) {
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
}
