package org.faktorips.util;

import static org.faktorips.testsupport.IpsMatchers.containsErrorMessage;
import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.faktorips.testsupport.IpsMatchers.hasMessageFor;
import static org.faktorips.testsupport.IpsMatchers.hasSize;
import static org.faktorips.testsupport.IpsMatchers.isEmpty;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.faktorips.runtime.MessageList;
import org.junit.Test;

public class MethodAccessTest {

    @Test
    public void testOf_CreatesWrapperForExistingMethod() {
        assertThat(MethodAccess.of(MethodAccessTest.class, "testOf"), is(notNullValue()));
    }

    @Test
    public void testOf_CreatesWrapperForNonExistingMethod() {
        assertThat(MethodAccess.of(MethodAccessTest.class, "doSomething"), is(notNullValue()));
        assertThat(MethodAccess.of(MethodAccessTest.class, "not a method"), is(notNullValue()));
    }

    @Test(expected = NullPointerException.class)
    public void testOf_ThrowsNPEifBothParametersAreNull() {
        MethodAccess.of(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testOf_ThrowsNPEifClassIsNull() {
        MethodAccess.of(null, "foo");
    }

    @Test(expected = NullPointerException.class)
    public void testOf_ThrowsNPEifNameIsNull() {
        MethodAccess.of(MethodAccessTest.class, null);
    }

    @Test(expected = NullPointerException.class)
    public void testOf_ThrowsNPEifParameterIsNull() {
        MethodAccess.of(MethodAccessTest.class, "foo", String.class, null, Integer.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOf_ThrowsIAEifMethodNameIsEmpty() {
        MethodAccess.of(MethodAccessTest.class, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOf_ThrowsIAEifMethodNameIsBlank() {
        MethodAccess.of(MethodAccessTest.class, "  ");
    }

    @Test
    public void testInvoke_WithoutParameters() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "foo");

        assertThat(methodAccess.invoke("to fool around with", this), is(42));
    }

    public int foo() {
        return 42;
    }

    @Test
    public void testInvoke_WithParameter() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "greet", String.class);

        assertThat(methodAccess.invoke("to", this, "Test"), is("Hello Test"));
    }

    public String greet(String name) {
        return "Hello " + name;
    }

    @Test
    public void testInvoke_WithParameters() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "repeat", String.class, Integer.TYPE);

        assertThat(methodAccess.invoke("to", this, "Test", 3), is("TestTestTest"));
    }

    public String repeat(String name, int times) {
        return IntStream.range(0, times).mapToObj($ -> name).collect(Collectors.joining());
    }

