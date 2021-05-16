
import com.examples.helloworld.*;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;


import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple client that requests a greeting from the .
 */
public class HelloWorldClient {
    private final ManagedChannel channel;
    private final GreeterGrpc.GreeterBlockingStub blockingStub;
    private final GreeterGrpc.GreeterFutureStub futureStub;
    private static final Logger logger = Logger.getLogger(HelloWorldClient.class.getName());

    public HelloWorldClient(String host,int port){
        channel = ManagedChannelBuilder.forAddress(host,port)
                .usePlaintext()
                .build();

        futureStub = GreeterGrpc.newFutureStub(channel);
        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }


    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void greet(String name) {
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        HelloReply response;
        try {
            response = blockingStub.sayHello(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Message from gRPC-Server: " + response.getMessage());
    }

    public void notifyMsg(String name) {
        NotifyRequest request = NotifyRequest
                .newBuilder()
                .setName(name)
                .setStatus(200)
                .build();
        NotifyReply response;
        try {
            ListenableFuture<NotifyReply> result = futureStub.notify(request);
            System.out.println("waiting result");
            response = result.get();
        } catch (Exception e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e);
            return;
        }
        logger.info("Message from gRPC-Server: " + response.getMessage());
    }

    public static void main(String[] args) throws InterruptedException {
        HelloWorldClient client = new HelloWorldClient("127.0.0.1",50051);
        try{
            String user = "world";
            if (args.length > 0){
                user = args[0];
            }
//            client.greet(user);
            client.notifyMsg(user);

        }finally {
            client.shutdown();
        }
    }
}