package com.fashnix.app.domain

data class ClassificationResult(
    val category: String,
    val categoryConfidence: Float,
    val colour: String,
    val colourConfidence: Float,
    val isColourOther: Boolean,
    val occasion: String,
    val occasionConfidence: Float,
    val gender: String,
    val genderConfidence: Float
)