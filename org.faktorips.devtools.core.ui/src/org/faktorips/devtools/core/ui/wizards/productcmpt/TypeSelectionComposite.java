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

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.StructuredViewerField;

class TypeSelectionComposite extends Composite {

    private final UIToolkit toolkit;
    private final ResourceManager resourManager;
    private Label title;
    private TableViewer listViewer;
    private StructuredViewerField<IProductCmptType> listViewerField;
    private Label descriptionTitle;
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
        listViewerField = new StructuredViewerField<IProductCmptType>(listViewer, IProductCmptType.class);

        Composite descriptionComposite = new Composite(this, SWT.BORDER);
        GridData descriptionCompositeData = new GridData(SWT.FILL, SWT.FILL, true, true);
        GridLayout descriptionCompositeLayout = new GridLayout();
        descriptionCompositeLayout.marginHeight = 3;
        descriptionCompositeLayout.marginWidth = 3;
        descriptionComposite.setLayoutData(descriptionCompositeData);
        descriptionComposite.setLayout(descriptionCompositeLayout);

        descriptionTitle = toolkit.createLabel(descriptionComposite, StringUtils.EMPTY);

        Font font = descriptionTitle.getFont();
        FontData fontData = font.getFontData()[0];
        fontData.setHeight(fontData.getHeight() + 1);
        fontData.setStyle(SWT.BOLD);
        descriptionTitle.setFont(resourManager.createFont(FontDescriptor.createFrom(fontData)));

        GridData descriptionLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        descriptionLayoutData.heightHint = 50;
        descriptionLayoutData.widthHint = 50;
        description = toolkit.createLabel(descriptionComposite, StringUtils.EMPTY, SWT.WRAP, descriptionLayoutData);
    }

    public void setTitle(String titleString) {
        title.setText(titleString);
    }

    public void setListInput(List<IProductCmptType> inputList) {
        listViewer.setInput(inputList);
    }

    public void addDoubleClickListener(IDoubleClickListener listener) {
        listViewer.addDoubleClickListener(listener);
    }

    public StructuredViewerField<IProductCmptType> getListViewerField() {
        return listViewerField;
    }

    public void setInput(IProductCmptType type) {
        if (type == null) {
            setDescriptionTitle(StringUtils.EMPTY);
            setDescription(StringUtils.EMPTY);
        } else {
            setDescriptionTitle(IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(type));
            setDescription(getDescription(type));
        }
    }

    private String getDescription(IProductCmptType type) {
        try {
            DescriptionFinder descriptionFinder = new DescriptionFinder(type.getIpsProject());
            descriptionFinder.start(type);
            return descriptionFinder.localizedDescription;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private void setDescriptionTitle(String title) {
        descriptionTitle.setText(title);
        descriptionTitle.pack();
    }

    private void setDescription(String descriptionString) {
        if (StringUtils.isEmpty(descriptionString) && StringUtils.isNotEmpty(descriptionTitle.getText())) {
            description.setText(Messages.TypeSelectionComposite_label_noDescriptionAvailable);
            description.setEnabled(false);
        } else {
            description.setText(descriptionString);
            description.setEnabled(true);
        }
    }

    private static class DescriptionFinder extends TypeHierarchyVisitor<IProductCmptType> {

        private String localizedDescription;

        public DescriptionFinder(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IProductCmptType currentType) throws CoreException {
            localizedDescription = IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(currentType);
            if (localizedDescription.isEmpty()) {
                return true;
            } else {
                return false;
            }
        }

    }

}