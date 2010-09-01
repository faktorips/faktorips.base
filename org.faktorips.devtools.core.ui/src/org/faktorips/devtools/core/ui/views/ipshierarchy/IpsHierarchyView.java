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

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
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
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.part.ViewPart;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.internal.model.pctype.TypeHierarchy;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.views.IpsElementDragListener;
import org.faktorips.devtools.core.ui.views.IpsElementDropListener;
import org.faktorips.devtools.core.ui.views.TreeViewerDoubleclickListener;

public class IpsHierarchyView extends ViewPart implements IResourceChangeListener {
    public static final String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.ipshierarchy.IpsHierarchy"; //$NON-NLS-1$
    public static final String LOGO = "IpsHierarchy.gif"; //$NON-NLS-1$
    private TreeViewer treeViewer;
    private Label errormsg;
    private Composite panel;
    private ImageHyperlink selectedElementLink;
    private HierarchyContentProvider hierarchyContentProvider = new HierarchyContentProvider();
    private Display display;

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
        dropTarget.addDropListener(new InstanceDropListener());
        dropTarget.setTransfer(new Transfer[] { FileTransfer.getInstance() });

        selectedElementLink = new ImageHyperlink(panel, SWT.FILL);
        selectedElementLink.setText(""); //$NON-NLS-1$
        selectedElementLink.setUnderlined(true);
        selectedElementLink.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

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
    }

    private void initToolBar(IToolBarManager toolBarManager) {
        // show Type Hierarchy
        toolBarManager.add(new Action(Messages.Hierarchy_tooltipShowTypeHierarchy, IpsUIPlugin.getImageHandling()
                .createImageDescriptor("ShowTypeHierarchy.gif")) { //$NON-NLS-1$
                    @Override
                    public void run() {
                        try {
                            getHierarchy(hierarchyContentProvider.getActualElement());
                        } catch (CoreException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public String getToolTipText() {
                        return Messages.Hierarchy_tooltipShowTypeHierarchy;
                    }
                });

        // show the Subtype Hierarchy
        toolBarManager.add(new Action(Messages.Hierarchy_tooltipShowSubtypeHierarchy, IpsUIPlugin.getImageHandling()
                .createImageDescriptor("ShowSubtypeHierarchy.gif")) { //$NON-NLS-1$
                    @Override
                    public void run() {
                        try {
                            setInputData(TypeHierarchy.getSubtypeHierarchy(hierarchyContentProvider.getActualElement()));
                        } catch (CoreException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public String getToolTipText() {
                        return Messages.Hierarchy_tooltipShowSubtypeHierarchy;
                    }
                });

        // refresh action
        Action refreshAction = new Action(Messages.Hierarchy_tooltipRefreshContents, IpsUIPlugin.getImageHandling()
                .createImageDescriptor("Refresh.gif")) { //$NON-NLS-1$

            @Override
            public void run() {
                try {
                    getHierarchy(hierarchyContentProvider.getActualElement());
                } catch (CoreException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String getToolTipText() {
                return Messages.Hierarchy_tooltipRefreshContents;
            }
        };
        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
        IWorkbenchAction retargetAction = ActionFactory.REFRESH.create(getViewSite().getWorkbenchWindow());
        retargetAction.setImageDescriptor(refreshAction.getImageDescriptor());
        retargetAction.setToolTipText(refreshAction.getToolTipText());
        toolBarManager.add(retargetAction);

        // clear action
        toolBarManager.add(new Action(Messages.Hierarchy_tooltipClear, IpsUIPlugin.getImageHandling()
                .createImageDescriptor("Clear.gif")) { //$NON-NLS-1$
                    @Override
                    public void run() {
                        hierarchyContentProvider.deleteActualElement();
                        setInputData(null);
                    }

                    @Override
                    public String getToolTipText() {
                        return Messages.Hierarchy_tooltipClear;
                    }
                });

    }

    private void setInputData(final ITypeHierarchy hierarchy) {
        treeViewer.setInput(hierarchy);
        updateView();
    }

    private void updateView() {

        if (treeViewer != null && !treeViewer.getControl().isDisposed()) {
            Object element = treeViewer.getInput();
            if (element == null) {
                showEmptyMessage();
            } else if (element instanceof TypeHierarchy) {
                showMessgeOrTableView(MessageTableSwitch.TABLE);
                treeViewer.refresh();
                int level = 1;
                treeViewer.expandToLevel(hierarchyContentProvider.getActualElement(), level);
                treeViewer.setSelection(new StructuredSelection(hierarchyContentProvider.getActualElement()), true);
            }
        }
    }

    private void showEmptyMessage() {
        showErrorMessage(Messages.Hierarchy_infoMessageEmptyView);
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
        return object instanceof IPolicyCmptType || object instanceof IProductCmptType;
    }

    private enum MessageTableSwitch {
        MESSAGE,
        TABLE;

        public boolean isMessage() {
            return equals(MESSAGE);
        }

    }

    @Override
    public void setFocus() {
        // does nothing
    }

    private class InstanceDropListener extends IpsElementDropListener {

        @Override
        public void dragEnter(DropTargetEvent event) {
            dropAccept(event);
        }

        @Override
        public void drop(DropTargetEvent event) {
            Object[] transferred = super.getTransferedElements(event.currentDataType);
            if (transferred.length > 0 && transferred[0] instanceof IIpsSrcFile) {
                try {
                    getHierarchy(((IIpsSrcFile)transferred[0]).getIpsObject());
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

    public void resourceChanged(IResourceChangeEvent event) {
        ITypeHierarchy hierarchy = hierarchyContentProvider.getInput();
        if (hierarchy != null) {
            IIpsElement iipsElement = null;
            // IType[] hierarchy2 = null;
            IIpsObject iipsObject = null;
            String supertype = ""; //$NON-NLS-1$
            IResourceDelta[] element = event.getDelta().getAffectedChildren();
            ArrayList<IResourceDelta> a = null;
            a = weiter(element[0].getAffectedChildren());

            for (int i = 0; a.size() > i; i++) {
                if (iipsElement == null) {
                    iipsElement = IpsPlugin.getDefault().getIpsModel().findIpsElement(a.get(i).getResource());
                }
            }
            IpsSrcFile ipsSrcFile = null;
            if (iipsElement instanceof IpsSrcFile) {
                ipsSrcFile = (IpsSrcFile)iipsElement;
            }
            try {
                if (ipsSrcFile != null) {
                    iipsObject = ipsSrcFile.getIpsObject();
                }
            } catch (CoreException e1) {
                e1.printStackTrace();
            }
            if (iipsObject instanceof IType) {
                IType itype = (IType)iipsObject;
                supertype = itype.getSupertype();
                // hierarchy2 = hierarchy.getAllSupertypes(itype);
            }
            if (/* hierarchy2.length > 0 || */hierarchy.isSupertype(supertype)) {
                display.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getHierarchy(hierarchyContentProvider.getActualElement());
                        } catch (CoreException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    private ArrayList<IResourceDelta> weiter(IResourceDelta[] resourceDelta) {
        ArrayList<IResourceDelta> a = new ArrayList<IResourceDelta>();
        for (int i = 0; resourceDelta.length > i; i++) {
            a.add(getAffectedChildren(resourceDelta[i].getAffectedChildren()));
        }
        return a;
    }

    private IResourceDelta getAffectedChildren(IResourceDelta[] resourceDelta) {
        if (resourceDelta[0].getAffectedChildren().length == 0) {
            return resourceDelta[0];
        }
        // for (int i = 0; resourceDelta.length > i; i++) {
        return getAffectedChildren(resourceDelta[0].getAffectedChildren());
        // }
    }

    /**
     * 
     * Returns the hierarchy of IIpsObject
     * 
     */
    public void getHierarchy(IIpsObject element) throws CoreException {
        ITypeHierarchy hierarchy = null;
        if (element != null && !element.getEnclosingResource().isAccessible()) {
            setInputData(null);
        } else if (element instanceof IProductCmptType) {
            IProductCmptType pcType = (IProductCmptType)element;
            hierarchy = TypeHierarchy.getTypeHierarchy(pcType);
            hierarchyContentProvider.setActualElement(pcType);
        } else if (element instanceof IPolicyCmptType) {
            IPolicyCmptType pcType = (IPolicyCmptType)element;
            hierarchy = TypeHierarchy.getTypeHierarchy(pcType);
            hierarchyContentProvider.setActualElement(pcType);
        }
        setInputData(hierarchy);
    }

}
