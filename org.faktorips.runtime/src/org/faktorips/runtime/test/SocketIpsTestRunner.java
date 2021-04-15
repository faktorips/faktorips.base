/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.faktorips.runtime.ClassloaderRuntimeRepository;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Socket test runner. Opens a socket connection to a given socket server port and runs the given
 * ips tests. The result will be written to the server socket.
 * 
 * @author Joerg Ortmann
 */
public class SocketIpsTestRunner extends AbstractIpsTestRunner {
    /** Socket messages */
    public static final String ALL_TESTS_STARTED = "Starting tests ";
    public static final String ALL_TESTS_FINISHED = "Ending tests ";
    public static final String TEST_STARTED = "Starting test ";
    public static final String TEST_FINISHED = "Ending test ";
    public static final String TEST_FAILED = "Test failed ";
    public static final String TEST_FAILED_DELIMITERS = "|";
    public static final String TEST_ERROR = "Test error ";
    public static final String TEST_ERROR_END = "ERROR_END";
    public static final String TEST_ERROR_MESSAGE_INDICATOR = ">>>";
    public static final String TEST_ERROR_STACK_INDICATOR = "---";

    private int port;
    private Socket socket;
    private PrintWriter writer;

    public SocketIpsTestRunner() {
        // nothing to do
    }

    public SocketIpsTestRunner(int port, String repositoryPackages, String additionalRepositoryPackages) {
        this();
        this.port = port;
        setRepositoryPackages(repositoryPackages);
        setAdditionalRepositoryPackages(additionalRepositoryPackages);
    }

    /**
     * The entry point for the socket test runner. The arguments are: args[0]: the port number to
     * connect to args[1]: package name of the classpath repository args[2]: Name of the testsuite
     * to run args[3]: additional classpath repositories (to find objects in the runtime
     * environment)
     */
    public static void main(String[] args) throws Exception {
        String suiteName = "";
        String additionalRepositoryPackages = "";
        if (args.length >= 3) {
            suiteName = args[2];
        }
        if (args.length >= 4) {
            additionalRepositoryPackages = args[3];
        }
        new SocketIpsTestRunner(Integer.parseInt(args[0]), args[1], additionalRepositoryPackages).run(suiteName);
    }

    @Override
    public void run(String name) {

        long testStartTime = System.currentTimeMillis();
        try {
            Exception exceptionDuringTestCount = null;
            openClientSocket();
            if (writer == null) {
                return;
            }

            int testCount = 0;
            try {
                testCount = super.countTests(name);
                // CSOFF: IllegalCatch
            } catch (Exception e) {
                // CSON: IllegalCatch
                exceptionDuringTestCount = e;
            }
            // format: SocketIpsTestRunner.ALL_TESTS_STARTED(<count>)
            // [<repositoryPackage>].[<testPackage>]:<testQualifiedName>{<testFullPath>},...
            writer.print(ALL_TESTS_STARTED);
            writer.print("(");
            writer.print(testCount);
            writer.print(") [");
            writer.print(getRepositoryPackages());
            writer.print("].[");
            writer.print(name);
            writer.print("]");
            printAllTests(testCount);
            writer.println();
            if (exceptionDuringTestCount != null) {
                throw exceptionDuringTestCount;
            }
            testStartTime = System.currentTimeMillis();
            super.run(name);
            // CSOFF: IllegalCatch
        } catch (Throwable e) {
            // CSON: IllegalCatch
            // an exception occurred
            // inform the socket listener about the error
            postError(e, null);
        }
        if (writer != null) {
            writer.println(ALL_TESTS_FINISHED + (System.currentTimeMillis() - testStartTime));
            closeClientSocket();
        }
    }

    /*
     * Adds all test as String with the following format: <br>
     * :<testQualifiedName>{<testFullPath>},...
     */
    private void printAllTests(int testCount) {
        List<IpsTest2> tests = getTests();
        List<IpsTest2> testCases = new ArrayList<>(testCount);
        writer.print(":");
        // get all test cases as flat structured list
        for (IpsTest2 currTest : tests) {
            addTestCasesAsFlatList(currTest, testCases);
        }
        // print all test cases in the writer
        for (IpsTest2 testCase2 : testCases) {
            printTestCase2(testCase2);
            writer.print(",");
        }
    }

    /*
     * Adds all test cases inside in the given ips test to the given list.
     */
    private void addTestCasesAsFlatList(IpsTest2 currTest, List<IpsTest2> testCases) {
        if (currTest instanceof IpsTestCaseBase) {
            testCases.add(currTest);
        } else if (currTest instanceof IpsTestSuite) {
            List<IpsTest2> testsInSuite = ((IpsTestSuite)currTest).getTests();
            for (IpsTest2 testInSuite : testsInSuite) {
                addTestCasesAsFlatList(testInSuite, testCases);
            }
        } else {
            throw new RuntimeException("Wrong instance of ips test: " + currTest.getClass().getName());
        }
    }

    @Override
    protected List<IRuntimeRepository> createRepositories() throws Exception {
        List<String> repositoryNameList = getRepositoryListFromInputString(getRepositoryPackages());
        List<IRuntimeRepository> runtimeRepositories = new ArrayList<>(repositoryNameList.size());
        for (String repositoryName : repositoryNameList) {
            runtimeRepositories.add(ClassloaderRuntimeRepository.create(repositoryName, getClassLoader()));
        }
        return runtimeRepositories;
    }

