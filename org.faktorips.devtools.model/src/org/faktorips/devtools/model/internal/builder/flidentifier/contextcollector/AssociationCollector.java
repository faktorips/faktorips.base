/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.builder.flidentifier.contextcollector;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.AssociationNode;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IType;

/**
 * This collector is used to collect the product components that may be used as qualifiers by the
 * current {@link AssociationNode}
 * 
 * @author dirmeier
 */
public class AssociationCollector extends AbstractProductCmptCollector {

    protected AssociationCollector(AssociationNode node, ContextProductCmptFinder finder) {
        super(node, finder);
    }

    @Override
    public AssociationNode getNode() {
        return (AssociationNode)super.getNode();
    }

    private IAssociation getAssociation() {
        return getNode().getAssociation();
    }

    @Override
    protected Set<IProductCmpt> getContextProductCmpts() {
        Set<IProductCmpt> contextProductCmpts = getPreviousContextProductCmpts();
        if (contextProductCmpts == null) {
            return getAllProductCmpts();
        } else {
            return getLinkedProductCmpts(contextProductCmpts);
        }
    }

    private Set<IProductCmpt> getAllProductCmpts() {
        LinkedHashSet<IProductCmpt> result = new LinkedHashSet<>();
        try {
            IType target = getAssociation().findTarget(getIpsProject());
            if (target instanceof IPolicyCmptType) {
                IPolicyCmptType policyCmptType = (IPolicyCmptType)target;
                IProductCmptType productCmptType = policyCmptType.findProductCmptType(getIpsProject());
                Collection<IIpsSrcFile> productComponents = productCmptType.searchProductComponents(true);
                for (IIpsSrcFile ipsSrcFile : productComponents) {
                    result.add((IProductCmpt)ipsSrcFile.getIpsObject());
                }
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return result;
    }

    private Set<IProductCmpt> getLinkedProductCmpts(Set<IProductCmpt> contextProductCmpts) {
        Set<IProductCmpt> newContextCmpts = new LinkedHashSet<>();
        try {
            IAssociation matchingAssociation = getAssociation().findMatchingAssociation();
            if (matchingAssociation == null) {
                return null;
            }
            for (IProductCmpt productCmpt : contextProductCmpts) {
                addLinkedProductCmpts(productCmpt, matchingAssociation, newContextCmpts);
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return newContextCmpts;
    }

    private void addLinkedProductCmpts(IProductCmpt productCmpt,
            IAssociation matchingAssociation,
            Set<IProductCmpt> newContextCmpts) throws CoreException {
        List<IProductCmptLink> links = getLinks(productCmpt, matchingAssociation.getName());
        for (IProductCmptLink cmptLink : links) {
            newContextCmpts.add(cmptLink.findTarget(getIpsProject()));
        }
    }

    private List<IProductCmptLink> getLinks(IProductCmpt productCmpt, String productAssociationName) {
        List<IProductCmptLink> links = productCmpt.getLinksAsList(productAssociationName);
        if (!links.isEmpty()) {
            return links;
        } else {
            IProductCmptGeneration generation = getGeneration(productCmpt);
            if (generation != null) {
                return generation.getLinksAsList(productAssociationName);
            }
        }
        return Collections.emptyList();
    }

    private IProductCmptGeneration getGeneration(IProductCmpt productCmpt) {
        IProductCmptGeneration generationEffectiveOn = productCmpt.getGenerationEffectiveOn(getValidFrom());
        if (generationEffectiveOn != null) {
            return generationEffectiveOn;
        } else {
            return productCmpt.getLatestProductCmptGeneration();
        }
    }

}
