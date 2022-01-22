package com.holokenmod.creation;

import com.holokenmod.Grid;
import com.holokenmod.GridCage;
import com.holokenmod.GridCageAction;
import com.holokenmod.GridCell;
import com.holokenmod.RandomSingleton;
import com.holokenmod.options.ApplicationPreferences;
import com.holokenmod.options.DigitSetting;
import com.holokenmod.options.GameVariant;
import com.holokenmod.options.GridCageOperation;
import com.holokenmod.options.SingleCageUsage;

import java.util.ArrayList;
import java.util.Optional;

public class GridCageCreator {
	
	private static final int SINGLE_CELL_CAGE = 0;
	
	// O = Origin (0,0) - must be the upper leftmost cell
	// X = Other cells used in cage
	private static final int[][][] CAGE_COORDS = new int[][][]{
			// O
			{{0, 0}},
			// O
			// X
			{{0, 0}, {0, 1}},
			// OX
			{{0, 0}, {1, 0}},
			// O
			// X
			// X
			{{0, 0}, {0, 1}, {0, 2}},
			// OXX
			{{0, 0}, {1, 0}, {2, 0}},
			// O
			// XX
			{{0, 0}, {0, 1}, {1, 1}},
			// O
			//XX
			{{0, 0}, {0, 1}, {-1, 1}},
			// OX
			//  X
			{{0, 0}, {1, 0}, {1, 1}},
			// OX
			// X
			{{0, 0}, {1, 0}, {0, 1}},
			// OX
			// XX
			//=== 9 ===
			{{0, 0}, {1, 0}, {0, 1}, {1, 1}},
			// OX
			// X
			// X
			{{0, 0}, {1, 0}, {0, 1}, {0, 2}},
			// OX
			//  X
			//  X
			//{{0,0},{1,0},{1,1},{1,2}},
			// O
			// X
			// XX
			//{{0,0},{0,1},{0,2},{1,2}},
			// O
			// X
			//XX
			//=== 11 ===
			{{0, 0}, {0, 1}, {0, 2}, {-1, 2}},
			// OXX
			// X
			{{0, 0}, {1, 0}, {2, 0}, {0, 1}},
			// OXX
			//   X
			{{0, 0}, {1, 0}, {2, 0}, {2, 1}},
			// O
			// XXX
			/*{{0,0},{0,1},{1,1},{2,1}},
			//  O
			//XXX
			{{0,0},{-2,1},{-1,1},{0,1}},
			// O
			// XX
			// X
			{{0,0},{0,1},{0,2},{1,1}},
			// O
			//XX
			// X
			{{0,0},{0,1},{0,2},{-1,1}},
			// OXX
			//  X
			{{0,0},{1,0},{2,0},{1,1}},
			// O
			//XXX
			{{0,0},{-1,1},{0,1},{1,1}},
			// OXXX
			{{0,0},{1,0},{2,0},{3,0}},
			// O
			// X
			// X
			// X
			{{0,0},{0,1},{0,2},{0,3}},
			// O
			// XX
			//  X
			{{0,0},{0,1},{1,1},{1,2}},
			// O
			//XX
			//X
			{{0,0},{0,1},{-1,1},{-1,2}},
			// OX
			//  XX
			{{0,0},{1,0},{1,1},{2,1}},
			// OX
			//XX
			{{0,0},{1,0},{0,1},{-1,1}}*/
	};
	
	private final Grid grid;
	
	public GridCageCreator(Grid grid) {
		this.grid = grid;
	}
	
	public void createCages() {
		final GridCageOperation operationSet = GameVariant.getInstance().getCageOperation();
		boolean restart;
		
		do {
			restart = false;
			
			int cageId = 0;
			
			if (ApplicationPreferences.getInstance()
					.getSingleCageUsage() == SingleCageUsage.FIXED_NUMBER) {
				cageId = createSingleCages();
			}
			
			for (final GridCell cell : grid.getCells()) {
				if (cell.CellInAnyCage()) {
					continue;
				}
				
				final ArrayList<Integer> possible_cages = getValidCages(grid, cell);
				
				final int cage_type;
				
				if (possible_cages.size() == 1) {
					// Only possible cage is a single
					if (ApplicationPreferences.getInstance()
							.getSingleCageUsage() != SingleCageUsage.DYNAMIC) {
						grid.ClearAllCages();
						restart = true;
						break;
					} else {
						cage_type = 0;
					}
				} else {
					cage_type = possible_cages.get(RandomSingleton.getInstance()
							.nextInt(possible_cages.size() - 1) + 1);
				}
				
				final GridCage cage = GridCage.createWithCells(grid, cell, CAGE_COORDS[cage_type]);
				
				calculateCageArithmetic(cage, operationSet);
				cage.setCageId(cageId++);
				grid.addCage(cage);
			}
		} while (restart);
		
		for (final GridCage cage : grid.getCages()) {
			cage.setBorders();
		}
		grid.setCageTexts();
	}
	
