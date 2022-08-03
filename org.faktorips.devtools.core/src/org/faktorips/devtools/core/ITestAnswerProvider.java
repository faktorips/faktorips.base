/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core;

/**
 * Interface for classes which provide values to be returned on simulated user interactions.
 * 
 * @author Thorsten Guenther
 */
public interface ITestAnswerProvider {

    /**
     * Returns the answer as boolean.
     */
    boolean getBooleanAnswer();

    /**
     * Returns the answer as integer.
     */
    int getIntAnswer();

    /**
     * Returns the answer as String.
     */
    String getStringAnswer();

    /**
     * Returns the answer as unspecified object.
     */
    Object getAnswer();

}
