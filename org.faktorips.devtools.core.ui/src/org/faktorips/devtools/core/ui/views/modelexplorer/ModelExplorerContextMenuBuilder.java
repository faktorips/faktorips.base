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

package org.faktorips.devtools.core.ui.views.modelexplorer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.actions.CloseResourceAction;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IFixDifferencesToModelSupport;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.ui.actions.CreateIpsArchiveAction;
import org.faktorips.devtools.core.ui.actions.CreateMissingEnumContentsAction;
import org.faktorips.devtools.core.ui.actions.EnumImportExportAction;
import org.faktorips.devtools.core.ui.actions.FindPolicyReferencesAction;
import org.faktorips.devtools.core.ui.actions.FindProductReferencesAction;
import org.faktorips.devtools.core.ui.actions.FixDifferencesAction;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.actions.IpsCopyAction;
import org.faktorips.devtools.core.ui.actions.IpsDeepCopyAction;
import org.faktorips.devtools.core.ui.actions.IpsEditSortOrderAction;
import org.faktorips.devtools.core.ui.actions.IpsPasteAction;
import org.faktorips.devtools.core.ui.actions.IpsPropertiesAction;
import org.faktorips.devtools.core.ui.actions.IpsTestAction;
import org.faktorips.devtools.core.ui.actions.IpsTestCaseCopyAction;
import org.faktorips.devtools.core.ui.actions.MigrateProjectAction;
import org.faktorips.devtools.core.ui.actions.ModelExplorerDeleteAction;
import org.faktorips.devtools.core.ui.actions.MoveAction;
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
import org.faktorips.devtools.core.ui.actions.RenameAction;
import org.faktorips.devtools.core.ui.actions.ShowInstanceAction;
import org.faktorips.devtools.core.ui.actions.ShowStructureAction;
import org.faktorips.devtools.core.ui.actions.TableImportExportAction;
import org.faktorips.devtools.core.ui.views.instanceexplorer.InstanceExplorer;
import org.faktorips.devtools.core.ui.wizards.deepcopy.DeepCopyWizard;

/**
 * The <tt>ModelExplorerContextMenuBuilder</tt> is used to create the context menu of the
 * <tt>ModelExplorer</tt>.
 */
public class ModelExplorerContextMenuBuilder implements IMenuListener {

    private ActionGroup openActionGroup;

    private ModelExplorerDeleteAction deleteAction;

    private IpsPropertiesAction propertiesAction;

    private IWorkbenchAction copy;

    private IWorkbenchAction paste;

    private IWorkbenchAction delete;

    private IWorkbenchAction refresh;

    private IWorkbenchAction rename;

    private IWorkbenchAction move;

    private IWorkbenchAction properties;

    protected ModelExplorerConfiguration modelExplorerConfig;

    private ModelExplorer modelExplorer;

    protected IViewSite viewSite;

    private TreeViewer treeViewer;

