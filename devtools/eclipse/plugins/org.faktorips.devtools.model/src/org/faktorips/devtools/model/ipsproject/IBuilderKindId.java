/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsproject;

/**
 * This interface identifies the kind of a builder.
 * <p>
 * A builder could be used to generate different kind of output. For example a builder for policy
 * component types could be instantiated to generate interfaces or implementations. Different kind
 * IDs identifies the different builders.
 * <p>
 * This interface is intended to be implemented by an java enum that specifies your different
 * builders.
 * 
 * @author dirmeier
 */
public interface IBuilderKindId {

    String getId();

}
