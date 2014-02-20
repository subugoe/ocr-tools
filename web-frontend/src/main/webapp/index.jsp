<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>OCR</title>
<script type="text/javascript">

</script>
<style type="text/css">
.inputOptions{background-color:#A9BCF5; margin: 10px; padding: 10px; float: left;}
</style>
</head>
<body>

	<h1>GDZ OCR</h1>

	<h3>Hier haben Sie die Möglichkeit, OCR an einem Band durchzuführen.</h3>

	<form action="ocr-started.jsp" method="get">
		<div class="inputOptions" style="float:left">
		Eingabeordner: <input type="text"> oder PPN: <input type="text">
		<br/>
		Ausgabeordner: <input type="text">
		</div>
		<br/>
		<br/>
		<br/>
		<br/>
		<br/>
		<div class="inputOptions" style="float:left">
		Texttyp:
		<br/>
		<input type="radio" name="textType" checked="checked"/>Fraktur <br/>
		<input type="radio" name="textType"/>Antiqua <br/>
		</div>
	
		
		<div class="inputOptions" style="float: left">
		Sprache(n):	
		<br/> 
		<select>
			<option>Deutsch</option>
			<option>Englisch</option>
			<option>Französisch</option>
		</select>
		<input type="button" value="Weitere Sprache" onclick="document.getElementById('secondLang').setAttribute('style', 'display: block')">
		<br/>
		<span id="secondLang" style="display: none;">
		<select>
			<option>Englisch</option>
			<option>Französisch</option>
		</select>
		<input type="button" value="Entfernen" onclick="document.getElementById('secondLang').setAttribute('style', 'display: none')"/>
		</span>
		</div>

		<div class="inputOptions" style="float: left">
		Ausgabeformat(e):	
		<br/> 
		<select>
			<option>XML</option>
			<option>PDF</option>
			<option>Text</option>
		</select>
		<input type="button" value="Weiteres Format" onclick="document.getElementById('secondFormat').setAttribute('style', 'display: block')">
		<br/>
		<span id="secondFormat" style="display: none;">
		<select>
			<option>PDF</option>
			<option>Text</option>
		</select>
		<input type="button" value="Entfernen" onclick="document.getElementById('secondFormat').setAttribute('style', 'display: none')"/>
		</span>
		</div>
		
		<br/>
		<br/>
		<br/>
		<br/>
		<br/>
		<br/>
		<div class="inputOptions" style="float: left">
		Benachrichtigungsadresse: <input type="text" name="email" value="test@test.de">
		</div>
		<br/>
		<br/>
		<br/>
		<br/>
		<div style="float: left;">
		<a style="padding: 10px;" href="#" onclick="document.getElementById('moreOptions').setAttribute('style', 'display: block')">Weitere Optionen anzeigen</a>
		</div>
		<div style="float: left;">
			<input style="position: relative; left: 300px;" type="submit" value="OCR starten">
		</div>
		
		<br/>
		<br/>
		<div class="inputOptions" id="moreOptions" style="display:none">
		OCR Engine:
		<select>
			<option>GBV Abbyy Server</option>
			<option>Abbyy OCRSDK Cloud</option>
		</select>
		<br/>
		Benutzername: <input type="text">
		<br/>
		Passwort: <input type="text">
		</div>
		</form>
</body>
</html>