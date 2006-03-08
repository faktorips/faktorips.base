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

package org.faktorips.devtools.core.ui;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;


/**
 *
 */
public class PdObjectSelectionDialog extends TwoPaneElementSelector {

    /**
     * @param parent
     * @param elementRenderer
     * @param qualifierRenderer
     */
    public PdObjectSelectionDialog(Shell parent, String title, String message) {
        super(parent, new DefaultLabelProvider(), new QualifierLabelProvider());
        setTitle(title);
        setMessage(message);
        setUpperListLabel(Messages.PdObjectSelectionDialog_labelMatches);
        setLowerListLabel(Messages.PdObjectSelectionDialog_labelQualifier);
        setIgnoreCase(true);
        setMatchEmptyString(true);
    }
    
    private static class QualifierLabelProvider extends LabelProvider {
        
        public Image getImage(Object element) {
            return ((IIpsObject)element).getIpsPackageFragment().getImage();
        }
        
        public String getText(Object element) {
            IIpsPackageFragment pck = ((IIpsObject)element).getIpsPackageFragment(); 
            return pck.getName()
            	+ " - " + pck.getEnclosingResource().getFullPath().toString(); //$NON-NLS-1$
        }
    }

}
