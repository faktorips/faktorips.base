package org.faktorips.devtools.core.ui.views.producttemplate;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;

/**
 * Comparator that compares {@link IPropertyValue} objects by the name of the {@code IProductCmpt}
 * to which they belong.
 */
public class PropertyValueProductCmptNameComparator implements Comparator<IPropertyValue>, Serializable {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(IPropertyValue v1, IPropertyValue v2) {
        String name1 = v1.getPropertyValueContainer().getProductCmpt().getName();
        String name2 = v2.getPropertyValueContainer().getProductCmpt().getName();
        return ObjectUtils.compare(name1, name2);
    }

}