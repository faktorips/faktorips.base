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

package org.faktorips.devtools.core.ui.views.instanceexplorer;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.part.ViewPart;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsMetaClass;
import org.faktorips.devtools.core.model.IIpsMetaObject;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;
import org.faktorips.devtools.core.ui.views.IpsElementDragListener;
import org.faktorips.devtools.core.ui.views.IpsElementDropListener;
import org.w3c.dom.Element;

/**
 * <p>
 * The InstanceExplorer is a <code>ViewPart</code> for displaying the instance objects of a selected
 * class. For example there is a list of product components for a selected product component type.
 * </p>
 * <p>
 * The view uses a <code>TableViewer</code> to display the list of available objects. It is possible
 * to hide the instance objects for subclasses of the selected type.
 * </p>
 * 
 * @author Cornelius Dirmeier
 * 
 */

public class InstanceExplorer extends ViewPart implements IResourceChangeListener {

    /**
     * Extension id of this viewer extension.
     */
    public static final String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.instanceexplorer"; //$NON-NLS-1$

    /**
     * The filename of the image for this view
     */
    public static final String IMAGE = "InstanceExplorer.gif"; //$NON-NLS-1$

    private InstanceLabelProvider labelProvider;
    private TableViewer tableViewer;
    private InstanceContentProvider contentProvider = new InstanceContentProvider();
    private Composite panel;
    private ImageHyperlink selectedElementLink;

    private SubtypeSearchAction subtypeSearchAction;

    private Label errormsg;

    private Display display;

    private DecoratingLabelProvider decoratedLabelProvider;

    /**
     * The default constructor setup the listener and loads the default view.
     */
    public InstanceExplorer() {
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

        labelProvider = new InstanceLabelProvider();
        IDecoratorManager decoManager = IpsPlugin.getDefault().getWorkbench().getDecoratorManager();
        decoratedLabelProvider = new DecoratingLabelProvider(labelProvider, decoManager.getLabelDecorator());

        selectedElementLink = new ImageHyperlink(panel, SWT.FILL);
        selectedElementLink.setText(""); //$NON-NLS-1$
        selectedElementLink.setUnderlined(true);
        selectedElementLink.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        selectedElementLink.addMouseListener(new MouseListener() {

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                // Nothing to do
            }

            @Override
            public void mouseDown(MouseEvent e) {
                // Nothing to do
            }

            @Override
            public void mouseUp(MouseEvent e) {
                if (contentProvider.getActualElement() != null) {
                    IpsUIPlugin.getDefault().openEditor(contentProvider.getActualElement());
                }
            }

        });

