/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.ipshierarchy;

import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.contexts.IContextService;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsMenuId;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.MenuCleaner;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.views.AbstractShowInSupportingViewPart;
import org.faktorips.devtools.core.ui.views.IpsElementDragListener;
import org.faktorips.devtools.core.ui.views.IpsElementDropListener;
import org.faktorips.devtools.core.ui.views.TreeViewerDoubleclickListener;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IIpsSrcFilesChangeListener;
import org.faktorips.devtools.model.IpsSrcFilesChangedEvent;
import org.faktorips.devtools.model.internal.type.TypeHierarchy;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.type.ITypeHierarchy;

/**
 * The <code>IpsHierarchyView</code> is a <code>ViewPart</code> for displaying
 * a<code>hierarchy of ITypes</code>
 * 
 * @author Quirin Stoll
 */
public class IpsHierarchyView extends AbstractShowInSupportingViewPart implements IIpsSrcFilesChangeListener {

    public static final String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.ipshierarchy.IpsHierarchy"; //$NON-NLS-1$
    protected static final String LINK_WITH_EDITOR_KEY = "linktoeditor"; //$NON-NLS-1$
    private static final String MEMENTO = "ipsHierarchyView.memento"; //$NON-NLS-1$

    private TreeViewer treeViewer;
    private Label errormsg;
    private Composite panel;
    private HierarchyContentProvider hierarchyContentProvider = new HierarchyContentProvider();
    private Display display;
    private Action clearAction;
    private Action refreshAction;
    private Action linkWithEditor;
    private Label selected;
    private boolean linkingEnabled = false;
    private ActivationListener editorActivationListener;

    public IpsHierarchyView() {
        IIpsModel.get().addIpsSrcFilesChangedListener(this);
    }

    /**
     * Check whether this HierarchyView supports this object or not. Null is also a supported type
     * to reset the editor.
     * 
     * @param object the object to test
     * @return true of the HierarchyView supports this object and false if not
     */
    public static boolean supports(Object object) {
        return object instanceof IType;
    }

    @Override
    public void createPartControl(Composite parent) {
        display = parent.getDisplay();
        panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new GridLayout());
        panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        DropTarget dropTarget = new DropTarget(parent, DND.DROP_LINK);
        dropTarget.addDropListener(new HierarchyDropListener());
        dropTarget.setTransfer(FileTransfer.getInstance());

        selected = new Label(panel, SWT.LEFT);
        selected.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        treeViewer = new TreeViewer(panel);
        DefaultLabelProvider labelProvider = new DefaultLabelProvider();
        IDecoratorManager decoManager = IpsPlugin.getDefault().getWorkbench().getDecoratorManager();
        DecoratingLabelProvider decoratedLabelProvider = new DecoratingLabelProvider(labelProvider,
                decoManager.getLabelDecorator());
        treeViewer.setLabelProvider(decoratedLabelProvider);
        treeViewer.setContentProvider(hierarchyContentProvider);
        treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        getSite().setSelectionProvider(treeViewer);
        treeViewer.addDoubleClickListener(new TreeViewerDoubleclickListener(treeViewer));

        treeViewer.addDragSupport(DND.DROP_LINK, new Transfer[] { FileTransfer.getInstance() },
                new IpsElementDragListener(treeViewer));

        GridData errorLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        errorLayoutData.exclude = true;
        errormsg = new Label(panel, SWT.WRAP);
        errormsg.setLayoutData(errorLayoutData);

        IActionBars actionBars = getViewSite().getActionBars();
        initToolBar(actionBars.getToolBarManager());
        showEmptyMessage();

