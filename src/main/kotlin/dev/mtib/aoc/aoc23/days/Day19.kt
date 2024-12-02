package dev.mtib.aoc.aoc23.days

import dev.mtib.aoc.aoc23.util.AbstractDay
import dev.mtib.aoc.util.AocLogger

class Day19 : AbstractDay(19) {

    companion object {
        private val logger = AocLogger.new {}
    }

    enum class Category(val char: Char) {
        X('x'), M('m'), A('a'), S('s');

        companion object {
            fun fromChar(char: Char): Category {
                return entries.first { it.char == char }
            }
        }
    }

    enum class Operator(val char: Char) {
        GT('>'), LT('<');

        companion object {
            fun fromChar(char: Char): Operator {
                return entries.first { it.char == char }
            }
        }
    }

    data class Condition(val category: Category, val operator: Operator, val value: Int)
    data class RuleName(val name: String) {
        val accepted: Boolean
            get() = name == "A"
        val rejected: Boolean
            get() = name == "R"
        val start: Boolean
            get() = name == "in"
    }

    data class Rule(val name: RuleName, val conditions: List<Pair<Condition, RuleName>>, val fallback: RuleName) {

        val accepted: Boolean
            get() = name.accepted
        val rejected: Boolean
            get() = name.rejected
        val start: Boolean
            get() = name.start
    }

    data class Part(val properties: Map<Category, Long>)

    private fun parseRules(input: Array<String>): Map<RuleName, Rule> {
        return (input.takeWhile { it != "" }.map { ruleString ->
            val (name, conditionString) = ruleString.split("{", "}")
            val ruleName = RuleName(name)
            val conditions = conditionString.split(",").takeWhile { ':' in it }.map { conditionDefinition ->
                val category = Category.fromChar(conditionDefinition[0])
                val operator = Operator.fromChar(conditionDefinition[1])
                val (valueString, targetString) = conditionDefinition.substring(2).split(":")
                Condition(category, operator, valueString.toInt()) to RuleName(targetString)
            }
            Rule(ruleName, conditions, RuleName(conditionString.split(",").last()))
        } + Rule(RuleName("R"), emptyList(), RuleName("R")) + Rule(RuleName("A"), emptyList(), RuleName("A")))
            .associateBy { it.name }
    }

    private fun parseParts(input: Array<String>): List<Part> {
        return input.dropWhile { it != "" }.drop(1).map { partString ->
            val properties = partString.substring(1, partString.length - 1).split(",").associate { propertyString ->
                val (categoryString, valueString) = propertyString.split("=")
                Category.fromChar(categoryString[0]) to valueString.toLong()
            }
            Part(properties)
        }
    }

    override fun solvePart1(input: Array<String>): Any? {
        val rules = parseRules(input)
        val parts = parseParts(input)

        val acceptedParts = parts.filter { part ->
            var currentRule = rules[RuleName("in")]!!

            while (!currentRule.accepted && !currentRule.rejected) {
                currentRule = (currentRule.conditions.find { (condition, _) ->
                    val value = part.properties[condition.category] ?: 0
                    when (condition.operator) {
                        Operator.GT -> value > condition.value
                        Operator.LT -> value < condition.value
                    }
                }?.second ?: currentRule.fallback).let { name ->
                    rules[name]!!
                }
            }

            currentRule.accepted
        }

        return acceptedParts.sumOf { part ->
            Category.entries.sumOf { category ->
                part.properties[category] ?: 0
            }
        }
    }