    /**
     * Creates a <tt>ModelExplorerContextMenuBuilder</tt>.
     * 
     * @param modelExplorer
     * @param modelExplorerConfig
     * @param viewSite
     * @param workbenchPartSite
     * @param treeViewer
     */
    public ModelExplorerContextMenuBuilder(ModelExplorer modelExplorer, ModelExplorerConfiguration modelExplorerConfig,
            IViewSite viewSite, IWorkbenchPartSite workbenchPartSite, TreeViewer treeViewer) {

        this.modelExplorer = modelExplorer;
        this.modelExplorerConfig = modelExplorerConfig;
        this.viewSite = viewSite;
        this.treeViewer = treeViewer;

        openActionGroup = new OpenActionGroup(modelExplorer);
        deleteAction = new ModelExplorerDeleteAction(treeViewer, viewSite.getShell());
        propertiesAction = new IpsPropertiesAction(viewSite, treeViewer);
        copy = ActionFactory.COPY.create(viewSite.getWorkbenchWindow());
        paste = ActionFactory.PASTE.create(viewSite.getWorkbenchWindow());
        delete = ActionFactory.DELETE.create(viewSite.getWorkbenchWindow());
        refresh = ActionFactory.REFRESH.create(viewSite.getWorkbenchWindow());
        rename = ActionFactory.RENAME.create(viewSite.getWorkbenchWindow());
        move = ActionFactory.MOVE.create(viewSite.getWorkbenchWindow());
        properties = ActionFactory.PROPERTIES.create(viewSite.getWorkbenchWindow());

        IActionBars actionBars = viewSite.getActionBars();
        actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), new IpsCopyAction(treeViewer, workbenchPartSite
                .getShell()));
        actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), new IpsPasteAction(treeViewer, workbenchPartSite
                .getShell()));
        actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), deleteAction);
        actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refresh);
        actionBars.setGlobalActionHandler(ActionFactory.RENAME.getId(), new RenameAction(workbenchPartSite.getShell(),
                treeViewer));
        actionBars.setGlobalActionHandler(ActionFactory.MOVE.getId(), new MoveAction(workbenchPartSite.getShell(),
                treeViewer));
        actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(), propertiesAction);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Creates this parts' context menu in the given MenuManager dynamically. The context menu and
     * its elements depend on the current selection and the <tt>ModelExplorerConfiguration</tt>.
     */
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

        manager.add(new Separator());
        createOpenMenu(manager, selected, (IStructuredSelection)treeViewer.getSelection());
        manager.add(new Separator());
        createReorgActions(manager, selected);
        manager.add(new Separator());
        createObjectInfoActions(manager, selected);
        manager.add(new Separator());
        createRefreshAction(manager, selected);
        createProjectActions(manager, selected, (IStructuredSelection)treeViewer.getSelection());
        manager.add(new Separator());

        createImportExportTableContentsActions(manager, selected);
        createImportExportEnumActions(manager, selected);
        createTestCaseAction(manager, selected);
        createIpsEditSortOrderAction(manager, selected);
        createFixDifferencesAction(manager, selected, (IStructuredSelection)treeViewer.getSelection());
        createIpsArchiveAction(manager, selected);
        createMissingEnumContentsAction(manager, selected);

        // Menus with sub menus.
        createRefactorMenu(manager, selected);
        manager.add(new Separator());

        manager.add(new GroupMarker("faktorIpsGroup"));
        manager.add(new Separator());
        createAdditionalActions(manager, structuredSelection);

        manager.add(new Separator());
        createPropertiesActions(manager, selected);
    }

    protected void createNewMenu(IMenuManager manager, Object selected) {
        selected = mapIpsSrcFile2IpsObject(selected);
        MenuManager newMenu = new MenuManager(Messages.ModelExplorer_submenuNew);

        if ((selected instanceof IFolder) || (selected instanceof IIpsProject)) {
            newMenu.add(new NewFolderAction(viewSite.getShell(), treeViewer));
            newMenu.add(new NewFileResourceAction(viewSite.getShell(), treeViewer));
        }

        if ((selected instanceof IIpsElement) && !(selected instanceof IIpsProject)) {
            IWorkbenchWindow workbenchWindow = viewSite.getWorkbenchWindow();

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

            newMenu.add(new Separator());

            // Product side elements
            if (modelExplorerConfig.isAllowedIpsElementType(IpsObjectType.PRODUCT_CMPT)) {
                newMenu.add(new NewProductComponentAction(workbenchWindow));
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

            newMenu.add(new Separator());

            // Ips package and default file actions
            newMenu.add(new NewIpsPacketAction(viewSite.getShell(), treeViewer));
            newMenu.add(new NewFileResourceAction(viewSite.getShell(), treeViewer));

            // Add copy actions depending on selected ips object type
            List<IpsAction> ipsCopyActions = new ArrayList<IpsAction>(3);
            if (selected instanceof IProductCmpt || selected instanceof IProductCmptGeneration) {
                ipsCopyActions.add(new IpsDeepCopyAction(viewSite.getShell(), treeViewer,
                        DeepCopyWizard.TYPE_NEW_VERSION));
                ipsCopyActions.add(new IpsDeepCopyAction(viewSite.getShell(), treeViewer,
                        DeepCopyWizard.TYPE_COPY_PRODUCT));
            } else if (selected instanceof ITestCase) {
                ipsCopyActions.add(new IpsTestCaseCopyAction(viewSite.getShell(), treeViewer));
            }

            if (ipsCopyActions.size() > 0) {
                newMenu.add(new Separator());
                for (Iterator<IpsAction> iter = ipsCopyActions.iterator(); iter.hasNext();) {
                    newMenu.add(iter.next());
                }

            }
        }

        manager.add(newMenu);
    }

    public Object mapIpsSrcFile2IpsObject(Object selected) {
        if (selected instanceof IIpsSrcFile) {
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)selected;
            selected = ipsSrcFile.getIpsObjectType().newObject(ipsSrcFile);
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
        delete.setEnabled(true);

        if (selected instanceof IIpsObjectPart) {
            copy.setEnabled(false);
            paste.setEnabled(false);
            delete.setEnabled(false);
            return;
        }

        if (isRootArchive(selected)) {
            paste.setEnabled(false);
            delete.setEnabled(false);
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
            try {
                return root.getIpsArchive() != null;
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        return false;
    }

    protected void createObjectInfoActions(IMenuManager manager, Object selected) {
        if (selected instanceof IIpsElement) {
            if (selected instanceof IProductCmpt) {
                manager.add(new ShowStructureAction(treeViewer));
            }
            if (selected instanceof IProductCmpt || selected instanceof ITableContents) {
                manager.add(new FindProductReferencesAction(treeViewer));
            }
            if (selected instanceof IPolicyCmptType) {
                manager.add(new FindPolicyReferencesAction(treeViewer));
            }
            if (InstanceExplorer.supports(selected)) {
                manager.add(new ShowInstanceAction((IIpsElement)selected, treeViewer));
            }
            // TODO not to be used in this release
            // if (selected instanceof IPolicyCmptType | selected instanceof IProductCmpt) {
            // manager.add(new ShowAttributesAction(treeViewer));
            // }
        }
    }

    protected void createProjectActions(IMenuManager manager, Object selected, IStructuredSelection selection) {
        if (selected instanceof IIpsElement) {
            if (selected instanceof IIpsProject) {
                manager.add(openCloseAction((IProject)((IIpsProject)selected).getCorrespondingResource()));
                try {
                    if (!IpsPlugin.getDefault().getMigrationOperation(((IIpsProject)selected)).isEmpty()) {
                        manager.add(new MigrateProjectAction(viewSite.getWorkbenchWindow(), selection));
                    }
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
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
                || (modelExplorerConfig.isAllowedIpsElementType(IpsObjectType.ENUM_CONTENT) && selected instanceof IEnumContent)) {
            boolean show = true;
            if (selected instanceof IEnumType) {
                /*
                 * The object has not been initialized, we do this now by requesting it again from
                 * the IpsSrcFile.
                 */
                IEnumType enumType = (IEnumType)selected;
                try {
                    enumType = (IEnumType)enumType.getIpsSrcFile().getIpsObject();
                    show = !(enumType.isAbstract()) && enumType.isContainingValues();
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
            }
            if (show) {
                manager.add(EnumImportExportAction.createEnumImportAction(viewSite.getShell(), treeViewer));
                manager.add(EnumImportExportAction.createEnumExportAction(viewSite.getShell(), treeViewer));
                manager.add(new Separator());
            }
        }
    }

    protected void createMissingEnumContentsAction(IMenuManager manager, Object selected) {
        if (selected instanceof IIpsElement) {
            manager.add(new CreateMissingEnumContentsAction(treeViewer, viewSite.getWorkbenchWindow()));
        }
    }

    protected void createTestCaseAction(IMenuManager manager, Object selected) {
        if (modelExplorerConfig.isAllowedIpsElementType(IpsObjectType.TEST_CASE)
                || modelExplorerConfig.isAllowedIpsElementType(IpsObjectType.PRODUCT_CMPT)) {
            if (selected instanceof IIpsPackageFragment || selected instanceof IIpsPackageFragmentRoot
                    || selected instanceof IIpsProject || selected instanceof ITestCase
                    || selected instanceof IProductCmpt) {
                manager.add(new IpsTestAction(treeViewer));
            }
        }
    }

    protected void createFixDifferencesAction(IMenuManager manager, Object selected, IStructuredSelection selection) {
        // show fix differences menu only for the model explorer
        if (!(modelExplorer.isModelExplorer())) {
            return;
        }
        if (selected instanceof IIpsElement) {
            if (selected instanceof IIpsProject) {
                IIpsProject project = (IIpsProject)selected;
                if (project.isProductDefinitionProject()) {
                    manager.add(new FixDifferencesAction(viewSite.getWorkbenchWindow(), selection));
                }
            } else if (selected instanceof IIpsPackageFragmentRoot) {
                manager.add(new FixDifferencesAction(viewSite.getWorkbenchWindow(), selection));
            } else if (selected instanceof IIpsPackageFragment) {
                manager.add(new FixDifferencesAction(viewSite.getWorkbenchWindow(), selection));
            } else if (selected instanceof IFixDifferencesToModelSupport) {
                manager.add(new FixDifferencesAction(viewSite.getWorkbenchWindow(), selection));
            }
        }
    }

    protected void createIpsArchiveAction(IMenuManager manager, Object selected) {
        // show ips archive menu only for the model explorer
        // TODO: should be moved to the configuration
        if (!(modelExplorer.isModelExplorer())) {
            return;
        }
        if (selected instanceof IIpsProject || selected instanceof IIpsPackageFragmentRoot) {
            if (selected instanceof IIpsPackageFragmentRoot) {
                try {
                    // don't enable menu for ips archives
                    if (((IIpsPackageFragmentRoot)selected).getIpsArchive() != null) {
                        return;
                    }
                } catch (CoreException e) {
                    // ignore exception while creating the menu
                }
            }
            manager.add(new CreateIpsArchiveAction(treeViewer));
        }
    }

    protected void createRefactorMenu(IMenuManager manager, Object selected) {
        if (selected instanceof IIpsElement & !(selected instanceof IIpsProject) | selected instanceof IFile
                | selected instanceof IFolder) {
            if (!isRootArchive(selected)) {
                MenuManager subMm = new MenuManager(Messages.ModelExplorer_submenuRefactor);
                subMm.add(rename);
                move.setText(Messages.ModelExplorer_menuItemMove);
                subMm.add(move);
                manager.add(subMm);
            }
        }
    }

    protected void createIpsEditSortOrderAction(IMenuManager manager, Object selected) {
        if (selected instanceof IIpsElement) {
            manager.add(new IpsEditSortOrderAction(treeViewer));
        }
    }

    protected void createAdditionalActions(IMenuManager manager, IStructuredSelection structuredSelection) {
        manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end"));//$NON-NLS-1$
    }

    protected void createPropertiesActions(IMenuManager manager, Object selected) {
        // all types of objects are supported
        properties.setEnabled(propertiesAction.isEnabledFor(selected));
        manager.add(properties);
    }

}