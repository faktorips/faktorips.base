/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper.path;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;

/**
 * LinkedFileType defines, whether a link goes to a content or an classes-overview page
 * 
 * @author dicker
 * 
 */
public class LinkedFileType {
    public static final LinkedFileType PACKAGE_CLASSES_OVERVIEW = new LinkedFileType("package_classes_", "", "classes"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    public static final LinkedFileType OBJECT_TYPE_CLASSES_OVERVIEW = new LinkedFileType(
            "object_type_classes_", "", "classes"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    public static final LinkedFileType ELEMENT_CONTENT = new LinkedFileType("element_", "", "content"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

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
     * @return {@link LinkedFileType} according to the given {@link IIpsElement}
     */
    public static LinkedFileType getLinkedFileTypeByIpsElement(IIpsElement element) {
        if (element instanceof IIpsPackageFragment) {
            return PACKAGE_CLASSES_OVERVIEW;
        }
        return ELEMENT_CONTENT;
    }
}
