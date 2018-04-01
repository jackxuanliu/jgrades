package website.jackl.data_processor

import com.google.auto.service.AutoService
import org.jetbrains.annotations.Nullable
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.PrimitiveType
import javax.tools.Diagnostic.Kind.*

/**
 * Created by jack on 2/4/18.
 */

@Target(AnnotationTarget.CLASS)
annotation class Data

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("website.jackl.data_processor.Data")
@SupportedOptions(DataProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
@AutoService(Processor::class)
class DataProcessor : AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    private lateinit var pEnv: ProcessingEnvironment

    override fun init(p0: ProcessingEnvironment?) {
        super.init(p0)
        pEnv = p0!!
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {

        val annotatedElements = roundEnv.getElementsAnnotatedWith(Data::class.java)
        if (annotatedElements.isEmpty()) return false

        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: run {
            processingEnv.messager.printMessage(ERROR, "Can't find the target directory for generated Kotlin files.")
            return false
        }


        val stringBuilder = StringBuilder()

        stringBuilder.append("package website.jackl.generated.data\n")

        for (element in annotatedElements) {
            val typeElement = element.toTypeElementOrNull() ?: continue

            stringBuilder.append(generateConstructs(typeElement))
            stringBuilder.append(generateDestructs(typeElement))
        }


        val file = File(kaptKotlinGeneratedDir, "dataGenerated.kt")
        file.parentFile.mkdirs()
        file.writeText(stringBuilder.toString())

        return true
    }

    private fun generateConstructs(typeElement: TypeElement): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("fun " + getConstructName(typeElement.qualifiedName.toString()) + "(json: org.json.JSONObject?): " + typeElement.qualifiedName + "? {if (json==null)return null else return " + typeElement.qualifiedName + "(")
        var index = 0
        for (element in typeElement.enclosedElements) {
            if (element.kind == ElementKind.FIELD && element.toString() != "Companion") {
                val name = element.toString()

                if (index != 0) stringBuilder.append(",")

                val type = element.asType()
                val qualifiedType = type.toString()

                if ((qualifiedType.startsWith("java.util.List"))) {
                    val type = type as DeclaredType
                    val argument = type.typeArguments[0].toString()
                    stringBuilder.append(getListConstructName(argument) + "(" + "json.optJSONArray(\"" + name + "\")" + ")")
                } else if (qualifiedType.startsWith("java.util.Map")) {
                    val type = type as DeclaredType
                    val argument = type.typeArguments[1].toString()
                    stringBuilder.append(getMapConstructName(argument) + "(json.optJSONObject(\"" + name + "\"))")
                } else {

                    when (qualifiedType) {
                        "java.lang.String" -> {
                            stringBuilder.append("json.optString(\"" + name + "\")")
                        }
                        "java.lang.Long" -> {
                            stringBuilder.append("json.optLong(\"" + name + "\")")
                        }
                        "int" -> {
                            stringBuilder.append("json.optInt(\"" + name + "\")")
                        }
                        "double" -> {
                            stringBuilder.append("json.optDouble(\"" + name + "\")")
                        }
                        "long" -> {
                            stringBuilder.append("json.optLong(\"" + name + "\")")
                        }
                        "boolean" -> {
                            stringBuilder.append("json.optBoolean(\"" + name + "\")")
                        }
                        else -> {
                            pEnv.messager.printMessage(WARNING, qualifiedType)
                            stringBuilder.append(getConstructName(qualifiedType) + "(json.optJSONObject(\"" + name + "\"))")
                        }
                    }
                }

                if (element.getAnnotation(Nullable::class.java) == null) stringBuilder.append("!!")

                index++
            }

        }

        stringBuilder.append(")}\n")

        val elementQualifiedName = typeElement.qualifiedName.toString()
        stringBuilder.append("fun " + getListConstructName(elementQualifiedName) + "(json: org.json.JSONArray): " + "kotlin.collections.List<" + elementQualifiedName + ">{val list = kotlin.collections.mutableListOf<" + elementQualifiedName  + ">();var i = 0;while(i<json.length()){list.add(" + getConstructName(elementQualifiedName) + "(json.optJSONObject(i))!!);i++};return list}\n")
        stringBuilder.append("fun " + getMapConstructName(elementQualifiedName) + "(json: org.json.JSONObject): kotlin.collections.Map<String," + elementQualifiedName + ">{val map = kotlin.collections.mutableMapOf<String," + elementQualifiedName + ">();for(key in json.keys())map.put(key, " + getConstructName(elementQualifiedName) + "(json.optJSONObject(key))!!);return map}\n")



        return stringBuilder.toString()
    }

    private fun generateDestructs(typeElement: TypeElement): String {
        val stringBuilder = StringBuilder()

        val typeQualifiedName = typeElement.qualifiedName.toString()

        stringBuilder.append("fun " + typeQualifiedName +  ".write(): org.json.JSONObject = " + getWriteName(typeQualifiedName) + "(this)\n")
        stringBuilder.append("fun " + getWriteName(typeQualifiedName) + "(data: " + typeQualifiedName + "): org.json.JSONObject {val json = org.json.JSONObject();")

        for (element in typeElement.enclosedElements) {
            if (element.kind == ElementKind.FIELD && element.toString() != "Companion") {
                val name = element.toString()

                val type = element.asType()
                val qualifiedType = type.toString()

                if ((qualifiedType.startsWith("java.util.List"))) {
                    val type = type as DeclaredType
                    val argument = type.typeArguments[0].toString()
                    stringBuilder.append("if (data." + name + "!= null)" + "json.put(\"" + name + "\"," +getListWriteName(argument) + "(data." + name + "));")

                } else if (qualifiedType.startsWith("java.util.Map")) {
                    val type = type as DeclaredType
                    val argument = type.typeArguments[1].toString()
                    stringBuilder.append("if (data." + name + "!= null)" + "json.put(\"" + name + "\"," +getMapWriteName(argument) + "(data." + name + "));")
                } else {
                    when (qualifiedType) {
                        "java.lang.String" -> {
                            stringBuilder.append("if (data." + name + "!= null)" + "json.put(\"" + name + "\",data." + name + ");")
                        }
                        "java.lang.Long" -> {
                            stringBuilder.append("if (data." + name + "!= null)" + "json.put(\"" + name + "\",data." + name + ");")
                        }
                        "int" -> {
                            stringBuilder.append("if (data." + name + "!= null)" + "json.put(\"" + name + "\",data." + name + ");")
                        }
                        "double" -> {
                            stringBuilder.append("if (data." + name + "!= null)" + "json.put(\"" + name + "\",data." + name + ");")
                        }
                        "long" -> {
                            stringBuilder.append("if (data." + name + "!= null)" + "json.put(\"" + name + "\",data." + name + ");")
                        }
                        "boolean" -> {
                            stringBuilder.append("if (data." + name + "!= null)" + "json.put(\"" + name + "\",data." + name + ");")
                        }
                        else -> {
                            stringBuilder.append("if (data." + name + "!= null)" + "json.put(\"" + name + "\"," +getWriteName(qualifiedType) + "(data." + name + "));")
                        }
                    }
                }


            }

        }

        stringBuilder.append("return json;};\n")

        stringBuilder.append("fun " + getListWriteName(typeQualifiedName) +"(list: kotlin.collections.List<" + typeQualifiedName  + ">): org.json.JSONArray{val json = org.json.JSONArray();for(item in list)json.put(" + getWriteName(typeQualifiedName) +"(item));return json}\n")
        stringBuilder.append("fun " + getMapWriteName(typeQualifiedName) + "(map: kotlin.collections.Map<String," + typeQualifiedName + ">): org.json.JSONObject{val json = org.json.JSONObject();for(entry in map)json.put(entry.key," + getWriteName(typeQualifiedName) + "(entry.value));return json}\n")


        return stringBuilder.toString()
    }

    private fun Element.toTypeElementOrNull(): TypeElement? {
        if (this !is TypeElement) {
            processingEnv.messager.printMessage(ERROR, "Invalid element type, class expected", this)
            return null
        }

        return this
    }

    private fun getConstructName(qualifiedName: String): String {
        pEnv.messager.printMessage(WARNING, qualifiedName)
        val end = qualifiedName.split(".Data.")[1]
        return "construct" + end.replace(".", "")
    }

    private fun getListConstructName(qualifiedName: String): String {
        return getConstructName(qualifiedName) + "List"
    }

    private fun getMapConstructName(qualifiedName: String): String {
        return getConstructName(qualifiedName) + "Map"
    }

    private fun getWriteName(qualifiedName: String): String {
        val end = qualifiedName.split(".Data.")[1]
        return "write" + end.replace(".", "")
    }

    private fun getListWriteName(qualifiedName: String): String {
        return getWriteName(qualifiedName) + "List"
    }

    private fun getMapWriteName(qualifiedName: String): String {
        return getWriteName(qualifiedName) + "Map"
    }


}