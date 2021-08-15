package com.company.juc.jucDesign.activeobject;

import com.company.juc.jucDesign.future.FutureTask;

public class ActiveFuture<T> extends FutureTask<T> {
    @Override
    public void finish(T result){
        super.finish(result);
    }
}
