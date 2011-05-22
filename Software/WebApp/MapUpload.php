<!doctype html>
<html>
<head>
	<meta http-Equiv="Expires" Content="0">
	<meta http-Equiv="Pragma" Content="no-cache">
	<meta http-Equiv="Cache-Control" Content="no-cache">
	<link href="Common.css" rel="stylesheet" type="text/css"/>
</head>
<?php

include 'Consts.php';

session_start();

function upload($fileId, $folder = '', $fileName = '', $types = '')
{
    if(!$_FILES[$fileId]['name']) return array('', 'No file specified');

    $name = $_FILES[$fileId]['name'];
    //Get file extension
    $ExtArray = split("\.", basename($name));
    $ext = strtolower($ExtArray[count($ExtArray)-1]); //Get the last extension

    $allowedTypes = explode(",", strtolower($types));
    if ($types)
        if (!in_array($ext, $allowedTypes))
		{
            $result = "'" . $name . "' is not a valid file."; //Show error if any.
            return array('', $result);
        }

    //Where the file must be uploaded to
    if ($folder) $folder .= '/';//Add a '/' at the end of the folder
	if ($fileName)
		$uploadFile = $folder . $fileName;
	else
		$uploadFile = $folder . $name;

    $result = '';
    //Move the file from the stored location to the new location
    if (!move_uploaded_file($_FILES[$fileId]['tmp_name'], $uploadFile))
	{
        $result = "Cannot upload the file '" . $name . "'"; //Show error if any.
        if (!file_exists($folder))
            $result .= " : Folder don't exist.";
        else if (!is_writable($folder))
            $result .= " : Folder not writable.";
        else if (!is_writable($uploadFile))
            $result .= " : File not writable.";
        $name = '';
        
    }
	else
        if (!$_FILES[$fileId]['size'])
		{
            @unlink($uploadFile);//Delete the Empty file
            $name = '';
            $result = "Empty file found - please use a valid file."; //Show the error message
        }
		else
		{
			chown($uploadFile, 'hoangman');
            chmod($uploadFile, 777);//Make it universally writable.
        }

    return array($name, $result);
}

$loggedIn = isset($_SESSION['loggedIn']);

$hasParams = false;
$onload = '';
$status = rsOK;
$data = 0;


$resolution = $_POST['resolution'];
if ($resolution)
{
	$hasParams = true;
	if (loggedIn)
	{
		$resolution = intval($resolution);
		if ($resolution > 0)
		{
			$handle = fopen(MapResolutionFileName, 'w');
			fwrite($handle, $resolution);
			fclose($handle);
			$data = $resolution;
		}
		else
		{
			$status = rsInvalidArgument;
			$data = "'Invalid resolution: $resolution'";
			$lines = file(MapResolutionFileName, FILE_IGNORE_NEW_LINES);
			$resolution = $lines[0];
		}
	}
	else
	{
		$status = rsSessionEnd;
		$data	= "'Your session has expried. Please log in again.'";
	}
}
else
{
	$lines = file(MapResolutionFileName, FILE_IGNORE_NEW_LINES);
	$data = $resolution = $lines[0];
}

// If the user has specified an image, try upload it.
if ($_FILES['mapFile']['name'])
{
	$hasParams = true;
	if ($loggedIn)
	{
		list($name, $error) = upload('mapFile', 'images', MapImageFileName, 'jpg,jpeg,gif,png');

		if ($error)
		{
			$status = rsInvalidArgument;
			$data = $error;
		}
		$data = "'" . addslashes($data) . "'";
	}
	else
	{
		$status = rsSessionEnd;
		$data	= "'Your session has expried. Please log in again.'";
	}
}

// Only calls top.changeMapImage if the uses has specified the resolution and/or the map image.
if ($hasParams)
	$onload = "onload=\" if (top.changeMapImage) top.changeMapImage({status:$status,data:$data});\"";

echo "<body $onload style=\"background: none;\">";


// Display the upload form if the user has logged in.
if ($loggedIn)
{
	echo <<<FORM
	<form method="post" enctype="multipart/form-data">
		<fieldset>
			<legend>Map</legend>
			<div style="margin-bottom: 0.5em;"><label for="resolution" style="margin-right: 7px;">Resolution:</label><input id="resolution" name="resolution" type="textbox" value="$resolution" style="width: 30px;" /><label>&nbsp;pixels per unit length</label></div>
			<div style="margin-bottom: 0.5em;">
				<button onclick="mapFile.click(); return false;">Browse</button>
				<input id="mapFile" name="mapFile" type="file" style="width: 0; height: 0; opacity: 0;"
					onchange="var v = this.value; var i = v.lastIndexOf('/');
						if (i == -1) i = v.lastIndexOf('\\\\');
						fileNameTextBox.value = v.substr(i + 1);" />
				<input id="fileNameTextBox" type="textbox" disabled style="margin-left: 1em;" placeholder="Map image file name"/><br />
			</div>
			<div style="margin-bottom: 0.5em;"><input type="submit" class="Button" value="Upload" style="width: 53px;"/></div>
		</fieldset>
	</form>
FORM;
}
?>
</body>
</html>