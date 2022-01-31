/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.internal.generationdate.GenerationDate;
import org.faktorips.devtools.core.ui.wizards.ResizableWizard;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.productcmpt.DeepCopyOperation;
import org.faktorips.devtools.model.internal.productcmpt.IDeepCopyOperationFixup;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;

/**
 * A wizard to create a deep copy from a given product component.
 * 
 * @author Thorsten Guenther
 */
public class DeepCopyWizard extends ResizableWizard {

    public static final int TYPE_COPY_PRODUCT = 10;
    public static final int TYPE_NEW_VERSION = 100;
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 800;

    private static final String SECTION_NAME = "DeepCopyWizard"; //$NON-NLS-1$

    private final DeepCopyPreview deepCopyPreview;

    private final DeepCopyPresentationModel presentationModel;

    private IIpsSrcFile copyResultRoot;
    private int type;
    private List<IAdditionalDeepCopyWizardPage> additionalPages;

    /**
     * Creates a new wizard which can make a deep copy of the given product.
     * 
     * @param productGeneration The source product generation which should be copied or used as
     *            template for the new version.
     * 
     * @param type One of TYPE_COPY_PRODUCT or TYPE_NEW_VERSION. The first one allows to enter the
     *            version id (if supported by product component naming strategy) free and enter a
     *            search- and a rename-pattern. The second one does neither support to set the
     *            version id manually nor does it allow the user to enter a search- and a
     *            rename-pattern.
     * 
     * @throws IllegalArgumentException if the given type is not valid.
     * @throws CycleInProductStructureException when the structure have a cycle
     */
    public DeepCopyWizard(IProductCmptGeneration productGeneration, int type) throws CycleInProductStructureException {
        super(SECTION_NAME, IpsPlugin.getDefault().getDialogSettings(), DEFAULT_WIDTH, DEFAULT_HEIGHT);

        setNeedsProgressMonitor(true);

        if (type != TYPE_COPY_PRODUCT && type != TYPE_NEW_VERSION) {
            throw new IllegalArgumentException("The given type is neither TYPE_COPY_PRODUCT nor TYPE_NEW_VERSION."); //$NON-NLS-1$
        }
        this.type = type;

        presentationModel = new DeepCopyPresentationModel(productGeneration);

        deepCopyPreview = new DeepCopyPreview(presentationModel);

        if (type == TYPE_COPY_PRODUCT) {
            super.setWindowTitle(Messages.DeepCopyWizard_title);
            super.setDefaultPageImageDescriptor(
                    IpsUIPlugin.getImageHandling().createImageDescriptor("wizards/DeepCopyWizard.png")); //$NON-NLS-1$
        } else {
            String title = NLS.bind(Messages.DeepCopyWizard_titleNewVersion, IpsPlugin.getDefault().getIpsPreferences()
                    .getChangesOverTimeNamingConvention().getVersionConceptNameSingular());
            super.setWindowTitle(title);
            super.setDefaultPageImageDescriptor(
                    IpsUIPlugin.getImageHandling().createImageDescriptor("wizards/NewVersionWizard.png")); //$NON-NLS-1$
        }
        settingDefaults();

    }

    @Override
    public void addPages() {
        SourcePage sourcePage = new SourcePage(type);
        super.addPage(sourcePage);
        ReferenceAndPreviewPage previewPage = new ReferenceAndPreviewPage(type);
        super.addPage(previewPage);
        for (IAdditionalDeepCopyWizardPage additionalPage : getAdditionalPages()) {
            super.addPage(additionalPage);
        }
    }

    private List<IAdditionalDeepCopyWizardPage> getAdditionalPages() {
        if (additionalPages == null) {
            ExtensionPoints extensionPoints = new ExtensionPoints(IpsUIPlugin.PLUGIN_ID);
            additionalPages = new AdditionalDeepCopyWizardPageExtensions(extensionPoints).get();
        }
        return additionalPages;
    }

    private void configureFixups(DeepCopyOperation dco) {
        final List<IDeepCopyOperationFixup> additionalFixups = dco.getAdditionalFixups();
        for (final IAdditionalDeepCopyWizardPage additionalPage : getAdditionalPages()) {
            ISafeRunnable runnable = new ISafeRunnable() {
                @Override
                public void handleException(Throwable exception) {
                    IpsPlugin.log(exception);
                }

                @Override
                public void run() throws Exception {
                    additionalPage.configureFixups(additionalFixups);
                }
            };
            SafeRunner.run(runnable);
        }
    }

    private void settingDefaults() {
        // set target default
        IIpsPackageFragment defaultPackage = getDefaultPackage();
        IIpsPackageFragmentRoot defaultPackageRoot = getDefaultPackage().getRoot();
        IIpsPackageFragmentRoot packRoot = defaultPackageRoot;
        if (!packRoot.isBasedOnSourceFolder()) {
            IIpsPackageFragmentRoot[] srcRoots;
            srcRoots = getPresentationModel().getIpsProject().getSourceIpsPackageFragmentRoots();
            if (srcRoots.length > 0) {
                packRoot = srcRoots[0];
            } else {
                packRoot = null;
            }
        }
        getPresentationModel().setNewValidFrom(IpsUIPlugin.getDefault().getDefaultValidityDate());

        getPresentationModel().setTargetPackageRoot(packRoot);
        getPresentationModel().setTargetPackage(defaultPackage);
    }

