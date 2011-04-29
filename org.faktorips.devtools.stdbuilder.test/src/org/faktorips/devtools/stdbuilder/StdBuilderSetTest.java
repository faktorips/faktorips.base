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

package org.faktorips.devtools.stdbuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.core.model.ipsproject.IIpsLoggingFrameworkConnector;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.junit.Ignore;
import org.junit.Test;

public class StdBuilderSetTest extends AbstractStdBuilderTest {

    /*
     * #bug 1460
     */
    @Test
    public void testBasePackageNamesWithUpperCaseLetters() throws CoreException {
        IIpsProject ipsProject = newIpsProject();
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IIpsSrcFolderEntry entry = path.getSourceFolderEntries()[0];
        entry.setSpecificBasePackageNameForDerivedJavaClasses("org.faktorips.sample.Model");
        entry.setSpecificBasePackageNameForMergableJavaClasses("org.faktorips.sample.Model");
        ipsProject.setIpsObjectPath(path);
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        newPolicyCmptType(ipsProject, "Policy");
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
    }

    @Test
    public void testStdBuilderSetPropertyDefinitions() throws CoreException {
        IIpsProject ipsProject = newIpsProject();
        IIpsArtefactBuilderSetInfo builderSetInfo = IpsPlugin.getDefault().getIpsModel()
                .getIpsArtefactBuilderSetInfo("org.faktorips.devtools.stdbuilder.ipsstdbuilderset");
        assertNotNull(builderSetInfo);
        IIpsBuilderSetPropertyDef[] propertyDefs = builderSetInfo.getPropertyDefinitions();
        assertEquals(11, propertyDefs.length);

        ArrayList<String> propertyDefNames = new ArrayList<String>();
        for (IIpsBuilderSetPropertyDef propertyDef : propertyDefs) {
            propertyDefNames.add(propertyDef.getName());
        }

        assertTrue(propertyDefNames.contains("generateChangeListener"));
        assertTrue(propertyDefNames.contains("useJavaEnumTypes"));
        assertTrue(propertyDefNames.contains("generatorLocale"));
        assertTrue(propertyDefNames.contains("useTypesafeCollections"));
        assertTrue(propertyDefNames.contains("generateDeltaSupport"));
        assertTrue(propertyDefNames.contains("generateCopySupport"));
        assertTrue(propertyDefNames.contains("generateVisitorSupport"));
        assertTrue(propertyDefNames.contains("loggingFrameworkConnector"));
        assertTrue(propertyDefNames.contains("generateJaxbSupport"));
        assertTrue(propertyDefNames.contains("persistenceProvider"));
        assertTrue(propertyDefNames.contains("formulaCompiling"));

        IIpsBuilderSetPropertyDef loggingConnectorPropertyDef = builderSetInfo
                .getPropertyDefinition("loggingFrameworkConnector");
        IIpsLoggingFrameworkConnector connector = (IIpsLoggingFrameworkConnector)loggingConnectorPropertyDef
                .parseValue(loggingConnectorPropertyDef.getDefaultValue(ipsProject));
        assertNull(connector);
    }

    /*
     * Bug FS#1864 Heap performance test. Test runs about 200sec!
     */
    @Ignore
    @Test
    public void testBuildPerformanceLongRun() throws CoreException {
        IIpsProject ipsProject = newIpsProject();
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IIpsSrcFolderEntry entry = path.getSourceFolderEntries()[0];
        entry.setSpecificBasePackageNameForDerivedJavaClasses("org.faktorips.sample.Model");
        entry.setSpecificBasePackageNameForMergableJavaClasses("org.faktorips.sample.Model");
        ipsProject.getProject().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
        ipsProject.setIpsObjectPath(path);
        System.out.println("init objects");
        for (int i = 0; i < 5000; i++) {
            PolicyCmptType policyType = newPolicyCmptType(ipsProject, "Policy" + i);
            ProductCmptType productType = newProductCmptType(ipsProject, "Product" + i);

            policyType.setConfigurableByProductCmptType(true);
            policyType.setProductCmptType(productType.getName());
            IAttribute attr = policyType.newAttribute();
            attr.setName("attribut" + i);
            attr.setDatatype("int");
            attr.setDefaultValue("1");

            productType.setConfigurationForPolicyCmptType(true);
            productType.setPolicyCmptType(policyType.getName());
            attr = productType.newAttribute();
            attr.setName("prodattribut" + i);
            attr.setDatatype("int");
            attr.setDefaultValue("1");
        }

        System.out.println("start build");
        Date time = new Date();
        ipsProject.getProject().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        System.out.println("Buildtime: " + ((new Date().getTime() - time.getTime()) / 1000) + "s");
    }
}
