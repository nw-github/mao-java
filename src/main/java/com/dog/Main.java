package com.dog;

import com.dog.net.*;

public class Main
{
    public static void main(String[] args)
    {
        if (args.length > 0) {
            System.out.println("Starting server...");
            try (var server = new Server(5000, 2)) {
                server.run();
            } catch (Exception ex) {
                System.out.printf("Server exception: %s", ex.toString());
            }
        } else {
            System.out.println("Starting client...");
            try (var client = new Client("127.0.0.1", 5000, 5, 2000)) {
                System.out.println("Connected to server!");
                client.run();
            } catch (Exception ex) {
                System.out.printf("Client exception: %s", ex.toString());
            }
        }
    }
}
