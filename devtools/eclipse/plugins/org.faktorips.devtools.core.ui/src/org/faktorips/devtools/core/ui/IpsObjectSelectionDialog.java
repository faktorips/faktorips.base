/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import java.util.Arrays;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectSelectionDialog;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;

/**
 * @deprecated use {@link OpenIpsObjectSelectionDialog} instead
 */
@Deprecated
public class IpsObjectSelectionDialog extends TwoPaneElementSelector {

    public IpsObjectSelectionDialog(Shell parent, String title, String message) {
        this(parent, title, message, DefaultLabelProvider.createWithIpsSourceFileMapping());
    }

    public IpsObjectSelectionDialog(Shell parent, String title, String message, ILabelProvider labelProvider) {
        super(parent, labelProvider, new QualifierLabelProvider());
        setTitle(title);
        setMessage(message);
        setUpperListLabel(Messages.PdObjectSelectionDialog_labelMatches);
        setLowerListLabel(Messages.PdObjectSelectionDialog_labelQualifier);
        setIgnoreCase(true);
        setMatchEmptyString(true);
    }

    /**
     * Set the selected elements of the upper pane as result if more than one element is select.
     * <p>
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

    private static class QualifierLabelProvider extends LabelProvider {

        @Override
        public Image getImage(Object element) {
            return IpsUIPlugin.getImageHandling().getImage(((IIpsSrcFile)element).getIpsPackageFragment());
        }

        @Override
        public String getText(Object element) {
            IIpsPackageFragment pck = ((IIpsSrcFile)element).getIpsPackageFragment();
            return pck.getName() + " - " + pck.getEnclosingResource().getWorkspaceRelativePath().toString(); //$NON-NLS-1$
        }
    }
}
