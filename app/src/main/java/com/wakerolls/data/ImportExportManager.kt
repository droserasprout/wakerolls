package com.wakerolls.data

import com.wakerolls.data.repository.ItemRepository
import com.wakerolls.data.repository.ScenarioRepository
import com.wakerolls.domain.model.Item
import com.wakerolls.domain.model.Rarity
import com.wakerolls.domain.model.Scenario
import com.wakerolls.domain.model.ScenarioSlot
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportExportManager @Inject constructor(
    private val itemRepository: ItemRepository,
    private val scenarioRepository: ScenarioRepository,
) {
    suspend fun exportToJson(): String {
        val root = JSONObject()

        val itemsArray = JSONArray()
        for (item in itemRepository.getAll()) {
            itemsArray.put(JSONObject().apply {
                put("name", item.name)
                put("category", item.category)
                put("rarity", item.rarity.name)
                put("enabled", item.enabled)
            })
        }
        root.put("items", itemsArray)

        val scenariosArray = JSONArray()
        for (scenario in scenarioRepository.getAll()) {
            val slotsArray = JSONArray()
            for (slot in scenario.slots) {
                slotsArray.put(JSONObject().apply {
                    put("category", slot.category)
                    put("count", slot.count)
                })
            }
            scenariosArray.put(JSONObject().apply {
                put("name", scenario.name)
                put("slots", slotsArray)
            })
        }
        root.put("scenarios", scenariosArray)

        return root.toString(2)
    }

    suspend fun importFromJson(json: String) {
        val root = JSONObject(json)

        if (root.has("items")) {
            val items = mutableListOf<Item>()
            val arr = root.getJSONArray("items")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                items.add(Item(
                    name = obj.getString("name"),
                    category = obj.getString("category"),
                    rarity = Rarity.valueOf(obj.getString("rarity")),
                    enabled = obj.optBoolean("enabled", true),
                ))
            }
            itemRepository.deleteAll()
            itemRepository.insertAll(items)
        }

        if (root.has("scenarios")) {
            scenarioRepository.deleteAll()
            val arr = root.getJSONArray("scenarios")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val slotsArr = obj.getJSONArray("slots")
                val slots = mutableListOf<ScenarioSlot>()
                for (j in 0 until slotsArr.length()) {
                    val slotObj = slotsArr.getJSONObject(j)
                    slots.add(ScenarioSlot(
                        category = slotObj.getString("category"),
                        count = slotObj.optInt("count", 1),
                    ))
                }
                scenarioRepository.save(Scenario(name = obj.getString("name"), slots = slots))
            }
        }
    }
}
