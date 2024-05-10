package com.vlnabatov.alabaster;

public class WithLoader {
    static {
        bind();
    }

    public static void bind() {
        Thread.currentThread().setContextClassLoader(WithLoader.class.getClassLoader());
    }
}