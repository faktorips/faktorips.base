package org.faktorips.devtools.htmlexport.helper.path;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;

/**
 * LinkedFileType defines, whether a link goes to a content or an classes-overview page
 * 
 * @author dicker
 * 
 */
public class LinkedFileType {
    private static LinkedFileType PACKAGE_CLASSES_OVERVIEW = new LinkedFileType("package_classes_", "", "classes"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    private static LinkedFileType ELEMENT_CONTENT = new LinkedFileType("element_", "", "content"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    private LinkedFileType(String prefix, String suffix, String target) {
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

    /**
     * @param element
     * @return {@link LinkedFileType} according to the given {@link IIpsElement}
     */
    public static LinkedFileType getLinkedFileTypeByIpsElement(IIpsElement element) {
        if (element instanceof IIpsPackageFragment) {
            return PACKAGE_CLASSES_OVERVIEW;
        }
        if (element instanceof IIpsObject) {
            return new LinkedFileType(
                    "object_", "." + ((IIpsObject)element).getIpsObjectType().getFileExtension(), "content"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        return ELEMENT_CONTENT;
    }
}
