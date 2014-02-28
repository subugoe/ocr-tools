<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>OCR</title>
<script type="text/javascript">
	function setStyle(elementId, style) {
		document.getElementById(elementId).setAttribute('style', style);
	}
</script>
<style type="text/css">
.inputOptions{background-color:#A9BCF5; margin: 10px; padding: 10px; float: left;}
</style>
</head>
<body>

	<h1>GDZ OCR</h1>

	<h3>Hier haben Sie die Möglichkeit, OCR an einem Band durchzuführen.</h3>

	<form name="startOcr" action="ocr" method="post">
		<div class="inputOptions">
		Eingabeordner: <input name="inputFolder" type="text">
		<br/>
		Ausgabeordner: <input name="outputFolder" type="text">
		</div>
		<br/>
		<br/>
		<br/>
		<br/>
		<br/>
		
		<div class="inputOptions">
		Dateiformat:
		<br/>
		<select name="imageFormat">
			<option>tif</option>
			<option>jpg</option>
			<option>png</option>
		</select>
		</div>
		
		<div class="inputOptions">
		Texttyp:
		<br/>
		<input name="textType" type="radio" checked="checked" value="NORMAL"/>Antiqua <br/>
		<input name="textType" type="radio" value="GOTHIC"/>Fraktur <br/>
		</div>
	
		
		<div class="inputOptions">
		Sprache(n):	
		<br/> 
		<select name="languages" size="5" multiple="multiple">
			<option value="de" selected="selected">Deutsch</option>
			<option value="en">Englisch</option>
			<option value="fr">Französisch</option>
		</select>
		</div>

		<div class="inputOptions">
		Ausgabeformat(e):	
		<br/> 
		<select name="outputFormats" size="5" multiple="multiple">
			<option selected="selected">PDF</option>
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
		Benachrichtigungsadresse: <input type="text" name="email" value="test@test.de">
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
		<div class="inputOptions" id="moreOptions" style="display:none">
		OCR Engine:
		<select name="ocrEngine">
			<option value="gbvAntiqua">GBV Abbyy Server (Antiqua)</option>
			<option value="gbvGothic">GBV Abbyy Server (Fraktur)</option>
			<option value="abbyyCloud">Abbyy Cloud ocrsdk.com</option>
		</select>
		<br/>
		Benutzername: <input name="userName" type="text">
		<br/>
		Passwort: <input name="password" type="text">
		</div>
		</form>
</body>
</html>