/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

/**
 * An exception that indicates that a method do modify the repository contents was called, but the
 * repository does not allow to modify its contents.
 * 
 * @author Jan Ortmann
 */
public class IllegalRepositoryModificationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

}
