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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.search.AbstractIpsSearchPage;
import org.faktorips.devtools.core.ui.search.product.conditions.ICondition;
import org.faktorips.devtools.core.ui.search.product.conditions.ProductAttributeCondition;

public class ProductSearchPage extends AbstractIpsSearchPage<ProductSearchPresentationModel> {

    private static final String PRODUCT_SEARCH_PAGE_NAME = "ProductSearchPage"; //$NON-NLS-1$
    private static final String PRODUCT_SEARCH_DATA = "ProductSearchData"; //$NON-NLS-1$
    private ScrolledComposite conditionComposite;
    private Composite baseComposite;

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        readConfiguration();

        baseComposite = new Composite(parent, SWT.NONE);
        baseComposite.setLayout(new GridLayout(1, true));
        baseComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

        Composite createGridComposite = toolkit.createGridComposite(baseComposite, 2, false, false);

        toolkit.createLabel(createGridComposite, "Product Component Type");

        IpsObjectRefControl ctrProductComponentTypeChooser = new IpsObjectRefControl(null, createGridComposite,
                toolkit, "title", "message") {

            @Override
            protected IIpsSrcFile[] getIpsSrcFiles() throws CoreException {
                return getModel().getProductCmptTypesSrcFiles().toArray(new IIpsSrcFile[0]);
            }

            @Override
            protected void updateTextControlAfterDialogOK(List<IIpsSrcFile> ipsSrcFiles) {
                try {
                    getModel().setProductCmptType((IProductCmptType)ipsSrcFiles.get(0).getIpsObject());
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
                super.updateTextControlAfterDialogOK(ipsSrcFiles);
            }
        };
        ctrProductComponentTypeChooser.setEnabled(true);
        ctrProductComponentTypeChooser.getTextControl().setEnabled(false);

        Text txtSrcFilePatternText = createSrcFilePatternText(toolkit, baseComposite);
        getBindingContext().bindEnabled(txtSrcFilePatternText, getModel(),
                ProductSearchPresentationModel.PRODUCT_COMPONENT_TYPE_CHOSEN);

        conditionComposite = createConditionComposite(toolkit, baseComposite);

        Composite content = new Composite(conditionComposite, 0);
        content.setLayout(new RowLayout(SWT.VERTICAL));
        conditionComposite.setContent(content);

        getModel().initDefaultSearchValues();

        setControl(baseComposite);
    }

    private ScrolledComposite createConditionComposite(UIToolkit toolkit, Composite composite) {

        ScrolledComposite scrComposite = new ScrolledComposite(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

        scrComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        toolkit.createCheckbox(scrComposite);

        Button btnAdd = toolkit.createButton(composite, "Add Condition");
        btnAdd.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ICondition condition = new ProductAttributeCondition();
                addCondition(condition);
            }
        });
        getBindingContext().bindEnabled(btnAdd, getModel(),
                ProductSearchPresentationModel.PRODUCT_COMPONENT_TYPE_CHOSEN);

        return scrComposite;
    }

    private void addCondition(ICondition condition) {
        try {
            Composite content = new Composite(conditionComposite, NONE);
            content.setLayout(new RowLayout(SWT.VERTICAL | SWT.FILL));

            Control[] children = ((Composite)conditionComposite.getContent()).getChildren();

            int conditions = 0;
            for (Control control : children) {
                if (control instanceof ProductSearchConditionControl) {
                    conditions++;
                    control.setParent(content);
                }
            }

            ProductSearchConditionControl searchConditionControl = new ProductSearchConditionControl(content,
                    condition, getModel());
            searchConditionControl.setSize(800, 70);

            content.setSize(1200, (conditions + 1) * 70);
            conditionComposite.setContent(content);

            /*
             * Composite content = (Composite)conditionComposite.getContent();
             * ProductSearchConditionControl searchConditionControl = new
             * ProductSearchConditionControl(content, condition);
             * searchConditionControl.setSize(800, 200); searchConditionControl.layout(true);
             * 
             * content.layout(true); conditionComposite.setContent(content);
             * conditionComposite.layout(true);
             */

            // getControl().redraw();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        };
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
