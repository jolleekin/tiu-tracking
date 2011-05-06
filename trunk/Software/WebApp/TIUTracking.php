<?php

$request = $_POST['request'];

if (!$request)
	return;

session_start();




const DBURL		 	= 'db.cecs.pdx.edu';
const DBUsername	= 'hoangman';
const DBPassword	= 'c@p2011$#tT';
const DBName		= 'hoangman';

/* Response Status */
const	rsOK		= 0;
const	rsError		= -1;

/* Creates a 3 character sequence */
function createSalt()
{
	$s = md5(uniqid(rand(), true));
	return substr($s, 0, 3);
}

function getTagInfo()
{
	$result = mysql_query('SELECT * FROM Tags') or return '';
	$tags = array();
	
	while ($row = mysql_fetch_array($result, MYSQL_ASSOC))
		$tags[$row['TagID']] = $row['AssetID'];
	$tagCount = count($tags);

	mysql_free_result($result);
	
	$info = '[';
	foreach ($tags as $tagId => $assetId)
	{
		$result = mysql_query("SELECT * FROM TagInfo WHERE TagID = $tagId ORDER BY `Timestamp` DESC LIMIT 1") or return '';
		if ($row = mysql_fetch_array($result, MYSQL_ASSOC))
			$info .= sprintf("{s:'%s',t:%d,a:'%s',x:%0.1f,y:%0.1f,b:%d},", $row['Timestamp'], $row['TagID'], $assetId, $row['X'], $row['Y'], $row['Battery']);
		mysql_free_result($result);
	}
	$info .= '{}]';
	return $info;
}

function getDetectorInfo()
{
	$result = mysql_query('SELECT * FROM Detectors') or return '';
	
	$info = '[';
	while ($row = mysql_fetch_array($result, MYSQL_ASSOC))
		$info .= sprintf('{d:%d,x:%0.1f,y:%0.1f,b:%d},', $row['DetectorID'], $row['X'], $row['Y'], 0);
	$info .= '{}]';

	mysql_free_result($result);
	return $info;
}

function printResponse($status, $data)
{
	global $request;
	echo "0,{request:'$request',status:$status,data:$data}";
}


// Connect to the database
//TODO: Save the connection for each session to reduce overhead.
$connection = mysql_connect(DBURL, DBUsername, DBPassword) or die(mysql_error());
mysql_select_db(DBName) or die('Could not select database');

switch ($request)
{
	case 'login':
		$username = $_POST['username'];
		$password = $_POST['password'];

		// Sanitize username and query user info
		$username = mysql_real_escape_string(strtolower($username));
		$query = "SELECT password, salt FROM Users WHERE username = '$username'";
		$result = mysql_query($query);
		
		if(mysql_num_rows($result) < 1) // no such user exists
			printResponse(rsError, "'Invalid username'");
		else
		{
			$userData = mysql_fetch_array($result, MYSQL_ASSOC);
			$givenPassword = hash('sha256', $userData['salt'] . hash('sha256', $password));
			
			if($givenPassword != $userData['password']) // incorrect password
				printResponse(rsError, "'Invalid password'");
			else
			{
				session_regenerate_id (); // this is a security measure
				$_SESSION['loggedIn'] = true;
				printResponse(rsOK, 0);
			}
		}
		break;
		
	case 'logout':
		$_SESSION = array();
		session_destroy();
		break;
		
	case 'get-tags':
		printResponse(rsOK, getTagInfo());
		break;
		
	case 'get-detectors':
		printResponse(rsOK, getDetectorInfo());
		break;
		
	default:
		if (isset($_SESSION['loggedIn']))
		{
			switch ($request)
			{
				case 'add-tag':
					$tagIdStr = $_POST['tag-id'];
					$tagId = intval($tagIdStr);
					if ($tagId > 0)
					{
						$assetId = $_POST['asset-id'];
						//TODO: Valid asset id
						$assetId = "'$assetId'";  // Add quotation marks since it's a string
						mysql_query("INSERT INTO Tags VALUE ($tagId, $assetId) ON DUPLICATE KEY UPDATE AssetID = $assetId");
						printResponse(rsOK, $tagId);
					}
					else
						printResponse(rsError, "'Tag ID must be a positive integer: $tagIdStr'");
					break;
					
				case 'del-tag':
					$tagId = $_POST['tag-id'];
					mysql_query("DELETE FROM Tags WHERE TagID = $tagId");
					mysql_query("DELETE FROM TagInfo WHERE TagID = $tagId");
					printResponse(rsOK, $tagId);
					break;
					
				case 'add-detector':
					$detectorIdStr = $_POST['detector-id'];
					$detectorId = intval($detectorIdStr);
					if ($detectorId > 0)
					{
						$x = $_POST['x'];
						$y = $_POST['y'];
						if (is_numeric($x) && is_numeric($y))
						{
							mysql_query("INSERT INTO Detectors VALUE ($detectorId, $x, $y) ON DUPLICATE KEY UPDATE X = $x, Y = $y");
							printResponse(rsOK, $detectorId);
						}
						else
							printResponse(rsError, "'Invalid (x, y): ($x, $y)'");
					}
					else
						printResponse(rsError, "'Detector ID must be a positive integer: $detectorId'");
					break;
					
				case 'del-detector':
					$detectorId = $_POST['detector-id'];
					mysql_query("DELETE FROM Detectors WHERE DetectorID = $detectorId");
					printResponse(rsOK, $detectorId);
			}
		}
}

mysql_close($connection);
?>
