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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;


/**
 *
 */
public class IpsObjectPartTest extends AbstractIpsPluginTest {
    
	private IIpsProject project;
	private IProductCmpt productCmpt;
	private IIpsSrcFile pdSrcFile;
    private IIpsObjectPart part;
    private IIpsObjectPart subpart;

    protected void setUp() throws Exception {
    	super.setUp();
    	project = newIpsProject("TestProject");
    	productCmpt = (IProductCmpt)newIpsObject(project, IpsObjectType.PRODUCT_CMPT, "Product");
    	pdSrcFile = productCmpt.getIpsSrcFile();
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration(); 
        part = generation;  
        subpart = generation.newConfigElement();
    }

    public void testGetIpsObject() {
        assertEquals(productCmpt, part.getIpsObject());
        assertEquals(productCmpt, subpart.getIpsObject());
    }

    public void testSetDescription() {
        part.setDescription("newDescription");
        assertEquals("newDescription", part.getDescription());
        assertTrue(pdSrcFile.isDirty());
    }

    public void testEquals() throws CoreException {
    	assertFalse(part.equals(null));
    	assertFalse(part.equals("abc"));
    	
    	// different id
    	IIpsObjectGeneration gen2 = productCmpt.newGeneration();
    	assertFalse(part.equals(gen2));
    	
        IProductCmpt productCmpt2 = (IProductCmpt)newIpsObject(project, IpsObjectType.PRODUCT_CMPT, "Product2");
    	IIpsObjectGeneration gen3 = productCmpt2.newGeneration();

    	// same id, different parent
    	assertFalse(part.equals(gen3));
    
    	assertTrue(part.equals(part));
    }
}
