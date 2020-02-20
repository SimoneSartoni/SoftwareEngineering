package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdrenalineServer {
        private final ServerSocket serverSocket;
        private final ExecutorService pool;
        private final ServerController serverController;

    public AdrenalineServer(int port, ServerController serverController) throws IOException {
            serverSocket = new ServerSocket(port);
            pool = Executors.newCachedThreadPool();
            System.out.println(">>> Listening on " + port);
            this.serverController = serverController;
        }

    /**
     * this methods starts a infinite loop that accepts Socket connections.
     * After having accepted one connection it starts a thread that is constantly listening for messages in the InputStream
     * @throws IOException
     */
        public void run() throws IOException {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println(">>> New connection " + clientSocket.getRemoteSocketAddress());
                pool.submit(new ClientHandler(clientSocket, serverController));
            }
        }

        public void close() throws IOException {
            serverSocket.close();
            pool.shutdown();
        }
    }