    override fun solvePart2(input: Array<String>): Any? {
        val rules = parseRules(input)

        data class PartRange(
            val x: LongRange,
            val m: LongRange,
            val a: LongRange,
            val s: LongRange,
        ) {
            constructor(map: Map<Category, LongRange>) : this(
                map[Category.X]!!,
                map[Category.M]!!,
                map[Category.A]!!,
                map[Category.S]!!
            )

            operator fun get(category: Category): LongRange {
                return when (category) {
                    Category.X -> x
                    Category.M -> m
                    Category.A -> a
                    Category.S -> s
                }
            }

            fun toMap(): Map<Category, LongRange> {
                return mapOf(Category.X to x, Category.M to m, Category.A to a, Category.S to s)
            }

            fun count(): Long {
                return x.count().toLong() * m.count() * a.count() * s.count()
            }
        }

        data class State(val rule: Rule, val partRanges: PartRange = PartRange(1..4000L, 1..4000L, 1..4000L, 1..4000L))

        val startState = State(rules[RuleName("A")]!!)
        val visited = mutableListOf<State>()
        val toVisit = mutableListOf(startState)
        while (toVisit.isNotEmpty()) {
            val currentState = toVisit.removeFirst()
            if (currentState in visited) continue

            val parentRules = rules.values.filter { rule ->
                rule.conditions.any { (_, target) -> target == currentState.rule.name } || rule.fallback == currentState.rule.name
            }

            for (parentRule in parentRules) {
                val mapping = currentState.partRanges.toMap().toMutableMap()
                for ((condition, targetRule) in parentRule.conditions) {
                    val range = mapping[condition.category]!!
                    if (targetRule == currentState.rule.name) {
                        val range = mapping[condition.category]!!
                        // Be in those conditions
                        val newRange = when (condition.operator) {
                            Operator.GT -> range.first.coerceAtLeast(condition.value.toLong() + 1)..range.last
                            Operator.LT -> range.first..range.last.coerceAtMost(condition.value.toLong() - 1)
                        }
                        val newMapping = mapping.toMutableMap()
                        newMapping[condition.category] = newRange

                        val newPartRange = PartRange(newMapping)
                        if (newPartRange.count() == 0L) continue
                        toVisit.add(State(parentRule, newPartRange))
                    }
                    // Don't be in those conditions
                    val newRange = when (condition.operator) {
                        Operator.GT -> range.first..range.last.coerceAtMost(condition.value.toLong())
                        Operator.LT -> range.first.coerceAtLeast(condition.value.toLong())..range.last
                    }
                    mapping[condition.category] = newRange
                }
                if (parentRule.fallback == currentState.rule.name) {
                    val newPartRange = PartRange(mapping)
                    if (newPartRange.count() == 0L) continue
                    toVisit.add(State(parentRule, newPartRange))
                }
            }

            visited.add(currentState)
        }
        val firstOrderRanges = visited.filter { state -> state.rule.start }
            .map { it.partRanges }.distinct()

        val ranges = mutableListOf(firstOrderRanges)
        fun intersect(first: LongRange, second: LongRange): LongRange {
            return first.first.coerceAtLeast(second.first)..first.last.coerceAtMost(second.last)
        }

        logger.log {
            "${firstOrderRanges.size} first-order ranges"
        }

        while (ranges.last().isNotEmpty()) {

            val nthOrder = ranges.last()
            val nextOrder = nthOrder.distinct().flatMapIndexed { firstRangeIndex, firstRange ->
                nthOrder.drop(firstRangeIndex + 1).asSequence().map { secondRange ->
                    PartRange(
                        mapOf(
                            Category.X to intersect(firstRange.x, secondRange.x),
                            Category.M to intersect(firstRange.m, secondRange.m),
                            Category.A to intersect(firstRange.a, secondRange.a),
                            Category.S to intersect(firstRange.s, secondRange.s),
                        )
                    )
                }.filter { it.count() > 0 }
            }.distinct()
            logger.log {
                "${nextOrder.size} ${ranges.size + 1}-order ranges"
            }
            ranges.add(nextOrder)
        }
        var multiplier = 1
        val sum = ranges.fold(
            0L
        ) { acc, partRanges ->
            val result = acc + partRanges.sumOf { it.count() } * multiplier
            multiplier = -multiplier
            result
        }
        return sum
    }
}
