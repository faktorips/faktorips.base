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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.search.AbstractIpsSearchPage;

public class ProductSearchPage extends AbstractIpsSearchPage<ProductSearchPresentationModel> {

    private static final String PRODUCT_SEARCH_PAGE_NAME = "ProductSearchPage"; //$NON-NLS-1$
    private static final String PRODUCT_SEARCH_DATA = "ProductSearchData"; //$NON-NLS-1$
    private Combo cboSearchProductComponentType;

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        readConfiguration();

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, true));
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

        toolkit.createLabel(composite, "Product Component Type");

        cboSearchProductComponentType = new Combo(composite, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
        cboSearchProductComponentType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        cboSearchProductComponentType.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int selectionIndex = cboSearchProductComponentType.getSelectionIndex();
                if (selectionIndex == -1) {
                    return;
                }

                /*
                 * TODO Bindung fuer die Combo EnumField<Enum<Enum<E>>> field =
                 * getBindingContext().bindContent(cboSearchProductComponentType, getModel(),
                 * ModelSearchPresentationModel.SEARCH_ATTRIBUTES,
                 * getModel().getProductComponentTypeLabels().toArray());
                 */

                getModel().setProductCmptTypeIndex(selectionIndex);

                // TODO bereits gelesene Werte
                // model.read(previousSearchData.get(selectionIndex));
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing to do
            }
        });

        List<String> productComponentTypeLabels = getModel().getProductComponentTypeLabels();

        cboSearchProductComponentType.setItems(productComponentTypeLabels.toArray(new String[0]));
        /*
         * for (String productCmptTypeLabel : productComponentTypeLabels) {
         * cboSearchProductComponentType.add(productCmptTypeLabel); }
         */

        setControl(composite);
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
        return new ProductSearchPresentationModel();
    }

}
