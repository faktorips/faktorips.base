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

package org.faktorips.devtools.core.ui.actions;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.IpsObjectPartState;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.xml.sax.SAXException;


/**
 * Test for IpsCutAction.
 * 
 * @author Thorsten Guenther
 */
public class IpsCutActionTest extends AbstractIpsPluginTest {
    
	IpsCutAction cutAction;
	IAttribute attribute;
	IPolicyCmptType pcType;
	
    protected void setUp() throws Exception {
        super.setUp();

        IIpsProject project = this.newIpsProject("TestProject");
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];
        IIpsPackageFragment pack = root.createPackageFragment("products.folder", true, null);

        IIpsSrcFile pdSrcFile = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy", true, null);
        pcType = (PolicyCmptType)pdSrcFile.getIpsObject();
        attribute = pcType.newAttribute();

        cutAction = new IpsCutAction(new TestSelectionProvider(), IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell());

        
     }
    
    public void testRun() throws CoreException, SAXException, IOException, TransformerException {
        String current = new IpsObjectPartState(attribute).toString();

        assertEquals(1, pcType.getNumOfAttributes());
        cutAction.run();
        Clipboard clipboard = new Clipboard(IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay());
        String stored = (String)clipboard.getContents(TextTransfer.getInstance());

        assertEquals(current, stored);
        
        assertEquals(0, pcType.getNumOfAttributes());
        
    }
    
    private class TestSelectionProvider implements ISelectionProvider {

		public void addSelectionChangedListener(ISelectionChangedListener listener) {
		}

		public ISelection getSelection() {
			return new StructuredSelection(attribute);
		}

		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		}

		public void setSelection(ISelection selection) {
		}    	
    }
}
