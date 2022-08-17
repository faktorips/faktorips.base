/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest.matcher;

import java.util.function.Function;

import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;
import org.faktorips.testsupport.IpsMatchers;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsNot;

public class Matchers {

    private Matchers() {
        // avoid default constructor for utility class
    }

    /**
     * @deprecated since 21.12. Use {@link IpsMatchers#hasMessageCode(String)} instead.
     */
    @Deprecated
    public static Matcher<MessageList> hasMessageCode(final String msgCode) {
        return new MessageCodeMatcher(msgCode, true);
    }

    /**
     * @deprecated since 21.12. Use {@link IpsMatchers#lacksMessageCode(String)} instead.
     */
    @Deprecated
    public static Matcher<MessageList> lacksMessageCode(final String msgCode) {
        return new MessageCodeMatcher(msgCode, false);
    }

    /**
     * @deprecated since 21.12. Use {@link IpsMatchers#hasSize(int)} instead.
     */
    @Deprecated
    public static Matcher<MessageList> hasSize(int size) {
        return new MessageListSizeMatcher(size);
    }

    /**
     * @deprecated since 21.12. Use {@link IpsMatchers#isEmpty} instead.
     */
    @Deprecated
    public static Matcher<MessageList> isEmpty() {
        return new EmptyMessageListMatcher();
    }

    /**
     * @deprecated since 21.12. Use {@link IpsMatchers#containsMessages} instead.
     */
    @Deprecated
    public static Matcher<MessageList> containsMessages() {
        return new IsNot<>(new EmptyMessageListMatcher());
    }

    /**
     * @deprecated since 21.12. Use {@link IpsMatchers#hasInvalidObject(Object)} instead.
     */
    @Deprecated
    public static Matcher<Message> hasInvalidObject(Object invalidObject) {
        return new MessageInvalidObjectMatcher(invalidObject);
    }

    /**
     * @deprecated since 21.12. Use {@link IpsMatchers#hasInvalidObject(Object, String)} instead.
     */
    @Deprecated
    public static Matcher<Message> hasInvalidObject(Object invalidObject, String propertyName) {
        return new MessageInvalidObjectMatcher(invalidObject, propertyName);
    }

    /**
     * @deprecated since 21.12. Use {@link IpsMatchers#hasSeverity(Severity)} instead.
     */
    @Deprecated
    public static Matcher<Message> hasSeverity(Severity severity) {
        return new MessageSevertiyMatcher(severity);
    }

    /**
     * @deprecated since 21.12. Use {@link IpsMatchers#containsErrorMessage} instead.
     */
    @Deprecated
    public static Matcher<MessageList> containsErrorMsg() {
        return new ContainsErrorMatcher();
    }

    /**
     * @deprecated since 21.12. Use {@link IpsMatchers#containsText(String)} instead.
     */
    @Deprecated
    public static Matcher<Message> containsText(String text) {
        return new MessageTextMatcher(text);
    }

    /**
     * Similar to {@link CoreMatchers#allOf(Matcher...)}, but with better mismatch description.
     */
    @SafeVarargs
    public static <T> Matcher<T> allOf(Matcher<T>... matchers) {
        return AllMatcher.allOf(matchers);
    }

    /**
     * A {@link Matcher Matcher&lt;T&gt;} that uses a wrapped {@link Matcher Matcher&lt;P&gt;} on a
     * property of type &lt;P&gt; of the matched object of type &lt;T&gt;.
     * 
     * @param <T> the type of the matched object
     * @param <P> the type of the object's property matched by the wrapped matcher
     */
    public static <T, P> Matcher<T> hasProperty(Function<T, P> propertyGetter,
            String propertyDescription,
            Matcher<P> propertyMatcher) {
        return new PropertyMatcher<>(propertyGetter, propertyDescription, propertyMatcher);
    }

    /**
     * A {@link Matcher Matcher&lt;T&gt;} that uses a wrapped {@link Matcher Matcher&lt;P&gt;} on a
     * property of type &lt;P&gt; of the matched object of type &lt;T&gt; and referenced object of
     * that same type.
     *
     * @param <T> the type of the compared objects
     * @param <P> the type of the property
     */
    public static <T, P> Matcher<T> hasSame(String propertyDescription,
            Function<T, P> propertyGetter,
            T objectToMatch) {
        return SamePropertyMatcher.same(propertyGetter, propertyDescription, objectToMatch);
    }

    /**
     * A {@link Matcher Matcher&lt;T&gt;} that uses a wrapped {@link Matcher Matcher&lt;byte[]&gt;}
     * on a byte[]-typed property of the matched object of type &lt;T&gt; and referenced object of
     * that same type.
     *
     * @param <T> the type of the compared objects
     */
    public static <T> Matcher<T> hasSameByteArray(String propertyDescription,
            Function<T, byte[]> propertyGetter,
            T objectToMatch) {
        return SameByteArrayPropertyMatcher.sameByteArray(propertyGetter, propertyDescription, objectToMatch);
    }

    /**
     * A {@link Matcher} that matches if, for every given {@link Matcher}, the checked
     * {@link MessageList} contains a {@link Message} that is matched by that {@link Matcher}. This
     * must be a different {@link Message} for every {@link Matcher Matchers}. The
     * {@link MessageList} may contain additional {@link Message Messages} not matched by any
     * {@link Matcher}. The order of the {@link Message Messages} and {@link Matcher Matchers} is
     * irrelevant.
     * 
     * @deprecated since 21.12. Use {@link IpsMatchers#hasMessages(Matcher...)} instead.
     */
    @SafeVarargs
    @Deprecated
    public static Matcher<MessageList> hasMessages(Matcher<Message>... messageMatchers) {
        return new MessageListMessagesMatcher(messageMatchers);
    }

}
