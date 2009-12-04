/**
 * 
 */
package org.faktorips.devtools.htmlexport.helper.html;

class HtmlAttribute {
    String name;
    String value;

    public HtmlAttribute(String name, String value) {
        super();
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}