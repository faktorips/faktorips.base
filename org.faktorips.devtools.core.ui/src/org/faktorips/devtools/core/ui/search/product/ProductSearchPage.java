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

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.search.AbstractIpsSearchPage;

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

        getBindingContext().bindContent(chooser, getPresentationModel(),
                ProductSearchPresentationModel.PRODUCT_COMPONENT_TYPE);

        Text txtSrcFilePatternText = createSrcFilePatternText(toolkit, baseComposite,
                Messages.ProductSearchPage_labelProductComponent);
        getBindingContext().bindEnabled(txtSrcFilePatternText, getPresentationModel(),
                ProductSearchPresentationModel.PRODUCT_COMPONENT_TYPE_CHOSEN);

        Group conditionsGroup = toolkit.createGroup(baseComposite, Messages.ProductSearchPage_labelSearchConditions);

        conditionTableViewer = createConditionTable(conditionsGroup);
        addConditionButtons(toolkit, conditionsGroup);

        getBindingContext().bindEnabled(conditionsGroup, getPresentationModel(),
                ProductSearchPresentationModel.PRODUCT_COMPONENT_TYPE_CHOSEN);

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

        // FIXME remove button
    }

    private void addCondition() {
        try {
            new ProductSearchConditionPresentationModel(getPresentationModel());

            conditionTableViewer.refresh();
        } catch (Exception e) {
            // TODO exception handling
            e.printStackTrace();
        }
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
        model.addPropertyChangeListener(createPropertyChangeListenerforValidity());
        return model;
    }

    private PropertyChangeListener createPropertyChangeListenerforValidity() {
        return new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                boolean valid = getPresentationModel().isValid();
                getContainer().setPerformActionEnabled(valid);
            }
        };
    }
}
