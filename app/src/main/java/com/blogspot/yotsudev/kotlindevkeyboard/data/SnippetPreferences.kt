package com.blogspot.yotsudev.kotlindevkeyboard.data

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

data class UserCategory(val id: String, val name: String)

data class CustomSnippetEntry(
    val label: String,
    val insert: String,
    val categoryId: String,
)

object SnippetPreferences {
    private const val PREFS_NAME    = "snippet_prefs"
    private const val KEY_HIDDEN    = "hidden_labels"
    private const val KEY_CUSTOM    = "custom_snippets"
    private const val KEY_USER_CATS = "user_categories"

    fun loadHiddenLabels(context: Context): Set<String> {
        val raw = prefs(context).getString(KEY_HIDDEN, "[]") ?: "[]"
        val arr = JSONArray(raw)
        return (0 until arr.length()).map { arr.getString(it) }.toSet()
    }

    fun saveHiddenLabels(context: Context, hidden: Set<String>) {
        val arr = JSONArray().apply { hidden.forEach { put(it) } }
        prefs(context).edit { putString(KEY_HIDDEN, arr.toString()) }
    }

    fun loadUserCategories(context: Context): List<UserCategory> {
        val raw = prefs(context).getString(KEY_USER_CATS, "[]") ?: "[]"
        val arr = JSONArray(raw)
        return (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            UserCategory(id = obj.getString("id"), name = obj.getString("name"))
        }
    }

    fun saveUserCategories(context: Context, cats: List<UserCategory>) {
        val arr = JSONArray().apply {
            cats.forEach { put(JSONObject().apply { put("id", it.id); put("name", it.name) }) }
        }
        prefs(context).edit { putString(KEY_USER_CATS, arr.toString()) }
    }

    fun loadCustomEntries(context: Context): List<CustomSnippetEntry> {
        val raw = prefs(context).getString(KEY_CUSTOM, "[]") ?: "[]"
        val arr = JSONArray(raw)
        return (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            CustomSnippetEntry(
                label      = obj.getString("label"),
                insert     = obj.getString("insert"),
                // Falls back to CUSTOM if field is absent
                categoryId = obj.optString("categoryId", SnippetCategory.CUSTOM.name),
            )
        }
    }

    fun saveCustomEntries(context: Context, entries: List<CustomSnippetEntry>) {
        val arr = JSONArray().apply {
            entries.forEach { s ->
                put(JSONObject().apply {
                    put("label",      s.label)
                    put("insert",     s.insert)
                    put("categoryId", s.categoryId)
                })
            }
        }
        prefs(context).edit { putString(KEY_CUSTOM, arr.toString()) }
    }

    // Merges defaults and customs, excluding hidden labels
    // Custom snippets take precedence over defaults with the same label
    fun loadVisibleSnippets(context: Context): List<Snippet> {
        val hidden  = loadHiddenLabels(context)
        val entries = loadCustomEntries(context)
        val customs = entries.filter { it.label !in hidden }
            .map { entry ->
                val cat = SnippetCategory.entries
                    .firstOrNull { it.name == entry.categoryId }
                    ?: SnippetCategory.CUSTOM
                Snippet(label = entry.label, insert = entry.insert, category = cat)
            }
        val customLabels = customs.map { it.label }.toSet()
        val defaults = kotlinSnippets.filter { it.label !in hidden && it.label !in customLabels }
        return defaults + customs
    }

    // Removes category and all its snippets atomically
    // Also cleans up hidden labels for removed snippets to avoid stale entries
    fun deleteUserCategory(context: Context, categoryId: String) {
        val allEntries    = loadCustomEntries(context)
        val removedLabels = allEntries.filter { it.categoryId == categoryId }.map { it.label }.toSet()
        val entries = allEntries.filter { it.categoryId != categoryId }
        val cats    = loadUserCategories(context).filter { it.id != categoryId }
        val hidden  = loadHiddenLabels(context) - removedLabels
        saveHiddenLabels(context, hidden)
        saveUserCategories(context, cats)
        saveCustomEntries(context, entries)
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}