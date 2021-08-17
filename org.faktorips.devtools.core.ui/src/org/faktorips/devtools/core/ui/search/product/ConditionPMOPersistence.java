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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.faktorips.devtools.core.ui.search.product.conditions.table.ProductSearchConditionPresentationModel;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ComparableSearchOperatorType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ContainsSearchOperatorType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.EqualitySearchOperatorType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.IConditionType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ISearchOperatorType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.LikeSearchOperatorType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.PolicyAttributeConditionType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ProductAttributeConditionType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ProductComponentAssociationConditionType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ReferenceSearchOperatorType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ValueSetSearchOperatorType;

/**
 * Responsible for both loading and storing {@link ProductSearchConditionPresentationModel condition
 * PMOs} for a given {@link ProductSearchPresentationModel} from and to {@link IDialogSettings}.
 */
public class ConditionPMOPersistence {

    public static final String SECTION_NAME = "ProductSearchConditionPMOs-Section"; //$NON-NLS-1$
    public static final String CONDITION_TYPE_KEY = "ConditionTypes"; //$NON-NLS-1$
    public static final String OPERATOR_TYPE_KEY = "OperatorTypes"; //$NON-NLS-1$
    public static final String ARGUMENTS_KEY = "Arguments"; //$NON-NLS-1$
    public static final String SEARCHED_ELEMENT_KEY = "SearchedElement"; //$NON-NLS-1$

    public static final String PRODUCT_COMPONENT_ASSOCIATION_CONDITION_TYPE = "org.faktorips.devtools.core.ui.search.product.conditions.types.ProductComponentAssociationConditionType"; //$NON-NLS-1$
    public static final String PRODUCT_ATTRIBUTE_CONDITION_TYPE = "org.faktorips.devtools.core.ui.search.product.conditions.types.ProductAttributeConditionType"; //$NON-NLS-1$
    public static final String POLICY_ATTRIBUTE_CONDITION_TYPE = "org.faktorips.devtools.core.ui.search.product.conditions.types.PolicyAttributeConditionType"; //$NON-NLS-1$

    private static final Map<String, ISearchOperatorType> OPERATOR_TYPE_MAP = new HashMap<>();

    private final ProductSearchPresentationModel searchPMO;
    private final IDialogSettings settings;

    static {
        registerCondition(ComparableSearchOperatorType.GREATER);
        registerCondition(ComparableSearchOperatorType.GREATER_OR_EQUALS);
        registerCondition(ComparableSearchOperatorType.LESS);
        registerCondition(ComparableSearchOperatorType.LESS_OR_EQUALS);
        registerCondition(ContainsSearchOperatorType.CONTAINS);
        registerCondition(EqualitySearchOperatorType.EQUALITY);
        registerCondition(EqualitySearchOperatorType.INEQUALITY);
        registerCondition(LikeSearchOperatorType.LIKE);
        registerCondition(LikeSearchOperatorType.NOT_LIKE);
        registerCondition(ReferenceSearchOperatorType.REFERENCE);
        registerCondition(ReferenceSearchOperatorType.NO_REFERENCE);
        registerCondition(ValueSetSearchOperatorType.ALLOWED);
        registerCondition(ValueSetSearchOperatorType.NOT_ALLOWED);
    }

    /**
     * @param productSearchPresentationModel the PMO that provides the conditions to be persisted
     * @param settings the setting to write conditions to, or read from respectively.
     */
    public ConditionPMOPersistence(ProductSearchPresentationModel productSearchPresentationModel,
            IDialogSettings settings) {
        Assert.isNotNull(productSearchPresentationModel);
        Assert.isNotNull(settings);
        this.searchPMO = productSearchPresentationModel;
        this.settings = settings;
    }

    private static void registerCondition(ISearchOperatorType type) {
        OPERATOR_TYPE_MAP.put(type.name(), type);
    }

    /**
     * Loads all conditions saved in the dialog settings and returns them in a list. Returns an
     * empty list, if no conditions could be found.
     */
    public List<ProductSearchConditionPresentationModel> loadConditions() {
        IDialogSettings section = settings.getSection(SECTION_NAME);
        if (section == null) {
            return Collections.emptyList();
        }
        return loadFromSection(section);
    }

