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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.search.IIpsSearchPartPresentationModel;
import org.faktorips.devtools.core.ui.search.product.conditions.ICondition;
import org.faktorips.devtools.core.ui.search.product.conditions.ISearchOperatorType;
import org.faktorips.util.ArgumentCheck;

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

    private int operatorTypeIndex = -1;

    private ICondition condition = null;
    private String argument = null;
    private IIpsElement searchedElement = null;

    private List<String> allowedValues = null;

    public ProductSearchConditionPresentationModel(ProductSearchPresentationModel parentSearchPresentationModel) {
        this.parentSearchPresentationModel = parentSearchPresentationModel;
        this.searchableElements = Collections.emptyList();
        this.operatorTypes = Collections.emptyList();

        List<ICondition> conditionsWithSearchableElements = getConditionsWithSearchableElements();
        if (!conditionsWithSearchableElements.isEmpty()) {
            condition = conditionsWithSearchableElements.get(0);
            updateSearchableElements();
        }
    }

    @Override
    public Locale getSearchLocale() {
        return parentSearchPresentationModel.getSearchLocale();
    }

    @Override
    public void store(IDialogSettings settings) {
        // TODO Auto-generated method stub
    }

    @Override
    public void read(IDialogSettings settings) {
        // TODO Auto-generated method stub
    }

    public void setOperatorTypeIndex(Integer newValue) {
        operatorTypeIndex = newValue;

        parentSearchPresentationModel.notifyListeners(new PropertyChangeEvent(this, "", null, null));
    }

    public Integer getOperatorTypeIndex() {
        return operatorTypeIndex;
    }

    public ISearchOperatorType getOperatorType() {
        return operatorTypes.get(operatorTypeIndex);
    }

    protected List<? extends ISearchOperatorType> getSearchOperatorTypes() {
        return operatorTypes;
    }

    private void updateOperatorTypes() {
        if (getSearchedElement() == null) {
            operatorTypes = Collections.emptyList();
        }
        operatorTypes = getCondition().getSearchOperatorTypes(getSearchedElement());

    }

    private void updateOperatorType() {
        operatorTypeIndex = -1;
    }

    public void setSearchedElement(IIpsElement newValue) {
        searchedElement = newValue;

        updateOperatorTypes();
        updateOperatorType();
        updateAllowedAttributeValues();
        parentSearchPresentationModel.notifyListeners(new PropertyChangeEvent(this, "", null, null));
    }

    public IIpsElement getSearchedElement() {
        return searchedElement;
    }

    private void updateSearchedElement() {
        searchedElement = null;
    }

    private void updateSearchableElements() {
        searchableElements = condition.getSearchableElements(parentSearchPresentationModel.getProductCmptType());
    }

    public boolean isSearchedElementChosen() {
        return searchedElement != null;
    }

    protected List<? extends IIpsElement> getSearchableElements() {
        return searchableElements;
    }

    public ICondition getCondition() {
        return condition;
    }

    public void setCondition(ICondition condition) {
        ArgumentCheck.notNull(condition);
        ArgumentCheck.isTrue(!condition.getSearchableElements(parentSearchPresentationModel.getProductCmptType())
                .isEmpty(), "Conditions are not allowed without elements, which can be searched."); //$NON-NLS-1$

        if (condition.equals(this.condition)) {
            return;
        }

        this.condition = condition;

        updateSearchableElements();
        updateSearchedElement();

        updateOperatorTypes();
        updateOperatorType();
        updateArgument();

        parentSearchPresentationModel.notifyListeners(new PropertyChangeEvent(this, "", null, null)); //$NON-NLS-1$
    }

    protected List<ICondition> getConditionsWithSearchableElements() {
        return parentSearchPresentationModel.getAvailableConditions();
    }

    public ValueDatatype getValueDatatype() {
        return getCondition().getValueDatatype(getSearchedElement());
    }

    public void setArgument(String newValue) {
        argument = newValue;

        parentSearchPresentationModel.notifyListeners(new PropertyChangeEvent(this, "", null, null));
    }

    public String getArgument() {
        return argument;
    }

    private void updateArgument() {
        argument = null;
        if (!isSearchedElementChosen()) {
            return;
        }
        if (getCondition().hasValueSet()) {
            IValueSet valueSet = getCondition().getValueSet(getSearchedElement());

            if (valueSet instanceof EnumValueSet) {
                EnumValueSet enumValueSet = (EnumValueSet)valueSet;
                if (enumValueSet.size() > 0) {
                    argument = enumValueSet.getValue(0);
                    return;
                }
            }
            return;
        }
        if (!getAllowedAttributeValues().isEmpty()) {
            argument = getAllowedAttributeValues().get(0);
        }

    }

    /**
     * @return a List<String> with allowed values
     */
    protected List<String> getAllowedAttributeValues() {
        if (getCondition().hasValueSet()) {
            // TODO exception werfen????
            return Collections.emptyList();
        }

        if (allowedValues == null) {
            updateAllowedAttributeValues();
        }
        return allowedValues;
    }

    private void updateAllowedAttributeValues() {
        if (getCondition().hasValueSet()) {
            allowedValues = Collections.emptyList();
            return;
        }
        Collection<?> allowedValuesInCondition = getCondition().getAllowedValues(getSearchedElement());
        allowedValues = new ArrayList<String>();

        for (Object object : allowedValuesInCondition) {
            allowedValues.add(object.toString());
        }
    }

    public void dispose() {
        parentSearchPresentationModel.removeProductSearchConditionPresentationModels(this);
    }

    @Override
    public boolean isValid() {
        return isSearchedElementChosen() && isOperatorTypeChosen();
    }

    private boolean isOperatorTypeChosen() {
        return operatorTypeIndex >= 0;
    }
}
