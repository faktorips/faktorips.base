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

package org.faktorips.devtools.core.ui;

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.DefaultTestContent;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.IpsPreferences;

/**
 * Testcase to test some simple GUI-functions like opening an editor.
 * 
 * @author Thorsten Guenther
 */
public class SimpleDialogTest extends IpsPluginTest implements ILogListener {

	private DefaultTestContent content;
	private IpsPlugin plugin;
	
	public void setUp() throws Exception {
		content = new DefaultTestContent();
		plugin = IpsPlugin.getDefault();
		plugin.getLog().addLogListener(this);
	}
	
	public void testOpenProductCmptEditor() throws CoreException {
		IpsPreferences.setWorkingDate(new GregorianCalendar(2006, 12, 1));
		plugin.openEditor(content.getComfortCollisionCoverageA().getIpsSrcFile());
		plugin.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
	}

	/**
	 * {@inheritDoc}
	 */
	public void logging(IStatus status, String plugin) {
		// never ever should a logentry appear...
		fail();
	}
}
