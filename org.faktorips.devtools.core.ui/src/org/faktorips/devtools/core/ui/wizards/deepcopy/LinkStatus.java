/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.deepcopy;


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

    public LinkStatus(boolean checked, CopyOrLink copyOrLink) {
        this.setChecked(checked);
        this.setCopyOrLink(copyOrLink);
    }

    public void setChecked(boolean newValue) {
        this.checked = newValue;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setCopyOrLink(CopyOrLink newValue) {
        setCopyOrLinkInternal(newValue);
    }

    void setCopyOrLinkInternal(CopyOrLink newValue) {
        this.copyOrLink = newValue;
    }

    public CopyOrLink getCopyOrLink() {
        return copyOrLink;
    }

    public enum CopyOrLink {
        COPY(Messages.SourcePage_operationCopy),
        LINK(Messages.SourcePage_operationLink),
        UNDEFINED(""); //$NON-NLS-1$

        public final String text;

        private CopyOrLink(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

}
