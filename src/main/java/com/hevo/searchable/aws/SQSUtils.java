package com.hevo.searchable.aws;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hevo.searchable.FileService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Log4j2
public class SQSUtils {

    private static final ObjectMapper mapper= new ObjectMapper();

    private static final AmazonSQS SQS = AmazonSQSClientBuilder
            .standard()
            .withRegion("us-east-2")
            .withCredentials(new EnvironmentVariableCredentialsProvider())
            .build();

    private static final String QUEUE_URL = "https://sqs.us-east-2.amazonaws.com/385441345207/filenotification";

    @Autowired @Qualifier("s3FileService") private FileService fileService;
    @Autowired private S3Utils s3Utils;

    //Todo: Poll every 5 seconds for new messages
    public void receiveMessage() throws JsonProcessingException {
        while (true){
            List<Message> messages = SQS.receiveMessage(QUEUE_URL).getMessages();
            for(Message message : messages){
                JsonNode jsonNode = mapper.readTree(message.getBody());
                String key = jsonNode.get("Records").get(0).get("s3").get("object").get("key").asText();
                String client = key.split("/")[0];
                String fileName = key.split("/")[1];
                String eventName = jsonNode.get("Records").get(0).get("eventName").asText();

                log.info("Received message for client {} and file {} with event {}", client, key, eventName);
                processS3Events(client, fileName, eventName);
                SQS.deleteMessage(QUEUE_URL, message.getReceiptHandle());
            }
        }
    }

    //Todo: Put the Success/Failed files in DB for better Error Handling
    private void processS3Events(String client, String fileName, String eventName) {
        try{
            if (eventName.equals("ObjectCreated:Put")){
                fileService.index(client, s3Utils.downloadS3File("filetest1", client, fileName));
            }else if (eventName.equals("ObjectRemoved:Delete")){
                fileService.delete(client, fileName);
            }else {
                log.error("Unknown event {}", eventName);
            }
        }catch(Exception e){
            log.error("Error while processing event {} for file {} and client {}", eventName, fileName, client);
        }
    }
}
