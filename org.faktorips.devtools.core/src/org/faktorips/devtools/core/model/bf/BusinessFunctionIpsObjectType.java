/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.model.bf;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.bf.BusinessFunction;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;

/**
 * @author Peter Erzberger
 */
public class BusinessFunctionIpsObjectType extends IpsObjectType {

    public final static String ID = "org.faktorips.devtools.bf.model.BusinessFunction";

    public BusinessFunctionIpsObjectType() {
        super("BusinessFunction", ID, "BusinessFunction", "ipsbusinessfunction", false, false, "BusinessFunction.gif",
                "BusinessFunctionDisabled.gif");
    }

    public final static BusinessFunctionIpsObjectType getInstance() {
        return (BusinessFunctionIpsObjectType)IpsPlugin.getDefault().getIpsModel().getIpsObjectType(ID);
    }

    @Override
    public IIpsObject newObject(IIpsSrcFile file) {
        return new BusinessFunction(file);
    }

}
