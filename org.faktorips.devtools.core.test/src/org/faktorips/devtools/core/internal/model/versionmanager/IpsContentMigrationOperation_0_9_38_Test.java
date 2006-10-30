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

package org.faktorips.devtools.core.internal.model.versionmanager;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ITestAnswerProvider;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectProperties;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsContentMigrationOperation;
import org.faktorips.util.message.MessageList;

/**
 * 
 * @author Joerg Ortmann
 */
public class IpsContentMigrationOperation_0_9_38_Test  extends AbstractIpsPluginTest {

    private IIpsProject project;
    private IIpsPackageFragmentRoot root;
    private AbstractIpsContentMigrationOperation operation;
    
    /*
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        
        project = newIpsProject("TestProject");
        setMinVersion("0.9.38");
        
        root = project.getIpsPackageFragmentRoots()[0];
        
        IpsPlugin.getDefault().setTestMode(true);
        IpsPlugin.getDefault().setTestAnswerProvider(new ITestAnswerProvider() {
            public Object getAnswer() {
                return getClass().getClassLoader();
            }
            public String getStringAnswer() {
                return null;
            }
            public int getIntAnswer() {
                return 0;
            }
            public boolean getBooleanAnswer() {
                return false;
            }
        });
        operation = IpsPlugin.getDefault().getMigrationOperation(project);
    }
    
    private void setMinVersion(String version) throws CoreException {
        IIpsProjectProperties props = project.getProperties();
        props.setMinRequiredVersionNumber("org.faktorips.feature", version);
        project.setProperties(props);
    }
    
    public void testExecute() throws Exception {
        ITestCase testCase = createFailureContent();
        MessageList ml = testCase.validate();
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_NOT_EXISTS));
        
        assertNotNull(operation);
        assertFalse(operation.isEmpty());
        operation.run(null);
        ml = operation.getMessageList();
        assertEquals(0, ml.getNoOfMessages());

        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_NOT_EXISTS));
    }

    private ITestCase createFailureContent() throws CoreException {
        IProductCmpt productCmpt1 = newProductCmpt(root, "test.product1");
        productCmpt1.setRuntimeId("product1");
        IProductCmpt productCmpt2 = newProductCmpt(root, "test.product2");
        productCmpt2.setRuntimeId("product2");
        
        ITestCase testCase = (ITestCase) newIpsObject(root, IpsObjectType.TEST_CASE, "test.testCase1");
        ITestPolicyCmpt testPolicyCmpt = testCase.newTestPolicyCmpt();
        testPolicyCmpt.setProductCmpt("product1");
        ITestPolicyCmptRelation relation = testPolicyCmpt.newTestPolicyCmptRelation();
        testPolicyCmpt = relation.newTargetTestPolicyCmptChild();
        testPolicyCmpt.setProductCmpt("product2");
        return testCase;
    }
}
