/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.core.ui.LocalizedLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.StructuredViewerField;
import org.faktorips.devtools.core.ui.workbenchadapters.ProductCmptWorkbenchAdapter;

public class TypeSelectionComposite extends Composite {

    private final UIToolkit toolkit;
    private final ResourceManager resourManager;
    private Label title;
    private TableViewer listViewer;
    private StructuredViewerField<IIpsObject> listViewerField;
    private Label description;

    public TypeSelectionComposite(Composite parent, UIToolkit toolkit) {
        super(parent, SWT.NONE);
        this.toolkit = toolkit;
        this.resourManager = new LocalResourceManager(JFaceResources.getResources());

        setLayoutAndLayoutData();

        createControls();
        addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                resourManager.dispose();
            }
        });
    }

    private void setLayoutAndLayoutData() {
        GridLayout layout = new GridLayout(2, true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 10;
        setLayout(layout);

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        setLayoutData(gridData);
    }

    private void createControls() {
        title = toolkit.createLabel(this, StringUtils.EMPTY);
        toolkit.createLabel(this, Messages.TypeSelectionComposite_label_description);

        listViewer = new TableViewer(this);
        listViewer.setContentProvider(new ArrayContentProvider());
        listViewer.setLabelProvider(new ProductCmptWizardTypeLabelProvider());
        GridData listLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        listLayoutData.heightHint = 50;
        listLayoutData.widthHint = 50;
        listViewer.getControl().setLayoutData(listLayoutData);
        listViewerField = new StructuredViewerField<IIpsObject>(listViewer, IIpsObject.class);

        Composite descriptionComposite = new Composite(this, SWT.BORDER);
        GridData descriptionCompositeData = new GridData(SWT.FILL, SWT.FILL, true, true);
        GridLayout descriptionCompositeLayout = new GridLayout();
        descriptionCompositeLayout.marginHeight = 3;
        descriptionCompositeLayout.marginWidth = 3;
        descriptionComposite.setLayoutData(descriptionCompositeData);
        descriptionComposite.setLayout(descriptionCompositeLayout);

        GridData descriptionLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        descriptionLayoutData.heightHint = 50;
        descriptionLayoutData.widthHint = 50;
        description = toolkit.createLabel(descriptionComposite, StringUtils.EMPTY, SWT.WRAP, descriptionLayoutData);
    }

    public void setTitle(String titleString) {
        title.setText(titleString);
    }

    public void setListInput(List<? extends IIpsObject> inputList) {
        listViewer.setInput(inputList);
    }

    public void addDoubleClickListener(IDoubleClickListener listener) {
        listViewer.addDoubleClickListener(listener);
    }

    public StructuredViewerField<IIpsObject> getListViewerField() {
        return listViewerField;
    }

    public void setSelection(IIpsObject type) {
        if (type == null) {
            setDescription(StringUtils.EMPTY);
        } else {
            setDescription(getDescription(type));
        }
    }

    private String getDescription(IDescribedElement element) {
        try {
            DescriptionFinder descriptionFinder = new DescriptionFinder(element.getIpsProject());
            descriptionFinder.start(element);
            return descriptionFinder.localizedDescription;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private void setDescription(String descriptionString) {
        if (StringUtils.isEmpty(descriptionString)) {
            description.setText(Messages.TypeSelectionComposite_label_noDescriptionAvailable);
            description.setEnabled(false);
        } else {
            description.setText(descriptionString);
            description.setEnabled(true);
        }
    }

    private static class DescriptionFinder extends TypeHierarchyVisitor<IType> {

        private String localizedDescription;

        public DescriptionFinder(IIpsProject ipsProject) {
            super(ipsProject);
        }

        public void start(IDescribedElement element) throws CoreException {
            if (element instanceof IType) {
                IType type = (IType)element;
                super.start(type);
            } else {
                setDescription(element);
            }
        }

        @Override
        protected boolean visit(IType currentType) throws CoreException {
            setDescription(currentType);
            if (localizedDescription.isEmpty()) {
                return true;
            } else {
                return false;
            }
        }

        protected void setDescription(IDescribedElement currentType) {
            localizedDescription = IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(currentType);
        }

    }

    public static class ProductCmptWizardTypeLabelProvider extends LocalizedLabelProvider {

        private final ProductCmptWorkbenchAdapter productCmptWorkbenchAdapter = new ProductCmptWorkbenchAdapter();

        @Override
        public Image getImage(Object element) {
            if (element instanceof IProductCmptType) {
                IProductCmptType productCmptType = (IProductCmptType)element;
                ImageDescriptor descriptorForInstancesOf = productCmptWorkbenchAdapter
                        .getImageDescriptorForInstancesOf(productCmptType);
                return JFaceResources.getResources().createImage(descriptorForInstancesOf);
            }

            return super.getImage(element);
        }

    }

}