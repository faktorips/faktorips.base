package org.faktorips.devtools.bf.ui.edit;

import org.faktorips.devtools.core.model.bf.BFElementType;

/**
 * This specialization of {@link ActionEditPart} is only necessary because the tabbed property view framework
 * needs different classes to distinguish the kind of objects for which it provides editor views.
 * 
 * @author Peter Erzberger
 */
public class CallBusinessFunctionActionEditPart extends ActionEditPart {

    public CallBusinessFunctionActionEditPart() {
        super(BFElementType.ACTION_BUSINESSFUNCTIONCALL.getImageDescriptor());
    }

}
