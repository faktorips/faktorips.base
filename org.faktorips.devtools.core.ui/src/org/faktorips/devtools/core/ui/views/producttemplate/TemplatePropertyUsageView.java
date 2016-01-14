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

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.Messages;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.editors.productcmpt.SimpleOpenIpsObjectPartAction;
import org.faktorips.devtools.core.ui.util.TypedSelection;

public class TemplatePropertyUsageView {

    public static final String VIEW_ID = "org.faktorips.devtools.core.ui.views.producttemplate.TemplatePropertyUsageView"; //$NON-NLS-1$

    private final BindingContext bindingContext = new BindingContext();
    private TemplatePropertyUsagePmo usagePmo;

    private TreeViewer leftTreeViewer;
    private TreeViewer rightTreeViewer;

    /*
     * Eclipse 4 constructor. Requires additional libraries.
     */
    // @Inject
    public TemplatePropertyUsageView(Composite parent) {
        createPartControl(parent);
        setUpTrees();
    }

    /**
     * Sets the property value to display template information for. Refreshes this view.
     */
    public void setPropertyValue(IPropertyValue propertyValue) {
        disposePmo();
        usagePmo = new TemplatePropertyUsagePmo(propertyValue, IpsUIPlugin.getDefault().getDefaultValidityDate());
        leftTreeViewer.setInput(usagePmo);
        rightTreeViewer.setInput(usagePmo);
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
        leftTreeViewer.setContentProvider(new ProdCmptsWithSameValueProvider());
        leftTreeViewer.setLabelProvider(new DefaultLabelProvider());
        leftTreeViewer.addDoubleClickListener(new OpenProductCmptEditorListener());

        // TODO set up rightTreeViewer correctly, this is just so that the view works
        rightTreeViewer.setContentProvider(new ProdCmptsWithSameValueProvider());
        rightTreeViewer.setLabelProvider(new DefaultLabelProvider());
        rightTreeViewer.addDoubleClickListener(new OpenProductCmptEditorListener());
    }

    // @Focus
    public void setFocus() {
        // to do
    }

    // @PreDestroy
    public void dispose() {
        // to do
        bindingContext.dispose();
    }

    public static class ProdCmptsWithSameValueProvider implements ITreeContentProvider {
        private TemplatePropertyUsagePmo pmo;

        @Override
        public Object[] getElements(Object inputElement) {
            if (pmo != null) {
                return pmo.getInheritingProductCmpts().toArray();
            } else {
                return new Object[0];
            }
        }

        @Override
        public void dispose() {
            // nothing to do
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            if (newInput instanceof TemplatePropertyUsagePmo) {
                pmo = (TemplatePropertyUsagePmo)newInput;
            }
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            return new Object[0];
        }

        @Override
        public Object getParent(Object element) {
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            return false;
        }

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

}
