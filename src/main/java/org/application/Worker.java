package org.application;

import com.rabbitmq.client.*;
import com.rabbitmq.client.impl.AMQBasicProperties;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


// pega uma imagem de task queue aplica o tom acinzentado  e publica o resultado em fanout
public class Worker {
    private  static final  String TASK_QUEUE_NAME = "task_queue";
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

        DeliverCallback  deliverCallback = (consumerTag, delivery) -> {
            String fileName = delivery.getProperties().getHeaders().get("filename").toString();
            System.out.println(" [x] Received '" + fileName + "'");

            try {
                byte[] gray = doWork(delivery.getBody(),fileName);

                // repassando o nome do arquivo no header da mensagem convertida
                Map<String, Object> headers = new HashMap<String, Object>();
                headers.put("filename", fileName);

                AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
                builder.deliveryMode(MessageProperties.PERSISTENT_TEXT_PLAIN.getDeliveryMode());
                builder.priority(MessageProperties.PERSISTENT_TEXT_PLAIN.getPriority());
                builder.headers(headers);
                AMQP.BasicProperties properties = builder.build();

                // publicando no exchange fanout
                channel.basicPublish(EXCHANGE_NAME, "", properties, gray);
            } finally {
                System.out.println(" [x] Done");
                // confirma que terminou, so ai o rabbitmq apaga a mensagem da fila
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
        channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> { });

    }

    private  static byte[] doWork(byte[] imagem, String fileName) throws IOException {

        BufferedImage original = ImageIO.read(new ByteArrayInputStream(imagem));

        if (original == null) {
            throw new IOException("Imagem inválida: " + fileName);
        }

        // cria uma imagem vazia do mesmo tamnaho me escala de cinza
        BufferedImage gray = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        // desenhando a imagem original sobre ela pra fazer a conversão de cor
        Graphics2D graphics2D = gray.createGraphics();
        graphics2D.drawImage(original, 0, 0, null);
        graphics2D.dispose();

        String format = fileName.substring(fileName.lastIndexOf('.') +1).toLowerCase();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(gray, format, outputStream);
        return outputStream.toByteArray();
    }
}
