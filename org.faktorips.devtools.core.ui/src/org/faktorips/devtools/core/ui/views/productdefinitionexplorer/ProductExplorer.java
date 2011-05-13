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

package org.faktorips.devtools.core.ui.views.productdefinitionexplorer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.ui.MenuCleaner;
import org.faktorips.devtools.core.ui.refactor.IpsMoveHandler;
import org.faktorips.devtools.core.ui.refactor.IpsRefactoringHandler;
import org.faktorips.devtools.core.ui.refactor.IpsRenameHandler;
import org.faktorips.devtools.core.ui.views.modelexplorer.ModelContentProvider;
import org.faktorips.devtools.core.ui.views.modelexplorer.ModelExplorer;
import org.faktorips.devtools.core.ui.views.modelexplorer.ModelExplorerConfiguration;
import org.faktorips.devtools.core.ui.views.modelexplorer.ModelExplorerContextMenuBuilder;

/**
 * A <code>ModelExplorer</code> that displays product definition projects along with all contained
 * <code>ProductCmpt</code>s, <code>TableContents</code>, <code>TestCases</code> and
 * <code>TestCaseTypes</code>.
 * 
 * @author Stefan Widmaier
 */
public class ProductExplorer extends ModelExplorer {

    public static String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.productDefinitionExplorer"; //$NON-NLS-1$

    private static final String EXCLUDE_NON_IPSPRODDEF_PROJECTS_KEY = "exclude_non_ipsproddef_projects"; //$NON-NLS-1$

    /**
     * Used for saving the current layout style and filter in a eclipse memento.
     */
    private static final String MEMENTO = "productExplorer.memento"; //$NON-NLS-1$

    private boolean excludeNoIpsProductDefinitionProjects = false;

    private ProductExplorerFilter filter;

    public ProductExplorer() {
        super();
        supportCategories = true;
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        labelProvider.setProductDefinitionLabelProvider(true);
    }

    @Override
    protected ModelExplorerConfiguration createConfig() {
        IIpsModel ipsModel = IpsPlugin.getDefault().getIpsModel();
        IpsObjectType[] objectTypes = ipsModel.getIpsObjectTypes();
        List<IpsObjectType> allowedObjectTypes = new ArrayList<IpsObjectType>();
        for (IpsObjectType objectType : objectTypes) {
            if (objectType.isProductDefinitionType()) {
                allowedObjectTypes.add(objectType);
            }
        }
        return new ModelExplorerConfiguration(allowedObjectTypes.toArray(new IpsObjectType[allowedObjectTypes.size()]));
    }

    /**
     * User a separate content provider for ProductDefinitionExplorer. This content provider does
     * not display a default package, only its contents as children of the packageFragmentRoot.
     * {@inheritDoc}
     */
    @Override
    protected ModelContentProvider createContentProvider() {
        return new ProductContentProvider(config, layoutStyle);
    }

    @Override
    protected void createFilters(TreeViewer tree) {
        super.createFilters(tree);
        filter = new ProductExplorerFilter();
        tree.addFilter(filter);
        filter.setExcludeNoIpsProductDefinitionProjects(excludeNoIpsProductDefinitionProjects);
    }

