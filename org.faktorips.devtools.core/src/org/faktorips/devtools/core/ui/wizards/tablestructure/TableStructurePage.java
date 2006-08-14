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

package org.faktorips.devtools.core.ui.wizards.tablestructure;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;


/**
 *
 */
public class TableStructurePage extends IpsObjectPage {
    
    /**
     * @param pageName
     * @param selection
     * @throws JavaModelException
     */
    public TableStructurePage(IStructuredSelection selection) throws JavaModelException {
        super(selection, Messages.TableStructurePage_title);
    }

	protected void validateName() {
		super.validateName();
		if (getErrorMessage()!=null) {
			return;
		}
		String name=getIpsObjectName(); 
		IStatus val= JavaConventions.validateJavaTypeName(name);
		if (val.getSeverity() == IStatus.ERROR) {
			setErrorMessage(NLS.bind(org.faktorips.devtools.core.ui.wizards.Messages.IpsObjectPage_msgInvalidName, name));
			return;
		} else if (val.getSeverity() == IStatus.WARNING) {
			setMessage(org.faktorips.devtools.core.ui.wizards.Messages.IpsObjectPage_msgNameDiscouraged, IMessageProvider.WARNING); 
		}		
	}
}
