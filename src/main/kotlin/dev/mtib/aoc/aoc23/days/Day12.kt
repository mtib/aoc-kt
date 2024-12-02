package dev.mtib.aoc.aoc23.days

import dev.mtib.aoc.aoc23.util.AbstractDay
import dev.mtib.aoc.util.AocLogger

object Day12 : AbstractDay(12) {
    private val logger = AocLogger.new { }
    private fun String.asStates(): List<Row.State> {
        return this.map {
            when (it) {
                '#' -> Row.State.Damaged
                '.' -> Row.State.Operational
                '?' -> Row.State.Unknown
                else -> throw IllegalArgumentException("Unknown state $it")
            }
        }
    }

    private val cache = mutableMapOf<String, Long>()

    private class Row(val stateList: List<State>, val damageGroups: List<Int>) {
        enum class State {
            Damaged, Unknown, Operational;

            override fun toString(): String {
                return when (this) {
                    Damaged -> "#"
                    Unknown -> "?"
                    Operational -> "."
                }
            }
        }

        override fun toString(): String {
            return stateList.joinToString("") + " " + damageGroups.joinToString(",")
        }

        /**
         * Optimisation ideas:
         * - use sublist
         * - lookup tails
         */
        fun countGroups(
            states: List<State> = stateList,
            groups: List<Int> = damageGroups,
        ): Long {
            val cacheKey = states.joinToString("") + " " + groups.joinToString(",")
            if (cacheKey in cache) {
                return cache[cacheKey]!!
            }
            if (groups.isEmpty()) {
                if (states.all { it != State.Damaged }) {
                    return 1
                }
                return 0
            }
            if (states.isEmpty()) {
                return 0
            }
            val returnVal = when (states[0]) {
                State.Operational -> countGroups(states.subList(1, states.size), groups)
                State.Unknown -> {
                    val nextGroup = groups.first()
                    if (states.size < nextGroup) {
                        0
                    } else {
                        val nextStates = states.subList(0, nextGroup)
                        val followingState = states.getOrNull(nextGroup)
                        if (nextStates.all { it != State.Operational }) {
                            val applyNow = if (followingState != State.Damaged) {
                                countGroups(
                                    states.subList(nextGroup + if (followingState != null) 1 else 0, states.size),
                                    groups.subList(1, groups.size),
                                )
                            } else {
                                0
                            }
                            val applyLater = countGroups(
                                states.subList(1, states.size),
                                groups,
                            )
                            applyNow + applyLater
                        } else {
                            countGroups(
                                states.subList(1, states.size),
                                groups,
                            )
                        }
                    }
                }

                State.Damaged -> {
                    val nextGroup = groups.first()
                    if (states.size < nextGroup) {
                        0
                    } else {
                        val nextStates = states.subList(0, nextGroup)
                        val followingState = states.getOrNull(nextGroup)
                        if (nextStates.all { it != State.Operational } && followingState != State.Damaged && nextStates.size == nextGroup) {
                            countGroups(
                                states.subList(nextGroup + if (followingState != null) 1 else 0, states.size),
                                groups.subList(1, groups.size),
                            )
                        } else {
                            0
                        }
                    }
                }
            }

            cache[cacheKey] = returnVal
            return returnVal
        }