    @Override
    protected boolean isModelExplorer() {
        return false;
    }

    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);
        if (memento != null) {
            IMemento filterMemento = memento.getChild(MEMENTO);
            if (filterMemento != null) {
                Integer exludeNonPredDefProjects = filterMemento.getInteger(EXCLUDE_NON_IPSPRODDEF_PROJECTS_KEY);
                if (exludeNonPredDefProjects != null) {
                    excludeNoIpsProductDefinitionProjects = exludeNonPredDefProjects.intValue() == 1;
                }
            }
        }
    }

    @Override
    public void saveState(IMemento memento) {
        super.saveState(memento);
        IMemento layout = memento.createChild(MEMENTO);
        layout.putInteger(EXCLUDE_NON_IPSPRODDEF_PROJECTS_KEY, excludeNoIpsProductDefinitionProjects ? 1 : 0);
    }

    @Override
    protected void addProjectFilterAction(IMenuManager menuManager) {
        Action showNoIpsProdDefProjectsAction = createShowNoIpsProductDefinitionAction();
        showNoIpsProdDefProjectsAction.setChecked(excludeNoIpsProductDefinitionProjects);
        menuManager.appendToGroup(MENU_FILTER_GROUP, showNoIpsProdDefProjectsAction);
    }

    private Action createShowNoIpsProductDefinitionAction() {
        return new Action(Messages.ProductExplorer_MenuShowProdDefProjectsOnly_Title, IAction.AS_CHECK_BOX) {
            @Override
            public ImageDescriptor getImageDescriptor() {
                return null;
            }

            @Override
            public void run() {
                excludeNoIpsProductDefinitionProjects = !excludeNoIpsProductDefinitionProjects;
                filter.setExcludeNoIpsProductDefinitionProjects(excludeNoIpsProductDefinitionProjects);
                treeViewer.refresh();
            }

            @Override
            public String getToolTipText() {
                return Messages.ProductExplorer_MenuShowProdDefProjectsOnly_Tooltip;
            }
        };

    }

    @Override
    protected void createContextMenu() {
        final ProductMenuBuilder menuBuilder = new ProductMenuBuilder(this, config, getViewSite(), getSite(),
                treeViewer);

        MenuManager manager = new MenuManager();
        manager.setRemoveAllWhenShown(true);
        manager.addMenuListener(menuBuilder);

        Menu contextMenu = manager.createContextMenu(treeViewer.getControl());
        treeViewer.getControl().setMenu(contextMenu);

        getSite().registerContextMenu(manager, treeViewer);
        menuBuilder.registerAdditionsCleaner(manager);
        /*
         * We need to register the team cleaner via another menu listener because the team menu
         * manager is re-added on each menuAboutToShow.
         */
        manager.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                menuBuilder.registerTeamCleaner(manager);
            }
        });
    }

    protected class ProductMenuBuilder extends ModelExplorerContextMenuBuilder {

        // @formatter:off
        private static final String TEAM_MENU                           = "team.main"; //$NON-NLS-1$
        private static final String COMPARE_WITH_MENU                   = "compareWithMenu"; //$NON-NLS-1$
        private static final String REPLACE_WITH_MENU                   = "replaceWithMenu"; //$NON-NLS-1$
        
        private static final String TEAM_GROUP_1                        = "group1"; //$NON-NLS-1$
        private static final String TEAM_GROUP_2                        = "group2"; //$NON-NLS-1$
        private static final String TEAM_GROUP_3                        = "group3"; //$NON-NLS-1$
        private static final String TEAM_GROUP_4                        = "group4"; //$NON-NLS-1$
        private static final String TEAM_GROUP_6                        = "group6"; //$NON-NLS-1$
        
        private static final String CVS_SYNC                            = "org.eclipse.team.cvs.ui.sync"; //$NON-NLS-1$
        private static final String CVS_COMMIT                          = "org.eclipse.team.ccvs.ui.commit"; //$NON-NLS-1$
        private static final String CVS_UPDATE                          = "org.eclipse.team.ccvs.ui.update"; //$NON-NLS-1$
        private static final String CVS_TAG                             = "org.eclipse.team.cvs.ui.tag"; //$NON-NLS-1$
        private static final String CVS_BRANCH                          = "org.eclipse.team.cvs.ui.branch"; //$NON-NLS-1$
        private static final String CVS_SWITCH_BRANCH                   = "org.eclipse.team.cvs.ui.updateSwitch"; //$NON-NLS-1$
        private static final String CVS_SHOW_RESOURCE_HISTORY           = "org.eclipse.team.cvs.ui.showHistory"; //$NON-NLS-1$
        private static final String CVS_RESTORE_FROM_REPOSITORY         = "org.eclipse.team.ccvs.ui.restoreFromRepository"; //$NON-NLS-1$
        
        private static final String SUBVERSIVE_SYNC                     = "org.eclipse.team.svn.ui.action.local.SynchronizeAction"; //$NON-NLS-1$
        private static final String SUBVERSIVE_COMMIT                   = "org.eclipse.team.svn.ui.action.local.CommitAction"; //$NON-NLS-1$
        private static final String SUBVERSIVE_UPDATE                   = "org.eclipse.team.svn.ui.action.local.UpdateAction"; //$NON-NLS-1$
        private static final String SUBVERSIVE_UPDATE_TO_REVISION       = "org.eclipse.team.svn.ui.action.local.UpdateToRevisionAction"; //$NON-NLS-1$
        private static final String SUBVERSIVE_REVERT                   = "org.eclipse.team.svn.ui.action.local.RevertAction"; //$NON-NLS-1$
        private static final String SUBVERSIVE_TAG                      = "org.eclipse.team.svn.ui.action.local.TagAction"; //$NON-NLS-1$
        private static final String SUBVERSIVE_BRANCH                   = "org.eclipse.team.svn.ui.action.local.BranchAction"; //$NON-NLS-1$
        private static final String SUBVERSIVE_SWITCH_BRANCH            = "org.eclipse.team.svn.ui.action.local.SwitchAction"; //$NON-NLS-1$
        private static final String SUBVERSIVE_SHOW_RESOURCE_HISTORY    = "org.eclipse.team.svn.ui.action.local.ShowHistoryAction"; //$NON-NLS-1$
        
        private static final String SUBCLIPSE_SYNC                      = "org.tigris.subversion.subclipse.synchronize"; //$NON-NLS-1$
        private static final String SUBCLIPSE_COMMIT                    = "org.tigris.subversion.subclipse.ui.commit"; //$NON-NLS-1$
        private static final String SUBCLIPSE_UPDATE                    = "org.tigris.subversion.subclipse.ui.update"; //$NON-NLS-1$
        private static final String SUBCLIPSE_UPDATE_TO_REVISION        = "org.tigris.subversion.subclipse.ui.updateDialog"; //$NON-NLS-1$
        private static final String SUBCLIPSE_REVERT                    = "org.tigris.subversion.subclipse.ui.revert"; //$NON-NLS-1$
        private static final String SUBCLIPSE_BRANCH_TAG                = "org.tigris.subversion.subclipse.ui.branchtag"; //$NON-NLS-1$
        private static final String SUBCLIPSE_SWITCH_BRANCH             = "org.tigris.subversion.subclipse.ui.switch"; //$NON-NLS-1$
        private static final String SUBCLIPSE_SHOW_RESOURCE_HISTORY     = "org.tigris.subversion.subclipse.ui.ShowResourceInHistoryAction"; //$NON-NLS-1$
        // @formatter:on

        private final MenuCleaner additionsCleaner;

        private final MenuCleaner teamCleaner;

        public ProductMenuBuilder(ModelExplorer modelExplorer, ModelExplorerConfiguration modelExplorerConfig,
                IViewSite viewSite, IWorkbenchPartSite workbenchPartSite, TreeViewer treeViewer) {

            super(modelExplorer, modelExplorerConfig, viewSite, workbenchPartSite, treeViewer);
            additionsCleaner = new MenuCleaner();
            teamCleaner = new MenuCleaner();
        }

        private void registerAdditionsCleaner(IMenuManager manager) {
            manager.addMenuListener(additionsCleaner);
        }

        private void registerTeamCleaner(IMenuManager manager) {
            IMenuManager teamManager = getSubMenuManager(manager, TEAM_MENU);
            // If the team manager cannot be found there's no point in filtering in the first place
            if (teamManager != null) {
                teamManager.addMenuListener(teamCleaner);
                /*
                 * Team cleaner isn't added until the menu is shown so we need to call the cleaner
                 * on ourselves
                 */
                teamCleaner.menuAboutToShow(teamManager);
            }
        }

        private IMenuManager getSubMenuManager(IMenuManager menuManager, String subMenuManagerId) {
            for (IContributionItem item : menuManager.getItems()) {
                if (subMenuManagerId.equals(item.getId()) && item instanceof IMenuManager) {
                    return (IMenuManager)item;
                }
            }
            return null;
        }

        @Override
        protected void createReorgActions(IMenuManager manager, Object selected) {
            super.createReorgActions(manager, selected);
            // TODO AW: IIpsProject and IIpsPackageFragmentRoot should be supported as well
            if (selected instanceof IIpsObject || selected instanceof IIpsPackageFragment) {
                manager.add(IpsRefactoringHandler.getContributionItem(IpsRenameHandler.CONTRIBUTION_ID));
                manager.add(IpsRefactoringHandler.getContributionItem(IpsMoveHandler.CONTRIBUTION_ID));
            }
        }

        @Override
        protected void createAdditionalActions(IMenuManager manager, IStructuredSelection structuredSelection) {
            manager.add(new Separator("additions")); //$NON-NLS-1$
            configureAdditionsCleaner();
            configureTeamCleaner(structuredSelection);
        }

        @Override
        protected void createRefactorMenu(IMenuManager manager, Object selected) {
            /*
             * Overwritten to do nothing as refactoring actions are provided directly in
             * #createReorgActions
             */
        }

        private void configureAdditionsCleaner() {
            additionsCleaner.setWhiteListMode(true);
            additionsCleaner.setMatchingGroup(IWorkbenchActionConstants.MB_ADDITIONS);

            additionsCleaner.addFilteredPrefix(TEAM_MENU);
            additionsCleaner.addFilteredPrefix(COMPARE_WITH_MENU);
            additionsCleaner.addFilteredPrefix(REPLACE_WITH_MENU);
        }

        private void configureTeamCleaner(IStructuredSelection structuredSelection) {
            teamCleaner.setWhiteListMode(true);
            teamCleaner.clearFilteredPrefixes();

            boolean advancedTeamFunctionsEnabled = IpsPlugin.getDefault().getIpsPreferences()
                    .areAvancedTeamFunctionsForProductDefExplorerEnabled();
            if (advancedTeamFunctionsEnabled || config.representsProject(structuredSelection.getFirstElement())) {
                configureAdvancedCvsTeamActions();
                configureAdvancedSubversiveTeamActions();
                configureAdvancedSubclipseTeamActions();
            }
            configureDefaultCvsTeamActions();
            configureDefaultSubversiveTeamActions();
            configureDefaultSubclipseTeamActions();
        }

        private void configureAdvancedCvsTeamActions() {
            teamCleaner.addFilteredPrefix(TEAM_GROUP_1);
            teamCleaner.addFilteredPrefix(CVS_SYNC);
            teamCleaner.addFilteredPrefix(CVS_COMMIT);
            teamCleaner.addFilteredPrefix(CVS_UPDATE);

            teamCleaner.addFilteredPrefix(TEAM_GROUP_2);
            teamCleaner.addFilteredPrefix(CVS_TAG);
            teamCleaner.addFilteredPrefix(CVS_BRANCH);
            teamCleaner.addFilteredPrefix(CVS_SWITCH_BRANCH);
        }

        private void configureAdvancedSubversiveTeamActions() {
            teamCleaner.addFilteredPrefix(TEAM_GROUP_1);
            teamCleaner.addFilteredPrefix(SUBVERSIVE_SYNC);
            teamCleaner.addFilteredPrefix(SUBVERSIVE_COMMIT);
            teamCleaner.addFilteredPrefix(SUBVERSIVE_UPDATE);
            teamCleaner.addFilteredPrefix(SUBVERSIVE_UPDATE_TO_REVISION);

            teamCleaner.addFilteredPrefix(TEAM_GROUP_3);
            teamCleaner.addFilteredPrefix(SUBVERSIVE_TAG);
            teamCleaner.addFilteredPrefix(SUBVERSIVE_BRANCH);
            teamCleaner.addFilteredPrefix(SUBVERSIVE_SWITCH_BRANCH);
        }

        private void configureAdvancedSubclipseTeamActions() {
            teamCleaner.addFilteredPrefix(TEAM_GROUP_1);
            teamCleaner.addFilteredPrefix(SUBCLIPSE_SYNC);
            teamCleaner.addFilteredPrefix(SUBCLIPSE_COMMIT);
            teamCleaner.addFilteredPrefix(SUBCLIPSE_UPDATE);
            teamCleaner.addFilteredPrefix(SUBCLIPSE_UPDATE_TO_REVISION);

            teamCleaner.addFilteredPrefix(TEAM_GROUP_2);
            teamCleaner.addFilteredPrefix(SUBCLIPSE_BRANCH_TAG);
            teamCleaner.addFilteredPrefix(SUBCLIPSE_SWITCH_BRANCH);
        }

        private void configureDefaultCvsTeamActions() {
            teamCleaner.addFilteredPrefix(CVS_SHOW_RESOURCE_HISTORY);

            teamCleaner.addFilteredPrefix(TEAM_GROUP_3);
            teamCleaner.addFilteredPrefix(CVS_RESTORE_FROM_REPOSITORY);
        }

        private void configureDefaultSubversiveTeamActions() {
            teamCleaner.addFilteredPrefix(TEAM_GROUP_2);
            teamCleaner.addFilteredPrefix(SUBVERSIVE_REVERT);

            teamCleaner.addFilteredPrefix(TEAM_GROUP_4);
            teamCleaner.addFilteredPrefix(SUBVERSIVE_SHOW_RESOURCE_HISTORY);
        }

        private void configureDefaultSubclipseTeamActions() {
            teamCleaner.addFilteredPrefix(TEAM_GROUP_4);
            teamCleaner.addFilteredPrefix(SUBCLIPSE_SHOW_RESOURCE_HISTORY);

            teamCleaner.addFilteredPrefix(TEAM_GROUP_6);
            teamCleaner.addFilteredPrefix(SUBCLIPSE_REVERT);
        }

    }

}
