/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import java.util.ArrayList;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.ipsproject.IIpsBuilderSetPropertyDef;

public class StdBuilderSetTest extends AbstractIpsPluginTest {

    public void testStdBuilderSetPropertyDefinitions(){
        IIpsArtefactBuilderSetInfo builderSetInfo = IpsPlugin.getDefault().getIpsModel().getIpsArtefactBuilderSetInfo("org.faktorips.devtools.stdbuilder.ipsstdbuilderset");
        assertNotNull(builderSetInfo);
        IIpsBuilderSetPropertyDef[] propertyDefs = builderSetInfo.getPropertyDefinitions();
        assertEquals(8, propertyDefs.length);
        
        ArrayList propertyDefNames = new ArrayList();
        for (int i = 0; i < propertyDefs.length; i++) {
            propertyDefNames.add(propertyDefs[i].getName());
        }
        assertTrue(propertyDefNames.contains("generateChangeListener"));
        assertTrue(propertyDefNames.contains("useJavaEnumTypes"));
        assertTrue(propertyDefNames.contains("useTypesafeCollections"));
        assertTrue(propertyDefNames.contains("generateDeltaSupport"));
        assertTrue(propertyDefNames.contains("generateCopySupport"));
        assertTrue(propertyDefNames.contains("generateLoggingStatements"));
        assertTrue(propertyDefNames.contains("generateVisitorSupport"));
    }
}
