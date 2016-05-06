package com.example.api;

import org.apache.hadoop.mapred.loadhistory_jsp;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.server.eStatAccessor;

import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

@RestController
@RequestMapping(value = "api/population", produces="text/plain;charset=UTF-8")
public class PopulationRestController {
	
	static Map<String, String> populations = new LinkedHashMap<String, String>();
	/*
	{
		{
			put("001", "");
			put("047", "");
		}
	};
	*/

	//XMLの取得
	String applicationID = "ca1cf60ecad380c0abb3dd9cfd9689de7f25eff2";
	eStatAccessor estataccessor = new eStatAccessor( applicationID );
	HashMap<Integer, String> heiseiXMLMap = estataccessor.getHeiseiXML();
	String xmlData = heiseiXMLMap.get( 2014 );
	
	@RequestMapping(value = "single", method = RequestMethod.GET)
	String getPopulation(@RequestParam("id") String id) {
		
		String populationXML = estataccessor.getSinglePopulation( xmlData, id );
		return populationXML;
		/*
		if (populations.get(id) != null) {
			//return populations.get(id);
			return dataArray[0];
		} else {
			return "エラー";
		}*/
	}
	
	@RequestMapping(value = "all", method = RequestMethod.GET)
	String getAllPopulation(@RequestParam("id") String id) {
		//String prefecturePopulationsXML = estataccessor.returnYearsXML(applicationID, id);
		//String prefecturePopulationsXML = estataccessor.returnYearsXML(applicationID, id, dataIdMap );
		String prefecturePopulationsXML = estataccessor.returnYearsXML(applicationID, id, heiseiXMLMap );
		return prefecturePopulationsXML;
	}
	
	
}
