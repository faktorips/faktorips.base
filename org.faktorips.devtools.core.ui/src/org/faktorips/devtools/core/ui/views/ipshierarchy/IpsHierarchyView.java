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
import org.eclipse.ui.part.ViewPart;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.type.TypeHierarchy;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.views.IpsElementDragListener;
import org.faktorips.devtools.core.ui.views.IpsElementDropListener;
import org.faktorips.devtools.core.ui.views.TreeViewerDoubleclickListener;

public class IpsHierarchyView extends ViewPart implements IResourceChangeListener {
    public static final String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.ipshierarchy.IpsHierarchy"; //$NON-NLS-1$
    public static final String LOGO = "IpsHierarchyView.gif"; //$NON-NLS-1$
    private TreeViewer treeViewer;
    private Label errormsg;
    private Composite panel;
    private HierarchyContentProvider hierarchyContentProvider = new HierarchyContentProvider();
    private Display display;
    private Action showTypeHierarchyAction;
    private Action showSubtypeHierarchyAction;
    private Action clearAction;
    private Action refreshAction;
    protected boolean linkingEnabled = true;

    /**
     * Check whether this HierarchyView supports this object or not. Null is also a supported type
     * to reset the editor.
     * 
     * @param object the object to test
     * @return true of the HierarchyView supports this object
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
            IEditorPart editorPart = getSite().getPage().getActiveEditor();
            if (editorPart != null) {
                try {
                    editorActivated(editorPart);
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }
        }
    }

    private void initToolBar(IToolBarManager toolBarManager) {
        // show Type Hierarchy
        showTypeHierarchyAction = new Action(Messages.IpsHierarchy_tooltipShowTypeHierarchy, SWT.TOGGLE) {
            @Override
            public ImageDescriptor getImageDescriptor() {
                return IpsUIPlugin.getImageHandling().createImageDescriptor("ShowTypeHierarchy.gif"); //$NON-NLS-1$
            }

            @Override
            public void run() {
                try {
                    showHierarchy(hierarchyContentProvider.getTypeHierarchy().getType());
                    showSubtypeHierarchyAction.setChecked(false);
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }

            @Override
            public String getToolTipText() {
                return Messages.IpsHierarchy_tooltipShowTypeHierarchy;
            }
        };
        toolBarManager.add(showTypeHierarchyAction);
        // show the Subtype Hierarchy
        showSubtypeHierarchyAction = new Action(Messages.IpsHierarchy_tooltipShowSubtypeHierarchy, SWT.TOGGLE) {
            @Override
            public ImageDescriptor getImageDescriptor() {
                return IpsUIPlugin.getImageHandling().createImageDescriptor("ShowSubtypeHierarchy.gif"); //$NON-NLS-1$
            }

            @Override
            public void run() {
                try {
                    setInputData(TypeHierarchy.getSubtypeHierarchy(hierarchyContentProvider.getTypeHierarchy()
                            .getType()));
                    showTypeHierarchyAction.setChecked(false);
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }

            @Override
            public String getToolTipText() {
                return Messages.IpsHierarchy_tooltipShowSubtypeHierarchy;
            }
        };
        toolBarManager.add(showSubtypeHierarchyAction);
        refreshAction = new Action(Messages.IpsHierarchy_tooltipRefreshContents, IpsUIPlugin.getImageHandling()
                .createImageDescriptor("Refresh.gif")) //$NON-NLS-1$
        {
            @Override
            public void run() {
                setInputData((hierarchyContentProvider.getTypeHierarchy()));
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
                hierarchyContentProvider.getTypeHierarchy().getType();
                showTypeHierarchyAction.setChecked(false);
                setInputData(null);
            }

            @Override
            public String getToolTipText() {
                return Messages.IpsHierarchy_tooltipClear;
            }
        };
        toolBarManager.add(clearAction);
        toolBarManager.add(new Action(Messages.IpsHierarchy_tooltipLinkWithEditor, SWT.TOGGLE) {
            @Override
            public ImageDescriptor getImageDescriptor() {
                return IpsUIPlugin.getImageHandling().createImageDescriptor("elcl16/synced.gif"); //$NON-NLS-1$
            }

            @Override
            public void run() {
                changeLinkingEnabled();
            }

            @Override
            public String getToolTipText() {
                return Messages.IpsHierarchy_tooltipLinkWithEditor;
            }
        });
    }

    /**
     * Get the Hierarchy of the IIpsObject for setting it in the Treeviewer
     * 
     */
    public void showHierarchy(IIpsObject element) throws CoreException {
        ITypeHierarchy hierarchy = null;
        if (element instanceof IType && element.getEnclosingResource().isAccessible()) {
            IType iType = (IType)element;
            hierarchy = TypeHierarchy.getTypeHierarchy(iType);
        }
        showSubtypeHierarchyAction.setChecked(false);
        setInputData(hierarchy);
    }

