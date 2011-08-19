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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.search.ui.ISearchQuery;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.search.AbstractSearchPresentationModel;
import org.faktorips.devtools.core.ui.search.scope.IpsSearchWorkspaceScope;

public class ProductSearchPresentationModel extends AbstractSearchPresentationModel {

    public static final String VALID_SEARCH = "validSearch"; //$NON-NLS-1$
    public static final String PRODUCT_COMPONENT_TYPE_CHOSEN = "productCmptTypeChosen"; //$NON-NLS-1$
    public static final String PRODUCT_COMPONENT_TYPE = "productCmptType"; //$NON-NLS-1$

    private List<IIpsSrcFile> productCmptTypesSrcFiles;
    private final List<ProductSearchConditionPresentationModel> productSearchConditionPresentationModels = new ArrayList<ProductSearchConditionPresentationModel>();

    private IProductCmptType productCmptType;
    private boolean validSearch = false;

    @Override
    public void store(IDialogSettings settings) {
        // TODO Auto-generated method stub

    }

    @Override
    public void read(IDialogSettings settings) {
        // TODO Auto-generated method stub

    }

    @Override
    public ISearchQuery createSearchQuery() {
        return new ProductSearchQuery(this);
    }

    public List<IIpsSrcFile> getProductCmptTypesSrcFiles() {
        return productCmptTypesSrcFiles;
    }

    protected String getProductCmptTypeCompareValue(IProductCmptType productCmptType) {
        return productCmptType.getName();
    }

    public IProductCmptType getProductCmptType() {
        return productCmptType;
    }

    public void setProductCmptType(IProductCmptType newValue) {
        IProductCmptType oldValue = productCmptType;
        productCmptType = newValue;
        notifyListeners(new PropertyChangeEvent(this, PRODUCT_COMPONENT_TYPE, oldValue, newValue));

        setValidSearch(productCmptType != null);
    }

    public boolean isValidSearch() {
        return validSearch;
    }

    public boolean isProductCmptTypeChosen() {
        return productCmptType != null;
    }

    private void setValidSearch(boolean newValue) {
        boolean oldValue = validSearch;
        validSearch = newValue;

        notifyListeners(new PropertyChangeEvent(this, VALID_SEARCH, oldValue, newValue));

    }

    @Override
    protected void initDefaultSearchValues() {
        setValidSearch(false);

        // TODO doch mit IpsModel???
        IpsSearchWorkspaceScope ipsSearchWorkspaceScope = new IpsSearchWorkspaceScope();
        try {
            Set<IIpsSrcFile> selectedIpsSrcFiles = ipsSearchWorkspaceScope.getSelectedIpsSrcFiles();

            productCmptTypesSrcFiles = new ArrayList<IIpsSrcFile>();
            for (IIpsSrcFile srcFile : selectedIpsSrcFiles) {
                if (IpsObjectType.PRODUCT_CMPT_TYPE.equals(srcFile.getIpsObjectType())) {
                    productCmptTypesSrcFiles.add(srcFile);
                }
            }

        } catch (CoreException e) {
            // TODO was mach ma da?

        }
    }

    public List<ProductSearchConditionPresentationModel> getProductSearchConditionPresentationModels() {
        return new ArrayList<ProductSearchConditionPresentationModel>(productSearchConditionPresentationModels);
    }

    public void addProductSearchConditionPresentationModels(ProductSearchConditionPresentationModel productSearchConditionPresentationModel) {
        productSearchConditionPresentationModels.add(productSearchConditionPresentationModel);
    }

    public boolean removeProductSearchConditionPresentationModels(ProductSearchConditionPresentationModel productSearchConditionPresentationModel) {
        return productSearchConditionPresentationModels.remove(productSearchConditionPresentationModel);
    }
}
