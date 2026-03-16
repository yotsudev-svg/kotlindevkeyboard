package com.blogspot.yotsudev.kotlindevkeyboard.data

import androidx.annotation.StringRes
import com.blogspot.yotsudev.kotlindevkeyboard.R

// String resource ID enables i18n for category names
enum class SnippetCategory(@param:StringRes val displayNameRes: Int) {
    OPERATOR(R.string.snippet_category_operator),
    CONTROL(R.string.snippet_category_control),
    DECLARATION(R.string.snippet_category_declaration),
    MODIFIER(R.string.snippet_category_modifier),
    SCOPE(R.string.snippet_category_scope),
    COLLECTION(R.string.snippet_category_collection),
    NULL_SAFETY(R.string.snippet_category_null_safety),
    COROUTINE(R.string.snippet_category_coroutine),
    COMPOSE(R.string.snippet_category_compose),
    STRING(R.string.snippet_category_string),
    CONVERT(R.string.snippet_category_convert),
    EXCEPTION(R.string.snippet_category_exception),
    OUTPUT(R.string.snippet_category_output),
    CUSTOM(R.string.snippet_category_custom),
}

data class Snippet(
    val label: String,
    // Defaults to label when insert text differs
    val insert: String = label,
    val category: SnippetCategory = SnippetCategory.CUSTOM,
)

