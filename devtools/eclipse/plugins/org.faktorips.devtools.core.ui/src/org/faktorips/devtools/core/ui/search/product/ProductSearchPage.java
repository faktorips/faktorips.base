/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product;

import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.search.AbstractIpsSearchPage;
import org.faktorips.devtools.core.ui.search.product.conditions.table.ProductSearchConditionPresentationModel;
import org.faktorips.devtools.core.ui.search.product.conditions.table.ProductSearchConditionsTableViewerCreator;

/**
 * 
 * DialogPage for the Faktor-IPS Product Search
 * 
 * @author dicker
 */
public class ProductSearchPage extends AbstractIpsSearchPage<ProductSearchPresentationModel> {

    private static final String PRODUCT_SEARCH_PAGE_NAME = "ProductSearchPage"; //$NON-NLS-1$
    private static final String PRODUCT_SEARCH_DATA = "ProductSearchData"; //$NON-NLS-1$

    private Composite baseComposite;
    private TableViewer conditionTableViewer;

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        readDialogSettings();
        loadLastSearchIfAvailable();

        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutData.minimumHeight = 440;

        baseComposite = toolkit.createGridComposite(parent, 1, false, true);
        baseComposite.setLayoutData(layoutData);

        createProductComponentTypeChoser(toolkit);

        toolkit.createVerticalSpacer(baseComposite, 10);

        createSrcFilePatternText(toolkit, baseComposite, Messages.ProductSearchPage_labelProductComponent);

        toolkit.createVerticalSpacer(baseComposite, 20);

        createConditionTable(baseComposite, toolkit);

        setControl(baseComposite);

        getBindingContext().updateUI();
        conditionTableViewer.refresh();
    }

    private void loadLastSearchIfAvailable() {
        if (!getPreviousSearchData().isEmpty()) {
            getPresentationModel().read(getPreviousSearchData().get(0));
        }
    }

    private void createProductComponentTypeChoser(UIToolkit toolkit) {
        Composite cmpProductComponentTypeChooser = toolkit.createGridComposite(baseComposite, 3, false, false);
        toolkit.createLabel(cmpProductComponentTypeChooser, Messages.ProductSearchPage_labelProductComponentType);

        ProductComponentTypeField chooser = new ProductComponentTypeField(toolkit, cmpProductComponentTypeChooser);
        chooser.getTextControl().setEnabled(false);

        getBindingContext().bindContent(chooser, getPresentationModel(),
                ProductSearchPresentationModel.PRODUCT_COMPONENT_TYPE);
    }

    @Override
    protected Text createSrcFilePatternText(UIToolkit toolkit, Composite composite, String srcFilePatternTextLabel) {
        Text txtSrcFilePatternText = super.createSrcFilePatternText(toolkit, composite, srcFilePatternTextLabel);

        getBindingContext().bindEnabled(txtSrcFilePatternText, getPresentationModel(),
                ProductSearchPresentationModel.PRODUCT_COMPONENT_TYPE_CHOSEN);

        return txtSrcFilePatternText;
    }

    private void createConditionTable(Composite composite, UIToolkit toolkit) {
        Composite comp = toolkit.createGridComposite(composite, 3, false, false);

        Label label = toolkit.createLabel(comp, Messages.ProductSearchPage_labelSearchConditions);
        label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

        addConditionButtons(comp);

        conditionTableViewer = new ProductSearchConditionsTableViewerCreator().createTableViewer(
                getPresentationModel(), composite);
    }

    private void addConditionButtons(Composite comp) {

        Button btnAddCondition = new Button(comp, SWT.PUSH);
        btnAddCondition.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

        btnAddCondition.setToolTipText(Messages.ProductSearchPage_labelAddConditionButton);
        btnAddCondition.setImage(IpsUIPlugin.getImageHandling().getSharedImage("Add.gif", true)); //$NON-NLS-1$

        btnAddCondition.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                addCondition();
            }
        });
        getBindingContext().bindEnabled(btnAddCondition, getPresentationModel(),
                ProductSearchPresentationModel.CONDITION_TYPE_AVAILABLE);
        btnAddCondition.setEnabled(false);

        Button btnRemoveCondition = new Button(comp, SWT.PUSH);
        btnRemoveCondition.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

        btnRemoveCondition.setToolTipText(Messages.ProductSearchPage_labelRemoveConditionButton);
        btnRemoveCondition.setImage(IpsUIPlugin.getImageHandling().getSharedImage("elcl16/trash.gif", true)); //$NON-NLS-1$

        btnRemoveCondition.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                removeConditions();
            }

        });
        getBindingContext().bindEnabled(btnRemoveCondition, getPresentationModel(),
                ProductSearchPresentationModel.CONDITION_DEFINED);
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
            if (object instanceof ProductSearchConditionPresentationModel productSearchConditionPresentationModel) {
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
        ProductSearchPresentationModel searchPMO = new ProductSearchPresentationModel();
        searchPMO.addPropertyChangeListener(createPropertyChangeListenerForValidity());
        searchPMO.addPropertyChangeListener(createPropertyChangeListenerForTable());
        return searchPMO;
    }

    private PropertyChangeListener createPropertyChangeListenerForValidity() {
        return $ -> {
            boolean valid = getPresentationModel().isValid();
            getSearchPageContainer().setPerformActionEnabled(valid);
        };
    }

    private PropertyChangeListener createPropertyChangeListenerForTable() {
        return evt -> {
            if (ProductSearchPresentationModel.PRODUCT_COMPONENT_TYPE.equals(evt.getPropertyName())) {
                if (conditionTableViewer != null) {
                    conditionTableViewer.refresh();
                }
            }
        };

    }

    @Override
    protected ISearchQuery createSearchQuery() {
        return new ProductSearchQuery(getPresentationModel());
    }
}
