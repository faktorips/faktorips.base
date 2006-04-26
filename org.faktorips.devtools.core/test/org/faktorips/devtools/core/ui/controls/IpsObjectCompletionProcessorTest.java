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

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.jface.contentassist.IContentAssistSubjectControl;
import org.eclipse.jface.contentassist.TextContentAssistSubjectAdapter;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;

public class IpsObjectCompletionProcessorTest extends IpsPluginTest {

	IIpsProject project;
	
	/**
	 * {@inheritDoc}
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		project = newIpsProject("TestProject");
		IIpsProject base = newIpsProject("BaseProject");
		IIpsObjectPath path = project.getIpsObjectPath();
		path.newIpsProjectRefEntry(base);
		project.setIpsObjectPath(path);
		
		newPolicyCmptType(base, "pack.BaseType");
		newPolicyCmptType(project, "pc.DependendType");
	}

	public void testDoComputeCompletionProposals() throws Exception {

		IpsObjectCompletionProcessor processor = new IpsObjectCompletionProcessor(IpsObjectType.POLICY_CMPT_TYPE);
		processor.setIpsProject(project);
		
		Shell s = new Shell();
		Text text = new Text(s, SWT.NONE);
		text.setText("p");
		
		IContentAssistSubjectControl subject = new TextContentAssistSubjectAdapter(text);
		ICompletionProposal[] result = processor.computeCompletionProposals(subject, 1);
		
		assertEquals(2, result.length);
		assertEquals("pc", result[0].getDisplayString());
		assertEquals("pack", result[1].getDisplayString());

		text.setText("B");
		result = processor.computeCompletionProposals(subject, 1);
		
		assertEquals(1, result.length);
		result[0].apply(subject.getDocument());
		assertEquals("pack.BaseType", text.getText());
		
		text.setText("pc.d");
		result = processor.computeCompletionProposals(subject, 4);
		
		assertEquals(1, result.length);
		result[0].apply(subject.getDocument());
		assertEquals("pc.DependendType", text.getText());
	}

}
