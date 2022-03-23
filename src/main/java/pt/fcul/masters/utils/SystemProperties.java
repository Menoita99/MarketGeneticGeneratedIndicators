package pt.fcul.masters.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;

import lombok.Data;

@Data
public class SystemProperties {

	private static final String PROPERTIES_PATH = "src/main/resources/application.properties";

	private static SystemProperties INSTANCE;
	
	private  Properties properties = new Properties();
	
	private SystemProperties() {
		load();
	}

	private void load() {
		try {
			properties.load(new FileInputStream(new File(PROPERTIES_PATH)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	
	private static SystemProperties getInstance() {
		if(INSTANCE == null)
			INSTANCE = new SystemProperties();
		return INSTANCE;
	}
	
	
	
	
	public static String get(String key) {
		return getInstance().getProperties().getProperty(key);
	}
	
	
	
	
	public static String getOrDefault(String key,String defaultValue) {
		return getInstance().getProperties().getProperty(key,defaultValue);
	}
	
	
	
	
	public static HikariConfig getDbConfig() {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(get("db.url"));
		config.setUsername(get("db.username"));
		config.setPassword(get("db.password"));
		config.setDriverClassName(get("db.driver"));
		config.addDataSourceProperty( "cachePrepStmts" , get("db.cachePrepStmts") );
		config.addDataSourceProperty( "prepStmtCacheSize" ,get("db.prepStmtCacheSize") );
		config.addDataSourceProperty( "prepStmtCacheSqlLimit" , get("db.prepStmtCacheSqlLimit") );
		return config;
	}
}
