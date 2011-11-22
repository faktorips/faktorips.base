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
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.LocalizedLabelProvider;
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

        ((GridData)toolkit.createLabel(twoColumnComposite, "Type:").getLayoutData()).horizontalSpan = 2;

        Composite typeSelection = toolkit.createGridComposite(composite, 2, true, false);
        ListViewer listViewer = new ListViewer(typeSelection);
        listViewer.setContentProvider(new ArrayContentProvider());
        listViewer.setLabelProvider(new LocalizedLabelProvider());
        listViewer.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        StructuredViewerField<IProductCmptType> listViewerField = new StructuredViewerField<IProductCmptType>(
                listViewer, IProductCmptType.class);
        bindingContext.bindContent(listViewerField, pmo, NewProductCmptPMO.PROPERTY_SELECTED_BASE_TYPE);
        listInputUpdater = new ListInputUpdater(listViewer, pmo);
        listInputUpdater.updateListViewer();
        pmo.addPropertyChangeListener(listInputUpdater);

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

        final Text description = new Text(parent, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
        toolkit.createMultilineText(descriptionComposite);
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
    public void dispose() {
        super.dispose();
        resourManager.dispose();
        bindingContext.dispose();
        pmo.removePropertyChangeListener(listInputUpdater);
    }

    private static class ListInputUpdater implements PropertyChangeListener {

        private final ListViewer listViewer;
        private final NewProductCmptPMO pmo;

        public ListInputUpdater(ListViewer listViewer, NewProductCmptPMO pmo) {
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

}
