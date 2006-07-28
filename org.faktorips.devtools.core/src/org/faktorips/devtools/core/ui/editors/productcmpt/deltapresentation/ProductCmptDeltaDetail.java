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
}