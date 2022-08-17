/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product.conditions.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductPartsContainer;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.runtime.IProductComponent;

/**
 * A condition for {@link IAttribute IAttributes} of a {@link IProductComponent}.
 * <p>
 * The condition tests, whether the value of an attribute of a IProductComponent (within a
 * {@link IProductCmptGeneration}) matches a given argument.
 * <p>
 * The ProductAttributeConditionType uses
 * <ul>
 * <li>the {@link EqualitySearchOperatorType EqualitySearchOperatorTypes}</li>
 * <li>the {@link LikeSearchOperatorType LikeSearchOperatorTypes}, if the {@link ValueDatatype} of
 * the {@link IAttribute} is a {@link String}</li>
 * <li>the {@link ComparableSearchOperatorType ComparableSearchOperatorTypes}, if the
 * {@link ValueDatatype} of the {@link IAttribute} is a {@link Comparable}</li>
 * <li>the {@link ContainsSearchOperator ContainsSearchOperator}, if the {@link IAttribute} is a
 * {@link IProductCmptTypeAttribute multiValueAttribute}</li>
 * </ul>
 * 
 * @author dicker
 */
public class ProductAttributeConditionType extends AbstractAttributeConditionType {

    @Override
    public List<IIpsElement> getSearchableElements(IProductCmptType element) {
        return new ArrayList<>(element.findAllAttributes(element.getIpsProject()));
    }

    @Override
    public List<ISearchOperatorType> getSearchOperatorTypes(IIpsElement searchableElement) {
        List<ISearchOperatorType> searchOperatorTypes = new ArrayList<>();

        ValueDatatype valueDatatype = getValueDatatype(searchableElement);

        if (ValueDatatype.STRING.equals(valueDatatype)) {
            searchOperatorTypes.addAll(Arrays.asList(LikeSearchOperatorType.values()));
        }

        searchOperatorTypes.addAll(Arrays.asList(EqualitySearchOperatorType.values()));

        if (valueDatatype.supportsCompare()) {
            searchOperatorTypes.addAll(Arrays.asList(ComparableSearchOperatorType.values()));
        }

        if (isMultiValueAttribute(searchableElement)) {
            searchOperatorTypes.clear();
            searchOperatorTypes.addAll(Arrays.asList(ContainsSearchOperatorType.values()));
        }

        return searchOperatorTypes;
    }

    private boolean isMultiValueAttribute(IIpsElement searchableElement) {
        return searchableElement instanceof IProductCmptTypeAttribute
                && ((IProductCmptTypeAttribute)searchableElement).isMultiValueAttribute();
    }

    @Override
    public IOperandProvider createOperandProvider(IIpsElement elementPart) {
        return new ProductAttributeArgumentProvider((IAttribute)elementPart);
    }

    @Override
    public String getName() {
        return Messages.ProductAttributeCondition_conditionName;
    }

    private static final class ProductAttributeArgumentProvider implements IOperandProvider {
        private final IAttribute attribute;

        public ProductAttributeArgumentProvider(IAttribute attribute) {
            this.attribute = attribute;
        }

        @Override
        public Object getSearchOperand(IProductPartsContainer productPartsContainer) {
            List<IAttributeValue> attributeValues = productPartsContainer.getProductParts(IAttributeValue.class);
            for (IAttributeValue attributeValue : attributeValues) {
                if (attributeValue.getAttribute().equals(attribute.getName())) {
                    return attributeValue.getValueHolder();
                }
            }
            return null;
        }
    }
}
