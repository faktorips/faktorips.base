package org.faktorips.devtools.htmlexport.helper.path;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;

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

    public static LinkedFileTypes getLinkedFileTypeByIpsElement(IIpsElement element) {
        if (element instanceof IIpsPackageFragment) return PACKAGE_CLASSES_OVERVIEW;
        return CLASS_CONTENT;
    }
}
