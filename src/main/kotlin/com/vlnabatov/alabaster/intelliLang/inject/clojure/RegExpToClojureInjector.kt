package com.vlnabatov.alabaster.intelliLang.inject.clojure

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import cursive.psi.impl.ClSharp
import cursive.psi.impl.ClStringLiteral
import org.intellij.lang.regexp.RegExpLanguage


class RegExpToClojureInjector : MultiHostInjector {

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        println(context.language)
        if (context !is ClStringLiteral || context.parent !is ClSharp) return


        if (context.parent.children.size == 1) {
            registrar
                .startInjecting(RegExpLanguage.INSTANCE)
                .addPlace(null, null, context as PsiLanguageInjectionHost, TextRange.from(1, (context as PsiElement).textLength - 2))
                .doneInjecting()
        }
    }

    override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> {
        return mutableListOf(ClStringLiteral::class.java)
    }
}
