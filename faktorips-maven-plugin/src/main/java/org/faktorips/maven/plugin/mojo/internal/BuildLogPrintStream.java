/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.maven.plugin.mojo.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class BuildLogPrintStream {

    private final static String ANT_TASK_HEADER = "(?:\\[faktorips\\.[a-z]+\\] ?)?";
    private final static List<String> EXCEPTIONS_TEXT = List.of(
            "java\\.io\\.FileNotFoundException: org\\.eclipse\\.equinox\\.simpleconfigurator/bundles\\.info \\(No such file or directory\\)");

    private final File logFile;
    private final PrintStream printStream;
    private final String projectName;

    public BuildLogPrintStream(String projectName) throws IOException {
        this.projectName = projectName;
        this.logFile = File.createTempFile(projectName + "_", ".log");
        printStream = new PrintStream(logFile);
    }

    public PrintStream getPrintStream() {
        return printStream;
    }

    public String getFilteredLogContent() throws FileNotFoundException, IOException {
        return filter(IOUtils.toString(new FileInputStream(logFile), Charset.forName("UTF-8")));
    }

    /**
     * This regex will match:
     *
     * <pre>
     * [faktorips.import] !ENTRY org.eclipse.jdt.junit.core 4 4 2021-06-10 13:25:50.957
     * ...
     * [faktorips.import] !STACK 0
     * [faktorips.import] java.io.FileNotFoundException: org.eclipse.equinox.simpleconfigurator/bundles.info (No such file or directory)
     * [faktorips.import]  at java.base/java.io.FileInputStream.open0(Native Method)
     * [faktorips.import]  at java.base/java.io.FileInputStream.open(FileInputStream.java:219)
     * ...
     * [faktorips.import]  at org.eclipse.equinox.launcher.Main.main(Main.java:1449)
     * </pre>
     * 
     * @param content the original output of eclipse
     * @return the filtered output
     */
    protected String filter(String content) {
        for (String exceptionText : EXCEPTIONS_TEXT) {
            StringBuilder sb = new StringBuilder();
            // ECLIPSE ERROR HEADER
            sb.append(ANT_TASK_HEADER).append("!ENTRY(?:.*\\R)*?");
            sb.append(ANT_TASK_HEADER).append("!STACK.*\\R");
            // EXCEPTION TEXT
            sb.append(ANT_TASK_HEADER).append(exceptionText).append("\\R");
            // STACKTRACE
            sb.append("(?:").append(ANT_TASK_HEADER).append("\tat .*\\R)*");

            content = content.replaceAll(sb.toString(), "");
        }
        return content;
    }

    public void flush() {
        printStream.flush();
    }

    public void cleanUp() {
        if (!logFile.delete()) {
            logFile.deleteOnExit();
        }
    }

    public String getProjectName() {
        return projectName;
    }

}