    @Test
    public void testInvoke_ThrowsRuntimeExceptionIfMethodDoesNotExist() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "bar");

        try {
            methodAccess.invoke("that does not exist", this);
            fail("Expected " + RuntimeException.class.getSimpleName());
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString("that does not exist"));
        }
    }

    @Test
    public void testInvoke_ThrowsRuntimeExceptionIfMethodInvocationFails() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "failing");

        try {
            methodAccess.invoke("that fails", this);
            fail("Expected " + RuntimeException.class.getSimpleName());
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString("that fails"));
        }
    }

    public int failing() {
        throw new RuntimeException();
    }

    @Test
    public void testInvoke_ThrowsRuntimeExceptionIfMethodCantBeAccessed() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "cantBeAccessed");

        try {
            methodAccess.invoke("that can't be accessed", this);
            fail("Expected " + RuntimeException.class.getSimpleName());
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString("that can't be accessed"));
        }
    }

    @SuppressWarnings("unused")
    private int cantBeAccessed() {
        return -1;
    }

    @Test
    public void testInvoke_ThrowsClassCastExceptionIfMethodReturnTypeIsIncompatible() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "foo");

        try {
            String bar = methodAccess.invoke("will not be part of the message", this);
            System.out.println(bar);
            fail("Expected " + ClassCastException.class.getSimpleName());
        } catch (ClassCastException e) {
            assertThat(e.getMessage(), containsString("Integer"));
            assertThat(e.getMessage(), not(containsString("will not be part of the message")));
        }
    }

    @Test
    public void testInvoke_ThrowsRuntimeExceptionNoObjectIsGiven() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "foo");

        try {
            methodAccess.invoke("is not static", null);
            fail("Expected " + RuntimeException.class.getSimpleName());
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString("is not static"));
        }
    }

    @Test
    public void testInvoke_ThrowsRuntimeExceptionIfMethodIsStatic() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "fooStatic");

        try {
            methodAccess.invokeStatic("that is static", this);
            fail("Expected " + RuntimeException.class.getSimpleName());
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString("that is static"));
        }
    }

    @Test
    public void testInvokeStatic_WithoutParameters() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "fooStatic");

        assertThat(methodAccess.invokeStatic("to fool around with"), is(42));
    }

    public static Integer fooStatic() {
        return 42;
    }

    @Test
    public void testInvokeStatic_WithParameter() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "greetStatic", String.class);

        assertThat(methodAccess.invokeStatic("to", "Test"), is("Hello Test"));
    }

    public static String greetStatic(String name) {
        return "Hello " + name;
    }

    @Test
    public void testInvokeStatic_WithParameters() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "repeatStatic", String.class, Integer.TYPE);

        assertThat(methodAccess.invokeStatic("to", "Test", 3), is("TestTestTest"));
    }

    public static String repeatStatic(String name, int times) {
        return IntStream.range(0, times).mapToObj($ -> name).collect(Collectors.joining());
    }

    @Test
    public void testInvokeStatic_ThrowsRuntimeExceptionIfMethodDoesNotExist() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "barStatic");

        try {
            methodAccess.invokeStatic("that does not exist");
            fail("Expected " + RuntimeException.class.getSimpleName());
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString("that does not exist"));
        }
    }

    @Test
    public void testInvokeStatic_ThrowsRuntimeExceptionIfMethodInvocationFails() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "failingStatic");

        try {
            methodAccess.invokeStatic("that fails");
            fail("Expected " + RuntimeException.class.getSimpleName());
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString("that fails"));
        }
    }

    public static int failingStatic() {
        throw new RuntimeException();
    }

    @Test
    public void testInvokeStatic_ThrowsRuntimeExceptionIfMethodCantBeAccessed() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "cantBeAccessedStatic");

        try {
            methodAccess.invokeStatic("that can't be accessed");
            fail("Expected " + RuntimeException.class.getSimpleName());
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString("that can't be accessed"));
        }
    }

    @SuppressWarnings("unused")
    private static int cantBeAccessedStatic() {
        return -1;
    }

    @Test
    public void testInvokeStatic_ThrowsClassCastExceptionIfMethodReturnTypeIsIncompatible() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "fooStatic");

        try {
            String bar = methodAccess.invokeStatic("will not be part of the message");
            System.out.println(bar);
            fail("Expected " + ClassCastException.class.getSimpleName());
        } catch (ClassCastException e) {
            assertThat(e.getMessage(), containsString("Integer"));
            assertThat(e.getMessage(), not(containsString("will not be part of the message")));
        }
    }

    @Test
    public void testInvokeStatic_ThrowsRuntimeExceptionIfMethodNotStatic() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "foo");

        try {
            methodAccess.invokeStatic("that is not static");
            fail("Expected " + RuntimeException.class.getSimpleName());
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString("that is not static"));
        }
    }

    @Test
    public void testCheck_CreatesCheck() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "foo");

        assertThat(methodAccess.check(new MessageList(), "PRE"), is(notNullValue()));
    }

    @Test(expected = NullPointerException.class)
    public void testCheck_ThrowsNPEifMessageListIsNull() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "foo");

        methodAccess.check(null, "PRE");
    }

    @Test(expected = NullPointerException.class)
    public void testCheck_ThrowsNPEifPrefixIsNull() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "foo");

        methodAccess.check(new MessageList(), null);
    }

    @Test
    public void testCheckExists_WithoutParameters() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "foo");
        MessageList messageList = new MessageList();
        MethodAccess.Check check = methodAccess.check(messageList, "PRE");

        assertThat(check.exists(), is(check));

        assertThat(messageList, isEmpty());
    }

    @Test
    public void testCheckExists_WithParameters() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "repeat", String.class, Integer.TYPE);
        MessageList messageList = new MessageList();
        MethodAccess.Check check = methodAccess.check(messageList, "PRE");

        assertThat(check.exists(), is(check));

        assertThat(messageList, isEmpty());
    }

    @Test
    public void testCheckExists_WithSubclassParameters() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "repeat", CharSequence.class, Integer.TYPE);
        MessageList messageList = new MessageList();
        MethodAccess.Check check = methodAccess.check(messageList, "PRE");

        assertThat(check.exists(), is(check));

        assertThat(messageList, isEmpty());
    }

    @Test
    public void testCheckExists_Not() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "bar");
        MessageList messageList = new MessageList();
        MethodAccess.Check check = methodAccess.check(messageList, "PRE");

        assertThat(check.exists(), is(check));

        assertThat(messageList, containsErrorMessage());
        assertThat(messageList, hasMessageCode("PRE" + MethodAccess.Check.MSG_CODE_SUFFIX_DOES_NOT_EXIST));
        assertThat(messageList, hasMessageFor(MethodAccessTest.class, "bar"));
    }

    @Test
    public void testCheckExists_WrongParameters() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "repeat", Integer.class);
        MessageList messageList = new MessageList();
        MethodAccess.Check check = methodAccess.check(messageList, "PRE");

        assertThat(check.exists(), is(check));

        assertThat(messageList, containsErrorMessage());
        assertThat(messageList, hasMessageCode("PRE" + MethodAccess.Check.MSG_CODE_SUFFIX_DOES_NOT_EXIST));
        assertThat(messageList, hasMessageFor(MethodAccessTest.class, "repeat"));
    }

    @Test
    public void testCheckExists_NotAccessible() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "cantBeAccessed");
        MessageList messageList = new MessageList();
        MethodAccess.Check check = methodAccess.check(messageList, "PRE");

        assertThat(check.exists(), is(check));

        assertThat(messageList, containsErrorMessage());
        assertThat(messageList, hasMessageCode("PRE" + MethodAccess.Check.MSG_CODE_SUFFIX_DOES_NOT_EXIST));
        assertThat(messageList, hasMessageFor(MethodAccessTest.class, "cantBeAccessed"));
    }

    @Test
    public void testCheckIsStatic() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "fooStatic");
        MessageList messageList = new MessageList();
        MethodAccess.Check check = methodAccess.check(messageList, "PRE");

        assertThat(check.isStatic(), is(check));

        assertThat(messageList, isEmpty());
    }

    @Test
    public void testCheckIsStatic_Not() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "foo");
        MessageList messageList = new MessageList();
        MethodAccess.Check check = methodAccess.check(messageList, "PRE");

        assertThat(check.isStatic(), is(check));

        assertThat(messageList, containsErrorMessage());
        assertThat(messageList, hasMessageCode("PRE" + MethodAccess.Check.MSG_CODE_SUFFIX_NOT_STATIC));
        assertThat(messageList, hasMessageFor(MethodAccessTest.class, "foo"));
    }

    @Test
    public void testCheckIsNotStatic() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "foo");
        MessageList messageList = new MessageList();
        MethodAccess.Check check = methodAccess.check(messageList, "PRE");

        assertThat(check.isNotStatic(), is(check));

        assertThat(messageList, isEmpty());
    }

    @Test
    public void testCheckIsNotStatic_IsStatic() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "fooStatic");
        MessageList messageList = new MessageList();
        MethodAccess.Check check = methodAccess.check(messageList, "PRE");

        assertThat(check.isNotStatic(), is(check));

        assertThat(messageList, containsErrorMessage());
        assertThat(messageList, hasMessageCode("PRE" + MethodAccess.Check.MSG_CODE_SUFFIX_STATIC));
        assertThat(messageList, hasMessageFor(MethodAccessTest.class, "fooStatic"));
    }

    @Test
    public void testCheckReturnTypeIsCompatible() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "foo");
        MessageList messageList = new MessageList();
        MethodAccess.Check check = methodAccess.check(messageList, "PRE");

        assertThat(check.returnTypeIsCompatible(Integer.TYPE), is(check));

        assertThat(messageList, isEmpty());
    }

    @Test
    public void testCheckReturnTypeIsCompatible_Wrapper() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "foo");
        MessageList messageList = new MessageList();
        MethodAccess.Check check = methodAccess.check(messageList, "PRE");

        assertThat(check.returnTypeIsCompatible(Integer.class), is(check));

        assertThat(messageList, isEmpty());
    }

    @Test
    public void testCheckReturnTypeIsCompatible_Primitive() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "fooStatic");
        MessageList messageList = new MessageList();
        MethodAccess.Check check = methodAccess.check(messageList, "PRE");

        assertThat(check.returnTypeIsCompatible(Integer.TYPE), is(check));

        assertThat(messageList, isEmpty());
    }

    @Test
    public void testCheckReturnTypeIsCompatible_Any() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "foo");
        MessageList messageList = new MessageList();
        MethodAccess.Check check = methodAccess.check(messageList, "PRE");

        assertThat(check.returnTypeIsCompatible(String.class, Double.class, Integer.TYPE), is(check));

        assertThat(messageList, isEmpty());
    }

    @Test
    public void testCheckReturnTypeIsCompatible_Not() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "foo");
        MessageList messageList = new MessageList();
        MethodAccess.Check check = methodAccess.check(messageList, "PRE");

        assertThat(check.returnTypeIsCompatible(String.class), is(check));

        assertThat(messageList, containsErrorMessage());
        assertThat(messageList, hasMessageCode("PRE" + MethodAccess.Check.MSG_CODE_SUFFIX_INCOMPATIBLE_RETURN_TYPE));
        assertThat(messageList, hasMessageFor(MethodAccessTest.class, "foo"));
    }

    @Test
    public void testCheckReturnTypeIsCompatible_None() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "foo");
        MessageList messageList = new MessageList();
        MethodAccess.Check check = methodAccess.check(messageList, "PRE");

        assertThat(check.returnTypeIsCompatible(String.class, Test.class), is(check));

        assertThat(messageList, containsErrorMessage());
        assertThat(messageList, hasMessageCode("PRE" + MethodAccess.Check.MSG_CODE_SUFFIX_INCOMPATIBLE_RETURN_TYPE));
        assertThat(messageList, hasMessageFor(MethodAccessTest.class, "foo"));
    }

    @Test
    public void testCheck_Multiple() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "fooStatic");
        MessageList messageList = new MessageList();
        MethodAccess.Check check = methodAccess.check(messageList, "PRE");

        assertThat(check
                .isNotStatic()
                .returnTypeIsCompatible(String.class, Test.class), is(check));

        assertThat(messageList, containsErrorMessage());
        assertThat(messageList, hasMessageCode("PRE" + MethodAccess.Check.MSG_CODE_SUFFIX_INCOMPATIBLE_RETURN_TYPE));
        assertThat(messageList, hasMessageCode("PRE" + MethodAccess.Check.MSG_CODE_SUFFIX_STATIC));
        assertThat(messageList, hasMessageFor(MethodAccessTest.class, "fooStatic"));
    }

    @Test
    public void testCheck_MultipleOnlyIfExists() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "cantBeAccessed");
        MessageList messageList = new MessageList();
        MethodAccess.Check check = methodAccess.check(messageList, "PRE");

        assertThat(check
                .exists()
                .isStatic()
                .returnTypeIsCompatible(String.class, Test.class), is(check));

        assertThat(messageList, containsErrorMessage());
        assertThat(messageList, hasMessageCode("PRE" + MethodAccess.Check.MSG_CODE_SUFFIX_DOES_NOT_EXIST));
        assertThat(messageList, not(
                hasMessageCode("PRE" + MethodAccess.Check.MSG_CODE_SUFFIX_INCOMPATIBLE_RETURN_TYPE)));
        assertThat(messageList, not(hasMessageCode("PRE" + MethodAccess.Check.MSG_CODE_SUFFIX_STATIC)));
        assertThat(messageList, hasMessageFor(MethodAccessTest.class, "cantBeAccessed"));
    }

    @Test
    public void testCheck_MultipleOnlyIfExists_EvenIfExistsWasNotCalled() {
        var methodAccess = MethodAccess.of(MethodAccessTest.class, "cantBeAccessed");
        MessageList messageList = new MessageList();
        MethodAccess.Check check = methodAccess.check(messageList, "PRE");

        assertThat(check
                .isStatic()
                .returnTypeIsCompatible(String.class, Test.class), is(check));

        assertThat(messageList, hasSize(1));
        assertThat(messageList, containsErrorMessage());
        assertThat(messageList, hasMessageCode("PRE" + MethodAccess.Check.MSG_CODE_SUFFIX_DOES_NOT_EXIST));
        assertThat(messageList, not(
                hasMessageCode("PRE" + MethodAccess.Check.MSG_CODE_SUFFIX_INCOMPATIBLE_RETURN_TYPE)));
        assertThat(messageList, not(hasMessageCode("PRE" + MethodAccess.Check.MSG_CODE_SUFFIX_STATIC)));
        assertThat(messageList, hasMessageFor(MethodAccessTest.class, "cantBeAccessed"));
    }

}
