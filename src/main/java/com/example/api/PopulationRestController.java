package com.example.api;

import org.springframework.web.bind.annotation.*;

import com.example.server.eStatAccessor;

import java.util.HashMap;

@RestController
@RequestMapping(value = "api/population", produces="text/plain;charset=UTF-8")
public class PopulationRestController {
	
	//XMLの取得
	String applicationID = "ca1cf60ecad380c0abb3dd9cfd9689de7f25eff2";
	eStatAccessor estataccessor = new eStatAccessor( applicationID );
	HashMap<Integer, String> heiseiXMLMap = estataccessor.getHeiseiXML();
	String xmlData = heiseiXMLMap.get( 2014 );
	
	@RequestMapping(value = "single", method = RequestMethod.GET)
	String getPopulation(@RequestParam("id") String id) {
		
		String populationXML = estataccessor.getSinglePopulation( xmlData, id );
		return populationXML;
	}
	
	@RequestMapping(value = "all", method = RequestMethod.GET)
	String getAllPopulation(@RequestParam("id") String id) {
		String prefecturePopulationsXML = estataccessor.returnYearsXML(applicationID, id, heiseiXMLMap );
		return prefecturePopulationsXML;
	}	
}
