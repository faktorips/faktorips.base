/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelexplorer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.ui.actions.OpenProjectAction;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.actions.CloseResourceAction;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsFeatureMigrationOperation;
import org.faktorips.devtools.core.ui.IpsMenuId;
import org.faktorips.devtools.core.ui.actions.CleanUpTranslationsAction;
import org.faktorips.devtools.core.ui.actions.CopyRuntimeIdAction;
import org.faktorips.devtools.core.ui.actions.CopyTableAction;
import org.faktorips.devtools.core.ui.actions.CreateIpsArchiveAction;
import org.faktorips.devtools.core.ui.actions.CreateMissingEnumContentsAction;
import org.faktorips.devtools.core.ui.actions.CreateNewGenerationAction;
import org.faktorips.devtools.core.ui.actions.EnumImportExportAction;
import org.faktorips.devtools.core.ui.actions.FixDifferencesAction;
import org.faktorips.devtools.core.ui.actions.IpsCopyAction;
import org.faktorips.devtools.core.ui.actions.IpsDeepCopyAction;
import org.faktorips.devtools.core.ui.actions.IpsEditSortOrderAction;
import org.faktorips.devtools.core.ui.actions.IpsPasteAction;
import org.faktorips.devtools.core.ui.actions.IpsPropertiesAction;
import org.faktorips.devtools.core.ui.actions.IpsTestAction;
import org.faktorips.devtools.core.ui.actions.IpsTestCaseCopyAction;
import org.faktorips.devtools.core.ui.actions.MigrateProjectAction;
import org.faktorips.devtools.core.ui.actions.NewBusinessFunctionAction;
import org.faktorips.devtools.core.ui.actions.NewEnumContentAction;
import org.faktorips.devtools.core.ui.actions.NewEnumTypeAction;
import org.faktorips.devtools.core.ui.actions.NewFileResourceAction;
import org.faktorips.devtools.core.ui.actions.NewFolderAction;
import org.faktorips.devtools.core.ui.actions.NewIpsPacketAction;
import org.faktorips.devtools.core.ui.actions.NewPolicyComponentTypeAction;
import org.faktorips.devtools.core.ui.actions.NewProductCmptTypeAction;
import org.faktorips.devtools.core.ui.actions.NewProductComponentAction;
import org.faktorips.devtools.core.ui.actions.NewTableContentAction;
import org.faktorips.devtools.core.ui.actions.NewTableStructureAction;
import org.faktorips.devtools.core.ui.actions.NewTestCaseAction;
import org.faktorips.devtools.core.ui.actions.NewTestCaseTypeAction;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;
import org.faktorips.devtools.core.ui.actions.TableImportExportAction;
import org.faktorips.devtools.core.ui.commands.InferTemplateHandler;
import org.faktorips.devtools.core.ui.wizards.deepcopy.DeepCopyWizard;

/**
 * The <code>ModelExplorerContextMenuBuilder</code> is used to create the context menu of the
 * <code>ModelExplorer</code>.
 */
public class ModelExplorerContextMenuBuilder implements IMenuListener {

    public static final String NEW_MENU_ID = "group.new"; //$NON-NLS-1$

    public static final String GROUP_MODELDEF = "group.modeldef"; //$NON-NLS-1$

    public static final String GROUP_PRODUCTDEF = "group.productdef"; //$NON-NLS-1$

    public static final String GROUP_COMMON = "group.common"; //$NON-NLS-1$

    private ActionGroup openActionGroup;

    private IpsPropertiesAction propertiesAction;

    private IWorkbenchAction copy;

    private IWorkbenchAction paste;

    private CommandContributionItem delete;

    private IWorkbenchAction refresh;

    private IWorkbenchAction properties;

    private ModelExplorerConfiguration modelExplorerConfig;

    private ModelExplorer modelExplorer;

    private IViewSite viewSite;

    private TreeViewer treeViewer;

