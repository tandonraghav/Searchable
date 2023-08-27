package com.hevo.searchable;

import com.hevo.searchable.aws.SQSUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(scanBasePackages = {"com.hevo.searchable"})
@ConfigurationPropertiesScan("com.hevo.searchable")
@Log4j2
public class SearchableApplication {

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(SearchableApplication.class, args);
        startSQS(applicationContext);
    }

    private static void startSQS(ApplicationContext applicationContext){
        try{
            SQSUtils sqsUtils = applicationContext.getBean(SQSUtils.class);
            sqsUtils.receiveMessage();
        }catch (Exception e){
            log.error("Exception in startSQS ",e);
        }
    }
}
