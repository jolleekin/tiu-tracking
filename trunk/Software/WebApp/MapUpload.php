<!doctype html>
<html>
<head>
	<meta http-Equiv="Expires" Content="0">
	<meta http-Equiv="Pragma" Content="no-cache">
	<meta http-Equiv="Cache-Control" Content="no-cache">
	<link href="Common.css" rel="stylesheet" type="text/css"/>
</head>
<body onload="if (top.changeMapImage) top.changeMapImage();" style="background: none;">
<?php

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

// Display the upload form if the user has logged in.
if ($loggedIn)
{
	echo <<<FORM
	<form method="post" enctype="multipart/form-data">
		<fieldset>
			<legend>Map</legend>
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

$response = '';

// If the user has specified an image, try upload it.
if ($_FILES['mapFile']['name'])
{
	if ($loggedIn)
	{
		list($name, $error) = upload('mapFile', 'images', 'FloorPlan.jpg', 'jpg,jpeg,gif,png');

		$status = 0;
		$data = 'images/FloorPlan.jpg';
		if ($error)
		{
			$status = 1;
			$data = $error;
		}
	}
	else
	{
		$status = 1;
		$data	= "'Your session has expried. Please log in again.'";
	}

	$response = json_encode(array("status" =>	$status, "data" => $data));
}

echo '<label style="display: none;">' . $response . '</label>';
?>
</body>
</html>