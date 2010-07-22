/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.model.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * A class that contains validations of the model class <code>IProductCmpt</code> that are also used
 * in the creation wizard where the model object doesn't exist at the point of validation.
 * 
 * @author Jan Ortmann
 * 
 * @since 2.3
 */
public class ProductCmptValidations {

    /**
     * Checks if the product component type exists and is not abstract.
     * 
     * @param productCmpt The product component that is checked or <code>null</code> if it does not
     *            exist, yet.
     * @param productCmptTypeName Qualified name of the product component type.
     * @param list The list error messages are added to.
     * @param ipsProject The IPS project used to search.
     * 
     * @return The product component type if it exists and is not abstract.
     * 
     * @throws CoreException if an error occurs while searching.
     */
    /* Tests can be found in ProductCmptTest */
    public static final IProductCmptType validateProductCmptType(IProductCmpt productCmpt,
            String productCmptTypeName,
            MessageList list,
            IIpsProject ipsProject) throws CoreException {
        IProductCmptType type = ipsProject.findProductCmptType(productCmptTypeName);
        if (type == null) {
            String text = NLS.bind(
                    org.faktorips.devtools.core.model.productcmpt.Messages.ProductCmptValidations_typeDoesNotExist,
                    productCmptTypeName);
            list.add(new Message(IProductCmpt.MSGCODE_MISSINGG_PRODUCT_CMPT_TYPE, text, Message.ERROR, productCmpt,
                    IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE));
            return null;
        }
        if (type.isAbstract()) {
            String text = NLS.bind(
                    org.faktorips.devtools.core.model.productcmpt.Messages.ProductCmptValidations_typeIsAbstract,
                    productCmptTypeName);
            list.add(new Message(IProductCmpt.MSGCODE_ABSTRACT_PRODUCT_CMPT_TYPE, text, Message.ERROR, productCmpt,
                    IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE));
            return null;
        }
        return type;
    }

}
