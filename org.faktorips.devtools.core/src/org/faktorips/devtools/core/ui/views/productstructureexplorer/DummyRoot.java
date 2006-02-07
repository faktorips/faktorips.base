package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import org.faktorips.devtools.core.model.product.IProductCmpt;

/**
 * Primitive helper-class to avoid problems with the TreeViewer if an object is given as root which can be displayed directly.
 * If the ContentProvider returns this object directly, an endless recursion of this item as its own child is displayed. So these 
 * objects are encapsulated with these class to force different objects.
 * 
 * @author Thorsten Guenther
 */
public class DummyRoot {
    public IProductCmpt data;

}
