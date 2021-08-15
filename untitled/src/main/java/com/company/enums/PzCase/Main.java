package com.company.enums.PzCase;

public class Main {
    public static void main(String[] args) {
        System.out.println(PizzaStatus.Ordered);
        System.out.println(PizzaStatus.Ordered.name());
        System.out.println(PizzaStatus.Ordered.name().getClass());
    }
}
