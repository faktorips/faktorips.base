/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IAttribute;
import org.junit.Ignore;
import org.junit.Test;

public class StdBuilderSetTest extends AbstractStdBuilderTest {

    /*
     * #bug 1460
     */
    @Test
    public void testBasePackageNamesWithUpperCaseLetters() throws CoreRuntimeException {
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
    public void testStdBuilderSetPropertyDefinitions() throws CoreRuntimeException {
        IIpsProject ipsProject = newIpsProject();
        IIpsArtefactBuilderSetInfo builderSetInfo = IIpsModel.get()
                .getIpsArtefactBuilderSetInfo("org.faktorips.devtools.stdbuilder.ipsstdbuilderset");
        assertNotNull(builderSetInfo);
        IIpsBuilderSetPropertyDef[] propertyDefs = builderSetInfo.getPropertyDefinitions();

        ArrayList<String> propertyDefNames = new ArrayList<>();
        for (IIpsBuilderSetPropertyDef propertyDef : propertyDefs) {
            propertyDefNames.add(propertyDef.getName());
        }

        assertTrue(propertyDefNames.contains("generateChangeListener"));
        assertTrue(propertyDefNames.contains("generatorLocale"));
        assertTrue(propertyDefNames.contains("generateDeltaSupport"));
        assertTrue(propertyDefNames.contains("generateCopySupport"));
        assertTrue(propertyDefNames.contains("generateVisitorSupport"));
        assertTrue(propertyDefNames.contains("loggingFrameworkConnector"));
        assertTrue(propertyDefNames.contains("generateJaxbSupport"));
        assertTrue(propertyDefNames.contains("persistenceProvider"));
        assertTrue(propertyDefNames.contains("formulaCompiling"));
        assertTrue(propertyDefNames.contains("markNoneMergeableResourcesAsDerived"));
        assertTrue(propertyDefNames.contains("toXMLSupport"));
        // discontinued properties
        assertFalse(propertyDefNames.contains("useJavaEnumTypes"));
        assertFalse(propertyDefNames.contains("useTypesafeCollections"));

        IIpsBuilderSetPropertyDef loggingConnectorPropertyDef = builderSetInfo
                .getPropertyDefinition("loggingFrameworkConnector");
        @SuppressWarnings("deprecation")
        org.faktorips.devtools.model.ipsproject.IIpsLoggingFrameworkConnector connector = (org.faktorips.devtools.model.ipsproject.IIpsLoggingFrameworkConnector)loggingConnectorPropertyDef
                .parseValue(loggingConnectorPropertyDef.getDefaultValue(ipsProject));
        assertNull(connector);
    }

    /*
     * Bug FS#1864 Heap performance test. Test runs about 200sec!
     */
    @Ignore
    @Test
    public void testBuildPerformanceLongRun() throws CoreRuntimeException {
        IIpsProject ipsProject = newIpsProject();
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IIpsSrcFolderEntry entry = path.getSourceFolderEntries()[0];
        entry.setSpecificBasePackageNameForDerivedJavaClasses("org.faktorips.sample.Model");
        entry.setSpecificBasePackageNameForMergableJavaClasses("org.faktorips.sample.Model");
        ipsProject.getProject().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
        ipsProject.setIpsObjectPath(path);
        System.out.println("init objects");
        for (int i = 0; i < 5000; i++) {
            IPolicyCmptType policyType = newPolicyCmptType(ipsProject, "Policy" + i);
            IProductCmptType productType = newProductCmptType(ipsProject, "Product" + i);

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

    @Test
    public void testCleanBuildNonDerivedFiles() throws CoreRuntimeException {
        // need to set marking derived resources true because we want the clean build to remove all
        // derived files

        IProductCmptType type = newProductCmptType(ipsProject, "Product");
        IProductCmpt productCmpt = newProductCmpt(type, "Product");

        IPackageFragmentRoot packageFragmentRoot = productCmpt.getIpsPackageFragment().getRoot()
                .getArtefactDestination(true);
        // the artefact destination is expected to be there right from the beginning
        assertTrue(packageFragmentRoot.exists());

        /*
         * after an incremental build the base package and the generated xml file for the product
         * cmpt is expected to be there
         */
        productCmpt.getIpsProject().getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        IPackageFragment fragment = packageFragmentRoot.getPackageFragment("org.faktorips.sample.model");
        assertTrue(fragment.exists());
        // TODO little dirty here. Better to ask the builder for its package
        IFile productFile = ((IFolder)packageFragmentRoot.getResource())
                .getFile(new Path("/org/faktorips/sample/model/internal/Product.xml"));
        assertTrue(productFile.exists());

        // a clean build is expected to remove the base directory and the product xml file.
        productCmpt.getIpsProject().getProject().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
        assertFalse(productFile.exists());
        assertFalse(fragment.exists());

        // a full build creates the base directory and the product component xml file again.
        productCmpt.getIpsProject().getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        assertTrue(fragment.exists());
        assertTrue(productFile.exists());

        // Putting an arbitrary file into a sub folder of the derived destination folder.
        IFile file = ((IFolder)fragment.getResource()).getFile("keep.txt");
        file.create(new ByteArrayInputStream("".getBytes()), true, null);
        file.setDerived(false, null);
        assertTrue(file.exists());

        /*
         * after the clean build the non derived file in the destinations sub folder is expected to
         * stay
         */
        productCmpt.getIpsProject().getProject().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
        assertTrue(file.exists());
        assertTrue(fragment.exists());
    }

}
