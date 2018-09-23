package com.assylias.jbloomberg;

import com.google.common.collect.ImmutableSet;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UserEntitlements extends AbstractRequestResult {
    private final Set<Integer> eids = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public boolean isEmpty() {
        return eids.isEmpty();
    }

    public Set<Integer> getEids() {
        return ImmutableSet.copyOf(eids);
    }

    void addPermission(int eid) {
        eids.add(eid);
    }
}
