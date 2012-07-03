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

package org.faktorips.devtools.stdbuilder.xpand.nullout;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.internal.xpand2.ast.Statement;
import org.eclipse.xpand2.output.FileHandle;
import org.eclipse.xpand2.output.InsertionPointSupport;
import org.eclipse.xpand2.output.Outlet;

/**
 * This is an implementation of {@link FileHandle} to get String output instead of directly write to
 * files. We need Strings to merge and format the generated code before writing to file.
 * 
 * @author dirmeier
 */
public class NullFileHandle implements FileHandle, InsertionPointSupport {

    private final NullOutlet outlet;

    private CharSequence nullBuffer = new NullCharSequence();

    public NullFileHandle(NullOutlet outlet) {
        this.outlet = outlet;
    }

    @Override
    public Outlet getOutlet() {
        return outlet;
    }

    @Override
    public CharSequence getBuffer() {
        return nullBuffer;
    }

    @Override
    public void setBuffer(CharSequence newBuffer) {
        // do nothing
    }

    @Override
    @Deprecated
    public File getTargetFile() {
        throw new RuntimeException("Getting target file is not supported in StringFileHandler");
    }

    @Override
    public String getAbsolutePath() {
        return StringUtils.EMPTY;
    }

    @Override
    public boolean isAppend() {
        return false;
    }

    @Override
    public boolean isOverwrite() {
        return outlet.isOverwrite();
    }

    @Override
    public String getFileEncoding() {
        return "UTF-8";
    }

    @Override
    public void writeAndClose() {
        // do nothing
    }

    @Override
    public void activateInsertionPoint(Statement stmt) {
        // do nothing
    }

    @Override
    public void deactivateInsertionPoint(Statement stmt) {
        // do nothing
    }

    @Override
    public void registerInsertionPoint(Statement stmt) {
        // do nothing
    }

    private static class NullCharSequence implements CharSequence, Appendable {

        @Override
        public int length() {
            return 0;
        }

        @Override
        public char charAt(int index) {
            return 0;
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return this;
        }

        @Override
        public Appendable append(CharSequence csq) throws IOException {
            return this;
        }

        @Override
        public Appendable append(CharSequence csq, int start, int end) throws IOException {
            return this;
        }

        @Override
        public Appendable append(char c) throws IOException {
            return this;
        }

    }

}
