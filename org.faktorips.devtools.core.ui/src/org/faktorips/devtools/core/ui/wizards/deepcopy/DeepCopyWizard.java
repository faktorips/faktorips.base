/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import java.lang.reflect.InvocationTargetException;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.productcmpt.DeepCopyOperation;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
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
    private final static int DEFAULT_WIDTH = 800;
    private final static int DEFAULT_HEIGHT = 800;

    private static final String SECTION_NAME = "DeepCopyWizard"; //$NON-NLS-1$

    private final DeepCopyPreview deepCopyPreview;

    private final DeepCopyPresentationModel presentationModel;

    private IIpsSrcFile copyResultRoot;
    private int type;

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
     * @throws CycleInProductStructureException when the structure have a cyle
     */
    public DeepCopyWizard(IProductCmptGeneration productGeneration, int type) throws IllegalArgumentException,
            CycleInProductStructureException {
        super(SECTION_NAME, IpsPlugin.getDefault().getDialogSettings(), DEFAULT_WIDTH, DEFAULT_HEIGHT);

        setNeedsProgressMonitor(true);

        if (type != TYPE_COPY_PRODUCT && type != TYPE_NEW_VERSION) {
            throw new IllegalArgumentException("The given type is neither TYPE_COPY_PRODUCT nor TYPE_NEW_VERSION."); //$NON-NLS-1$
        }
        this.type = type;

        GregorianCalendar structureDate = productGeneration.getValidFrom();
        presentationModel = new DeepCopyPresentationModel(productGeneration.getProductCmpt().getStructure(
                structureDate, productGeneration.getIpsProject()));
        deepCopyPreview = new DeepCopyPreview(presentationModel);

        if (type == TYPE_COPY_PRODUCT) {
            super.setWindowTitle(Messages.DeepCopyWizard_title);
            super.setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                    "wizards/DeepCopyWizard.png")); //$NON-NLS-1$
        } else {
            String title = NLS.bind(Messages.DeepCopyWizard_titleNewVersion, IpsPlugin.getDefault().getIpsPreferences()
                    .getChangesOverTimeNamingConvention().getVersionConceptNameSingular());
            super.setWindowTitle(title);
            super.setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                    "wizards/NewVersionWizard.png")); //$NON-NLS-1$
        }
        settingDefaults();

    }

    @Override
    public void addPages() {
        SourcePage sourcePage = new SourcePage(type);
        super.addPage(sourcePage);
        ReferenceAndPreviewPage previewPage = new ReferenceAndPreviewPage(type);
        super.addPage(previewPage);

    }

    private void settingDefaults() {
        // set target default
        IIpsPackageFragment defaultPackage = getDefaultPackage();
        IIpsPackageFragmentRoot defaultPackageRoot = getDefaultPackage().getRoot();
        IIpsPackageFragmentRoot packRoot = defaultPackageRoot;
        if (!packRoot.isBasedOnSourceFolder()) {
            IIpsPackageFragmentRoot srcRoots[];
            try {
                srcRoots = getPresentationModel().getIpsProject().getSourceIpsPackageFragmentRoots();
                if (srcRoots.length > 0) {
                    packRoot = srcRoots[0];
                } else {
                    packRoot = null;
                }
            } catch (CoreException e1) {
                packRoot = null;
            }
        }
        getPresentationModel().setNewValidFrom(IpsPlugin.getDefault().getIpsPreferences().getWorkingDate());

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

            final boolean createEmptyTableContents = presentationModel.isCreateEmptyTable();

            final IWorkspaceRoot schedulingRule = getStructure().getRoot().getProductCmpt().getIpsProject()
                    .getCorrespondingResource().getWorkspace().getRoot();
            WorkspaceModifyOperation operation = new WorkspaceModifyOperation(schedulingRule) {

                @Override
                protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
                        InterruptedException {
                    monitor.beginTask("", 2); //$NON-NLS-1$
                    final Map<IProductCmptStructureReference, IIpsSrcFile> handles = deepCopyPreview
                            .getHandles(new SubProgressMonitor(monitor, 1));
                    DeepCopyOperation dco = new DeepCopyOperation(getStructure().getRoot(), toCopy, toLink, handles,
                            getStructure().getValidAt(), presentationModel.getNewValidFrom());
                    dco.setIpsPackageFragmentRoot(presentationModel.getTargetPackageRoot());
                    dco.setCreateEmptyTableContents(createEmptyTableContents);
                    dco.run(new SubProgressMonitor(monitor, 1));
                    copyResultRoot = dco.getCopiedRoot();
                }
            };
            getContainer().run(true, true, operation);
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(new IpsStatus("An error occurred during the copying process.", e)); //$NON-NLS-1$
        }

        // Setting the new working date of the created product component
        IpsPlugin.getDefault().getIpsPreferences().setWorkingDate(getPresentationModel().getNewValidFrom());

        // this implementation of this method should always return true since this causes the wizard
        // dialog to close.
        // in either case if an exception arises or not it doesn't make sense to keep the dialog up
        return super.performFinish();
    }

    /**
     * Returns the root product component which was copied. Is null until {@link #performFinish()}
     * was performed successfully
     */
    public IProductCmpt getCopyResultRoot() {
        if (copyResultRoot.exists()) {
            try {
                return (IProductCmpt)copyResultRoot.getIpsObject();
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
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
            // ignore number format exceptions
        }
        return size;
    }

    public DeepCopyPresentationModel getPresentationModel() {
        return presentationModel;
    }
}
