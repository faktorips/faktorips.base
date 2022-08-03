/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt;

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
    String getName();

    /**
     * Returns the id that uniquely identifies this kind at runtime. When operative systems access
     * the runtime repository, they have to use this id, to get access to the versions / product
     * components of this kind.
     * <p>
     * We distinguish between the name and the runtime id, to allow for example a numeric id for use
     * with operative systems. If this id is persisted in a database some people prefer to use
     * numeric values as they consume less disk space.
     */
    String getRuntimeId();

}
