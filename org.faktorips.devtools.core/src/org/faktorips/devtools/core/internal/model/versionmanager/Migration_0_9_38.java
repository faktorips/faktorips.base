/**
 * 
 */
package org.faktorips.devtools.core.internal.model.versionmanager;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.versionmanager.AbstractMigrationOperation;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * The following migration steps will be performed:<ul>
 * <li>Migration of ips test cases.
 * </ul>
 * 
 * @author Joerg Ortmann
 */
public class Migration_0_9_38 extends AbstractMigrationOperation {

    public Migration_0_9_38(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return Messages.Migration_0_9_38_Description;
    }

    /**
     * {@inheritDoc}
     */
    public String getTargetVersion() {
        return "0.9.39"; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
        return migrateTestCases(monitor);
    }

    /*
     * Mirgation of ips test cases. Replace product identifier from runtime id to the full qualified name.
     */
    private MessageList migrateTestCases(IProgressMonitor monitor) throws CoreException {
        MessageList messageList = new MessageList();
        IIpsObject[] testCases = getIpsProject().findIpsObjects(IpsObjectType.TEST_CASE);
        // mirgrate all test cases
        monitor.beginTask(Messages.Migration_0_9_38_Task, testCases.length);
        for (int i = 0; i < testCases.length; i++) {
            monitor.subTask(testCases[i].getQualifiedName());
            ITestPolicyCmpt[] testPolicyCmpts = ((ITestCase)testCases[i]).getTestPolicyCmpts();
            // migrate the test policy cmpt
            for (int j = 0; j < testPolicyCmpts.length; j++) {
                messageList.add(migrateTestPolicyCmpt(testPolicyCmpts[j]));
            }
            monitor.worked(1);
        }
        monitor.done();
        return messageList;
    }

    /*
     * Migrates the given test case
     */
    private MessageList migrateTestPolicyCmpt(ITestPolicyCmpt testPolicyCmpt) throws CoreException {
        MessageList messageList = new MessageList();
        if (!StringUtils.isEmpty(testPolicyCmpt.getProductCmpt())) {
            IProductCmpt productCmpt = testPolicyCmpt.findProductCmpt();
            if (productCmpt == null) {
                // the previous version stored the runtime id instead of the qualified name of the product cmpt
                productCmpt = getIpsProject().findProductCmpt(testPolicyCmpt.getProductCmpt());
            }
            if (productCmpt != null) {
                // fix: store the qualified name
                testPolicyCmpt.setProductCmpt(productCmpt.getQualifiedName());
            }
            else {
                String text = NLS.bind(Messages.Migration_0_9_38_Message_ProductComponentNotFound, testPolicyCmpt.getProductCmpt());
                Message msg = new Message(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_NOT_EXISTS, text, Message.ERROR, this,
                        ITestPolicyCmpt.PROPERTY_PRODUCTCMPT);
                messageList.add(msg);
            }
        }
        // migrate the childs of test policy cmpt
        ITestPolicyCmptRelation[] relations = testPolicyCmpt.getTestPolicyCmptRelations();
        for (int i = 0; i < relations.length; i++) {
            if (relations[i].isComposition()) {
                ITestPolicyCmpt target = relations[i].findTarget();
                if (target != null) {
                    messageList.add(migrateTestPolicyCmpt(target));
                }
                else {
                    String text = NLS.bind(Messages.Migration_0_9_38_Message_TestPolicyCmptNotFound, relations[i].getTarget());
                    Message msg = new Message(ITestPolicyCmptRelation.MSGCODE_PREFIX + Messages.Migration_0_9_38_MsgCode_TargetNotFound, text, Message.ERROR, this,
                            ITestPolicyCmptRelation.PROPERTY_TARGET);
                    messageList.add(msg);
                }
            }
        }
        return messageList;
    }
}
