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

package org.faktorips.devtools.core.internal.migrationextensions;

import static org.mockito.Mockito.mock;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Test;

public class Migration_3_5Test {

    @Test
    public void testMigrate() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);
        new Migration_3_5(ipsProject, "testFeatureId");
    }

}
