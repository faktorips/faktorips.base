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

package org.faktorips.devtools.stdbuilder.xpand.productcmpt.model;

import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XAttribute;

public class XProductAttribute extends XAttribute {

    public XProductAttribute(IProductCmptTypeAttribute attribute, GeneratorModelContext context,
            ModelService modelService) {
        super(attribute, context, modelService);
    }

    @Override
    public IProductCmptTypeAttribute getIpsObjectPartContainer() {
        return (IProductCmptTypeAttribute)super.getIpsObjectPartContainer();
    }

    @Override
    public IProductCmptTypeAttribute getAttribute() {
        return (IProductCmptTypeAttribute)super.getAttribute();
    }

}
