package org.faktorips.devtools.htmlexport.generators.html;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.htmlexport.helper.filter.IpsObjectFilter;
import org.faktorips.devtools.htmlexport.pages.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextType;

public class AllPackagesPageElement extends AbstractAllPageElement {

    private Comparator<IIpsObject> packagesComparator = new Comparator<IIpsObject>() {
        public int compare(IIpsObject arg0, IIpsObject arg1) {
            return arg0.getIpsPackageFragment().getName().compareTo(arg1.getIpsPackageFragment().getName());
        }
    };

    public AllPackagesPageElement(IIpsElement baseIpsElement, List<IIpsObject> objects, IpsObjectFilter filter) {
        super(baseIpsElement, objects, filter);
        setTitle("All Packages");
    }

    public AllPackagesPageElement(IIpsElement baseIpsElement, List<IIpsObject> objects) {
        this(baseIpsElement, objects, ALL_FILTER);
    }

    @Override
    public void build() {
        super.build();
        addPageElement(new TextPageElement(getTitle(), TextType.HEADING_2));
        
        List<PageElement> list = createPackageList();
        
        addPageElement(new TextPageElement(list.size() + " Packages"));
        
        if (list.size() > 0) {
            addPageElement(new ListPageElement(list));
        }
    }

    private List<PageElement> createPackageList() {

        Collections.sort(objects, packagesComparator);

        Set<IIpsPackageFragment> packageFragments = getRelatedPackageFragments();

        List<PageElement> packageLinks = new ArrayList<PageElement>();
        for (IIpsPackageFragment packageFragment : packageFragments) {
            if (!filter.accept(packageFragment)) continue;
            PageElement link = new LinkPageElement(baseIpsElement, packageFragment, new TextPageElement(packageFragment.getName()), getLinkTarget());
            packageLinks.add(link);
        }
        return packageLinks;
    }
}
