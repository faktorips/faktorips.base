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

package org.faktorips.devtools.core.ui;

import java.util.Arrays;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;

/**
 *
 */
public class IpsObjectSelectionDialog extends TwoPaneElementSelector {

    public IpsObjectSelectionDialog(Shell parent, String title, String message) {
        this(parent, title, message, DefaultLabelProvider.createWithIpsSourceFileMapping());
    }

    /**
     * @param parent
     * @param elementRenderer
     * @param qualifierRenderer
     */
    public IpsObjectSelectionDialog(Shell parent, String title, String message, ILabelProvider labelProvider) {
        super(parent, labelProvider, new QualifierLabelProvider());
        setTitle(title);
        setMessage(message);
        setUpperListLabel(Messages.PdObjectSelectionDialog_labelMatches);
        setLowerListLabel(Messages.PdObjectSelectionDialog_labelQualifier);
        setIgnoreCase(true);
        setMatchEmptyString(true);
    }

    private static class QualifierLabelProvider extends LabelProvider {

        @Override
        public Image getImage(Object element) {
            return IpsUIPlugin.getImageHandling().getImage(((IIpsSrcFile)element).getIpsPackageFragment());
        }

        @Override
        public String getText(Object element) {
            IIpsPackageFragment pck = ((IIpsSrcFile)element).getIpsPackageFragment();
            return pck.getName() + " - " + pck.getEnclosingResource().getFullPath().toString(); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc} Set the selected elements of the upper pane as result if more than one element
     * is select.<br>
     * Remark: The default implementation of TwoPaneElementSelector uses alays the lower pane as
     * result. If there is only one selected element in the upper pane then the lower pane selection
     * will be used, because maybe the same object (same name) could be exists in different
     * packages. But if more than one elements are selected (@see this
     * {@link #setMultipleSelection(boolean)}) then the upper pane is relevant.
     */
    @Override
    protected void computeResult() {
        Object[] selectedElements = getSelectedElements();
        if (selectedElements != null && selectedElements.length > 1) {
            setResult(Arrays.asList(selectedElements));
        } else {
            super.computeResult();
        }
    }
}
