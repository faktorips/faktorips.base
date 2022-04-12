/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.testsupport;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;

import java.util.function.Function;

import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.Severity;
import org.faktorips.testsupport.matchers.EmptyMessageListMatcher;
import org.faktorips.testsupport.matchers.MessageCodeMatcher;
import org.faktorips.testsupport.matchers.MessageInvalidObjectMatcher;
import org.faktorips.testsupport.matchers.MessageListCodeMatcher;
import org.faktorips.testsupport.matchers.MessageListMessageMatcher;
import org.faktorips.testsupport.matchers.MessageListObjectPropertyMatcher;
import org.faktorips.testsupport.matchers.MessageListSizeMatcher;
import org.faktorips.testsupport.matchers.MessageSeverityMatcher;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.core.CombinableMatcher;

/**
 * Hamcrest {@link Matcher Matchers} for use in JUnit tests of Faktor-IPS (generated) code.
 */
public class IpsMatchers {

    private IpsMatchers() {
        // do not instantiate
    }

    /**
     * Creates an {@link EmptyMessageListMatcher} that matches a {@link MessageList} if it
     * {@link MessageList#isEmpty() is empty}.
     */
    public static EmptyMessageListMatcher isEmpty() {
        return new EmptyMessageListMatcher();
    }

    /**
     * Creates a {@link Matcher} that matches a {@link MessageList} if it is not
     * {@link MessageList#isEmpty() empty}.
     */
    public static Matcher<MessageList> containsMessages() {
        return not(new EmptyMessageListMatcher());
    }

    /**
     * Creates a {@link MessageListSizeMatcher} that matches a {@link MessageList} if it's
     * {@link MessageList#size() size} equal to the given size.
     *
     * @param size the expected size
     */
    public static MessageListSizeMatcher hasSize(int size) {
        return new MessageListSizeMatcher(equalTo(size));
    }

    /**
     * Creates a {@link MessageListSizeMatcher} that matches a {@link MessageList} if it's
     * {@link MessageList#size() size} is matched by the given {@link Matcher
     * Matcher&lt;Integer&gt;}.
     *
     * @param intMatcher the {@link Matcher Matcher&lt;Integer&gt;} for the size
     */
    public static MessageListSizeMatcher hasSize(Matcher<Integer> intMatcher) {
        return new MessageListSizeMatcher(intMatcher);
    }

    /**
     * Creates a {@link MessageListObjectPropertyMatcher} that matches a {@link MessageList} if it
     * contains exactly one {@link Message} with an {@link ObjectProperty} for the given object.
     *
     * @param object the expected object
     */
    public static MessageListObjectPropertyMatcher hasMessageFor(Object object) {
        return new MessageListObjectPropertyMatcher(new ObjectProperty(object));
    }

    /**
     * Creates a {@link MessageListObjectPropertyMatcher} that matches a {@link MessageList} if it
     * contains exactly one {@link Message} with an {@link ObjectProperty} for the given object's
     * given property.
     *
     * @param object the expected object
     * @param property the expected object property
     */
    public static MessageListObjectPropertyMatcher hasMessageFor(Object object, String property) {
        return new MessageListObjectPropertyMatcher(new ObjectProperty(object, property));
    }

    /**
     * Creates a {@link MessageListObjectPropertyMatcher} that matches a {@link MessageList} if it
     * contains exactly {@code count} {@link Message Messages} with an {@link ObjectProperty} for
     * the given object's given property.
     *
     * @param object the expected object
     * @param property the expected object property
     * @param count the expected number of messages for the given object property
     */
    public static MessageListObjectPropertyMatcher hasMessagesFor(int count, Object object, String property) {
        return new MessageListObjectPropertyMatcher(new ObjectProperty(object, property), count);
    }

    /**
     * Creates a {@link MessageListMessageMatcher} that matches a {@link MessageList} if it contains
     * at least one {@link Message} with the given {@link Message#getCode() code}.
     *
     * @param code the expected message code
     */
    public static Matcher<MessageList> hasMessageCode(String code) {
        return new MessageListCodeMatcher(code);
    }

    /**
     * Creates a {@link MessageListMessageMatcher} that matches a {@link MessageList} if it contains
     * no {@link Message} with the given {@link Message#getCode() code}.
     *
     * @param code the message code
     */
    public static Matcher<MessageList> lacksMessageCode(String code) {
        return new MessageListCodeMatcher(code, false);
    }

    /**
     * Creates a {@link MessageListMessageMatcher} that matches a {@link MessageList} if it contains
     * at least one {@link Message} with the given {@link Message#getCode() code} and
     * {@link Severity#INFO}.
     *
     * @param code the expected message code
     */
    public static MessageListMessageMatcher hasInfoMessage(String code) {
        return new MessageListMessageMatcher(codeAndSeverity(code, Severity.INFO));
    }

