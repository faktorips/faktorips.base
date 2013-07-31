/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;

public class AggregateRootFinder {

    private Set<String> nonRootCmpts = new HashSet<String>();
    private Set<String> potentialRootCmpts = new HashSet<String>();

    private final IIpsProject ipsProject;

    public AggregateRootFinder(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
    }

    public List<IProductCmpt> findAggregateRoots() {
        resetIndices();
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
        indexProductComponents(prodCmpts);
        return getIndexedRootCmpts();
    }

    protected void indexProductComponents(List<IProductCmpt> prodCmpts) {
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
        potentialRootCmpts.add(prodCmpt.getQualifiedName());
    }

    private void processLinks(IProductCmpt prodCmpt) {
        List<IProductCmptLink> links = prodCmpt.getLinksAsList();
        for (IProductCmptLink link : links) {
            processTarget(prodCmpt, link);
        }
    }

    protected void processTarget(IProductCmpt prodCmpt, IProductCmptLink link) {
        if (isComposition(link)) {
            String targetName = link.getTarget();
            removeFromPotentialRoots(prodCmpt, targetName);
        }
    }

    private boolean isComposition(IProductCmptLink link) {
        try {
            return link.findAssociation(getIpsProject()).is1ToManyIgnoringQualifier();
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
        List<IProductCmpt> rootCmpts = new ArrayList<IProductCmpt>();
        for (String rootQName : potentialRootCmpts) {
            IProductCmpt cmpt = findProductCmpt(rootQName);
            rootCmpts.add(cmpt);
        }
        return rootCmpts;
    }

    protected IProductCmpt findProductCmpt(String rootQName) {
        try {
            IProductCmpt cmpt = getIpsProject().findProductCmpt(rootQName);
            return cmpt;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private IIpsProject getIpsProject() {
        return ipsProject;
    }
}
