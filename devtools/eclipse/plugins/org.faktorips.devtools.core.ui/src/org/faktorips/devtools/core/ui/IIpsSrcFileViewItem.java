/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.faktorips.devtools.model.adapter.IIpsSrcFileWrapper;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * This Interface provides a simple possibility to retrieve the underling <code>IpsSrcFile</code> of
 * an object. It´s useful to build standardized ui features e.g. "open in editor" which needs an
 * <code>IpsSrcFile</code> as input.
 */
public interface IIpsSrcFileViewItem extends IIpsSrcFileWrapper {

    /**
     * Provide the <code>IpsSrcFile</code> of this object.
     * 
     * @return <code>IpsSrcFile</code>
     */
    IIpsSrcFile getIpsSrcFile();

}
