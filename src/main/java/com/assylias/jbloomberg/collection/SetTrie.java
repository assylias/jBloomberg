package com.assylias.jbloomberg.collection;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import java.util.*;

public final class SetTrie<T> {
    private final Comparator<T> comparator;
    private final TreeMap<T, Node> firstGeneration;

    private SetTrie(final Comparator<T> comparator) {
        this.comparator = comparator;
        this.firstGeneration = new TreeMap<>(comparator);
    }

    public static <T extends Comparable<? super T>> SetTrie<T> create() {
        return new SetTrie<>(Comparator.<T>naturalOrder());
    }

    public static <T> SetTrie<T> create(final Comparator<T> comparator) {
        return new SetTrie<>(comparator);
    }

    public synchronized boolean add(final Set<T> set) {
        Preconditions.checkArgument(!Preconditions.checkNotNull(set, "cannot add null set").isEmpty(), "cannot add empty set");
        final TreeSet<T> insertionOrder = insertionOrder(set);

        final T last = insertionOrder.last();
        Map<T, Node> children = firstGeneration;
        for (final T toInsert : insertionOrder) {
            final Node node = children.computeIfAbsent(toInsert, i -> new Node());
            if (toInsert.equals(last)) {
                final boolean added = node.isPlaceholder;
                node.isPlaceholder = false;
                return added;
            } else {
                children = node.children;
            }
        }
        throw new IllegalStateException("failed to add");
    }

    public synchronized boolean contains(final Set<T> set) {
        if (set == null || set.isEmpty()) {
            return false;
        }
        final TreeSet<T> insertionOrder = insertionOrder(set);
        final T last = insertionOrder.last();
        Map<T, Node> children = firstGeneration;
        for (final T toInsert : insertionOrder) {
            final Node node = children.get(toInsert);
            if (node == null) {
                return false;
            }
            if (toInsert.equals(last)) {
                return !node.isPlaceholder;
            } else {
                children = node.children;
            }
        }
        return false;
    }

    public synchronized Set<Set<T>> getAllSubsetsOf(final Set<T> set) {
        final Set<Set<T>> subsets = new HashSet<>();
        if (!set.isEmpty()) {
            final TreeSet<T> insertionOrder = insertionOrder(set);
            final T last = insertionOrder.last();
            traverseSubset(insertionOrder, last, Collections.emptySet(), firstGeneration, subsets);
        }
        subsets.remove(set);
        return subsets;
    }

    private void traverseSubset(final NavigableSet<T> insertionOrder, final T last, final Set<T> path, final TreeMap<T, Node> children, final Set<Set<T>> subsets) {
        for (final T toInsert : insertionOrder) {
            final Node node = children.get(toInsert);
            if (node != null) {
                final Set<T> newPath = Sets.union(path, Collections.singleton(toInsert));
                if (!node.isPlaceholder) {
                    subsets.add(newPath);
                }
                final NavigableSet<T> remaining = insertionOrder.subSet(toInsert, false, last, true);
                if (!remaining.isEmpty()) {
                    traverseSubset(remaining, last, newPath, node.children, subsets);
                }
            }
        }
    }

    public synchronized Set<Set<T>> getAllSupersetsOf(final Set<T> set) {
        final Set<Set<T>> supersets = new HashSet<>();
        if (!set.isEmpty()) {
            final TreeSet<T> insertionOrder = insertionOrder(set);
            final T first = insertionOrder.first();
            traverseSuperset(insertionOrder, first, Collections.emptySet(), firstGeneration, supersets);
        }
        supersets.remove(set);
        return supersets;
    }

    private void traverseSuperset(final NavigableSet<T> insertionOrder, final T first, final Set<T> path, final TreeMap<T, Node> children, final Set<Set<T>> supersets) {
        final Map<T, Node> pruned = children.headMap(first, true);
        for (final Map.Entry<T, Node> candidate : pruned.entrySet()) {
            final Node node = candidate.getValue();
            final Set<T> newPath = Sets.union(path, Collections.singleton(candidate.getKey()));
            final NavigableSet<T> remaining = insertionOrder.tailSet(candidate.getKey(), false);
            if (!remaining.isEmpty()) {
                traverseSuperset(remaining, remaining.first(), newPath, node.children, supersets);
            } else {
                if (!node.isPlaceholder) {
                    supersets.add(newPath);
                }
                addAllChildren(newPath, candidate.getValue().children, supersets);
            }
        }
    }

    private void addAllChildren(final Set<T> path, final Map<T, Node> children, final Set<Set<T>> supersets) {
        for (final Map.Entry<T, Node> child : children.entrySet()) {
            final Set<T> newPath = Sets.union(path, Collections.singleton(child.getKey()));
            if (!child.getValue().isPlaceholder) {
                supersets.add(newPath);
            }
            addAllChildren(newPath, child.getValue().children, supersets);
        }
    }

    private TreeSet<T> insertionOrder(final Set<T> set) {
        final TreeSet<T> insertionOrder = new TreeSet<>(comparator);
        insertionOrder.addAll(set);
        return insertionOrder;
    }

    private final class Node {
        final TreeMap<T, Node> children = new TreeMap<>(comparator);
        boolean isPlaceholder = true;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        toString(Collections.singletonList(firstGeneration), sb);
        return sb.toString();
    }

    private void toString(final Iterable<Map<T, Node>> children, final StringBuilder sb) {
        final LinkedList<Map<T, Node>> grandchildren = new LinkedList<>();
        children.forEach(subTree -> {
            subTree.forEach((key, value) -> {
                sb.append(key);
                grandchildren.add(value.children);
            });
            if (subTree.isEmpty()) {
                sb.append("-|");
            } else {
                sb.append('|');
            }
        });
        if (grandchildren.isEmpty() || grandchildren.stream().allMatch(Map::isEmpty)) {
            return;
        }
        sb.append('\n');
        toString(grandchildren, sb);
    }
}
