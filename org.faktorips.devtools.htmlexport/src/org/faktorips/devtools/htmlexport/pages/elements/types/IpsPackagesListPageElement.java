/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.helper.filter.IIpsElementFilter;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.types.chooser.TypeChooserPageElement;

/**
 * Lists and links the packages of the given {@link IpsObject}s in a page
 * 
 * @author dicker
 * 
 */
public class IpsPackagesListPageElement extends AbstractIpsElementListPageElement {

    /**
     * Comparator, which support the sorting of packages by name
     */
    private Comparator<IIpsSrcFile> packagesComparator = new Comparator<IIpsSrcFile>() {
        @Override
        public int compare(IIpsSrcFile arg0, IIpsSrcFile arg1) {
            return arg0.getIpsPackageFragment().getName().compareTo(arg1.getIpsPackageFragment().getName());
        }
    };

    /**
     * @see AbstractIpsElementListPageElement
     */
    public IpsPackagesListPageElement(IIpsElement baseIpsElement, List<IIpsSrcFile> srcFiles, IIpsElementFilter filter,
            DocumentationContext context) {
        super(baseIpsElement, srcFiles, filter, context);
        setTitle(getContext().getMessage(HtmlExportMessages.IpsPackagesListPageElement_allPackages));
    }

    /**
     * @see AbstractIpsElementListPageElement
     */
    public IpsPackagesListPageElement(IIpsElement baseIpsElement, List<IIpsSrcFile> srcFiles,
            DocumentationContext context) {
        this(baseIpsElement, srcFiles, ALL_FILTER, context);
    }

    @Override
    protected void buildInternal() {
        super.buildInternal();
        addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.IpsPackagesListPageElement_headlineObjectTypes), TextType.HEADING_2, getContext()));

        addPageElements(new TypeChooserPageElement(getContext()));

        addPageElements(new TextPageElement(getTitle(), TextType.HEADING_2, getContext()));

        List<IPageElement> list = createPackageList();

        addPageElements(new TextPageElement(
                list.size() + " " + getContext().getMessage(HtmlExportMessages.IpsPackagesListPageElement_packages), TextType.BLOCK, getContext())); //$NON-NLS-1$

        if (list.size() > 0) {
            addPageElements(new ListPageElement(list, getContext()));
        }
    }

    private List<IPageElement> createPackageList() {

        Collections.sort(getSrcFiles(), packagesComparator);

        Set<IIpsPackageFragment> packageFragments = getRelatedPackageFragments();

        List<IPageElement> packageLinks = new ArrayList<IPageElement>();
        Set<String> linkedPackagesNames = new HashSet<String>();

        for (IIpsPackageFragment packageFragment : packageFragments) {
            if (!getFilter().accept(packageFragment)) {
                continue;
            }
            if (linkedPackagesNames.contains(packageFragment.getName())) {
                continue;
            }

            linkedPackagesNames.add(packageFragment.getName());
            packageLinks.add(new PageElementUtils(getContext()).createLinkPageElement(getContext(), packageFragment,
                    getLinkTarget(), IpsUIPlugin.getLabel(packageFragment), true));
        }

        return packageLinks;
    }
}
