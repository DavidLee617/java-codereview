package com.company.juc.jucDesign.future;

public interface Task<S1, S2> {
    S2 get(S1 input);
}
