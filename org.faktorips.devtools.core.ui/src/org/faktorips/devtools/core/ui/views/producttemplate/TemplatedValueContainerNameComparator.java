package org.faktorips.devtools.core.ui.views.producttemplate;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.devtools.core.model.productcmpt.ITemplatedValue;

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