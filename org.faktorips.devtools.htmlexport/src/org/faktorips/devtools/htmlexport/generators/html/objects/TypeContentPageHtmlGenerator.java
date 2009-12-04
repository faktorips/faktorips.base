package org.faktorips.devtools.htmlexport.generators.html.objects;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.htmlexport.helper.html.HtmlUtil;

public abstract class TypeContentPageHtmlGenerator<T extends IType> extends AbstractObjectContentPageHtmlGenerator<IType> {

    public TypeContentPageHtmlGenerator(IType object) {
        super(object);
    }

    @Override
    protected String getObjectDetails() {
        StringBuilder builder = new StringBuilder();
        builder.append(createAttributesTable());
        return builder.toString();
    }

    protected String createAttributesTable() {
        IAttribute[] attributes;
        try {
            attributes = object.findAllAttributes(object.getIpsProject());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
        
        StringBuilder builder = new StringBuilder();
        builder.append(HtmlUtil.createHtmlElement("h2", "Attribute"));
        if (attributes.length == 0) {
            builder.append(HtmlUtil.createHtmlElement("div", "Keine Attribute gefunden"));
            return builder.toString();
        }
        
        String[][] cells = new String[attributes.length + 1][3];
        cells[0][0] = "Name";
        cells[0][1] = "Datentyp";
        cells[0][2] = "Modifier";
        
        for (int i = 0; i < attributes.length; i++) {
            cells[i + 1][0] = attributes[i].getName();
            cells[i + 1][1] = attributes[i].getDatatype();
            cells[i + 1][2] = attributes[i].getModifier().getName();
        }
        
        builder.append(HtmlUtil.createHtmlTable(cells, "", ""));
        
        return builder.toString();
    }

}