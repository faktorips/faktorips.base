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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.search.IIpsSearchPartPresentationModel;
import org.faktorips.devtools.core.ui.search.product.conditions.ICondition;
import org.faktorips.devtools.core.ui.search.product.conditions.ISearchOperatorType;

public class ProductSearchConditionPresentationModel extends PresentationModelObject implements
        IIpsSearchPartPresentationModel {

    public static final String ARGUMENT = "argument"; //$NON-NLS-1$
    public static final String SEARCHED_ELEMENT_INDEX = "searchedElementIndex"; //$NON-NLS-1$
    public static final String SEARCHED_ELEMENT_CHOSEN = "searchedElementChosen"; //$NON-NLS-1$

    public static final String OPERATOR_TYPE_INDEX = "operatorTypeIndex"; //$NON-NLS-1$
    public static final String OPERATOR_TYPE_CHOSEN = "operatorTypeChosen"; //$NON-NLS-1$

    private final ProductSearchPresentationModel parentSearchPresentationModel;
    private final ICondition condition;

    private final List<? extends IIpsElement> searchableElements;
    private Integer searchedElementIndex;

    private List<ISearchOperatorType> operatorTypes;
    private Integer operatorTypeIndex;

    private String argument = null;

    public ProductSearchConditionPresentationModel(ProductSearchPresentationModel parentSearchPresentationModel,
            ICondition condition) throws CoreException {
        this.parentSearchPresentationModel = parentSearchPresentationModel;
        this.condition = condition;
        this.searchableElements = condition.getSearchableElements(parentSearchPresentationModel.getProductCmptType());
        this.operatorTypes = Collections.emptyList();
        this.parentSearchPresentationModel.addProductSearchConditionPresentationModels(this);
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
        Integer oldValue = operatorTypeIndex;
        operatorTypeIndex = newValue;

        notifyListeners(new PropertyChangeEvent(this, OPERATOR_TYPE_INDEX, oldValue, newValue));

        notifyListeners(new PropertyChangeEvent(this, OPERATOR_TYPE_CHOSEN, Boolean.valueOf(oldValue != null),
                Boolean.valueOf(newValue != null)));
    }

    public Integer getOperatorTypeIndex() {
        return operatorTypeIndex;
    }

    public ISearchOperatorType getOperatorType() {
        return operatorTypes.get(operatorTypeIndex);
    }

    public void setSearchedElementIndex(Integer newValue) {
        Integer oldValue = searchedElementIndex;
        searchedElementIndex = newValue;

        notifyListeners(new PropertyChangeEvent(this, SEARCHED_ELEMENT_INDEX, oldValue, newValue));

        updateOperatorTypes();
        notifyListeners(new PropertyChangeEvent(this, SEARCHED_ELEMENT_CHOSEN, Boolean.valueOf(oldValue != null),
                Boolean.valueOf(newValue != null)));
    }

    public Integer getSearchedElementIndex() {
        return searchedElementIndex;
    }

    private void updateOperatorTypes() {
        operatorTypes = getCondition().getSearchOperatorTypes(getSearchedElement());

    }

    public IIpsElement getSearchedElement() {
        return getSearchableElements().get(searchedElementIndex);
    }

    public ICondition getCondition() {
        return condition;
    }

    protected List<ISearchOperatorType> getSearchOperatorTypes() {
        return operatorTypes;
    }

    protected List<? extends IIpsElement> getSearchableElements() {
        return searchableElements;
    }

    public boolean isSearchedElementChosen() {
        return searchedElementIndex >= 0;
    }

    public ValueDatatype getValueDatatype() {
        return condition.getValueDatatype(getSearchedElement());
    }

    public void setArgument(String newValue) {
        String oldValue = argument;
        argument = newValue;

        notifyListeners(new PropertyChangeEvent(this, ARGUMENT, oldValue, newValue));
    }

    public String getArgument() {
        return argument;
    }

    public void dispose() {
        parentSearchPresentationModel.removeProductSearchConditionPresentationModels(this);
    }

    public boolean isValid() {
        if (searchedElementIndex == null) {
            return false;
        }
        if (operatorTypeIndex == null) {
            return false;
        }
        return true;
    }
}
