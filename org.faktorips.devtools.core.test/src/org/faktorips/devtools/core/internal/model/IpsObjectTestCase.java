/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.util.XmlAbstractTestCase;


/**
 * Abstract test case for ips objects and parts.
 */
public abstract class IpsObjectTestCase extends XmlAbstractTestCase {
    
    protected IpsSrcFile pdSrcFile;

    public IpsObjectTestCase() {
        super();
    }

    public IpsObjectTestCase(String name) {
        super(name);
    }
    
    protected void setUp(IpsObjectType type) throws Exception {
        pdSrcFile = new IpsSrcFile(null, type.getFileName("Test"));
        IpsPlugin.getDefault().getManager().putSrcFileContents(pdSrcFile, new IpsSourceFileContents(pdSrcFile, "", "UTF-8"));
        createObjectAndPart();
        pdSrcFile.markAsClean();
    }
    
    protected abstract void createObjectAndPart();

}
