//var url = "/api/population/single?id=";
var xhr = new XMLHttpRequest();

//人口データキャッシュ用
var populations = new Array(47);

//年度別人口データキャッシュ用
var prefecturePopulationXML = new Array(47);

function getPopulationProc() {

	var dataObject = {};

	if ( xhr.readyState == 4 ) {
		if ( xhr.status == 0) {
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

				var index = Number( document.getElementById("prefectures").value);
				prefecturePopulationXML[index-1] = xhr.responseText;				
				outputCSV();
				
			} else {
				console.log( xhr.status );
			}
		}
	}
	document.getElementById( "downloadCSV" ).disabled = false;
};

function getPopulation() {

	var url = "/api/population/single?id=";
	var index = document.getElementById("prefectures").value;

	if (typeof prefecturePopulationXML[index-1] === "undefined"){
		document.getElementById( "download" ).removeAttribute("href");
	}
	
	//キャッシュ機能（一度読み込んだデータはリクエストしない）
	if (typeof populations[index-1] === "undefined"){
		xhr.open( "GET", url + index);
		xhr.onreadystatechange = getPopulationProc;
		xhr.send();
	} else {
		showPopulation();
	}	
};

function showPopulation(){
	var index = document.getElementById("prefectures").value
	
	document.getElementById( "pop" ).innerHTML
				= populations[index-1]["population"] + "万人です";
};


function parseXML(xmlData) {
	var dataObject = {};

	var parser = new DOMParser();
    var dom = parser.parseFromString(xmlData, 'text/xml');
    
    var element = dom.firstChild.firstChild;
    
    //各人口の取得
    while(element != null) {
		var nodeName = element.nodeName;
		var nodeText = element.textContent;
		//alert(nodeName+":"+nodeText);
		dataObject[nodeName] = nodeText;
		element = element.nextSibling;
    }
  
    return dataObject;
};

function outputCSV(){
	
	var index = document.getElementById("prefectures").value
	var prefecture = document.getElementById("prefectures").options[Number(index)-1].text;
	
	var CSVString = "";
	var dataObject = {};

	CSVString += "#year, total, male, female\r\n";
	var parser = new DOMParser();
	var dom = parser.parseFromString(prefecturePopulationXML[index-1], 'text/xml');

	var yearElement = dom.firstChild.firstChild;

	while (yearElement != null){
		var year = yearElement.getAttribute("year");
		var element = yearElement.firstChild;
		
  		while(element != null) {
	 		var nodeName = element.nodeName;
		 	var nodeText = element.textContent;
		 	//alert(year+" "+nodeName+":"+nodeText);
			dataObject[nodeName] = nodeText;
		 	element = element.nextSibling;
		}
		
		CSVString += year+","+
					dataObject["population"]+","+
					dataObject["malePopulation"]+","+
					dataObject["femalePopulation"]+"\r\n";
		
		dataObject = {};
		yearElement = yearElement.nextSibling;
	}
		
	//CSV作成
	var blob = new Blob([CSVString], {type:"text/csv"});
	var filename = prefecture + ".csv"
	
	if (window.navigator.msSaveBlob) {
		window.navigator.msSaveBlob(blob, filename); 
		
		// msSaveOrOpenBlobの場合はファイルを保存せずに開ける
        window.navigator.msSaveOrOpenBlob(blob, filename); 
        } else {
        	document.getElementById("download").href = window.URL.createObjectURL(blob);
        	document.getElementById("download").download = filename;
        }
        
    document.getElementById( "download" ).click();
    
};

function getAllPopulation(){
	
	var url = "/api/population/all?id=";
	var index = document.getElementById("prefectures").value;

	//document.getElementById( "download" ).innerHTML = "しばらくお待ちください"

	document.getElementById( "downloadCSV" ).disabled = true;

	if (typeof prefecturePopulationXML[index-1] === "undefined"){
		xhr.open( "GET", url + index);
		xhr.onreadystatechange = getAllPopulationProc;
		xhr.send();
	} else {
		outputCSV();
		document.getElementById( "downloadCSV" ).disabled = false;
	}
};
