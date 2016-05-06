package com.example.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class eStatAccessor {
	
	String applicationID;

	//コンストラクタ
	public eStatAccessor( String applicationID ){
		this.applicationID = applicationID;
	}
	
	private String getSearchWord( String surveyYears ) {
		
		//西暦から平成に変換
		String heisei = String.valueOf( (Integer.parseInt( surveyYears ) + 12) % 100 );

		String searchWord;
		
		if ( Integer.parseInt( heisei ) > 18) {
			searchWord = "都道府県，男女別人口及び人口性比"  + " 平成"+ heisei + "年";
		} else {
			searchWord = "人口数及び性比－総人口 全国・都道府県" + " 平成" + heisei + "年";
		}
		
		String encodedSearchWord = "";
		
		//検索ワードのURIエンコード
		try{
			encodedSearchWord = URLEncoder.encode( searchWord, "UTF-8" );
		} catch ( Exception e ){
			e.printStackTrace();
		}
		
		return encodedSearchWord;
	}
	
	private String createStatListURL( String surveyYears ) {
		
		//統計表情報取得URL
		String statListURL = "http://api.e-stat.go.jp/rest/2.0/app/getStatsList?";
		
		//検索ワード
		String searchWord = getSearchWord( surveyYears );
				
		//パラメータの追加
		statListURL += "appId=" + applicationID;
		statListURL += "&statsCode=00200524";
		statListURL += "&searchWord=" + searchWord;
		
		return statListURL;
	}
	
	private String parseStatListXML( String statListXML ) {
		
		String statsDataId = "";
		
		//XML解析
		try{
			DocumentBuilderFactory dbf= DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			StringReader sr = new StringReader( statListXML );
			InputSource is = new InputSource( sr );
			
			Document document = db.parse( is );
			
			//統計表IDを取得
			NodeList nl = document.getElementsByTagName( "TABLE_INF" );
			if ( nl.getLength() == 1 ) {
				Element e = ( Element )nl.item( 0 );				
				
				if ( e.getNodeName() == "TABLE_INF" ) { 
					statsDataId = e.getAttribute( "id" ).toString();
				} else {
					System.out.println( e.getNodeName() );
				}
			}
			
			return statsDataId;
			
		} catch ( Exception e ) {
			return statsDataId;
		}
	}
	
	//統計表IDの取得
	private String getStatsDataId( String surveyYears ) {
		
		//統計表情報取得URL
		String statListURL = createStatListURL( surveyYears );
		
		//統計表情報XML
		String statListXML = getXML( statListURL );
		
		//統計表ID
		String statsDataId = parseStatListXML( statListXML );

		return statsDataId;
	}
	
	//統計XMLデータの取得
	private String getStatXML( String statsDataId ) { 
		
		//統計データ取得URL
		String statDataURL = "http://api.e-stat.go.jp/rest/2.0/app/getStatsData?";
		
		//パラメータの追加
		statDataURL += "appId=" + applicationID;
		statDataURL += "&statsDataId=" + statsDataId;
		
		String statDataXML = getXML( statDataURL );
		
		return statDataXML;
	}
		
	//XMLデータ取得
	private String getXML( String requestURL ) {
		
		//HTTP通信によるXMLの受信
		HttpURLConnection conn = null;
		
		try{
			//HTTP通信開始
			URL url = new URL( requestURL );
			conn = ( HttpURLConnection )url.openConnection();
		    conn.setRequestMethod( "GET" );
		    conn.setDoOutput( true );
		    conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
		    conn.connect();
		    
		    //レスポンス受信
		    InputStream is = conn.getInputStream();
		    BufferedReader reader = new BufferedReader( new InputStreamReader( is ) );
		    
		    //xmlの読み込み
		    StringBuilder xml = new StringBuilder();
		    String s;
		    
		    while ( ( s = reader.readLine() ) != null ) {  
		        xml.append( s );  
		    }
		    is.close();
		    
		    String xmlData = xml.toString();
		    return xmlData;
		    
		} catch( SocketTimeoutException e ){
			return e.toString();
		} catch( IOException e ){
			return e.toString();
		} finally{
			conn.disconnect();
		}
	}
	
	//XMLデータから各人口を抽出
	private String[] parseXML( String xmlData, String code ) {
		
		String[] dataArray = new String[3];
		code += "000";
		
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			StringReader sr = new StringReader( xmlData );
			InputSource is = new InputSource( sr );
			
			Document document = db.parse( is );
			
			//人口データの取得						
			if ( document.getElementsByTagName( "DATA_INF" ).getLength() == 1 ) {
				Element datainf = ( Element )document.getElementsByTagName( "DATA_INF" ).item( 0 );
				NodeList values = datainf.getElementsByTagName( "VALUE" );
				
				dataArray = searchPrefecturePopulation( values, code );
				
			}
			
		} catch( Exception e ) {
	        System.out.println( e );
		}
		
		return dataArray;
	}
	
	private String[] searchPrefecturePopulation( NodeList values, String code ){
		
		String[] dataArray = new String[3];
		
		for ( int i = 0; i < values.getLength(); i++ ) {
			Element value = ( Element )values.item( i );
			String areaValue = value.getAttribute( "area" );
			
			//指定都道府県と一致する場合
			if ( code.equals( areaValue ) ) {
				
				String cat01 = value.getAttribute( "cat01" );
				String cat02 = value.getAttribute( "cat02" );
				String population = value.getFirstChild().getNodeValue();
										
				//総人口データ(単位が千人なので万人に変換)
				if ( cat02.equals( "001" ) || cat02.equals("") ) {
				
					if ( cat01.equals( "001" ) ) {
						dataArray[0] = String.valueOf( Float.parseFloat( population ) / 10 );
					} else if ( cat01.equals( "002" ) ) {
						dataArray[1] = String.valueOf( Float.parseFloat( population ) / 10 );
					} else if ( cat01.equals( "003" ) ) {
						dataArray[2] = String.valueOf( Float.parseFloat( population ) / 10 );
					}
				}
			}
			
		}
		return dataArray;
	}
 	
	private Document createXMLDocument( String[] population ){
		DocumentBuilder documentBuilder = null;

		try{
			documentBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
		} catch ( ParserConfigurationException e ) {
			e.printStackTrace();
		}
		
		Document document = documentBuilder.newDocument();
		
		//XMLデータの作成
		Element statData = document.createElement( "statData" );
		document.appendChild( statData );
		Element totalPopulation = document.createElement( "population" );
		totalPopulation.appendChild( document.createTextNode( population[0] ) );
		Element malePopulation = document.createElement( "malePopulation" );
		malePopulation.appendChild( document.createTextNode( population[1] ) );
		Element femalePopulation = document.createElement( "femalePopulation" );
		femalePopulation.appendChild( document.createTextNode( population[2] ) );
		statData.appendChild( totalPopulation );
		statData.appendChild( malePopulation );
		statData.appendChild( femalePopulation );
		
		return document;
	}
	
	private String documentToString( Document document ) {
		
		//DocumentからStringに変換
		Transformer transformer = null;
		StringWriter stringWriter = null;
		try {
			stringWriter = new StringWriter();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformer = transformerFactory.newTransformer();
		} catch ( TransformerConfigurationException e ) {
			e.printStackTrace();
		}
		
		transformer.setOutputProperty( "indent", "no" );
		transformer.setOutputProperty( "encoding", "utf-8" );

		try {
			transformer.transform( new DOMSource( document ), new StreamResult( stringWriter ) );
		} catch ( TransformerException e ) {
			e.printStackTrace();
		}
		String xmlData = stringWriter.toString();
		return xmlData;
	}
	
	private String returnXML( String[] population ) {
		
		Document document = createXMLDocument( population );
		String xmlData = documentToString( document );
		return xmlData;
	}
	
	public HashMap<Integer, String> getHeiseiXML() {
		HashMap<Integer, String> heiseiXML = new HashMap<Integer, String>();
		//平成の人口データを取得
		for ( int year = 1989; year < 2017; year++ ) {
			String surveyYears = String.valueOf( year );
			
			String statDataId = getStatsDataId( surveyYears );
			
			if ( ! statDataId.equals("") ){
				String xmlData = getStatXML( statDataId );
				heiseiXML.put(year, xmlData );

				try{
					Thread.sleep( 50 );
				} catch ( InterruptedException e ){}
			} 
		}
		return heiseiXML;
	}
	
	//overload
	private HashMap<Integer, String[]> getHeiseiPopulations( String prefectureID, HashMap<Integer, String> heiseiXMLMap) {
		System.out.println("getHeiseiPopulations overload");
		
		HashMap<Integer, String[]> populations = new HashMap<Integer, String[]>();
		
		//平成の人口データを取得
		for ( int year = 1989; year < 2017; year++ ) {
			String surveyYears = String.valueOf( year );
			
			String statDataId = getStatsDataId( surveyYears );
			
			if ( ! statDataId.equals("") ){
				String xmlData = heiseiXMLMap.get( year );
				String[] dataArray = parseXML( xmlData, prefectureID );
				populations.put(Integer.parseInt( surveyYears ), dataArray );
			} 
		}
		
		return populations;
	}
	
	private Document createHeiseiPopulationsDocument( HashMap<Integer, String[]> populations, String prefectureID ) {
		
		DocumentBuilder documentBuilder = null;

		try{
			documentBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
		} catch ( ParserConfigurationException e ) {
			e.printStackTrace();
		}
		
		Document document = documentBuilder.newDocument();
		
		//XMLデータの作成
		Element root = document.createElement( "prefecture" );
		root.setAttribute( "id", prefectureID );
		document.appendChild( root );
		
		//平成の人口データを含むXMLの作成
		for ( int dataYear = 1989; dataYear<2017; dataYear++ ) {

			System.out.println(dataYear);
			String[] population;
			String dYear = String.valueOf( dataYear );
			
			if ( populations.containsKey( dataYear ) ) {
				population = populations.get( dataYear );
			} else {
				population = new String[]{"-", "-", "-"};
			}
		
			Element year = document.createElement( "population" );
			year.setAttribute( "year", dYear );
			root.appendChild( year );
			
			Element totalPopulation = document.createElement( "population" );
			totalPopulation.appendChild( document.createTextNode( population[0] ) );
			Element malePopulation = document.createElement( "malePopulation" );
			malePopulation.appendChild( document.createTextNode( population[1] ) );
			Element femalePopulation = document.createElement( "femalePopulation" );
			femalePopulation.appendChild( document.createTextNode( population[2] ) );
			year.appendChild( totalPopulation );
			year.appendChild( malePopulation );
			year.appendChild( femalePopulation );
		}
		return document;
	}
	
	public String returnYearsXML( String applicationID, String prefectureID, HashMap<Integer, String> HeiseiXML ) {
		System.out.println("returnYearsXML");

		HashMap<Integer, String[]> populations = new HashMap<Integer, String[]>();
		
		//指定された都道府県の平成における人口
		populations = getHeiseiPopulations( prefectureID, HeiseiXML );
		
		Document document = createHeiseiPopulationsDocument( populations, prefectureID );
		String xmlData = documentToString( document );

		return xmlData;
	}
	
	public String loadXMLData( String surveyYears) {
		
		String statDataId = getStatsDataId( surveyYears );
		String xmlData = getStatXML( statDataId );
		return xmlData;
	}
	
	public String getSinglePopulation( String xmlData, String id ) {

		String[] dataArray = parseXML(xmlData, id);
		String populationXML = returnXML( dataArray );
		
		return populationXML;
	}

}
