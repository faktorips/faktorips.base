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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.search.AbstractIpsSearchPage;
import org.faktorips.devtools.core.ui.search.product.conditions.ICondition;
import org.faktorips.devtools.core.ui.search.product.conditions.PolicyAttributeCondition;
import org.faktorips.devtools.core.ui.search.product.conditions.ProductAttributeCondition;

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

        Composite cmpProductComponentTypeChooser = toolkit.createGridComposite(baseComposite, 2, false, false);
        toolkit.createLabel(cmpProductComponentTypeChooser, Messages.ProductSearchPage_labelProductComponentType);

        IpsObjectRefControl ctrProductComponentTypeChooser = new IpsObjectRefControl(null,
                cmpProductComponentTypeChooser, toolkit, Messages.ProductSearchPage_labelProductComponentType,
                Messages.ProductSearchPage_labelChooseProductComponentType) {

            @Override
            protected IIpsSrcFile[] getIpsSrcFiles() throws CoreException {
                return getPresentationModel().getProductCmptTypesSrcFiles().toArray(new IIpsSrcFile[0]);
            }

            @Override
            protected void updateTextControlAfterDialogOK(List<IIpsSrcFile> ipsSrcFiles) {
                try {
                    getPresentationModel().setProductCmptType((IProductCmptType)ipsSrcFiles.get(0).getIpsObject());
                } catch (CoreException e) {
                    // TODO Exc handeln
                    throw new RuntimeException(e);
                }
                super.updateTextControlAfterDialogOK(ipsSrcFiles);
            }
        };
        ctrProductComponentTypeChooser.setEnabled(true);
        ctrProductComponentTypeChooser.getTextControl().setEditable(false);
        ctrProductComponentTypeChooser.getTextControl().setEnabled(false);

        // TODO qualname per binding an model binden

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
        // comp.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

        Button btnAddProduct = toolkit
                .createButton(
                        comp,
                        NLS.bind(Messages.ProductSearchPage_labelAddConditionButton,
                                new ProductAttributeCondition().getName()));

        // btnAddProduct.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

        btnAddProduct.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ICondition condition = new ProductAttributeCondition();
                addCondition(condition);
            }
        });
        // getBindingContext().bindEnabled(btnAddProduct, getModel(),
        // ProductSearchPresentationModel.PRODUCT_COMPONENT_TYPE_CHOSEN);

        Button btnAddPolicy = toolkit.createButton(comp,
                NLS.bind(Messages.ProductSearchPage_labelAddConditionButton, new PolicyAttributeCondition().getName()));

        // btnAddPolicy.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

        btnAddPolicy.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ICondition condition = new PolicyAttributeCondition();
                addCondition(condition);
            }
        });
        // getBindingContext().bindEnabled(btnAddPolicy, getModel(),
        // ProductSearchPresentationModel.PRODUCT_COMPONENT_TYPE_CHOSEN);

    }

    private void addCondition(ICondition condition) {
        try {
            if (condition.getSearchableElements(getPresentationModel().getProductCmptType()).isEmpty()) {
                MessageBox mb = new MessageBox(getShell());

                mb.setMessage(condition.getNoSearchableElementsMessage(getPresentationModel().getProductCmptType()));

                mb.open();
                return;
            }

            new ProductSearchConditionPresentationModel(getPresentationModel(), condition);

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
        return model;
    }

}
