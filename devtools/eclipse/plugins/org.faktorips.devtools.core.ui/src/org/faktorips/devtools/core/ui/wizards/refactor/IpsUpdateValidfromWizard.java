/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.refactor;

import java.lang.reflect.InvocationTargetException;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.abstraction.AWorkspace;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.refactor.IpsRefactoringOperation;
import org.faktorips.devtools.core.ui.wizards.ResizableWizard;
import org.faktorips.devtools.core.ui.wizards.deepcopy.LinkStatus.CopyOrLink;
import org.faktorips.devtools.model.internal.tablecontents.TableContents;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;

/**
 * A wizard that guides the user through the process of updating the "Valid From" date and
 * optionally the generation ID for a product component and its structure.
 */
public class IpsUpdateValidfromWizard extends ResizableWizard {

    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 800;

    private static final String SECTION_NAME = "UpdateValidfromWizard"; //$NON-NLS-1$
    private static final String IMAGE_PATH = "icons/wizards/UpdateValidFromWizard.png"; //$NON-NLS-1$
    private static final String PLUGIN_ID = "org.faktorips.devtools.core.ui"; //$NON-NLS-1$

    private final UpdateValidfromPresentationModel presentationModel;

    /**
     * Creates a new wizard instance for updating the Valid-From date and optionally the generation
     * ID.
     *
     * @param product the product component to update
     */
    public IpsUpdateValidfromWizard(IProductCmpt product) {
        super(SECTION_NAME, IpsPlugin.getDefault().getDialogSettings(), DEFAULT_WIDTH, DEFAULT_HEIGHT);

        presentationModel = new UpdateValidfromPresentationModel(product);
        setDefaultPageImageDescriptor(IpsPlugin.imageDescriptorFromPlugin(PLUGIN_ID, IMAGE_PATH));
        initializeDefaults();
    }

    @Override
    public void addPages() {
        addPage(new UpdateValidFromSourcePage(UpdateValidFromSourcePage.PAGE_ID));
        addPage(new UpdateValidFromPreviewPage(UpdateValidFromPreviewPage.PAGE_ID));
    }

    private void initializeDefaults() {
        presentationModel.setNewValidFrom(IpsUIPlugin.getDefault().getDefaultValidityDate());
    }

    public UpdateValidfromPresentationModel getPresentationModel() {
        return presentationModel;
    }

    public IProductCmptTreeStructure getStructure() {
        return presentationModel.getStructure();
    }

    @Override
    public boolean performFinish() {
        var model = getPresentationModel();

        Set<IProductCmptStructureReference> selectedItems = model.getTreeStatus().getAllEnabledElements(
                CopyOrLink.COPY, getStructure(), false);

        boolean shouldChangeId = model.isChangeGenerationId();
        String newVersionId = model.getNewVersionId();
        GregorianCalendar newValidFrom = model.getNewValidFrom();

        runValidFromUpdateOperation(selectedItems, newValidFrom);
        if (shouldChangeId && StringUtils.isNotBlank(newVersionId)) {
            applyNewGenerationIdsAsync(selectedItems, newVersionId);
        }

        return true;
    }

    private void runValidFromUpdateOperation(Set<IProductCmptStructureReference> selectedItems,
            GregorianCalendar newValidFrom) {

        AWorkspace workspace = getIpsProject().getCorrespondingResource().getWorkspace();
        IWorkspaceRoot schedulingRule = workspace.getRoot().unwrap();

        WorkspaceModifyOperation operation = new WorkspaceModifyOperation(schedulingRule) {
            @Override
            protected void execute(IProgressMonitor monitor) throws IpsException {
                for (IProductCmptStructureReference ref : selectedItems) {
                    IIpsObject object = ref.getWrappedIpsObject();
                    if (object instanceof IProductCmpt productCmpt) {
                        if (!Objects.equals(productCmpt.getValidFrom(), newValidFrom)) {
                            productCmpt.setValidFrom(newValidFrom);
                            productCmpt.getIpsSrcFile().save(monitor);
                        }
                    }
                }
            }
        };

        try {
            getContainer().run(true, true, operation);
        } catch (InvocationTargetException | InterruptedException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    private void applyNewGenerationIdsAsync(Set<IProductCmptStructureReference> selectedItems,
            String newVersionId) {

        Display.getDefault().asyncExec(() -> {
            IProductCmptNamingStrategy namingStrategy = getNamingStrategy();
            if (namingStrategy == null || !namingStrategy.supportsVersionId()) {
                return;
            }

            for (IProductCmptStructureReference ref : selectedItems) {
                IIpsObject object = ref.getWrappedIpsObject();
                if (object instanceof IProductCmpt product) {
                    renameIfNecessary(product, newVersionId, namingStrategy);
                } else if (object instanceof TableContents table) {
                    renameIfNecessary(table, newVersionId, namingStrategy);
                }
            }
        });
    }

    private void renameIfNecessary(IIpsObject ipsObject,
            String newVersionId,
            IProductCmptNamingStrategy namingStrategy) {

        String currentName = ipsObject.getName();
        String kindId = namingStrategy.getKindId(currentName);
        String newName = namingStrategy.getProductCmptName(kindId, newVersionId);

        if (!newName.equals(currentName)) {
            performRenameRefactoring(ipsObject, newName);
        }
    }

    private void performRenameRefactoring(IIpsObjectPartContainer target, String newName) {
        IIpsRefactoring refactoring = IpsPlugin.getIpsRefactoringFactory()
                .createRenameRefactoring(target, newName, null, true);
        new IpsRefactoringOperation(refactoring, PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell())
                .runDirectExecution();
    }

    private IIpsProject getIpsProject() {
        return getPresentationModel().getIpsProject();
    }

    private IProductCmptNamingStrategy getNamingStrategy() {
        return getIpsProject().getProductCmptNamingStrategy();
    }
}