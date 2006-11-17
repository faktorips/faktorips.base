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

import java.util.GregorianCalendar;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.product.DateBasedProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptWithoutVersionIdTest extends AbstractIpsPluginTest {

    public ProductCmptWithoutVersionIdTest() {
        super();
    }

    public ProductCmptWithoutVersionIdTest(String name) {
        super(name);
    }

    public void test() throws CoreException {
        IIpsProject project = newIpsProject();
        IIpsProjectProperties props = project.getProperties();
        props.setProductCmptNamingStrategy(new DateBasedProductCmptNamingStrategy(" ", "yyyy-MM", false));
        project.setProperties(props);
        
        IPolicyCmptType type = newPolicyCmptType(project, "Policy");
        IProductCmpt cmpt = newProductCmpt(project, "Product-A");
        cmpt.setPolicyCmptType(type.getQualifiedName());
        cmpt.newGeneration(new GregorianCalendar(2010, 0, 1));
        cmpt.getIpsSrcFile().save(true, null);
        
        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }
}
