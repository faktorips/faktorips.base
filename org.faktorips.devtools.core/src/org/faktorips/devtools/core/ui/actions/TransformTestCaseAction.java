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

package org.faktorips.devtools.core.ui.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.actions.ActionDelegate;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.testcase.TestCaseTransformer;

/**
 * Action delegate to transform runtime ips test cases.
 * 
 * @author Joerg Ortmann
 */
public class TransformTestCaseAction extends ActionDelegate {
	private IStructuredSelection selection = StructuredSelection.EMPTY;

	/**
	 * {@inheritDoc}
	 */
	public void selectionChanged(IAction action, ISelection newSelection) {
		if (newSelection instanceof IStructuredSelection)
			selection = (IStructuredSelection) newSelection;
		else
			selection = StructuredSelection.EMPTY;
	}

	public void runWithEvent(IAction action, Event event) {
        // TODO Joerg: select of target package, test case type, and optional extension
		String packageName = "hp.Tests.transformed";
        String testCaseTypeName = "hp.Tests.NeuzugangFamilieType";
        String nameExtension = "_transformed";
        
        try{
			for (Iterator iter = selection.iterator(); iter.hasNext();) {
				Object selObj = (Object) iter.next();
				if (selObj instanceof IFile){
					IFile file = (IFile) selObj;
					TestCaseTransformer testCaseTransformer = new TestCaseTransformer();
					try {
						testCaseTransformer.createTestCaseFromRuntimeXml(file, testCaseTypeName, packageName, nameExtension);
					} catch (Exception e) {
						throw new CoreException(new IpsStatus("An error occured during transforming the test case runtime xml: \"" + file.getName() + "\"", e));
					}
				}
			}
        } catch (CoreException e) {
        	IpsPlugin.logAndShowErrorDialog(e);
        }
	}
}
