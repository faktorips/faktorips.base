/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductPartsContainer;
import org.faktorips.devtools.core.ui.search.product.conditions.table.ProductSearchConditionPresentationModel;
import org.faktorips.devtools.core.ui.search.product.conditions.types.IOperandProvider;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ISearchOperator;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ISearchOperatorType;

class ProductSearchQueryConditionMatcher {

    private final ProductSearchPresentationModel searchModel;
    private final List<ISearchOperator> searchOperators;

    public ProductSearchQueryConditionMatcher(ProductSearchPresentationModel searchModel) {
        this.searchModel = searchModel;
        this.searchOperators = createSearchOperators();
    }

    protected ProductSearchQueryConditionMatcher(ProductSearchPresentationModel searchModel,
            List<ISearchOperator> searchOperators) {
        this.searchModel = searchModel;
        this.searchOperators = searchOperators;
    }

    private List<ISearchOperator> createSearchOperators() {
        List<ISearchOperator> newSearchOperators = new ArrayList<ISearchOperator>();
        List<ProductSearchConditionPresentationModel> models = searchModel
                .getProductSearchConditionPresentationModels();
        for (ProductSearchConditionPresentationModel conditionModel : models) {
            if (conditionModel.isValid()) {
                ISearchOperator searchOperator = createSearchOperator(conditionModel);
                newSearchOperators.add(searchOperator);
            }
        }
        return newSearchOperators;
    }

    private ISearchOperator createSearchOperator(ProductSearchConditionPresentationModel conditionModel) {

        IOperandProvider operandProvider = conditionModel.getConditionType().createOperandProvider(
                conditionModel.getSearchedElement());
        ISearchOperatorType operatorType = conditionModel.getOperatorType();

        ISearchOperator searchOperator = operatorType.createSearchOperator(operandProvider,
                conditionModel.getValueDatatype(), conditionModel.getArgument());

        return searchOperator;
    }

    protected Set<IProductPartsContainer> getResults(Set<IIpsSrcFile> matchingSrcFiles) {
        Set<IProductPartsContainer> productPartsContainers = new HashSet<IProductPartsContainer>();
        for (IIpsSrcFile srcFile : matchingSrcFiles) {
            addMatchingProductPartContainers(srcFile, productPartsContainers);
        }
        return productPartsContainers;
    }

    private void addMatchingProductPartContainers(IIpsSrcFile srcFile,
            Set<IProductPartsContainer> productPartsContainers) {
        if (isProductCmpt(srcFile)) {
            processProdCmpt(srcFile, productPartsContainers);
        }
    }

    private boolean isProductCmpt(IIpsSrcFile srcFile) {
        return IpsObjectType.PRODUCT_CMPT.equals(srcFile.getIpsObjectType());
    }

    private void processProdCmpt(IIpsSrcFile srcFile, Set<IProductPartsContainer> productPartsContainers) {
        IProductCmpt productComponent = getProdCmptFrom(srcFile);
        addMatchingProdCmpt(productPartsContainers, productComponent);
        addMatchingGenerations(productPartsContainers, productComponent);
    }

    private IProductCmpt getProdCmptFrom(IIpsSrcFile srcFile) {
        try {
            return (IProductCmpt)srcFile.getIpsObject();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private void addMatchingProdCmpt(Set<IProductPartsContainer> productPartsContainers, IProductCmpt productComponent) {
        if (isMatchingContainer(productComponent)) {
            productPartsContainers.add(productComponent);
        }
    }

    private boolean isMatchingContainer(IProductPartsContainer productPartsContainer) {
        for (ISearchOperator searchOperator : searchOperators) {
            if (!searchOperator.check(productPartsContainer)) {
                return false;
            }
        }
        return true;
    }

    private void addMatchingGenerations(Set<IProductPartsContainer> productPartsContainers,
            IProductCmpt productComponent) {
        List<IProductCmptGeneration> generations = productComponent.getProductCmptGenerations();
        for (IProductCmptGeneration productCmptGeneration : generations) {
            if (isMatchingContainer(productCmptGeneration)) {
                productPartsContainers.add(productCmptGeneration);
            }
        }
    }

    List<ISearchOperator> getSearchOperators() {
        return searchOperators;
    }
}
