package com.holokenmod.creation.dlx

import com.holokenmod.grid.Grid
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class MathDokuDLX(
    private val grid: Grid
) {
    private val dlxGrid = DLXGrid(grid)

    private var numberOfCages = 0

    private fun initialize() {
        // Number of columns = number of constraints =
        // 		BOARD * BOARD (for columns) +
        // 		BOARD * BOARD (for rows)	+
        // 		Num cages (each cage has to be filled once and only once)
        // Number of rows = number of "moves" =
        // 		Sum of all the possible cage combinations
        // Number of nodes = sum of each move:
        //      num_cells column constraints +
        //      num_cells row constraints +
        //      1 (cage constraint)

        val extraRectingularCages = if (grid.gridSize.isSquare) {
            0
        } else {
            grid.gridSize.largestSide()
        }

        numberOfCages = dlxGrid.creators.size + extraRectingularCages
    }

    private fun logConstraints(constraints: List<BooleanArray>) {
        if (logger.isDebugEnabled()) {
            logConstraintsHeader()

            for (constraint in constraints) {
                val constraintInfo = StringBuilder()

                constraint.forEach {
                    constraintInfo.append(
                        if (it) {
                            "*"
                        } else {
                            "-"
                        }.padEnd(4)
                    )
                }

                logger.debug { constraintInfo.toString() }
            }
        }
    }

    private fun logConstraintsHeader() {
        val headerCellId = StringBuilder()
        val headerValue = StringBuilder()

        for (value in dlxGrid.possibleDigits) {
            for (column in 0 until grid.gridSize.width) {
                headerCellId.append("c$column".padEnd(4))
                headerValue.append("$value".padEnd(4))
            }
        }

        for (value in dlxGrid.possibleDigits) {
            for (row in 0 until grid.gridSize.height) {
                headerCellId.append("r$row".padEnd(4))
                headerValue.append("$value".padEnd(4))
            }
        }

        for (cage in 0 until numberOfCages) {
            headerValue.append("$cage".padEnd(4))
        }

        logger.debug { headerValue.toString() }
        logger.debug { headerCellId.toString() }
    }

    fun solve(type: DLX.SolveType): Int {
        initialize()

        val constraintCageCalculator = ConstraintsFromGridCagesCalculator(dlxGrid, numberOfCages)
        val constraintRectangularCalculator = ConstraintsFromRectangularGridsCalculator(dlxGrid, numberOfCages)

        val constraints = constraintCageCalculator.calculateConstraints() +
            constraintRectangularCalculator.calculateConstraints()

        logConstraints(constraints)

        val numberOfNodes = constraintCageCalculator.numberOfNodes() + constraintRectangularCalculator.numberOfNodes()

        val dlx = DLX(
            dlxGrid.possibleDigits.size * (grid.gridSize.width + grid.gridSize.height) + numberOfCages,
            numberOfNodes
        )

        for ((currentCombination, constraint) in constraints.withIndex()) {
            for (constraintIndex in constraint.indices) {
                if (constraint[constraintIndex]) {
                    dlx.addNode(constraintIndex, currentCombination)
                }
            }
        }

        return dlx.solve(type)
    }
}
