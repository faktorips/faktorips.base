package org.faktorips.devtools.htmlexport.io;

public abstract class AbstractHtmlGenerator {

    protected String createHtmlElement(String node, String text) {
        return createHtmlElement(node, text, null);
    }

    protected String createHtmlElement(String node, String text, String classes) {
        StringBuilder builder = new StringBuilder();
        builder.append('<').append(node);
        if (classes != null && !classes.trim().equals("")) {
            builder.append(" class=\"").append(classes).append('\"');
        }
        builder.append('>').append(text).append("</").append(node).append('>');
        return builder.toString();
    }
    
    protected abstract String getTitle();
    
    
}
