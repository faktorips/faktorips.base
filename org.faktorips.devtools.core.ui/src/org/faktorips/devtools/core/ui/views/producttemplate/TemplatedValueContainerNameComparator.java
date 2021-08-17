/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.producttemplate;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;

/**
 * Comparator that compares {@link ITemplatedValue} objects by the name of the product component of
 * the {@code ITemplatedValueContainer} to which they belong.
 */
public class TemplatedValueContainerNameComparator implements Comparator<ITemplatedValue>, Serializable {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(ITemplatedValue v1, ITemplatedValue v2) {
        String name1 = v1.getTemplatedValueContainer().getProductCmpt().getName();
        String name2 = v2.getTemplatedValueContainer().getProductCmpt().getName();
        return ObjectUtils.compare(name1, name2);
    }

}