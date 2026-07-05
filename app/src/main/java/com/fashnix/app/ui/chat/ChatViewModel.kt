package com.fashnix.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fashnix.app.BuildConfig
import com.fashnix.app.data.model.ChatMessage
import com.fashnix.app.data.model.ClothingItem
import com.fashnix.app.data.repository.AuthRepository
import com.fashnix.app.data.repository.ChatRepository
import com.fashnix.app.data.repository.WardrobeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

/**
 * ChatViewModel: High-Intelligence Fashion Copilot.
 * Uses an on-device wardrobe-aware stylist so chat stays usable on the free Firebase plan.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
    private val wardrobeRepository: WardrobeRepository,
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping = _isTyping.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState = _errorState.asStateFlow()

    init {
        viewModelScope.launch { loadMessages() }
    }

    private suspend fun loadMessages() {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            sendWelcomeMessage()
            return
        }

        chatRepository.getMessages(userId).fold(
            onSuccess = { 
                if (it.isEmpty()) sendWelcomeMessage() else _messages.value = it
            },
            onFailure = {
                sendWelcomeMessage()
            }
        )
    }

    private fun sendWelcomeMessage() {
        val welcome = ChatMessage(
            id = "welcome",
            role = "assistant",
            content = "Hi, I am your Fashnix stylist. Ask me what to wear for work, a party, casual day, weather, laundry, or gaps in your wardrobe."
        )
        _messages.value = listOf(welcome)
    }

    fun sendMessage(text: String, imageUrl: String? = null) {
        if (text.isBlank() && imageUrl == null) return
        
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _errorState.value = "Please sign in to use chat."
            return
        }

        val userMsg = ChatMessage(
            id = System.currentTimeMillis().toString(),
            role = "user",
            content = text,
            imageUrl = imageUrl
        )
        
        _messages.value = _messages.value + userMsg
        viewModelScope.launch { chatRepository.saveMessage(userId, userMsg) }

        generateAiResponse(userId)
    }

    private fun generateAiResponse(userId: String) {
        _isTyping.value = true
        viewModelScope.launch {
            try {
                delay(450)
                val items = wardrobeRepository.getUserItems(userId).getOrDefault(emptyList())
                val lastUserText = _messages.value.lastOrNull { it.role == "user" }?.content.orEmpty()
                val reply = fetchCloudStylistReply(lastUserText, items)
                    ?: buildLocalStylistReply(lastUserText, items)
                appendAssistantMessage(userId, reply)
            } catch (e: Exception) {
                val lastUserText = _messages.value.lastOrNull { it.role == "user" }?.content.orEmpty()
                appendAssistantMessage(userId, buildLocalStylistReply(lastUserText, emptyList()))
            } finally {
                _isTyping.value = false
            }
        }
    }

    private fun buildLocalStylistReply(userText: String, items: List<ClothingItem>): String {
        val query = userText.lowercase()
        if (query.isBlank()) {
            return "Send me your plan, weather, event, color, or a clothing photo. I will answer with a practical outfit, not a generic tip."
        }

        if (items.isEmpty()) {
            return buildGeneralStylistReply(query)
        }

        if (query.contains("gap") || query.contains("buy") || query.contains("shopping")) {
            return buildGapReply(items)
        }

        if (query.contains("laundry") || query.contains("wash") || query.contains("dirty")) {
            val needsCare = items.filter { it.laundryStatus.equals("NeedsWash", true) || it.laundryStatus.equals("Dirty", true) || it.wearCount >= it.laundryIntervalWears }
            return if (needsCare.isEmpty()) {
                "Your wardrobe looks fresh. Keep rotating pieces so no single item gets overused, and mark items as worn after each outfit."
            } else {
                "Care reminder: ${needsCare.take(3).joinToString(", ") { it.name }} should be washed or refreshed before your next outfit."
            }
        }

        val eventHint = when {
            query.contains("interview") || query.contains("office") || query.contains("work") || query.contains("meeting") || query.contains("presentation") || query.contains("boss") -> "Formal"
            query.contains("party") || query.contains("date") || query.contains("dinner") -> "Party"
            query.contains("sport") || query.contains("gym") -> "Sports"
            query.contains("wedding") || query.contains("festive") || query.contains("eid") -> "Festive"
            query.contains("casual") || query.contains("daily") || query.contains("today") -> "Casual"
            else -> null
        }

        val outfitIntent = listOf("wear", "outfit", "meeting", "office", "work", "presentation", "party", "date", "wedding", "eid", "college", "today")
            .any { it in query }

        val candidates = items
            .filter { item -> eventHint == null || item.occasion.contains(eventHint, ignoreCase = true) }
            .ifEmpty { items }

        val apparel = candidates.firstOrNull { it.category.contains("apparel", true) }
        val footwear = candidates.firstOrNull { it.category.contains("footwear", true) }
        val accessory = candidates.firstOrNull { it.category.contains("accessor", true) }

        val outfit = listOfNotNull(apparel, footwear, accessory)
            .joinToString(" + ") { it.name }
            .ifEmpty { candidates.take(3).joinToString(" + ") { it.name } }

        if (outfitIntent) {
            val opener = when (eventHint) {
                "Formal" -> "For the office meeting, keep it sharp: $outfit."
                "Party" -> "For the party, use one standout piece: $outfit."
                "Sports" -> "For an active day, stay comfortable: $outfit."
                "Festive" -> "For the festive event, keep it polished: $outfit."
                "Casual" -> "For today, keep it clean and easy: $outfit."
                else -> "A practical outfit from your wardrobe: $outfit."
            }
            val fitTip = when (eventHint) {
                "Formal" -> "Choose clean lines, polished shoes, and one quiet accessory."
                "Party" -> "Let only one item be loud so the whole look still feels premium."
                "Sports" -> "Avoid heavy accessories and pick breathable fabric."
                "Festive" -> "Use one premium color or texture and keep shoes spotless."
                else -> "Repeat one color once and keep the shoes fresh."
            }
            val weatherTip = when {
                query.contains("rain") -> " If it rains, avoid suede or delicate shoes."
                query.contains("cold") || query.contains("winter") -> " Add a neat layer if it is cold."
                query.contains("hot") || query.contains("summer") -> " Use breathable fabric if it is hot."
                else -> ""
            }
            return "$opener $fitTip$weatherTip"
        }

        val tip = when {
            query.contains("rain") -> "Add darker footwear and avoid delicate fabrics if you are going outside."
            query.contains("cold") || query.contains("winter") -> "Layer with a jacket or heavier texture so the outfit feels intentional."
            query.contains("hot") || query.contains("summer") -> "Choose breathable fabric and keep accessories light."
            eventHint == "Formal" -> "Keep colors controlled and make sure the fit looks crisp."
            eventHint == "Party" -> "Let one piece stand out and keep the rest clean."
            else -> "Repeat one color once, keep shoes clean, and avoid over-accessorizing."
        }

        return "Fashnix pick: $outfit. $tip"
    }

    private fun buildGeneralStylistReply(query: String): String {
        val greeting = listOf("hello", "hi", "hey", "salam", "assalam", "hy").any { it in query }
        if (greeting && query.length < 18) {
            return "Hey, I am ready. Tell me where you are going, the weather, and one color you want to wear. I will build a full outfit for you."
        }

        val event = detectEvent(query)
        val color = detectColor(query)
        val weather = detectWeather(query)

        if ("what" in query && ("wear" in query || "pehn" in query || "outfit" in query)) {
            return buildOutfitFormula(event, color, weather)
        }

        if ("color" in query || "colour" in query || "match" in query || "matching" in query) {
            val base = color ?: "black"
            return when (base) {
                "black" -> "Black works best with white, grey, denim blue, tan, or one electric orange accent. Keep shoes clean and avoid adding too many loud colors."
                "orange" -> "With orange, keep the rest controlled: black jeans, white tee, dark shoes, and one small orange detail. Orange should be the highlight, not the whole outfit."
                "white" -> "White is safest with blue denim, black trousers, beige layers, or clean sneakers. Add one darker item so the look does not feel flat."
                "blue" -> "Blue pairs well with white, grey, tan, black, and brown leather. For a sharper look, use blue plus one neutral only."
                else -> "$base can work if you balance it with black, white, denim, or beige. Keep one hero color and let everything else support it."
            }
        }

        if ("buy" in query || "shopping" in query || "purchase" in query) {
            return "Before buying, check three things: does it match at least 3 outfits, can you wear it in 2 occasions, and is it different from what you already own? If not, skip it."
        }

        if ("laundry" in query || "wash" in query || "dirty" in query) {
            return "If you are unsure about laundry, avoid tight collars, sweaty inner layers, and shoes that smell. Pick a clean top first, then build the outfit around it."
        }

        if ("fat" in query || "slim" in query || "height" in query || "body" in query) {
            return "For body balance: use darker colors where you want a slimmer look, keep shoulder fit clean, avoid oversized bunching, and choose one vertical line like an open jacket."
        }

        if ("photo" in query || "image" in query || "scan" in query) {
            return "Upload or scan the clothing photo in clear light with the item centered. I can help identify category, color, occasion, and how to style it."
        }

        return buildAdaptiveFallback(query)
    }

    private suspend fun fetchCloudStylistReply(userText: String, items: List<ClothingItem>): String? {
        val endpoint = BuildConfig.CLOUD_FUNCTION_URL.takeIf { it.isNotBlank() } ?: return null
        if (userText.isBlank()) return null
        return withContext(Dispatchers.IO) {
            runCatching {
                val wardrobe = JSONArray().apply {
                    items.take(18).forEach { item ->
                        put(JSONObject().apply {
                            put("name", item.name)
                            put("category", item.category)
                            put("color", item.color)
                            put("occasion", item.occasion)
                            put("laundryStatus", item.laundryStatus)
                            put("wearCount", item.wearCount)
                        })
                    }
                }
                val payload = JSONObject()
                    .put("message", userText)
                    .put("wardrobe", wardrobe)
                    .put("mode", "fashion_stylist")
                    .toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(endpoint)
                    .post(payload)
                    .build()
                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@runCatching null
                    val body = response.body?.string().orEmpty()
                    val json = JSONObject(body)
                    json.optString("reply")
                        .ifBlank { json.optString("output_text") }
                        .ifBlank { null }
                }
            }.getOrNull()
        }
    }

    private fun buildAdaptiveFallback(query: String): String {
        val intent = when {
            listOf("job", "interview", "meeting", "boss", "presentation").any { it in query } -> {
                "For a sharp impression: dark fitted bottoms, a clean light top, structured layer, polished shoes, and one quiet accessory."
            }
            listOf("date", "dinner", "party", "birthday").any { it in query } -> {
                "For a social look: one standout piece, dark clean base, fresh shoes, and a small accessory. Keep perfume/light grooming controlled."
            }
            listOf("college", "class", "university", "campus").any { it in query } -> {
                "For campus: breathable top, denim/chinos, comfortable sneakers, and one layer you can remove if it gets hot."
            }
            listOf("eid", "wedding", "mehndi", "nikkah", "barat").any { it in query } -> {
                "For festive wear: choose one premium fabric or color, clean footwear, and minimal accessories so the outfit looks expensive."
            }
            else -> {
                val variants = listOf(
                    "Use one hero item, one neutral base, clean shoes, and one small accessory. Share the event and I will narrow it down.",
                    "Start with clean shoes, then pick a top that matches the weather. Keep colors to two main tones.",
                    "A safe polished formula is black or denim base, light top, sharp footwear, and one orange/dark accent."
                )
                variants[kotlin.math.abs(query.hashCode()) % variants.size]
            }
        }
        return intent
    }

    private fun detectEvent(query: String): String {
        return when {
            listOf("office", "work", "interview", "meeting", "presentation").any { it in query } -> "work"
            listOf("wedding", "eid", "festive", "mehndi", "barat", "nikkah").any { it in query } -> "wedding"
            listOf("party", "date", "dinner", "birthday").any { it in query } -> "party"
            listOf("college", "university", "class", "school", "campus").any { it in query } -> "college"
            listOf("gym", "sport", "running", "walk").any { it in query } -> "sport"
            else -> "daily"
        }
    }

    private fun detectColor(query: String): String? {
        return listOf("black", "white", "orange", "blue", "red", "green", "grey", "gray", "brown", "beige", "pink")
            .firstOrNull { it in query }
            ?.let { if (it == "gray") "grey" else it }
    }

    private fun detectWeather(query: String): String {
        return when {
            listOf("rain", "rainy", "barish").any { it in query } -> "rain"
            listOf("cold", "winter", "sardi").any { it in query } -> "cold"
            listOf("hot", "summer", "garmi").any { it in query } -> "hot"
            else -> "normal"
        }
    }

    private fun buildOutfitFormula(event: String, color: String?, weather: String): String {
        val colorLine = color?.let { "Use $it as the main color and keep the rest neutral." }
            ?: "Use one main color and keep the rest neutral."
        val base = when (event) {
            "work" -> "Wear a structured shirt or clean polo, tailored trousers, polished shoes, and a minimal watch."
            "wedding" -> "Wear a sharp formal/ethnic piece, clean shoes, and one premium accessory. Avoid casual sneakers unless the event is relaxed."
            "party" -> "Wear one standout top or jacket, dark fitted bottoms, clean shoes, and one accessory."
            "college" -> "Wear a breathable tee or casual shirt, denim or chinos, comfortable sneakers, and a light layer if needed."
            "sport" -> "Wear breathable activewear, supportive shoes, and avoid heavy accessories."
            else -> "Wear a clean top, fitted bottoms, matching shoes, and one small accessory."
        }
        val weatherLine = when (weather) {
            "rain" -> "For rain, choose darker bottoms and shoes that can handle water."
            "cold" -> "For cold weather, add a jacket or knit layer."
            "hot" -> "For heat, choose cotton or breathable fabric."
            else -> "Keep the fit clean and comfortable."
        }
        return "$base $colorLine $weatherLine"
    }

    private fun buildGapReply(items: List<ClothingItem>): String {
        val hasFootwear = items.any { it.category.contains("footwear", true) }
        val hasAccessory = items.any { it.category.contains("accessor", true) }
        val hasFormal = items.any { it.occasion.contains("formal", true) || it.occasion.contains("professional", true) }
        val gaps = buildList {
            if (!hasFootwear) add("clean versatile shoes")
            if (!hasAccessory) add("one simple accessory")
            if (!hasFormal) add("one formal/work piece")
        }
        return if (gaps.isEmpty()) {
            "Your wardrobe base is balanced. Next smart buy: one high-quality neutral piece that matches at least three items you already own."
        } else {
            "Shopping gaps I see: ${gaps.joinToString(", ")}. Buy neutral colors first so each new item works with more outfits."
        }
    }

    private suspend fun appendAssistantMessage(userId: String, text: String) {
        val aiMsg = ChatMessage(
            id = (System.currentTimeMillis() + 1).toString(),
            role = "assistant",
            content = text
        )

        _messages.value = _messages.value + aiMsg
        chatRepository.saveMessage(userId, aiMsg)
    }

    fun clearError() {
        _errorState.value = null
    }
}
