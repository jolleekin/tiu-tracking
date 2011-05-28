﻿<!doctype html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8">
	<title>Asset Tracking System</title>
	<link rel="shortcut icon" href= "favicon.ico">
	<script type="application/x-javascript">
	<?php
		session_start();
		include 'Common.php';
		// Returns the map info
		$lines = file(MapResolutionFileName, FILE_IGNORE_NEW_LINES);
		echo "var UpdateIntervalSecs = " . UpdateIntervalSecs . ", MapFileName = '" . MapFileName . "', MapResolution = " . $lines[0] . ";\n";
	?>
	</script>
	<link href="Common.css" rel="stylesheet" type="text/css"/>
	<link href="TIUTracking.css" rel="stylesheet" type="text/css"/>
	<script type="application/x-javascript" src="js/Main.js"></script>
	<script type="application/x-javascript" src="js/Common.js"></script> 
	<script type="application/x-javascript" src="js/FlexTable.js"></script> 
	<script type="application/x-javascript" src="js/InfoBox.js"></script> 
	<script type="application/x-javascript" src="js/Map.js"></script> 
	<script type="application/x-javascript" src="js/TabControl.js"></script> 
	<script type="application/x-javascript" src="js/Timer.js"></script> 
	<script type="application/x-javascript" src="js/Vector2D.js"></script> 
	<script type="application/x-javascript" src="js/fx.js"></script>
	<!--[if IE]>
		<style type="text/css">
			.TTabName {
				padding-bottom: 3px; 
			}
		</style>
	<![endif]-->
</head>
<body>
	<div id="headerPanel" class="TCastShadow" style="text-align: center;">
		<h1>Asset Tracking System</h1>
		<label id="msgLabel"></label>
	</div>
	<button id="showHideTabPanelButton" class="TextIcon" title="Hide panel" style="position: absolute; z-index: 4; padding: 0 4px 2px;">«</button>
	<div id="tabPanel" class="TTabPanel TCastShadow" style="position: relative; left: 0; top: 0; margin: 0; padding: 0; z-index: 2; background-color: #FFF; width: 360px;">		
		<ul class="TTabNamePanel">
			<li class="TTabName">Welcome</li>
			<li class="TTabName">&nbsp;&nbsp;&nbsp;Tags&nbsp;&nbsp;&nbsp;&nbsp;</li>
			<li class="TTabName">Detectors</li>
		</ul>
		<div class="TTabContentPanel">
			<div id="welcomeTab" class="TTabContent" style="vertical-align: middle;">
				<div>
					<img src="images/user.png" width=32 height=32 style="float: left;" />
					<div class="gc" id="loginDialog">
						<div class="gcr Caption">Admin</div>
						<div class="gcr"><div class="gcrd1">Username:</div><input id="usernameTextBox" type="textbox" maxlength="30" /><br /></div>
						<div class="gcr"><div class="gcrd1">Password:</div><input id="passwordTextBox" type="password" maxlength="30" /><br /></div>
						<div><button id="loginButton">Log in</button></div>
					</div>
					<div class="gc" id="loggedInDialog" style="display: none;">
						<label id="greetingLabel" style="padding: 0 1em 0 0;"></label>
						<a id="changePwdLink" href="javascript: showChangePwdForm();" style="padding: 0 1em; border-left: 1px solid #CCC;">Change password</a>
						<a id="logOutLink" href="javascript: logout();" style="padding: 0 1em; border-left: 1px solid #CCC;">Log out</a>
						<br /><br />
					</div>
				</div>

				<div>
					<img src="images/options.png" width=32 height=32 style="float: left;" />
					<div class="gc">
						<div class="gcr Caption">Options</div>
						<input id="showAssetsCheckBox" type="checkbox" checked /><label for="showAssetsCheckBox">Show tags</label><br />
						<input id="showDetectorsCheckBox" type="checkbox" checked /><label for="showDetectorsCheckBox">Show detectors</label><br />
					</div>
				</div>
				<iframe id="mapUploadFrame" src="MapUpload.php" style="border: none; width: 100%;"></iframe>
			</div>
			<div id="assetsTab" class="TTabContent">
				<input type="textbox" id="assetSearchBox" class="TSearchBox" placeholder="Search for an asset"/>
				<div class="TableHeader"><div class="Col Id">ID</div><div class="Col AssetId">Asset ID</div><div class="Col X">X</div><div class="Col Y">Y</div><div class="Col Battery">Battery</div><div class="Col More"></div></div>
			</div>
			<div id="detectorsTab" class="TTabContent">
				<input type="textbox" id="detectorSearchBox" class="TSearchBox" placeholder="Search for a detector"/>
				<div class="TableHeader"><div class="Col Id">ID</div><div class="Col X">X</div><div class="Col Y">Y</div><div class="Col Battery">Battery</div><div class="Col More"></div></div>
			</div>
		</div>
	</div>
	<div id="mapToolbar" class="TCastShadow">
		<button id="zoomOutButton" class="LeftPill TextIcon Translucent" title="Zoom out">-</button>
		<button id="zoomFitButton" class="Middle TextIcon Translucent" title="Zoom fit">[]</button>
		<button id="zoomInButton" class="RightPill TextIcon Translucent" title="Zoom in">+</button>
	</div>
</body>
</html>