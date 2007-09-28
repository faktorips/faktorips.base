/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.deltapresentation;

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpttype2.ProdDefPropertyType;

/**
 * Representation of exactly one delta by its type and a message describing the delta.
 * 
 * @author Thorsten Guenther
 */
final class ProductCmptDeltaDetail {
	private ProductCmptDeltaType type;
	private String message;
	
	public ProductCmptDeltaDetail(ProductCmptDeltaType type, String message) {
		this.type = type;
		this.message = message;
	}
	
	public ProductCmptDeltaType getType() {
		return type;
	}
	
	public String getMessage() {
		return message;
	}
    
    public Image getImage() {
        if (type == ProductCmptDeltaType.MISSING_ATTRIBUTE) {
            return DeltaCompositeIcon.createDeleteImage(ProdDefPropertyType.VALUE.getImage());
        }
        if (type == ProductCmptDeltaType.MISSING_CFGELEMENT) {
            return DeltaCompositeIcon.createAddImage(ProdDefPropertyType.VALUE.getImage());
        }
        if (type == ProductCmptDeltaType.CFGELEMENT_TYPE_MISMATCH) {
            return DeltaCompositeIcon.createModifyImage(ProdDefPropertyType.VALUE.getImage());
        }
        if (type == ProductCmptDeltaType.RELATION_MISMATCH) {
            return DeltaCompositeIcon.createDeleteImage(IpsPlugin.getDefault().getImage("Relation.gif")); //$NON-NLS-1$
        }
        if (type == ProductCmptDeltaType.VALUESET_MISMATCH) {
            return DeltaCompositeIcon.createModifyImage(IpsPlugin.getDefault().getImage("ValueSet.gif")); //$NON-NLS-1$
        }
        if (type == ProductCmptDeltaType.MISSING_CONTENTUSAGE) {
            return DeltaCompositeIcon.createAddImage(IpsPlugin.getDefault().getImage("TableContents.gif")); //$NON-NLS-1$
        }
        if (type == ProductCmptDeltaType.MISSING_STRUCTUREUSAGE) {
            return DeltaCompositeIcon.createDeleteImage(IpsPlugin.getDefault().getImage("TableContents.gif")); //$NON-NLS-1$
        }
        
        return null;
    }
}