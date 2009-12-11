package org.faktorips.devtools.htmlexport.generators.html.objects;

import org.apache.commons.lang.NotImplementedException;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.htmlexport.helper.Util;
import org.faktorips.devtools.htmlexport.helper.html.HtmlUtil;
import org.faktorips.devtools.htmlexport.pages.elements.RootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextType;

public abstract class AbstractObjectContentPageElement<T extends IIpsObject> extends RootPageElement {

    protected T object;

    public static RootPageElement getInstance(IIpsObject object) {
        if (object.getIpsObjectType() == IpsObjectType.POLICY_CMPT_TYPE)
            return new PolicyCmptContentPageElement((PolicyCmptType) object);
        if (object.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT_TYPE)
            return new ProductCmptContentPageElement((ProductCmptType) object);
        throw new NotImplementedException();
    }

    protected AbstractObjectContentPageElement(T object) {
        this.object = object;
        setTitle(object.getName());
    }

    
    @Override
    public void build() {
        super.build();
        addPageElement(new TextPageElement(Util.getIpsPackageName(object.getIpsPackageFragment())));
        addPageElement(new TextPageElement(object.getName(), TextType.HEADING_1));

    }

    public String generateText() {
        StringBuilder builder = new StringBuilder();
        builder.append(HtmlUtil.createHtmlHead(object.getName()));

        builder.append(HtmlUtil.createHtmlElement("body", createPage()));

        builder.append(HtmlUtil.createHtmlElementCloseTag("html"));
        return builder.toString();
    }

    private String createPage() {
        StringBuilder builder = new StringBuilder();
        builder.append(getObjectPageHead());
        builder.append(getObjectDetails());
        return builder.toString();
    }

    protected String getObjectPageHead() {
        StringBuilder builder = new StringBuilder();
        
        builder.append(HtmlUtil.createHtmlElement("div", Util.getIpsPackageName(object.getIpsPackageFragment()), "package-small"));
        builder.append(HtmlUtil.createHtmlElement("h1", object.getName()));
        
        String[][] basis = {{"Type", object.getIpsObjectType().getDisplayName()}, {"Beschreibung",  HtmlUtil.getHtmlText(object.getDescription())}};
        builder.append(HtmlUtil.createHtmlTable(basis, "table", ""));
        
        return builder.toString();
    }

    protected abstract String getObjectDetails();

}
