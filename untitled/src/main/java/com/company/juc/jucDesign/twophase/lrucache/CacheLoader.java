package com.company.juc.jucDesign.twophase.lrucache;

@FunctionalInterface
public interface CacheLoader<T1, T2> {
    T2 load(T1 k);
}
