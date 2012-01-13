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
import org.faktorips.devtools.core.ui.search.product.conditions.table.ProductSearchConditionsTableViewerProvider;

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

        ProductSearchConditionsTableViewerProvider productSearchConditionsTableViewerProvider = new ProductSearchConditionsTableViewerProvider(
                getPresentationModel(), composite);

        conditionTableViewer = productSearchConditionsTableViewerProvider.getTableViewer();
    }

    private void addConditionButtons(Composite comp) {

        Button btnAddCondition = new Button(comp, SWT.PUSH);
        btnAddCondition.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

        btnAddCondition.setToolTipText(Messages.ProductSearchPage_labelAddConditionButton);
        btnAddCondition.setImage(IpsUIPlugin.getImageHandling().getSharedImage("Add.gif", true));

        btnAddCondition.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                addCondition();
            }
        });
        getBindingContext().bindEnabled(btnAddCondition, getPresentationModel(),
                ProductSearchPresentationModel.PRODUCT_COMPONENT_TYPE_CHOSEN);
        btnAddCondition.setEnabled(false);

        Button btnRemoveCondition = new Button(comp, SWT.PUSH);
        btnRemoveCondition.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

        btnRemoveCondition.setToolTipText(Messages.ProductSearchPage_labelRemoveConditionButton);
        btnRemoveCondition.setImage(IpsUIPlugin.getImageHandling().getSharedImage("trash.gif", true));

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
                getSearchPageContainer().setPerformActionEnabled(valid);
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

    @Override
    protected ISearchQuery createSearchQuery() {
        return new ProductSearchQuery(getPresentationModel());
    }
}
