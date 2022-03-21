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
import org.faktorips.devtools.model.versionmanager.options.IpsMigrationOption;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.junit.Test;

public class Migration_22_6_0Test extends AbstractStdBuilderTest {

    @SuppressWarnings({ "deprecation", "unchecked" })
    @Test
    public void testMigrate() throws Exception {
        IIpsProject ipsProject = newIpsProject("Migration_22_6_0Test");
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
        ipsProject = IIpsModel.get().getIpsProject("Migration_22_6_0Test");

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
