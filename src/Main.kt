import robot.theRobot
import server.BayesWorld

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val worldFile = args.getOrNull(0) ?: "mundo_15_15.txt"
        val moveProb = args.getOrNull(1)?.toDoubleOrNull() ?: 1.0
        val sensorAccuracy = args.getOrNull(2)?.toDoubleOrNull() ?: 0.8
        val knownFlag = args.getOrNull(3) ?: "unknown" // "known" or "unknown"

        val robotMode = args.getOrNull(4) ?: "manual" // "manual" or "automatic"
        val robotDelayMs = args.getOrNull(5)?.toIntOrNull() ?: 50

        val serverThread = Thread { BayesWorld(worldFile, moveProb, sensorAccuracy, knownFlag) }
        serverThread.isDaemon = true
        serverThread.start()

        try {
            Thread.sleep(1000) // brief pause to ensure server socket binds before client connects
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
        }

        val clientThread = Thread { theRobot(robotMode, robotDelayMs) }
        clientThread.start()

        try {
            clientThread.join()
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}
