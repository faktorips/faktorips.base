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

package org.faktorips.devtools.core.internal.model;

import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;


/**
 *
 */
public class IpsObjectPartStateTest extends IpsPluginTest {
    
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
    }

    public void testAll() {
    	IpsObjectPartState state = new IpsObjectPartState(attribute);
    	IpsObjectPartState stringState = new IpsObjectPartState(state.toString());

    	assertEquals(1, pcType.getNumOfAttributes());
    	attribute.delete();
    	assertEquals(0, pcType.getNumOfAttributes());
    	attribute = (IAttribute)state.newPart(pcType);
    	assertEquals(1, pcType.getNumOfAttributes());
    	attribute.delete();
    	assertEquals(0, pcType.getNumOfAttributes());
    	stringState.newPart(pcType);
    	assertEquals(1, pcType.getNumOfAttributes());
    	
    	IMethod method = pcType.newMethod();
    	state = new IpsObjectPartState(method);
    	stringState = new IpsObjectPartState(state.toString());
    	
    	assertEquals(1, pcType.getNumOfMethods());
    	method.delete();
    	assertEquals(0, pcType.getNumOfMethods());
    	method = (IMethod)state.newPart(pcType);
    	assertEquals(1, pcType.getNumOfMethods());
    	method.delete();
    	assertEquals(0, pcType.getNumOfMethods());
    	stringState.newPart(pcType);
    	assertEquals(1, pcType.getNumOfMethods());
    }    

}
