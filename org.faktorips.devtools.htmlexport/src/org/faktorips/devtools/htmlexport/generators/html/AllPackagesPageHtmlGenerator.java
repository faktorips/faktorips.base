package org.faktorips.devtools.htmlexport.generators.html;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.htmlexport.helper.filter.IpsObjectFilter;
import org.faktorips.devtools.htmlexport.helper.html.HtmlUtil;
import org.faktorips.devtools.htmlexport.helper.html.path.LinkedFileTypes;

public class AllPackagesPageHtmlGenerator extends AbstractAllPageHtmlGenerator {

    private Comparator<IIpsObject> packagesComparator = new Comparator<IIpsObject>() {
        public int compare(IIpsObject arg0, IIpsObject arg1) {
            return arg0.getIpsPackageFragment().getName().compareTo(arg1.getIpsPackageFragment().getName());
        }
    };

    public AllPackagesPageHtmlGenerator(IIpsElement baseIpsElement, List<IIpsObject> objects, IpsObjectFilter filter) {
        super(baseIpsElement, objects, filter);
    }

    public AllPackagesPageHtmlGenerator(IIpsElement baseIpsElement, List<IIpsObject> objects) {
        super(baseIpsElement, objects);
    }

    @Override
    public String generateText() {
        StringBuilder builder = new StringBuilder();
        builder.append(HtmlUtil.createHtmlHead("All Packages"));

        String list = createPackageList();

        builder.append(HtmlUtil.createHtmlElement("body", list));

        builder.append(HtmlUtil.createHtmlElementCloseTag("html"));
        return builder.toString();
    }

    private String createPackageList() {

        Collections.sort(objects, packagesComparator);

        Set<IIpsPackageFragment> packageFragments = getRelatedPackageFragments();

        Set<String> packageLinks = new LinkedHashSet<String>();
        for (IIpsPackageFragment packageFragment : packageFragments) {
            packageLinks.add(HtmlUtil.createLink(getLinkToPackage(packageFragment), HtmlUtil.getLinkName(packageFragment, true), HtmlUtil.getLinkName(packageFragment, false), LinkedFileTypes.PACKAGE_CLASSES_OVERVIEW.getTarget(), "link-package"));
        }
        
        
        StringBuilder list = new StringBuilder();
        String listClass = "all-classes-list";
        String itemClass = "all-classes-item";

        list.append(HtmlUtil.createHtmlElement("h2", "All Packages"));
        list.append(HtmlUtil.createHtmlElement("div", packageFragments.size() + " Packages"));
        list.append(HtmlUtil.createList(packageLinks, listClass, itemClass));
        return list.toString();
    }

    private String getLinkToPackage(IIpsPackageFragment packageFragment) {
        return HtmlUtil.createLinkBase(baseIpsElement, packageFragment, LinkedFileTypes.PACKAGE_CLASSES_OVERVIEW);
    }

}
