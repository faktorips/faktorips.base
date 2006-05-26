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

package org.faktorips.devtools.core.internal.model.pctype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Signature;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.util.StringUtil;


/**
*
*/
public class MethodPluginTest extends AbstractIpsPluginTest {
   
   private IIpsProject ipsProject;
   private IIpsPackageFragmentRoot ipsRoot;
   private IIpsPackageFragment ipsPack;
   private IIpsSrcFile ipsSrcFile;
   private PolicyCmptType pcType;
   private IMethod method;
   
   protected void setUp() throws Exception {
       super.setUp();
       ipsProject = this.newIpsProject("TestProject");
       ipsRoot = ipsProject.getIpsPackageFragmentRoots()[0];
       ipsPack = ipsRoot.createPackageFragment("motor.coverages", true, null);
       ipsSrcFile = ipsPack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "CollisionCoverage", true, null);
       pcType = (PolicyCmptType)ipsSrcFile.getIpsObject();
       method = pcType.newMethod();
   }
   
   public void testGetParameterTypeSignatures() throws CoreException {
       String[] signatures = method.getParameterTypeSignatures();
       assertEquals(0, signatures.length);
       
       Parameter p0 = new Parameter(0, "p0", "Decimal");
       Parameter p1 = new Parameter(1, "p1", "Money");
       method.setParameters(new Parameter[] {p0, p1});
       
       signatures = method.getParameterTypeSignatures();
       assertEquals(2, signatures.length);
       String decimalClassName = StringUtil.unqualifiedName(Datatype.DECIMAL.getJavaClassName());
       String moneyClassName = StringUtil.unqualifiedName(Datatype.MONEY.getJavaClassName());
       String s0 = Signature.createTypeSignature(decimalClassName, false);
       String s1 = Signature.createTypeSignature(moneyClassName, false);
       assertEquals(s0, signatures[0]);
       assertEquals(s1, signatures[1]);
       
       p1 = new Parameter(1, "p1", "UnkownDatatype");
       method.setParameters(new Parameter[] {p0, p1});
       try {
           signatures = method.getParameterTypeSignatures();
           fail();
       } catch (CoreException e) {
       }
   }
   
}
