/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.application;

import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.NewWizardMenu;
import org.eclipse.ui.actions.OpenPerspectiveAction;
import org.eclipse.ui.actions.PerspectiveMenu;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.ide.IIDEActionConstants;
import org.eclipse.ui.internal.provisional.application.IActionBarConfigurer2;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsProductDefinitionPerspectiveFactory;

/**
 * Provides the actions available for FaktorIps as Eclipse-Product
 * 
 * @author Thorsten Guenther
 */
class IpsActionBarAdvisor extends ActionBarAdvisor {

    final IWorkbenchWindow window;

    // Generic actions

    private IWorkbenchAction closeAction;

    private IWorkbenchAction closeAllAction;

    private IWorkbenchAction saveAction;

    private IWorkbenchAction saveAllAction;

    private IWorkbenchAction helpContentsAction;

    private IWorkbenchAction helpSearchAction;

    private IWorkbenchAction dynamicHelpAction;

    private IWorkbenchAction aboutAction;

    private IWorkbenchAction openPreferencesAction;

    private IWorkbenchAction saveAsAction;

    private IWorkbenchAction hideShowEditorAction;

    private IWorkbenchAction savePerspectiveAction;

    private OpenPerspectiveAction openSynchronizePerspectiveAction;

    private OpenPerspectiveAction openCVSPerspectiveAction;

    private OpenPerspectiveAction openProductDefinitionPerspectiveAction;

    private IWorkbenchAction resetPerspectiveAction;

    private IWorkbenchAction editActionSetAction;

    private IWorkbenchAction lockToolBarAction;

    private IWorkbenchAction showViewMenuAction;

    private IWorkbenchAction showPartPaneMenuAction;

    private IWorkbenchAction nextPartAction;

    private IWorkbenchAction prevPartAction;

    private IWorkbenchAction nextEditorAction;

    private IWorkbenchAction prevEditorAction;

    private IWorkbenchAction nextPerspectiveAction;

    private IWorkbenchAction prevPerspectiveAction;

    private IWorkbenchAction activateEditorAction;

    private IWorkbenchAction maximizePartAction;

    private IWorkbenchAction minimizePartAction;

    private IWorkbenchAction workbenchEditorsAction;

    private IWorkbenchAction workbookEditorsAction;

    private IWorkbenchAction backwardHistoryAction;

    private IWorkbenchAction forwardHistoryAction;

    // Generic re-target actions

    private IWorkbenchAction undoAction;

    private IWorkbenchAction redoAction;

    private IWorkbenchAction cutAction;

    private IWorkbenchAction copyAction;

    private IWorkbenchAction pasteAction;

    private IWorkbenchAction deleteAction;

    private IWorkbenchAction selectAllAction;

    private IWorkbenchAction findAction;

    private IWorkbenchAction printAction;

    private IWorkbenchAction revertAction;

    private IWorkbenchAction refreshAction;

    private IWorkbenchAction propertiesAction;

    private IWorkbenchAction quitAction;

    private IWorkbenchAction moveAction;

    private IWorkbenchAction renameAction;

    private IWorkbenchAction goIntoAction;

    private IWorkbenchAction backAction;

    private IWorkbenchAction forwardAction;

    private IWorkbenchAction upAction;

    private IWorkbenchAction nextAction;

    private IWorkbenchAction previousAction;

    // IDE-specific actions

    private IWorkbenchAction newWizardAction;

    private IWorkbenchAction newWizardDropDownAction;

    private IWorkbenchAction importResourcesAction;

    private IWorkbenchAction exportResourcesAction;

    private IWorkbenchAction openProjectAction;

    private IWorkbenchAction closeProjectAction;

    private IWorkbenchAction cleanProjectsAction;

    // Contribution items

    private NewWizardMenu newWizardMenu;

    private IContributionItem pinEditorContributionItem;

    private Preferences.IPropertyChangeListener prefListener;

