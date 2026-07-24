androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            println(output.javaClass.methods.map { it.name }.joinToString(", "))
        }
    }
}
