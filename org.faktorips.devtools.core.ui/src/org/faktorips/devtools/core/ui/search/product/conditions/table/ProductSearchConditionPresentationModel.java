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

package org.faktorips.devtools.core.ui.search.product.conditions.table;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.search.IIpsSearchPartPresentationModel;
import org.faktorips.devtools.core.ui.search.product.ProductSearchPresentationModel;
import org.faktorips.devtools.core.ui.search.product.conditions.types.IConditionType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ISearchOperatorType;
import org.faktorips.util.ArgumentCheck;

/**
 * 
 * The ProductSearchConditionPresentationModel represents one condition for the Product Search of
 * Faktor-IPS
 * 
 * @author dicker
 */
public class ProductSearchConditionPresentationModel extends PresentationModelObject implements
        IIpsSearchPartPresentationModel {

    public static final String ARGUMENT = "argument"; //$NON-NLS-1$
    public static final String SEARCHED_ELEMENT_INDEX = "searchedElementIndex"; //$NON-NLS-1$
    public static final String SEARCHED_ELEMENT_CHOSEN = "searchedElementChosen"; //$NON-NLS-1$

    public static final String OPERATOR_TYPE_INDEX = "operatorTypeIndex"; //$NON-NLS-1$
    public static final String OPERATOR_TYPE_CHOSEN = "operatorTypeChosen"; //$NON-NLS-1$

    private final ProductSearchPresentationModel parentSearchPresentationModel;

    private List<? extends IIpsElement> searchableElements;
    private List<? extends ISearchOperatorType> operatorTypes;

    private IConditionType conditionType = null;
    private IIpsElement searchedElement = null;
    private ISearchOperatorType operatorType = null;
    private String argument = null;

    private List<String> allowedValues = null;

    public ProductSearchConditionPresentationModel(ProductSearchPresentationModel parentSearchPresentationModel) {
        this.parentSearchPresentationModel = parentSearchPresentationModel;
        this.searchableElements = Collections.emptyList();
        this.operatorTypes = Collections.emptyList();

        List<IConditionType> conditionsWithSearchableElements = getConditionsWithSearchableElements();
        if (!conditionsWithSearchableElements.isEmpty()) {
            conditionType = conditionsWithSearchableElements.get(0);
            updateSearchableElements();
        }
    }

    @Override
    public void store(IDialogSettings settings) {
        // nothing to do
    }

    @Override
    public void read(IDialogSettings settings) {
        // nothing to do
    }

    public void setOperatorType(ISearchOperatorType newValue) {
        operatorType = newValue;

        parentSearchPresentationModel.notifyListeners(new PropertyChangeEvent(this, StringUtils.EMPTY, null, null));
    }

    public ISearchOperatorType getOperatorType() {
        return operatorType;
    }

    protected List<? extends ISearchOperatorType> getSearchOperatorTypes() {
        return operatorTypes;
    }

    private void updateOperatorTypes() {
        if (getSearchedElement() == null) {
            operatorTypes = Collections.emptyList();
        }
        operatorTypes = getConditionType().getSearchOperatorTypes(getSearchedElement());
    }

    private void updateOperatorType() {
        if (operatorTypes.isEmpty()) {
            operatorType = null;
        } else {
            if (!operatorTypes.contains(operatorType)) {
                operatorType = operatorTypes.get(0);
            }
        }
    }

    public void setSearchedElement(IIpsElement newValue) {
        if ((searchedElement == null && newValue != null)
                || (searchedElement != null && !searchedElement.equals(newValue))) {

            searchedElement = newValue;

            updateOperatorTypes();
            updateOperatorType();
            updateAllowedAttributeValues();
            updateArgument();
        }
        parentSearchPresentationModel.notifyListeners(new PropertyChangeEvent(this, StringUtils.EMPTY, null, null));
    }

    public IIpsElement getSearchedElement() {
        return searchedElement;
    }

    private void updateSearchedElement() {
        if (searchableElements.isEmpty()) {
            searchedElement = null;
        } else {
            if (!searchableElements.contains(searchedElement)) {
                searchedElement = searchableElements.get(0);
            }
        }
    }

    private void updateSearchableElements() {
        searchableElements = conditionType.getSearchableElements(parentSearchPresentationModel.getProductCmptType());
    }

    public boolean isSearchedElementChosen() {
        return searchedElement != null;
    }

    protected List<? extends IIpsElement> getSearchableElements() {
        return searchableElements;
    }

    public IConditionType getConditionType() {
        return conditionType;
    }

    public void setCondition(IConditionType conditionType) {
        ArgumentCheck.notNull(conditionType);
        ArgumentCheck.isTrue(!conditionType.getSearchableElements(parentSearchPresentationModel.getProductCmptType())
                .isEmpty(), "Conditions are not allowed without elements, which can be searched."); //$NON-NLS-1$

        if (conditionType.equals(this.conditionType)) {
            return;
        }

        this.conditionType = conditionType;

        updateSearchableElements();
        updateSearchedElement();

        updateOperatorTypes();
        updateOperatorType();
        updateAllowedAttributeValues();
        updateArgument();

        parentSearchPresentationModel.notifyListeners(new PropertyChangeEvent(this, StringUtils.EMPTY, null, null));
    }

    /**
     * return a List of {@link IConditionType IConditionTypes}: For the given
     * {@link IProductCmptType} all these IConditionTypes have elements, which can be searched.
     */
    protected List<IConditionType> getConditionsWithSearchableElements() {
        return parentSearchPresentationModel.getAvailableConditionTypes();
    }

    /**
     * returns the {@link ValueDatatype} of the element, which is chosen for the search
     */
    public ValueDatatype getValueDatatype() {
        return getConditionType().getValueDatatype(getSearchedElement());
    }

    public void setArgument(String newValue) {
        argument = newValue;

        parentSearchPresentationModel.notifyListeners(new PropertyChangeEvent(this, StringUtils.EMPTY, null, null));
    }

    public String getArgument() {
        return argument;
    }

    private void updateArgument() {
        argument = null;
    }

    /**
     * Returns a List<String> with allowed values
     */
    protected List<String> getAllowedAttributeValues() {
        if (getConditionType().hasValueSet()) {
            return Collections.emptyList();
        }

        if (allowedValues == null) {
            updateAllowedAttributeValues();
        }
        return allowedValues;
    }

    private void updateAllowedAttributeValues() {
        if (getConditionType().hasValueSet()) {
            allowedValues = Collections.emptyList();
            return;
        }
        Collection<?> allowedValuesInCondition = getConditionType().getAllowedValues(getSearchedElement());
        allowedValues = new ArrayList<String>();

        for (Object object : allowedValuesInCondition) {
            allowedValues.add(object.toString());
        }
    }

    @Override
    public boolean isValid() {
        return isSearchedElementChosen() && isOperatorTypeChosen();
    }

    private boolean isOperatorTypeChosen() {
        return operatorType != null;
    }

    public ProductSearchPresentationModel getParentSearchPresentationModel() {
        return parentSearchPresentationModel;
    }
}
