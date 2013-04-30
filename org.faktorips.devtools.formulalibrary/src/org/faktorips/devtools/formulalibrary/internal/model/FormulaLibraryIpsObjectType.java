package org.faktorips.devtools.formulalibrary.internal.model;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;

/**
 * The IPS object type for a formula library. A reference to this class is made in an extension of
 * the extension-point <code>org.faktorips.devtools.core.ipsobjecttype</code>
 * 
 * @author dicker
 */
public class FormulaLibraryIpsObjectType extends IpsObjectType {

    public static final String ID = "FormulaLibrary"; //$NON-NLS-1$

    private static final String IPSFORMULALIBRARY = "ipsformulalibrary"; //$NON-NLS-1$
    private static final String TAG_NAME = "FormulaLibrary"; //$NON-NLS-1$

    public FormulaLibraryIpsObjectType() {
        super(ID, TAG_NAME, Messages.FormulaLibraryIpsObjectType_nameFormulaLibrary,
                Messages.FormulaLibraryIpsObjectType_nameFormulaLibraryPlural, IPSFORMULALIBRARY, false, true,
                FormulaLibrary.class);
    }

    /**
     * Returns the unique instance of this class.
     */
    public static final FormulaLibraryIpsObjectType getInstance() {
        return (FormulaLibraryIpsObjectType)IpsPlugin.getDefault().getIpsModel().getIpsObjectType(ID);
    }

    @Override
    public IIpsObject newObject(IIpsSrcFile file) {
        return new FormulaLibrary(file);
    }
}
