/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.faktorips.annotation.UtilityClass;

/**
 * Utility class for handling {@link MessageList} objects.
 */
@UtilityClass
public class MessageLists {

    private MessageLists() {
        // do not instantiate
    }

    /**
     * Returns a new empty {@code MessageList}.
     * 
     * @return a new empty {@code MessageList}
     */
    public static final MessageList emptyMessageList() {
        return new MessageList();
    }

    /**
     * Returns the given {@code MessageList} if it is not {@code null} or a new empty
     * {@code MessageList} if it is.
     * 
     * @param ml a {@code MessageList}, may be {@code null}
     * @return the given {@code MessageList} if it is not {@code null} or a new empty
     *         {@code MessageList} if it is
     */
    public static final MessageList orEmptyMessageList(MessageList ml) {
        if (ml == null) {
            return emptyMessageList();
        } else {
            return ml;
        }
    }

    /**
     * Returns a new {@code MessageList} containing the same messages as the given list, sorted by
     * descending {@code Severity}. Within each severity the previous order is preserved. Returns an
     * empty {@code MessageList} if {@code null} is given.
     * 
     * @param unsortedMessageList a {@code MessageList} that may be {@code null}
     * @return a new {@code MessageList} containing the same messages as the given list, sorted by
     *         descending {@code Severity}
     */
    public static final MessageList sortBySeverity(MessageList unsortedMessageList) {
        if (unsortedMessageList == null) {
            return emptyMessageList();
        }
        return StreamSupport.stream(unsortedMessageList.spliterator(), false)
                .collect(Collectors.groupingBy(Message::getSeverity))
                .entrySet().stream().sorted((e1, e2) -> e2.getKey().compareTo(e1.getKey()))
                .flatMap(e -> e.getValue().stream())
                .collect(collectMessages());
    }

    /**
     * Returns a new {@code MessageList} that contains all messages from the given list that satisfy
     * the given predicate. Returns an empty {@code MessageList} if {@code null} is given.
     * 
     * @param ml a {@code MessageList} that may be {@code null}
     * @param predicate a predicate to filter messages with
     * @return a new {@code MessageList} all messages from the given list that satisfy the given
     *         predicate
     */
    public static final MessageList filtered(MessageList ml, Predicate<Message> predicate) {
        if (ml == null) {
            return emptyMessageList();
        }
        return StreamSupport.stream(ml.spliterator(), false)
                .filter(predicate)
                .collect(collectMessages());
    }

    /**
     * Returns a {@link Collector} that can be used to {@linkplain Stream#collect(Collector)
     * collect} a {@code Stream} of {@link MessageList MessageLists} into a new {@code MessageList}.
     * 
     * @return a {@code Collector} that collects {@link MessageList MessageLists} into a new
     *         {@code MessageList}
     */
    public static final Collector<MessageList, ?, MessageList> flatten() {
        return new MessageListCollector();
    }

    /**
     * Returns a {@link Collector} that can be used to {@linkplain Stream#collect(Collector)
     * collect} messages from a {@code Stream} of {@link Message Messages} into a
     * {@code MessageList}.
     * 
     * @return a {@code Collector} that collects messages into a {@code MessageList}
     */
    public static final Collector<Message, ?, MessageList> collectMessages() {
        return new MessageCollector();
    }

    /**
     * Returns a new {@code MessageList} that combines the messages of the given
     * {@code MessageLists} including duplicates. Returns an empty {@code MessageList} if
     * {@code null} is given.
     * 
     * @param messageLists the {@code MessageLists} that the new {@code MessageList} will join. May
     *            be {@code null}
     * @return a new {@code MessageList} that contains all messages of the given
     *         {@code MessageLists}
     */
    public static final MessageList join(MessageList... messageLists) {
        if (messageLists == null) {
            return new MessageList();
        }
        return Stream.of(messageLists).collect(MessageLists.flatten());
    }

    private static class MessageListCollector implements Collector<MessageList, MessageList, MessageList> {

        @Override
        public Supplier<MessageList> supplier() {
            return MessageList::new;
        }

        @Override
        public BiConsumer<MessageList, MessageList> accumulator() {
            return MessageList::add;
        }

        @Override
        public BinaryOperator<MessageList> combiner() {
            return (ml1, ml2) -> {
                MessageList ml = new MessageList();
                ml.add(ml1);
                ml.add(ml2);
                return ml;
            };
        }

        @Override
        public Function<MessageList, MessageList> finisher() {
            return Function.identity();
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Collections.singleton(Characteristics.IDENTITY_FINISH);
        }

    }

    private static class MessageCollector implements Collector<Message, MessageList, MessageList> {

        @Override
        public Supplier<MessageList> supplier() {
            return MessageList::new;
        }

        @Override
        public BiConsumer<MessageList, Message> accumulator() {
            return MessageList::add;
        }

        @Override
        public BinaryOperator<MessageList> combiner() {
            return (ml1, ml2) -> {
                MessageList ml = new MessageList();
                ml.add(ml1);
                ml.add(ml2);
                return ml;
            };
        }

        @Override
        public Function<MessageList, MessageList> finisher() {
            return Function.identity();
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Collections.singleton(Characteristics.IDENTITY_FINISH);
        }

    }
}