    private IPageListener pageListener;

    private IResourceChangeListener resourceListener;

    private boolean disablingUnwantedActionSets = false;

    /**
     * Indicates if the action builder has been disposed.
     */
    private boolean isDisposed = false;

    /**
     * Constructs a new action builder which contributes actions to the given window.
     * 
     * @param configurer the action bar configurer for the window
     */
    public IpsActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
        window = configurer.getWindowConfigurer().getWindow();

        // Hide actions that are not useful in this product.
        window.addPageListener(new IPageListener() {
            @Override
            public void pageOpened(IWorkbenchPage page) {
                disableUnwantedActionSets(page);
            }

            @Override
            public void pageClosed(IWorkbenchPage page) {
                // Nothing to do.
            }

            @Override
            public void pageActivated(IWorkbenchPage page) {
                // Nothing to do.
            }
        });

        window.addPerspectiveListener(new IPerspectiveListener() {

            @Override
            public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
                fixPerspective(page);
            }

            @Override
            public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
                // Nothing to do.
            }
        });
    }

    /**
     * Returns the window to which this action builder is contributing.
     */
    private IWorkbenchWindow getWindow() {
        return window;
    }

    @Override
    protected void fillMenuBar(IMenuManager menuBar) {
        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());
        menuBar.add(createNavigateMenu());
        menuBar.add(createProjectMenu());
        menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menuBar.add(createWindowMenu());
        menuBar.add(createHelpMenu());
        if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
            disableUnwantedActionSets(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage());
        }
    }

    @Override
    protected void fillCoolBar(ICoolBarManager coolBar) {
        IActionBarConfigurer2 actionBarConfigurer = (IActionBarConfigurer2)getActionBarConfigurer();
        coolBar.add(new GroupMarker(IIDEActionConstants.GROUP_FILE));
        { // File Group
            IToolBarManager fileToolBar = actionBarConfigurer.createToolBarManager();
            fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.SAVE_GROUP));
            fileToolBar.add(saveAction);
            fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
            fileToolBar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

            // Add to the cool bar manager
            coolBar.add(actionBarConfigurer.createToolBarContributionItem(fileToolBar,
                    IWorkbenchActionConstants.TOOLBAR_FILE));
        }

        coolBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    /**
     * Creates and returns the "File" menu.
     */
    private MenuManager createFileMenu() {
        MenuManager menu = new MenuManager(Messages.IpsActionBarAdvisor_file, IWorkbenchActionConstants.M_FILE);
        menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
        {
            // Create the "New" sub menu, using the same ID for it as for the "New" action.
            String newText = Messages.IpsActionBarAdvisor_new;
            String newId = ActionFactory.NEW.getId();
            MenuManager newMenu = new MenuManager(newText, newId);
            newMenu.add(new Separator(newId));
            newWizardMenu = new NewWizardMenu(getWindow());
            newMenu.add(newWizardMenu);
            newMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
            menu.add(newMenu);
        }

        menu.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
        menu.add(new Separator());

        menu.add(closeAction);
        menu.add(closeAllAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.CLOSE_EXT));
        menu.add(new Separator());
        menu.add(saveAction);
        menu.add(saveAsAction);
        menu.add(saveAllAction);
        menu.add(revertAction);
        menu.add(new Separator());
        menu.add(moveAction);
        menu.add(renameAction);
        menu.add(refreshAction);

        menu.add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
        menu.add(new Separator());
        menu.add(printAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.PRINT_EXT));
        menu.add(new Separator());
        menu.add(new GroupMarker(IWorkbenchActionConstants.OPEN_EXT));
        menu.add(new Separator());
        menu.add(importResourcesAction);
        menu.add(exportResourcesAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.IMPORT_EXT));
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        menu.add(new Separator());
        menu.add(propertiesAction);

        menu.add(ContributionItemFactory.REOPEN_EDITORS.create(getWindow()));
        menu.add(new GroupMarker(IWorkbenchActionConstants.MRU));
        menu.add(new Separator());
        menu.add(quitAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));
        return menu;
    }

    /**
     * Creates and returns the "Edit" menu.
     */
    private MenuManager createEditMenu() {
        MenuManager menu = new MenuManager(Messages.IpsActionBarAdvisor_edit, IWorkbenchActionConstants.M_EDIT);
        menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));

        menu.add(undoAction);
        menu.add(redoAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.UNDO_EXT));
        menu.add(new Separator());

        menu.add(cutAction);
        menu.add(copyAction);
        menu.add(pasteAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.CUT_EXT));
        menu.add(new Separator());

        menu.add(deleteAction);
        menu.add(selectAllAction);
        menu.add(new Separator());

        menu.add(findAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.FIND_EXT));
        menu.add(new Separator());

        menu.add(new GroupMarker(IWorkbenchActionConstants.ADD_EXT));

        menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_END));
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        return menu;
    }

    /**
     * Creates and returns the "Navigate" menu.
     */
    private MenuManager createNavigateMenu() {
        MenuManager menu = new MenuManager(Messages.IpsActionBarAdvisor_navigate, IWorkbenchActionConstants.M_NAVIGATE);
        menu.add(new GroupMarker(IWorkbenchActionConstants.NAV_START));
        menu.add(goIntoAction);

        MenuManager goToSubMenu = new MenuManager(Messages.IpsActionBarAdvisor_goto, IWorkbenchActionConstants.GO_TO);
        menu.add(goToSubMenu);
        goToSubMenu.add(backAction);
        goToSubMenu.add(forwardAction);
        goToSubMenu.add(upAction);
        goToSubMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        menu.add(new Separator(IWorkbenchActionConstants.OPEN_EXT));
        for (int i = 2; i < 5; ++i) {
            menu.add(new Separator(IWorkbenchActionConstants.OPEN_EXT + i));
        }
        menu.add(new Separator(IWorkbenchActionConstants.SHOW_EXT));
        {

            MenuManager showInSubMenu = new MenuManager(Messages.IpsActionBarAdvisor_showIn, "showIn"); //$NON-NLS-1$
            showInSubMenu.add(ContributionItemFactory.VIEWS_SHOW_IN.create(getWindow()));
            menu.add(showInSubMenu);
        }
        for (int i = 2; i < 5; ++i) {
            menu.add(new Separator(IWorkbenchActionConstants.SHOW_EXT + i));
        }
        menu.add(new Separator());
        menu.add(nextAction);
        menu.add(previousAction);
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menu.add(new GroupMarker(IWorkbenchActionConstants.NAV_END));

        menu.add(new Separator());
        menu.add(backwardHistoryAction);
        menu.add(forwardHistoryAction);
        return menu;
    }

    /**
     * Creates and returns the "Project" menu.
     */
    private MenuManager createProjectMenu() {
        MenuManager menu = new MenuManager(Messages.IpsActionBarAdvisor_project, IWorkbenchActionConstants.M_PROJECT);
        menu.add(new Separator(IWorkbenchActionConstants.PROJ_START));

        menu.add(openProjectAction);
        menu.add(closeProjectAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.OPEN_EXT));
        menu.add(new Separator());
        menu.add(new GroupMarker(IWorkbenchActionConstants.BUILD_EXT));
        menu.add(cleanProjectsAction);
        menu.add(new Separator());

        menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menu.add(new GroupMarker(IWorkbenchActionConstants.PROJ_END));
        menu.add(new Separator());
        return menu;
    }

    /**
     * Creates and returns the "Window" menu.
     */
    private MenuManager createWindowMenu() {
        MenuManager menu = new MenuManager(Messages.IpsActionBarAdvisor_Window, IWorkbenchActionConstants.M_WINDOW);

        menu.add(resetPerspectiveAction);
        // menu.add(editActionSetAction);
        menu.add(openProductDefinitionPerspectiveAction);
        menu.add(openSynchronizePerspectiveAction);
        menu.add(openCVSPerspectiveAction);
        menu.add(new Separator());
        addKeyboardShortcuts(menu);
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "end")); //$NON-NLS-1$
        menu.add(openPreferencesAction);

        menu.add(ContributionItemFactory.OPEN_WINDOWS.create(getWindow()));
        return menu;
    }

    /**
     * Adds the keyboard navigation sub menu to the specified menu.
     */
    private void addKeyboardShortcuts(MenuManager menu) {
        MenuManager subMenu = new MenuManager("Navi&gation", Messages.IpsActionBarAdvisor_shortcuts); //$NON-NLS-1$
        menu.add(subMenu);
        subMenu.add(showPartPaneMenuAction);
        subMenu.add(showViewMenuAction);
        subMenu.add(new Separator());
        subMenu.add(maximizePartAction);
        subMenu.add(minimizePartAction);
        subMenu.add(new Separator());
        subMenu.add(activateEditorAction);
        subMenu.add(nextEditorAction);
        subMenu.add(prevEditorAction);
        subMenu.add(workbookEditorsAction);
        subMenu.add(new Separator());
        subMenu.add(nextPartAction);
        subMenu.add(prevPartAction);
    }

    /**
     * Creates and returns the "Help" menu.
     */
    private MenuManager createHelpMenu() {
        MenuManager menu = new MenuManager(Messages.IpsActionBarAdvisor_help, IWorkbenchActionConstants.M_HELP);
        addSeparatorOrGroupMarker(menu, "group.intro"); //$NON-NLS-1$
        menu.add(new GroupMarker("group.intro.ext")); //$NON-NLS-1$
        addSeparatorOrGroupMarker(menu, "group.main"); //$NON-NLS-1$
        menu.add(helpContentsAction);
        menu.add(helpSearchAction);
        menu.add(dynamicHelpAction);
        addSeparatorOrGroupMarker(menu, "group.assist"); //$NON-NLS-1$
        // See if a tips and tricks page is specified
        menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_START));
        menu.add(new GroupMarker("group.main.ext")); //$NON-NLS-1$
        addSeparatorOrGroupMarker(menu, "group.tutorials"); //$NON-NLS-1$
        addSeparatorOrGroupMarker(menu, "group.tools"); //$NON-NLS-1$
        addSeparatorOrGroupMarker(menu, "group.updates"); //$NON-NLS-1$
        menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_END));
        addSeparatorOrGroupMarker(menu, IWorkbenchActionConstants.MB_ADDITIONS);
        // about should always be at the bottom
        menu.add(new Separator("group.about")); //$NON-NLS-1$
        menu.add(aboutAction);
        menu.add(new GroupMarker("group.about.ext")); //$NON-NLS-1$

        return menu;
    }

    /**
     * Adds a <code>GroupMarker</code> or <code>Separator</code> to a menu. The test for whether a
     * separator should be added is done by checking for the existence of a preference matching the
     * string useSeparator.MENUID.GROUPID that is set to <code>true</code>.
     * 
     * @param menu the menu to add to
     * @param groupId the group id for the added separator or group marker
     */
    private void addSeparatorOrGroupMarker(MenuManager menu, String groupId) {
        String prefId = "useSeparator." + menu.getId() + "." + groupId; //$NON-NLS-1$ //$NON-NLS-2$
        boolean addExtraSeparators = IpsPlugin.getDefault().getPreferenceStore().getBoolean(prefId);
        if (addExtraSeparators) {
            menu.add(new Separator(groupId));
        } else {
            menu.add(new GroupMarker(groupId));
        }
    }

    /**
     * Disposes any resources and unhooks any listeners that are no longer needed. Called when the
     * window is closed.
     */
    @Override
    public void dispose() {
        if (isDisposed) {
            return;
        }
        isDisposed = true;
        if (pageListener != null) {
            window.removePageListener(pageListener);
            pageListener = null;
        }
        if (prefListener != null) {
            ResourcesPlugin.getPlugin().getPluginPreferences().removePropertyChangeListener(prefListener);
            prefListener = null;
        }
        if (resourceListener != null) {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
            resourceListener = null;
        }

        pinEditorContributionItem.dispose();

        // Null out actions to make leak debugging easier.
        closeAction = null;
        closeAllAction = null;
        saveAction = null;
        saveAllAction = null;
        helpContentsAction = null;
        helpSearchAction = null;
        dynamicHelpAction = null;
        aboutAction = null;
        openPreferencesAction = null;
        saveAsAction = null;
        hideShowEditorAction = null;
        savePerspectiveAction = null;
        resetPerspectiveAction = null;
        editActionSetAction = null;
        lockToolBarAction = null;
        showViewMenuAction = null;
        showPartPaneMenuAction = null;
        nextPartAction = null;
        prevPartAction = null;
        nextEditorAction = null;
        prevEditorAction = null;
        nextPerspectiveAction = null;
        prevPerspectiveAction = null;
        activateEditorAction = null;
        maximizePartAction = null;
        minimizePartAction = null;
        workbenchEditorsAction = null;
        workbookEditorsAction = null;
        backwardHistoryAction = null;
        forwardHistoryAction = null;
        undoAction = null;
        redoAction = null;
        cutAction = null;
        copyAction = null;
        pasteAction = null;
        deleteAction = null;
        selectAllAction = null;
        findAction = null;
        printAction = null;
        revertAction = null;
        refreshAction = null;
        propertiesAction = null;
        quitAction = null;
        moveAction = null;
        renameAction = null;
        goIntoAction = null;
        backAction = null;
        forwardAction = null;
        upAction = null;
        nextAction = null;
        previousAction = null;
        newWizardAction = null;
        newWizardDropDownAction = null;
        importResourcesAction = null;
        exportResourcesAction = null;
        openProjectAction = null;
        closeProjectAction = null;
        cleanProjectsAction = null;
        newWizardMenu = null;
        pinEditorContributionItem = null;
        prefListener = null;
        super.dispose();
    }

    @Override
    protected void makeActions(final IWorkbenchWindow window) {
        newWizardAction = ActionFactory.NEW.create(window);
        register(newWizardAction);

        newWizardDropDownAction = IDEActionFactory.NEW_WIZARD_DROP_DOWN.create(window);
        register(newWizardDropDownAction);

        importResourcesAction = ActionFactory.IMPORT.create(window);
        register(importResourcesAction);

        exportResourcesAction = ActionFactory.EXPORT.create(window);
        register(exportResourcesAction);

        saveAction = ActionFactory.SAVE.create(window);
        register(saveAction);

        saveAsAction = ActionFactory.SAVE_AS.create(window);
        register(saveAsAction);

        saveAllAction = ActionFactory.SAVE_ALL.create(window);
        register(saveAllAction);

        undoAction = ActionFactory.UNDO.create(window);
        register(undoAction);

        redoAction = ActionFactory.REDO.create(window);
        register(redoAction);

        cutAction = ActionFactory.CUT.create(window);
        register(cutAction);

        copyAction = ActionFactory.COPY.create(window);
        register(copyAction);

        pasteAction = ActionFactory.PASTE.create(window);
        register(pasteAction);

        printAction = ActionFactory.PRINT.create(window);
        register(printAction);

        selectAllAction = ActionFactory.SELECT_ALL.create(window);
        register(selectAllAction);

        findAction = ActionFactory.FIND.create(window);
        register(findAction);

        closeAction = ActionFactory.CLOSE.create(window);
        register(closeAction);

        closeAllAction = ActionFactory.CLOSE_ALL.create(window);
        register(closeAllAction);

        helpContentsAction = ActionFactory.HELP_CONTENTS.create(window);
        register(helpContentsAction);

        helpSearchAction = ActionFactory.HELP_SEARCH.create(window);
        register(helpSearchAction);

        dynamicHelpAction = ActionFactory.DYNAMIC_HELP.create(window);
        register(dynamicHelpAction);

        aboutAction = ActionFactory.ABOUT.create(window);
        register(aboutAction);

        openPreferencesAction = ActionFactory.PREFERENCES.create(window);
        register(openPreferencesAction);

        deleteAction = ActionFactory.DELETE.create(window);
        register(deleteAction);

        // Actions for invisible accelerators
        showViewMenuAction = ActionFactory.SHOW_VIEW_MENU.create(window);
        register(showViewMenuAction);

        showPartPaneMenuAction = ActionFactory.SHOW_PART_PANE_MENU.create(window);
        register(showPartPaneMenuAction);

        nextEditorAction = ActionFactory.NEXT_EDITOR.create(window);
        register(nextEditorAction);
        prevEditorAction = ActionFactory.PREVIOUS_EDITOR.create(window);
        register(prevEditorAction);
        ActionFactory.linkCycleActionPair(nextEditorAction, prevEditorAction);

        nextPartAction = ActionFactory.NEXT_PART.create(window);
        register(nextPartAction);
        prevPartAction = ActionFactory.PREVIOUS_PART.create(window);
        register(prevPartAction);
        ActionFactory.linkCycleActionPair(nextPartAction, prevPartAction);

        nextPerspectiveAction = ActionFactory.NEXT_PERSPECTIVE.create(window);
        register(nextPerspectiveAction);
        prevPerspectiveAction = ActionFactory.PREVIOUS_PERSPECTIVE.create(window);
        register(prevPerspectiveAction);
        ActionFactory.linkCycleActionPair(nextPerspectiveAction, prevPerspectiveAction);

        activateEditorAction = ActionFactory.ACTIVATE_EDITOR.create(window);
        register(activateEditorAction);

        maximizePartAction = ActionFactory.MAXIMIZE.create(window);
        register(maximizePartAction);

        minimizePartAction = ActionFactory.MINIMIZE.create(window);
        register(minimizePartAction);

        workbenchEditorsAction = ActionFactory.SHOW_OPEN_EDITORS.create(window);
        register(workbenchEditorsAction);

        workbookEditorsAction = ActionFactory.SHOW_WORKBOOK_EDITORS.create(window);
        register(workbookEditorsAction);

        hideShowEditorAction = ActionFactory.SHOW_EDITOR.create(window);
        register(hideShowEditorAction);
        savePerspectiveAction = ActionFactory.SAVE_PERSPECTIVE.create(window);
        register(savePerspectiveAction);
        editActionSetAction = ActionFactory.EDIT_ACTION_SETS.create(window);
        register(editActionSetAction);
        lockToolBarAction = ActionFactory.LOCK_TOOL_BAR.create(window);
        register(lockToolBarAction);
        resetPerspectiveAction = ActionFactory.RESET_PERSPECTIVE.create(window);
        register(resetPerspectiveAction);

        forwardHistoryAction = ActionFactory.FORWARD_HISTORY.create(window);
        register(forwardHistoryAction);

        backwardHistoryAction = ActionFactory.BACKWARD_HISTORY.create(window);
        register(backwardHistoryAction);

        revertAction = ActionFactory.REVERT.create(window);
        register(revertAction);

        refreshAction = ActionFactory.REFRESH.create(window);
        register(refreshAction);

        propertiesAction = ActionFactory.PROPERTIES.create(window);
        register(propertiesAction);

        quitAction = ActionFactory.QUIT.create(window);
        register(quitAction);

        moveAction = ActionFactory.MOVE.create(window);
        register(moveAction);

        renameAction = ActionFactory.RENAME.create(window);
        register(renameAction);

        goIntoAction = ActionFactory.GO_INTO.create(window);
        register(goIntoAction);

        backAction = ActionFactory.BACK.create(window);
        register(backAction);

        forwardAction = ActionFactory.FORWARD.create(window);
        register(forwardAction);

        upAction = ActionFactory.UP.create(window);
        register(upAction);

        nextAction = ActionFactory.NEXT.create(window);
        register(nextAction);

        previousAction = ActionFactory.PREVIOUS.create(window);
        register(previousAction);

        openProjectAction = IDEActionFactory.OPEN_PROJECT.create(window);
        register(openProjectAction);

        closeProjectAction = IDEActionFactory.CLOSE_PROJECT.create(window);
        register(closeProjectAction);

        cleanProjectsAction = IDEActionFactory.BUILD_CLEAN.create(window);
        register(cleanProjectsAction);

        PerspectiveMenu m = new PerspecitveHandler(getWindow(), "unknown"); //$NON-NLS-1$
        openProductDefinitionPerspectiveAction = new OpenPerspectiveAction(getWindow(), PlatformUI.getWorkbench()
                .getPerspectiveRegistry()
                .findPerspectiveWithId(IpsProductDefinitionPerspectiveFactory.PRODUCTDEFINITIONPERSPECTIVE_ID), m);
        openSynchronizePerspectiveAction = new OpenPerspectiveAction(getWindow(), PlatformUI.getWorkbench()
                .getPerspectiveRegistry().findPerspectiveWithId("org.eclipse.team.ui.TeamSynchronizingPerspective"), m); //$NON-NLS-1$
        openCVSPerspectiveAction = new OpenPerspectiveAction(getWindow(), PlatformUI.getWorkbench()
                .getPerspectiveRegistry().findPerspectiveWithId("org.eclipse.team.cvs.ui.cvsPerspective"), m); //$NON-NLS-1$

        pinEditorContributionItem = ContributionItemFactory.PIN_EDITOR.create(window);

    }

    private void disableUnwantedActionSets(IWorkbenchPage page) {
        if (page == null || disablingUnwantedActionSets) {
            return;
        }
        disablingUnwantedActionSets = true;
        page.hideActionSet("org.eclipse.ui.externaltools.ExternalToolsSet"); //$NON-NLS-1$
        page.hideActionSet("org.eclipse.update.ui.softwareUpdates"); //$NON-NLS-1$
        page.hideActionSet("org.eclipse.ui.cheatsheets.actionSet"); //$NON-NLS-1$
        page.hideActionSet("org.eclipse.ui.edit.text.actionSet.convertLineDelimitersTo"); //$NON-NLS-1$
        page.hideActionSet("org.eclipse.debug.ui.debugActionSet"); //$NON-NLS-1$
        page.hideActionSet("org.eclipse.ui.actionSet.keyBindings"); //$NON-NLS-1$
        disablingUnwantedActionSets = false;

    }

    private void fixPerspective(IWorkbenchPage page) {
        disableUnwantedActionSets(page);

        if (page.getPerspective().getId().equals("org.eclipse.team.ui.TeamSynchronizingPerspective")) { //$NON-NLS-1$
            IViewReference[] refs = page.getViewReferences();
            for (int i = 0; i < refs.length; i++) {
                if (!refs[i].getId().equals("org.eclipse.team.sync.views.SynchronizeView")) { //$NON-NLS-1$
                    page.hideView(refs[i]);
                }
            }

        }

    }

    /**
     * Class handling the selection of a open-perspective-action.
     */
    private class PerspecitveHandler extends PerspectiveMenu {

        public PerspecitveHandler(IWorkbenchWindow window, String id) {
            super(window, id);
        }

        @Override
        protected void run(IPerspectiveDescriptor desc) {
            try {
                IWorkbenchPage page = PlatformUI.getWorkbench().showPerspective(desc.getId(), getWindow());
                fixPerspective(page);
            } catch (WorkbenchException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
