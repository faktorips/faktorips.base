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

package org.faktorips.devtools.core.ui.editors.type;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;


public class OverrideMethodDialog extends OverrideDialog {
    
	/**
     * Creates a new dialog to select candidates for overwriting.
     * 
     * @param pcType The type to get the candidates for overwriting from.
     * @param parent The shell to show this dialog in.
     */
    public OverrideMethodDialog(IType pcType, Shell parent) {
        super(pcType, parent, new CandidatesContentProvider(pcType));
        setTitle(Messages.OverrideMethodDialog_title);
        setEmptyListMessage(Messages.OverrideMethodDialog_msgEmpty);
        setSelectLabelText(Messages.OverrideMethodDialog_labelSelectMethods);
       	selectAbstractMethods(pcType);
    }
    
    private void selectAbstractMethods(IType type) {
        try {
            // select abstract mehods
            List<IMethod> selected = new ArrayList<IMethod>();
            IMethod[] method = type.findOverrideMethodCandidates(false, type.getIpsProject());
            for (int i=0; i<method.length; i++) {
                if (method[i].isAbstract()) {
                    selected.add(method[i]);
                }
            }
            setInitialElementSelections(selected);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

	/**
	 * Returns the methods the user has selected to override. 
	 */
	public IMethod[] getSelectedMethods() {
	    List<Object> methods = new ArrayList<Object>();
	    Object[] checked = getResult();
	    for (int i=0; i<checked.length; i++) {
	        if (checked[i] instanceof IMethod) {
	            methods.add(checked[i]);
	        }
	    }
	    return methods.toArray(new IMethod[methods.size()]);
	}

    private static class CandidatesContentProvider extends OverrideDialog.CandidatesContentProvider {
        
        CandidatesContentProvider(IType pcType) {
        	super(pcType);
        }

		public IIpsObjectPart[] getCandidates(IType pcType) {
			try {
				return pcType.findOverrideMethodCandidates(false, pcType.getIpsProject());
			} catch (CoreException e) {
				IpsPlugin.log(e);
				return new IIpsObjectPart[0];
			}
		}
	}
}
