package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.helper.DocumentorUtil;
import org.faktorips.devtools.htmlexport.helper.filter.IpsElementFilter;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;

/**
 * Lists and links the packages of the given {@link IpsObject}s in a page
 * 
 * @author dicker
 * 
 */
public class IpsPackagesListPageElement extends AbstractListPageElement {

    /*
     * Comparator, which support the sorting of packages by name
     */
    private Comparator<IIpsObject> packagesComparator = new Comparator<IIpsObject>() {
        public int compare(IIpsObject arg0, IIpsObject arg1) {
            return arg0.getIpsPackageFragment().getName().compareTo(arg1.getIpsPackageFragment().getName());
        }
    };

    /**
     * @see AbstractListPageElement#AbstractListPageElement(IIpsElement, List, IpsElementFilter)
     * @param baseIpsElement
     * @param objects
     * @param filter
     * @param config
     */
    public IpsPackagesListPageElement(IIpsElement baseIpsElement, List<IIpsObject> objects, IpsElementFilter filter,
            DocumentorConfiguration config) {
        super(baseIpsElement, objects, filter, config);
        setTitle(Messages.IpsPackagesListPageElement_allPackages);
    }

    /**
     * @see AbstractListPageElement#AbstractListPageElement(IIpsElement, List)
     * @param baseIpsElement
     * @param objects
     * @param config
     */
    public IpsPackagesListPageElement(IIpsElement baseIpsElement, List<IIpsObject> objects,
            DocumentorConfiguration config) {
        this(baseIpsElement, objects, ALL_FILTER, config);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement#build()
     */
    @Override
    public void build() {
        super.build();
        addPageElements(new TextPageElement(getTitle(), TextType.HEADING_2));

        List<PageElement> list = createPackageList();

        addPageElements(new TextPageElement(
                list.size() + " " + Messages.IpsPackagesListPageElement_packages, TextType.BLOCK)); //$NON-NLS-1$

        if (list.size() > 0) {
            addPageElements(new ListPageElement(list));
        }
    }

    private List<PageElement> createPackageList() {

        Collections.sort(objects, packagesComparator);

        Set<IIpsPackageFragment> packageFragments = getRelatedPackageFragments();

        List<PageElement> packageLinks = new ArrayList<PageElement>();
        Set<String> linkedPackagesNames = new HashSet<String>();

        for (IIpsPackageFragment packageFragment : packageFragments) {
            if (!filter.accept(packageFragment)) {
                continue;
            }
            if (linkedPackagesNames.contains(packageFragment.getName())) {
                continue;
            }

            linkedPackagesNames.add(packageFragment.getName());
            packageLinks.add(PageElementUtils.createLinkPageElement(getConfig(), packageFragment, getLinkTarget(),
                    DocumentorUtil.getIpsPackageName(packageFragment), true));
        }

        return packageLinks;
    }
}
