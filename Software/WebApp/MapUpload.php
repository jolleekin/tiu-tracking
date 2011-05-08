<html>
<body>
<?php
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

list($name, $error) = upload('mapFile', 'images', 'FloorPlan.jpg', 'jpg,jpeg,gif,png');

$status = 0;
$data = 'images/FloorPlan.jpg';
if ($error)
{
	$status = 1;
	$data = $error;
}

print json_encode(array("status" =>	$status, "data" => $data));

?>
</body>
</html>
