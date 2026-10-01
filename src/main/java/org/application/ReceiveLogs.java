package org.application;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ReceiveLogs {
    private static final String EXCHANGE_NAME = "logs";

    public static void main(String[] argv) throws Exception {

        final Path diretorio = Paths.get(argv.length < 1 ? "output" : argv[0]);
        Files.createDirectories(diretorio);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(System.getenv().getOrDefault("RABBITMQ_HOST", "localhost"));
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
          String fileName = delivery.getProperties().getHeaders().get("filename").toString();
          Path dest = diretorio.resolve(Paths.get(fileName).getFileName());
          Files.write(dest, delivery.getBody());
          System.out.println(" [x] Received '" + fileName + "'");
        };

        // a mensagem e considerada entregue ao ser recebida
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }
}
