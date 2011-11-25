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
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
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
        ((GridData)title.getLayoutData()).horizontalSpan = 2;

        listViewer = new TableViewer(this);
        listViewer.setContentProvider(new ArrayContentProvider());
        listViewer.setLabelProvider(new ProductCmptWizardTypeLabelProvider());
        GridData listLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        listLayoutData.heightHint = 100;
        listViewer.getControl().setLayoutData(listLayoutData);
        listViewerField = new StructuredViewerField<IProductCmptType>(listViewer, IProductCmptType.class);

        Composite descriptionComposite = toolkit.createGridComposite(this, 1, false, false);

        descriptionTitle = toolkit.createLabel(descriptionComposite, StringUtils.EMPTY);

        Font font = descriptionTitle.getFont();
        FontData fontData = font.getFontData()[0];
        fontData.setHeight(fontData.getHeight() + 1);
        fontData.setStyle(SWT.BOLD);
        descriptionTitle.setFont(resourManager.createFont(FontDescriptor.createFrom(fontData)));

        GridData descriptionLayoutData = listLayoutData;
        descriptionLayoutData.heightHint = 50;
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

    public void setDescriptionTitle(String title) {
        descriptionTitle.setText(title);
        descriptionTitle.pack();
    }

    public void setDescription(String descriptionString) {
        description.setText(descriptionString);
    }

}