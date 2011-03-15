/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class MissingProductCmptTypeTest extends AbstractIpsPluginTest {

    public MissingProductCmptTypeTest() {
        super();
    }

    /**
     * Tests if the build finishes without throwing an exception if the product component type name
     * is missing
     */
    @Test
    public void test() throws Exception {
        IIpsProject project = newIpsProject();
        IPolicyCmptType type = newPolicyCmptTypeWithoutProductCmptType(project, "Policy");
        type.setConfigurableByProductCmptType(true);
        type.setProductCmptType("Product"); // missing product component type!

        type.getIpsSrcFile().save(true, null);
        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }

}