// Built-in snippets grouped by category
val kotlinSnippets: List<Snippet> = buildList {
    fun add(cat: SnippetCategory, vararg snippets: Snippet) =
        addAll(snippets.map { it.copy(category = cat) })

    add(SnippetCategory.OPERATOR,
        // Equality and comparison
        Snippet("=="), Snippet("!="), Snippet("==="), Snippet("!=="),
        Snippet("<="), Snippet(">="),
        // Logical
        Snippet("&&"), Snippet("||"), Snippet("!!"),
        // Null-related
        Snippet("?."), Snippet("?:"), Snippet("?:return", "?: return"),
        Snippet("?:throw", "?: throw "),
        // Miscellaneous
        Snippet("->"), Snippet("=>"), Snippet("::"), Snippet(".."), Snippet("..<"),
        Snippet("..."),
        Snippet("+="), Snippet("-="), Snippet("*="), Snippet("/="), Snippet("%="),
        Snippet("@"), Snippet("^"), Snippet("`"), Snippet("#"), Snippet("\\"),
    )

    add(SnippetCategory.CONTROL,
        Snippet("if"), Snippet("else"), Snippet("else if", "else if "),
        Snippet("when"),
        Snippet("for"), Snippet("while"), Snippet("do"),
        Snippet("return"), Snippet("return@"),
        Snippet("break"), Snippet("continue"),
        // throw/try/catch/finally also registered in EXCEPTION for discoverability
        Snippet("throw"), Snippet("try"), Snippet("catch"), Snippet("finally"),
        Snippet("runCatching"),
        Snippet("repeat"),
    )

    add(SnippetCategory.DECLARATION,
        Snippet("fun"), Snippet("val"), Snippet("var"),
        Snippet("class"), Snippet("object"), Snippet("interface"),
        Snippet("data class", "data class "),
        Snippet("sealed class", "sealed class "),
        Snippet("sealed interface", "sealed interface "),
        Snippet("abstract class", "abstract class "),
        Snippet("enum class", "enum class "),
        Snippet("annotation class", "annotation class "),
        Snippet("value class", "@JvmInline\nvalue class "),
        Snippet("companion object", "companion object "),
        Snippet("typealias"),
        Snippet("by"),
        Snippet("init"),
    )

    add(SnippetCategory.MODIFIER,
        // Access modifiers
        Snippet("private"), Snippet("public"), Snippet("protected"), Snippet("internal"),
        // Inheritance
        Snippet("open"), Snippet("override"), Snippet("final"), Snippet("abstract"),
        Snippet("sealed"),
        // Function modifiers
        Snippet("suspend"), Snippet("inline"), Snippet("noinline"), Snippet("crossinline"),
        Snippet("infix"), Snippet("operator"), Snippet("tailrec"), Snippet("external"),
        // Property modifiers
        Snippet("companion"), Snippet("const val", "const val "),
        Snippet("lateinit var", "lateinit var "),
        Snippet("by lazy", "by lazy {\n}"),
        Snippet("by lazy(LazyThreadSafetyMode.NONE)", "by lazy(LazyThreadSafetyMode.NONE) {\n}"),
        Snippet("vararg"),
    )

    add(SnippetCategory.SCOPE,
        Snippet("let"), Snippet("run"), Snippet("apply"), Snippet("also"), Snippet("with"),
        Snippet("use"),
        Snippet("takeIf"), Snippet("takeUnless"),
    )

    add(SnippetCategory.COLLECTION,
        // Transform
        // map/filter/onEach also registered in COROUTINE as Flow operators
        Snippet("map"), Snippet("mapNotNull"), Snippet("flatMap"), Snippet("flatten"),
        Snippet("associate"), Snippet("associateBy"),
        Snippet("groupBy"), Snippet("partition"),
        Snippet("zip"), Snippet("unzip"),
        // Filter
        Snippet("filter"), Snippet("filterNotNull"), Snippet("filterIsInstance"),
        Snippet("distinct"), Snippet("distinctBy"),
        // Search
        Snippet("find"), Snippet("findLast"),
        Snippet("first"), Snippet("firstOrNull"),
        Snippet("last"), Snippet("lastOrNull"),
        Snippet("any"), Snippet("all"), Snippet("none"),
        Snippet("contains"),
        // Aggregation
        Snippet("count"), Snippet("sum"), Snippet("sumOf"),
        Snippet("reduce"), Snippet("fold"),
        Snippet("minOf"), Snippet("maxOf"),
        Snippet("minByOrNull"), Snippet("maxByOrNull"),
        // Operations
        Snippet("forEach"), Snippet("forEachIndexed"),
        Snippet("onEach"), Snippet("onEachIndexed"),
        Snippet("take"), Snippet("drop"),
        Snippet("takeWhile"), Snippet("dropWhile"),
        Snippet("chunked"), Snippet("windowed"),
        Snippet("sortedBy"), Snippet("sortedByDescending"),
        Snippet("sortedWith"),
        Snippet("reversed"), Snippet("shuffled"),
        // Conversion
        Snippet("toList"), Snippet("toMutableList"),
        Snippet("toSet"), Snippet("toMutableSet"),
        Snippet("toMap"), Snippet("toMutableMap"),
        Snippet("joinToString"),
        // Construction
        Snippet("listOf"), Snippet("mutableListOf"), Snippet("emptyList()"),
        Snippet("mapOf"), Snippet("mutableMapOf"), Snippet("emptyMap()"),
        Snippet("setOf"), Snippet("mutableSetOf"), Snippet("emptySet()"),
        Snippet("arrayOf"), Snippet("arrayOfNulls"),
        Snippet("buildList"), Snippet("buildMap"), Snippet("buildSet"),
        Snippet("List("), Snippet("Array("),
        Snippet("sequence"),
    )

    add(SnippetCategory.NULL_SAFETY,
        Snippet("null"),
        Snippet("is"), Snippet("!is"),
        Snippet("as"), Snippet("as?"),
        Snippet("in"), Snippet("!in"),
        Snippet("checkNotNull"), Snippet("requireNotNull"),
        // require/check/error also registered in EXCEPTION for discoverability
        Snippet("require"), Snippet("check"),
        Snippet("error"),
    )

    add(SnippetCategory.COROUTINE,
        // Coroutine builders
        Snippet("launch"), Snippet("async"), Snippet("await"), Snippet("awaitAll"),
        Snippet("runBlocking"),
        Snippet("withContext"),
        Snippet("withTimeout"), Snippet("withTimeoutOrNull"),
        // Dispatchers
        Snippet("Dispatchers.IO"), Snippet("Dispatchers.Main"),
        Snippet("Dispatchers.Default"), Snippet("Dispatchers.Unconfined"),
        // Flow; map/filter/onEach/catch also registered in COLLECTION/CONTROL for discoverability
        Snippet("flow"), Snippet("flowOf"), Snippet("asFlow"),
        Snippet("collect"), Snippet("collectLatest"),
        Snippet("emit"), Snippet("emitAll"),
        Snippet("map"), Snippet("filter"), Snippet("onEach"),
        Snippet("catch"), Snippet("onCompletion"),
        Snippet("stateIn"), Snippet("shareIn"),
        Snippet("StateFlow"), Snippet("MutableStateFlow"),
        Snippet("SharedFlow"), Snippet("MutableSharedFlow"),
        // Scope and synchronization
        Snippet("coroutineScope"), Snippet("supervisorScope"),
        Snippet("CoroutineScope"), Snippet("SupervisorJob"),
        Snippet("cancel"), Snippet("cancelAndJoin"),
        Snippet("delay"), Snippet("yield"),
        Snippet("Mutex"), Snippet("Channel"),
        Snippet("produce"), Snippet("actor"),
    )

    add(SnippetCategory.COMPOSE,
        // State
        Snippet("remember"), Snippet("rememberSaveable"),
        Snippet("mutableStateOf"), Snippet("mutableStateListOf"),
        Snippet("mutableIntStateOf"), Snippet("mutableLongStateOf"),
        Snippet("mutableFloatStateOf"), Snippet("derivedStateOf"),
        Snippet("snapshotFlow"),
        Snippet("collectAsState"), Snippet("collectAsStateWithLifecycle"),
        // Side effects
        Snippet("LaunchedEffect"), Snippet("SideEffect"),
        Snippet("DisposableEffect"), Snippet("rememberUpdatedState"),
        Snippet("rememberCoroutineScope"),
        // Layout
        Snippet("Column"), Snippet("Row"), Snippet("Box"),
        Snippet("LazyColumn"), Snippet("LazyRow"),
        Snippet("Scaffold"), Snippet("Surface"),
        // UI components
        Snippet("Text"), Snippet("Button"), Snippet("IconButton"),
        Snippet("TextField"), Snippet("OutlinedTextField"),
        Snippet("Image"), Snippet("Icon"),
        Snippet("Spacer"), Snippet("Divider"),
        Snippet("Card"), Snippet("AlertDialog"),
        // Modifier
        Snippet("Modifier"),
        Snippet(".fillMaxSize()"), Snippet(".fillMaxWidth()"), Snippet(".fillMaxHeight()"),
        Snippet(".wrapContentSize()"),
        Snippet(".padding()"), Snippet(".size()"), Snippet(".weight()"),
        Snippet(".clickable {}"),
        // CompositionLocals
        Snippet("LocalContext"), Snippet("LocalDensity"),
        Snippet("LocalConfiguration"),
        // Annotations
        Snippet("@Composable"), Snippet("@Preview"),
        Snippet("@SuppressLint"),
    )

    add(SnippetCategory.STRING,
        Snippet("trimIndent()"), Snippet("trimMargin()"),
        Snippet("split()"), Snippet("replace()"),
        Snippet("contains()"), Snippet("startsWith()"), Snippet("endsWith()"),
        Snippet("uppercase()"), Snippet("lowercase()"),
        Snippet("trim()"), Snippet("trimStart()"), Snippet("trimEnd()"),
        Snippet("padStart()"), Snippet("padEnd()"),
        Snippet("substring()"), Snippet("take()"), Snippet("drop()"),
        Snippet("isBlank()"), Snippet("isNotBlank()"),
        Snippet("isEmpty()"), Snippet("isNotEmpty()"),
        Snippet("isNullOrBlank()"), Snippet("isNullOrEmpty()"),
        Snippet("format()"), Snippet("toRegex()"),
        Snippet("buildString"),
        Snippet("\"\"\".trimIndent()", "\"\"\"\n    \n\"\"\".trimIndent()"),
    )

    add(SnippetCategory.CONVERT,
        Snippet("toInt()"), Snippet("toIntOrNull()"),
        Snippet("toLong()"), Snippet("toLongOrNull()"),
        Snippet("toDouble()"), Snippet("toDoubleOrNull()"),
        Snippet("toFloat()"), Snippet("toFloatOrNull()"),
        Snippet("toBigDecimal()"),
        Snippet("toBoolean()"), Snippet("toBooleanStrictOrNull()"),
        Snippet("toString()"), Snippet("toCharArray()"),
        Snippet("toByteArray()"),
    )

    add(SnippetCategory.EXCEPTION,
        Snippet("try-catch", "try {\n} catch (e: Exception) {\n}"),
        Snippet("try-finally", "try {\n} finally {\n}"),
        // throw/runCatching also registered in CONTROL for discoverability
        Snippet("throw"),
        Snippet("Exception("), Snippet("RuntimeException("),
        Snippet("IllegalArgumentException("),
        Snippet("IllegalStateException("),
        Snippet("UnsupportedOperationException("),
        Snippet("NullPointerException("),
        Snippet("IndexOutOfBoundsException("),
        Snippet("NoSuchElementException("),
        // require/check/error also registered in NULL_SAFETY for discoverability
        Snippet("require"), Snippet("check"), Snippet("error"),
        Snippet("runCatching"),
        Snippet("getOrElse"), Snippet("getOrNull"), Snippet("getOrThrow"),
        Snippet("onFailure"), Snippet("onSuccess"),
        Snippet("Result.success("), Snippet("Result.failure("),
    )

    add(SnippetCategory.OUTPUT,
        Snippet("println"), Snippet("print"),
        Snippet("Log.d"), Snippet("Log.i"),
        Snippet("Log.w"), Snippet("Log.e"),
        Snippet("Log.v"),
        Snippet("System.err.println"),
        Snippet("buildString"),
    )
}