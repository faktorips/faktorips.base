/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.search.AbstractSearchPresentationModel;
import org.faktorips.devtools.core.ui.search.product.conditions.table.ProductSearchConditionPresentationModel;
import org.faktorips.devtools.core.ui.search.product.conditions.types.IConditionType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.PolicyAttributeConditionType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ProductAttributeConditionType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ProductComponentAssociationConditionType;

/**
 * The ProductSearchPresentationModel is the implementation of the {@link PresentationModelObject}
 * for the product search.
 * <p>
 * The ProductSearchPresentationModel contains the {@link IProductCmptType} {@link #productCmptType}
 * and a List of {@link ProductSearchConditionPresentationModel} for more detailed search
 * conditions.
 * 
 * @author dicker
 */
public class ProductSearchPresentationModel extends AbstractSearchPresentationModel {

    public static final String PRODUCT_COMPONENT_TYPE = "productCmptType"; //$NON-NLS-1$

    public static final String PRODUCT_COMPONENT_TYPE_CHOSEN = "productCmptTypeChosen"; //$NON-NLS-1$

    public static final String VALID_SEARCH = "valid"; //$NON-NLS-1$

    public static final String CONDITION_DEFINED = "conditionDefined"; //$NON-NLS-1$

    public static final String CONDITION_TYPE_AVAILABLE = "conditionTypeAvailable"; //$NON-NLS-1$

    private static final IConditionType[] CONDITION_TYPES = { new ProductAttributeConditionType(),
            new PolicyAttributeConditionType(), new ProductComponentAssociationConditionType() };

    private final List<ProductSearchConditionPresentationModel> productSearchConditionPresentationModels = new ArrayList<ProductSearchConditionPresentationModel>();

    private IProductCmptType productCmptType;

    /**
     * Returns the {@link IProductCmptType}, which is the base for the search
     */
    public IProductCmptType getProductCmptType() {
        return productCmptType;
    }

    /**
     * @see #getProductCmptType()
     */
    public void setProductCmptType(IProductCmptType newValue) {
        IProductCmptType oldValue = productCmptType;
        boolean oldConditionTypeAvailable = isConditionTypeAvailable();
        boolean oldConditionDefined = isConditionDefined();

        productCmptType = newValue;

        productSearchConditionPresentationModels.clear();

        notifyListeners(new PropertyChangeEvent(this, PRODUCT_COMPONENT_TYPE, oldValue, newValue));
        notifyListeners(new PropertyChangeEvent(this, CONDITION_TYPE_AVAILABLE, oldConditionTypeAvailable,
                isConditionTypeAvailable()));
        notifyListeners(new PropertyChangeEvent(this, CONDITION_DEFINED, oldConditionDefined, isConditionDefined()));
    }

    /**
     * Returns true, if the {@link #productCmptType} is already set
     */
    public boolean isProductCmptTypeChosen() {
        return productCmptType != null;
    }

    @Override
    public boolean isValid() {
        return isProductCmptTypeChosen() && areAllProductSearchConditionPresentationModelsValid();
    }

    /**
     * Returns a copy of the List with the {@link ProductSearchConditionPresentationModel
     * ProductSearchConditionPresentationModels}
     */
    public List<ProductSearchConditionPresentationModel> getProductSearchConditionPresentationModels() {
        return Collections.unmodifiableList(productSearchConditionPresentationModels);
    }

    /**
     * Creates a new {@link ProductSearchConditionPresentationModel} and adds it to the List of
     * ProductSearchConditionPresentationModel
     */
    public void createProductSearchConditionPresentationModel() {
        boolean oldConditionDefined = isConditionDefined();

        productSearchConditionPresentationModels.add(new ProductSearchConditionPresentationModel(this));
        notifyListeners(new PropertyChangeEvent(this, CONDITION_DEFINED, oldConditionDefined, isConditionDefined()));
    }

    /**
     * Removes the given {@link ProductSearchConditionPresentationModel} from the list
     * 
     * @return true if the list contained the specified ProductSearchConditionPresentationModel
     */
    public boolean removeProductSearchConditionPresentationModels(ProductSearchConditionPresentationModel productSearchConditionPresentationModel) {
        boolean oldConditionDefined = isConditionDefined();

        boolean remove = productSearchConditionPresentationModels.remove(productSearchConditionPresentationModel);

        notifyListeners(new PropertyChangeEvent(this, CONDITION_DEFINED, oldConditionDefined, isConditionDefined()));

        return remove;
    }

    private boolean areAllProductSearchConditionPresentationModelsValid() {
        for (ProductSearchConditionPresentationModel productSearchConditionPresentationModel : getProductSearchConditionPresentationModels()) {
            if (!productSearchConditionPresentationModel.isValid()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true}, if there is any element on the {@link #productCmptType}, which can be
     * searched. If there are no elements, which can be served, this method returns {@code false}
     * 
     */
    public boolean isConditionTypeAvailable() {
        return !getAvailableConditionTypes().isEmpty();
    }

    /**
     * Returns {@code true}, if there is any element on the {@link #productCmptType}, which can be
     * searched. If there are no elements, which can be served, this method returns {@code false}
     * 
     */
    public boolean isConditionDefined() {
        return !productSearchConditionPresentationModels.isEmpty();
    }

    /**
     * Returns a List with all available {@link IConditionType IConditionTypes}
     */
    public List<IConditionType> getAvailableConditionTypes() {
        if (productCmptType == null) {
            return Collections.emptyList();
        }

        List<IConditionType> conditionsWithSearchableElements = new ArrayList<IConditionType>();
        for (IConditionType conditionType : CONDITION_TYPES) {
            List<IIpsElement> searchableElements = conditionType.getSearchableElements(productCmptType);
            if (!searchableElements.isEmpty()) {
                conditionsWithSearchableElements.add(conditionType);
            }
        }
        return conditionsWithSearchableElements;
    }

    @Override
    public void store(IDialogSettings settings) {
        // no idea yet

    }

    @Override
    public void read(IDialogSettings settings) {
        // TODO which data should be stored in the dialog settings?

    }

    @Override
    public void notifyListeners(PropertyChangeEvent event) {
        super.notifyListeners(event);
    }

    @Override
    protected void initDefaultSearchValues() {
        // nothing to do
    }
}
