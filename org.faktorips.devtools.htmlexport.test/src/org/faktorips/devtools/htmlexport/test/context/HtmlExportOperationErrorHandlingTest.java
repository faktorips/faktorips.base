/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.test.context;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.htmlexport.HtmlExportOperation;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.standard.StandardDocumentorScript;

public class HtmlExportOperationErrorHandlingTest extends AbstractHtmlExportTest {

    public class MockIpsSrcFile extends IpsSrcFile {

        public MockIpsSrcFile(IIpsElement parent, String name) {
            super(parent, name);
        }

        @Override
        public IpsObjectType getIpsObjectType() {
            return IpsObjectType.POLICY_CMPT_TYPE;
        }
    }

    public void testSrcFileWithoutIpsObject() throws IOException {
        context = new DocumentationContext() {
            private boolean alreadyAdded = false;

            @Override
            public List<IIpsSrcFile> getDocumentedSourceFiles() {

                List<IIpsSrcFile> documentedSourceFiles = super.getDocumentedSourceFiles();
                if (alreadyAdded) {
                    return documentedSourceFiles;
                }

                try {
                    IpsSrcFile ipsSrcFile = new MockIpsSrcFile(
                            ipsProject.getIpsPackageFragmentRoots()[0].getDefaultIpsPackageFragment(),
                            "MichGibtEsNicht." + IpsObjectType.POLICY_CMPT_TYPE.getFileExtension());

                    documentedSourceFiles.add(ipsSrcFile);
                } catch (CoreException e) {
                    throw new RuntimeException("Error running the test: " + e.getMessage(), e);
                }

                alreadyAdded = true;
                return documentedSourceFiles;
            }

        };
        initContext();

        operation = new HtmlExportOperation(context);

        createStandardProjekt();

        context.setPath(zielpfad);
        context.addDocumentorScript(new StandardDocumentorScript());

        context.setDocumentedIpsObjectTypes(context.getIpsProject().getIpsModel().getIpsObjectTypes());

        try {
            operation.run(new NullProgressMonitor());
            fail("sollte CoreException werfen");
        } catch (CoreException e) {
            // nix zu tun
        }
    }
}
