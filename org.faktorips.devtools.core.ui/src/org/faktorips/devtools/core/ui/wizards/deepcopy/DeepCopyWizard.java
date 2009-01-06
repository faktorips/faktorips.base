/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.deepcopy;


import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.productcmpt.DeepCopyOperation;
import org.faktorips.devtools.core.internal.model.productcmpt.treestructure.ProductCmptTreeStructure;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.ResizableWizard;

/**
 * A wizard to create a deep copy from a given product component.
 *
 * @author Thorsten Guenther
 */
public class DeepCopyWizard extends ResizableWizard {

	public static final int TYPE_COPY_PRODUCT = 10;
	public static final int TYPE_NEW_VERSION = 100;

	private ProductCmptTreeStructure structure;
	private SourcePage sourcePage;
	private ReferenceAndPreviewPage previewPage;
	private IProductCmpt copiedRoot;
	private ISchedulingRule schedulingRule;
	private int type;
    private GregorianCalendar structureDate;
    private String prevEnteredWorkingDate;
    
    // contains the previous working date, 
    // in case of cancel the working date will be set to the old value
    private GregorianCalendar prevWorkingDate;
    private IIpsProject ipsProject;

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
		super("DeepCopyWizard", IpsPlugin.getDefault().getDialogSettings(), 600, 800); //$NON-NLS-1$
		setNeedsProgressMonitor(true);

		if (type != TYPE_COPY_PRODUCT && type != TYPE_NEW_VERSION) {
			throw new IllegalArgumentException("The given type is neither TYPE_COPY_PRODUCT nor TYPE_NEW_VERSION."); //$NON-NLS-1$
		}
		this.type = type;
		this.ipsProject = product.getIpsProject();
		
        prevWorkingDate = IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
		structureDate = IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
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

				structureDate = product.getFirstGeneration().getValidFrom();
				structure = (ProductCmptTreeStructure)product.getStructure(structureDate, product.getIpsProject());
			} else {
				structure = (ProductCmptTreeStructure)product.getStructure(structureDate, product.getIpsProject());
			}
		} catch (CycleInProductStructureException e) {
			IpsPlugin.log(e);
		}

		if (type == TYPE_COPY_PRODUCT) {
			super.setWindowTitle(Messages.DeepCopyWizard_title);
            super.setDefaultPageImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor("wizards/DeepCopyWizard.png")); //$NON-NLS-1$
		} else {
			String title = NLS.bind(Messages.DeepCopyWizard_titleNewVersion, IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention().getVersionConceptNameSingular());
			super.setWindowTitle(title);
            super.setDefaultPageImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor("wizards/NewVersionWizard.png")); //$NON-NLS-1$
		}
		
		prevEnteredWorkingDate = IpsPlugin.getDefault().getIpsPreferences().getDateFormat().format(structureDate.getTime());
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

	/**
     * {@inheritDoc}
     */
    public boolean performCancel() {
        // maybe the working date has changed, thus restore the old working date
        IpsPreferences ipsPreferences = IpsPlugin.getDefault().getIpsPreferences();
        ipsPreferences.setWorkingDate(prevWorkingDate);
        return super.performCancel();
    }

    /**
	 * {@inheritDoc}
	 */
	public boolean performFinish() {
		try {
			final IProductCmptStructureReference[] toCopy = previewPage.getProductCmptStructRefToCopy();
			final IProductCmptStructureReference[] toRefer = previewPage.getProductsOrtTableContentsToRefer();
			final Map handles = previewPage.getHandles();
			final boolean createEmptyTableContents = sourcePage.isCreateEmptyTableContents();

			schedulingRule = structure.getRoot().getProductCmpt().getIpsProject().getCorrespondingResource().getWorkspace().getRoot();
			WorkspaceModifyOperation operation = new WorkspaceModifyOperation(schedulingRule){
			    protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
					DeepCopyOperation dco = new DeepCopyOperation(toCopy, toRefer, handles);
                    dco.setCreateEmptyTableContents(createEmptyTableContents);
					dco.run(monitor);
					copiedRoot = dco.getCopiedRoot();
				}
			};
			getContainer().run(true, true, operation);

		} catch (Exception e) {
			IpsPlugin.logAndShowErrorDialog(new IpsStatus("An error occurred during the copying process.",e)); //$NON-NLS-1$
		}

		//this implementation of this method should always return true since this causes the wizard dialog to close.
		//in either case if an exception arises or not it doesn't make sense to keep the dialog up
		return super.performFinish();
	}

	/**
	 * Returns the root product component which was copied.
	 */
	public IProductCmpt getCopiedRoot() {
		return copiedRoot;
	}

	/**
	 * Updates the wizards working date
	 */
    void applyWorkingDate() {
        IpsPreferences ipsPreferences = IpsPlugin.getDefault().getIpsPreferences();
        DateFormat format = ipsPreferences.getDateFormat();
        Date newDate;
            // apply working date in ips preferences
            final String currentWorkingDate = sourcePage.getWorkingDate();
            if (currentWorkingDate.equals(prevEnteredWorkingDate)){
                // nothing changed
                return;
            }

            
            try {
                newDate = format.parse(currentWorkingDate);
            } catch (ParseException e) {
                IpsPlugin.log(e);
                return;
            }
            final GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(newDate);
            ipsPreferences.setWorkingDate(calendar);

            getShell().getDisplay().asyncExec(new Runnable(){
                public void run() {
                    try {
                    // apply working date in wizard pages
                    structure = (ProductCmptTreeStructure)structure.getRoot().getProductCmpt().getStructure(calendar,
                            structure.getRoot().getProductCmpt().getIpsProject());
                    sourcePage.refreshStructureAndVersionId(structure);
                    previewPage.resetCheckState();
                    } catch (CycleInProductStructureException e) {
                        IpsPlugin.log(e);
                    }
                }
            });
        
        getContainer().updateButtons();
    }

    /**
     * Returns the current structure date (the date the structure is valid) 
     * as formatted string
     */
    String getFormattedStructureDate() {
        return sourcePage.getWorkingDate();
    }

    /**
     * Returns the current structure date (the date the structure is valid) 
     */
    Calendar getStructureDate() {
        return structureDate;
    }
    
    /**
     * @return Returns the structure.
     */
    public ProductCmptTreeStructure getStructure() {
        return structure;
    }

    /**
     * Returns the ips project which created this wizard
     */
    public IIpsProject getIpsProject() {
        return ipsProject;
    }
}
