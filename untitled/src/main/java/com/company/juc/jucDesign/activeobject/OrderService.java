package com.company.juc.jucDesign.activeobject;
import com.company.juc.jucDesign.future.Future;
public interface OrderService {
    Future<String> findOrferDetails(long orderId);
    void order(String account,long orderId);
}