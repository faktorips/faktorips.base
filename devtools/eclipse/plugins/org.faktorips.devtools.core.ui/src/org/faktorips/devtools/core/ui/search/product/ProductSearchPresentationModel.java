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

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.search.AbstractSearchPresentationModel;
import org.faktorips.devtools.core.ui.search.product.conditions.table.ProductSearchConditionPresentationModel;
import org.faktorips.devtools.core.ui.search.product.conditions.types.IConditionType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.PolicyAttributeConditionType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ProductAttributeConditionType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ProductComponentAssociationConditionType;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.runtime.internal.IpsStringUtils;

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

    public static final String IPS_PROJECT_NAME = "ipsProjectName"; //$NON-NLS-1$

    public static final String PRODUCT_COMPONENT_TYPE = "productCmptType"; //$NON-NLS-1$

    public static final String PRODUCT_COMPONENT_TYPE_CHOSEN = "productCmptTypeChosen"; //$NON-NLS-1$

    public static final String VALID_SEARCH = "valid"; //$NON-NLS-1$

    public static final String CONDITION_DEFINED = "conditionDefined"; //$NON-NLS-1$

    public static final String CONDITION_TYPE_AVAILABLE = "conditionTypeAvailable"; //$NON-NLS-1$

    private static final IConditionType[] CONDITION_TYPES = { new ProductAttributeConditionType(),
            new PolicyAttributeConditionType(), new ProductComponentAssociationConditionType() };

    private List<ProductSearchConditionPresentationModel> conditionPMOs = new ArrayList<>();

    private IProductCmptType productCmptType;

    private String ipsProjectName = IpsStringUtils.EMPTY;

    public ProductSearchPresentationModel() {
    }

    /**
     * Returns the {@link IProductCmptType}, which is the base for the search
     */
    public IProductCmptType getProductCmptType() {
        return productCmptType;
    }

    public String getProductCmptTypeQName() {
        return productCmptType.getQualifiedName();
    }

    public void setProductCmptTypeQName(String projectName, String productCmptTypeName) {
        IIpsProject ipsProject = IIpsModel.get().getIpsProject(projectName);
        IIpsObject ipsObject = ipsProject.findIpsObject(new QualifiedNameType(productCmptTypeName,
                IpsObjectType.PRODUCT_CMPT_TYPE));
        setProductCmptType((IProductCmptType)ipsObject);
    }

    public String getIpsProjectName() {
        if (getProductCmptType() != null) {
            return getProductCmptType().getIpsProject().getName();
        } else {
            return ipsProjectName;
        }
    }

    public void setIpsProjectName(String ipsProjectName) {
        this.ipsProjectName = ipsProjectName;
    }

    /**
     * @see #getProductCmptType()
     */
    public void setProductCmptType(IProductCmptType newValue) {
        IProductCmptType oldValue = productCmptType;
        boolean oldConditionTypeAvailable = isConditionTypeAvailable();
        boolean oldConditionDefined = isConditionDefined();

        productCmptType = newValue;

        conditionPMOs.clear();

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
        return Collections.unmodifiableList(conditionPMOs);
    }

    /**
     * Creates a new {@link ProductSearchConditionPresentationModel} and adds it to the List of
     * ProductSearchConditionPresentationModel and returns it.
     */
    public ProductSearchConditionPresentationModel createProductSearchConditionPresentationModel() {
        boolean oldConditionDefined = isConditionDefined();

        ProductSearchConditionPresentationModel conditionPresentationModel = new ProductSearchConditionPresentationModel(
                this);
        conditionPMOs.add(conditionPresentationModel);
        notifyListeners(new PropertyChangeEvent(this, CONDITION_DEFINED, oldConditionDefined, isConditionDefined()));
        return conditionPresentationModel;
    }

    /**
     * Removes the given {@link ProductSearchConditionPresentationModel} from the list
     * 
     * @return true if the list contained the specified ProductSearchConditionPresentationModel
     */
    public boolean removeProductSearchConditionPresentationModels(
            ProductSearchConditionPresentationModel productSearchConditionPresentationModel) {
        boolean oldConditionDefined = isConditionDefined();

        boolean remove = conditionPMOs.remove(productSearchConditionPresentationModel);

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
        return !conditionPMOs.isEmpty();
    }

    /**
     * Returns a List with all available {@link IConditionType IConditionTypes}
     */
    public List<IConditionType> getAvailableConditionTypes() {
        if (productCmptType == null) {
            return Collections.emptyList();
        }

        List<IConditionType> conditionsWithSearchableElements = new ArrayList<>();
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
        settings.put(PRODUCT_COMPONENT_TYPE, getProductCmptTypeQName());
        settings.put(IPS_PROJECT_NAME, getIpsProjectName());
        settings.put(SRC_FILE_PATTERN, getSrcFilePattern());

        new ConditionPMOPersistence(this, settings).saveConditions();
    }

    @Override
    public void read(IDialogSettings settings) {
        if (settingIsValid(IPS_PROJECT_NAME, settings)) {
            String ipsProject = settings.get(IPS_PROJECT_NAME);
            setIpsProjectName(ipsProject);

            if (settingIsValid(PRODUCT_COMPONENT_TYPE, settings)) {
                setProductCmptTypeQName(ipsProject, settings.get(PRODUCT_COMPONENT_TYPE));
            }
        }
        if (settingIsValid(SRC_FILE_PATTERN, settings)) {
            setSrcFilePattern(settings.get(SRC_FILE_PATTERN));
        }
        conditionPMOs = new ArrayList<>(new ConditionPMOPersistence(this,
                settings).loadConditions());
    }

    private boolean settingIsValid(String storedKey, IDialogSettings settings) {
        return !IpsStringUtils.isEmpty(settings.get(storedKey));
    }

    @Override
    public void notifyListeners(PropertyChangeEvent event) {
        super.notifyListeners(event);
    }

}
