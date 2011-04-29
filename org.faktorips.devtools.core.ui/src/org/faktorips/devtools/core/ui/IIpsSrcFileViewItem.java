/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.faktorips.devtools.core.internal.model.adapter.IIpsSrcFileWrapper;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

/**
 * This Interface provides a simple possibility to retrieve the underling <code>IpsSrcFile</code> of
 * an object. It´s useful to build standardized ui features e.g. "open in editor" which needs an
 * <code>IpsSrcFile</code> as input.
 * 
 * @author Faktor Zehn AG
 */
public interface IIpsSrcFileViewItem extends IIpsSrcFileWrapper {

    /**
     * Provide the <code>IpsSrcFile</code> of this object.
     * 
     * @return <code>IpsSrcFile</code>
     */
    public IIpsSrcFile getIpsSrcFile();

}
