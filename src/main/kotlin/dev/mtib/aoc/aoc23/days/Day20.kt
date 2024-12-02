package dev.mtib.aoc.aoc23.days

import dev.mtib.aoc.aoc23.util.AbstractDay
import dev.mtib.aoc.util.AocLogger
import dev.mtib.aoc23.utils.lcm

object Day20 : AbstractDay(20) {
    private enum class Pulse {
        High, Low;
    }

    private val logger = AocLogger.new {}

    private class ExecutionContext {
        var executionCount: Long = 0
        val sentPulses = mutableMapOf(
            Pulse.High to 0L,
            Pulse.Low to 0L,
        )
        val eventQueue = mutableListOf<QueuedSend>()
        fun process() {
            while (eventQueue.isNotEmpty()) {
                val send = eventQueue.removeFirst()
                send.target.receive(send.origin, send.pulse)
                executionCount++
                sentPulses[send.pulse] = sentPulses[send.pulse]!! + 1
            }
        }

        data class QueuedSend(val origin: Module, val target: Module, val pulse: Pulse)

        fun queueSend(origin: Module, target: Module, pulse: Pulse) {
            eventQueue.add(QueuedSend(origin, target, pulse))
        }
    }

    private abstract class Module(val executionContext: ExecutionContext, val name: String = "unknown") {
        val inputs = mutableListOf<Module>()
        val outputs = mutableListOf<Module>()

        val onReceive: MutableList<(origin: Module, pulse: Pulse) -> Unit> = mutableListOf()
        val onSend: MutableList<(target: Module, pulse: Pulse) -> Unit> = mutableListOf()
        fun receive(origin: Module, pulse: Pulse) {
            onReceive.forEach { it(origin, pulse) }
            when (pulse) {
                Pulse.High -> handleHigh(origin)
                Pulse.Low -> handleLow(origin)
            }
        }

        abstract fun handleHigh(origin: Module)
        abstract fun handleLow(origin: Module)
        fun send(pulse: Pulse) {
            outputs.forEach { target ->
                onSend.forEach { callback -> callback(target, pulse) }
                executionContext.queueSend(this, target, pulse)
            }
        }

        companion object {
            fun fromString(string: String, executionContext: ExecutionContext): Pair<Module, List<String>> {
                val (def, targetDef) = string.split(" -> ")
                val name = if (def == "broadcaster") def else def.substring(1)
                val type = def[0]
                val targets = targetDef.split(", ")
                return (when (type) {
                    '%' -> FlipFlop(executionContext, name)
                    '&' -> Conjunction(executionContext, name)
                    'b' -> Broadcaster(executionContext, name)
                    else -> throw IllegalArgumentException()
                }) to targets
            }

            fun parseFromInput(input: Array<String>, executionContext: ExecutionContext): List<Module> {
                val modules =
                    input.map {
                        fromString(
                            it,
                            executionContext
                        )
                    } + (Button(executionContext) to listOf("broadcaster")) + (Nop(
                        executionContext,
                        "output"
                    ) to emptyList())

                val fillerModules = mutableListOf<Nop>()
                modules.forEach { (module, targets) ->
                    targets.forEach { target ->
                        val targetPair = modules.find { it.first.name == target }
                        val targetModule = targetPair?.first
                            ?: Nop(executionContext, target).also {
                                fillerModules.add(it)
                            }
                        module.outputs.add(targetModule)
                        targetModule.inputs.add(module)
                    }
                }

                return modules.map { it.first }.toList() + fillerModules
            }
        }
    }

    private class FlipFlop(executionContext: ExecutionContext, name: String) : Module(executionContext, name) {
        var state = false
        override fun handleHigh(origin: Module) {
        }

        override fun handleLow(origin: Module) {
            state = !state
            send(if (state) Pulse.High else Pulse.Low)
        }
    }

    private class Nop(executionContext: ExecutionContext, name: String) : Module(executionContext, name) {
        override fun handleHigh(origin: Module) {
        }

        override fun handleLow(origin: Module) {
        }
    }

    private class Conjunction(executionContext: ExecutionContext, name: String) : Module(executionContext, name) {
        val map = mutableMapOf<Module, Pulse>()
        override fun handleHigh(origin: Module) {
            map[origin] = Pulse.High
            handle()
        }

        override fun handleLow(origin: Module) {
            map[origin] = Pulse.Low
            handle()
        }

        private fun handle() {
            send(
                if (inputs.all { map[it] == Pulse.High }) {
                    Pulse.Low
                } else {
                    Pulse.High
                }
            )
        }
    }

    private class Broadcaster(executionContext: ExecutionContext, name: String) : Module(executionContext, name) {
        override fun handleHigh(origin: Module) {
            send(Pulse.High)
        }

        override fun handleLow(origin: Module) {
            send(Pulse.Low)
        }
    }

    private class Button(executionContext: ExecutionContext) : Module(executionContext, "button") {
        override fun handleHigh(origin: Module) {
        }

        override fun handleLow(origin: Module) {
        }

        fun push() {
            send(Pulse.Low)
        }
    }

    override fun solvePart1(input: Array<String>): Any? {
        val executionContext = ExecutionContext()
        val modules = Module.parseFromInput(input, executionContext)
        val button = modules.find { it is Button }!! as Button

        logger.log {
            buildString {
                appendLine("flowchart TD")
                modules.forEach { module ->
                    module.outputs.forEach { output ->
                        appendLine("  ${module.name}_${module.javaClass.simpleName} --> ${output.name}_${output.javaClass.simpleName}")
                    }
                }
            }
        }

        for (i in 1..1000) {
            button.push()
            executionContext.process()
        }

        return executionContext.sentPulses.values.reduce(Long::times)
    }

    override fun solvePart2(input: Array<String>): Any? {
        val executionContext = ExecutionContext()
        val modules = Module.parseFromInput(input, executionContext)
        val button = modules.find { it is Button }!! as Button

        val rx = modules.find { it.name == "rx" }!!
        val preRx = modules.find { rx in it.outputs }!!
        val dependencies = modules.filter { preRx in it.outputs }

        logger.log {
            require(dependencies.isNotEmpty()) { "dependencies is empty" }
            "dependencies are met"
        }

        var buttonPresses = 0L
        val periods = mutableMapOf<Module, Long?>()
        dependencies.forEach { dependency ->
            periods[dependency] = null
            dependency.onSend.add { target, pulse ->
                if (pulse == Pulse.High && target == preRx) {
                    periods[dependency] = buttonPresses
                }
            }
        }
        while (periods.values.any { it == null }) {
            buttonPresses++
            button.push()
            executionContext.process()
        }
        val foundPeriods = periods as Map<Module, Long>

        logger.log {
            buildString {
                foundPeriods.forEach { (module, period) ->
                    appendLine("${module.name}: $period")
                }
            }
        }

        val constructiveInterference = foundPeriods.values.fold(1L) { acc, period ->
            lcm(acc, period)
        }

        return constructiveInterference
    }
}
