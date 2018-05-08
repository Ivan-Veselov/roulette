package ru.spbau.bachelors2015.roulette.server

import java.net.ServerSocket

fun main(args: Array<String>) {
    val port = args[0].toInt()
    val serverSocket = ServerSocket(port)
    val casinoModel = CasinoModel()

    while (true) {
        val socket = serverSocket.accept()
        val clientThread = Thread(ClientHandler(socket, casinoModel))
        clientThread.run()
    }
}