        tableViewer = new TableViewer(panel);
        tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        tableViewer.setLabelProvider(decoratedLabelProvider);
        tableViewer.setContentProvider(contentProvider);
        tableViewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                OpenEditorAction action = new OpenEditorAction(tableViewer);
                action.openEditor();
            }
        });
        tableViewer.addDragSupport(DND.DROP_LINK, new Transfer[] { FileTransfer.getInstance() },
                new IpsElementDragListener(tableViewer));

        GridData errorLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        errorLayoutData.exclude = true;
        errormsg = new Label(panel, SWT.WRAP);
        errormsg.setLayoutData(errorLayoutData);

        getSite().setSelectionProvider(tableViewer);

        IActionBars actionBars = getViewSite().getActionBars();
        initToolBar(actionBars.getToolBarManager());

        showEmptyMessage();
    }

    private void initToolBar(IToolBarManager toolBarManager) {
        // subtype-search action

        subtypeSearchAction = new SubtypeSearchAction();
        subtypeSearchAction.setEnabled(false);
        toolBarManager.add(subtypeSearchAction);

        // refresh action
        Action refreshAction = new Action(Messages.InstanceExplorer_tooltipRefreshContents, IpsUIPlugin
                .getImageHandling().createImageDescriptor("Refresh.gif")) { //$NON-NLS-1$
            @Override
            public void run() {
                setInputData(contentProvider.getActualElement());
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

    /**
     * Loads the element in the editor if it is supported.
     * 
     * @param element The element to load into the editor
     * @throws CoreException if there is an exception with searching objects
     */
    public void showInstancesOf(IIpsObject element) throws CoreException {
        if (element != null && !element.getEnclosingResource().isAccessible()) {
            setInputData(null);
        } else if (element instanceof IIpsMetaObject) {
            IIpsMetaObject metaObject = (IIpsMetaObject)element;
            IIpsSrcFile metaClassSrcFile = metaObject.findMetaClassSrcFile(metaObject.getIpsProject());
            if (metaClassSrcFile != null) {
                setInputData((IIpsMetaClass)metaClassSrcFile.getIpsObject());
            } else {
                setInputData(new NotFoundMetaClass(metaObject));
            }
        } else if (element instanceof IIpsMetaClass) {
            setInputData((IIpsMetaClass)element);
        }
    }

    private void setInputData(final IIpsMetaClass element) {
        tableViewer.setInput(element);
        updateView();
    }

    private void showEmptyTableMessage(IIpsObject element) {
        String message = ""; //$NON-NLS-1$
        if (element instanceof IEnumType && ((IEnumType)element).isContainingValues()) {
            message = Messages.InstanceExplorer_enumContainsValues;
        } else {
            message = NLS.bind(Messages.InstanceExplorer_noInstancesFoundInProject, element.getIpsProject().getName());
            if (subtypeSearchAction.isEnabled() && !subtypeSearchAction.isChecked()) {
                message += Messages.InstanceExplorer_tryToSearchSubtypes;
            }
        }
        showErrorMessage(message);
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
        if (tableViewer != null && !tableViewer.getTable().isDisposed()) {
            tableViewer.getTable().setVisible(!mtSwitch.isMessage());
            ((GridData)tableViewer.getTable().getLayoutData()).exclude = mtSwitch.isMessage();
        }
        if (panel != null && !panel.isDisposed()) {
            panel.layout();
        }
    }

    @Override
    public void setFocus() {
        // nothing to do.
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        display.syncExec(new Runnable() {

            @Override
            public void run() {
                updateView();
            }
        });
    }

    private void updateView() {
        if (tableViewer != null && !tableViewer.getControl().isDisposed()) {
            Object element = tableViewer.getInput();
            if (element == null) {
                showEmptyMessage();
            } else if (element instanceof IIpsObject) {
                IIpsObject ipsObject = (IIpsObject)element;
                subtypeSearchAction.setEnabled(supportsSubtypes(ipsObject));
                if (tableViewer.getTable().getItemCount() == 0) {
                    showEmptyTableMessage(ipsObject);
                } else {
                    showMessgeOrTableView(MessageTableSwitch.TABLE);
                }
                if (selectedElementLink != null && !selectedElementLink.isDisposed()) {
                    selectedElementLink.setText(decoratedLabelProvider.getText(ipsObject));
                    selectedElementLink.setImage(decoratedLabelProvider.getImage(ipsObject));
                }
                tableViewer.refresh();
            }
        }
    }

    /**
     * Checks wether the argument supports sub type hierarchy or not
     * 
     * @param ipsObject the object to be checked
     * @return true if the parameter support subtypes
     */
    protected static boolean supportsSubtypes(IIpsObject ipsObject) {
        if (ipsObject instanceof IProductCmptType || ipsObject instanceof IProductCmpt) {
            return true;
        } else if (ipsObject instanceof IEnumType || ipsObject instanceof IEnumContent) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check whether this explorer supports this object or not. Null is also a supported type to
     * reset the editor.
     * 
     * @param object the object to test
     * @return true of the explorer supports this object
     */
    public static boolean supports(Object object) {
        if (object == null) {
            return true;
        }
        return object instanceof IIpsMetaClass || object instanceof IIpsMetaObject;
    }

    @Override
    public void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        getSite().setSelectionProvider(null);
        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.REFRESH.getId(), null);
        super.dispose();
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
                    showInstancesOf(((IIpsSrcFile)transferred[0]).getIpsObject());
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
                    if (InstanceExplorer.supports(selected)) {
                        event.detail = DND.DROP_LINK;
                    }
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }// ipsSrcFile.getIpsObjectType().newObject(ipsSrcFile);

            }
        }
    }

    private class SubtypeSearchAction extends Action {

        private static final String SUBTYPE_SEARCH_IMG = "InstanceExplorerSubtypeSearch.gif"; //$NON-NLS-1$

        public SubtypeSearchAction() {
            setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(SUBTYPE_SEARCH_IMG));
            setChecked(true);
            labelProvider.setSubTypeSearch(isChecked());
            contentProvider.setSubTypeSearch(isChecked());
        }

        @Override
        public int getStyle() {
            return IAction.AS_CHECK_BOX;
        }

        @Override
        public void run() {
            labelProvider.setSubTypeSearch(isChecked());
            contentProvider.setSubTypeSearch(isChecked());
            setInputData(contentProvider.getActualElement());
            super.run();
        }

        @Override
        public String getToolTipText() {
            return Messages.InstanceExplorer_tooltipSubtypeSearch;
        }

        @Override
        public String getDescription() {
            return getToolTipText();
        }

    }

    private enum MessageTableSwitch {
        MESSAGE,
        TABLE;

        public boolean isMessage() {
            return equals(MESSAGE);
        }

    }

    private static class NotFoundMetaClass extends IpsObject implements IIpsMetaClass {

        private final IIpsMetaObject metaObject;

        public NotFoundMetaClass(IIpsMetaObject metaObject) {
            this.metaObject = metaObject;
        }

        @Override
        public String getName() {
            return Messages.InstanceExplorer_noMetaClassFound;
        }

        @Override
        public String getQualifiedName() {
            return getName();
        }

        @Override
        protected void addPart(IIpsObjectPart part) {
            // Nothing to do
        }

        @Override
        public IIpsElement[] getChildren() {
            return new IIpsElement[] { metaObject };
        }

        @Override
        protected IIpsObjectPart newPart(Element xmlTag, String id) {
            return null;
        }

        @Override
        protected void reinitPartCollections() {
            // Nothing to do
        }

        @Override
        protected void removePart(IIpsObjectPart part) {
            // Nothing to do
        }

        @Override
        public IIpsSrcFile[] searchMetaObjectSrcFiles(boolean includeSubtypes) throws CoreException {
            return new IIpsSrcFile[] { metaObject.getIpsSrcFile() };
        }

        @Override
        public IpsObjectType getIpsObjectType() {
            return null;
        }

        @Override
        public IDependency[] dependsOn() throws CoreException {
            return new IDependency[0];
        }

        @Override
        public IIpsObjectPart newPart(Class<?> partType) {
            return null;
        }

    }

}
