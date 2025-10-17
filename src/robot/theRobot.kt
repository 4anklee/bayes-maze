package robot

import java.awt.Color
import java.awt.Graphics
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import javax.swing.JComponent
import javax.swing.JFrame
import kotlin.math.sqrt

// This class draws the probability map and value iteration map that you create to the window
// You need only call updateProbs() and updateValues() from your theRobot class to update these maps
class mySmartMap(w: Int, h: Int, wld: World) : JComponent(), KeyListener {
    var currentKey: Int

    var winWidth: Int = w
    var winHeight: Int = h
    var sqrWdth: Double
    var sqrHght: Double
    var gris: Color = Color(170, 170, 170)
    var myWhite: Color = Color(220, 220, 220)
    var mundo: World = wld

    var gameStatus: Int

    var probs: Array<DoubleArray> = Array(mundo.width) { DoubleArray(mundo.height) }
    var vals: Array<DoubleArray> = Array(mundo.width) { DoubleArray(mundo.height) }

    init {
        sqrWdth = w.toDouble() / mundo.width
        sqrHght = h.toDouble() / mundo.height
        currentKey = -1

        addKeyListener(this)

        gameStatus = 0
    }

    override fun addNotify() {
        super.addNotify()
        requestFocus()
    }

    fun setWin() {
        gameStatus = 1
        repaint()
    }

    fun setLoss() {
        gameStatus = 2
        repaint()
    }

    fun updateProbs(_probs: Array<DoubleArray>) {
        for (y in 0 ..< mundo.height) {
            for (x in 0 ..< mundo.width) {
                probs[x][y] = _probs[x][y]
            }
        }

        repaint()
    }

    fun updateValues(_vals: Array<DoubleArray>) {
        for (y in 0 ..< mundo.height) {
            for (x in 0 ..< mundo.width) {
                vals[x][y] = _vals[x][y]
            }
        }

        repaint()
    }

    override fun paint(g: Graphics) {
        paintProbs(g)
        // paintValues(g);
    }

    fun paintProbs(g: Graphics) {
        var maxProbs = 0.0
        var mx = 0
        var my = 0
        for (y in 0 ..< mundo.height) {
            for (x in 0 ..< mundo.width) {
                if (probs[x][y] > maxProbs) {
                    maxProbs = probs[x][y]
                    mx = x
                    my = y
                }
                if (mundo.grid[x][y] == 1) {
                    g.color = Color.black
                    g.fillRect(
                            (x * sqrWdth).toInt(),
                            (y * sqrHght).toInt(),
                            sqrWdth.toInt(),
                            sqrHght.toInt()
                    )
                } else if (mundo.grid[x][y] == 0) {
                    // g.setColor(myWhite);

                    var col = (255 * sqrt(probs[x][y])).toInt()
                    if (col > 255) col = 255
                    g.color = Color(255 - col, 255 - col, 255)
                    g.fillRect(
                            (x * sqrWdth).toInt(),
                            (y * sqrHght).toInt(),
                            sqrWdth.toInt(),
                            sqrHght.toInt()
                    )
                } else if (mundo.grid[x][y] == 2) {
                    g.color = Color.red
                    g.fillRect(
                            (x * sqrWdth).toInt(),
                            (y * sqrHght).toInt(),
                            sqrWdth.toInt(),
                            sqrHght.toInt()
                    )
                } else if (mundo.grid[x][y] == 3) {
                    g.color = Color.green
                    g.fillRect(
                            (x * sqrWdth).toInt(),
                            (y * sqrHght).toInt(),
                            sqrWdth.toInt(),
                            sqrHght.toInt()
                    )
                }
            }
            if (y != 0) {
                g.color = gris
                g.drawLine(0, (y * sqrHght).toInt(), winWidth, (y * sqrHght).toInt())
            }
        }
        for (x in 0 ..< mundo.width) {
            g.color = gris
            g.drawLine((x * sqrWdth).toInt(), 0, (x * sqrWdth).toInt(), winHeight)
        }

        // System.out.println("repaint maxProb: " + maxProbs + "; " + mx + ", " + my);
        g.color = Color.green
        g.drawOval(
                (mx * sqrWdth).toInt() + 1,
                (my * sqrHght).toInt() + 1,
                (sqrWdth - 1.4).toInt(),
                (sqrHght - 1.4).toInt()
        )

        if (gameStatus == 1) {
            g.color = Color.green
            g.drawString("You Won!", 8, 25)
        } else if (gameStatus == 2) {
            g.color = Color.red
            g.drawString("You're a Loser!", 8, 25)
        }
    }

