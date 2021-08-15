package com.company.juc.jucDesign.future;
public interface Future<T> {
    T get() throws InterruptedException;
    boolean done();
}