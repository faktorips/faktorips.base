package org.faktorips.devtools.core.ui.bf.edit;

import org.faktorips.devtools.core.model.bf.BFElementType;

/**
 * This specialization of {@link ActionEditPart} is only necessary because the tabbed property view framework
 * needs different classes to distinguish the kind of objects for which it provides editor views.
 * 
 * @author Peter Erzberger
 */
public class CallMethodActionEditPart extends ActionEditPart {

    public CallMethodActionEditPart() {
        super(BFElementType.ACTION_METHODCALL.getImageDescriptor());
    }

}
