package io.verse.profiling.tracer

import io.tagd.core.Releasable
import io.tagd.langx.ref.WeakReference
import kotlin.jvm.Transient

interface Leaf

interface Fruit

data class Branch(private val tree: Tree<Traceable>, val name: String, val parent: Branch?) {

    @Transient
    var forked: Tree<Traceable>? = null
    private val forkedTreeName = forked?.name

    private val leavesAndFruits: ArrayList<Trace<*>> = arrayListOf()

    fun add(trace: Trace<*>) {
        leavesAndFruits.add(trace)
    }

    fun print() {
        leavesAndFruits.forEach { leafOrFruit ->
            println(leafOrFruit)
        }
    }

    override fun toString(): String {
        return tree.name + " >> " + name
    }
}

data class Tree<T : Traceable>(val traceable: WeakReference<T>) {

    var name: String
        private set

    private val branches: ArrayList<Branch> = arrayListOf()
    var forkedFrom: Branch? = null

    init {
        name = traceable.get()!!.name
    }

    fun add(branch: Branch) {
        branches.add(branch)
    }

    fun isEmpty(): Boolean {
        return branches.isEmpty()
    }

    fun branch(method: String?): Branch? {
        return branches.firstOrNull {
            it.name === method
        }
    }

    fun print() {
        branches.forEach { branch ->
            branch.print()
        }
    }

    override fun toString(): String {
        val branchNames = branches.map { branch ->
            branch.name
        }
        return "Tree - $name ${if (branches.isEmpty()) "" else "with branches $branchNames"}"
    }
}

class TraceForest : Releasable {

    private val trees = HashMap<String, TraceTree>()

    internal fun plant(seed: Traceable): Tree<Traceable> {
        return get(seed)
    }

    private fun get(seed: Traceable): Tree<Traceable> {
        var traceTree = trees[seed.name]
        if (traceTree == null) {
            traceTree = TraceTree()
            trees[seed.name] = traceTree
        }

        return traceTree.get(seed) ?: kotlin.run {
            traceTree.add(seed)
        }
    }

    internal fun branch(branch: String, parent: Branch?, plant: Traceable): Branch {
        val newBranch: Branch
        get(plant).apply {
            newBranch = Branch(this, name = branch, parent)
            add(newBranch)
        }
        return newBranch
    }

    internal fun cut(plant: Traceable) {
        trees[plant.name]?.remove(plant)
    }

    fun print() {
        trees.values.forEach { traceTree ->
            traceTree.print()
        }
    }

    override fun release() {
        trees.clear()
    }

    private class TraceTree {

        private val _trees: ArrayList<Tree<Traceable>> = arrayListOf()

        val trees: List<Tree<Traceable>>
            get() =  _trees

        fun get(traceable: Traceable): Tree<Traceable>? {
            return trees.firstOrNull { tree ->
                tree.traceable.get() == traceable
            }
        }

        fun add(seed: Traceable): Tree<Traceable> {
            return Tree(WeakReference(seed)).apply {
                _trees.add(this)
            }
        }

        fun remove(plant: Traceable) {
            get(plant)?.let {
                _trees.remove(it)
            }
        }

        fun print() {
            trees.forEach { tree ->
                tree.print()
            }
        }
    }
}

interface Foresting : Releasable {

    fun plant(seed: Traceable): Tree<Traceable>

    fun branch(branch: String, parent: Branch? = null, plant: Traceable): Branch

    fun cut(plant: Traceable)
}