    IIpsPackageFragment getDefaultPackage() {
        int ignore = deepCopyPreview.getSegmentsToIgnore(getStructure().toSet(true));
        IIpsPackageFragment pack = getStructure().getRoot().getProductCmpt().getIpsPackageFragment();
        int segments = pack.getRelativePath().segmentCount();
        if (segments - ignore > 0) {
            IPath path = pack.getRelativePath().removeLastSegments(segments - ignore);
            pack = pack.getRoot().getIpsPackageFragment(path.toString().replace('/', '.'));
        }
        return pack;
    }

    @Override
    public boolean performCancel() {
        // maybe the working date has changed, thus restore the old working date
        return super.performCancel();
    }

    @Override
    public boolean performFinish() {
        try {
            final Set<IProductCmptStructureReference> toCopy = presentationModel.getAllCopyElements(true);
            final Set<IProductCmptStructureReference> toLink = presentationModel.getLinkedElements();

            if (presentationModel.isCopyExistingGenerations()) {
                GenerationDate selectedDate = presentationModel.getOldValidFrom();
                for (GenerationDate date : presentationModel.getGenerationDates()) {
                    if (!date.equals(selectedDate)) {
                        presentationModel.setOldValidFrom(date);
                        toCopy.addAll(presentationModel.getAllCopyElements(true));
                    }
                }
            }

            final boolean createEmptyTableContents = presentationModel.isCreateEmptyTable();

            final IWorkspaceRoot schedulingRule = getStructure().getRoot().getProductCmpt().getIpsProject()
                    .getCorrespondingResource().getWorkspace().getRoot().unwrap();
            WorkspaceModifyOperation operation = new WorkspaceModifyOperation(schedulingRule) {

                @SuppressWarnings("deprecation")
                @Override
                protected void execute(IProgressMonitor monitor) throws CoreRuntimeException, InterruptedException {
                    monitor.beginTask("", 2); //$NON-NLS-1$
                    final Map<IProductCmptStructureReference, IIpsSrcFile> handles = deepCopyPreview
                            .getHandles(new org.eclipse.core.runtime.SubProgressMonitor(monitor, 1), toCopy);
                    DeepCopyOperation dco = new DeepCopyOperation(getStructure().getRoot(), toCopy, toLink, handles,
                            getStructure().getValidAt(), presentationModel.getNewValidFrom());
                    dco.setIpsPackageFragmentRoot(presentationModel.getTargetPackageRoot());
                    dco.setSourceIpsPackageFragment(getDefaultPackage());
                    dco.setTargetIpsPackageFragment(presentationModel.getTargetPackage());
                    dco.setCreateEmptyTableContents(createEmptyTableContents);
                    dco.setCopyExistingGenerations(presentationModel.isCopyExistingGenerations());
                    configureFixups(dco);
                    dco.run(new org.eclipse.core.runtime.SubProgressMonitor(monitor, 1));
                    copyResultRoot = dco.getCopiedRoot();
                }
            };
            getContainer().run(true, true, operation);
        } catch (InvocationTargetException e) {
            IpsPlugin.logAndShowErrorDialog(new IpsStatus("An error occurred during the copying process.", e)); //$NON-NLS-1$
        } catch (InterruptedException e) {
            IpsPlugin.logAndShowErrorDialog(new IpsStatus("An error occurred during the copying process.", e)); //$NON-NLS-1$
        }

        // Setting the new working date of the created product component
        IpsUIPlugin.getDefault().setDefaultValidityDate(getPresentationModel().getNewValidFrom());

        // this implementation of this method should always return true since this causes the wizard
        // dialog to close.
        // in either case if an exception arises or not it doesn't make sense to keep the dialog up
        return true;
    }

    /**
     * Returns the root product component which was copied. Is null until {@link #performFinish()}
     * was performed successfully
     */
    public IProductCmpt getCopyResultRoot() {
        if (copyResultRoot != null && copyResultRoot.exists()) {
            return (IProductCmpt)copyResultRoot.getIpsObject();
        }
        return null;
    }

    public IProductCmptTreeStructure getStructure() {
        return getPresentationModel().getStructure();
    }

    /**
     * Returns the type of the deep copy TYPE_COPY_PRODUCT or TYPE_NEW_VERSION
     */
    public int getType() {
        return type;
    }

    public DeepCopyPreview getDeepCopyPreview() {
        return deepCopyPreview;
    }

    /**
     * Returns the size of the wizard which was stored the last time the wizard was used or the
     * default size if the wizard was never used before.
     */
    protected Point getSize() {
        final Point size = new Point(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        final IDialogSettings settings = getDialogSettings();
        if (settings == null) {
            return size;
        }

        final IDialogSettings section = settings.getSection(SECTION_NAME);
        if (section == null) {
            return size;
        }

        try {
            size.x = section.getInt(BOUNDS_WIDTH_KEY);
            size.y = section.getInt(BOUNDS_HEIGHT_KEY);
        } catch (NumberFormatException e) {
            size.x = DEFAULT_WIDTH;
            size.y = DEFAULT_HEIGHT;
        }
        return size;
    }

    public DeepCopyPresentationModel getPresentationModel() {
        return presentationModel;
    }
}
