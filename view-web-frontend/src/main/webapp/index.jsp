<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib prefix="fromfile" uri="OptionsReader" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>OCR</title>
<script type="text/javascript">
	function setStyle(elementId, style) {
		document.getElementById(elementId).setAttribute('style', style);
	}
	var inputFolderInfo = "Absolute Pfadangabe eines Ordners auf dem Server. Dieser Ordner muss Unterordner (oder Links) haben, "
	 + "die jeweils einen Band repräsentieren. In jedem dieser Unterordner befinden sich Bilddateien des Bandes.";
	var outputFolderInfo = "Absolute Angabe eines Ordners auf dem Server.";
	var textTypeInfo = "Die Auswahl hier ist unabhängig von der Auswahl der OCR-Engine. Man kann z. B. einen Fraktur-"
	 + "Text mit einer Antiqua-Engine erkennen lassen.";
	var languagesInfo = "Sprachen, die in den Bänden vorkommen. Mehrfachauswahl mit Strg+Maus möglich. "
	 + "Die Liste wird aus der Datei languages.properties gelesen. Die Datei kann beliebig erweitert werden, "
	 + "wobei die Schlüsselwerte dem ISO-639-1 Standard entsprechen müssen.";
	var outputFormatsInfo = "Mehrfachauswahl mit Strg+Maus möglich.";
	var emailInfo = "An diese Mail-Adresse werden Benachrichtigungen über den Verlauf des OCR-Prozesses gesendet";
	var ocrEngineInfo = "Der GBV Antiqua Server ist z. Z. gebührenfrei. Der Fraktur-Server und die Abbyy-Cloud "
	 + "sind kostenpflichtig und erfordern Benutzerangaben.";
	var userInfo = "Im Falle von Abbyy-Cloud ist es die AppID.";
	var logFileInfo = "Absoluter Pfad zu einer Datei auf dem Server. Wenn die Datei existiert, wird unten angehängt. "
	 + "Wenn hier nichts angegeben wird, dann wird in die Logdatei des Servers geloggt. Achtung: Auch wenn hier eine "
	 + "Datei angegeben wird, gehen RuntimeExceptions trotzdem in das Log des Servers."
</script>
<style type="text/css">
.inputOptions{background-color:#A9BCF5; margin: 10px; padding: 10px; float: left;}
</style>
</head>
<body>

	<h1>GBV OCR</h1>

	<h3>Hier haben Sie die Möglichkeit, OCR an einem oder mehreren Bänden durchzuführen.</h3>

	<form name="startOcr" action="${pageContext.request.contextPath}/ocr" method="post">
		<div class="inputOptions">
		Eingabeordner:<a href="javascript:alert(inputFolderInfo)">?</a> <input name="inputFolder" type="text">
		<br/>
		Ausgabeordner:<a href="javascript:alert(outputFolderInfo)">?</a> <input name="outputFolder" type="text">
		</div>
		<br/>
		<br/>
		<br/>
		<br/>
		<br/>
		
		<div class="inputOptions">
		Texttyp:<a href="javascript:alert(textTypeInfo)">?</a>
		<br/>
		<input name="textType" type="radio" checked="checked" value="NORMAL"/>Antiqua <br/>
		<input name="textType" type="radio" value="GOTHIC"/>Fraktur <br/>
		</div>
	
		
		<div class="inputOptions">
		Sprache(n):<a href="javascript:alert(languagesInfo)">?</a>	
		<br/> 
		<select name="languages" size="5" multiple="multiple">
			${fromfile:getLanguages()}
		</select>
		</div>

		<div class="inputOptions">
		Ausgabeformat(e):<a href="javascript:alert(outputFormatsInfo)">?</a>	
		<br/> 
		<select name="outputFormats" size="5" multiple="multiple">
			<option selected="selected">PDF</option>
			<option>PDFA</option>
			<option>XML</option>
			<option>TXT</option>
		</select>
		</div>
		
		<br/>
		<br/>
		<br/>
		<br/>
		<br/>
		<br/>
		<br/>
		<br/>
		<div class="inputOptions">
		Benachrichtigungsadresse:<a href="javascript:alert(emailInfo)">?</a> <input type="text" name="email">
		</div>
		<br/>
		<br/>
		<br/>
		<br/>
		<div style="float: left;">
		<a style="padding: 10px;" href="#" onclick="setStyle('moreOptions','display: block')">Weitere Optionen anzeigen</a>
		</div>
		<div style="float: left;">
			<input style="position: relative; left: 300px;" type="submit" name="submit" value="OCR starten">
		</div>
		
		<br/>
		<br/>
		<div id="moreOptions" style="display:none">
		
		<div class="inputOptions"> 
		OCR-Engine:<a href="javascript:alert(ocrEngineInfo)">?</a>
		<select name="ocrEngine">
			<option value="gbvAntiqua">GBV Abbyy Server (Antiqua)</option>
			<option value="gbvFraktur">GBV Abbyy Server (Fraktur)</option>
			<option value="abbyyCloud">Abbyy Cloud ocrsdk.com</option>
		</select>
		<br/>
		Benutzername:<a href="javascript:alert(userInfo)">?</a> <input name="user" type="text">
		<br/>
		Passwort: <input name="password" type="text">
		</div>
		
				<br/>
		
		</div>
		</form>
</body>
</html>