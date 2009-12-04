package org.faktorips.devtools.htmlexport.generators.html.objects;

import org.apache.commons.lang.NotImplementedException;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.htmlexport.generators.AbstractTextGenerator;
import org.faktorips.devtools.htmlexport.helper.Util;
import org.faktorips.devtools.htmlexport.helper.html.HtmlUtil;

public abstract class AbstractObjectContentPageHtmlGenerator<T extends IIpsObject> extends AbstractTextGenerator {

    protected T object;

    public static AbstractTextGenerator getInstance(IIpsObject object) {
        if (object.getIpsObjectType() == IpsObjectType.POLICY_CMPT_TYPE)
            return new PolicyCmptContentPageHtmlGenerator((PolicyCmptType) object);
        if (object.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT_TYPE)
            return new ProductCmptContentPageHtmlGenerator((ProductCmptType) object);
        throw new NotImplementedException();
    }

    protected AbstractObjectContentPageHtmlGenerator(T object) {
        this.object = object;
    }

    @Override
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
