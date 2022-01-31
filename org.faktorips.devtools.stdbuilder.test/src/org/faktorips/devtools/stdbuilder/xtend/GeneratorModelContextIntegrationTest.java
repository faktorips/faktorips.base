/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.util.PathUtil;
import org.faktorips.devtools.model.CreateIpsArchiveOperation;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.internal.ipsproject.bundle.IpsBundleEntry;
import org.faktorips.devtools.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xmodel.GeneratorConfig;
import org.junit.Test;

public class GeneratorModelContextIntegrationTest extends AbstractStdBuilderTest {

    @Test
    public void testGetGeneratorConfig_IpsObject_FromLibrary() throws CoreException, IOException {
        IIpsProject libIpsProject = newIpsProject("lib");
        newPolicyCmptTypeWithoutProductCmptType(libIpsProject, "lib.Policy");
        createManifest(libIpsProject);
        File libFile = createBundle(libIpsProject);
        ((IProject)libIpsProject.getProject().unwrap()).close(null);
        IpsObjectPath ipsObjectPath = (IpsObjectPath)ipsProject.getIpsObjectPath();
        IpsBundleEntry ipsBundleEntry = createBundleEntry(libFile, ipsObjectPath);
        addEntry(ipsObjectPath, ipsBundleEntry);

        IIpsArtefactBuilderSet builderSet = ipsProject.getIpsArtefactBuilderSet();
        assertThat(builderSet, is(instanceOf(StandardBuilderSet.class)));

        GeneratorModelContext generatorModelContext = ((StandardBuilderSet)builderSet).getGeneratorModelContext();
        IPolicyCmptType subPolicy = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "my.SubPolicy");
        subPolicy.setSupertype("lib.Policy");
        IPolicyCmptType policy = ipsProject.findPolicyCmptType("lib.Policy");
        assertThat(policy, is(notNullValue()));

        GeneratorConfig libGeneratorConfig = generatorModelContext.getGeneratorConfig(policy);
        GeneratorConfig generatorConfig = generatorModelContext.getGeneratorConfig(subPolicy);

        assertThat(libGeneratorConfig, is(not(generatorConfig)));
        assertThat(generatorConfig.getChangesOverTimeNamingConvention().getId(),
                is(IChangesOverTimeNamingConvention.VAA));
        assertThat(libGeneratorConfig.getChangesOverTimeNamingConvention().getId(),
                is(IChangesOverTimeNamingConvention.FAKTOR_IPS));
    }

    private void createManifest(IIpsProject libIpsProject) throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue(Name.MANIFEST_VERSION.toString(), "1.0");
        manifest.getMainAttributes().putValue(IpsBundleManifest.HEADER_OBJECT_DIR, "ipsobjects");
        manifest.getMainAttributes().putValue(IpsBundleManifest.HEADER_GENERATOR_CONFIG,
                StandardBuilderSet.ID + ";" + StandardBuilderSet.CONFIG_PROPERTY_CHANGES_OVER_TIME_NAMING_CONVENTION
                        + "=\"" + IChangesOverTimeNamingConvention.FAKTOR_IPS + "\";"
                        + StandardBuilderSet.CONFIG_PROPERTY_PUBLISHED_INTERFACES + "=" + Boolean.TRUE.toString());
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        manifest.write(out);
        out.close();
        AFolder metaInf = libIpsProject.getProject().getFolder("src/META-INF");
        metaInf.create(null);
        AFile manifestWorkspaceFile = libIpsProject.getProject().getFile("src/META-INF/MANIFEST.MF");
        manifestWorkspaceFile.create(in, null);
    }

    private File createBundle(IIpsProject libIpsProject) throws IOException {
        File libFile = File.createTempFile("externalArchiveFile", ".jar");
        libFile.deleteOnExit();
        CreateIpsArchiveOperation createIpsArchiveOperation = new CreateIpsArchiveOperation(libIpsProject, libFile);
        createIpsArchiveOperation.setInclJavaSources(true);
        createIpsArchiveOperation.setInclJavaBinaries(true);
        createIpsArchiveOperation.run(null);
        return libFile;
    }

    private IpsBundleEntry createBundleEntry(File libFile, IpsObjectPath ipsObjectPath) throws IOException {
        IpsBundleEntry ipsBundleEntry = new IpsBundleEntry(ipsObjectPath);
        Path libPath = PathUtil.fromOSString(libFile.getAbsolutePath());
        ipsBundleEntry.initStorage(libPath);
        return ipsBundleEntry;
    }

    private void addEntry(IpsObjectPath ipsObjectPath, IpsBundleEntry ipsBundleEntry) throws CoreRuntimeException {
        List<IIpsObjectPathEntry> entries = new LinkedList<>(
                Arrays.asList(ipsObjectPath.getEntries()));
        entries.add(ipsBundleEntry);
        ipsObjectPath.setEntries(entries.toArray(new IIpsObjectPathEntry[entries.size()]));
        ipsProject.setIpsObjectPath(ipsObjectPath);
    }

}
