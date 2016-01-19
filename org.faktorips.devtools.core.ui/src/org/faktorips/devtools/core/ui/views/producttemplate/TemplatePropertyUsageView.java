/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.views.producttemplate;

import com.google.common.base.Optional;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsMenuId;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.MenuCleaner;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.editors.SelectionProviderIntermediate;
import org.faktorips.devtools.core.ui.editors.productcmpt.SimpleOpenIpsObjectPartAction;
import org.faktorips.devtools.core.ui.util.TypedSelection;

public class TemplatePropertyUsageView {

    public static final String VIEW_ID = "org.faktorips.devtools.core.ui.views.producttemplate.TemplatePropertyUsageView"; //$NON-NLS-1$

    private final BindingContext bindingContext = new BindingContext();
    private TemplatePropertyUsagePmo usagePmo;

    private TreeViewer leftTreeViewer;
    private TreeViewer rightTreeViewer;

    private IViewSite site;

    private SelectionProviderIntermediate selectionProviderDispatcher;

    /*
     * Eclipse 4 constructor. Requires additional libraries.
     */
    // @Inject
    public TemplatePropertyUsageView(Composite parent, IViewSite site) {
        this.site = site;
        createPartControl(parent);
        setUpTrees();
        setUpSelectionProvider();
        buildContextMenu();
        setUpToolbar();
    }

    /**
     * Sets the property value to display template information for. Refreshes this view.
     */
    public void setPropertyValue(IPropertyValue propertyValue) {
        disposePmo();
        usagePmo = new TemplatePropertyUsagePmo(propertyValue);
        leftTreeViewer.setInput(usagePmo);
        rightTreeViewer.setInput(usagePmo);
        rightTreeViewer.expandAll();
    }

    private void disposePmo() {
        if (usagePmo != null) {
            usagePmo.dispose();
        }
    }

    private void createPartControl(Composite parent) {
        parent.setLayout(createDefaultLayout());
        SashForm sash = new SashForm(parent, SWT.HORIZONTAL);
        sash.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        sash.setLayout(createDefaultLayout());

        Composite leftSide = new Composite(sash, SWT.NONE);
        leftSide.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        leftSide.setLayout(createTreeCompositeLayout());
        Composite rightSide = new Composite(sash, SWT.NONE);
        rightSide.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        rightSide.setLayout(createTreeCompositeLayout());

        new Label(leftSide, SWT.NONE).setText(Messages.TemplatePropertyUsageView_SameValue_label);
        leftTreeViewer = new TreeViewer(leftSide, SWT.BORDER);
        leftTreeViewer.getTree().setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        new Label(rightSide, SWT.NONE).setText(Messages.TemplatePropertyUsageView_DifferingValues_Label);
        rightTreeViewer = new TreeViewer(rightSide, SWT.BORDER);
        rightTreeViewer.getTree().setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
    }

    private GridLayout createDefaultLayout() {
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        return layout;
    }

    private GridLayout createTreeCompositeLayout() {
        GridLayout layout = createDefaultLayout();
        layout.marginTop = 2;
        return layout;
    }

    private void setUpTrees() {
        leftTreeViewer.addDoubleClickListener(new OpenProductCmptEditorListener());
        leftTreeViewer.setLabelProvider(new TemplatePropertyUsageLabelProvider());
        leftTreeViewer.setContentProvider(new InheritedPropertyValueContentProvider());

        rightTreeViewer.addDoubleClickListener(new OpenProductCmptEditorListener());
        rightTreeViewer.setLabelProvider(new TemplatePropertyUsageLabelProvider());
        rightTreeViewer.setContentProvider(new DefinedValuesContentProvider());
    }

    void setUpToolbar() {
        final String toolTip = Messages.TemplatePropertyUsageView_toolTipRefreshContents;
        final ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor("Refresh.gif"); //$NON-NLS-1$
        Action refreshAction = new Action(toolTip, imageDescriptor) {
            @Override
            public void run() {
                refresh();
            }

            @Override
            public String getToolTipText() {
                return toolTip;
            }
        };

        site.getActionBars().setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
        IWorkbenchAction retargetAction = ActionFactory.REFRESH.create(site.getWorkbenchWindow());
        retargetAction.setToolTipText(toolTip);
        retargetAction.setImageDescriptor(imageDescriptor);
        site.getActionBars().getToolBarManager().add(retargetAction);
    }

    private void refresh() {
        if (usagePmo != null) {
            usagePmo.partHasChanged();
            leftTreeViewer.refresh();
            rightTreeViewer.refresh();
        }
    }

    private void setUpSelectionProvider() {
        selectionProviderDispatcher = new SelectionProviderIntermediate();
        selectionProviderDispatcher.registerListenersFor(leftTreeViewer);
        selectionProviderDispatcher.registerListenersFor(rightTreeViewer);
        site.setSelectionProvider(selectionProviderDispatcher);
    }

    private void buildContextMenu() {
        buildContextMenu(leftTreeViewer);
        buildContextMenu(rightTreeViewer);
    }

    protected void buildContextMenu(TreeViewer treeViewer) {
        MenuManager menuManager = new MenuManager();

        menuManager.add(new GroupMarker("open")); //$NON-NLS-1$
        IpsMenuId.GROUP_NAVIGATE.addSeparator(menuManager);

        site.registerContextMenu(VIEW_ID, menuManager, treeViewer);

        MenuCleaner menuCleaner = new MenuCleaner();
        menuCleaner.setWhiteListMode(true);
        menuCleaner.addFilteredPrefix(MenuCleaner.WHITE_LIST_IPS_PREFIX);
        menuManager.addMenuListener(menuCleaner);

        Menu treePopup = menuManager.createContextMenu(treeViewer.getControl());
        treeViewer.getTree().setMenu(treePopup);
    }

    // @Focus
    public void setFocus() {
        // to do
    }

    // @PreDestroy
    public void dispose() {
        bindingContext.dispose();
    }

    private static class OpenProductCmptEditorListener implements IDoubleClickListener {

        @Override
        public void doubleClick(DoubleClickEvent event) {
            Optional<IIpsObjectPartContainer> selectedElement = TypedSelection.singleElement(
                    IIpsObjectPartContainer.class, event.getSelection());
            if (selectedElement.isPresent()) {
                new SimpleOpenIpsObjectPartAction(selectedElement.get(), "").run(); //$NON-NLS-1$
            }

        }
    }

    private static class TemplatePropertyUsageLabelProvider extends DefaultLabelProvider {

        @Override
        public String getText(Object element) {
            if (element instanceof IPropertyValue) {
                IPropertyValueContainer propertyValueContainer = ((IPropertyValue)element).getPropertyValueContainer();
                if (propertyValueContainer instanceof IProductCmptGeneration) {
                    IProductCmptGeneration generation = (IProductCmptGeneration)propertyValueContainer;
                    return super.getText(generation.getProductCmpt()) + " (" + super.getText(generation) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    return super.getText(propertyValueContainer);
                }
            } else if (element instanceof DefinedValuesContentProvider.ValueViewItem) {
                DefinedValuesContentProvider.ValueViewItem viewItem = (DefinedValuesContentProvider.ValueViewItem)element;
                return viewItem.getText();
            }
            return super.getText(element);
        }

        @Override
        public Image getImage(Object element) {
            if (element instanceof IPropertyValue) {
                IPropertyValue propertyValue = (IPropertyValue)element;
                return super.getImage(propertyValue.getPropertyValueContainer().getProductCmpt());
            }
            return super.getImage(element);
        }

    }

}
