/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.context;

import static org.junit.Assert.fail;

import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.htmlexport.HtmlExportOperation;
import org.faktorips.devtools.htmlexport.TestUtil;
import org.faktorips.devtools.htmlexport.standard.StandardDocumentorScript;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.junit.Test;

public class HtmlExportOperationErrorHandlingTest extends AbstractHtmlExportPluginTest {

    public class MockIpsSrcFile extends IpsSrcFile {

        public MockIpsSrcFile(IIpsElement parent, String name) {
            super(parent, name);
        }

        @Override
        public IpsObjectType getIpsObjectType() {
            return IpsObjectType.POLICY_CMPT_TYPE;
        }
    }

    @Test
    public void testSrcFileWithoutIpsObject() throws Exception {
        context = new DocumentationContext() {
            private boolean alreadyAdded = false;

            @Override
            public Set<IIpsSrcFile> getDocumentedSourceFiles() {

                Set<IIpsSrcFile> documentedSourceFiles = super.getDocumentedSourceFiles();
                if (alreadyAdded) {
                    return documentedSourceFiles;
                }

                IIpsSrcFile ipsSrcFile = new MockIpsSrcFile(
                        ipsProject.getIpsPackageFragmentRoots()[0].getDefaultIpsPackageFragment(),
                        "MichGibtEsNicht." + IpsObjectType.POLICY_CMPT_TYPE.getFileExtension());

                documentedSourceFiles.add(ipsSrcFile);

                alreadyAdded = true;
                return documentedSourceFiles;
            }

        };
        initContext();

        operation = new HtmlExportOperation(context);

        createStandardProjekt();

        context.setPath(zielpfad);
        context.addDocumentorScript(new StandardDocumentorScript(new TestUtil().createMockIoHandler()));

        context.setDocumentedIpsObjectTypes(context.getIpsProject().getIpsModel().getIpsObjectTypes());

        Display.getDefault().syncExec(() -> {
            try {
                operation.run(new NullProgressMonitor());
                fail("sollte IpsException werfen");
            } catch (IpsException e1) {
                // nix zu tun
            } catch (Exception e2) {
                fail(e2.getMessage());
            }
        });
    }
}
