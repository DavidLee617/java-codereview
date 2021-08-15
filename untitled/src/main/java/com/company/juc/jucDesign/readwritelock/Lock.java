package com.company.juc.jucDesign.readwritelock;
public interface Lock {
    void lock() throws InterruptedException;
    void unlock();
}