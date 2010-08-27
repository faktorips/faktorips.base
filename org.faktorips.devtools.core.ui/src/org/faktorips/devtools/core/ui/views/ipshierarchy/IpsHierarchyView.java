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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.part.ViewPart;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.pctype.TypeHierarchy;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.views.IpsElementDragListener;
import org.faktorips.devtools.core.ui.views.IpsElementDropListener;
import org.faktorips.devtools.core.ui.views.instanceexplorer.InstanceLabelProvider;
import org.faktorips.devtools.core.ui.views.instanceexplorer.Messages;

public class IpsHierarchyView extends ViewPart {
    public static final String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.ipshierarchy.IpsHierarchy"; //$NON-NLS-1$
    public static final String IMAGE = "IpsHierarchy.gif"; //$NON-NLS-1$

    private TreeViewer treeViewer;
    private Label errormsg;
    private Composite panel;
    private ImageHyperlink selectedElementLink;
    private HierarchyContentProvider hierarchyContentProvider = new HierarchyContentProvider();
    private InstanceLabelProvider labelProvider;

    private DecoratingLabelProvider decoratedLabelProvider;

    @Override
    public void createPartControl(Composite parent) {

        panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new GridLayout());
        panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        DropTarget dropTarget = new DropTarget(parent, DND.DROP_LINK);
        dropTarget.addDropListener(new InstanceDropListener());
        dropTarget.setTransfer(new Transfer[] { FileTransfer.getInstance() });

        labelProvider = new InstanceLabelProvider();
        IDecoratorManager decoManager = IpsPlugin.getDefault().getWorkbench().getDecoratorManager();
        decoratedLabelProvider = new DecoratingLabelProvider(labelProvider, decoManager.getLabelDecorator());

        selectedElementLink = new ImageHyperlink(panel, SWT.FILL);
        selectedElementLink.setText(""); //$NON-NLS-1$
        selectedElementLink.setUnderlined(true);
        selectedElementLink.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        treeViewer = new TreeViewer(panel);
        treeViewer.setLabelProvider(new DefaultLabelProvider());
        treeViewer.setContentProvider(hierarchyContentProvider);
        treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        getSite().setSelectionProvider(treeViewer);
        treeViewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                // TODO Auto-generated method stub

            }
        });
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
        // refresh action

        Action refreshAction = new Action(Messages.InstanceExplorer_tooltipRefreshContents, IpsUIPlugin
                .getImageHandling().createImageDescriptor("Refresh.gif")) { //$NON-NLS-1$

            @Override
            public void run() {
                try {
                    getHierarchy(hierarchyContentProvider.getActualElement());
                } catch (CoreException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            @Override
            public String getToolTipText() {
                return Messages.InstanceExplorer_tooltipRefreshContents;
            }
        };
        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
        IWorkbenchAction retargetAction = ActionFactory.REFRESH.create(getViewSite().getWorkbenchWindow());
        retargetAction.setImageDescriptor(refreshAction.getImageDescriptor());
        retargetAction.setToolTipText(refreshAction.getToolTipText());
        toolBarManager.add(retargetAction);

        // clear action
        toolBarManager.add(new Action(Messages.InstanceExplorer_tooltipClear, IpsUIPlugin.getImageHandling()
                .createImageDescriptor("Clear.gif")) { //$NON-NLS-1$
                    @Override
                    public void run() {
                        setInputData(null);
                    }

                    @Override
                    public String getToolTipText() {
                        return Messages.InstanceExplorer_tooltipClear;
                    }
                });
    }

    private void setInputData(final ITypeHierarchy b) {
        treeViewer.setInput(b);
        updateView();
    }

    private void updateView() {

        if (treeViewer != null && !treeViewer.getControl().isDisposed()) {
            Object element = treeViewer.getInput();
            if (element == null) {
                showEmptyMessage();
            } else if (element instanceof TypeHierarchy) {

                // IIpsObject ipsObject = (IIpsObject)element;
                // if (selectedElementLink != null && !selectedElementLink.isDisposed()) {
                // selectedElementLink.setText(decoratedLabelProvider.getText(ipsObject));
                // selectedElementLink.setImage(decoratedLabelProvider.getImage(ipsObject));
                // }

                showMessgeOrTableView(MessageTableSwitch.TABLE);
                treeViewer.refresh();
            }
        }
    }

    private void showEmptyMessage() {
        showErrorMessage(Messages.InstanceExplorer_infoMessageEmptyView);
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
        // TODO Auto-generated method stub
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
                    IIpsObject selected = ipsSrcFile.getIpsObject(); // TODO Fragen???
                    event.detail = DND.DROP_LINK;
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }// ipsSrcFile.getIpsObjectType().newObject(ipsSrcFile);

            }
        }
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
            // hierarchy = TypeHierarchy.getTypeHierarchy(pcType);
        } else if (element instanceof IPolicyCmptType) {
            IPolicyCmptType pcType = (IPolicyCmptType)element;
            hierarchyContentProvider.setActualElement(pcType);
            hierarchy = TypeHierarchy.getTypeHierarchy(pcType);
        }
        setInputData(hierarchy);
    }
}
