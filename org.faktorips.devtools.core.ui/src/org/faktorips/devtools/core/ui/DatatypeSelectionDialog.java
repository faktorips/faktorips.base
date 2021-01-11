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
