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

import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/**
 * The workbench-advisor for FaktorIps as own product.
 * 
 * @author Thorsten Guenther
 */
class IpsWorkbenchAdvisor extends WorkbenchAdvisor {

	/**
	 * {@inheritDoc}
	 */
	public String getInitialWindowPerspectiveId() {
		return "org.faktorips.devtools.core.productDefinitionPerspective"; //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		configurer.setShowFastViewBars(false);
		configurer.setShowPerspectiveBar(false);
		configurer.setTitle(Messages.IpsWorkbenchAdvisor_title);
		return new IpsWorkbenchWindowAdvisor(configurer);
	}
}