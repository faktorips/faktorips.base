package org.faktorips.devtools.htmlexport.helper.html.path;

public enum LinkedFileTypes {
    PACKAGE_CLASSES_OVERVIEW("package_classes_", ".html", "classes"),
    CLASS_CONTENT("class_", ".html", "content");

    private LinkedFileTypes(String prefix, String suffix, String target) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.target = target;
    }

    private String prefix;
    private String suffix;
    private String target;

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getTarget() {
        return target;
    }

}
