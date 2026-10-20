package com.giammariagristina.sumday

import kotlin.random.Random

data class MagicSquarePuzzle(
    val size: Int,
    val solution: List<List<Int>>,
    val given: List<List<Boolean>>,
    val targetSum: Int
)

data class MagicSquareGameState(
    val puzzle: MagicSquarePuzzle,
    val placed: List<List<Int?>>,
    val bank: List<Int>,
    val selectedBankIndex: Int? = null
) {
    val isFull: Boolean get() = placed.all { row -> row.all { it != null } }
    val isSolved: Boolean get() = isFull && placed == puzzle.solution.map { row -> row.map { it as Int? } }
}

// De la Loubere (Siamese) method, valid for any odd n: place 1 in the middle of
// the top row, then step up-right (wrapping) for each next number, dropping straight
// down instead whenever the up-right cell is already taken.
private fun siamese(n: Int): List<List<Int>> {
    val grid = Array(n) { IntArray(n) }
    var row = 0
    var col = n / 2
    for (num in 1..n * n) {
        grid[row][col] = num
        val upRow = (row - 1 + n) % n
        val rightCol = (col + 1) % n
        if (grid[upRow][rightCol] != 0) {
            row = (row + 1) % n
        } else {
            row = upRow
            col = rightCol
        }
    }
    return grid.map { it.toList() }
}

private val BASE_3X3 = siamese(3)

private fun rotate(grid: List<List<Int>>): List<List<Int>> {
    val n = grid.size
    return List(n) { r -> List(n) { c -> grid[n - 1 - c][r] } }
}

private fun reflect(grid: List<List<Int>>): List<List<Int>> = grid.map { it.reversed() }

private fun randomTransform(grid: List<List<Int>>, random: Random = Random): List<List<Int>> {
    var result = grid
    repeat(random.nextInt(4)) { result = rotate(result) }
    if (random.nextBoolean()) result = reflect(result)
    return result
}

private fun blanksFor(size: Int, blankCount: Int, random: Random = Random): Set<Pair<Int, Int>> {
    val allPositions = (0 until size).flatMap { r -> (0 until size).map { c -> r to c } }
    return allPositions.shuffled(random).take(blankCount).toSet()
}

fun generateMathSosPuzzle(blankCount: Int = 3): MagicSquarePuzzle {
    // A little variety so it isn't always exactly target 15, but kept small (each
    // +1 shift raises every line sum by 3) so the target stays comfortably under 40.
    val shift = Random.nextInt(0, 9)
    val shifted = BASE_3X3.map { row -> row.map { it + shift } }
    val grid = randomTransform(shifted)
    val blanks = blanksFor(3, blankCount)
    val given = List(3) { r -> List(3) { c -> (r to c) !in blanks } }
    return MagicSquarePuzzle(size = 3, solution = grid, given = given, targetSum = grid[0].sum())
}

fun generateHomeworkHeroPuzzle(blankCount: Int = 5): MagicSquarePuzzle {
    // A plain +shift keeps the 9 numbers consecutive, so their last digits are all
    // different and the puzzle can be solved by matching last digits alone, ignoring
    // the tens. Scaling the 1..9 base by an even step instead (still a valid magic
    // square: w = base + step*(v-1) preserves every line sum) spreads the numbers out
    // and forces several pairs to share a last digit, so the tens digit actually matters.
    // base >= 16 keeps the target sum at 70+ regardless of step, so it never
    // overlaps with Math S.O.S.'s (now under-40) range.
    val step = listOf(2, 4, 6, 8).random()
    val base = Random.nextInt(16, 30)
    val shifted = BASE_3X3.map { row -> row.map { base + step * (it - 1) } }
    val grid = randomTransform(shifted)
    val blanks = blanksFor(3, blankCount)
    val given = List(3) { r -> List(3) { c -> (r to c) !in blanks } }
    return MagicSquarePuzzle(size = 3, solution = grid, given = given, targetSum = grid[0].sum())
}

