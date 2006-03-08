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

package org.faktorips.devtools.core.ui.controller;

/**
 * @author eidenschink
 *
 * This class will replace the subclasses IpsObjectUIController and IpsPartUIController
 * when the implementation of the corresponding class IpsObjectPartContainer is finished.
 */
public abstract class IpsObjectPartContainerUIController extends DefaultUIController {


	public IpsObjectPartContainerUIController() {
		super();
	}

		public abstract void add(EditField editField, String propertyName);

}
