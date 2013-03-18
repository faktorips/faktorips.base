/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.ManifestElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IpsBundleManifestTest {

    private static final String MY_BASE_PACKAGE = "myBasePackage";

    private static final String MY_BASE_PACKAGE2 = "myBasePackage";

    private static final String MY_OBJECT_DIR = "myObjectdir";

    private static final String INVALID_OBJECT_DIR = "invalidObjectdir";

    private static final String MY_SRC_OUT = "mySrcOut";

    private static final String MY_SRC_OUT2 = "mySrcOut2";

    private static final String MY_RESOURCE_OUT = "myResourceOut";

    private static final String MY_RESOURCE_OUT2 = "myResourceOut2";

    private static final String MY_TOC = "myToc";

    private static final String MY_MESSAGES = "myMessages";

    @Mock
    private Manifest manifest;

    private IpsBundleManifest ipsBundleManifest;

    @Mock
    private IpsProject ipsProject;

    @Before
    public void mockManifest() {
        Attributes attributes = mock(Attributes.class);
        when(manifest.getMainAttributes()).thenReturn(attributes);
        when(attributes.getValue(IpsBundleManifest.HEADER_BASE_PACKAGE)).thenReturn(MY_BASE_PACKAGE);
        when(attributes.getValue(IpsBundleManifest.HEADER_SRC_OUT)).thenReturn(MY_SRC_OUT);
        when(attributes.getValue(IpsBundleManifest.HEADER_RESOURCE_OUT)).thenReturn(MY_RESOURCE_OUT);
        when(attributes.getValue(IpsBundleManifest.HEADER_OBJECT_DIR)).thenReturn(
                MY_OBJECT_DIR + " ; toc=\"" + MY_TOC + "\";messages=\"" + MY_MESSAGES + "\"");

        Attributes attributesForObjectDir = mock(Attributes.class);
        when(manifest.getAttributes(MY_OBJECT_DIR)).thenReturn(attributesForObjectDir);
        when(attributesForObjectDir.getValue(IpsBundleManifest.HEADER_BASE_PACKAGE)).thenReturn(MY_BASE_PACKAGE2);
        when(attributesForObjectDir.getValue(IpsBundleManifest.HEADER_SRC_OUT)).thenReturn(MY_SRC_OUT2);
        when(attributesForObjectDir.getValue(IpsBundleManifest.HEADER_RESOURCE_OUT)).thenReturn(MY_RESOURCE_OUT2);
    }

    @Before
    public void mockProject() {
        IProject project = mock(IProject.class);
        when(ipsProject.getProject()).thenReturn(project);
        IFolder folder = mock(IFolder.class);
        when(project.getFolder(anyString())).thenReturn(folder);
    }

    @Before
    public void createIpsBundleManifest() throws Exception {
        ipsBundleManifest = new IpsBundleManifest(manifest);
    }

    @Test
    public void testGetBasePackage() throws Exception {
        when(manifest.getMainAttributes().getValue(IpsBundleManifest.HEADER_BASE_PACKAGE)).thenReturn(
                " " + MY_BASE_PACKAGE + " ");

        String basePackage = ipsBundleManifest.getBasePackage();

        assertEquals(MY_BASE_PACKAGE, basePackage);
    }

    @Test
    public void testGetBasePackage_trim() throws Exception {
        String basePackage = ipsBundleManifest.getBasePackage();

        assertEquals(MY_BASE_PACKAGE, basePackage);
    }

    @Test
    public void testGetBasePackage_forObjectDir() throws Exception {
        String basePackage = ipsBundleManifest.getBasePackage(MY_OBJECT_DIR);

        assertEquals(MY_BASE_PACKAGE2, basePackage);
    }

    @Test
    public void testGetBasePackage_forObjectDirTrim() throws Exception {
        when(manifest.getAttributes(MY_OBJECT_DIR).getValue(IpsBundleManifest.HEADER_BASE_PACKAGE)).thenReturn(
                " " + MY_BASE_PACKAGE2 + " ");

        String basePackage = ipsBundleManifest.getBasePackage(MY_OBJECT_DIR);

        assertEquals(MY_BASE_PACKAGE2, basePackage);
    }

    @Test
    public void testGetBasePackage_forInvalidObjectDir() throws Exception {
        String basePackage = ipsBundleManifest.getBasePackage(INVALID_OBJECT_DIR);

        assertEquals(MY_BASE_PACKAGE, basePackage);
    }

    @Test
    public void testGetSourcecodeOutput() throws Exception {
        String srcOutput = ipsBundleManifest.getSourcecodeOutput();

        assertEquals(MY_SRC_OUT, srcOutput);
    }

    @Test
    public void testGetSourcecodeOutput_forObjectDir() throws Exception {
        String srcOutput = ipsBundleManifest.getSourcecodeOutput(MY_OBJECT_DIR);

        assertEquals(MY_SRC_OUT2, srcOutput);
    }

    @Test
    public void testGetSourcecodeOutput_trim() throws Exception {
        when(manifest.getMainAttributes().getValue(IpsBundleManifest.HEADER_SRC_OUT)).thenReturn(MY_SRC_OUT + " ");

        String srcOutput = ipsBundleManifest.getSourcecodeOutput();

        assertEquals(MY_SRC_OUT, srcOutput);
    }

    @Test
    public void testGetSourcecodeOutput_objectDirAndTrim() throws Exception {
        when(manifest.getAttributes(MY_OBJECT_DIR).getValue(IpsBundleManifest.HEADER_SRC_OUT)).thenReturn(
                MY_SRC_OUT2 + " ");

        String srcOutput = ipsBundleManifest.getSourcecodeOutput(MY_OBJECT_DIR);

        assertEquals(MY_SRC_OUT2, srcOutput);
    }

    @Test
    public void testGetSourcecodeOutput_forInvalidObjectDir() throws Exception {
        String srcOutput = ipsBundleManifest.getSourcecodeOutput(INVALID_OBJECT_DIR);

        assertEquals(MY_SRC_OUT, srcOutput);
    }

    @Test
    public void testGetResourceOutput() throws Exception {
        String srcOutput = ipsBundleManifest.getResourceOutput();

        assertEquals(MY_RESOURCE_OUT, srcOutput);
    }

    @Test
    public void testGetResourceOutput_trim() throws Exception {
        when(manifest.getMainAttributes().getValue(IpsBundleManifest.HEADER_RESOURCE_OUT)).thenReturn(
                MY_RESOURCE_OUT + " ");

        String srcOutput = ipsBundleManifest.getResourceOutput();

        assertEquals(MY_RESOURCE_OUT, srcOutput);
    }

    @Test
    public void testGetResourceOutput_forObjectDir() throws Exception {
        String srcOutput = ipsBundleManifest.getResourceOutput(MY_OBJECT_DIR);

        assertEquals(MY_RESOURCE_OUT2, srcOutput);
    }

    @Test
    public void testGetResourceOutput_forObjectDirTrim() throws Exception {
        when(manifest.getAttributes(MY_OBJECT_DIR).getValue(IpsBundleManifest.HEADER_RESOURCE_OUT)).thenReturn(
                MY_RESOURCE_OUT2 + " ");

        String srcOutput = ipsBundleManifest.getResourceOutput(MY_OBJECT_DIR);

        assertEquals(MY_RESOURCE_OUT2, srcOutput);
    }

    @Test
    public void testGetResourceOutput_forInvalidObjectDir() throws Exception {
        String srcOutput = ipsBundleManifest.getResourceOutput(INVALID_OBJECT_DIR);

        assertEquals(MY_RESOURCE_OUT, srcOutput);
    }

    @Test
    public void testGetObjectDirs() throws Exception {
        List<IPath> objectDir = ipsBundleManifest.getObjectDirs();

        assertEquals(1, objectDir.size());
        assertEquals(new Path(MY_OBJECT_DIR), objectDir.get(0));
    }

    @Test
    public void testGetObjectDirElements_empty() throws Exception {
        ipsBundleManifest = new IpsBundleManifest(mock(Manifest.class));
        ManifestElement[] objectDir = ipsBundleManifest.getObjectDirElements();

        assertEquals(0, objectDir.length);
    }

    @Test
    public void testGetObjectDirElements_noValue() throws Exception {
        Attributes attributes = mock(Attributes.class);
        when(manifest.getMainAttributes()).thenReturn(attributes);
        ManifestElement[] objectDir = ipsBundleManifest.getObjectDirElements();

        assertEquals(0, objectDir.length);
    }

    @Test
    public void testGetObjectDirElements() throws Exception {
        ManifestElement[] objectDir = ipsBundleManifest.getObjectDirElements();

        assertEquals(1, objectDir.length);
        assertEquals(1, objectDir[0].getValueComponents().length);
        assertEquals(MY_OBJECT_DIR, objectDir[0].getValue());
    }

    @Test
    public void testGetTocPath() throws Exception {
        ManifestElement objectDirElement = ipsBundleManifest.getObjectDirElements()[0];

        String toc = ipsBundleManifest.getTocPath(objectDirElement);

        assertEquals(MY_TOC, toc);
    }

    @Test
    public void testGetValidationMessagesBundle() throws Exception {
        ManifestElement objectDirElement = ipsBundleManifest.getObjectDirElements()[0];

        String messages = ipsBundleManifest.getValidationMessagesBundle(objectDirElement);

        assertEquals(MY_MESSAGES, messages);
    }

}
