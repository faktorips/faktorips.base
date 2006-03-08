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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeRelationTest extends IpsPluginTest {

	private IIpsProject ipsProject;
	private IPolicyCmptType policyCmptType;
	private IProductCmptTypeRelation relation;
	private IRelation policyCmptTypeRelation;
	
	protected void setUp() throws Exception {
		super.setUp();
		ipsProject = newIpsProject("TestProject");
		policyCmptType = (IPolicyCmptType)newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "motor.MotorPolicy");
		policyCmptType.setUnqualifiedProductCmptType("MotorProduct");
		policyCmptType.getIpsSrcFile().save(true, null);
		policyCmptTypeRelation = policyCmptType.newRelation();
		relation = new ProductCmptTypeRelation(policyCmptTypeRelation);
	}


	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeRelation.findContainerRelation()'
	 */
	public void testFindContainerRelation() throws CoreException {
		assertNull(relation.findContainerRelation());
		policyCmptTypeRelation.setContainerRelation("containerRelation");
		assertNull(relation.findContainerRelation()); // container relation no there

		IRelation policyCmptTypeContainerRel = policyCmptType.newRelation();
		policyCmptTypeContainerRel.setTargetRoleSingular("containerRelation");
		IProductCmptTypeRelation containerRelation = new ProductCmptTypeRelation(policyCmptTypeContainerRel);
		
		assertEquals(containerRelation, relation.findContainerRelation());
	}

}
