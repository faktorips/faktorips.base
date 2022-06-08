/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.migrationextensions;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.internal.migrationextensions.Migration_22_6_0;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.builder.settings.ValueSetMethods;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.devtools.model.versionmanager.options.IpsMigrationOption;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.junit.Test;

public class Migration_22_6_0Test extends AbstractStdBuilderTest {

    @SuppressWarnings({ "deprecation", "unchecked" })
    @Test
    public void testMigrate_BusinessFunctions() throws Exception {
        IIpsProject ipsProject = newIpsProject("Migration_22_6_0Test_BF");
        IProject project = ipsProject.getProject().unwrap();
        copy("ipsproject", project);
        project.getFile(".ipsproject").delete(true, null);
        project.getFile("ipsproject").move(Path.fromPortableString(".ipsproject"), true, null);
        IFolder productDef = project.getFolder("productdef");
        IFile businessProcess1 = copy("BusinessProcess1.ipsbf", productDef);
        IFile businessProcess2 = copy("BusinessProcess2.ipsbf", productDef);
        copy("PcWithRules.ipspolicycmpttype", productDef);
        IFolder businessfunctionsFolder = productDef.getFolder("businessfunctions");
        businessfunctionsFolder.create(true, true, null);
        IFile bf1 = copy("Bf1.ipsbusinessfunction", businessfunctionsFolder);
        IFile bf3 = copy("Bf3.ipsbusinessfunction", businessfunctionsFolder);
        copy("BfPolicy.ipspolicycmpttype", businessfunctionsFolder);
        IFolder subBfs = businessfunctionsFolder.getFolder("subBfs");
        subBfs.create(true, true, null);
        IFile bf2 = copy("Bf2.ipsbusinessfunction", subBfs);

        IpsModel.reInit();
        ipsProject = IIpsModel.get().getIpsProject("Migration_22_6_0Test_BF");

        Migration_22_6_0 migration = new Migration_22_6_0(ipsProject, "irrelevant");
        ((IpsMigrationOption<ValueSetMethods>)migration.getOptions().iterator().next())
                .setSelectedValue(ValueSetMethods.Unified);

        migration.migrate(new NullProgressMonitor());

        assertThat(businessProcess1.exists(), is(false));
        assertThat(businessProcess2.exists(), is(false));
        assertThat(bf1.exists(), is(false));
        assertThat(bf2.exists(), is(false));
        assertThat(bf3.exists(), is(false));
        IPolicyCmptType pcWithRules = (IPolicyCmptType)ipsProject.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE,
                "PcWithRules");
        assertThat(pcWithRules.getNumOfRules(), is(3));
        String pcWithRulesXml = new String(pcWithRules.getIpsSrcFile().getContentFromEnclosingResource().readAllBytes(),
                StandardCharsets.UTF_8);
        assertThat(pcWithRulesXml,
                containsString("https://doc.faktorzehn.org/schema/faktor-ips/22.6/PolicyCmptType.xsd"));
        assertThat(pcWithRulesXml, not(containsString("BusinessFunction")));
        assertThat(pcWithRulesXml, not(containsString("appliedForAllBusinessFunctions")));

