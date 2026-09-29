package org.application;

import com.rabbitmq.client.*;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;


public class NewTask {
    private static final String TASK_QUEUE_NAME = "task_queue";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(System.getenv().getOrDefault("RABBITMQ_HOST", "localhost"));

        // abrindo a conexão e o canal e fechando com o uso do try with resources
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            Map<String, Object> args = Map.of("x-queue-type", "quorum");
            channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, args);

            String diretorio = argv.length < 1 ? "imagens" : argv[0];
            File[] files = new File(diretorio).listFiles();

            if(files ==  null) {
                System.out.println("Diretorio não encontrado: " + diretorio);
                return;
            }

            for (File file: files) {
                String fileName = file.getName();

                // guarda o nome original no header da mensagem para que chegue igual no fim
                Map<String, Object> header = new HashMap<String, Object>();
                header.put("filename", fileName);

                AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
                builder.deliveryMode(MessageProperties.PERSISTENT_TEXT_PLAIN.getDeliveryMode());
                builder.priority(MessageProperties.PERSISTENT_TEXT_PLAIN.getPriority());

                builder.headers(header);
                AMQP.BasicProperties theProps = builder.build();

                // o corpa da mensagem serão os bytes da iamgem
                channel.basicPublish("", TASK_QUEUE_NAME, theProps, Files.readAllBytes(file.toPath()));
                System.out.println(" [x] Sent '" + fileName + "'");
            }


        }

    }
}
