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

/**
 * Product components might represent a single product in different versions that are sold for a
 * certain period of time. The product component kind represents the constant aspect.
 * <p>
 * Example:
 * <p>
 * The two product components MotorProduct_2003-11 and MotorProduct_2006-01 are two different
 * versions of the same product component kind MotorProduct.
 * 
 * @author Jan Ortmann
 */
public interface IProductCmptKind {

    /**
     * Returns the name of this product component kind. The name is used to present this kind to the
     * user.
     */
    public String getName();

    /**
     * Returns the id that uniquely identifies this kind at runtime. When operative systems access
     * the runtime repository, they have to use this id, to get access to the versions / product
     * components of this kind.
     * <p>
     * We distinguish between the name and the runtime id, to allow for example a numeric id for use
     * with operative systems. If this id is persisted in a database some people prefer to use
     * numeric values as they consume less disk space.
     */
    public String getRuntimeId();

}
