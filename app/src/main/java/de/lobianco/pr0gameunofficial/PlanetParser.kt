package de.lobianco.pr0gameunofficial

import org.json.JSONArray
import org.json.JSONObject

object PlanetParser {

    /**
     * Parsed HTML und extrahiert Planeten aus dem planetSelector
     * Beispiel HTML:
     * <select id="planetSelector">
     *   <option value="1003146">YranuS [1:268:10]</option>
     * </select>
     */
    fun parseFromHtml(html: String): List<Planet> {
        val planets = mutableListOf<Planet>()

        try {
            // Finde planetSelector
            val selectorStart = html.indexOf("id=\"planetSelector\"")
            if (selectorStart == -1) return emptyList()

            val selectEnd = html.indexOf("</select>", selectorStart)
            if (selectEnd == -1) return emptyList()

            val selectHtml = html.substring(selectorStart, selectEnd)

            // Parse alle <option> Tags
            val optionRegex = """<option value="(\d+)"[^>]*>([^<]+)</option>""".toRegex()
            optionRegex.findAll(selectHtml).forEach { match ->
                val id = match.groupValues[1]
                val fullText = match.groupValues[2].trim()

                // Extrahiere Name und Koordinaten
                // Format: "YranuS [1:268:10]"
                val coordRegex = """\[([^\]]+)\]""".toRegex()
                val coordMatch = coordRegex.find(fullText)

                val coordinates = coordMatch?.groupValues?.get(1) ?: ""
                val name = fullText.replace(coordMatch?.value ?: "", "").trim()

                planets.add(Planet(id, name, coordinates))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return planets
    }

    /**
     * Speichert Planeten als JSON
     */
    fun toJson(planets: List<Planet>): String {
        val jsonArray = JSONArray()
        planets.forEach { planet ->
            val jsonObj = JSONObject()
            jsonObj.put("id", planet.id)
            jsonObj.put("name", planet.name)
            jsonObj.put("coordinates", planet.coordinates)
            jsonArray.put(jsonObj)
        }
        return jsonArray.toString()
    }

    /**
     * Lädt Planeten aus JSON
     */
    fun fromJson(json: String): List<Planet> {
        val planets = mutableListOf<Planet>()
        try {
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                planets.add(
                    Planet(
                        obj.getString("id"),
                        obj.getString("name"),
                        obj.getString("coordinates")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return planets
    }
}