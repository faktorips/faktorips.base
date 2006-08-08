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

package org.faktorips.devtools.core.internal.application;

import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * Application for FaktorIPS to be used with eclipse. Provides reduced functionality in comparison
 * with the use as plugin within an eclipse runing the IDE-Product. Used for departement-workers.
 * 
 * @author Thorsten Guenther
 */
public class IpsApplication implements IPlatformRunnable {

	/**
	 * {@inheritDoc}
	 */
	public Object run(Object args) throws Exception {
		Display display = PlatformUI.createDisplay();
		Integer retValue = new Integer(PlatformUI.createAndRunWorkbench(display, new IpsWorkbenchAdvisor()));
		return retValue;
	}
}
