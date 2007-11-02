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

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;


public class OverrideAttributeDialog extends OverrideDialog {
    
	/**
     * Creates a new dialog to select candidates for overwriting.
     * 
     * @param pcType The type to get the candidates for overwriting from.
     * @param parent The shell to show this dialog in.
     */
    public OverrideAttributeDialog(IPolicyCmptType pcType, Shell parent) {
        super(pcType, parent, new CandidatesContentProvider(pcType));
        setTitle(Messages.OverrideAttributeDialog_title);
        setEmptyListMessage(Messages.OverrideAttributeDialog_labelNoAttributes);
        setSelectLabelText(Messages.OverrideAttributeDialog_labelSelectAttribute);
    }
    
	/**
	 * Returns the methods the user has selected to override. 
	 */
	public IPolicyCmptTypeAttribute[] getSelectedAttributes() {
	    List attributes = new ArrayList();
	    Object[] checked = getResult();
	    for (int i=0; i<checked.length; i++) {
	        if (checked[i] instanceof IPolicyCmptTypeAttribute) {
	            attributes.add(checked[i]);
	        }
	    }
	    return (IPolicyCmptTypeAttribute[])attributes.toArray(new IPolicyCmptTypeAttribute[attributes.size()]);
	}

    private static class CandidatesContentProvider extends OverrideDialog.CandidatesContentProvider {
        
        CandidatesContentProvider(IPolicyCmptType pcType) {
        	super(pcType);
        }

		public IIpsObjectPartContainer[] getCandidates(IPolicyCmptType pcType) {
			try {
				return pcType.findOverrideAttributeCandidates();
			} catch (CoreException e) {
				IpsPlugin.log(e);
				return new IIpsObjectPartContainer[0];
			}
		}
	}
}