        fun iterateGroups(
            states: List<State> = stateList,
            groups: List<Int> = damageGroups,
            prefix: List<State> = emptyList()
        ): List<List<State>> {
            require(states.size + prefix.size == stateList.size) {
                "Expected ${stateList.size} (${
                    stateList.joinToString(
                        ""
                    )
                }) states, got ${states.size + prefix.size} (${states.joinToString("")} and ${prefix.joinToString("")})"
            }
            if (groups.isEmpty()) {
                if (states.all { it != State.Damaged }) {
                    return listOf(prefix + (1..states.size).map { State.Operational })
                }
                return emptyList()
            }
            if (states.isEmpty()) {
                return emptyList()
            }
            return when (states[0]) {
                State.Operational -> iterateGroups(states.drop(1), groups, prefix + State.Operational)
                State.Unknown -> {
                    val nextGroup = groups.first()
                    val nextStates = states.take(nextGroup)
                    val followingState = states.getOrNull(nextGroup)
                    if (nextStates.all { it != State.Operational } && nextStates.size == nextGroup) {
                        val applyNowPrefix = buildList {
                            addAll(prefix)
                            addAll((1..nextGroup).map { State.Damaged })
                            if (followingState != null) {
                                add(State.Operational)
                            }
                        }
                        val applyNow = if (followingState != State.Damaged) {
                            iterateGroups(
                                states.drop(nextGroup + if (followingState != null) 1 else 0),
                                groups.drop(1),
                                applyNowPrefix
                            )
                        } else {
                            emptyList()
                        }
                        val applyLater = iterateGroups(
                            states.drop(1),
                            groups,
                            prefix + State.Operational
                        )
                        return applyNow + applyLater
                    }
                    return iterateGroups(
                        states.drop(1),
                        groups,
                        prefix + State.Operational
                    )
                }

                State.Damaged -> {
                    val nextGroup = groups.first()
                    val nextStates = states.take(nextGroup)
                    val followingState = states.getOrNull(nextGroup)
                    if (nextStates.all { it != State.Operational } && followingState != State.Damaged && nextStates.size == nextGroup) {
                        val newPrefix = buildList {
                            addAll(prefix)
                            addAll((1..nextGroup).map { State.Damaged })
                            if (followingState != null) {
                                add(State.Operational)
                            }
                        }
                        return iterateGroups(
                            states.drop(nextGroup + if (followingState != null) 1 else 0),
                            groups.drop(1),
                            newPrefix
                        )
                    }
                    return emptyList()
                }
            }
        }

        companion object {
            operator fun invoke(input: String): Row {
                val (statesString, groupString) = input.split(" ")
                val states = statesString.asStates()
                val groups = groupString.split(",").map { it.toInt() }
                return Row(states, groups)
            }
        }
    }

    override fun solvePart1(input: Array<String>): Any? {
        val rows = input.map { Row(it) }

        logger.log {
            data class TestCase(val row: Row, val expected: List<List<Row.State>>)
            listOf(
                TestCase(
                    Row("#???#???#???.#?? 5,3,1,1,1"),
                    listOf(
                        "#####.###.#..#.#".asStates(),
                        "#####.###..#.#.#".asStates(),
                        "#####..###.#.#.#".asStates()
                    )
                ),
                TestCase(
                    Row("????.####.? 1,4"),
                    listOf(
                        "#....####..".asStates(),
                        ".#...####..".asStates(),
                        "..#..####..".asStates(),
                        "...#.####..".asStates()
                    )
                ),
                TestCase(
                    Row("?###???????? 3,2,1"),
                    listOf(
                        ".###.##.#...",
                        ".###.##..#..",
                        ".###.##...#.",
                        ".###.##....#",
                        ".###..##.#..",
                        ".###..##..#.",
                        ".###..##...#",
                        ".###...##.#.",
                        ".###...##..#",
                        ".###....##.#"
                    ).map { it.asStates() }
                ),
                TestCase(
                    Row("...?#?????.?? 5,1"),
                    listOf(
                        "...#####...#.",
                        "....#####..#.",
                        "...#####....#",
                        "....#####...#",
                        "...#####.#...",
                    ).map { it.asStates() }
                )
            ).forEach {
                val groups = it.row.iterateGroups()
                require(groups.size == it.expected.size) { "${it.row} | Expected ${it.expected.size} groups, got ${groups.size}" }
                it.expected.forEach { expectedGroup ->
                    require(groups.contains(expectedGroup)) { "${it.row} | Expected $expectedGroup in $groups" }
                }
                groups.forEach { group ->
                    require(it.expected.contains(group)) { "${it.row} | Expected $group in ${it.expected}" }
                }
                groups.forEach { resultStates ->
                    require(resultStates.size == it.row.stateList.size) { "${it.row} | Expected ${it.row.stateList.size} states, got ${resultStates.size}" }
                }
                groups.forEach { resultStates ->
                    it.row.stateList.forEachIndexed { index, state ->
                        if (resultStates[index] == Row.State.Damaged) {
                            require(
                                state in listOf(
                                    Row.State.Damaged,
                                    Row.State.Unknown
                                )
                            ) { "${it.row} | Expected $state to be Damaged or Unknown" }
                        }
                        if (resultStates[index] == Row.State.Operational) {
                            require(
                                state in listOf(
                                    Row.State.Operational,
                                    Row.State.Unknown
                                )
                            ) { "${it.row} | Expected $state to be Operational or Unknown" }
                        }
                    }
                }
                groups.forEach { resultStates ->
                    val desc = buildList<Int> {
                        var result = resultStates
                        while (result.isNotEmpty()) {
                            result = result.dropWhile { it == Row.State.Operational }
                            val range = result.takeWhile { it == Row.State.Damaged }.size
                            if (range != 0) {
                                add(range)
                            }
                            result = result.drop(range)
                        }
                    }
                    require(desc == it.row.damageGroups) { "${it.row} | Expected ${it.row.damageGroups}, got $desc" }
                }
                it.row.stateList
            }
            "validated test cases #1"
        }

        logger.log {
            rows.forEach { row ->
                val groups = row.iterateGroups()

                require(groups.size.toLong() == row.countGroups()) { "${row} | Expected ${row.countGroups()} groups, got ${groups.size}" }

                groups.forEach { resultStates ->
                    require(resultStates.size == row.stateList.size) { "${row} | Expected ${row.stateList.size} states, got ${resultStates.size}" }
                }
                groups.forEach { resultStates ->
                    row.stateList.forEachIndexed { index, state ->
                        if (resultStates[index] == Row.State.Damaged) {
                            require(
                                state in listOf(
                                    Row.State.Damaged,
                                    Row.State.Unknown
                                )
                            ) { "${row} | Expected $state to be Damaged or Unknown" }
                        }
                        if (resultStates[index] == Row.State.Operational) {
                            require(
                                state in listOf(
                                    Row.State.Operational,
                                    Row.State.Unknown
                                )
                            ) { "${row} | Expected $state to be Operational or Unknown" }
                        }
                    }
                }
                groups.forEach { resultStates ->
                    val desc = buildList<Int> {
                        var result = resultStates
                        while (result.isNotEmpty()) {
                            result = result.dropWhile { it == Row.State.Operational }
                            val range = result.takeWhile { it == Row.State.Damaged }.size
                            if (range != 0) {
                                add(range)
                            }
                            result = result.drop(range)
                        }
                    }
                    require(desc == row.damageGroups) { "${row} | Expected ${row.damageGroups}, got $desc" }
                }
            }
            "validated test cases #2"
        }


        return rows.sumOf { it.countGroups() }
    }

    override fun solvePart2(input: Array<String>): Any? {
        val rows = input.map {
            val (states, report) = it.split(" ")
            Row((1..5).joinToString("?") { states } + " " + (1..5).joinToString(",") { report })
        }
        var finished = 0
        return rows.sumOf { row ->
            row.countGroups().also {
                finished++
                // logger.log { "$finished / ${rows.size} (row=$row value=${it}, cache_size=${cache.size})" }
            }
        }
    }

    override suspend fun setup() {
        cache.clear() // To be fair for timing.
    }
}
