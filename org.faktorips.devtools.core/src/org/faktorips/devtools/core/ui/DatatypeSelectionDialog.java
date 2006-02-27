package org.faktorips.devtools.core.ui;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;
import org.faktorips.datatype.Datatype;


/**
 *
 */
public class DatatypeSelectionDialog extends TwoPaneElementSelector {

    /**
     * @param parent
     * @param elementRenderer
     * @param qualifierRenderer
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
        
        public String getText(Object element) {
            Datatype datatype = (Datatype)element; 
            return datatype.getName();
        }
    }

}
