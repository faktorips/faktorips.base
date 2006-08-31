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

package org.faktorips.devtools.core.ui.wizards.deepcopy;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.product.DeepCopyOperation;
import org.faktorips.devtools.core.internal.model.product.ProductCmptStructure;
import org.faktorips.devtools.core.model.CycleException;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptReference;

/**
 * A wizard to create a deep copy from a given product component.
 * 
 * @author Thorsten Guenther
 */
public class DeepCopyWizard extends Wizard {
	
	public static final int TYPE_COPY_PRODUCT = 10;
	public static final int TYPE_NEW_VERSION = 100;
	
	private ProductCmptStructure structure;
	private SourcePage sourcePage;
	private ReferenceAndPreviewPage previewPage;
	private IProductCmpt copiedRoot;
	private ISchedulingRule schedulingRule;
	private int type;
	private DialogSettings settings; 
	private Composite pageContainer;
	
	private static String settingsFilename;
	private static final String SETTINGS_SECTION_SIZE = "size"; //$NON-NLS-1$
	private static final String SETTINGS_SIZE_X = "x"; //$NON-NLS-1$
	private static final String SETTINGS_SIZE_Y = "y"; //$NON-NLS-1$

	/**
	 * Creates a new wizard which can make a deep copy of the given product.
	 * 
	 * @param type One of TYPE_COPY_PRODUCT or TYPE_NEW_VERSION. The first one 
	 * allows to enter the version id (if supported by product component naming strategy)
	 * free and enter a search- and a rename-pattern. The second one does neither support
	 * to set the version id manually nor does it allow the user to enter a search- and a 
	 * rename-pattern.
	 * 
	 * @throws IllegalArgumentException if the given type is not valid.
	 */
	public DeepCopyWizard(IProductCmpt product, int type) throws IllegalArgumentException {
		super();

		if (type != TYPE_COPY_PRODUCT && type != TYPE_NEW_VERSION) {
			throw new IllegalArgumentException("The given type is neither TYPE_COPY_PRODUCT nor TYPE_NEW_VERSION."); //$NON-NLS-1$
		}
		this.type = type;
		
		try {
			// the working date lies before the valid from date of the first available generation 
			// of the given product component - so we have to take this valid-from date rather
			// then the working date to build the product component structure.
			if (IpsPlugin.getDefault().getIpsPreferences().getWorkingDate().before(product.getFirstGeneration().getValidFrom())) {
				String title = Messages.DeepCopyWizard_titleWorkingDateNotUsed;
				String msg = NLS
						.bind(
								Messages.DeepCopyWizard_msgWorkingDateNotUsed,
								IpsPlugin.getDefault().getIpsPreferences()
										.getChangesOverTimeNamingConvention()
										.getGenerationConceptNameSingular());
				
				MessageDialog.openInformation(getShell(), title, msg);
				
				structure = (ProductCmptStructure)product.getStructure(product.getFirstGeneration().getValidFrom());
			}
			else {
				structure = (ProductCmptStructure)product.getStructure();
			}
			
		} catch (CycleException e) {
			IpsPlugin.log(e);
		}
		
		if (type == TYPE_COPY_PRODUCT) {
			super.setWindowTitle(Messages.DeepCopyWizard_title);
            super.setDefaultPageImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("wizards/DeepCopyWizard.gif"));
		} else {
			String title = NLS.bind(Messages.DeepCopyWizard_titleNewVersion, IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention().getVersionConceptNameSingular());
			super.setWindowTitle(title);
            super.setDefaultPageImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("wizards/NewVersionWizard.gif"));
		}
		
		IPath path = IpsPlugin.getDefault().getStateLocation();
		settingsFilename = path.append("deepCopyWizard.settings").toOSString(); //$NON-NLS-1$

		settings = new DialogSettings(SETTINGS_SECTION_SIZE);
		settings.put(SETTINGS_SIZE_X, 0);
		settings.put(SETTINGS_SIZE_Y, 0);
		try {
			settings.load(settingsFilename);
		} catch (IOException e) {
			// cant read the settings, use defaults.
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addPages() {
		sourcePage = new SourcePage(structure, type);
		super.addPage(sourcePage);
		previewPage = new ReferenceAndPreviewPage(structure, sourcePage, type);
		super.addPage(previewPage);
	}

	public void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);
		this.pageContainer = pageContainer;
		GridData layoutData = (GridData)pageContainer.getLayoutData();

		// restore size
		int width = Math.max(settings.getInt(SETTINGS_SIZE_X), layoutData.heightHint);
		int height = Math.max(settings.getInt(SETTINGS_SIZE_Y), layoutData.widthHint);
		layoutData.widthHint = Math.max(width, layoutData.minimumWidth);
		layoutData.heightHint = Math.max(height, layoutData.minimumHeight);
	}

	public boolean performCancel() {
		storeSize();
		return super.performCancel();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean performFinish() {
		try {
			final IProductCmptReference[] toCopy = previewPage.getProductsToCopy();
			final IProductCmptReference[] toRefer = previewPage.getProductsToRefer();
			final Map handles = previewPage.getHandles();
			schedulingRule = structure.getRoot().getProductCmpt().getIpsProject().getCorrespondingResource();
			WorkspaceModifyOperation operation = new WorkspaceModifyOperation(schedulingRule){

				protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
					DeepCopyOperation dco = new DeepCopyOperation(toCopy, toRefer, handles);
					dco.run(monitor);
					copiedRoot = dco.getCopiedRoot();
				}
				
			};
			getContainer().run(true, true, operation);
			
		} catch (Exception e) {
			IpsPlugin.logAndShowErrorDialog(new IpsStatus("An error occured during the copying process.",e)); //$NON-NLS-1$
		}
		
		storeSize();
		
		//this implementation of this method should always return true since this causes the wizard dialog to close.
		//in either case if an exception arises or not it doesn't make sense to keep the dialog up
		return true;
	}

	private void storeSize() {
		Point size = pageContainer.getSize();
		settings.put(SETTINGS_SIZE_X, size.x);
		settings.put(SETTINGS_SIZE_Y, size.y);
		try {
			settings.save(settingsFilename);
		} catch (IOException e) {
			// cant save - use defaults the next time
		}
	}
	
	/**
	 * Returns the root product component which was copied.
	 */
	public IProductCmpt getCopiedRoot() {
		return copiedRoot;
	}
	
}
