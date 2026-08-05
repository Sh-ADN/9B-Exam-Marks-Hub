sed -i -e '/val rank = studentResults.count { it.failedCount < result.failedCount } +/{
c\
                val rank = studentResults.count {\n\
                    if (it.failedCount < result.failedCount) {\n\
                        true\n\
                    } else if (it.failedCount == result.failedCount) {\n\
                        if (result.failedCount == 0) {\n\
                            val itGpa = it.gpa ?: 0.0\n\
                            val resultGpa = result.gpa ?: 0.0\n\
                            (itGpa > resultGpa) || (itGpa == resultGpa && it.grandTotal > result.grandTotal)\n\
                        } else {\n\
                            it.grandTotal > result.grandTotal\n\
                        }\n\
                    } else {\n\
                        false\n\
                    }\n\
                } + 1
}' app/src/main/java/com/abutorab/marks9b/ui/screens/TabulationEngine.kt
sed -i -e '/studentResults.count { it.failedCount == result.failedCount && it.grandTotal > result.grandTotal } + 1/d' app/src/main/java/com/abutorab/marks9b/ui/screens/TabulationEngine.kt
