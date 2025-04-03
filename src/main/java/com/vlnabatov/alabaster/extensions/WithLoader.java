package com.vlnabatov.alabaster.extensions;

public class WithLoader {
    static {
        bind();
    }
    public static void bind() {
        Thread.currentThread().setContextClassLoader(WithLoader.class.getClassLoader());
    }
}
