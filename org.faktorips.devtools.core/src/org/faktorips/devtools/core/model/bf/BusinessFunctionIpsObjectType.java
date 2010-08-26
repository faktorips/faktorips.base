/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.bf;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.bf.BusinessFunction;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;

/**
 * The IPS object type for a business function. A reference to this class is made in an extension of
 * the extension-point <code>org.faktorips.devtools.core.ipsobjecttype</code>
 * 
 * @author Peter Erzberger
 */
public class BusinessFunctionIpsObjectType extends IpsObjectType {

    public final static String ID = "org.faktorips.devtools.bf.model.BusinessFunction"; //$NON-NLS-1$

    public BusinessFunctionIpsObjectType() {
        super(ID, "BusinessFunction", // XML element name $NON-NLS-1$   //$NON-NLS-1$
                Messages.BusinessFunctionIpsObjectType_displayName, // display name
                Messages.BusinessFunctionIpsObjectType_displayNamePlural, // display name plural
                "ipsbusinessfunction", false, false, BusinessFunction.class); //$NON-NLS-1$
    }

    /**
     * Returns the unique instance of this class.
     */
    public final static BusinessFunctionIpsObjectType getInstance() {
        return (BusinessFunctionIpsObjectType)IpsPlugin.getDefault().getIpsModel().getIpsObjectType(ID);
    }

    @Override
    public IIpsObject newObject(IIpsSrcFile file) {
        return new BusinessFunction(file);
    }

}
