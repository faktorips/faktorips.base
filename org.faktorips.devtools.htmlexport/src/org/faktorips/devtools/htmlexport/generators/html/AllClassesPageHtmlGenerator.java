package org.faktorips.devtools.htmlexport.generators.html;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.htmlexport.helper.filter.IpsObjectFilter;
import org.faktorips.devtools.htmlexport.helper.html.HtmlUtil;
import org.faktorips.devtools.htmlexport.helper.html.path.LinkedFileTypes;

public class AllClassesPageHtmlGenerator extends AbstractAllPageElement {
    public AllClassesPageHtmlGenerator(IIpsElement baseIpsElement, List<IIpsObject> objects) {
        this(baseIpsElement, objects, ALL_FILTER);
    }

    public AllClassesPageHtmlGenerator(IIpsElement baseIpsElement, List<IIpsObject> objects, IpsObjectFilter filter) {
        super(baseIpsElement, objects,filter);
    }

    
    public String generateText() {
        StringBuilder builder = new StringBuilder();
        builder.append(HtmlUtil.createHtmlHead("All Classes"));

        String list = createItemList();
        
        builder.append(HtmlUtil.createHtmlElement("body", list));

        builder.append(HtmlUtil.createHtmlElementCloseTag("html"));
        return builder.toString();
    }

    private String createItemList() {
        Collections.sort(objects, IPS_OBJECT_COMPARATOR);
        
        List<String> items = new ArrayList<String>(); 
        for (IIpsObject object : objects) {
            if (!filter.accept(object)) continue;
            items.add(HtmlUtil.createLink(getLinkToObject(object), HtmlUtil.getLinkName(object, true), HtmlUtil.getLinkName(object, false), LinkedFileTypes.CLASS_CONTENT.getTarget(), "link-class"));
        }

        StringBuilder list = new StringBuilder();
        String listClass = "all-classes-list";
        String itemClass = "all-classes-item";
        
        list.append(HtmlUtil.createHtmlElement("h2", "All classes"));
        list.append(HtmlUtil.createHtmlElement("div", items.size() + " classes"));
        list.append(  HtmlUtil.createList(items, listClass, itemClass));
        return list.toString();
    }

    private String getLinkToObject(IIpsObject object) {
        return HtmlUtil.createLinkBase(baseIpsElement, object, LinkedFileTypes.CLASS_CONTENT);
    }
}
