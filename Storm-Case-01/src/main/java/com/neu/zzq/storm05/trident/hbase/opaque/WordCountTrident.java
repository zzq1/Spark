/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.neu.zzq.storm05.trident.hbase.opaque;

import java.util.HashMap;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.trident.operation.builtin.Count;
import org.apache.storm.trident.testing.FixedBatchSpout;
import org.apache.storm.trident.testing.Split;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

public class WordCountTrident {
    public static StormTopology buildTopology(){
        Fields fields = new Fields("word", "count");
        FixedBatchSpout spout = new FixedBatchSpout(new Fields("sentence"), 3,  
                new Values("tanjie is a good man"), new Values(  
                        "what is your name"), new Values("how old are you"),  
                new Values("my name is tanjie"), new Values("i am 18"));  
        spout.setCycle(true); 
        
        MyHbaseState.Options options = new MyHbaseState.Options();  
        options.setTableName("WordCount");  
        options.setColumFamily("cf");  
        options.setQualifier("q1");  
        
        TridentTopology topology = new TridentTopology();
        topology.newStream("sentencestream", spout)  
                .each(new Fields("sentence"), new Split(), new Fields("word"))  
                .groupBy(new Fields("word"))
                .persistentAggregate(new MyHbaseState.HbaseFactory(options), new Count(), new Fields("count"))  
                .parallelismHint(3); 
        
        return topology.build();
    }

    public static void main(String[] args) throws Exception {
    	
    	Config conf = new Config();
        conf.setMaxSpoutPending(5);
        conf.put("hbase.conf", new HashMap());
        
        if (args.length == 0) {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("wordCounter", conf, buildTopology());
            Thread.sleep(60 * 1000);
//            cluster.killTopology("wordCounter");
//            cluster.shutdown();
//            System.exit(0);
        }
        else if(args.length == 2) {
            conf.setNumWorkers(3);
            StormSubmitter.submitTopology(args[1], conf, buildTopology());
        } else{
            System.out.println("Usage: TridentFileTopology <hdfs url> [topology name]");
        }
    }

}
