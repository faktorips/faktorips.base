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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.search.ui.text.Match;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.search.AbstractIpsSearchQuery;
import org.faktorips.devtools.core.ui.search.product.conditions.IOperandProvider;
import org.faktorips.devtools.core.ui.search.product.conditions.ISearchOperator;
import org.faktorips.devtools.core.ui.search.product.conditions.ISearchOperatorType;

public class ProductSearchQuery extends AbstractIpsSearchQuery<ProductSearchPresentationModel> {

    public ProductSearchQuery(ProductSearchPresentationModel model, IIpsModel ipsModel) {
        super(model, ipsModel);
    }

    public ProductSearchQuery(ProductSearchPresentationModel model) {
        this(model, IpsPlugin.getDefault().getIpsModel());
    }

    @Override
    public String getLabel() {
        return Messages.ProductSearchQuery_faktorIpsProductSearchLabel;
    }

    @Override
    protected void searchDetails() throws CoreException {
        Set<IIpsSrcFile> matchingSrcFiles = getMatchingSrcFiles();

        List<ISearchOperator> searchOperators = new ArrayList<ISearchOperator>();
        List<ProductSearchConditionPresentationModel> models = searchModel
                .getProductSearchConditionPresentationModels();
        for (ProductSearchConditionPresentationModel conditionModel : models) {
            if (conditionModel.isValid()) {

                ISearchOperator searchOperator = createSearchOperator(conditionModel);

                searchOperators.add(searchOperator);
            }
        }

        for (IIpsSrcFile srcFile : matchingSrcFiles) {
            searchDetailProductCmptGenerations(srcFile, searchOperators);
        }
    }

    protected ISearchOperator createSearchOperator(ProductSearchConditionPresentationModel conditionModel) {

        IOperandProvider operandProvider = conditionModel.getCondition().createOperandProvider(
                conditionModel.getSearchedElement());
        ISearchOperatorType operatorType = conditionModel.getOperatorType();

        ISearchOperator searchOperator = operatorType.createSearchOperator(operandProvider,
                conditionModel.getValueDatatype(), conditionModel.getArgument());

        return searchOperator;
    }

    private void searchDetailProductCmptGenerations(IIpsSrcFile srcFile, List<ISearchOperator> searchOperators)
            throws CoreException {
        if (!IpsObjectType.PRODUCT_CMPT.equals(srcFile.getIpsObjectType())) {
            return;
        }

        IProductCmpt productComponent = (IProductCmpt)srcFile.getIpsObject();

        List<IProductCmptGeneration> generations = productComponent.getProductCmptGenerations();

        for (IProductCmptGeneration productCmptGeneration : generations) {
            if (isMatchingProductCmptGeneration(searchOperators, productCmptGeneration)) {
                searchResult.addMatch(new Match(productCmptGeneration, 0, 0));
            }
        }
    }

    protected boolean isMatchingProductCmptGeneration(List<ISearchOperator> searchOperators,
            IProductCmptGeneration productCmptGeneration) {

        for (ISearchOperator searchOperator : searchOperators) {
            if (!searchOperator.check(productCmptGeneration)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected Set<IIpsSrcFile> getSelectedSrcFiles() throws CoreException {
        Set<IIpsSrcFile> selectedSrcFiles = super.getSelectedSrcFiles();
        Set<IIpsSrcFile> matchingProductComponents = new HashSet<IIpsSrcFile>();

        IIpsProject[] productDefinitionProjects = ipsModel.getIpsProductDefinitionProjects();
        for (IIpsProject project : productDefinitionProjects) {

            IProductCmptType type = project.findProductCmptType(searchModel.getProductCmptType().getQualifiedName());

            if (type != null) {
                List<IIpsSrcFile> asList = Arrays.asList(project.findAllProductCmptSrcFiles(type, true));
                matchingProductComponents.addAll(asList);
            }
        }

        selectedSrcFiles.retainAll(matchingProductComponents);

        return selectedSrcFiles;
    }

    @Override
    protected boolean isJustTypeNameSearch() {
        for (ProductSearchConditionPresentationModel conditionModel : searchModel
                .getProductSearchConditionPresentationModels()) {
            if (conditionModel.isValid()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected List<IpsObjectType> getIpsObjectTypeFilter() {
        return Collections.singletonList(IpsObjectType.PRODUCT_CMPT);
    }

    @Override
    public String getResultLabel(int matchCount) {
        return matchCount + Messages.ProductSearchQuery_1 + searchModel.getProductCmptType().getQualifiedName();
    }

}
