package com.assylias.jbloomberg.collection;

import com.google.common.collect.Sets;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@SuppressWarnings("unchecked")
@Test(groups = "unit")
public class SetTrieTest {
    private final Set<Set<Integer>> contents = setOfSets(
            Sets.newHashSet(1, 3),
            Sets.newHashSet(1, 3, 5),
            Sets.newHashSet(1, 4),
            Sets.newHashSet(1, 2, 4),
            Sets.newHashSet(2, 4),
            Sets.newHashSet(2, 3, 5)
    );
    private final SetTrie<Integer> setTrie;

    public SetTrieTest() {
        this.setTrie = SetTrie.create();
        contents.forEach(setTrie::add);
    }

    @Test
    public void testContains() {
        assertTrue(contents.stream().allMatch(setTrie::contains));
        assertFalse(setTrie.contains(Sets.newHashSet(1)));
        assertFalse(setTrie.contains(Sets.newHashSet(2)));
        assertFalse(setTrie.contains(Sets.newHashSet(1, 2)));
        assertFalse(setTrie.contains(Sets.newHashSet(2, 3)));
    }

    @Test
    public void testSubsets() {
        assertEquals(setTrie.getAllSubsetsOf(Sets.newHashSet(1, 3, 5)), setOfSets(Sets.newHashSet(1, 3)));
        assertEquals(setTrie.getAllSubsetsOf(Sets.newHashSet(1, 2, 4)), setOfSets(Sets.newHashSet(1, 4), Sets.newHashSet(2, 4)));
        assertEquals(setTrie.getAllSubsetsOf(Sets.newHashSet(2, 3, 5)), Collections.emptySet());
        assertEquals(setTrie.getAllSubsetsOf(Sets.newHashSet(1)), Collections.emptySet());
    }

    @Test
    public void testSupersets() {
        assertEquals(setTrie.getAllSupersetsOf(Sets.newHashSet(1)), setOfSets(Sets.newHashSet(1, 3), Sets.newHashSet(1, 3, 5), Sets.newHashSet(1, 4), Sets.newHashSet(1, 2, 4)));
        assertEquals(setTrie.getAllSupersetsOf(Sets.newHashSet(1, 2)), setOfSets(Sets.newHashSet(1, 2, 4)));
        assertEquals(setTrie.getAllSupersetsOf(Sets.newHashSet(3, 5)), setOfSets(Sets.newHashSet(1, 3, 5), Sets.newHashSet(2, 3, 5)));
        assertEquals(setTrie.getAllSupersetsOf(Sets.newHashSet(4, 5)), Collections.emptySet());
    }

//    @Test
//    public void testSetTrieWithRandomisedContents() {
//        Random rand = new Random(0);
//        for (int i = 0; i < 100; ++i) {
//            int size = rand.nextInt(49);
//            setTrie.add(IntStream.range(0, size + 1).map(x -> rand.nextInt(100)).boxed().collect(Collectors.toSet()));
//        }
//
//        setTrie.add(Sets.newHashSet(1, 2, 3));
//        setTrie.add(Sets.newHashSet(1, 2, 4));
//        setTrie.add(Sets.newHashSet(2, 3, 4));
//
//        setTrie.getAllSubsetsOf(Sets.newHashSet(1, 2, 3));
//
//        setTrie.getAllSupersetsOf(Sets.newHashSet(1, 4));
//    }

    private static Set<Set<Integer>> setOfSets(final Set<Integer> ... sets) {
        return Sets.newHashSet(sets);
    }
}
