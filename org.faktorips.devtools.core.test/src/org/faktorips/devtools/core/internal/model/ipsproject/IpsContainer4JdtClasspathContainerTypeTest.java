/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Test;

public class IpsContainer4JdtClasspathContainerTypeTest {

    @Test
    public void testNewContainer() {
        IpsContainer4JdtClasspathContainerType type = new IpsContainer4JdtClasspathContainerType();
        IIpsProject ipsProject = new IpsProject();
        IIpsObjectPathContainer container = type.newContainer(ipsProject, "path");

        assertEquals(type, container.getContainerType());
        assertEquals("path", container.getOptionalPath());
        assertSame(ipsProject, container.getIpsProject());
    }
}
