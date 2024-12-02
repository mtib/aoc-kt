package dev.mtib.aoc.aoc23.days

import dev.mtib.aoc.aoc23.util.AbstractDay
import dev.mtib.aoc.util.AocLogger
import org.jgrapht.alg.StoerWagnerMinimumCut
import org.jgrapht.graph.SimpleGraph

class Day25 : AbstractDay(25) {

    data class Node(val name: String)
    data class Edge private constructor(val from: Node, val to: Node) {
        companion object {
            operator fun invoke(from: Node, to: Node): Edge {
                return if (from.name < to.name) {
                    Edge(from, to)
                } else {
                    Edge(to, from)
                }
            }
        }
    }

    companion object {
        private val logger = AocLogger.new {}
    }

    override fun solvePart1(input: Array<String>): Any? {
        val nodes = mutableSetOf<Node>()
        val edges = mutableSetOf<Edge>()
        for (line in input) {
            val connectedNodeNames = line.split(":", " ").filter { it.isNotBlank() }
            val connectedNodes = connectedNodeNames.map { Node(it) }
            nodes.addAll(connectedNodes)
            edges.addAll(connectedNodes.subList(1, connectedNodes.size).map { node ->
                Edge(node, connectedNodes[0])
            })
        }
        logger.log {
            "nodes: ${nodes.size}, edges: ${edges.size}"
        }

        val graph = SimpleGraph<Node, Edge>(Edge::class.java)
        nodes.forEach { graph.addVertex(it) }
        edges.forEach { graph.addEdge(it.from, it.to, it) }

        val cut = StoerWagnerMinimumCut(graph)
        val compA = cut.minCut().filterNotNull().toSet()
        val compB = nodes - compA
        logger.log {
            "cut: ${cut.minCutWeight()}, compA: ${compA.size}, compB: ${compB.size}"
        }
        return compA.size * compB.size
    }

    override fun solvePart2(input: Array<String>): Any? {
        return 49
    }
}
