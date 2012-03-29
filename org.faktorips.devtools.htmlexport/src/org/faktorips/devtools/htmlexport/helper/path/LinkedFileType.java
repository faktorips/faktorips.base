/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
public enum LinkedFileType {

    PACKAGE_CLASSES_OVERVIEW("package_classes_", "", TargetType.CLASSES), //$NON-NLS-1$ //$NON-NLS-2$ 
    OBJECT_TYPE_CLASSES_OVERVIEW("object_type_classes_", "", TargetType.CLASSES), //$NON-NLS-1$ //$NON-NLS-2$ 
    ELEMENT_CONTENT("element_", "", TargetType.CONTENT); //$NON-NLS-1$ //$NON-NLS-2$ 

    private LinkedFileType(String prefix, String suffix, TargetType target) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.target = target;
    }

    private String prefix;
    private String suffix;
    private TargetType target;

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public TargetType getTarget() {
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
