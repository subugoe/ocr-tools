<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@ taglib prefix="fromfile" uri="OptionsReader" %>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>OCR-Dienst der Staats- und Universitätsbibliothek Göttingen</title>
	<link rel="stylesheet" href="style.css">
</head>

<body>
	<div><img src="sub-logo.png" alt="SUB Göttingen"></div>
	<h1>OCR-Dienst der Niedersächsischen Staats- und Universitätsbibliothek Göttingen</h1>
	<p>Hier haben Sie die Möglichkeit, OCR an einem oder mehreren Bänden durchzuführen.</p>
	<form name="startOcr" action="${pageContext.request.contextPath}/ocr" method="post">
		<fieldset>
			<label>
				Eingabeordner
				<div class="help">
					<span class="help_button">?</span>
					<div class="help_popup">
						Absolute Pfadangabe eines Ordners auf dem Server. Dieser Ordner muss Unterordner (oder Links) haben,
						die jeweils einen Band repräsentieren. In jedem dieser Unterordner befinden sich Bilddateien des Bandes.
					</div>
				</div>
				<input name="inputFolder" type="text">
			</label>
			<label>
				Ausgabeordner
				<div class="help">
					<span class="help_button">?</span>
					<div class="help_popup">
						Absolute Angabe eines Ordners auf dem Server.
					</div>
				</div>
				<input name="outputFolder" type="text">
			</label>
		</fieldset>
		<fieldset>
			<legend>
				Texttyp
				<div class="help">
					<span class="help_button">?</span>
					<div class="help_popup">
						Die Auswahl hier ist unabhängig von der Auswahl der OCR-Engine. Man kann z.&nbsp;B. einen Fraktur-
						Text mit einer Antiqua-Engine erkennen lassen.
					</div>
				</div>
			</legend>
			<div class="field-group">
				<label class="inline"><input name="textType" type="radio" checked="checked" value="NORMAL"/> Antiqua</label>
				<label class="inline"><input name="textType" type="radio" value="GOTHIC"/> Fraktur</label>
			</div>
		</fieldset>
		<fieldset>
			<label>
				Sprache(n)
				<div class="help">
					<span class="help_button">?</span>
					<div class="help_popup">
						Sprachen, die in den Bänden vorkommen. Mehrfachauswahl mit Strg+Maus möglich.
						Die Liste wird aus der Datei languages.properties gelesen. Die Datei kann beliebig erweitert werden,
						wobei die Schlüsselwerte dem ISO-639-1 Standard entsprechen müssen.
					</div>
				</div>
				<select name="languages" size="5" multiple="multiple">
					${fromfile:getLanguages()}
				</select>
			</label>
		</fieldset>
		<fieldset>
			<label>
				Ausgabeformat(e)
				<div class="help">
					<span class="help_button">?</span>
					<div class="help_popup">
						Mehrfachauswahl mit Strg+Maus möglich.
					</div>
				</div>
				<select name="outputFormats" size="5" multiple="multiple">
					<option selected="selected">PDF</option>
					<option>PDFA</option>
					<option>XML</option>
					<option>TXT</option>
				</select>
			</label>
		</fieldset>
		<fieldset>
			<label>
				Benachrichtigungsadresse
				<div class="help">
					<span class="help_button">?</span>
					<div class="help_popup">
						An diese Mail-Adresse werden Benachrichtigungen über den Verlauf des OCR-Prozesses gesendet.
					</div>
				</div>
				<input type="text" name="email">
			</label>
		</fieldset>
		<div class="row">
			<span class="more-options-toggle -visible">Weitere Optionen anzeigen</span>
			<span class="more-options-toggle">Weitere Optionen ausblenden</span>
		</div>
		<fieldset class="more-options">
			<label>
				OCR-Engine
				<div class="help">
					<span class="help_button">?</span>
					<div class="help_popup">
						Der GBV Antiqua-Server ist z.&nbsp;Z. gebührenfrei. Der Fraktur-Server und die Abbyy-Cloud
						sind kostenpflichtig und erfordern Benutzerangaben.
					</div>
				</div>
				<select name="ocrEngine">
					<option value="gbvAntiqua">GBV Abbyy Server (Antiqua)</option>
					<option value="gbvFraktur">GBV Abbyy Server (Fraktur)</option>
					<option value="abbyyCloud">Abbyy Cloud ocrsdk.com</option>
				</select>
			</label>
			<label>
				Benutzername
				<div class="help">
					<span class="help_button">?</span>
					<div class="help_popup">
						Im Falle von Abbyy-Cloud ist es die AppID.
					</div>
				</div>
				<input name="user" type="text">
			</label>
			<label>
				Passwort
				<input name="password" type="text">
			</label>
			<label>
				Logfile
				<div class="help">
					<span class="help_button">?</span>
					<div class="help_popup">
						Absoluter Pfad zu einer Datei auf dem Server. Wenn die Datei existiert, wird unten angehängt.
						Wenn hier nichts angegeben wird, dann wird in die Logdatei des Servers geloggt. Achtung: Auch wenn hier eine
						Datei angegeben wird, gehen RuntimeExceptions trotzdem in das Log des Servers.
					</div>
				</div>
				<input name="user" type="text">
			</label>
		</fieldset>
		<input type="submit" name="submit" value="OCR starten">
	</form>
	<script type="text/javascript" src="script.js"></script>
</body>
</html>