        String ipsProjectFile = Files.readString(project.getFile(".ipsproject").getLocation().toFile().toPath(),
                StandardCharsets.UTF_8);
        assertThat(ipsProjectFile, not(containsString("businessFunctionsForValidationRules")));
        assertThat(ipsProjectFile, not(containsString("associationsInFormulas")));
        assertThat(ipsProjectFile, containsString("<Property name=\"valueSetMethods\" value=\"Unified\"/>"));
    }

    @SuppressWarnings({ "deprecation", "unchecked" })
    @Test
    public void testMigrate_Categories() throws Exception {
        IIpsProject ipsProject = newIpsProject("Migration_22_6_0Test_Cat");
        IProject project = ipsProject.getProject().unwrap();
        copy("ipsproject", project);
        project.getFile(".ipsproject").delete(true, null);
        project.getFile("ipsproject").move(Path.fromPortableString(".ipsproject"), true, null);
        IFolder productDef = project.getFolder("productdef");
        copy("BasePolicy.ipspolicycmpttype", productDef);
        copy("BaseProduct.ipsproductcmpttype", productDef);
        copy("BaseTableStructure.ipstablestructure", productDef);
        copy("SubPolicy.ipspolicycmpttype", productDef);
        copy("SubProduct.ipsproductcmpttype", productDef);

        IpsModel.reInit();
        ipsProject = IIpsModel.get().getIpsProject("Migration_22_6_0Test_Cat");

        Migration_22_6_0 migration = new Migration_22_6_0(ipsProject, "irrelevant");
        ((IpsMigrationOption<ValueSetMethods>)migration.getOptions().iterator().next())
                .setSelectedValue(ValueSetMethods.ByValueSetType);

        migration.migrate(new NullProgressMonitor());

        IPolicyCmptType basePolicy = (IPolicyCmptType)ipsProject.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE,
                "BasePolicy");
        basePolicy.getIpsSrcFile().save(null);
        String basePolicyXml = new String(basePolicy.getIpsSrcFile().getContentFromEnclosingResource().readAllBytes(),
                StandardCharsets.UTF_8);
        assertThat(basePolicyXml, not(containsString("id=")));
        IProductCmptType baseProduct = basePolicy.findProductCmptType(ipsProject);
        baseProduct.getIpsSrcFile().save(null);
        String baseProductXml = new String(baseProduct.getIpsSrcFile().getContentFromEnclosingResource().readAllBytes(),
                StandardCharsets.UTF_8);
        assertThat(baseProductXml, not(containsString("id=")));
        List<IProductCmptProperty> propertiesA = baseProduct.getCategory("CategoryA")
                .findProductCmptProperties(baseProduct, true, ipsProject);
        assertThat(propertiesA.get(0).getPropertyName(), is("BaseAttribute3"));
        assertThat(propertiesA.get(0).getCategoryPosition(), is(1));
        assertThat(propertiesA.get(1).getPropertyName(), is("BasePolicyAttribute1"));
        assertThat(propertiesA.get(1).getCategoryPosition(), is(2));
        assertThat(propertiesA.get(2).getPropertyName(), is("BaseFormula"));
        assertThat(propertiesA.get(2).getCategoryPosition(), is(3));
        assertThat(propertiesA.get(3).getPropertyName(), is("BaseAttribute1"));
        assertThat(propertiesA.get(3).getCategoryPosition(), is(4));
        assertThat(propertiesA.get(4).getPropertyName(), is("BasePolicyAttribute3"));
        assertThat(propertiesA.get(4).getCategoryPosition(), is(5));
        List<IProductCmptProperty> propertiesB = baseProduct.getCategory("CategoryB")
                .findProductCmptProperties(baseProduct, true, ipsProject);
        assertThat(propertiesB.get(0).getPropertyName(), is("BaseAttribute2"));
        assertThat(propertiesB.get(0).getCategoryPosition(), is(1));
        assertThat(propertiesB.get(1).getPropertyName(), is("baseTableUsage"));
        assertThat(propertiesB.get(1).getCategoryPosition(), is(2));
        assertThat(propertiesB.get(2).getPropertyName(), is("BasePolicyAttribute2"));
        assertThat(propertiesB.get(2).getCategoryPosition(), is(3));

        IPolicyCmptType subPolicy = (IPolicyCmptType)ipsProject.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE,
                "SubPolicy");
        subPolicy.getIpsSrcFile().save(null);
        String subPolicyXml = new String(subPolicy.getIpsSrcFile().getContentFromEnclosingResource().readAllBytes(),
                StandardCharsets.UTF_8);
        assertThat(subPolicyXml, not(containsString("id=")));
        IProductCmptType subProduct = subPolicy.findProductCmptType(ipsProject);
        subProduct.getIpsSrcFile().save(null);
        String subProductXml = new String(subProduct.getIpsSrcFile().getContentFromEnclosingResource().readAllBytes(),
                StandardCharsets.UTF_8);
        assertThat(subProductXml, not(containsString("id=")));
        propertiesA = subProduct.findCategory("CategoryA", ipsProject)
                .findProductCmptProperties(subProduct, true, ipsProject);
        assertThat(propertiesA.get(0).getPropertyName(), is("BaseAttribute3"));
        assertThat(propertiesA.get(0).getCategoryPosition(), is(1));
        assertThat(propertiesA.get(1).getPropertyName(), is("BasePolicyAttribute1"));
        assertThat(propertiesA.get(1).getCategoryPosition(), is(2));
        assertThat(propertiesA.get(2).getPropertyName(), is("BaseFormula"));
        assertThat(propertiesA.get(2).getCategoryPosition(), is(3));
        assertThat(propertiesA.get(3).getPropertyName(), is("BaseAttribute1"));
        assertThat(propertiesA.get(3).getCategoryPosition(), is(4));
        assertThat(propertiesA.get(4).getPropertyName(), is("BasePolicyAttribute3"));
        assertThat(propertiesA.get(4).getCategoryPosition(), is(5));
        assertThat(propertiesA.get(5).getPropertyName(), is("SubAttribute1"));
        assertThat(propertiesA.get(5).getCategoryPosition(), is(6));
        propertiesB = subProduct.findCategory("CategoryB", ipsProject)
                .findProductCmptProperties(subProduct, true, ipsProject);
        assertThat(propertiesB.get(0).getPropertyName(), is("BaseAttribute2"));
        assertThat(propertiesB.get(0).getCategoryPosition(), is(1));
        assertThat(propertiesB.get(1).getPropertyName(), is("baseTableUsage"));
        assertThat(propertiesB.get(1).getCategoryPosition(), is(2));
        assertThat(propertiesB.get(2).getPropertyName(), is("BasePolicyAttribute2"));
        assertThat(propertiesB.get(2).getCategoryPosition(), is(3));
        List<IProductCmptProperty> propertiesC = subProduct.getCategory("CategoryC")
                .findProductCmptProperties(subProduct, true, ipsProject);
        assertThat(propertiesC.get(0).getPropertyName(), is("SubAttribute3"));
        assertThat(propertiesC.get(0).getCategoryPosition(), is(1));
        assertThat(propertiesC.get(1).getPropertyName(), is("SubAttribute2"));
        assertThat(propertiesC.get(1).getCategoryPosition(), is(2));

        String ipsProjectFile = Files.readString(project.getFile(".ipsproject").getLocation().toFile().toPath(),
                StandardCharsets.UTF_8);
        assertThat(ipsProjectFile, containsString("<Property name=\"valueSetMethods\" value=\"ByValueSetType\"/>"));
    }

    @SuppressWarnings({ "deprecation", "unchecked" })
    @Test
    public void testMigrate_Manifest() throws Exception {
        IIpsProject ipsProject = newIpsProject("Migration_22_6_0Test_MF");
        IProject project = ipsProject.getProject().unwrap();
        copy("ipsproject", project);
        project.getFile(".ipsproject").delete(true, null);
        project.getFile("ipsproject").move(Path.fromPortableString(".ipsproject"), true, null);
        IFolder metaInf = project.getFolder("META-INF");
        metaInf.create(true, true, null);
        IFile manifest = copy("MANIFEST.MF", metaInf);

        IpsModel.reInit();
        ipsProject = IIpsModel.get().getIpsProject("Migration_22_6_0Test_MF");

        Migration_22_6_0 migration = new Migration_22_6_0(ipsProject, "irrelevant");
        ((IpsMigrationOption<ValueSetMethods>)migration.getOptions().iterator().next())
                .setSelectedValue(ValueSetMethods.Unified);

        migration.migrate(new NullProgressMonitor());

        String manifestContent = Files.readString(manifest.getLocation().toFile().toPath(), StandardCharsets.UTF_8);
        manifestContent = manifestContent.replace("\n ", "");
        assertThat(manifestContent, containsString("Manifest-Version: 1.0"));
        assertThat(manifestContent,
                containsString("Fips-GeneratorConfig: org.faktorips.devtools.stdbuilder.ipsstdbuilderset"));
        assertThat(manifestContent, containsString("valueSetMethods=\"Unified\""));
    }

    @SuppressWarnings({ "deprecation", "unchecked" })
    @Test
    public void testMigrate_Manifest_MissingVersion() throws Exception {
        IIpsProject ipsProject = newIpsProject("Migration_22_6_0Test_MF2");
        IProject project = ipsProject.getProject().unwrap();
        copy("ipsproject", project);
        project.getFile(".ipsproject").delete(true, null);
        project.getFile("ipsproject").move(Path.fromPortableString(".ipsproject"), true, null);
        IFolder metaInf = project.getFolder("META-INF");
        metaInf.create(true, true, null);
        IFile manifestTmp = copy("MANIFEST.MF_missingVersion", metaInf);
        IFile manifest = metaInf.getFile("MANIFEST.MF");
        manifestTmp.move(manifest.getFullPath(), true, null);

        IpsModel.reInit();
        ipsProject = IIpsModel.get().getIpsProject("Migration_22_6_0Test_MF2");

        Migration_22_6_0 migration = new Migration_22_6_0(ipsProject, "irrelevant");
        ((IpsMigrationOption<ValueSetMethods>)migration.getOptions().iterator().next())
                .setSelectedValue(ValueSetMethods.Both);

        migration.migrate(new NullProgressMonitor());

        String manifestContent = Files.readString(manifest.getLocation().toFile().toPath(), StandardCharsets.UTF_8);
        manifestContent = manifestContent.replace("\n ", "");
        assertThat(manifestContent, containsString("Manifest-Version: 1.0"));
        assertThat(manifestContent,
                containsString("Fips-GeneratorConfig: org.faktorips.devtools.stdbuilder.ipsstdbuilderset"));
        assertThat(manifestContent, containsString("valueSetMethods=\"Both\""));
    }

    private IFile copy(String fileName, IContainer container) throws CoreException {
        IFile file = container.getFile(new Path(fileName));
        InputStream inputStream = getClass().getResourceAsStream(getClass().getSimpleName() + '.' + fileName);
        if (file.exists()) {
            file.setContents(inputStream, true, true, null);
        } else {
            file.create(inputStream, true, null);
        }
        return file;
    }

}