    fun paintValues(g: Graphics) {
        var maxVal = -99999.0
        var minVal = 99999.0
        val mx = 0
        val my = 0

        for (y in 0 ..< mundo.height) {
            for (x in 0 ..< mundo.width) {
                if (mundo.grid[x][y] != 0) continue

                if (vals[x][y] > maxVal) maxVal = vals[x][y]
                if (vals[x][y] < minVal) minVal = vals[x][y]
            }
        }
        if (minVal == maxVal) {
            maxVal = minVal + 1
        }

        val offset = winWidth + 20
        for (y in 0 ..< mundo.height) {
            for (x in 0 ..< mundo.width) {
                if (mundo.grid[x][y] == 1) {
                    g.color = Color.black
                    g.fillRect(
                            (x * sqrWdth).toInt() + offset,
                            (y * sqrHght).toInt(),
                            sqrWdth.toInt(),
                            sqrHght.toInt()
                    )
                } else if (mundo.grid[x][y] == 0) {
                    // g.setColor(myWhite);

                    // int col = (int)(255 * Math.sqrt((vals[x][y]-minVal)/(maxVal-minVal)));

                    var col = (255 * (vals[x][y] - minVal) / (maxVal - minVal)).toInt()
                    if (col > 255) col = 255
                    g.color = Color(255 - col, 255 - col, 255)
                    g.fillRect(
                            (x * sqrWdth).toInt() + offset,
                            (y * sqrHght).toInt(),
                            sqrWdth.toInt(),
                            sqrHght.toInt()
                    )
                } else if (mundo.grid[x][y] == 2) {
                    g.color = Color.red
                    g.fillRect(
                            (x * sqrWdth).toInt() + offset,
                            (y * sqrHght).toInt(),
                            sqrWdth.toInt(),
                            sqrHght.toInt()
                    )
                } else if (mundo.grid[x][y] == 3) {
                    g.color = Color.green
                    g.fillRect(
                            (x * sqrWdth).toInt() + offset,
                            (y * sqrHght).toInt(),
                            sqrWdth.toInt(),
                            sqrHght.toInt()
                    )
                }
            }
            if (y != 0) {
                g.color = gris
                g.drawLine(offset, (y * sqrHght).toInt(), winWidth + offset, (y * sqrHght).toInt())
            }
        }
        for (x in 0 ..< mundo.width) {
            g.color = gris
            g.drawLine((x * sqrWdth).toInt() + offset, 0, (x * sqrWdth).toInt() + offset, winHeight)
        }
    }

    override fun keyPressed(e: KeyEvent) {
        // System.out.println("keyPressed");
    }

    override fun keyReleased(e: KeyEvent) {
        // System.out.println("keyReleased");
    }

    override fun keyTyped(e: KeyEvent) {
        val key = e.keyChar

        // System.out.println(key);
        when (key) {
            'i' -> currentKey = NORTH
            ',' -> currentKey = SOUTH
            'j' -> currentKey = WEST
            'l' -> currentKey = EAST
            'k' -> currentKey = STAY
        }
    }

    companion object {
        const val NORTH: Int = 0
        const val SOUTH: Int = 1
        const val EAST: Int = 2
        const val WEST: Int = 3
        const val STAY: Int = 4
    }
}

// This is the main class that you will add to in order to complete the lab
class theRobot(_manual: String, _decisionDelay: Int) : JFrame() {
    var bkgroundColor: Color = Color(230, 230, 230)

