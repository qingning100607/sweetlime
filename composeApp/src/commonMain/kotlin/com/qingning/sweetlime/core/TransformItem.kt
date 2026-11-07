package com.qingning.sweetlime.core

data class TransformItem(
    val styleId: String,
    val styleTitle: String,
    val input: String,
    val output: String,
) {

    val key: String get() = styleId + SEPARATOR + input

    companion object {
        const val SEPARATOR = "\u0001"

        fun fromKey(key: String): Pair<String, String>? {
            val index = key.indexOf(SEPARATOR)
            if (index <= 0) return null
            return key.substring(0, index) to key.substring(index + 1)
        }

        fun of(transform: TextTransform, input: String): TransformItem? {
            val output = transform.transform(input)
            if (output == input) return null
            return TransformItem(transform.id, transform.title, input, output)
        }
    }
}