    private List<ProductSearchConditionPresentationModel> loadFromSection(IDialogSettings section) {
        String[] conditionTypes = section.getArray(CONDITION_TYPE_KEY);
        String[] operatorTypes = section.getArray(OPERATOR_TYPE_KEY);
        String[] searchedElements = section.getArray(SEARCHED_ELEMENT_KEY);
        String[] arguments = section.getArray(ARGUMENTS_KEY);

        List<ProductSearchConditionPresentationModel> pmoList = new ArrayList<>();
        try {
            for (int i = 0; i < arguments.length; i++) {
                ProductSearchConditionPresentationModel conditionPMO = new ProductSearchConditionPresentationModel(
                        searchPMO);
                conditionPMO.setCondition(getConditionType(conditionTypes[i]));
                conditionPMO.setOperatorType(getOperatorTypeFor(operatorTypes[i]));
                conditionPMO.setSearchedElementByName(searchedElements[i]);
                conditionPMO.setArgument(arguments[i]);
                pmoList.add(conditionPMO);
            }
        } catch (NullPointerException e) {
            return pmoList;
        } catch (ArrayIndexOutOfBoundsException e) {
            return pmoList;
        }
        return pmoList;
    }

    private IConditionType getConditionType(String conditionTypeName) {
        if (POLICY_ATTRIBUTE_CONDITION_TYPE.equals(conditionTypeName)) {
            return new PolicyAttributeConditionType();
        }
        if (PRODUCT_ATTRIBUTE_CONDITION_TYPE.equals(conditionTypeName)) {
            return new ProductAttributeConditionType();
        }
        if (PRODUCT_COMPONENT_ASSOCIATION_CONDITION_TYPE.equals(conditionTypeName)) {
            return new ProductComponentAssociationConditionType();
        }
        return null;
    }

    private ISearchOperatorType getOperatorTypeFor(String typeName) {
        return OPERATOR_TYPE_MAP.get(typeName);
    }

    /**
     * Saves all search conditions of the current {@link ProductSearchPresentationModel} to the
     * dialog settings.
     */
    public void saveConditions() {
        IDialogSettings section = settings.addNewSection(SECTION_NAME);
        section.put(CONDITION_TYPE_KEY, getConditionTypes());
        section.put(OPERATOR_TYPE_KEY, getOperandTypes());
        section.put(ARGUMENTS_KEY, getArguments());
        section.put(SEARCHED_ELEMENT_KEY, getSearchedElements());
    }

    private String[] getConditionTypes() {
        List<String> values = new ArrayList<>();
        List<ProductSearchConditionPresentationModel> conditionPMOs = searchPMO
                .getProductSearchConditionPresentationModels();
        for (ProductSearchConditionPresentationModel condition : conditionPMOs) {
            values.add(condition.getConditionType().getClass().getName());
        }
        return values.toArray(new String[values.size()]);
    }

    private String[] getArguments() {
        List<String> values = new ArrayList<>();
        List<ProductSearchConditionPresentationModel> conditionPMOs = searchPMO
                .getProductSearchConditionPresentationModels();
        for (ProductSearchConditionPresentationModel condition : conditionPMOs) {
            values.add(condition.getArgument());
        }
        return values.toArray(new String[values.size()]);
    }

    private String[] getOperandTypes() {
        List<String> values = new ArrayList<>();
        List<ProductSearchConditionPresentationModel> conditionPMOs = searchPMO
                .getProductSearchConditionPresentationModels();
        for (ProductSearchConditionPresentationModel condition : conditionPMOs) {
            values.add(condition.getOperatorType().toString());
        }
        return values.toArray(new String[values.size()]);
    }

    private String[] getSearchedElements() {
        List<String> values = new ArrayList<>();
        List<ProductSearchConditionPresentationModel> conditionPMOs = searchPMO
                .getProductSearchConditionPresentationModels();
        for (ProductSearchConditionPresentationModel condition : conditionPMOs) {
            values.add(condition.getSearchedElement().getName());
        }
        return values.toArray(new String[values.size()]);
    }

}