    /**
     * Creates a <code>ModelExplorerContextMenuBuilder</code>.
     * 
     */
    public ModelExplorerContextMenuBuilder(ModelExplorer modelExplorer, ModelExplorerConfiguration modelExplorerConfig,
            IViewSite viewSite, IWorkbenchPartSite workbenchPartSite, TreeViewer treeViewer) {

        this.modelExplorer = modelExplorer;
        this.modelExplorerConfig = modelExplorerConfig;
        this.viewSite = viewSite;
        this.treeViewer = treeViewer;

        openActionGroup = new OpenActionGroup(modelExplorer);
        propertiesAction = new IpsPropertiesAction(viewSite, treeViewer);
        copy = ActionFactory.COPY.create(viewSite.getWorkbenchWindow());
        paste = ActionFactory.PASTE.create(viewSite.getWorkbenchWindow());
        delete = new CommandContributionItem(new CommandContributionItemParameter(PlatformUI.getWorkbench(), null,
                "org.eclipse.ui.edit.delete", CommandContributionItem.STYLE_PUSH)); //$NON-NLS-1$
        refresh = ActionFactory.REFRESH.create(viewSite.getWorkbenchWindow());
        properties = ActionFactory.PROPERTIES.create(viewSite.getWorkbenchWindow());
        IActionBars actionBars = viewSite.getActionBars();
        actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(),
                new IpsCopyAction(treeViewer, workbenchPartSite.getShell()));
        actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(),
                new IpsPasteAction(treeViewer, workbenchPartSite.getShell()));
        actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refresh);
        actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(), propertiesAction);
    }

    /**
     * Creates this parts' context menu in the given MenuManager dynamically. The context menu and
     * its elements depend on the current selection and the <code>ModelExplorerConfiguration</code>.
     */
    @Override
    public void menuAboutToShow(IMenuManager manager) {
        if (!(treeViewer.getSelection() instanceof IStructuredSelection)) {
            return;
        }

        Object selected = ((IStructuredSelection)treeViewer.getSelection()).getFirstElement();
        IStructuredSelection structuredSelection = (IStructuredSelection)treeViewer.getSelection();
        if (selected == null) {
            return;
        }

        selected = mapIpsSrcFile2IpsObject(selected);
        createNewMenu(manager, selected);

        manager.add(new Separator("copy")); //$NON-NLS-1$
        // Add copy actions depending on selected ips object type
        if (selected instanceof IProductCmpt) {
            IProductCmpt cmpt = (IProductCmpt)selected;
            if (cmpt.isProductTemplate()) {
                addProductTemplateActions(manager);
            } else {
                addProductComponentActions(manager);
            }
        } else if (selected instanceof IProductCmptGeneration) {
            IProductCmptGeneration gen = (IProductCmptGeneration)selected;
            if (gen.isProductTemplate()) {
                addProductTemplateActions(manager);
            } else {
                addProductComponentActions(manager);
            }
        } else if (selected instanceof ITestCase) {
            manager.add(new IpsTestCaseCopyAction(viewSite.getShell(), treeViewer));
        } else if (selected instanceof ITableContents) {
            manager.add(new CopyTableAction(viewSite.getShell(), treeViewer));
        }

        manager.add(new Separator("open")); //$NON-NLS-1$
        createOpenMenu(manager, selected, (IStructuredSelection)treeViewer.getSelection());
        manager.add(new Separator("reorg")); //$NON-NLS-1$
        createReorgActions(manager, selected);
        manager.add(new Separator(IpsMenuId.GROUP_JUMP_TO_SOURCE_CODE.getId()));
        manager.add(new GroupMarker(IpsMenuId.GROUP_NAVIGATE.getId()));
        manager.add(new Separator("misc")); //$NON-NLS-1$
        createRefreshAction(manager, selected);
        createProjectActions(manager, selected, (IStructuredSelection)treeViewer.getSelection());
        manager.add(new Separator("manage")); //$NON-NLS-1$

        createImportExportTableContentsActions(manager, selected);
        createImportExportEnumActions(manager, selected);
        createTestCaseAction(manager, selected);
        createIpsEditSortOrderAction(manager, selected);
        createIpsArchiveAction(manager, selected);

        createCleanUpMenu(manager, selected);

        // Menus with sub menus.
        createRefactorMenu(manager, selected);
        manager.add(new Separator("global")); //$NON-NLS-1$

        manager.add(new GroupMarker("faktorIpsGroup")); //$NON-NLS-1$
        createAdditionalActions(manager, structuredSelection);

        manager.add(new Separator("properties")); //$NON-NLS-1$
        createPropertiesActions(manager, selected);
    }

    private void addProductComponentActions(IMenuManager manager) {
        manager.add(new IpsDeepCopyAction(viewSite.getShell(), treeViewer, DeepCopyWizard.TYPE_NEW_VERSION));
        manager.add(new CreateNewGenerationAction(viewSite.getShell(), treeViewer));
        manager.add(new IpsDeepCopyAction(viewSite.getShell(), treeViewer, DeepCopyWizard.TYPE_COPY_PRODUCT));
        manager.add(InferTemplateHandler.createContributionItem(viewSite));
        manager.add(new CopyRuntimeIdAction(treeViewer, viewSite.getShell()));
    }

    private void addProductTemplateActions(IMenuManager manager) {
        manager.add(new CreateNewGenerationAction(viewSite.getShell(), treeViewer));
        manager.add(InferTemplateHandler.createContributionItem(viewSite));
    }

    protected void createNewMenu(IMenuManager manager, final Object selected) {
        Object selection = mapIpsSrcFile2IpsObject(selected);
        MenuManager newMenu = new MenuManager(Messages.ModelExplorer_submenuNew, NEW_MENU_ID);

        if ((selection instanceof IFolder) || (selection instanceof IIpsProject)) {
            newMenu.add(new NewFolderAction(viewSite.getShell(), treeViewer));
            newMenu.add(new NewFileResourceAction(viewSite.getShell(), treeViewer));
        }

        if ((selection instanceof IIpsElement) && !(selection instanceof IIpsProject)) {
            IWorkbenchWindow workbenchWindow = viewSite.getWorkbenchWindow();

            newMenu.add(new Separator(GROUP_MODELDEF));
            addModelMenueItems(newMenu, workbenchWindow);
            newMenu.add(new Separator(GROUP_PRODUCTDEF));
            addProductCmptMenuItems(newMenu, workbenchWindow);
            newMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
            newMenu.add(new Separator(GROUP_COMMON));

            // Ips package and default file actions
            newMenu.add(new NewIpsPacketAction(viewSite.getShell(), treeViewer));
            newMenu.add(new NewFileResourceAction(viewSite.getShell(), treeViewer));
        }

        manager.add(newMenu);
    }

    private void addModelMenueItems(MenuManager newMenu, IWorkbenchWindow workbenchWindow) {
        // Model side elements
        if (modelExplorerConfig.isAllowedIpsElementType(IpsObjectType.POLICY_CMPT_TYPE)) {
            newMenu.add(new NewPolicyComponentTypeAction(workbenchWindow));
        }
        if (modelExplorerConfig.isAllowedIpsElementType(IpsObjectType.PRODUCT_CMPT_TYPE)) {
            newMenu.add(new NewProductCmptTypeAction(workbenchWindow));
        }
        if (modelExplorerConfig.isAllowedIpsElementType(IpsObjectType.ENUM_TYPE)) {
            newMenu.add(new NewEnumTypeAction(workbenchWindow));
        }
        if (modelExplorerConfig.isAllowedIpsElementType(IpsObjectType.BUSINESS_FUNCTION)) {
            newMenu.add(new NewBusinessFunctionAction(workbenchWindow));
        }
        if (modelExplorerConfig.isAllowedIpsElementType(IpsObjectType.TABLE_STRUCTURE)) {
            newMenu.add(new NewTableStructureAction(workbenchWindow));
        }
        if (modelExplorerConfig.isAllowedIpsElementType(IpsObjectType.TEST_CASE_TYPE)) {
            newMenu.add(new NewTestCaseTypeAction(workbenchWindow));
        }
    }

    private void addProductCmptMenuItems(MenuManager newMenu, IWorkbenchWindow workbenchWindow) {
        // Product side elements
        if (modelExplorerConfig.isAllowedIpsElementType(IpsObjectType.PRODUCT_CMPT)) {
            newMenu.add(new NewProductComponentAction(workbenchWindow, false));
        }
        if (modelExplorerConfig.isAllowedIpsElementType(IpsObjectType.PRODUCT_TEMPLATE)) {
            newMenu.add(new NewProductComponentAction(workbenchWindow, true));
        }
        if (modelExplorerConfig.isAllowedIpsElementType(IpsObjectType.ENUM_CONTENT)) {
            newMenu.add(new NewEnumContentAction(workbenchWindow));
        }
        if (modelExplorerConfig.isAllowedIpsElementType(IpsObjectType.TABLE_CONTENTS)) {
            newMenu.add(new NewTableContentAction(workbenchWindow));
        }
        if (modelExplorerConfig.isAllowedIpsElementType(IpsObjectType.TEST_CASE)) {
            newMenu.add(new NewTestCaseAction(workbenchWindow));
        }
    }

    public Object mapIpsSrcFile2IpsObject(Object selected) {
        if (selected instanceof IIpsSrcFile) {
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)selected;
            return ipsSrcFile.getIpsObjectType().newObject(ipsSrcFile);
        }
        return selected;
    }

    protected void createOpenMenu(IMenuManager manager, Object selected, IStructuredSelection structuredSelected) {
        if (selected instanceof IIpsObject || selected instanceof IPolicyCmptTypeAssociation
                || selected instanceof IPolicyCmptTypeAttribute) {
            manager.add(new OpenEditorAction(treeViewer));
        } else {
            openActionGroup.setContext(new ActionContext(structuredSelected));
            openActionGroup.fillContextMenu(manager);
        }
    }

    protected void createReorgActions(IMenuManager manager, Object selected) {
        manager.add(copy);
        manager.add(paste);
        manager.add(delete);

        copy.setEnabled(true);
        paste.setEnabled(true);

        if (selected instanceof IIpsObjectPart) {
            copy.setEnabled(false);
            paste.setEnabled(false);
            return;
        }

        if (isRootArchive(selected)) {
            paste.setEnabled(false);
        }
    }

    protected void createRefreshAction(IMenuManager manager, Object selected) {
        boolean open = false;
        if (selected instanceof IIpsElement) {
            open = ((IIpsElement)selected).getIpsProject().getProject().isOpen();
        } else if (selected instanceof IResource) {
            open = ((IResource)selected).getProject().isOpen();
        }
        if (open) {
            manager.add(refresh);
            refresh.setEnabled(true);
        }
    }

    private IIpsPackageFragmentRoot getPackageFragmentRoot(Object object) {
        IIpsPackageFragmentRoot root = null;
        if (object instanceof IIpsObject) {
            root = ((IIpsObject)object).getIpsPackageFragment().getRoot();
        } else if (object instanceof IIpsPackageFragment) {
            root = ((IIpsPackageFragment)object).getRoot();
        } else if (object instanceof IIpsPackageFragmentRoot) {
            root = (IIpsPackageFragmentRoot)object;
        }
        return root;
    }

    private boolean isRootArchive(Object object) {
        IIpsPackageFragmentRoot root = getPackageFragmentRoot(object);
        if (root != null) {
            return root.getIpsStorage() != null;
        }
        return false;
    }

    protected void createProjectActions(IMenuManager manager, Object selected, IStructuredSelection selection) {
        if (selected instanceof IIpsProject) {
            IIpsProject ipsProject = (IIpsProject)selected;
            manager.add(openCloseAction((IProject)ipsProject.getCorrespondingResource()));

            try {
                AbstractIpsFeatureMigrationOperation migrationOperation = IpsPlugin.getDefault()
                        .getMigrationOperation(ipsProject);
                MigrateProjectAction migrateAction = new MigrateProjectAction(viewSite.getWorkbenchWindow(), selection);
                migrateAction.setEnabled(!(migrationOperation.isEmpty()));
                if (modelExplorer.isModelExplorer()) {
                    // in model explorer the action is always added
                    manager.add(migrateAction);
                } else {
                    // in product explorer only if it is enabled
                    if (!migrationOperation.isEmpty()) {
                        manager.add(migrateAction);
                    }
                }
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        } else {
            if (selected instanceof IProject) {
                manager.add(openCloseAction((IProject)selected));
            }
        }
    }

    protected IAction openCloseAction(IProject project) {
        if (project.isOpen()) {
            CloseResourceAction close = new CloseResourceAction(viewSite);
            close.selectionChanged((IStructuredSelection)treeViewer.getSelection());
            return close;
        } else {
            OpenProjectAction open = new OpenProjectAction(viewSite);
            open.selectionChanged((IStructuredSelection)treeViewer.getSelection());
            return open;
        }
    }

    protected void createImportExportTableContentsActions(IMenuManager manager, Object selected) {
        if (modelExplorerConfig.isAllowedIpsElementType(IpsObjectType.TABLE_CONTENTS)
                && selected instanceof ITableContents) {
            manager.add(TableImportExportAction.createTableImportAction(viewSite.getShell(), treeViewer));
            manager.add(TableImportExportAction.createTableExportAction(viewSite.getShell(), treeViewer));
            manager.add(new Separator());
        }
    }

    protected void createImportExportEnumActions(IMenuManager manager, Object selected) {
        if ((modelExplorerConfig.isAllowedIpsElementType(IpsObjectType.ENUM_TYPE) && selected instanceof IEnumType)
                || (modelExplorerConfig.isAllowedIpsElementType(IpsObjectType.ENUM_CONTENT)
                        && selected instanceof IEnumContent)) {
            boolean show = true;
            if (selected instanceof IEnumType) {
                /*
                 * The object has not been initialized, we do this now by requesting it again from
                 * the IpsSrcFile.
                 */
                IEnumType enumType = (IEnumType)selected;
                enumType = (IEnumType)enumType.getIpsSrcFile().getIpsObject();
                show = enumType.isCapableOfContainingValues();
            }
            if (show) {
                manager.add(EnumImportExportAction.createEnumImportAction(viewSite.getShell(), treeViewer));
                manager.add(EnumImportExportAction.createEnumExportAction(viewSite.getShell(), treeViewer));
                manager.add(new Separator());
            }
        }
    }

    protected void createCleanUpMenu(IMenuManager manager, Object selected) {
        if (selected instanceof IIpsElement) {
            MenuManager cleanUpMenu = new MenuManager(Messages.ModelExplorer_submenuCleanUp,
                    "org.faktorips.devtools.core.ui.views.modelexplorer.cleanup"); //$NON-NLS-1$

            cleanUpMenu.add(new FixDifferencesAction(viewSite.getWorkbenchWindow(),
                    (IStructuredSelection)treeViewer.getSelection()));

            cleanUpMenu.add(new CreateMissingEnumContentsAction(treeViewer, viewSite.getWorkbenchWindow()));

            if (modelExplorer.isModelExplorer()) {
                cleanUpMenu.add(new CleanUpTranslationsAction(treeViewer, viewSite.getWorkbenchWindow()));
            }

            manager.add(cleanUpMenu);
        }
    }

    protected void createTestCaseAction(IMenuManager manager, Object selected) {
        if (modelExplorerConfig.isAllowedIpsElementType(IpsObjectType.TEST_CASE)
                || modelExplorerConfig.isAllowedIpsElementType(IpsObjectType.PRODUCT_CMPT)) {
            if (isAllowIpsTest(selected)) {
                manager.add(new IpsTestAction(treeViewer));
            }
        }
    }

    private boolean isAllowIpsTest(Object selected) {
        return isFolderLike(selected) || selected instanceof ITestCase || selected instanceof IProductCmpt;
    }

    private boolean isFolderLike(Object selected) {
        return selected instanceof IIpsPackageFragment || selected instanceof IIpsPackageFragmentRoot
                || selected instanceof IIpsProject;
    }

    protected void createIpsArchiveAction(IMenuManager manager, Object selected) {
        // show ips archive menu only for the model explorer
        // TODO: should be moved to the configuration
        if (!(modelExplorer.isModelExplorer())) {
            return;
        }
        if (selected instanceof IIpsProject || selected instanceof IIpsPackageFragmentRoot) {
            if (selected instanceof IIpsPackageFragmentRoot) {
                // don't enable menu for ips archives
                if (((IIpsPackageFragmentRoot)selected).getIpsStorage() != null) {
                    return;
                }
            }
            manager.add(new CreateIpsArchiveAction(treeViewer));
        }
    }

    protected void createRefactorMenu(IMenuManager manager, Object selected) {
        if (selected instanceof IIpsElement & !(selected instanceof IIpsProject) | selected instanceof IFile
                | selected instanceof IFolder) {
            if (!isRootArchive(selected)) {
                MenuManager subMm = new MenuManager(Messages.ModelExplorer_submenuRefactor,
                        "org.faktorips.devtools.core.ui.views.modelexplorer.refactoring"); //$NON-NLS-1$
                // Commands added via extension point org.eclipse.ui.menus
                manager.add(subMm);
            }
        }
    }

    protected void createIpsEditSortOrderAction(IMenuManager manager, Object selected) {
        if (selected instanceof IIpsElement) {
            manager.add(new IpsEditSortOrderAction(treeViewer));
        }
    }

    /**
     * @param structuredSelection actual selection
     */
    protected void createAdditionalActions(IMenuManager manager, IStructuredSelection structuredSelection) {
        manager.add(new Separator("additions")); //$NON-NLS-1$
        manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end")); //$NON-NLS-1$
    }

    protected void createPropertiesActions(IMenuManager manager, Object selected) {
        // all types of objects are supported
        properties.setEnabled(propertiesAction.isEnabledFor(selected));
        manager.add(properties);
    }

}