    /**
     * 
     * Update the IpsHierarchyView in chase of changes on the file
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

    @Override
    public void setFocus() {
        // does nothing
    }

    private void changeLinkingEnabled() {
        if (linkingEnabled) {
            linkingEnabled = false;
        } else {
            linkingEnabled = true;
        }
    }

    private void setInputData(final ITypeHierarchy hierarchy) {
        treeViewer.setInput(hierarchy);
        updateView();
    }

    private void enableButtons(boolean status) {
        showSubtypeHierarchyAction.setEnabled(status);
        showTypeHierarchyAction.setEnabled(status);
        clearAction.setEnabled(status);
        refreshAction.setEnabled(status);
    }

    private void updateView() {
        if (treeViewer != null && !treeViewer.getControl().isDisposed()) {
            Object element = treeViewer.getInput();
            if (element == null) {
                showEmptyMessage();
            } else if (element instanceof ITypeHierarchy) {
                ITypeHierarchy hierarchy = (ITypeHierarchy)element;
                showMessgeOrTableView(MessageTableSwitch.TABLE);
                enableButtons(true);
                treeViewer.refresh();
                int level = 1;
                treeViewer.expandToLevel(hierarchy.getType(), level);
                treeViewer.setSelection(new StructuredSelection(hierarchy.getType()), true);
                showTypeHierarchyAction.setChecked(true);
            }
        }
    }

    private void showEmptyMessage() {
        enableButtons(false);
        showErrorMessage(Messages.IpsHierarchy_infoMessageEmptyView);
    }

    private void showErrorMessage(String message) {
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

    private void isNodeOfHierarchy(List<IIpsSrcFile> ipsSrcFiles, ITypeHierarchy hierarchyTreeViewer)
            throws CoreException {
        for (IIpsSrcFile ipsSrcFile : ipsSrcFiles) {
            String qName = ipsSrcFile.getQualifiedNameType().getName();
            if (hierarchyTreeViewer.isPartOfHierarchy(qName)) {
                updateHierarchy();
                return;
            }
            if (ipsSrcFile.exists()) {
                String superType = ipsSrcFile.getPropertyValue(IType.PROPERTY_SUPERTYPE);
                if (hierarchyTreeViewer.isPartOfHierarchy(superType)) {
                    updateHierarchy();
                    return;
                }
            }
        }
    }

    private void updateHierarchy() {
        display.asyncExec(new Runnable() {
            @Override
            public void run() {
                try {
                    showHierarchy(hierarchyContentProvider.getTypeHierarchy().getType());
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }
        });
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

    private void editorActivated(IEditorPart editorPart) throws CoreException {
        if (!linkingEnabled || editorPart == null) {
            return;
        }
        if (editorPart instanceof IpsObjectEditor) {
            IpsObjectEditor ipsEditor = (IpsObjectEditor)editorPart;
            IIpsObject i = ipsEditor.getIpsSrcFile().getIpsObject();
            if (i instanceof IType) {
                showHierarchy(i);
            }
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