    var mundoName: String = ""

    var mundo: World // mundo contains all the information about the world.  See World.java
    var moveProb: Double = 0.0
    var sensorAccuracy: Double =
            0.0 // stores probabilies that the robot moves in the intended direction

    // and the probability that a sonar reading is correct, respectively
    // variables to communicate with the Server via sockets
    var s: Socket? = null
    var sin: BufferedReader? = null
    var sout: PrintWriter? = null

    // variables to store information entered through the command-line about the current scenario
    var isManual: Boolean =
            false // determines whether you (manual) or the AI (automatic) controls the robots
    // movements
    var knownPosition: Boolean = false
    var startX: Int = -1
    var startY: Int = -1
    var decisionDelay: Int = 250

    // store your probability map (for position of the robot in this array (bel)
    var probs: Array<DoubleArray> = arrayOf()

    // store your computed value of being in each state (x, y)
    var Vs: Array<DoubleArray> = arrayOf()

    private val dX = arrayOf(0, 0, 1, -1) // North, South, East, West
    private val dY = arrayOf(-1, 1, 0, 0) // North, South, East, West

    private fun inBounds(x: Int, y: Int) = x >= 0 && x < mundo.width && y >= 0 && y < mundo.height

    private fun isWall(x: Int, y: Int) = !inBounds(x, y) || mundo.grid[x][y] == 1

    // attempts to move from (x,y), stay if there is a wall in the way
    private fun attemptMove(x: Int, y: Int, action: Int): Pair<Int, Int> {
        if (action == STAY) return x to y
        val newX = x + dX[action]
        val newY = y + dY[action]
        return if (isWall(newX, newY)) x to y else newX to newY
    }

    // returns all actions except the given action
    private fun altActionGiven(action: Int): IntArray {
        val allActions = intArrayOf(NORTH, SOUTH, EAST, WEST, STAY)
        return allActions.filter { it != action }.toIntArray()
    }

    // calculates P(sonars | in state (x, y))
    private fun sonarLikelihood(x: Int, y: Int, sonars: String): Double {
        val expected =
                charArrayOf(
                        if (isWall(x, y - 1)) '1' else '0', // North
                        if (isWall(x, y + 1)) '1' else '0', // South
                        if (isWall(x + 1, y)) '1' else '0', // East
                        if (isWall(x - 1, y)) '1' else '0', // West
                )
        var likelihood = 1.0
        for (i in 0 ..< 4) {
            likelihood *= if (expected[i] == sonars[i]) sensorAccuracy else 1 - sensorAccuracy
        }
        return likelihood
    }

    private fun normalize(probs: Array<DoubleArray>) {
        var sum = 0.0
        for (y in 0 ..< mundo.height) {
            for (x in 0 ..< mundo.width) {
                sum += probs[x][y]
            }
        }

        if (sum <= 0.0) {
            var count = 0
            for (y in 0 ..< mundo.height) {
                for (x in 0 ..< mundo.width) {
                    if (mundo.grid[x][y] != 1) count++
                }
            }
            if (count == 0) return
            val u = 1.0 / count
            for (y in 0 ..< mundo.height) {
                for (x in 0 ..< mundo.width) {
                    probs[x][y] = if (mundo.grid[x][y] != 1) u else 0.0
                }
            }

            return
        }

        for (y in 0 ..< mundo.height) {
            for (x in 0 ..< mundo.width) {
                probs[x][y] /= sum
            }
        }
    }

    init {
        // initialize variables as specified from the command-line
        isManual = if (_manual == "automatic") false else true
        decisionDelay = _decisionDelay

        // get a connection to the server and get initial information about the world
        initClient()

        // Read in the world
        mundo = World(mundoName)

        // set up the GUI that displays the information you compute
        val width = 500
        val height = 500
        val bar = 20
        setSize(width, height + bar)
        contentPane.background = bkgroundColor
        defaultCloseOperation = EXIT_ON_CLOSE
        setBounds(0, 0, width, height + bar)
        myMaps = mySmartMap(width, height, mundo)
        contentPane.add(myMaps)

        isVisible = true
        title = "Probability and Value Maps"

        doStuff() // Function to have the robot move about its world until it gets to its goal or
        // falls in a stairwell
    }

