package com.hevo.searchable.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
@Log4j2
public class S3Utils {

    private static final AmazonS3 s3Client = AmazonS3ClientBuilder
            .standard()
            .withRegion("us-east-2")
            .withCredentials(new EnvironmentVariableCredentialsProvider())
            .build();

    public File downloadS3File(String bucketName, String client, String fileName) throws IOException {
        String key = client + "/" + fileName;
        log.info("Downloading file {} from bucket {}", key, bucketName);
        S3Object fullObject = null, objectPortion = null, headerOverrideObject = null;
        FileOutputStream fos = null;
        try{
            fullObject = s3Client.getObject(new GetObjectRequest(bucketName, key));
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fullObject.getObjectContent());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n = 0;
            while (-1 != (n = bufferedInputStream.read(buf))) {
                byteArrayOutputStream.write(buf, 0, n);
            }
            byteArrayOutputStream.close();
            bufferedInputStream.close();

            byte[] response = byteArrayOutputStream.toByteArray();
            fos = new FileOutputStream("/tmp/"+ fileName);
            fos.write(response);
        }catch (AmazonServiceException e) {
            log.error("Error Message:    " + e.getMessage());
        } catch (SdkClientException e) {
            log.error("Error Message:    " + e.getMessage());
        } finally{
            if (fullObject != null) {
                fullObject.close();
            }
            if (objectPortion != null) {
                objectPortion.close();
            }
            if (headerOverrideObject != null) {
                headerOverrideObject.close();
            }
            if (fos!=null){
                fos.close();
            }
        }
        return new File("/tmp/"+ fileName);
    }
}
