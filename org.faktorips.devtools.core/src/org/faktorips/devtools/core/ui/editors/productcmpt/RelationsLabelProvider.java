package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;

public class RelationsLabelProvider extends DefaultLabelProvider{

        public String getText(Object element) {
        	return element.toString();
//            IProductCmptRelation relation = (IProductCmptRelation)element;
//            return relation.getName() 
//            + " [" + relation.getMinCardinality() + //$NON-NLS-1$
//            	".." + relation.getMaxCardinality() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }

}
