package com.intentpay.domain.models

enum class Category(
    val displayName: String,
    val emoji: String,
    val colorHex: Long
) {
    FOOD("Food", "🍕", 0xFFFF6B35),
    TRAVEL("Travel", "🚗", 0xFF4ECDC4),
    BILLS("Bills", "📄", 0xFFFFE66D),
    STUDY("Study", "📚", 0xFF95E1D3),
    SHOPPING("Shopping", "🛍️", 0xFFFF85A1),
    ENTERTAINMENT("Entertainment", "🎬", 0xFFB388FF),
    HEALTH("Health", "💊", 0xFF69F0AE),
    LUXURY("Luxury", "✨", 0xFFFFD700),
    TRANSFER("Transfer", "↔️", 0xFF90CAF9),
    OTHER("Other", "📌", 0xFFBDBDBD),
    UNCATEGORIZED("Uncategorized", "❓", 0xFF757575)
}
