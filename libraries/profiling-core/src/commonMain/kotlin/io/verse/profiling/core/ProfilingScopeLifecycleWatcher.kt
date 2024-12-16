package io.verse.profiling.core

import io.tagd.core.Releasable
import io.tagd.core.ValueProvider
import io.tagd.langx.IllegalAccessException
import io.tagd.langx.collection.concurrent.CopyOnWriteArrayList
import io.verse.storage.core.KeyValueDto

open class ProfilingScopeLifecycleWatcher(
    private val usageProvider: ValueProvider<KeyValueDto>
): Releasable {

    protected open val registry = hashMapOf<ProfilingScope.Rank, OnScopeVisitListener>()

    data class ScopeStats(val sequence: Int, val visits: Int)

    private val tour = CopyOnWriteArrayList<ProfilingScope>()
    private val containerStack = OrderedStack<ProfilingScope>()

    private var sequence: Int = 0

    open fun onVisit(scope: ProfilingScope): ScopeStats {
        tour.add(scope)

        if (scope.rank < ProfilingScope.Rank.Hero) {
            containerStack.push(scope)
        }

        val stats = ScopeStats(sequence = ++sequence, visits = getVisits(scope))
        registry[scope.rank]?.onScopeVisit(scope)
        return stats
    }

    open fun addOnScopeVisitListener(rank: ProfilingScope.Rank, listener: OnScopeVisitListener) {
        registry[rank] = listener
    }

    open fun removeOnScopeVisitListener(rank: ProfilingScope.Rank) {
        registry.remove(rank)
    }

    private fun getVisits(scope: ProfilingScope): Int {
        val visits = usageProvider.value().let { usage ->
            scope.weakProfilable?.get()?.let {
                val key = "${scope.name}-${it.name}-visits"
                val pastVisits = usage.get(key, 0) ?: 0
                val visits = pastVisits + 1
                usage.putInt(key, visits)
                println("usage-debug : $key having visits $visits")
                visits
            } ?: 1
        }
        return visits
    }

    open fun onLeave(scope: ProfilingScope) {
        if (containerStack.isEmpty()) {
            throw IllegalStateException("invalid leave call")
        }

        if (scope.rank < ProfilingScope.Rank.Hero) {
            if (containerStack.top() !== scope) {
                throw IllegalStateException("corrupted leave ${insights(scope)}")
            }
            containerStack.pop()
        }
    }

    protected open fun insights(scope: ProfilingScope): String {
        return "$scope and $containerStack"
    }

    override fun release() {
        tour.clear()
        registry.clear()
        containerStack.release()
        sequence = 0
    }

    fun interface OnScopeVisitListener {

        fun onScopeVisit(scope: ProfilingScope)
    }

    open class OrderedStack<T : Comparable<T>>() : Releasable {

        protected open val stack = ArrayList<T>()
        protected open var currentVisited: T? = null

        open fun push(scope: T) {
            stack.add(scope)

            currentVisited?.let { current ->
                if (current > scope) {
                    throw IllegalAccessException("corrupted visit")
                }
            }
            currentVisited = scope
        }

        open fun isEmpty(): Boolean {
            return stack.isEmpty()
        }

        open fun top(): T {
            return stack[stack.lastIndex]
        }

        open fun pop(): T {
            return stack.removeAt(stack.lastIndex).also {
                currentVisited = stack.lastOrNull()
            }
        }

        fun bottom(): T {
            return stack[0]
        }

        override fun release() {
            stack.clear()
            currentVisited = null
        }

        override fun toString(): String {
            return "$stack, currentVisited=$currentVisited"
        }
    }
}