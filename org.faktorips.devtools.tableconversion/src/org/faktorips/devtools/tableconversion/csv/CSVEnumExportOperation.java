/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.csv;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.util.message.MessageList;

/**
 * 
 * 
 * @author Roman Grutza
 */
// TODO rg: implement
public class CSVEnumExportOperation implements IWorkspaceRunnable{

    public CSVEnumExportOperation(IEnumValueContainer valueContainer, String string, CSVTableFormat tableFormat,
            String nullRepresentationString, boolean exportColumnHeaderRow, MessageList list) {
        // TODO Auto-generated constructor stub
    }

    public void run(IProgressMonitor monitor) throws CoreException {
        // TODO Auto-generated method stub
        
    }

}
