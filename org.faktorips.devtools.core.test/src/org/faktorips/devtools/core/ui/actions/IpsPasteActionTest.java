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
public class IpsPasteActionTest extends AbstractIpsPluginTest {
    
	IpsCutAction cutAction;
    IpsPasteAction pasteAction;
	IAttribute attribute;
	IPolicyCmptType pcType;
    IIpsPackageFragment pack;
    IIpsPackageFragmentRoot root;
	
    protected void setUp() throws Exception {
        super.setUp();

        IIpsProject project = this.newIpsProject("TestProject");
        root = project.getIpsPackageFragmentRoots()[0];
        pack = root.createPackageFragment("products.folder", true, null);

        IIpsSrcFile pdSrcFile = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy", true, null);
        pcType = (PolicyCmptType)pdSrcFile.getIpsObject();
        attribute = pcType.newAttribute();

        cutAction = new IpsCutAction(new TestSelectionProvider(attribute), IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell());
        pasteAction = new IpsPasteAction(new TestSelectionProvider(pcType), IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell());
        
     }
    
    public void testRun() throws CoreException, SAXException, IOException, TransformerException {
        String old = new IpsObjectPartState(attribute).toString();
        assertEquals(1, pcType.getNumOfAttributes());
        cutAction.run();
        assertEquals(0, pcType.getNumOfAttributes());
        pasteAction.run();
        assertEquals(1, pcType.getNumOfAttributes());
        assertEquals(old, new IpsObjectPartState(pcType.getAttributes()[0]).toString());
        
        IpsCopyAction copyAction = new IpsCopyAction(new TestSelectionProvider(pack), IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell());
        pasteAction = new IpsPasteAction(new TestSelectionProvider(root), IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell());
        copyAction.run();
        assertEquals(1, root.getIpsDefaultPackageFragment().getIpsChildPackageFragments().length);
        pasteAction.run();
        assertEquals(2, root.getIpsDefaultPackageFragment().getIpsChildPackageFragments().length);
    }
    
    private class TestSelectionProvider implements ISelectionProvider {
        Object selected;
        
        public TestSelectionProvider(Object selected) {
            this.selected = selected; 
        }
        
		public void addSelectionChangedListener(ISelectionChangedListener listener) {
		}

		public ISelection getSelection() {
			return new StructuredSelection(selected);
		}

		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		}

		public void setSelection(ISelection selection) {
		}    	
    }
}
