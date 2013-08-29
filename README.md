this is for benchmark RabbitMQ

because I couldn't find any maven repo that provide rabbimq-client 3.1.5 so we should install it manually, so run

    ./install.sh
	mvn install

and then you will get target/rabbitMqBench-1.0-SNAPSHOT.jar edit src/main/resources/config.txt a little, and then run

    java -cp target/rabbitMqBench-1.0-SNAPSHOT.jar org.xudifsd.Bench publish src/main/resources/config.txt
    java -cp target/rabbitMqBench-1.0-SNAPSHOT.jar org.xudifsd.Bench batchPublish src/main/resources/config.txt
    java -cp target/rabbitMqBench-1.0-SNAPSHOT.jar org.xudifsd.Bench consume src/main/resources/config.txt
