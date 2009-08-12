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

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;

public class OverrideAttributeDialog extends SelectSupertypeHierarchyPartsDialog {

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
        for (int i = 0; i < checked.length; i++) {
            if (checked[i] instanceof IPolicyCmptTypeAttribute) {
                attributes.add(checked[i]);
            }
        }
        return (IPolicyCmptTypeAttribute[])attributes.toArray(new IPolicyCmptTypeAttribute[attributes.size()]);
    }

    private static class CandidatesContentProvider extends SelectSupertypeHierarchyPartsDialog.PartsContentProvider {

        CandidatesContentProvider(IPolicyCmptType pcType) {
            super(pcType);
        }

        public IIpsObjectPart[] getAvailableParts(IIpsObject ipsObject) {
            IPolicyCmptType pcType = (IPolicyCmptType)ipsObject;
            try {
                return pcType.findOverrideAttributeCandidates(pcType.getIpsProject());
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return new IIpsObjectPart[0];
            }
        }
    }
}
