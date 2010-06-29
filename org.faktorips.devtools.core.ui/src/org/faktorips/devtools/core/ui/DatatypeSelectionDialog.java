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

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;
import org.faktorips.datatype.Datatype;

public class DatatypeSelectionDialog extends TwoPaneElementSelector {

    /**
     * @param parent The parent shell.
     */
    public DatatypeSelectionDialog(Shell parent) {
        super(parent, new QualifierLabelProvider(), new QualifierLabelProvider());

        setTitle(Messages.DatatypeSelectionDialog_title);
        setMessage(Messages.DatatypeSelectionDialog_description);
        setUpperListLabel(Messages.DatatypeSelectionDialog_labelMatchingDatatypes);
        setLowerListLabel(Messages.DatatypeSelectionDialog_msgLabelQualifier);
        setIgnoreCase(true);
        setMatchEmptyString(true);
    }

    private static class QualifierLabelProvider extends DefaultLabelProvider {
        @Override
        public String getText(Object element) {
            Datatype datatype = (Datatype)element;
            return datatype.getName();
        }
    }

}
