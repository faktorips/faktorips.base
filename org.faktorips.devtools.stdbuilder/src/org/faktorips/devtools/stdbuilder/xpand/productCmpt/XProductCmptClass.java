/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.xpand.productCmpt;

import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.xpand.XpandBuilder;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelObject;

public class XProductCmptClass extends AbstractGeneratorModelObject {

    public XProductCmptClass(IProductCmptType ipsObjectPartContainer, XpandBuilder builder) {
        super(ipsObjectPartContainer, builder);
    }

    @Override
    public IProductCmptType getIpsObjectPartContainer() {
        return (IProductCmptType)super.getIpsObjectPartContainer();
    }

    public IProductCmptType getProductCmptType() {
        return getIpsObjectPartContainer();
    }

}
