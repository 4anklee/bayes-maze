package robot

import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException

class World(worldFile: String) {
    var width: Int = 0
    var height: Int = 0 // the number of grid squares in the x and y directions
    var grid: Array<IntArray> =
            arrayOf() // will store the map of the world. 0: empty square; 1: wall; 2: stairwell; 3:

    // goal
    init {
        try {
            FileReader("Mundos/$worldFile").use { fileReader ->
                BufferedReader(fileReader).use { bufferedReader ->
                    width = bufferedReader.readLine().toInt()
                    height = bufferedReader.readLine().toInt()

                    // System.out.println("Width: " + width + "; Height = " + height);
                    grid = Array(width) { IntArray(height) }
                    for (y in 0 ..< height) {
                        val line = bufferedReader.readLine()
                        for (x in 0 ..< width) {
                            when (line[x]) {
                                '0' -> grid[x][y] = 0
                                '1' -> grid[x][y] = 1
                                '2' -> grid[x][y] = 2
                                '3' -> grid[x][y] = 3
                                else -> {}
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            println(e)
        }
    }
}
