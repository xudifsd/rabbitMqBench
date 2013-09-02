package org.xudifsd;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.MessageProperties;

import java.util.Map;

import org.xudifsd.Utils;

public class Bench {
	public static void main(String[] argv) throws Exception {
		if (argv.length != 3) {
			System.err.println("args: (batchPublish|publish|consume) configPath messageSize");
			System.exit(1);
		}

		String configPath = argv[1];
		int messageSize = Integer.valueOf(argv[2]);

		if (messageSize <= 0) {
			System.err.println(argv + " isn't natural number");
			System.exit(1);
		}

		StringBuilder sb = new StringBuilder(messageSize);

		for (int i = 0; i < messageSize; i++)
			sb.append('a');

		Map<String, String> map = Utils.getConfigFromFile(configPath);

		final Channel channel = Utils.getChannel(map.get("host"),
				Integer.valueOf(Utils.getIfNotNull(map, "port", "-1")),
				map.get("user"),
				map.get("password"));

		String queueName = Utils.getIfNotNull(map, "queue", "queue");
		channel.queueDeclare(
					queueName,
					Boolean.valueOf(Utils.getIfNotNull(map, "durable", "false")),
					false,
					false,
					null);

		if (argv[0].equals("batchPublish"))
			publishInBatch(channel, queueName, sb.toString());
		else if (argv[0].equals("publish"))
			publishInASec(channel, queueName, sb.toString());
		else if (argv[0].equals("consume"))
			consume(channel, queueName);
		else {
			System.err.println("args: (batchPublish|publish|consume) configPath [queueName]");
			System.exit(1);
		}
	}

	public static void consume(Channel channel, String queueName) throws Exception {
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(queueName, true, consumer);
		
		long start = System.currentTimeMillis()/1000;
		for (int seq = 0;;) {
			long last = System.currentTimeMillis()/1000;
			for (int n = 0;; n++, seq++) {
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				String message = new String(delivery.getBody());
				long now = System.currentTimeMillis()/1000;
				if (now != last) {
					System.out.format("recv %d in 1 sec, %d/sec. Overall %d/sec.\n", n, n/(now-last), seq/(now-start));
					break;
				}
			}
		}
	}

	public static void publishInASec(Channel channel, String queueName, String message) throws Exception {
		// publis one at a time
		long start = System.currentTimeMillis()/1000;
		for (int seq = 0;;) {
			long last = System.currentTimeMillis()/1000;
			for (int n = 0;; n++, seq++) {
				channel.basicPublish("", queueName, MessageProperties.PERSISTENT_BASIC, message.getBytes());
				long now = System.currentTimeMillis()/1000;
				if (now != last) {
					System.out.format("%d in 1 sec, %d/sec. Overall %d/sec.\n", n, n/(now-last), seq/(now-start));
					break;
				}
			}
		}
	}

	public static void publishInBatch(Channel channel, String queueName, String message) throws Exception {
		int N = 1000;//in batches of 1000

		long start = System.currentTimeMillis();
		for (int seq = 0;; seq += N) {
			long last = System.currentTimeMillis();
			for (int n = 0; n < N; n++)
				channel.basicPublish("", queueName, MessageProperties.PERSISTENT_BASIC, message.getBytes());

			long now = System.currentTimeMillis();
			System.out.format("%d in %.2f (last is %d) is %.2f/sec. Overall %.2f/sec.\n", N, (now-last)/1000.0, seq, N*1000.0/(now-last), seq*1000.0/(now-start));
		}
	}
}
