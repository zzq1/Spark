package com.horizon.GenDataKafka;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

public class GenerateData implements Runnable {
	private final KafkaProducer<String, String> producer;
    private final String topic;
    
    private final String fileName;

    public GenerateData(String topic, String fileName) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.17.11.156:9092,172.17.11.155:9092,172.17.11.154:9092");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);//16M  16384
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        
//        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        
        producer = new KafkaProducer<>(props);
        this.topic = topic;
        this.fileName = fileName;
    }
    
    
    public void run() {

//		long start = System.currentTimeMillis();
    	
    	readFileData(); 
    	
//      long end = System.currentTimeMillis();
//		System.out.println(end-start);
    }  
    
    /** 
     * 以行为单位读取文件，常用于读面向行的格式化文件 
     */ 
    public void readFileData(){

        File file = new File(fileName);  
        BufferedReader reader = null;  
        try {
//            System.out.println("以行为单位读取文件内容，一次读一整行：");  
            reader = new BufferedReader(new FileReader(file));  
            String tempString = null;  
            long line = 1;

            // 一次读入一行，直到读入null为文件结束  
            while ((tempString = reader.readLine()) != null) {  
                if(line == 1){
                	line++;
                	continue; // 第一行表头
                }
                String[] tempData = tempString.split(",");
                
                // Kafka send
                producer.send(new ProducerRecord<>(topic, tempData[1], tempString));
                
                Thread.sleep(100);
            }
            reader.close();
            producer.close();
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            if (reader != null) {
                try {  
                    reader.close();  
                    producer.close();
                } catch (IOException e) {  
                }  
            }  
        }  
    }
    
}
