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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.UIToolkit;


/**
 * A control to edit table content references.  
 */
public class TableContentsRefControl extends IpsObjectRefControl {

    public TableContentsRefControl(
            IIpsProject project, 
            Composite parent, 
            UIToolkit toolkit) {
        super(project, parent, toolkit, Messages.TableContentsRefControl_title, Messages.TableContentsRefControl_description);
    }
    
    /**
     * Returns the table contents entered in this control. Returns <code>null</code>
     * if the text in the control does not identify a table contents.
     * 
     * @throws CoreException if an exception occurs while searching for
     * the table contents.
     */
    public ITableContents findTableContents() throws CoreException {
        if (getIpsProject()==null) {
            return null;
        }
        return (ITableContents)getIpsProject().findIpsObject(IpsObjectType.TABLE_CONTENTS, getText());
    }
    
    /** 
     * {@inheritDoc}
     */
    protected IIpsObject[] getIpsObjects() throws CoreException {
        if (getIpsProject()==null) {
            return new IIpsObject[0];
        }
        return getIpsProject().findIpsObjects(IpsObjectType.TABLE_CONTENTS);
    }

}
