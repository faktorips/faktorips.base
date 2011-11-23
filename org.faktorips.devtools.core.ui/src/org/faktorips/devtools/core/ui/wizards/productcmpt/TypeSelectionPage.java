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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.ControlPropertyBinding;
import org.faktorips.devtools.core.ui.controller.fields.StructuredViewerField;
import org.faktorips.devtools.core.ui.controls.IpsProjectRefControl;

public class TypeSelectionPage extends WizardPage {

    private final ResourceManager resourManager;
    private final NewProductCmptPMO pmo;
    private final BindingContext bindingContext;
    private ListInputUpdater listInputUpdater;

    public TypeSelectionPage(NewProductCmptPMO pmo) {
        super("New Product Component");
        this.pmo = pmo;
        setTitle("Which kind of product component do you want to create?");
        resourManager = new LocalResourceManager(JFaceResources.getResources());
        bindingContext = new BindingContext();
        pmo.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (isCurrentPage()) {
                    getContainer().updateButtons();
                }
            }
        });
    }

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        Composite composite = toolkit.createGridComposite(parent, 1, false, false);
        Composite twoColumnComposite = toolkit.createLabelEditColumnComposite(composite);

        // Select Project
        toolkit.createLabel(twoColumnComposite, "Project:");
        IpsProjectRefControl ipsProjectRefControl = toolkit.createIpsProjectRefControl(twoColumnComposite);
        bindingContext.bindContent(ipsProjectRefControl, pmo, NewProductCmptPMO.PROPERTY_IPSPROJECT);

        toolkit.createLabel(composite, "Type:");

        Composite typeSelection = toolkit.createGridComposite(composite, 2, true, false);
        TableViewer listViewer = new TableViewer(typeSelection);
        listViewer.setContentProvider(new ArrayContentProvider());
        listViewer.setLabelProvider(new ProductCmptWizardTypeLabelProvider());
        GridData listLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        listLayoutData.heightHint = 100;
        listViewer.getControl().setLayoutData(listLayoutData);
        StructuredViewerField<IProductCmptType> listViewerField = new StructuredViewerField<IProductCmptType>(
                listViewer, IProductCmptType.class);
        bindingContext.bindContent(listViewerField, pmo, NewProductCmptPMO.PROPERTY_SELECTED_BASE_TYPE);
        listInputUpdater = new ListInputUpdater(listViewer, pmo);
        listInputUpdater.updateListViewer();
        pmo.addPropertyChangeListener(listInputUpdater);
        listViewer.addDoubleClickListener(new DoubleClickListener(this));

        Composite descriptionComposite = toolkit.createGridComposite(typeSelection, 1, false, false);

        final Label descriptionTitle = toolkit.createLabel(descriptionComposite, StringUtils.EMPTY);
        bindingContext.add(new ControlPropertyBinding(descriptionTitle, pmo,
                NewProductCmptPMO.PROPERTY_SELECTED_BASE_TYPE, IProductCmptType.class) {

            @Override
            public void updateUiIfNotDisposed(String nameOfChangedProperty) {
                if (pmo.getSelectedBaseType() == null) {
                    descriptionTitle.setText(StringUtils.EMPTY);
                } else {
                    descriptionTitle.setText(IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(
                            pmo.getSelectedBaseType()));
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
                if (pmo.getSelectedBaseType() == null) {
                    descriptionTitle.setText(StringUtils.EMPTY);
                } else {
                    description.setText(IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(
                            pmo.getSelectedBaseType()));
                }
                description.update();
            }
        });

        // TODO Auto-generated method stub
        bindingContext.updateUI();
        setControl(composite);
    }

    @Override
    public boolean isPageComplete() {
        return pmo.getIpsProject() != null && pmo.getSelectedBaseType() != null;
    }

    @Override
    public void dispose() {
        super.dispose();
        resourManager.dispose();
        bindingContext.dispose();
        pmo.removePropertyChangeListener(listInputUpdater);
    }

    private static class ListInputUpdater implements PropertyChangeListener {

        private final Viewer listViewer;
        private final NewProductCmptPMO pmo;

        public ListInputUpdater(Viewer listViewer, NewProductCmptPMO pmo) {
            this.listViewer = listViewer;
            this.pmo = pmo;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(NewProductCmptPMO.PROPERTY_IPSPROJECT)) {
                updateListViewer();
            }
        }

        void updateListViewer() {
            listViewer.setInput(pmo.getBaseTypes());
        }

    }

    private static class DoubleClickListener implements IDoubleClickListener {

        private final TypeSelectionPage page;

        public DoubleClickListener(TypeSelectionPage page) {
            this.page = page;
        }

        @Override
        public void doubleClick(DoubleClickEvent event) {
            page.getWizard().getContainer().showPage(page.getNextPage());

        }
    }

}
