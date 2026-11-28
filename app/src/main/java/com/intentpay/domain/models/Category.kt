package com.intentpay.domain.models

// Muted, earthy hues: distinguishable in the spending ring without competing with the sage accent.
enum class Category(
    val displayName: String,
    val emoji: String,
    val colorHex: Long
) {
    FOOD("Food", "🍕", 0xFFC9825B),
    TRAVEL("Travel", "🚗", 0xFF5F9EA0),
    BILLS("Bills", "📄", 0xFFC4A35A),
    STUDY("Study", "📚", 0xFF7C93B8),
    SHOPPING("Shopping", "🛍️", 0xFFB57A8E),
    ENTERTAINMENT("Entertainment", "🎬", 0xFF8E7CB8),
    HEALTH("Health", "💊", 0xFF7FA877),
    LUXURY("Luxury", "✨", 0xFFB59B6A),
    TRANSFER("Transfer", "↔️", 0xFF7C8F9E),
    OTHER("Other", "📌", 0xFF8E8E93),
    UNCATEGORIZED("Uncategorized", "❓", 0xFF636366)
}
