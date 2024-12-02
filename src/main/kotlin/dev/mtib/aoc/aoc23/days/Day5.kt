package dev.mtib.aoc.aoc23.days

import dev.mtib.aoc.aoc23.util.AbstractDay
import kotlin.math.max
import kotlin.math.min

object Day5 : AbstractDay(5) {

    private data class Seed(val id: Long)
    private data class CategoryName(val name: String);
    private data class GardenerMap(
        val sourceType: CategoryName,
        val destinationType: CategoryName,
        val mapping: Map<LongRange, LongRange>
    ) {
        fun map(source: Long): Long {
            val (fromRange, toRange) = mapping.entries.find { it.key.contains(source) } ?: return source
            val index = source - fromRange.first
            return toRange.first + index
        }
    }

    private val categoryMapStart = Regex("""^(\S+)-to-(\S+) map:""")
    private fun getMaps(input: Array<String>): List<GardenerMap> {
        return input.withIndex().filter { categoryMapStart.matches(it.value) }.map { categoryHead ->
            val groups = categoryMapStart.find(categoryHead.value)!!.groupValues
            val sourceType = CategoryName(groups[1])
            val destinationType = CategoryName(groups[2])
            val startLines = categoryHead.index
            val endLines = input.withIndex().drop(categoryHead.index + 1).find {
                it.value.isBlank()
            }?.index ?: input.size
            val mappedRanges = input.drop(startLines + 1).take(endLines - startLines - 1).map {
                val values = it.split(" ").map { it.toLong() }
                object {
                    val source = values[1]
                    val destination = values[0]
                    val length = values[2]
                }
            }
            val mapping = buildMap<LongRange, LongRange> {
                mappedRanges.forEach {
                    put(it.source..<it.source + it.length, it.destination..<it.destination + it.length)
                }
            }
            GardenerMap(sourceType, destinationType, mapping)
        }
    }

    override fun solvePart1(input: Array<String>): String? {
        val maps = getMaps(input)
        val seeds = input[0].drop(7).split(" ").map { Seed(it.toLong()) }
        val locationsForSeeds = seeds.map { seed ->
            var currentCategory = CategoryName("seed")
            var currentId: Long = seed.id
            while (currentCategory.name != "location") {
                val map = maps.find { it.sourceType == currentCategory }!!
                currentCategory = map.destinationType
                currentId = map.map(currentId)
            }
            currentId
        }.min()
        return locationsForSeeds.toString()
    }

    override fun solvePart2(input: Array<String>): String? {
        val maps = getMaps(input)
        val seeds = input[0].drop(7).split(" ").map { it.toLong() }.chunked(2)
            .map { it[0]..<it[0] + it[1] }
        val locationsForSeeds = seeds.flatMap { seed ->
            var currentCategory = CategoryName("seed")
            var currentRanges = listOf(seed)
            while (currentCategory.name != "location") {
                val gardenerMap = maps.find { it.sourceType == currentCategory }!!
                currentCategory = gardenerMap.destinationType
                val maps = buildMap<LongRange, LongRange> {
                    val gardenerMappings = gardenerMap.mapping.entries.toList().sortedBy { it.key.first }
                    put(0..<gardenerMappings.first().key.first, 0..<gardenerMappings.first().key.first)
                    for (mapIndex in gardenerMappings.indices) {
                        val entry = gardenerMappings[mapIndex]
                        val fromMap = entry.key
                        val toMap = entry.value
                        put(fromMap, toMap)
                        when (val next = gardenerMappings.getOrNull(mapIndex + 1)) {
                            null -> {
                                put(fromMap.last + 1..Long.MAX_VALUE, fromMap.last + 1..Long.MAX_VALUE)
                            }

                            else -> {
                                val fromNext = next.key
                                put(fromMap.last + 1..<fromNext.first, fromMap.last + 1..<fromNext.first)
                            }
                        }
                    }
                }
                currentRanges = maps
                    .flatMap { conversionMap ->
                        currentRanges.mapNotNull {
                            if (it.first > conversionMap.key.last || it.last < conversionMap.key.first) {
                                null
                            } else {
                                val start = max(it.first, conversionMap.key.first)
                                val end = min(it.last, conversionMap.key.last)
                                start..end
                            }
                        }
                            .map { conversionMap.value.first + (it.first - conversionMap.key.first)..conversionMap.value.first + (it.last - conversionMap.key.first) }
                    }
            }
            currentRanges
        }
        return locationsForSeeds.minOf { it.first }.toString()
    }
}
