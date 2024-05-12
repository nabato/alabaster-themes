package com.vlnabatov.alabaster.extensions.annotation

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity.INFORMATION
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes.*

import com.vlnabatov.alabaster.annotateSeparationMarks

private val valTokens = setOf(*BOOLEAN_TOKEN_SET.types, *NUMBER_TOKEN_SET.types, kNULL)


