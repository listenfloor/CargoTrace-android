package com.yfrt.cargotrace;

public interface AppConstants {
	String version = "app_version";
	
	String current_version = "1.0";
	
	String service_url = "http://awbtrace.efreight.cn:8080/cargotrace/HttpEngine";
	String login_url = "http://emall.efreight.cn/eFreightHttpEngine";
	
	int order_id_length = 11;
	int order_id_verify_mod = 7;
	
	String default_preference = "default_sp";
	
	String session = "session";
	String user = "user";
	
	String record_file_name = "/record.xml";
	
	String record_air_company = "/air_conpany.xml";
	
	String air_company_id = "air_company_id";
}
