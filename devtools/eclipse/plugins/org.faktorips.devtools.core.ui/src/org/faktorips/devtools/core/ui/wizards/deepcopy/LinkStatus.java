/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;

/**
 * This is a container for the status of a product component link in the tree of a deep copy wizard.
 * The status consists of the checked status and the copy or link status.
 *
 * @author dirmeier
 */
public class LinkStatus {

    public static final String CHECKED = "checked"; //$NON-NLS-1$
    public static final String COPY_OR_LINK = "copyOrLink"; //$NON-NLS-1$

    private boolean checked;

    private CopyOrLink copyOrLink;
    private final IIpsObjectPart ipsObjectPart;
    private final IIpsObject target;

    public LinkStatus(IIpsObjectPart ipsObjectPart, IIpsObject target, boolean checked, CopyOrLink copyOrLink) {
        this.ipsObjectPart = ipsObjectPart;
        this.target = target;
        this.checked = checked;
        this.copyOrLink = copyOrLink;
    }

    public void setChecked(boolean newValue) {
        checked = newValue;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setCopyOrLink(CopyOrLink newValue) {
        copyOrLink = newValue;
    }

    public CopyOrLink getCopyOrLink() {
        return copyOrLink;
    }

    public IIpsObject getTarget() {
        return target;
    }

    public IIpsObjectPart getIpsObjectPart() {
        return ipsObjectPart;
    }

    public enum CopyOrLink {
        COPY(Messages.SourcePage_operationCopy),
        LINK(Messages.SourcePage_operationLink),
        UNDEFINED(""); //$NON-NLS-1$

        private final String text;

        CopyOrLink(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

}