        if (linkingEnabled) {
            linkWithEditor.setChecked(true);
            IEditorPart editorPart = getSite().getPage().getActiveEditor();
            if (editorPart != null) {
                editorActivated(editorPart);
            }
        }
        editorActivationListener = new ActivationListener(getSite().getPage());
        activateContext();
        createContextMenu();
    }

    private void initToolBar(IToolBarManager toolBarManager) {

        // refresh action
        refreshAction = new Action(Messages.IpsHierarchy_tooltipRefreshContents,
                IpsUIPlugin.getImageHandling().createImageDescriptor("Refresh.gif")) { //$NON-NLS-1$
            @Override
            public void run() {
                showHierarchy(((ITypeHierarchy)treeViewer.getInput()).getType());
            }

            @Override
            public String getToolTipText() {
                return Messages.IpsHierarchy_tooltipRefreshContents;
            }
        };
        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
        IWorkbenchAction retargetAction = ActionFactory.REFRESH.create(getViewSite().getWorkbenchWindow());
        retargetAction.setImageDescriptor(refreshAction.getImageDescriptor());
        retargetAction.setToolTipText(refreshAction.getToolTipText());
        getViewSite().getActionBars().getToolBarManager().add(refreshAction);
        // toolBarManager.add(refreshAction);

        // clear action
        clearAction = new Action(Messages.IpsHierarchy_tooltipClear, IpsUIPlugin.getImageHandling()
                .createImageDescriptor("Clear.gif")) { //$NON-NLS-1$
            @Override
            public void run() {
                enableButtons(false);
                selected.setText(""); //$NON-NLS-1$
                showHierarchy(null);
            }

            @Override
            public String getToolTipText() {
                return Messages.IpsHierarchy_tooltipClear;
            }
        };
        toolBarManager.add(clearAction);

        // link with editor action
        linkWithEditor = new Action(Messages.IpsHierarchy_tooltipLinkWithEditor, SWT.TOGGLE) {
            @Override
            public ImageDescriptor getImageDescriptor() {
                return IpsUIPlugin.getImageHandling().createImageDescriptor("elcl16/synced.gif"); //$NON-NLS-1$
            }

            @Override
            public void run() {
                setLinkingEnabled(isChecked());
            }

            @Override
            public String getToolTipText() {
                return Messages.IpsHierarchy_tooltipLinkWithEditor;
            }
        };
        toolBarManager.add(linkWithEditor);
    }

    private void createContextMenu() {
        MenuManager manager = new MenuManager();
        manager.add(new Separator("open")); //$NON-NLS-1$
        manager.add(new OpenEditorAction(treeViewer));
        IpsMenuId.GROUP_JUMP_TO_SOURCE_CODE.addSeparator(manager);
        IpsMenuId.addDefaultGroups(manager);
        Menu contextMenu = manager.createContextMenu(treeViewer.getControl());
        treeViewer.getControl().setMenu(contextMenu);
        getSite().registerContextMenu(manager, treeViewer);
        MenuCleaner.addDefaultCleaner(manager);
    }

    @Override
    public void dispose() {
        editorActivationListener.dispose();
        IIpsModel.get().removeIpsSrcFilesChangedListener(this);
        super.dispose();
    }

    private void enableButtons(boolean status) {
        clearAction.setEnabled(status);
        refreshAction.setEnabled(status);
    }

    @Override
    public void setFocus() {
        Control control = treeViewer.getControl();
        if (control != null && !control.isDisposed()) {
            control.setFocus();
        }
    }

    protected String getWaitingLabel() {
        return Messages.IpsHierarchy_waitingLabel;
    }

    /**
     * Get the Hierarchy of the IIpsObject for setting it in the TreeViewer
     */
    public void showHierarchy(final IIpsObject element) {
        if (element instanceof IType iType && element.getEnclosingResource().isAccessible()) {
            // TODO: Waiting label is currently deactivated. Check if we do need some
            // display.asyncExec(new Runnable() {
            // @Override
            // public void run() {
            // selected.setText(""); //$NON-NLS-1$
            // treeViewer.setInput(getWaitingLabel());
            // updateView();
            // }
            // });
            BuildingHierarchyJob job = new BuildingHierarchyJob(iType);
            job.schedule();
        } else if (element == null) {
            setInputData(null);
        }
    }

    private void setInputData(final ITypeHierarchy hierarchy) {
        treeViewer.setInput(hierarchy);
        updateView();
    }

    /**
     * Updates View
     */
    private void updateView() {
        if (treeViewer != null && !treeViewer.getControl().isDisposed()) {
            Object element = treeViewer.getInput();
            if (element == null) {
                showEmptyMessage();
            } else if (element instanceof String) {
                // waiting for building hierarchy
                showMessageOrTableView(MessageTableSwitch.TABLE);
            } else if (element instanceof ITypeHierarchy hierarchy) {
                showMessageOrTableView(MessageTableSwitch.TABLE);
                enableButtons(true);
                IType type = hierarchy.getType();
                treeViewer.expandAll();
                treeViewer.setSelection(new StructuredSelection(type), true);
                selected.setText(type.getName());
            }
        }
    }

    private void showEmptyMessage() {
        showErrorMessage(Messages.IpsHierarchy_infoMessageEmptyView);
        selected.setText(""); //$NON-NLS-1$
    }

    private void showErrorMessage(String message) {
        enableButtons(false);
        if (errormsg != null && !errormsg.isDisposed()) {
            errormsg.setText(message);
            showMessageOrTableView(MessageTableSwitch.MESSAGE);
        }
    }

    private void showMessageOrTableView(MessageTableSwitch mtSwitch) {
        boolean messageWasVisible = false;
        if (errormsg != null && !errormsg.isDisposed()) {
            messageWasVisible = errormsg.isVisible();
            errormsg.setVisible(mtSwitch.isMessage());
            ((GridData)errormsg.getLayoutData()).exclude = !mtSwitch.isMessage();
        }
        if (treeViewer != null && !treeViewer.getTree().isDisposed()) {
            treeViewer.getTree().setVisible(!mtSwitch.isMessage());
            ((GridData)treeViewer.getTree().getLayoutData()).exclude = mtSwitch.isMessage();
        }
        if (panel != null && !panel.isDisposed()) {
            panel.layout();
        }
        if (messageWasVisible) {
            setFocus();
        }
    }

    /**
     * 
     * Update the IpsHierarchyView in chase of changes on the object
     */
    @Override
    public void ipsSrcFilesChanged(IpsSrcFilesChangedEvent event) {
        final Set<IIpsSrcFile> changedIpsSrcFiles = event.getChangedIpsSrcFiles();
        if (treeViewer == null) {
            return;
        }
        if (changedIpsSrcFiles.size() > 0) {
            Display.getDefault().asyncExec(() -> {
                ITypeHierarchy hierarchyTree = (ITypeHierarchy)treeViewer.getInput();
                if (hierarchyTree != null) {
                    isNodeOfHierarchy(changedIpsSrcFiles, hierarchyTree);
                }
            });
        }

    }

    /**
     * Test if changed object is part of the hierarchy
     */
    protected void isNodeOfHierarchy(Set<IIpsSrcFile> ipsSrcFiles, ITypeHierarchy hierarchyTreeViewer) {
        for (IIpsSrcFile ipsSrcFile : ipsSrcFiles) {
            String qName = ipsSrcFile.getQualifiedNameType().getName();
            if (hierarchyTreeViewer.isSelectedType(qName) && !ipsSrcFile.exists()) {
                showHierarchy(null);
                return;
            }
            if (hierarchyTreeViewer.isPartOfHierarchy(qName)) {
                showHierarchy(hierarchyTreeViewer.getType());
                return;
            }
            if (ipsSrcFile.exists()) {
                // getPropertyValue simply returns null if the property does not exist
                String superType = ipsSrcFile.getPropertyValue(IType.PROPERTY_SUPERTYPE);
                if (superType != null) {
                    if (hierarchyTreeViewer.isPartOfHierarchy(superType)) {
                        showHierarchy(hierarchyTreeViewer.getType());
                        return;
                    }
                }
            }
        }
    }

    private void setLinkingEnabled(boolean linkingEnabled) {
        this.linkingEnabled = linkingEnabled;

        if (linkingEnabled) {
            IEditorPart editorPart = getSite().getPage().getActiveEditor();
            if (editorPart != null) {
                editorActivated(editorPart);
            }
        }
    }

    private void editorActivated(IEditorPart editorPart) {
        if (!linkingEnabled || editorPart == null) {
            return;
        }
        if (editorPart instanceof IpsObjectEditor ipsEditor) {
            IIpsObject iipsObject = ipsEditor.getIpsSrcFile().getIpsObject();
            if (iipsObject instanceof IType) {
                showHierarchy(iipsObject);
            }
        }
    }

    /**
     * Initialize the button "Link with Editor"
     */
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);
        if (memento != null) {
            IMemento layout = memento.getChild(MEMENTO);
            if (layout != null) {
                Integer linkingValue = layout.getInteger(LINK_WITH_EDITOR_KEY);
                linkingEnabled = linkingValue == null ? false : linkingValue.intValue() == 1;
            }
        }
    }

    /**
     * Saves state of the button "Link with Editor"
     * 
     */
    @Override
    public void saveState(IMemento memento) {
        super.saveState(memento);
        IMemento layout = memento.createChild(MEMENTO);
        layout.putInteger(LINK_WITH_EDITOR_KEY, linkingEnabled ? 1 : 0);
    }

    private void activateContext() {
        IContextService service = getSite().getService(IContextService.class);
        service.activateContext("org.faktorips.devtools.core.ui.views.modelExplorer.context"); //$NON-NLS-1$
    }

    @Override
    protected ISelection getSelection() {
        return treeViewer.getSelection();
    }

    @Override
    protected boolean show(IAdaptable adaptable) {
        IIpsObject ipsObject = adaptable.getAdapter(IIpsObject.class);
        if (ipsObject == null) {
            return false;
        }
        if (supports(ipsObject)) {
            showHierarchy(ipsObject);
            return true;
        }
        return false;
    }

    private class ActivationListener implements IPartListener, IWindowListener {

        private IPartService partService;

        /**
         * Creates a new activation listener.
         */
        public ActivationListener(IPartService partService) {
            this.partService = partService;
            partService.addPartListener(this);
            PlatformUI.getWorkbench().addWindowListener(this);
        }

        /**
         * Disposes this activation listener.
         */
        public void dispose() {
            partService.removePartListener(this);
            PlatformUI.getWorkbench().removeWindowListener(this);
            partService = null;
        }

        @Override
        public void partActivated(IWorkbenchPart part) {
            if (part instanceof IEditorPart) {
                editorActivated((IEditorPart)part);
            }
        }

        @Override
        public void windowActivated(IWorkbenchWindow window) {
            editorActivated(window.getActivePage().getActiveEditor());
        }

        @Override
        public void partBroughtToTop(IWorkbenchPart part) {
            // Nothing to do
        }

        @Override
        public void partClosed(IWorkbenchPart part) {
            // Nothing to do
        }

        @Override
        public void partDeactivated(IWorkbenchPart part) {
            // Nothing to do
        }

        @Override
        public void partOpened(IWorkbenchPart part) {
            // Nothing to do
        }

        @Override
        public void windowDeactivated(IWorkbenchWindow window) {
            // Nothing to do
        }

        @Override
        public void windowClosed(IWorkbenchWindow window) {
            // Nothing to do
        }

        @Override
        public void windowOpened(IWorkbenchWindow window) {
            // Nothing to do
        }

    }

    public class BuildingHierarchyJob extends Job {
        private final IType iType;

        public BuildingHierarchyJob(IType iType) {
            super(getWaitingLabel());
            this.iType = iType;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            try {
                final ITypeHierarchy hierarchy = TypeHierarchy.getTypeHierarchy(iType);
                display.asyncExec(() -> setInputData(hierarchy));
            } catch (IpsException e) {
                IpsPlugin.log(e);
            }
            return new Status(IStatus.OK, IpsUIPlugin.PLUGIN_ID, null);
        }
    }

    private class HierarchyDropListener extends IpsElementDropListener {

        @Override
        public void dragEnter(DropTargetEvent event) {
            dropAccept(event);
        }

        @Override
        public void drop(DropTargetEvent event) {
            Object[] transferred = super.getTransferedElements(event.currentDataType);
            if (transferred.length > 0 && transferred[0] instanceof IIpsSrcFile) {
                showHierarchy(((IIpsSrcFile)transferred[0]).getIpsObject());
            }
        }

        @Override
        public void dropAccept(DropTargetEvent event) {
            event.detail = DND.DROP_NONE;
            Object[] transferred = super.getTransferedElements(event.currentDataType);
            if (transferred == null) {
                if (super.getTransfer().isSupportedType(event.currentDataType)) {
                    event.detail = DND.DROP_LINK;
                }
                return;
            }
            if (transferred.length == 1 && transferred[0] instanceof IIpsSrcFile ipsSrcFile) {
                IIpsObject selectedIpsObject = ipsSrcFile.getIpsObject();
                if (selectedIpsObject instanceof IType) {
                    event.detail = DND.DROP_LINK;
                }

            }
        }

        @Override
        public int getSupportedOperations() {
            return DND.DROP_LINK;
        }
    }

    private enum MessageTableSwitch {
        MESSAGE,
        TABLE;

        public boolean isMessage() {
            return equals(MESSAGE);
        }
    }

}
