/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views.ipshierarchy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
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
import org.eclipse.ui.part.ViewPart;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.type.TypeHierarchy;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.ITypeHierarchy;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.views.IpsElementDragListener;
import org.faktorips.devtools.core.ui.views.IpsElementDropListener;
import org.faktorips.devtools.core.ui.views.TreeViewerDoubleclickListener;

/**
 * The <tt>IpsHierarchyView</tt> is a <tt>ViewPart</tt> for displaying a<tt>hierarchy of ITypes</tt>
 * 
 * @author stoll
 */
public class IpsHierarchyView extends ViewPart implements IResourceChangeListener {
    public static final String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.ipshierarchy.IpsHierarchy"; //$NON-NLS-1$
    public static final String LOGO = "IpsHierarchyView.gif"; //$NON-NLS-1$
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

    /**
     * Check whether this HierarchyView supports this object or not. Null is also a supported type
     * to reset the editor.
     * 
     * @param object the object to test
     * @return true of the HierarchyView supports this object and false if not
     */
    public static boolean supports(Object object) {
        if (object == null) {
            return true;
        }
        return object instanceof IType;
    }

    public IpsHierarchyView() {
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_BUILD);
    }

    @Override
    public void createPartControl(Composite parent) {
        display = parent.getDisplay();
        panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new GridLayout());
        panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        DropTarget dropTarget = new DropTarget(parent, DND.DROP_LINK);
        dropTarget.addDropListener(new HierarchyDropListener());
        dropTarget.setTransfer(new Transfer[] { FileTransfer.getInstance() });

        selected = new Label(panel, SWT.LEFT);
        selected.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        treeViewer = new TreeViewer(panel);
        treeViewer.setLabelProvider(new DefaultLabelProvider());
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
                try {
                    editorActivated(editorPart);
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }
        }
        editorActivationListener = new ActivationListener(getSite().getPage());
    }

    private void initToolBar(IToolBarManager toolBarManager) {

        // refresh action
        refreshAction = new Action(Messages.IpsHierarchy_tooltipRefreshContents, IpsUIPlugin.getImageHandling()
                .createImageDescriptor("Refresh.gif")) //$NON-NLS-1$
        {
            @Override
            public void run() {
                showHierarchy((hierarchyContentProvider.getTypeHierarchy().getType()));
            }

            @Override
            public String getToolTipText() {
                return Messages.IpsHierarchy_tooltipRefreshContents;
            }
        };
        toolBarManager.add(refreshAction);

        // clear action
        clearAction = new Action(Messages.IpsHierarchy_tooltipClear, IpsUIPlugin.getImageHandling()
                .createImageDescriptor("Clear.gif")) { //$NON-NLS-1$
            @Override
            public void run() {
                enableButtons(false);
                selected.setText(""); //$NON-NLS-1$
                setInputData(null);
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
                try {
                    setLinkingEnabled(isChecked());
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }

            @Override
            public String getToolTipText() {
                return Messages.IpsHierarchy_tooltipLinkWithEditor;
            }
        };
        toolBarManager.add(linkWithEditor);
    }

    @Override
    public void dispose() {
        editorActivationListener.dispose();
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        super.dispose();
    }

    private void enableButtons(boolean status) {
        clearAction.setEnabled(status);
        refreshAction.setEnabled(status);
    }

    @Override
    public void setFocus() {
        // does nothing
    }

    protected String getWaitingLabel() {
        return Messages.IpsHierarchy_waitingLabel;
    }

    /**
     * Get the Hierarchy of the IIpsObject for setting it in the TreeViewer
     * 
     * 
     */
    public void showHierarchy(final IIpsObject element) {
        if (element instanceof IType && element.getEnclosingResource().isAccessible()) {
            IType iType = (IType)element;
            selected.setText(""); //$NON-NLS-1$
            treeViewer.setInput(getWaitingLabel());
            updateView();
            BuildingHierarchyJob job = new BuildingHierarchyJob(iType);
            job.schedule();
        }
    }

    private void setInputData(final ITypeHierarchy hierarchy) {
        display.asyncExec(new Runnable() {
            @Override
            public void run() {
                treeViewer.setInput(hierarchy);
                updateView();
            }
        });
    }

    private void updateView() {
        if (treeViewer != null && !treeViewer.getControl().isDisposed()) {
            Object element = treeViewer.getInput();
            if (element == null) {
                showEmptyMessage();
            } else if (element instanceof String) {
                // waiting for building hierarchy
                showMessgeOrTableView(MessageTableSwitch.TABLE);
            } else if (element instanceof ITypeHierarchy) {
                ITypeHierarchy hierarchy = (ITypeHierarchy)element;
                showMessgeOrTableView(MessageTableSwitch.TABLE);
                enableButtons(true);
                int level = 1;
                IType type = hierarchy.getType();
                treeViewer.expandToLevel(type, level);
                treeViewer.setSelection(new StructuredSelection(type), true);
                treeViewer.refresh();
                selected.setText(type.getName());
            }
        }
    }

    private void showEmptyMessage() {
        showErrorMessage(Messages.IpsHierarchy_infoMessageEmptyView);
    }

    private void showErrorMessage(String message) {
        enableButtons(false);
        if (errormsg != null && !errormsg.isDisposed()) {
            errormsg.setText(message);
            showMessgeOrTableView(MessageTableSwitch.MESSAGE);
        }
    }

    private void showMessgeOrTableView(MessageTableSwitch mtSwitch) {
        if (errormsg != null && !errormsg.isDisposed()) {
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
    }

    /**
     * 
     * Update the IpsHierarchyView in chase of changes on the object
     */
    public void resourceChanged(IResourceChangeEvent event) {
        ITypeHierarchy hierarchyTreeViewer = hierarchyContentProvider.getTypeHierarchy();
        if (hierarchyTreeViewer != null) {
            try {
                List<IFile> affectedFiles = getChangedFiles(event.getDelta().getAffectedChildren());
                List<IIpsSrcFile> aIipsElement = getIpsSrcFiles(affectedFiles);
                if (aIipsElement.size() > 0) {
                    isNodeOfHierarchy(aIipsElement, hierarchyTreeViewer);
                }
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
    }

    private void isNodeOfHierarchy(List<IIpsSrcFile> ipsSrcFiles, ITypeHierarchy hierarchyTreeViewer)
            throws CoreException {
        for (IIpsSrcFile ipsSrcFile : ipsSrcFiles) {
            String qName = ipsSrcFile.getQualifiedNameType().getName();
            if (hierarchyTreeViewer.isPartOfHierarchy(qName)) {
                showHierarchy(hierarchyContentProvider.getTypeHierarchy().getType());
                return;
            }
            if (ipsSrcFile.exists()) {
                String superType = ipsSrcFile.getPropertyValue(IType.PROPERTY_SUPERTYPE);
                if (superType != null) {
                    if (hierarchyTreeViewer.isPartOfHierarchy(superType)) {
                        showHierarchy(hierarchyContentProvider.getTypeHierarchy().getType());
                        return;
                    }
                }
            }
        }
    }

    /**
     * protected to test this method
     */
    protected List<IFile> getChangedFiles(IResourceDelta[] projectResourceDeltas) throws CoreException {
        List<IResourceDelta> projectDeltas = new ArrayList<IResourceDelta>();
        for (IResourceDelta aResourceDelta : projectResourceDeltas) {
            IResource aResource = aResourceDelta.getResource();
            IIpsProject ipsProject = null;
            if (aResource instanceof IProject) {
                IProject aProject = (IProject)aResource;
                ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(aProject);
            }
            if (ipsProject != null) {
                projectDeltas.add(aResourceDelta);
            }
        }
        return getChangedFilesFromIpsProjectDelta(projectDeltas);
    }

    private List<IFile> getChangedFilesFromIpsProjectDelta(List<IResourceDelta> ipsProjectDeltas) throws CoreException {
        List<IResourceDelta> ipsPackageFragmentRootDeltas = new ArrayList<IResourceDelta>();
        for (IResourceDelta aResourceDelta : ipsProjectDeltas) {
            IResource resource = aResourceDelta.getResource();
            IProject aProject = (IProject)resource;
            IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(aProject);
            IIpsPackageFragmentRoot[] roots = ipsProject.getIpsPackageFragmentRoots();
            for (IResourceDelta child : aResourceDelta.getAffectedChildren()) {
                for (IIpsPackageFragmentRoot pfRoot : roots) {
                    if (child.getResource().getName().equals(pfRoot.getName())) {
                        ipsPackageFragmentRootDeltas.add(child);
                    }
                }
            }
        }
        return getChangedFilesForIpsPackeFragmentRootDeltas(ipsPackageFragmentRootDeltas);
    }

    private List<IFile> getChangedFilesForIpsPackeFragmentRootDeltas(List<IResourceDelta> ipsPackageFragmentRootDeltas) {
        List<IFile> result = new ArrayList<IFile>();
        getChangedFilesRecoursive(ipsPackageFragmentRootDeltas.toArray(new IResourceDelta[0]), result);
        return result;
    }

    private void getChangedFilesRecoursive(IResourceDelta[] deltas, List<IFile> result) {
        for (IResourceDelta aDelta : deltas) {
            if (aDelta.getResource() instanceof IFile) {
                result.add((IFile)aDelta.getResource());
            } else {
                getChangedFilesRecoursive(aDelta.getAffectedChildren(), result);
            }
        }
    }

    private List<IIpsSrcFile> getIpsSrcFiles(List<IFile> affectedFiles) {
        List<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        for (IFile aFile : affectedFiles) {
            IIpsSrcFile srcFile = (IIpsSrcFile)IpsPlugin.getDefault().getIpsModel().getIpsElement(aFile);
            result.add(srcFile);
        }
        return result;
    }

    private void setLinkingEnabled(boolean linkingEnabled) throws CoreException {
        this.linkingEnabled = linkingEnabled;

        if (linkingEnabled) {
            IEditorPart editorPart = getSite().getPage().getActiveEditor();
            if (editorPart != null) {
                editorActivated(editorPart);
            }
        }
    }

    private void editorActivated(IEditorPart editorPart) throws CoreException {
        if (!linkingEnabled || editorPart == null) {
            return;
        }
        if (editorPart instanceof IpsObjectEditor) {
            IpsObjectEditor ipsEditor = (IpsObjectEditor)editorPart;
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
                try {
                    editorActivated((IEditorPart)part);
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }
        }

        @Override
        public void windowActivated(IWorkbenchWindow window) {
            try {
                editorActivated(window.getActivePage().getActiveEditor());
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
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
                ITypeHierarchy hierarchy = TypeHierarchy.getTypeHierarchy(iType);
                setInputData(hierarchy);
            } catch (CoreException e) {
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
                try {
                    showHierarchy(((IIpsSrcFile)transferred[0]).getIpsObject());
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
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
            if (transferred.length == 1 && transferred[0] instanceof IIpsSrcFile) {
                IIpsSrcFile ipsSrcFile = (IIpsSrcFile)transferred[0];
                try {
                    IIpsObject selected = ipsSrcFile.getIpsObject();
                    if (selected instanceof IType) {
                        event.detail = DND.DROP_LINK;
                    }
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }

            }
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