    // this function establishes a connection with the server and learns
    //   1 -- which world it is in
    //   2 -- it's transition model (specified by moveProb)
    //   3 -- it's sensor model (specified by sensorAccuracy)
    //   4 -- whether it's initial position is known.  if known, its position is stored in (startX,
    // startY)
    fun initClient() {
        val portNumber = 3333
        val host = "localhost"
        var retryCount = 0
        val maxRetries = 10

        while (retryCount < maxRetries) {
            try {
                s = Socket(host, portNumber)
                sout = PrintWriter(s!!.getOutputStream(), true)
                sin = BufferedReader(InputStreamReader(s!!.getInputStream()))

                mundoName = sin!!.readLine()
                moveProb = sin!!.readLine().toDouble()
                sensorAccuracy = sin!!.readLine().toDouble()
                println("Need to open the mundo: $mundoName")
                println("moveProb: $moveProb")
                println("sensorAccuracy: $sensorAccuracy")

                // find out of the robots position is know
                val _known = sin!!.readLine()
                if (_known == "known") {
                    knownPosition = true
                    startX = sin!!.readLine().toInt()
                    startY = sin!!.readLine().toInt()
                    println("Robot's initial position is known: $startX, $startY")
                } else {
                    println("Robot's initial position is unknown")
                }
                break // Success, exit retry loop
            } catch (e: IOException) {
                retryCount++
                System.err.println("Connection attempt $retryCount failed: " + e.message)
                if (retryCount < maxRetries) {
                    try {
                        Thread.sleep(1000) // Wait 1 second before retry
                    } catch (ex: InterruptedException) {
                        Thread.currentThread().interrupt()
                        break
                    }
                } else {
                    System.err.println("Failed to connect after $maxRetries attempts")
                }
            }
        }
    }

