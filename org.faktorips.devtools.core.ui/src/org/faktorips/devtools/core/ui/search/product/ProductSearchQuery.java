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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.search.AbstractIpsSearchQuery;

public class ProductSearchQuery extends AbstractIpsSearchQuery<ProductSearchPresentationModel> {

    public ProductSearchQuery(ProductSearchPresentationModel model, IIpsModel ipsModel) {
        super(model, ipsModel);
    }

    public ProductSearchQuery(ProductSearchPresentationModel model) {
        this(model, IpsPlugin.getDefault().getIpsModel());
    }

    @Override
    public String getLabel() {
        return "Faktor-IPS Product Search";
    }

    @Override
    protected void searchDetails() throws CoreException {
        // da gibt es noch nix

    }

    @Override
    protected Set<IIpsSrcFile> getSelectedSrcFiles() throws CoreException {
        Set<IIpsSrcFile> selectedSrcFiles = super.getSelectedSrcFiles();

        Set<IIpsSrcFile> matchingProductComponents = new HashSet<IIpsSrcFile>();

        IIpsProject[] productDefinitionProjects = ipsModel.getIpsProductDefinitionProjects();
        for (IIpsProject project : productDefinitionProjects) {
            matchingProductComponents.addAll(Arrays.asList(project.findAllProductCmptSrcFiles(
                    searchModel.getProductCmptType(), true)));
        }

        selectedSrcFiles.retainAll(matchingProductComponents);

        return selectedSrcFiles;
    }

    @Override
    protected boolean isJustTypeNameSearch() {
        return true;
    }

    @Override
    protected List<IpsObjectType> getIpsObjectTypeFilter() {
        return Collections.singletonList(IpsObjectType.PRODUCT_CMPT);
    }

    @Override
    public String getResultLabel(int matchCount) {
        return matchCount + " Hits for " + searchModel.getProductCmptType().getName();
    }

}
