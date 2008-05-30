/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.runtime.testrepository;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.ProductComponent;

/**
 * @author Jan Ortmann
 */
public abstract class PnCProduct extends ProductComponent {
	
	public PnCProduct(IRuntimeRepository repository, String id, String productKindId, String versionId) {
		super(repository, id, productKindId, versionId);
	}
	
}