    // function that gets human-specified actions
    fun getHumanAction(): Int {
        println("Reading the action selected by the user")
        while (myMaps.currentKey < 0) {
            try {
                Thread.sleep(50)
            } catch (ex: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        val a = myMaps.currentKey
        myMaps.currentKey = -1

        println("Action: $a")

        return a
    }

    // initializes the probabilities of where the AI is
    fun initializeProbabilities() {
        probs = Array(mundo.width) { DoubleArray(mundo.height) }
        // if the robot's initial position is known, reflect that in the probability map
        if (knownPosition) {
            for (y in 0 ..< mundo.height) {
                for (x in 0 ..< mundo.width) {
                    if ((x == startX) && (y == startY)) probs[x][y] = 1.0 else probs[x][y] = 0.0
                }
            }
        } else { // otherwise, set up a uniform prior over all the positions in the world that are
            // open spaces
            var count = 0

            for (y in 0 ..< mundo.height) {
                for (x in 0 ..< mundo.width) {
                    if (mundo.grid[x][y] == 0) count++
                }
            }

            for (y in 0 ..< mundo.height) {
                for (x in 0 ..< mundo.width) {
                    if (mundo.grid[x][y] == 0) probs[x][y] = 1.0 / count else probs[x][y] = 0.0
                }
            }
        }

        myMaps.updateProbs(probs)
    }

    // TODO (FILTERING ASSIGNMENT): update the probabilities of where the AI thinks it is based on
    // the action selected and the new sonar readings
    //       To do this, you should update the 2D-array "probs"
    // Note: sonars is a bit string with four characters, specifying the sonar reading in the
    // direction of North, South, East, and West
    //       For example, the sonar string 1001, specifies that the sonars found a wall in the North
    // and West directions, but not in the South and East directions
    fun updateProbabilities(action: Int, sonars: String) {

        val predicted = Array(mundo.width) { DoubleArray(mundo.height) }
        val other = altActionGiven(action)
        val pIntended = moveProb
        val pOther = (1 - moveProb) / other.size

        for (y in 0 ..< mundo.height) {
            for (x in 0 ..< mundo.width) {
                val prior = probs[x][y]

                if (prior == 0.0 || mundo.grid[x][y] == 1) continue // wall

                run {
                    val (nx: Int, ny: Int) = attemptMove(x, y, action)
                    predicted[nx][ny] += prior * pIntended
                }

                for (a in other) {
                    run {
                        val (nx: Int, ny: Int) = attemptMove(x, y, a)
                        predicted[nx][ny] += prior * pOther
                    }
                }
            }
        }

        val corrected = Array(mundo.width) { DoubleArray(mundo.height) }
        val bits4 = sonars.take(4)
        for (y in 0 ..< mundo.height) {
            for (x in 0 ..< mundo.width) {
                if (mundo.grid[x][y] == 1) {
                    corrected[x][y] = 0.0
                    continue
                }

                val likelihood = sonarLikelihood(x, y, bits4)
                corrected[x][y] = predicted[x][y] * likelihood
            }
        }

        if (sonars.length == 4) {
            for (y in 0 ..< mundo.height) {
                for (x in 0 ..< mundo.width) {
                    if (mundo.grid[x][y] == 2 || mundo.grid[x][y] == 3) {
                        corrected[x][y] = 0.0
                    }
                }
            }
        }

        normalize(corrected)
        probs = corrected

        myMaps.updateProbs(
                probs
        ) // make sure to call this function after updating your probabilities so that the
        // new probabilities will show up in the probability map on the GUI
    }

    // This is the function to implement to make the robot move using your AI;
    // (FILTERING ASSIGNMENT): You do NOT need to write this function yet; it can remain as is
    fun automaticAction(): Int {
        // TODO (MDP ASSIGNMENT): automatically determine the action the robot should take
        return STAY // default action for now
    }

    fun doStuff() {
        var action: Int

        // valueIteration();  // TODO (MDP ASSIGNMENT): uncomment, implement function, and use to
        // compute your value function
        initializeProbabilities() // Initializes the location (probability) map

        while (true) {
            try {
                action =
                        if (isManual)
                                getHumanAction() // get the action selected by the user (from the
                        // keyboard)
                        else automaticAction() // TODO (MDP ASSIGNMENT): get the action selected
                // by your AI;

                sout!!.println(action) // send the action to the Server

                // get sonar readings after the robot moves
                val sonars = sin!!.readLine()

                // System.out.println("Sonars: " + sonars); // Uncomment if you want to see what the
                // sonar readings are at each time step
                updateProbabilities(
                        action,
                        sonars
                ) // TODO (FILTERING ASSIGNMENT): this function should update the probabilities of
                // where the AI thinks it is

                if (sonars.length > 4
                ) { // check to see if the robot has reached its goal or fallen down stairs
                    if (sonars[4] == 'w') {
                        println("I won!")
                        myMaps.setWin()
                        break
                    } else if (sonars[4] == 'l') {
                        println("I lost!")
                        myMaps.setLoss()
                        break
                    }
                } else {
                    // here, you'll want to update the position probabilities
                    // since you know that the result of the move as that the robot
                    // was not at the goal or in a stairwell
                }
                Thread.sleep(
                        decisionDelay.toLong()
                ) // delay that is useful to see what is happening when the AI selects actions
                // decisionDelay is specified by the send command-line argument, which is given in
                // milliseconds
            } catch (e: IOException) {
                println(e)
            } catch (ex: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }

    companion object {
        // Mapping of actions to integers
        const val NORTH: Int = 0
        const val SOUTH: Int = 1
        const val EAST: Int = 2
        const val WEST: Int = 3
        const val STAY: Int = 4

        var myMaps: mySmartMap =
                mySmartMap(0, 0, World("")) // instance of the class that draw everything to the GUI

        // java theRobot [manual/automatic] [delay]
        @JvmStatic
        fun main(args: Array<String>) {
            val robot = theRobot(args[0], args[1].toInt()) // starts up the robot
        }
    }
}
