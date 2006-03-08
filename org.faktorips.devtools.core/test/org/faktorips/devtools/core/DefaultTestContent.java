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

package org.faktorips.devtools.core;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.xml.sax.SAXException;


/**
 * Creates a new project with a default test content.
 * 
 * @author Thorsten Guenther
 */
public class DefaultTestContent {

	private IIpsProject project;
	private IpsPluginTest helper;
	
	public DefaultTestContent() throws CoreException, SAXException, IOException, ParserConfigurationException {
		helper = new IpsPluginTest() {};
		project = helper.newIpsProject("DefaultTestProject");
		IFolder res = (IFolder)project.getIpsPackageFragmentRoots()[0].getEnclosingResource();
		
		copy(res, "independant", "Contract.ipspct");
		copy(res, "independant", "Coverage.ipspct");
		copy(res, "motor", "CollisionCoverage.ipspct");
		copy(res, "motor", "TplCoverage.ipspct");
		copy(res, "motor", "MotorContract.ipspct");
		copy(res, "motor", "Vehicle.ipspct");
		copy(res, "products", "BasicCollisionCoverage.ipsproduct");
		copy(res, "products", "BasicMotorProduct.ipsproduct");
		copy(res, "products", "ComfortCollisionCoverageA.ipsproduct");
		copy(res, "products", "ComfortCollisionCoverageB.ipsproduct");
		copy(res, "products", "ComfortMotorProduct.ipsproduct");
		copy(res, "products", "StandardTplCoverage.ipsproduct");
		copy(res, "products", "StandardVehicle.ipsproduct");
	}
	
	private void copy(IFolder target, String pack, String filename) throws SAXException, IOException, ParserConfigurationException, CoreException {
		String sourceName = "defaulttestcontent" + SystemUtils.FILE_SEPARATOR + pack + SystemUtils.FILE_SEPARATOR + filename;
		

		IFolder packFolder = target.getFolder(pack);
		if (!packFolder.exists()) {
			packFolder.create(true, true, null);
		}

		packFolder.getFile(filename).create(getClass().getResourceAsStream(sourceName), true, null);
	}
	
	public IIpsProject getProject() {
		return this.project;
	}

	public IProductCmpt getBasicCollisionCoverage() throws CoreException {
		return (IProductCmpt)project.findIpsObject(IpsObjectType.PRODUCT_CMPT, "products.BasicCollisionCoverage");
	}
	
	public IProductCmpt getBasicMotorProduct() throws CoreException {
		return (IProductCmpt)project.findIpsObject(IpsObjectType.PRODUCT_CMPT, "products.BasicMotorProduct");
	}
	
	public IProductCmpt getComfortCollisionCoverageA() throws CoreException {
		return (IProductCmpt)project.findIpsObject(IpsObjectType.PRODUCT_CMPT, "products.ComfortCollisionCoverageA");
	}
	
	public IProductCmpt getComfortCollisionCoverageB() throws CoreException {
		return (IProductCmpt)project.findIpsObject(IpsObjectType.PRODUCT_CMPT, "products.ComfortCollisionCoverageB");
	}
	
	public IProductCmpt getComfortMotorProduct() throws CoreException {
		return (IProductCmpt)project.findIpsObject(IpsObjectType.PRODUCT_CMPT, "products.ComfortMotorProduct");
	}
	
	public IProductCmpt getStandardTplCoverage() throws CoreException {
		return (IProductCmpt)project.findIpsObject(IpsObjectType.PRODUCT_CMPT, "products.StandardTplCoverage");
	}
	
	public IProductCmpt getStandardVehicle() throws CoreException {
		return (IProductCmpt)project.findIpsObject(IpsObjectType.PRODUCT_CMPT, "products.StandardVehicle");
	}
	
	public IPolicyCmptType getContract() throws CoreException {
		return (IPolicyCmptType)project.findIpsObject(IpsObjectType.PRODUCT_CMPT, "independant.Contract");
	}
	
	public IPolicyCmptType getCoverage() throws CoreException {
		return (IPolicyCmptType)project.findIpsObject(IpsObjectType.PRODUCT_CMPT, "independant.Coverage");
	}

	public IPolicyCmptType getCollisionCoverage() throws CoreException {
		return (IPolicyCmptType)project.findIpsObject(IpsObjectType.PRODUCT_CMPT, "motor.CollisionCoverage");
	}

	public IPolicyCmptType getTplCoverage() throws CoreException {
		return (IPolicyCmptType)project.findIpsObject(IpsObjectType.PRODUCT_CMPT, "motor.TplCoverage");
	}

	public IPolicyCmptType getMotorContract() throws CoreException {
		return (IPolicyCmptType)project.findIpsObject(IpsObjectType.PRODUCT_CMPT, "motor.MotorContract");
	}

	public IPolicyCmptType getVehicle() throws CoreException {
		return (IPolicyCmptType)project.findIpsObject(IpsObjectType.PRODUCT_CMPT, "motor.Vehicle");
	}
}
