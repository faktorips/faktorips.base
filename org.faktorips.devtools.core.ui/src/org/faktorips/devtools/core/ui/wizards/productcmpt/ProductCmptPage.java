/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.ControlPropertyBinding;
import org.faktorips.devtools.core.ui.controller.fields.StructuredViewerField;

/**
 *
 */
public class ProductCmptPage extends WizardPage {

    private final ResourceManager resourManager;

    private boolean canModifyRuntimeId;

    // product cmpt template
    private IProductCmpt sourceProductCmpt;

    private final NewProductCmptPMO pmo;

    private BindingContext bindingContext;

    public ProductCmptPage(NewProductCmptPMO pmo) {
        super("create product component");
        this.pmo = pmo;
        setTitle("Create new " + pmo.getSelectedBaseType());
        resourManager = new LocalResourceManager(JFaceResources.getResources());
        bindingContext = new BindingContext();
    }

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        Composite composite = toolkit.createGridComposite(parent, 1, false, false);

        ((GridData)toolkit.createLabel(composite, "Type:").getLayoutData()).horizontalSpan = 2;

        Composite typeSelection = toolkit.createGridComposite(composite, 2, true, false);
        final TableViewer listViewer = new TableViewer(typeSelection);
        listViewer.setContentProvider(new ArrayContentProvider());
        listViewer.setLabelProvider(new ProductCmptWizardTypeLabelProvider());
        GridData listLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        listLayoutData.heightHint = 100;
        listViewer.getControl().setLayoutData(listLayoutData);
        StructuredViewerField<IProductCmptType> listViewerField = new StructuredViewerField<IProductCmptType>(
                listViewer, IProductCmptType.class);
        bindingContext.bindContent(listViewerField, pmo, NewProductCmptPMO.PROPERTY_SELECTED_TYPE);
        bindingContext.add(new ControlPropertyBinding(listViewer.getControl(), pmo,
                NewProductCmptPMO.PROPERTY_SELECTED_BASE_TYPE, IProductCmptType.class) {

            @Override
            public void updateUiIfNotDisposed(String nameOfChangedProperty) {
                if (nameOfChangedProperty.equals(NewProductCmptPMO.PROPERTY_SELECTED_BASE_TYPE)) {
                    listViewer.setInput(pmo.getSubtypes());
                }
            }
        });

        Composite descriptionComposite = toolkit.createGridComposite(typeSelection, 1, false, false);

        final Label descriptionTitle = toolkit.createLabel(descriptionComposite, StringUtils.EMPTY);
        bindingContext.add(new ControlPropertyBinding(descriptionTitle, pmo, NewProductCmptPMO.PROPERTY_SELECTED_TYPE,
                IProductCmptType.class) {

            @Override
            public void updateUiIfNotDisposed(String nameOfChangedProperty) {
                if (pmo.getSelectedType() == null) {
                    descriptionTitle.setText(StringUtils.EMPTY);
                } else {
                    descriptionTitle.setText(IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(
                            pmo.getSelectedType()));
                }
                descriptionTitle.pack();
            }
        });

        Font font = descriptionTitle.getFont();
        FontData fontData = font.getFontData()[0];
        fontData.setHeight(fontData.getHeight() + 1);
        fontData.setStyle(SWT.BOLD);
        descriptionTitle.setFont(resourManager.createFont(FontDescriptor.createFrom(fontData)));

        GridData descriptionLayoutData = listLayoutData;
        descriptionLayoutData.heightHint = 50;
        final Label description = toolkit.createLabel(descriptionComposite, StringUtils.EMPTY, SWT.WRAP,
                descriptionLayoutData);

        bindingContext.add(new ControlPropertyBinding(description, pmo, NewProductCmptPMO.PROPERTY_SELECTED_BASE_TYPE,
                IProductCmptType.class) {

            @Override
            public void updateUiIfNotDisposed(String nameOfChangedProperty) {
                if (pmo.getSelectedType() == null) {
                    descriptionTitle.setText(StringUtils.EMPTY);
                } else {
                    description.setText(IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(
                            pmo.getSelectedType()));
                }
                description.update();
            }
        });

        setControl(composite);
    }

}
