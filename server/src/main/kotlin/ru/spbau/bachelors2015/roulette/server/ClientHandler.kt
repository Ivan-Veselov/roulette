package ru.spbau.bachelors2015.roulette.server

import ru.spbau.bachelors2015.roulette.protocol.highlevel.*
import java.net.Socket

class ClientHandler(
    private val clientSocket: Socket,
    private val casinoModel: CasinoModel
): Runnable {
    private var shouldStop = false

    private var handler: RequestHandler = RegistrationHandler()

    override fun run() {
        val communicator = ServerCommunicationSocket(clientSocket)

        while (!shouldStop) {
            communicator.handleNextRequest(handler)
        }
    }

    private inner class RegistrationHandler: RequestHandler {
        override fun handle(request: RegistrationRequest): Response {
            try {
                when (request.clientRole) {
                    ClientRole.PLAYER -> {
                        casinoModel.registerPlayer(request.nickname)
                        TODO("need to create a handler")
                    }

                    ClientRole.CROUPIER -> {
                        handler = CroupierHandler(casinoModel.registerCroupier(request.nickname))
                    }
                }
            } catch (_: NicknameIsTakenException) {
                return ErrorResponse("Nickname is already taken")
            } catch (_: CroupierIsAlreadyRegisteredException) {
                return ErrorResponse("There is already a croupier in the game")
            }

            return OkResponse()
        }

        override fun handle(request: GameStartRequest): Response {
            return ErrorResponse(unauthorizedRequestErrorMessage)
        }

        override fun handle(request: GameStatusRequest): Response {
            return ErrorResponse(unauthorizedRequestErrorMessage)
        }

        override fun handle(request: BalanceRequest): Response {
            return ErrorResponse(unauthorizedRequestErrorMessage)
        }

        override fun handle(request: BetRequest): Response {
            return ErrorResponse(unauthorizedRequestErrorMessage)
        }

        override fun handle(request: GameResultsRequest): Response {
            return ErrorResponse(unauthorizedRequestErrorMessage)
        }

        override fun handleSocketClosing() {
            shouldStop = true
        }
    }

    private inner class CroupierHandler(val croupier: CasinoModel.Croupier): RequestHandler {
        override fun handle(request: RegistrationRequest): Response {
            return ErrorResponse(repeatedRegistrationErrorMessage)
        }

        override fun handle(request: GameStartRequest): Response {
            try {
                casinoModel.startNewGame()
            } catch (_: GameIsRunningException) {
                return ErrorResponse("Game is already running")
            }

            return OkResponse()
        }

        override fun handle(request: GameStatusRequest): Response {
            val game = casinoModel.getCurrentGame() ?: return GameStatusNegativeResponse()

            return GameStatusPositiveResponse(
                game.id,
                game.secondsLeft(),
                game.getBets().mapKeys { it.key.nickname }.mapValues { it.value.value }
            )
        }

        override fun handle(request: BalanceRequest): Response {
            return ErrorResponse(unsupportedOperationErrorMessage)
        }

        override fun handle(request: BetRequest): Response {
            return ErrorResponse(unsupportedOperationErrorMessage)
        }

        override fun handle(request: GameResultsRequest): Response {
            val game = casinoModel.getGame(request.gameId) ?:
                return ErrorResponse("No results for game with given id")

            return GameResultsResponse(
                game.value,
                game.getResults().mapKeys { it.key.nickname }
            )
        }

        override fun handleSocketClosing() {
            croupier.destroy()
            shouldStop = true
        }
    }

    private companion object {
        const val unauthorizedRequestErrorMessage = "Unauthorized request"

        const val repeatedRegistrationErrorMessage = "Repeated registration"

        const val unsupportedOperationErrorMessage = "Unsupported operation"
    }
}