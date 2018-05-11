package ru.spbau.bachelors2015.roulette.client

import javafx.scene.control.ListView
import ru.spbau.bachelors2015.roulette.protocol.highlevel.ClientCommunicationSocket
import ru.spbau.bachelors2015.roulette.protocol.highlevel.GameStatusRequest

class ListUpdate(
        private val client: ClientCommunicationSocket,
        private val listView: ListView<String>,
        private val sceneFactory: SceneFactory
) : Runnable {
    override fun run() {
        val request = GameStatusRequest()
        client.send(request, GameStatusHandler(listView))
    }
}