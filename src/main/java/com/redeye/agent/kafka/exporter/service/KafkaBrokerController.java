package com.redeye.agent.kafka.exporter.service;

import com.redeye.agent.kafka.acquisitor.KafkaAcquisitor;
import com.redeye.agent.util.JSONUtil;
import com.redeye.agent.util.http.service.annotation.Controller;
import com.redeye.agent.util.http.service.annotation.RequestHandler;

/**
 * 
 * 
 * @author jmsohn
 */
@Controller(basePath = "/broker")
public class KafkaBrokerController {
	
	/**
	 * 
	 * 
	 * @return
	 */
	@RequestHandler(path = "/client/connection")
	public String getClientConnectList() {
		return JSONUtil.toJSON(KafkaAcquisitor.getClientConnMap());
	}
}
