package server

import java.awt.Color
import java.awt.Graphics
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import javax.swing.JComponent
import javax.swing.JFrame

class MyCanvas(w: Int, h: Int, wld: World, _x: Int, _y: Int) : JComponent() {
    var winWidth: Int = w
    var winHeight: Int = h
    var sqrWdth: Double
    var sqrHght: Double
    var gris: Color = Color(170, 170, 170)
    var myWhite: Color = Color(220, 220, 220)

    var xpos: Int = 0
    var ypos: Int = 0

    var mundo: World = wld

    init {
        updatePosition(_x, _y)

        sqrWdth = w.toDouble() / mundo.width
        sqrHght = h.toDouble() / mundo.height
    }

    fun updatePosition(_x: Int, _y: Int) {
        xpos = _x
        ypos = _y

        repaint()
    }

    override fun paint(g: Graphics) {
        for (y in 0 ..< mundo.height) {
            for (x in 0 ..< mundo.width) {
                if (mundo.grid[x][y] == 1) {
                    g.color = Color.black
                    g.fillRect(
                            (x * sqrWdth).toInt(),
                            (y * sqrHght).toInt(),
                            sqrWdth.toInt(),
                            sqrHght.toInt()
                    )
                } else if (mundo.grid[x][y] == 0) {
                    g.color = myWhite
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

        g.color = Color.blue
        g.fillOval(
                (xpos * sqrWdth).toInt() + 1,
                (ypos * sqrHght).toInt() + 1,
                (sqrWdth - 1.4).toInt(),
                (sqrHght - 1.4).toInt()
        )
    }
}

class BayesWorld(fnombre: String, _moveProb: Double, _sensorAccuracy: Double, _known: String) :
        JFrame() {
    var bkgroundColor: Color = Color(230, 230, 230)
    var mundo: World = World(fnombre)
    var xpos: Int = 0
    var ypos: Int = 0
    var moveProb: Double
    var sensorAccuracy: Double

    var serverSocket: ServerSocket? = null
    var clientSocket: Socket? = null
    var sout: PrintWriter? = null
    var sin: BufferedReader? = null

    var rand: Random = Random()

    init {
        val width = 500
        val height = 500
        moveProb = _moveProb
        sensorAccuracy = _sensorAccuracy

        initRobotPosition()

        val bar = 20
        setSize(width, height + bar)
        contentPane.background = bkgroundColor
        defaultCloseOperation = EXIT_ON_CLOSE
        setBounds(0, 0, width, height + bar)
        canvas = MyCanvas(width, height, mundo, xpos, ypos)
        contentPane.add(canvas)

        isVisible = true
        title = "BayesWorld"

        getConnection(3333, fnombre, _known)
        survive()
    }

    private fun getConnection(port: Int, fnombre: String, _known: String) {
        println("Set up the connection:$port")

        try {
            serverSocket = ServerSocket(port)
            clientSocket = serverSocket!!.accept()
            sout = PrintWriter(clientSocket!!.getOutputStream(), true)
            sin = BufferedReader(InputStreamReader(clientSocket!!.getInputStream()))

            println("Connection established.")

            sout!!.println(fnombre)
            sout!!.println(moveProb)
            sout!!.println(sensorAccuracy)

            if (_known == "known") {
                sout!!.println("known")
                sout!!.println(xpos)
                sout!!.println(ypos)
            } else {
                sout!!.println("unknown")
            }
        } catch (e: IOException) {
            System.err.println("Caught IOException: " + e.message)
        }
    }

    fun initRobotPosition() {
        while (true) {
            // random initial position
            // xpos = rand.nextInt(mundo.width);
            // ypos = rand.nextInt(mundo.height);

            // random initial position in bottom right quadrant

            xpos = rand.nextInt(mundo.width / 2) + (mundo.width / 2)
            ypos = rand.nextInt(mundo.height / 2) + (mundo.height / 2)

            if (mundo.grid[xpos][ypos] == 0) break
        }
    }

    fun moveIt(action: Int) {
        val oldx = xpos
        val oldy = ypos

        when (action) {
            NORTH -> ypos--
            SOUTH -> ypos++
            WEST -> xpos--
            EAST -> xpos++
            4 -> {}
        }

        if (mundo.grid[xpos][ypos] == 1) {
            xpos = oldx
            ypos = oldy
        }
        canvas.updatePosition(xpos, ypos)
    }

    fun moveRobot(action: Int) {
        val value = rand.nextInt(1001) / 1001.0

        if (value <= moveProb) moveIt(action)
        else { // pick a different move randomly
            var other = rand.nextInt(5)
            while (other == action) other = rand.nextInt(5)
            moveIt(other)
        }
    }

    val sonarReadings: String
        // returns a strong with a char specifying north south east west; 1 = wall; 0 =
        get() {
            var value = rand.nextInt(1001) / 1001.0
            var reading = ""
            // north
            reading +=
                    if (mundo.grid[xpos][ypos - 1] == 1) { // it is a wall
                        if (value <= sensorAccuracy) "1" else "0"
                    } else { // it is not a wall
                        if (value <= sensorAccuracy) "0" else "1"
                    }
            // south
            value = rand.nextInt(1001) / 1001.0
            reading +=
                    if (mundo.grid[xpos][ypos + 1] == 1) { // it is a wall
                        if (value <= sensorAccuracy) "1" else "0"
                    } else { // it is not a wall
                        if (value <= sensorAccuracy) "0" else "1"
                    }
            // east
            value = rand.nextInt(1001) / 1001.0
            reading +=
                    if (mundo.grid[xpos + 1][ypos] == 1) { // it is a wall
                        if (value <= sensorAccuracy) "1" else "0"
                    } else { // it is not a wall
                        if (value <= sensorAccuracy) "0" else "1"
                    }
            // west
            value = rand.nextInt(1001) / 1001.0
            reading +=
                    if (mundo.grid[xpos - 1][ypos] == 1) { // it is a wall
                        if (value <= sensorAccuracy) "1" else "0"
                    } else { // it is not a wall
                        if (value <= sensorAccuracy) "0" else "1"
                    }

            return reading
        }

    fun survive() {
        var action: Int
        var theEnd = false
        var numMoves = 0

        while (true) {
            try {
                action = sin!!.readLine().toInt()
                println("Move the robot: $action")
                moveRobot(action)

                var sonars = sonarReadings
                println(sonars)
                if (mundo.grid[xpos][ypos] == 3) {
                    println("Winner")
                    // sout.println("win");
                    sonars += "winner"
                    theEnd = true
                } else if (mundo.grid[xpos][ypos] == 2) {
                    println("Loser")
                    // sout.println("lose");
                    sonars += "loser"
                    theEnd = true
                }
                sout!!.println(sonars)

                numMoves++

                if (theEnd) break
            } catch (e: IOException) {
                println(e)
            }
        }
        println("It took $numMoves moves.")
    }

    companion object {
        const val NORTH: Int = 0
        const val SOUTH: Int = 1
        const val EAST: Int = 2
        const val WEST: Int = 3
        const val STAY: Int = 4

        var canvas: MyCanvas = MyCanvas(0, 0, World(""), 0, 0)

        @JvmStatic
        fun main(args: Array<String>) {
            val bw = BayesWorld(args[0], args[1].toDouble(), args[2].toDouble(), args[3])
        }
    }
}
