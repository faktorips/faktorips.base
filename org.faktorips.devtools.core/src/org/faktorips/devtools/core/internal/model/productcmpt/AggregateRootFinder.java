/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.AssociationType;

/**
 * Utility class that searches a specific project for so called "aggregate roots". An
 * "aggregate root" is a product component that has no parent. In other words, there is no
 * master-to-detail relation (composition) between any product component and an aggregate root. Thus
 * each aggregate root is the root of a product component structure (a.k.a. aggregate).
 * 
 * This class searches a given project and all dependent projects for all contained product
 * components. Using only the product components and the composition-relations between them, the
 * aggregate roots are calculated. Only compositions and the tree-like structure they define can be
 * used for this purpose, associations are ignored.
 * 
 */
public class AggregateRootFinder {

    private final Set<String> nonRootCmpts = new HashSet<String>();
    private final Map<String, IProductCmpt> potentialRootCmpts = new ConcurrentHashMap<String, IProductCmpt>();

    private final IIpsProject ipsProject;

    public AggregateRootFinder(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
    }

    /**
     * Every call of this method calculates the aggregate roots in the specified project. For
     * projects with many product components or highly linked product structures, this is a costly
     * operation.
     */
    public List<IProductCmpt> findAggregateRoots() {
        List<IIpsSrcFile> prodCmptSrcFiles = getIpsProject().findAllIpsSrcFiles(IpsObjectType.PRODUCT_CMPT);
        return findAggregateRootsFromSrcFiles(prodCmptSrcFiles);
    }

    protected void resetIndices() {
        nonRootCmpts.clear();
        potentialRootCmpts.clear();
    }

    private List<IProductCmpt> findAggregateRootsFromSrcFiles(List<IIpsSrcFile> prodCmptSrcFiles) {
        List<IProductCmpt> prodCmpts = getProductCmpts(prodCmptSrcFiles);
        return findAggregateRoots(prodCmpts);
    }

    protected List<IProductCmpt> getProductCmpts(List<IIpsSrcFile> prodCmptSrcFiles) {
        List<IProductCmpt> prodCmpts = new ArrayList<IProductCmpt>();
        try {
            for (IIpsSrcFile srcFile : prodCmptSrcFiles) {
                IProductCmpt productCmpt;
                productCmpt = (IProductCmpt)srcFile.getIpsObject();
                prodCmpts.add(productCmpt);
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return prodCmpts;
    }

    private List<IProductCmpt> findAggregateRoots(List<IProductCmpt> prodCmpts) {
        resetIndices();
        indexAllProductComponents(prodCmpts);
        return getIndexedRootCmpts();
    }

    protected void indexAllProductComponents(List<IProductCmpt> prodCmpts) {
        for (IProductCmpt prodCmpt : prodCmpts) {
            if (!isRegisteredChild(prodCmpt)) {
                registerAsPotentialRoot(prodCmpt);
            }
            processLinks(prodCmpt);
        }
    }

    protected boolean isRegisteredChild(IProductCmpt prodCmpt) {
        String qName = prodCmpt.getQualifiedName();
        return nonRootCmpts.contains(qName);
    }

    protected void registerAsPotentialRoot(IProductCmpt prodCmpt) {
        potentialRootCmpts.put(prodCmpt.getQualifiedName(), prodCmpt);
    }

    private void processLinks(IProductCmpt prodCmpt) {
        List<IProductCmptLink> links = getChangingAndStaticLinksFor(prodCmpt);
        for (IProductCmptLink link : links) {
            processTarget(prodCmpt, link);
        }
    }

    protected List<IProductCmptLink> getChangingAndStaticLinksFor(IProductCmpt prodCmpt) {
        List<IProductCmptLink> allLinks = prodCmpt.getLinksAsList();
        for (IProductCmptGeneration gen : prodCmpt.getProductCmptGenerations()) {
            allLinks.addAll(gen.getLinksAsList());
        }
        return allLinks;
    }

    protected void processTarget(IProductCmpt prodCmpt, IProductCmptLink link) {
        if (isComposition(link)) {
            String targetName = link.getTarget();
            removeFromPotentialRoots(prodCmpt, targetName);
        }
    }

    private boolean isComposition(IProductCmptLink link) {
        try {
            IProductCmptTypeAssociation association = link.findAssociation(getIpsProject());
            return association.getAssociationType() == AssociationType.AGGREGATION;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private void removeFromPotentialRoots(IProductCmpt sourceProdCmpt, String targetName) {
        if (!isSelfReferencing(sourceProdCmpt, targetName)) {
            removeFromPotentialRoots(targetName);
        }
    }

    protected boolean isSelfReferencing(IProductCmpt prodCmpt, String targetName) {
        return prodCmpt.getQualifiedName().equals(targetName);
    }

    protected void removeFromPotentialRoots(String targetName) {
        potentialRootCmpts.remove(targetName);
        nonRootCmpts.add(targetName);
    }

    private List<IProductCmpt> getIndexedRootCmpts() {
        List<IProductCmpt> rootCmpts = new ArrayList<IProductCmpt>(potentialRootCmpts.values());
        return rootCmpts;
    }

    private IIpsProject getIpsProject() {
        return ipsProject;
    }
}
