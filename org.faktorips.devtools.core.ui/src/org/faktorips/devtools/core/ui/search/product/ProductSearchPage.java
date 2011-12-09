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

package org.faktorips.devtools.core.ui.search.product;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.search.AbstractIpsSearchPage;
import org.faktorips.devtools.core.ui.search.product.conditions.table.ProductSearchConditionPresentationModel;
import org.faktorips.devtools.core.ui.search.product.conditions.table.ProductSearchConditionsTableViewerProvider;

public class ProductSearchPage extends AbstractIpsSearchPage<ProductSearchPresentationModel> {

    private static final String PRODUCT_SEARCH_PAGE_NAME = "ProductSearchPage"; //$NON-NLS-1$
    private static final String PRODUCT_SEARCH_DATA = "ProductSearchData"; //$NON-NLS-1$

    private Composite baseComposite;
    private TableViewer conditionTableViewer;

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        readConfiguration();

        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutData.minimumHeight = 440;
        baseComposite = toolkit.createGridComposite(parent, 1, false, true);
        baseComposite.setLayoutData(layoutData);

        Composite cmpProductComponentTypeChooser = toolkit.createGridComposite(baseComposite, 3, false, false);
        toolkit.createLabel(cmpProductComponentTypeChooser, Messages.ProductSearchPage_labelProductComponentType);

        Text text = toolkit.createText(cmpProductComponentTypeChooser);
        Button button = toolkit.createButton(cmpProductComponentTypeChooser,
                Messages.ProductSearchPage_labelChooseProductComponentType);
        ProductComponentTypeField chooser = new ProductComponentTypeField(text, button);
        text.setEnabled(false);

        getBindingContext().bindContent(chooser, getPresentationModel(),
                ProductSearchPresentationModel.PRODUCT_COMPONENT_TYPE);

        toolkit.createVerticalSpacer(baseComposite, 10);

        Text txtSrcFilePatternText = createSrcFilePatternText(toolkit, baseComposite,
                Messages.ProductSearchPage_labelProductComponent);
        getBindingContext().bindEnabled(txtSrcFilePatternText, getPresentationModel(),
                ProductSearchPresentationModel.PRODUCT_COMPONENT_TYPE_CHOSEN);

        toolkit.createVerticalSpacer(baseComposite, 20);

        toolkit.createLabel(baseComposite, Messages.ProductSearchPage_labelSearchConditions);
        conditionTableViewer = createConditionTable(baseComposite);
        addConditionButtons(toolkit, baseComposite);

        getPresentationModel().initDefaultSearchValues();

        setControl(baseComposite);

    }

    private TableViewer createConditionTable(Composite composite) {
        ProductSearchConditionsTableViewerProvider productSearchConditionsTableViewerProvider = new ProductSearchConditionsTableViewerProvider(
                getPresentationModel(), composite);

        return productSearchConditionsTableViewerProvider.getTableViewer();
    }

    private void addConditionButtons(UIToolkit toolkit, Composite composite) {

        Composite comp = toolkit.createComposite(composite);

        comp.setLayout(new FillLayout(SWT.HORIZONTAL));

        Button btnAddCondition = toolkit.createButton(comp, Messages.ProductSearchPage_labelAddConditionButton);

        btnAddCondition.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                addCondition();
            }
        });
        getBindingContext().bindEnabled(btnAddCondition, getPresentationModel(),
                ProductSearchPresentationModel.PRODUCT_COMPONENT_TYPE_CHOSEN);
        btnAddCondition.setEnabled(false);

        Button btnRemoveCondition = toolkit.createButton(comp, Messages.ProductSearchPage_labelRemoveConditionButton);

        btnRemoveCondition.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                removeConditions();
            }

        });
        getBindingContext().bindEnabled(btnRemoveCondition, getPresentationModel(),
                ProductSearchPresentationModel.PRODUCT_COMPONENT_TYPE_CHOSEN);
        btnRemoveCondition.setEnabled(false);
    }

    private void addCondition() {
        getPresentationModel().createProductSearchConditionPresentationModel();
        conditionTableViewer.refresh();
    }

    private void removeConditions() {
        ISelection selection = conditionTableViewer.getSelection();
        IStructuredSelection structuredSelection = (IStructuredSelection)selection;

        List<?> list = structuredSelection.toList();
        for (Object object : list) {
            if (object instanceof ProductSearchConditionPresentationModel) {
                ProductSearchConditionPresentationModel productSearchConditionPresentationModel = (ProductSearchConditionPresentationModel)object;
                getPresentationModel().removeProductSearchConditionPresentationModels(
                        productSearchConditionPresentationModel);
            }
        }
        conditionTableViewer.refresh();
    }

    @Override
    protected String getDialogSettingPrefix() {
        return PRODUCT_SEARCH_DATA;
    }

    @Override
    protected String getSearchPageName() {
        return PRODUCT_SEARCH_PAGE_NAME;
    }

    @Override
    protected ProductSearchPresentationModel createPresentationModel() {
        ProductSearchPresentationModel model = new ProductSearchPresentationModel();
        model.initDefaultSearchValues();
        model.addPropertyChangeListener(createPropertyChangeListenerForValidity());
        model.addPropertyChangeListener(createPropertyChangeListenerForTable());
        return model;
    }

    private PropertyChangeListener createPropertyChangeListenerForValidity() {
        return new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                boolean valid = getPresentationModel().isValid();
                getContainer().setPerformActionEnabled(valid);
            }
        };
    }

    private PropertyChangeListener createPropertyChangeListenerForTable() {
        return new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (ProductSearchPresentationModel.PRODUCT_COMPONENT_TYPE.equals(evt.getPropertyName())) {
                    conditionTableViewer.refresh();
                }
            }
        };

    }
}