    /*
     * Opens the client socket connection
     */
    private void openClientSocket() throws Exception {
        Exception lastException = null;
        // try two times to connect
        for (int i = 0; i < 2; i++) {
            try {
                lastException = null;
                socket = new Socket("localhost", port); //$NON-NLS-1$
                writer = new PrintWriter(socket.getOutputStream(), true);
                return;
            } catch (UnknownHostException e) {
                lastException = e;
            } catch (IOException e) {
                lastException = e;
            }
            try {
                // wait and try to connect again
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                // ignore exceptions
            }
        }
        if (lastException != null) {
            throw new Exception(lastException);
        }
    }

    private void closeClientSocket() {
        writer.close();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void testStarted(IpsTest2 test) {
        // format: TEST_CASE_STARTED<qualifiedName>{<fullPath>}
        writer.print(TEST_STARTED);
        printTestCase2(test);
        writer.println();
    }

    /*
     * Prints the given test in the writer with the following format: <qualifiedName>{<fullPath>}
     */
    private void printTestCase2(IpsTest2 test) {
        writer.print(test.getQualifiedName());
        writer.print("{");
        writer.print(test.getFullPath());
        writer.print("}");
    }

    @Override
    public void testFinished(IpsTest2 test) {
        writer.print(TEST_FINISHED);
        writer.println(test.getQualifiedName());
    }

    @Override
    public void testFailureOccured(IpsTestFailure failure) {
        if (failure.isError()) {
            postError(failure.getThrowable(), failure.getTestCase().getQualifiedName());
        } else {
            writer.print(TEST_FAILED);
            writer.println(testFailureToStr(failure));
        }
    }

    private String testFailureToStr(IpsTestFailure failure) {
        StringBuilder formattedFailure = new StringBuilder();
        // format:
        // qualifiedName|testObject|testedAttribute|expectedValue|actualValue|message[|actualValueAsString]
        formattedFailure.append(failure.getTestCase().getQualifiedName());
        formattedFailure.append(TEST_FAILED_DELIMITERS);
        formattedFailure.append(failure.getTestObject() == null ? "<null>" : failure.getTestObject());
        formattedFailure.append(TEST_FAILED_DELIMITERS);
        formattedFailure.append(failure.getTestedAttribute() == null ? "<null>" : failure.getTestedAttribute());
        formattedFailure.append(TEST_FAILED_DELIMITERS);
        formattedFailure.append(failure.getExpectedValue() == null ? "<null>" : failure.getExpectedValue());
        formattedFailure.append(TEST_FAILED_DELIMITERS);
        formattedFailure.append(failure.getActualValue() == null ? "<null>" : failure.getActualValue());
        formattedFailure.append(TEST_FAILED_DELIMITERS);
        formattedFailure.append(failure.getMessage() == null ? "<null>" : failure.getMessage());
        formattedFailure.append(TEST_FAILED_DELIMITERS);

        if (actualValueStringRepresentationDiffers(failure)) {
            formattedFailure.append(failure.getActualValueAsString());
            formattedFailure.append(TEST_FAILED_DELIMITERS);
        }

        return formattedFailure.toString();
    }

    private boolean actualValueStringRepresentationDiffers(IpsTestFailure failure) {
        return failure.getActualValue() != null
                && !failure.getActualValue().toString().equals(failure.getActualValueAsString());
    }

    /*
     * Informs the socket listener about an error.
     */
    private void postError(Throwable t, String qualifiedTestName) {
        // format: qualifiedTestName{StacktraceLine1}{StacktraceLine2}...{StacktraceLineN}
        if (writer == null) {
            return;
            // get the error message or if not given, the name of the exception class
        }

        writer.print(TEST_ERROR);
        writer.print(qualifiedTestName == null ? "" : qualifiedTestName);
        Throwable cause = t;
        while (cause != null) {
            String errorMsg = cause.getLocalizedMessage();
            if (IpsStringUtils.isBlank(errorMsg)) {
                errorMsg = cause.getMessage();
            }
            if (IpsStringUtils.isBlank(errorMsg)) {
                errorMsg = cause.getClass().getName();
            }
            errorMsg = wrapNull(cause, errorMsg);
            errorMsg = wrapClassNotFoundException(cause, errorMsg);
            writer.print(!(errorMsg != null && errorMsg.length() > 0) ? "" : "{");
            writer.print(TEST_ERROR_MESSAGE_INDICATOR);
            writer.print(errorMsg);
            writer.print("}");
            printStack(cause);
            cause = cause.getCause();
        }

        writer.println();
        writer.println(TEST_ERROR_END);
    }

    private String wrapClassNotFoundException(Throwable cause, String errorMsg) {
        if (cause instanceof ClassNotFoundException) {
            return "ClassNotFoundException " + errorMsg;
        } else {
            return errorMsg;
        }
    }

    private String wrapNull(Throwable cause, String errorMsg) {
        if ("null".equals(errorMsg)) {
            return cause.getClass().getName() + " " + errorMsg;
        } else {
            return errorMsg;
        }
    }

    private void printStack(Throwable cause) {
        StackTraceElement[] stackElems = cause.getStackTrace();
        for (StackTraceElement stackElem : stackElems) {
            writer.print("{");
            writer.print(TEST_ERROR_STACK_INDICATOR);
            writer.print(stackElem.toString());
            writer.print("}");
        }
    }

}