	private int createSingleCages() {
		final int singles = (int) (Math.sqrt(grid.getGridSize().getSurfaceArea()) / 2);
		
		final boolean[] RowUsed = new boolean[grid.getGridSize().getHeight()];
		final boolean[] ColUsed = new boolean[grid.getGridSize().getWidth()];
		final boolean[] ValUsed = new boolean[grid.getGridSize().getAmountOfNumbers()];
		
		for (int i = 0; i < singles; i++) {
			GridCell cell;
			int cellIndex;
			do {
				cell = grid.getCell(RandomSingleton.getInstance()
						.nextInt(grid.getGridSize().getSurfaceArea()));
				
				cellIndex = cell.getValue();
				
				if (GameVariant.getInstance()
						.getDigitSetting() == DigitSetting.FIRST_DIGIT_ONE) {
					cellIndex--;
				}
				
			} while (RowUsed[cell.getRow()] || ColUsed[cell.getRow()] || ValUsed[cellIndex]);
			ColUsed[cell.getColumn()] = true;
			RowUsed[cell.getRow()] = true;
			ValUsed[cellIndex] = true;
			final GridCage cage = new GridCage(grid);
			cage.addCell(cell);
			cage.setSingleCellArithmetic();
			cage.setCageId(i);
			grid.addCage(cage);
		}
		return singles;
	}
	
	private ArrayList<Integer> getValidCages(final Grid grid, final GridCell origin) {
		final ArrayList<Integer> valid = new ArrayList<>();
		
		for (int cage_num = 0; cage_num < CAGE_COORDS.length; cage_num++) {
			final int[][] cage_coords = CAGE_COORDS[cage_num];
			
			boolean validCage = true;
			
			for (final int[] cage_coord : cage_coords) {
				final int col = origin.getColumn() + cage_coord[0];
				final int row = origin.getRow() + cage_coord[1];
				final GridCell c = grid.getCellAt(row, col);
				if (c == null || c.CellInAnyCage()) {
					validCage = false;
					break;
				}
			}
			
			if (validCage) {
				valid.add(cage_num);
			}
		}
		
		return valid;
	}
	
	/*
	 * Generates the arithmetic for the cage, semi-randomly.
	 *
	 * - If a cage has 3 or more cells, it can only be an add or multiply.
	 * - else if the cells are evenly divisible, division is used, else
	 *   subtraction.
	 */
	private void calculateCageArithmetic(GridCage cage, final GridCageOperation operationSet) {
		cage.setAction(null);
		if (cage.getCells().size() == 1) {
			cage.setSingleCellArithmetic();
			return;
		}
		
		Optional<GridCageAction> action = decideMultipleOrAddOrOther(cage, operationSet);
		
		if (action.isPresent()) {
			cage.setAction(action.get());
			cage.calculateResultFromAction();
			
			return;
		}
		
		final int cell1Value = cage.getCell(0).getValue();
		final int cell2Value = cage.getCell(1).getValue();
		int higher = cell1Value;
		int lower = cell2Value;
		boolean canDivide = false;
		
		if (cell1Value < cell2Value) {
			higher = cell2Value;
			lower = cell1Value;
		}
		
		if (GameVariant.getInstance()
				.getDigitSetting() == DigitSetting.FIRST_DIGIT_ONE && higher % lower == 0 && operationSet != GridCageOperation.OPERATIONS_ADD_SUB) {
			canDivide = true;
		}
		
		if (GameVariant.getInstance()
				.getDigitSetting() == DigitSetting.FIRST_DIGIT_ZERO && lower > 0 && higher % lower == 0 && operationSet != GridCageOperation.OPERATIONS_ADD_SUB) {
			canDivide = true;
		}
		
		if (canDivide) {
			cage.setResult(higher / lower);
			cage.setAction(GridCageAction.ACTION_DIVIDE);
		} else {
			cage.setResult(higher - lower);
			cage.setAction(GridCageAction.ACTION_SUBTRACT);
		}
	}
	
	private Optional<GridCageAction> decideMultipleOrAddOrOther(GridCage cage, GridCageOperation operationSet) {
		if (operationSet == GridCageOperation.OPERATIONS_MULT) {
			return Optional.of(GridCageAction.ACTION_MULTIPLY);
		}
		
		final double rand = RandomSingleton.getInstance().nextDouble();
		
		double addChance = 0.25;
		double multChance = 0.5;
		
		if (operationSet == GridCageOperation.OPERATIONS_ADD_SUB) {
			if (cage.getCells().size() > 2) {
				addChance = 1.0;
			} else {
				addChance = 0.4;
			}
			multChance = 0.0;
		} else if (cage.getCells().size() > 2
				|| operationSet == GridCageOperation.OPERATIONS_ADD_MULT) { // force + and x only
			addChance = 0.5;
			multChance = 1.0;
		}
		
		if (rand <= addChance) {
			return Optional.of(GridCageAction.ACTION_ADD);
		} else if (rand <= multChance) {
			return Optional.of(GridCageAction.ACTION_MULTIPLY);
		}
		
		return Optional.empty();
	}
	
}
