package com.holokenmod.calculation

import com.holokenmod.creation.GridCalculator
import com.holokenmod.grid.Grid
import com.holokenmod.options.GameVariant
import kotlinx.coroutines.runBlocking

class GridCalculationService(
    private var variant: GameVariant
) {
    private val listeners = mutableListOf<GridCalculationListener>()
    private var nextGrid: Grid? = null

    fun addListener(listener: GridCalculationListener) {
        listeners += listener
    }

    fun calculateCurrentAndNextGrids(variant: GameVariant) {
        nextGrid = null
        this.variant = variant
        calculateCurrentGrid()
        calculateNextGrid()
    }

    private fun calculateCurrentGrid() {
        listeners.forEach { it.startingCurrentGridCalculation() }
        val creator = GridCalculator(variant)
        val newGrid = runBlocking {
            creator.calculate()
        }

        listeners.forEach{ it.currentGridCalculated(newGrid) }
    }

    fun calculateNextGrid() {
        listeners.forEach{ it.startingNextGridCalculation() }
        val creator = GridCalculator(variant)
        val grid = runBlocking {
            creator.calculate()
        }
        nextGrid = grid
        listeners.forEach{ it.nextGridCalculated(grid) }
    }

    fun hasCalculatedNextGrid(variantParam: GameVariant): Boolean {
        return nextGrid != null && variantParam == variant
    }

    fun consumeNextGrid(): Grid {
        val grid = nextGrid!!
        nextGrid = null
        return grid
    }

    fun setVariant(variant: GameVariant) {
        this.variant = variant
    }

    fun setNextGrid(grid: Grid) {
        nextGrid = grid
    }
}
