var xhr = new XMLHttpRequest();

//人口データキャッシュ用
var populations = new Array(47);

//年度別人口データキャッシュ用
var prefecturePopulationXML = new Array(47);

function getPopulationProc() {

	var dataObject = {};

	if ( xhr.readyState == 4 ) {
		if ( xhr.status == 0 ) {
			console.log( "通信失敗" );
		} else {
			if ( xhr.status === 200 ) {
				console.log( xhr.responseText );
				
				dataObject = parseXML( xhr.responseText );
				var index = Number( document.getElementById( "prefectures" ).value );
				populations[ index-1 ] = dataObject;
				showPopulation();

			} else {
				console.log( xhr.status );
			}
		}
	}
};

function getAllPopulationProc() {

	if ( xhr.readyState == 4 ) {
		if ( xhr.status == 0 ) {
			console.log("getAllPopulationProc 通信失敗");
		} else {
			if ( xhr.status === 200 ) {
				console.log( xhr.responseText );

				var index = Number( document.getElementById( "prefectures" ).value );
				prefecturePopulationXML[ index-1 ] = xhr.responseText;
				outputCSV();
				
			} else {
				console.log( xhr.status );
			}
		}
	}
	document.getElementById( "message" ).innerHTML = "";
	document.getElementById( "downloadCSV" ).disabled = false;
	document.getElementById(　"prefectures"　).disabled = false;
};

function getPopulation() {

	var url = "/api/population/single?id=";
	var index = document.getElementById( "prefectures" ).value;

	if ( typeof prefecturePopulationXML[ index-1 ] === "undefined" ){
		document.getElementById( "download" ).removeAttribute( "href" );
	}
	
	//キャッシュ機能（一度読み込んだデータはリクエストしない）
	if ( typeof populations[ index-1 ] === "undefined" ){
		XMLRequest( url, index, getPopulationProc );
	} else {
		showPopulation();
	}	
};

function XMLRequest( url, pulldownIndex, functionName ) {
		xhr.open( "GET", url + pulldownIndex );
		xhr.onreadystatechange = functionName;
		xhr.send();
}

function showPopulation(){
	var index = document.getElementById( "prefectures" ).value
	
	document.getElementById( "pop" ).innerHTML
					= populations[ index-1 ][ "population" ] + "万人です";
};

function parseXML( xmlData ) {
	var dataObject = {};

	var parser = new DOMParser();
    var dom = parser.parseFromString( xmlData, 'text/xml' );
    var element = dom.firstChild.firstChild;

    dataObject = searchPopulation( element );
  
    return dataObject;
};

function searchPopulation( element ) {
	var dataObject = {};

	while( element != null ) {
		var nodeName = element.nodeName;
		var nodeText = element.textContent;
		dataObject[ nodeName ] = nodeText;
		element = element.nextSibling;
    }

    return dataObject;

}

function outputCSV(){
	
	var index = document.getElementById( "prefectures" ).value;

	var CSVString = createCSVString( prefecturePopulationXML[ index-1 ] );
    createCSV( CSVString, index );

    document.getElementById( "download" ).click();
    
};

function createCSVString( xmlData ) {

	var dataObject = {};
	var parser = new DOMParser();
	var dom = parser.parseFromString( xmlData, 'text/xml' );
	var yearElement = dom.firstChild.firstChild;

	//CSVヘッダー部
	var CSVString = "#year, total, male, female\r\n";

	while ( yearElement != null ){
		var year = yearElement.getAttribute( "year" );
		var element = yearElement.firstChild;
		
		dataObject = searchPopulation( element );
		
		CSVString += year + "," +
					dataObject[ "population" ]+ "," +
					dataObject[ "malePopulation" ]+ "," +
					dataObject[ "femalePopulation" ] + "\r\n";
		
		dataObject = {};
		yearElement = yearElement.nextSibling;
	}

	return CSVString;
}

function createCSV( CSVString, index ) {
	var blob = new Blob( [ CSVString ], { type:"text/csv" } );
	var prefecture = document.getElementById( "prefectures" ).options[ Number( index ) -1 ].text;
	var filename = prefecture + ".csv"
	
	//ブラウザがInternetExplorerの場合
	if ( window.navigator.msSaveBlob ) {
		// msSaveOrOpenBlobの場合はファイルを保存せずに開ける
        window.navigator.msSaveOrOpenBlob( blob, filename ); 
        } else {
        	document.getElementById(　"download"　).href = window.URL.createObjectURL(　blob　);
        	document.getElementById(　"download"　).download = filename;
        }

}

function getAllPopulation(){
	
	var url = "/api/population/all?id=";
	var index = document.getElementById(　"prefectures"　).value;

	document.getElementById( "downloadCSV" ).disabled = true;
	document.getElementById( "message" ).innerHTML = "しばらくお待ちください";
	document.getElementById(　"prefectures"　).disabled = true;

	if (　typeof prefecturePopulationXML[　index-1　] === "undefined"　){
		XMLRequest( url, index, getAllPopulationProc );
	} else {
		outputCSV();
		document.getElementById( "message" ).innerHTML = "";
		document.getElementById( "downloadCSV" ).disabled = false;
		document.getElementById(　"prefectures"　).disabled = false;
	}
};
