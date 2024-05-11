package com.vlnabatov.alabaster

open class WithLoader {
    companion object {
        init {
            bind()
        }

        @JvmStatic
        fun bind() {
            Thread.currentThread().contextClassLoader = WithLoader::class.java.classLoader
        }
    }
}
