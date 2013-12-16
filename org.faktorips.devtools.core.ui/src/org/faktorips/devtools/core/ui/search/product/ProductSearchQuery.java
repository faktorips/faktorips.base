/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product;

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
import org.faktorips.devtools.core.model.productcmpt.IProductPartsContainer;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.search.AbstractIpsSearchQuery;
import org.faktorips.devtools.core.ui.search.IpsSearchResult;
import org.faktorips.devtools.core.ui.search.product.conditions.table.ProductSearchConditionPresentationModel;

/**
 * Contains the logic of the Faktor-IPS Product Search. It contains a
 * {@link ProductSearchPresentationModel} with the conditions for the search and the result is
 * stored in the {@link IpsSearchResult}
 * <p>
 * 
 * @author dicker
 */
public class ProductSearchQuery extends AbstractIpsSearchQuery<ProductSearchPresentationModel> {

    public ProductSearchQuery(ProductSearchPresentationModel model) {
        this(model, IpsPlugin.getDefault().getIpsModel());
    }

    protected ProductSearchQuery(ProductSearchPresentationModel model, IIpsModel ipsModel) {
        super(model, ipsModel);

    }

    @Override
    public String getLabel() {
        return Messages.ProductSearchQuery_faktorIpsProductSearchLabel;
    }

    @Override
    protected void searchDetails() throws CoreException {
        ProductSearchQueryConditionMatcher resultBuilder = new ProductSearchQueryConditionMatcher(getSearchModel());
        Set<IProductPartsContainer> results = resultBuilder.getResults(getMatchingSrcFiles());

        for (IProductPartsContainer productPartsContainer : results) {
            getSearchResult().addMatch(new Match(productPartsContainer, 0, 0));
        }
    }

    @Override
    protected Set<IIpsSrcFile> getSelectedSrcFiles() throws CoreException {
        Set<IIpsSrcFile> selectedSrcFiles = super.getSelectedSrcFiles();

        if (selectedSrcFiles.isEmpty()) {
            return Collections.emptySet();
        }

        Set<IIpsSrcFile> instancesOfProductComponentType = getAllInstancesOfProductCmptType(getSearchModel()
                .getProductCmptType());

        selectedSrcFiles.retainAll(instancesOfProductComponentType);

        return selectedSrcFiles;
    }

    private Set<IIpsSrcFile> getAllInstancesOfProductCmptType(IProductCmptType productCmptType) throws CoreException {
        Set<IIpsSrcFile> instancesOfProductComponentType = new HashSet<IIpsSrcFile>();

        IIpsProject[] productDefinitionProjects = getIpsModel().getIpsProductDefinitionProjects();
        for (IIpsProject project : productDefinitionProjects) {

            IProductCmptType type = project.findProductCmptType(productCmptType.getQualifiedName());

            if (type != null) {
                List<IIpsSrcFile> asList = Arrays.asList(project.findAllProductCmptSrcFiles(type, true));
                instancesOfProductComponentType.addAll(asList);
            }
        }
        return instancesOfProductComponentType;
    }

    @Override
    protected boolean isOnlyTypeNameSearch() {
        for (ProductSearchConditionPresentationModel conditionModel : getSearchModel()
                .getProductSearchConditionPresentationModels()) {
            if (conditionModel.isValid()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected List<IpsObjectType> getAllowedIpsObjectTypes() {
        return Collections.singletonList(IpsObjectType.PRODUCT_CMPT);
    }

    @Override
    public String getResultLabel(int matchCount) {
        return matchCount + Messages.ProductSearchQuery_1 + getSearchModel().getProductCmptType().getQualifiedName();
    }

    // overwritten just for testing purposes
    @Override
    protected Set<IIpsSrcFile> getMatchingSrcFiles() throws CoreException {
        return super.getMatchingSrcFiles();
    }

}
