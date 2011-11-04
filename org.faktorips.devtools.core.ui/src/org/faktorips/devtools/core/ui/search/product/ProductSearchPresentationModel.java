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
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.search.ui.ISearchQuery;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.search.AbstractSearchPresentationModel;
import org.faktorips.devtools.core.ui.search.product.conditions.ICondition;
import org.faktorips.devtools.core.ui.search.product.conditions.PolicyAttributeCondition;
import org.faktorips.devtools.core.ui.search.product.conditions.ProductAttributeCondition;
import org.faktorips.devtools.core.ui.search.product.conditions.ProductComponentAssociationCondition;

public class ProductSearchPresentationModel extends AbstractSearchPresentationModel {

    public static final String VALID_SEARCH = "valid"; //$NON-NLS-1$
    public static final String PRODUCT_COMPONENT_TYPE_CHOSEN = "productCmptTypeChosen"; //$NON-NLS-1$
    public static final String PRODUCT_COMPONENT_TYPE = "productCmptType"; //$NON-NLS-1$

    private final List<ProductSearchConditionPresentationModel> productSearchConditionPresentationModels = new ArrayList<ProductSearchConditionPresentationModel>();

    private static final ICondition[] CONDITION_TYPES = { new ProductAttributeCondition(),
            new PolicyAttributeCondition(), new ProductComponentAssociationCondition() };

    private IProductCmptType productCmptType;

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
    }

    @Override
    protected void notifyListeners(PropertyChangeEvent event) {
        super.notifyListeners(event);
    }

    private boolean areAllProductSearchConditionPresentationModelsValid() {
        for (ProductSearchConditionPresentationModel productSearchConditionPresentationModel : getProductSearchConditionPresentationModels()) {
            if (!productSearchConditionPresentationModel.isValid()) {
                return false;
            }
        }
        return true;
    }

    public boolean isProductCmptTypeChosen() {
        return productCmptType != null;
    }

    @Override
    public boolean isValid() {
        return isProductCmptTypeChosen() && areAllProductSearchConditionPresentationModelsValid();
    }

    @Override
    protected void initDefaultSearchValues() {
        // nothing to do
    }

    public List<ProductSearchConditionPresentationModel> getProductSearchConditionPresentationModels() {
        return productSearchConditionPresentationModels;
    }

    public void addProductSearchConditionPresentationModels(ProductSearchConditionPresentationModel productSearchConditionPresentationModel) {
        productSearchConditionPresentationModels.add(productSearchConditionPresentationModel);
    }

    public boolean removeProductSearchConditionPresentationModels(ProductSearchConditionPresentationModel productSearchConditionPresentationModel) {
        return productSearchConditionPresentationModels.remove(productSearchConditionPresentationModel);
    }

    protected List<ICondition> getConditionsWithSearchableElements() {
        if (productCmptType == null) {
            return Collections.emptyList();
        }
        
        List<ICondition> conditionsWithSearchableElements = new ArrayList<ICondition>();
        for (ICondition condition : CONDITION_TYPES) {
            List<IIpsElement> searchableElements = condition.getSearchableElements(productCmptType);
            if (!searchableElements.isEmpty()) {
                conditionsWithSearchableElements.add(condition);
            }
        }
        return conditionsWithSearchableElements;
    }
}