    /**
     * Creates a {@link MessageListMessageMatcher} that matches a {@link MessageList} if it contains
     * at least one {@link Message} with the given {@link Message#getCode() code} and
     * {@link Severity#WARNING}.
     *
     * @param code the expected message code
     */
    public static MessageListMessageMatcher hasWarningMessage(String code) {
        return new MessageListMessageMatcher(codeAndSeverity(code, Severity.WARNING));
    }

    /**
     * Creates a {@link MessageListMessageMatcher} that matches a {@link MessageList} if it contains
     * at least one {@link Message} with the given {@link Message#getCode() code} and
     * {@link Severity#ERROR}.
     *
     * @param code the expected message code
     */
    public static MessageListMessageMatcher hasErrorMessage(String code) {
        return new MessageListMessageMatcher(codeAndSeverity(code, Severity.ERROR));
    }

    /**
     * Creates a {@link MessageListMessageMatcher} that matches a {@link MessageList} if it contains
     * at least one {@link Message} with the given {@link Severity}.
     *
     * @param severity the expected {@link Severity}
     */
    public static MessageListMessageMatcher hasMessageWithSeverity(Severity severity) {
        return new MessageListMessageMatcher(new MessageSeverityMatcher(severity));
    }

    /**
     * Creates a {@link MessageListMessageMatcher} that matches a {@link MessageList} if it contains
     * at least one {@link Message} with the {@link Severity#ERROR}.
     */
    public static MessageListMessageMatcher containsErrorMessage() {
        return new MessageListMessageMatcher(new MessageSeverityMatcher(Severity.ERROR));
    }

    /**
     * Creates a {@link MessageListMessageMatcher} that matches a {@link MessageList} if it contains
     * no {@link Message} with the {@link Severity#ERROR}.
     */
    public static Matcher<MessageList> containsNoErrorMessage() {
        return not(new MessageListMessageMatcher(new MessageSeverityMatcher(Severity.ERROR)));
    }

    /**
     * Creates a {@link Matcher} that matches if any {@link Message} in the {@link MessageList} is
     * matched by the given {@link Matcher}.
     * 
     * @param messageMatcher a {@link Matcher} for a single {@link Message}
     */
    public static MessageListMessageMatcher hasMessageThat(Matcher<Message> messageMatcher) {
        return new MessageListMessageMatcher(messageMatcher);
    }

    /**
     * A {@link Matcher} that matches if, for every given {@link Matcher}, the checked
     * {@link MessageList} contains a {@link Message} that is matched by that {@link Matcher}. This
     * must be a different {@link Message} for every {@link Matcher Matchers}. The
     * {@link MessageList} may contain additional {@link Message Messages} not matched by any
     * {@link Matcher}. The order of the {@link Message Messages} and {@link Matcher Matchers} is
     * irrelevant.
     */
    @SafeVarargs
    public static Matcher<MessageList> hasMessages(Matcher<Message>... messageMatchers) {
        return new MessageListMessageMatcher(messageMatchers);
    }

    /**
     * Creates a {@link Matcher} that matches if the {@link Message#getText() Message's text}
     * {@link String#contains(CharSequence) contains} the given text.
     * 
     * @param text the text to match
     */
    public static Matcher<Message> containsText(String text) {
        return hasFeature(Message::getText, containsString(text), "a message where the text is", "text");
    }

    public static Matcher<Message> hasSeverity(Severity severity) {
        return new MessageSeverityMatcher(severity);
    }

    private static Matcher<Message> codeAndSeverity(String code, Severity severity) {
        return CombinableMatcher.both(new MessageCodeMatcher(code)).and(new MessageSeverityMatcher(severity));
    }

    public static Matcher<Message> hasInvalidObject(Object invalidObject) {
        return new MessageInvalidObjectMatcher(invalidObject);
    }

    public static Matcher<Message> hasInvalidObject(Object invalidObject, String propertyName) {
        return new MessageInvalidObjectMatcher(invalidObject, propertyName);
    }

    public static Matcher<Message> hasMessageCodeThat(Matcher<String> messageCodeMatcher) {
        return new MessageCodeMatcher(messageCodeMatcher);
    }

    /**
     * Creates a {@link FeatureMatcher} that extracts a {@code &lt;U&gt;} feature from a
     * {@code &lt;T&gt;} object and matches it with the given matcher.
     * 
     * @param <T> the object type to match
     * @param <U> the feature type to match
     * @param featureExtractor the function to get the feature from the object
     * @param featureMatcher the matcher for the feature
     * @param featureDescription the description of the object and feature (e.g. "a car where the
     *            color is") that will be combined with the description of the given matcher
     * @param featureName the name of the feature
     * @return a {@link FeatureMatcher}
     */
    public static <T, U> FeatureMatcher<T, U> hasFeature(Function<T, U> featureExtractor,
            Matcher<U> featureMatcher,
            String featureDescription,
            String featureName) {
        return new FeatureMatcher<T, U>(featureMatcher, featureDescription, featureName) {

            @Override
            protected U featureValueOf(T actual) {
                return featureExtractor.apply(actual);
            }
        };
    }
}
