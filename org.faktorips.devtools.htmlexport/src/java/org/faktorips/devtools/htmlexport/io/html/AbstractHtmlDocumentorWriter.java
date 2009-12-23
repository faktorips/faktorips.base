package org.faktorips.devtools.htmlexport.io.html;

import org.faktorips.devtools.htmlexport.io.AbstractDocumentorWriter;

public abstract class AbstractHtmlDocumentorWriter extends AbstractDocumentorWriter {

    protected String createLink(String aim, String text) {
        StringBuilder link = new StringBuilder();
        link.append("<a href=\"");
        link.append(aim);
        link.append("\">");
        link.append(text);
        link.append("</a>");
        return link.toString();
    }

}
