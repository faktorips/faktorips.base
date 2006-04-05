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

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.DefaultTestContent;
import org.faktorips.devtools.core.ITestAnswerProvider;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsSrcFile;

/**
 * Testcase to test some simple GUI-functions like opening an editor.
 * 
 * @author Thorsten Guenther
 */
public class SimpleDialogTest extends IpsPluginTest implements ILogListener, ITestAnswerProvider {

	private DefaultTestContent content;
	private IpsPlugin plugin;
	private boolean answer = false;
	
	public void setUp() throws Exception {
		content = new DefaultTestContent();
		plugin = IpsPlugin.getDefault();
		plugin.getLog().addLogListener(this);
		plugin.setTestMode(true);
		plugin.setTestAnswerProvider(this);
	}
	
	public void testOpenProductCmptEditor() throws Exception {
		openEditor(content.getBasicMotorProduct().getIpsSrcFile());
		openEditor(content.getComfortCollisionCoverageA().getIpsSrcFile());
		openEditor(content.getStandardBike().getIpsSrcFile());
		openEditor(content.getBasicCollisionCoverage().getIpsSrcFile());
	}

	private void openEditor(IIpsSrcFile file) throws Exception {
		IpsPlugin.getDefault().getIpsPreferences().setWorkingDate(new GregorianCalendar(2003, 7, 1));
		plugin.openEditor(file);
		plugin.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
		
		answer = false;
		IpsPlugin.getDefault().getIpsPreferences().setWorkingDate(new GregorianCalendar(2003, 10, 1));
		plugin.openEditor(content.getComfortCollisionCoverageA().getIpsSrcFile());
		plugin.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);

		answer = true;
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

	/**
	 * {@inheritDoc}
	 */
	public boolean getBooleanAnswer() {
		return answer;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getStringAnswer() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getAnswer() {
		return null;
	}
}
