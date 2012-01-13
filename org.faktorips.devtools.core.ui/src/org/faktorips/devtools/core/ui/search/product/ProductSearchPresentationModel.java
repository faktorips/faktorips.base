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

    private static final IConditionType[] CONDITION_TYPES = { new ProductAttributeConditionType(),
            new PolicyAttributeConditionType(), new ProductComponentAssociationConditionType() };

    private final List<ProductSearchConditionPresentationModel> productSearchConditionPresentationModels = new ArrayList<ProductSearchConditionPresentationModel>();

    public static final String PRODUCT_COMPONENT_TYPE = "productCmptType"; //$NON-NLS-1$

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
        productCmptType = newValue;

        productSearchConditionPresentationModels.clear();

        notifyListeners(new PropertyChangeEvent(this, PRODUCT_COMPONENT_TYPE, oldValue, newValue));
    }

    public static final String PRODUCT_COMPONENT_TYPE_CHOSEN = "productCmptTypeChosen"; //$NON-NLS-1$

    /**
     * Returns true, if the {@link #productCmptType} is already set
     */
    public boolean isProductCmptTypeChosen() {
        return productCmptType != null;
    }

    public static final String VALID_SEARCH = "valid"; //$NON-NLS-1$

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
        productSearchConditionPresentationModels.add(new ProductSearchConditionPresentationModel(this));
    }

    /**
     * Removes the given {@link ProductSearchConditionPresentationModel} from the list
     * 
     * @return true if the list contained the specified ProductSearchConditionPresentationModel
     */
    public boolean removeProductSearchConditionPresentationModels(ProductSearchConditionPresentationModel productSearchConditionPresentationModel) {
        return productSearchConditionPresentationModels.remove(productSearchConditionPresentationModel);
    }

    private boolean areAllProductSearchConditionPresentationModelsValid() {
        for (ProductSearchConditionPresentationModel productSearchConditionPresentationModel : getProductSearchConditionPresentationModels()) {
            if (!productSearchConditionPresentationModel.isValid()) {
                return false;
            }
        }
        return true;
    }

    public static final String CONDITION_AVAILABLE = "conditionAvailable"; //$NON-NLS-1$

    /**
     * Returns {@code true}, if there is any element on the {@link #productCmptType}, which can be
     * searched. If there are no elements, which can be served, this method returns {@code false}
     * 
     */
    public boolean isConditionAvailable() {
        return !getAvailableConditions().isEmpty();
    }

    /**
     * Returns a List with all available {@link IConditionType IConditions}
     */
    public List<IConditionType> getAvailableConditions() {
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
