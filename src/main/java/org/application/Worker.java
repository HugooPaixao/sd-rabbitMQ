package org.application;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Map;


// pega uma imagem de task queue aplica o tom acinzentado  e publica o resultado em fanout
public class Worker {
    private  static  final  String TASK_QUEUE_NAME = "task_queue";
    private  static final String EXCHANGE_NAME = "logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(System.getenv().getOrDefault("RABBITMQ_HOST","localhost"));

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // declarando a fila de trabalho
        Map<String, Object> args = Map.of("x-queue-type", "quorum");
        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, args);
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

        // recebe uma mensagem por vez
        channel.basicQos(1);

    }
}
