package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.TestEnumType;
import org.faktorips.devtools.core.internal.model.IpsProject;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.util.StringUtil;

public class FormulaCompletionProcessorTest extends IpsPluginTest {

	private FormulaCompletionProcessor processor;
	private IpsProject ipsProject;
	
	public void setUp() throws Exception{
		super.setUp();
		System.out.println(Thread.currentThread().getName());
		ipsProject = (IpsProject)newIpsProject("TestProject");
		PolicyCmptType cmptType = newPolicyCmptType(ipsProject.getIpsPackageFragmentRoots()[0], "TestPolicy");
		newDefinedEnumDatatype(ipsProject, new Class[]{TestEnumType.class});
		IAttribute attr = cmptType.newAttribute();
		attr.setAttributeType(AttributeType.CHANGEABLE);
		attr.setDatatype("String");
		attr.setModifier(Modifier.PUBLISHED);
		attr.setName("a");
		ExprCompiler compiler = new ExprCompiler();
		//the compiler is not further considered in this test case. It is just neccessary to obey to the constructor requirements
		processor = new FormulaCompletionProcessor(attr, ipsProject, compiler);
	}
	
	/*
	 * Test method for 'org.faktorips.devtools.core.ui.editors.productcmpt.FormulaCompletionProcessor.doComputeCompletionProposals(String, int, List)'
	 */
	public void testDoComputeCompletionProposals() throws Exception {
		System.out.println(Thread.currentThread().getName());
		ArrayList results = new ArrayList();
		processor.doComputeCompletionProposals("Test", 0, results);
		assertEquals(1, results.size());
		CompletionProposal proposal = (CompletionProposal)results.get(0);
		assertEquals(StringUtil.unqualifiedName(TestEnumType.class.getName()), proposal.getDisplayString());
		results = new ArrayList();
		processor.doComputeCompletionProposals("TestEnumType.", 0, results);
		assertEquals(3, results.size());
		ArrayList expectedValues = new ArrayList();
		for (Iterator iter = results.iterator(); iter.hasNext();) {
			proposal = (CompletionProposal) iter.next();
			expectedValues.add(proposal.getDisplayString());
		}
		assertTrue(expectedValues.contains("1"));
		assertTrue(expectedValues.contains("2"));
		assertTrue(expectedValues.contains("3"));

	}

}
