package com.intentpay.categorization

import com.intentpay.domain.models.Category

object KeywordRules {
    private val RULES: Map<List<String>, Category> = mapOf(
        listOf("zomato", "swiggy", "dominos", "pizza", "burger", "food",
            "restaurant", "cafe", "bakery", "kitchen", "biryani",
            "mcdonalds", "kfc", "starbucks", "chai") to Category.FOOD,

        listOf("uber", "ola", "rapido", "irctc", "redbus", "metro",
            "petrol", "diesel", "fuel", "parking", "toll",
            "makemytrip", "goibibo", "cleartrip") to Category.TRAVEL,

        listOf("jio", "airtel", "vi", "bsnl", "broadband", "wifi",
            "electricity", "water", "gas", "rent", "insurance",
            "emi", "loan") to Category.BILLS,

        listOf("amazon", "flipkart", "myntra", "ajio", "nykaa",
            "meesho", "shopping", "mall") to Category.SHOPPING,

        listOf("netflix", "hotstar", "spotify", "prime", "youtube",
            "movie", "cinema", "pvr", "inox", "game") to Category.ENTERTAINMENT,

        listOf("apollo", "pharmeasy", "1mg", "netmeds", "hospital",
            "doctor", "clinic", "medical", "lab", "diagnostic") to Category.HEALTH,

        listOf("udemy", "coursera", "book", "library", "tuition",
            "college", "school", "exam", "coaching") to Category.STUDY,

        listOf("gym", "salon", "spa", "parlour", "grooming",
            "luxury", "jewel", "watch", "perfume") to Category.LUXURY
    )

    fun match(normalizedMerchant: String): Category? {
        val lowercased = normalizedMerchant.lowercase()
        for ((keywords, category) in RULES) {
            if (keywords.any { lowercased.contains(it) }) {
                return category
            }
        }
        return null
    }
}
