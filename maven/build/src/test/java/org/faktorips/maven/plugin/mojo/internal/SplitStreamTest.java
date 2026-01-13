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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.Test;

class SplitStreamTest {

    private static final class TestBooleanSupplier implements BooleanSupplier {
        private boolean value;

        @Override
        public boolean getAsBoolean() {
            return value;
        }

        public void setValue(boolean value) {
            this.value = value;
        }
    }

    @Test
    public void print_String() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.print("1");
            decider.setValue(true);
            s.print("2");

            assertThat(Files.readString(fileA), containsString("1"));
            assertThat(Files.readString(fileB), containsString("2"));
        }
    }

    @Test
    public void write_int() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.write(65); // 'A'
            decider.setValue(true);
            s.write(66); // 'B'

            assertThat(Files.readString(fileA), containsString("A"));
            assertThat(Files.readString(fileB), containsString("B"));
        }
    }

    @Test
    public void write_byteArrayWithOffsetAndLength() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            byte[] data = "HELLO".getBytes();
            s.write(data, 0, 2); // "HE"
            decider.setValue(true);
            s.write(data, 2, 3); // "LLO"

            assertThat(Files.readString(fileA), containsString("HE"));
            assertThat(Files.readString(fileB), containsString("LLO"));
        }
    }

    @Test
    public void write_byteArray() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.write("original".getBytes());
            decider.setValue(true);
            s.write("diverted".getBytes());

            assertThat(Files.readString(fileA), containsString("original"));
            assertThat(Files.readString(fileB), containsString("diverted"));
        }
    }

    @Test
    public void writeBytes() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.writeBytes("first".getBytes());
            decider.setValue(true);
            s.writeBytes("second".getBytes());

            assertThat(Files.readString(fileA), containsString("first"));
            assertThat(Files.readString(fileB), containsString("second"));
        }
    }

    @Test
    public void print_boolean() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.print(true);
            decider.setValue(true);
            s.print(false);

            assertThat(Files.readString(fileA), containsString("true"));
            assertThat(Files.readString(fileB), containsString("false"));
        }
    }

    @Test
    public void print_char() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.print('X');
            decider.setValue(true);
            s.print('Y');

            assertThat(Files.readString(fileA), containsString("X"));
            assertThat(Files.readString(fileB), containsString("Y"));
        }
    }

    @Test
    public void print_int() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.print(42);
            decider.setValue(true);
            s.print(99);

            assertThat(Files.readString(fileA), containsString("42"));
            assertThat(Files.readString(fileB), containsString("99"));
        }
    }

    @Test
    public void print_long() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.print(123456789L);
            decider.setValue(true);
            s.print(987654321L);

            assertThat(Files.readString(fileA), containsString("123456789"));
            assertThat(Files.readString(fileB), containsString("987654321"));
        }
    }

    @Test
    public void print_float() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.print(1.5f);
            decider.setValue(true);
            s.print(2.5f);

            assertThat(Files.readString(fileA), containsString("1.5"));
            assertThat(Files.readString(fileB), containsString("2.5"));
        }
    }

    @Test
    public void print_double() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.print(3.14159);
            decider.setValue(true);
            s.print(2.71828);

            assertThat(Files.readString(fileA), containsString("3.14159"));
            assertThat(Files.readString(fileB), containsString("2.71828"));
        }
    }

    @Test
    public void print_charArray() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.print(new char[] { 'a', 'b', 'c' });
            decider.setValue(true);
            s.print(new char[] { 'x', 'y', 'z' });

            assertThat(Files.readString(fileA), containsString("abc"));
            assertThat(Files.readString(fileB), containsString("xyz"));
        }
    }

    @Test
    public void print_Object() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.print(Integer.valueOf(100));
            decider.setValue(true);
            s.print(Integer.valueOf(200));

            assertThat(Files.readString(fileA), containsString("100"));
            assertThat(Files.readString(fileB), containsString("200"));
        }
    }

    @Test
    public void println_noArgs() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.print("A");
            s.println();
            decider.setValue(true);
            s.print("B");
            s.println();

            String contentA = Files.readString(fileA);
            String contentB = Files.readString(fileB);
            assertThat(contentA, containsString("A"));
            assertThat(contentB, containsString("B"));
        }
    }

    @Test
    public void println_boolean() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.println(true);
            decider.setValue(true);
            s.println(false);

            assertThat(Files.readString(fileA), containsString("true"));
            assertThat(Files.readString(fileB), containsString("false"));
        }
    }

    @Test
    public void println_char() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.println('P');
            decider.setValue(true);
            s.println('Q');

            assertThat(Files.readString(fileA), containsString("P"));
            assertThat(Files.readString(fileB), containsString("Q"));
        }
    }

    @Test
    public void println_int() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.println(111);
            decider.setValue(true);
            s.println(222);

            assertThat(Files.readString(fileA), containsString("111"));
            assertThat(Files.readString(fileB), containsString("222"));
        }
    }

    @Test
    public void println_long() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.println(999888777L);
            decider.setValue(true);
            s.println(111222333L);

            assertThat(Files.readString(fileA), containsString("999888777"));
            assertThat(Files.readString(fileB), containsString("111222333"));
        }
    }

    @Test
    public void println_float() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.println(7.5f);
            decider.setValue(true);
            s.println(8.5f);

            assertThat(Files.readString(fileA), containsString("7.5"));
            assertThat(Files.readString(fileB), containsString("8.5"));
        }
    }

    @Test
    public void println_double() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.println(1.618033);
            decider.setValue(true);
            s.println(0.577215);

            assertThat(Files.readString(fileA), containsString("1.618033"));
            assertThat(Files.readString(fileB), containsString("0.577215"));
        }
    }

    @Test
    public void println_charArray() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.println(new char[] { 'f', 'o', 'o' });
            decider.setValue(true);
            s.println(new char[] { 'b', 'a', 'r' });

            assertThat(Files.readString(fileA), containsString("foo"));
            assertThat(Files.readString(fileB), containsString("bar"));
        }
    }

    @Test
    public void println_String() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.println("original line");
            decider.setValue(true);
            s.println("diverted line");

            assertThat(Files.readString(fileA), containsString("original line"));
            assertThat(Files.readString(fileB), containsString("diverted line"));
        }
    }

    @Test
    public void println_Object() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.println(Integer.valueOf(300));
            decider.setValue(true);
            s.println(Integer.valueOf(400));

            assertThat(Files.readString(fileA), containsString("300"));
            assertThat(Files.readString(fileB), containsString("400"));
        }
    }

    @Test
    public void printf_String() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.printf("Value: %d", 10);
            decider.setValue(true);
            s.printf("Value: %d", 20);

            assertThat(Files.readString(fileA), containsString("Value: 10"));
            assertThat(Files.readString(fileB), containsString("Value: 20"));
        }
    }

    @Test
    public void printf_Locale() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.printf(java.util.Locale.US, "Number: %.2f", 1.5);
            decider.setValue(true);
            s.printf(java.util.Locale.US, "Number: %.2f", 2.5);

            assertThat(Files.readString(fileA), containsString("Number: 1.50"));
            assertThat(Files.readString(fileB), containsString("Number: 2.50"));
        }
    }

    @Test
    public void format_String() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.format("Result: %s", "OK");
            decider.setValue(true);
            s.format("Result: %s", "ERROR");

            assertThat(Files.readString(fileA), containsString("Result: OK"));
            assertThat(Files.readString(fileB), containsString("Result: ERROR"));
        }
    }

    @Test
    public void format_Locale() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.format(java.util.Locale.GERMAN, "Zahl: %.2f", 3.14);
            decider.setValue(true);
            s.format(java.util.Locale.GERMAN, "Zahl: %.2f", 2.71);

            assertThat(Files.readString(fileA), containsString("Zahl: 3,14"));
            assertThat(Files.readString(fileB), containsString("Zahl: 2,71"));
        }
    }

    @Test
    public void append_CharSequence() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.append("alpha");
            decider.setValue(true);
            s.append("beta");

            assertThat(Files.readString(fileA), containsString("alpha"));
            assertThat(Files.readString(fileB), containsString("beta"));
        }
    }

    @Test
    public void append_CharSequenceWithRange() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.append("HELLO", 0, 2); // "HE"
            decider.setValue(true);
            s.append("WORLD", 0, 2); // "WO"

            assertThat(Files.readString(fileA), containsString("HE"));
            assertThat(Files.readString(fileB), containsString("WO"));
        }
    }

    @Test
    public void append_char() throws IOException {
        Path fileA = Files.createTempFile("a", ".log");
        Path fileB = Files.createTempFile("b", ".log");
        TestBooleanSupplier decider = new TestBooleanSupplier();
        try (
                var a = new PrintStream(fileA.toFile());
                var b = new PrintStream(fileB.toFile());
                var s = new SplitStream(a, b, decider)) {

            s.append('M');
            decider.setValue(true);
            s.append('N');

            assertThat(Files.readString(fileA), containsString("M"));
            assertThat(Files.readString(fileB), containsString("N"));
        }
    }

}