fun generateIveGotThisPuzzle(blankCount: Int = 6, random: Random = Random): MagicSquarePuzzle {
    val shift = random.nextInt(2, 15)
    val base = List(4) { r ->
        List(4) { c ->
            val v = r * 4 + c + 1
            val onDiagonal = (r == c) || (r + c == 3)
            (if (onDiagonal) v else 17 - v) + shift
        }
    }
    val grid = randomTransform(base, random)
    val blanks = blanksFor(4, blankCount, random)
    val given = List(4) { r -> List(4) { c -> (r to c) !in blanks } }
    return MagicSquarePuzzle(size = 4, solution = grid, given = given, targetSum = grid[0].sum())
}

// Daily Quiz: same puzzle for every player on a given day, seeded from the
// calendar date so no server round-trip is needed to keep everyone in sync.
fun generateDailyQuizPuzzle(dateStr: String): MagicSquarePuzzle =
    generateIveGotThisPuzzle(blankCount = 6, random = Random(dateStr.hashCode().toLong()))

fun generateGuruPuzzle(blankCount: Int = 9): MagicSquarePuzzle {
    val shift = Random.nextInt(50, 300)
    val shifted = siamese(5).map { row -> row.map { it + shift } }
    val grid = randomTransform(shifted)
    val blanks = blanksFor(5, blankCount)
    val given = List(5) { r -> List(5) { c -> (r to c) !in blanks } }
    return MagicSquarePuzzle(size = 5, solution = grid, given = given, targetSum = grid[0].sum())
}

fun newGameState(puzzle: MagicSquarePuzzle): MagicSquareGameState {
    val n = puzzle.size
    val placed = List(n) { r -> List(n) { c -> if (puzzle.given[r][c]) puzzle.solution[r][c] else null } }
    val bank = (0 until n).flatMap { r -> (0 until n).map { c -> r to c } }
        .filter { (r, c) -> !puzzle.given[r][c] }
        .map { (r, c) -> puzzle.solution[r][c] }
        .shuffled()
    return MagicSquareGameState(puzzle = puzzle, placed = placed, bank = bank)
}

fun MagicSquareGameState.selectBankIndex(index: Int): MagicSquareGameState =
    copy(selectedBankIndex = if (selectedBankIndex == index) null else index)

fun MagicSquareGameState.placeSelectedAt(row: Int, col: Int): MagicSquareGameState {
    val index = selectedBankIndex ?: return this
    if (puzzle.given[row][col] || placed[row][col] != null) return this
    val value = bank[index]
    val newPlaced = placed.mapIndexed { r, rowList ->
        rowList.mapIndexed { c, v -> if (r == row && c == col) value else v }
    }
    val newBank = bank.toMutableList().apply { removeAt(index) }
    return copy(placed = newPlaced, bank = newBank, selectedBankIndex = null)
}

fun MagicSquareGameState.clearCell(row: Int, col: Int): MagicSquareGameState {
    if (puzzle.given[row][col]) return this
    val value = placed[row][col] ?: return this
    val newPlaced = placed.mapIndexed { r, rowList ->
        rowList.mapIndexed { c, v -> if (r == row && c == col) null else v }
    }
    return copy(placed = newPlaced, bank = bank + value, selectedBankIndex = null)
}

fun MagicSquareGameState.wrongCells(): Set<Pair<Int, Int>> {
    if (!isFull) return emptySet()
    val n = puzzle.size
    val wrong = mutableSetOf<Pair<Int, Int>>()
    for (r in 0 until n) for (c in 0 until n) {
        if (!puzzle.given[r][c] && placed[r][c] != puzzle.solution[r][c]) wrong.add(r to c)
    }
    return wrong
}

fun generatePuzzleFor(id: DifficultyId): MagicSquarePuzzle = when (id) {
    DifficultyId.MATH_SOS -> generateMathSosPuzzle()
    DifficultyId.HOMEWORK_HERO -> generateHomeworkHeroPuzzle()
    DifficultyId.IVE_GOT_THIS -> generateIveGotThisPuzzle()
    DifficultyId.GURU -> generateGuruPuzzle()
}

fun parSecondsFor(id: DifficultyId): Int = when (id) {
    DifficultyId.MATH_SOS -> 30
    DifficultyId.HOMEWORK_HERO -> 120
    DifficultyId.IVE_GOT_THIS -> 180
    DifficultyId.GURU -> 300
}
