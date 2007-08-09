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
package org.faktorips.devtools.core.ui.views.modeldescription;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.part.IPage;

/**
 * Mark a class for providing input to {@link ModelDescriptionView}. 
 *  
 * @see PageBookView 
 * 
 * @author blum
 * 
 */
public interface IModelDescriptionSupport {

	/**
	 * Create a Page for {@link ModelDescriptionView}.
	 * 
	 * @return IPage new Page.
	 * @throws CoreException 
	 */
	public IPage createModelDescriptionPage() throws CoreException;
}
