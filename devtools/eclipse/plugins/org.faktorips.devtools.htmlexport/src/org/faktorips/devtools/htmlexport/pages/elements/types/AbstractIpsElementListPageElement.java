/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.helper.IpsObjectTypeComparator;
import org.faktorips.devtools.htmlexport.helper.filter.IIpsElementFilter;
import org.faktorips.devtools.htmlexport.helper.path.HtmlPathFactory;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;

/**
 * Creates a list with links to the pages of the given {@link IIpsObject}s. The {@link IIpsObject}s
 * will be filtered and sorted on the page.
 * 
 * @author dicker
 * 
 */
public abstract class AbstractIpsElementListPageElement extends AbstractRootPageElement {

    /**
     * {@link IIpsElementFilter}, which accepts all {@link IIpsElement}s
     */
    protected static final IIpsElementFilter ALL_FILTER = $ -> true;

    /**
     * {@link Comparator}, which is used for sorting the {@link IIpsObject}s according to their
     * {@link IpsObjectType} and then their unqualified name.
     */
    protected static final Comparator<IIpsSrcFile> IPS_OBJECT_COMPARATOR = (o1, o2) -> {
        IpsObjectTypeComparator ipsObjectTypeComparator = new IpsObjectTypeComparator();

        int comparationIpsObjectType = ipsObjectTypeComparator
                .compare(o1.getIpsObjectType(), o2.getIpsObjectType());

        if (comparationIpsObjectType == 0) {
            return o1.getIpsObjectName().compareTo(o2.getIpsObjectName());
        }

        return comparationIpsObjectType;
    };

    private IIpsElement baseIpsElement;
    private List<IIpsSrcFile> srcFiles;
    private TargetType linkTarget;
    private IIpsElementFilter filter = ALL_FILTER;

    /**
     * creates an {@link AbstractIpsElementListPageElement}
     * 
     * @param baseIpsElement ipsElement, which represents the location of the page for links from
     *            the page
     * @param srcFiles unfiltered and unsorted objects to list on the page
     * @param filter for objects
     */
    public AbstractIpsElementListPageElement(IIpsElement baseIpsElement, List<IIpsSrcFile> srcFiles,
            IIpsElementFilter filter, DocumentationContext context) {
        super(context);
        this.baseIpsElement = baseIpsElement;
        this.srcFiles = srcFiles;
        this.filter = filter;
    }

    /**
     * creates an {@link AbstractIpsElementListPageElement}
     * 
     * @param baseIpsElement ipsElement, which represents the location of the page for links from
     *            the page
     * @param srcFiles objects to list on the page
     */
    public AbstractIpsElementListPageElement(IIpsElement baseIpsElement, List<IIpsSrcFile> srcFiles,
            DocumentationContext context) {
        this(baseIpsElement, srcFiles, ALL_FILTER, context);
    }

    /**
     * @return the {@link IIpsPackageFragment}s of all filtered objects
     */
    protected Set<IIpsPackageFragment> getRelatedPackageFragments() {
        Set<IIpsPackageFragment> packageFragments = new LinkedHashSet<>();
        for (IIpsSrcFile object : srcFiles) {
            if (!filter.accept(object)) {
                continue;
            }
            packageFragments.add(object.getIpsPackageFragment());
        }
        return packageFragments;
    }

    /**
     * @return the {@link IpsObjectType}s of all filtered objects
     */
    protected Set<IpsObjectType> getRelatedObjectTypes() {
        Set<IpsObjectType> packageFragments = new LinkedHashSet<>();
        for (IIpsSrcFile object : getSrcFiles()) {
            if (!getFilter().accept(object)) {
                continue;
            }
            packageFragments.add(object.getIpsObjectType());
        }
        return packageFragments;
    }

    public IIpsElement getBaseIpsElement() {
        return baseIpsElement;
    }

    public List<IIpsSrcFile> getSrcFiles() {
        return srcFiles;
    }

    public IIpsElementFilter getFilter() {
        return filter;
    }

    /**
     * @return the target for all links
     */
    public TargetType getLinkTarget() {
        return linkTarget;
    }

    /**
     * sets the target for all links
     * 
     */
    public void setLinkTarget(TargetType linkTarget) {
        this.linkTarget = linkTarget;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement#getPathToRoot()
     */
    @Override
    public String getPathToRoot() {
        return HtmlPathFactory.createPathUtil(baseIpsElement).getPathToRoot();
    }

    @Override
    public boolean isContentUnit() {
        return false;
    }
}
