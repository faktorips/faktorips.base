/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
