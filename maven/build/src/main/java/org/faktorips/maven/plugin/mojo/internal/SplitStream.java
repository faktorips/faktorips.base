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

import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;
import java.util.function.BooleanSupplier;

/**
 * A {@link PrintStream} wrapper that diverts some of its output to another stream, depending on a
 * deciding boolean supplier.
 */
class SplitStream extends PrintStream {
    private final PrintStream originalStream;
    private final PrintStream divertedStream;
    private final BooleanSupplier diversionDecider;

    SplitStream(PrintStream originalStream, PrintStream divertedStream, BooleanSupplier diversionDecider) {
        super(originalStream);
        this.originalStream = originalStream;
        this.divertedStream = divertedStream;
        this.diversionDecider = diversionDecider;
    }

    private boolean divert() {
        return diversionDecider.getAsBoolean();
    }

    @Override
    public void write(int b) {
        if (divert()) {
            divertedStream.write(b);
        } else {
            originalStream.write(b);
        }
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        if (divert()) {
            divertedStream.write(buf, off, len);
        } else {
            originalStream.write(buf, off, len);
        }
    }

    @Override
    public void write(byte[] buf) throws IOException {
        if (divert()) {
            divertedStream.write(buf);
        } else {
            originalStream.write(buf);
        }
    }

    @Override
    public void writeBytes(byte[] buf) {
        if (divert()) {
            divertedStream.writeBytes(buf);
        } else {
            originalStream.writeBytes(buf);
        }
    }

    @Override
    public void print(boolean b) {
        if (divert()) {
            divertedStream.print(b);
        } else {
            originalStream.print(b);
        }
    }

    @Override
    public void print(char c) {
        if (divert()) {
            divertedStream.print(c);
        } else {
            originalStream.print(c);
        }
    }

    @Override
    public void print(int i) {
        if (divert()) {
            divertedStream.print(i);
        } else {
            originalStream.print(i);
        }
    }

    @Override
    public void print(long l) {
        if (divert()) {
            divertedStream.print(l);
        } else {
            originalStream.print(l);
        }
    }

    @Override
    public void print(float f) {
        if (divert()) {
            divertedStream.print(f);
        } else {
            originalStream.print(f);
        }
    }

    @Override
    public void print(double d) {
        if (divert()) {
            divertedStream.print(d);
        } else {
            originalStream.print(d);
        }
    }

    @Override
    public void print(char[] s) {
        if (divert()) {
            divertedStream.print(s);
        } else {
            originalStream.print(s);
        }
    }

    @Override
    public void print(String s) {
        if (divert()) {
            divertedStream.print(s);
        } else {
            originalStream.print(s);
        }
    }

    @Override
    public void print(Object obj) {
        if (divert()) {
            divertedStream.print(obj);
        } else {
            originalStream.print(obj);
        }
    }

    @Override
    public void println() {
        if (divert()) {
            divertedStream.println();
        } else {
            originalStream.println();
        }
    }

    @Override
    public void println(boolean x) {
        if (divert()) {
            divertedStream.println(x);
        } else {
            originalStream.println(x);
        }
    }

    @Override
    public void println(char x) {
        if (divert()) {
            divertedStream.println(x);
        } else {
            originalStream.println(x);
        }
    }

    @Override
    public void println(int x) {
        if (divert()) {
            divertedStream.println(x);
        } else {
            originalStream.println(x);
        }
    }

    @Override
    public void println(long x) {
        if (divert()) {
            divertedStream.println(x);
        } else {
            originalStream.println(x);
        }
    }

    @Override
    public void println(float x) {
        if (divert()) {
            divertedStream.println(x);
        } else {
            originalStream.println(x);
        }
    }

    @Override
    public void println(double x) {
        if (divert()) {
            divertedStream.println(x);
        } else {
            originalStream.println(x);
        }
    }

    @Override
    public void println(char[] x) {
        if (divert()) {
            divertedStream.println(x);
        } else {
            originalStream.println(x);
        }
    }

    @Override
    public void println(String x) {
        if (divert()) {
            divertedStream.println(x);
        } else {
            originalStream.println(x);
        }
    }

    @Override
    public void println(Object x) {
        if (divert()) {
            divertedStream.println(x);
        } else {
            originalStream.println(x);
        }
    }

    @Override
    public PrintStream printf(String format, Object... args) {
        if (divert()) {
            return divertedStream.printf(format, args);
        } else {
            return originalStream.printf(format, args);
        }
    }

    @Override
    public PrintStream printf(Locale l, String format, Object... args) {
        if (divert()) {
            return divertedStream.printf(l, format, args);
        } else {
            return originalStream.printf(l, format, args);
        }
    }

    @Override
    public PrintStream format(String format, Object... args) {
        if (divert()) {
            return divertedStream.format(format, args);
        } else {
            return originalStream.format(format, args);
        }
    }

    @Override
    public PrintStream format(Locale l, String format, Object... args) {
        if (divert()) {
            return divertedStream.format(l, format, args);
        } else {
            return originalStream.format(l, format, args);
        }
    }

    @Override
    public PrintStream append(CharSequence csq) {
        if (divert()) {
            return divertedStream.append(csq);
        } else {
            return originalStream.append(csq);
        }
    }

    @Override
    public PrintStream append(CharSequence csq, int start, int end) {
        if (divert()) {
            return divertedStream.append(csq, start, end);
        } else {
            return originalStream.append(csq, start, end);
        }
    }

    @Override
    public PrintStream append(char c) {
        if (divert()) {
            return divertedStream.append(c);
        } else {
            return originalStream.append(c);
        }
    }

}