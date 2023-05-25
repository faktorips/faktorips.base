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
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

public class BuildLogPrintStream {

    private static final String ANT_TASK_HEADER = "(\\[faktorips\\.\\w+\\] ?)?";
    private static final String REPEAT_ANT_TASK_HEADER = "\\1";
    private static final String ALL_LINE = ".+?\\R";
    private static final String OPTIONAL_BLANK_LINE = "\\R?";

    private final File logFile;
    private final PrintStream printStream;
    private final String projectName;
    private List<String> filters;

    public BuildLogPrintStream(String projectName, List<String> filters) throws IOException {
        this.projectName = projectName;
        this.filters = filters;
        logFile = File.createTempFile(projectName + "_", ".log");
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
     * [faktorips.import] !MESSAGE Unexpected error
     * [faktorips.import] !STACK 0
     * [faktorips.import] java.io.FileNotFoundException: org.eclipse.equinox.simpleconfigurator/bundles.info (No such file or directory)
     * [faktorips.import]  at java.base/java.io.FileInputStream.open0(Native Method)
     * ... till last line of the stacktrace
     * [faktorips.import]  at org.eclipse.equinox.launcher.Main.main(Main.java:1449)
     * </pre>
     *
     * @param content the original output of eclipse
     * @return the filtered output
     */
    protected String filter(String content) {
        String filteredContent = content;
        for (Pattern pattern : compileRegexPatternForFilters()) {
            filteredContent = pattern.matcher(filteredContent).replaceAll("");
        }
        return filteredContent;
    }

    private List<Pattern> compileRegexPatternForFilters() {
        return filters.stream().map(exceptionText -> {
            StringBuilder sb = new StringBuilder();
            sb.append("(?:");
            patternWithAntTaskHeader(exceptionText, sb);
            sb.append(")");
            sb.append("|");
            sb.append("(?:");
            pattern(exceptionText, sb);
            sb.append(")");
            return Pattern.compile(sb.toString());
        }).toList();
    }

    private void pattern(String exceptionText, StringBuilder sb) {
        // ECLIPSE ERROR HEADER
        sb.append("!ENTRY").append(ALL_LINE);
        sb.append("!MESSAGE").append(ALL_LINE);
        sb.append("(?:").append(ALL_LINE).append(")*");
        sb.append(OPTIONAL_BLANK_LINE);
        sb.append("!STACK").append(ALL_LINE);
        // EXCEPTION TEXT
        sb.append(exceptionText).append("\\R");
        // STACKTRACE
        sb.append("(?:").append("\\s+at .*\\R)*");
    }

    private void patternWithAntTaskHeader(String exceptionText, StringBuilder sb) {
        // ECLIPSE ERROR HEADER
        sb.append(ANT_TASK_HEADER).append("!ENTRY").append(ALL_LINE);
        sb.append(REPEAT_ANT_TASK_HEADER).append("!MESSAGE").append(ALL_LINE);
        sb.append("(?:").append(REPEAT_ANT_TASK_HEADER).append(ALL_LINE).append(")*");
        sb.append(OPTIONAL_BLANK_LINE);
        sb.append(REPEAT_ANT_TASK_HEADER).append("!STACK").append(ALL_LINE);
        // EXCEPTION TEXT
        sb.append(REPEAT_ANT_TASK_HEADER).append(exceptionText).append("\\R");
        // STACKTRACE
        sb.append("(?:").append(REPEAT_ANT_TASK_HEADER).append("\\tat .*\\R)*");
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
