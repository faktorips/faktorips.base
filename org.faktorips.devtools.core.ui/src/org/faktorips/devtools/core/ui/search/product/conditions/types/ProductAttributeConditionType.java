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

package org.faktorips.devtools.core.ui.search.product.conditions.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAttribute;
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
 * </ul>
 * 
 * @author dicker
 */
public class ProductAttributeConditionType extends AbstractAttributeConditionType {

    private static final class ProductAttributeArgumentProvider implements IOperandProvider {
        private final IAttribute attribute;

        public ProductAttributeArgumentProvider(IAttribute attribute) {
            this.attribute = attribute;
        }

        @Override
        public String getSearchOperand(IProductCmptGeneration productComponentGeneration) {
            IAttributeValue attributeValue = productComponentGeneration.getAttributeValue(attribute.getName());

            // null-check necessary, because attributeValue maybe null if a product component is not
            // migrated
            if (attributeValue == null) {
                return null;
            }

            return attributeValue.getValue();
        }
    }

    @Override
    public List<IIpsElement> getSearchableElements(IProductCmptType element) {
        try {
            return new ArrayList<IIpsElement>(element.findAllAttributes(element.getIpsProject()));
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    public List<ISearchOperatorType> getSearchOperatorTypes(IIpsElement searchableElement) {
        List<ISearchOperatorType> searchOperatorTypes = new ArrayList<ISearchOperatorType>();

        ValueDatatype valueDatatype = getValueDatatype(searchableElement);

        if (String.class.getName().equals(valueDatatype.getJavaClassName())) {
            searchOperatorTypes.addAll(Arrays.asList(LikeSearchOperatorType.values()));
        }

        searchOperatorTypes.addAll(Arrays.asList(EqualitySearchOperatorType.values()));

        if (valueDatatype.supportsCompare()) {
            searchOperatorTypes.addAll(Arrays.asList(ComparableSearchOperatorType.values()));
        }

        return searchOperatorTypes;
    }

    @Override
    public IOperandProvider createOperandProvider(IIpsElement elementPart) {
        return new ProductAttributeArgumentProvider((IAttribute)elementPart);
    }

    @Override
    public String getName() {
        return Messages.ProductAttributeCondition_conditionName;
    }
}
