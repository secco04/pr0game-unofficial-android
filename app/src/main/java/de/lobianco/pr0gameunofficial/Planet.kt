package de.lobianco.pr0gameunofficial

data class Planet(
    val id: String,
    val name: String,
    val coordinates: String
) {
    fun getUrl(page: String = "overview"): String {
        return "${Config.BASE_URL}/game.php?page=$page&cp=$id"
    }
    
    fun getCoordinatesString(): String {
        return "[$coordinates]"
    }
}