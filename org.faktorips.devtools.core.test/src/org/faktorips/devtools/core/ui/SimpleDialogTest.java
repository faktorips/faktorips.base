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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.DefaultTestContent;
import org.faktorips.devtools.core.ITestAnswerProvider;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.ui.editors.productcmpt.GenerationSelectionDialog;

/**
 * Testcase to test some simple GUI-functions like opening an editor.
 * 
 * @author Thorsten Guenther
 */
public class SimpleDialogTest extends AbstractIpsPluginTest implements ILogListener, ITestAnswerProvider {

	private DefaultTestContent content;
	private IpsPlugin plugin;
	private int answer = GenerationSelectionDialog.CHOICE_BROWSE;
	
	public void setUp() throws Exception {
        super.setUp();
		content = new DefaultTestContent();
		plugin = IpsPlugin.getDefault();
		plugin.getLog().addLogListener(this);
		plugin.setTestMode(true);
		plugin.setTestAnswerProvider(this);
	}
	
	public void testOpenProductCmptEditor() throws Exception {
		openEditor(content.getBasicMotorProduct());
		openEditor(content.getComfortCollisionCoverageA());
		openEditor(content.getStandardBike());
		openEditor(content.getBasicCollisionCoverage());
	}

	private void openEditor(IIpsObject file) throws Exception {
		IpsPlugin.getDefault().getIpsPreferences().setWorkingDate(new GregorianCalendar(2003, 7, 1));

    	IpsPlugin.getDefault().openEditor((IFile) file.getCorrespondingResource());
		plugin.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
		
		answer = GenerationSelectionDialog.CHOICE_BROWSE;
        IpsPlugin.getDefault().openEditor((IFile) content.getComfortCollisionCoverageA().getCorrespondingResource());
        plugin.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);

		answer = GenerationSelectionDialog.CHOICE_CREATE;
        IpsPlugin.getDefault().openEditor((IFile) content.getComfortCollisionCoverageA().getCorrespondingResource());
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
		return false;
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

    public int getIntAnswer() {
        return answer;
    }

    /**
     * {@inheritDoc}
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        plugin.getLog().removeLogListener(this);
    }

    
    
}
