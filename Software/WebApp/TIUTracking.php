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
const rsOK					= 0;
const rsSessionEnd			= 1;
const rsInvalidArgument		= 2;

/* Creates a 3 character sequence */
function createSalt()
{
	$s = md5(uniqid(rand(), true));
	return substr($s, 0, 3);
}

function invalidateUser()
{
	$_SESSION = array();
	$expire = time() - 100000;
	if (isset($_COOKIE[session_name()]))
		setcookie(session_name(), '', $expire);
	if (isset($_COOKIE['username']))
		setcookie('username', '', $expire);
	session_destroy();
}

function getTagsInfo()
{
	$result = mysql_query('SELECT * FROM Tags ORDER BY TagID') or die();
	$tags = array();
	
	while ($row = mysql_fetch_array($result, MYSQL_ASSOC))
		$tags[$row['TagID']] = $row['AssetID'];
	$tagCount = count($tags);

	mysql_free_result($result);
	
	$info = '[';
	foreach ($tags as $tagId => $assetId)
	{
		$result = mysql_query("SELECT * FROM TagInfo WHERE TagID = $tagId ORDER BY `Timestamp` DESC LIMIT 1") or die();
		$row = mysql_fetch_array($result, MYSQL_ASSOC);
		$x = -1;
		$y = -1;
		$b = 0;
		if ($row)
		{
			$x = $row['X'];
			$y = $row['Y'];
			$b = $row['Battery'];
		}
		$info .= sprintf("{s:'%s',i:%d,a:'%s',x:%0.1f,y:%0.1f,b:%d},", $row['Timestamp'], $tagId, $assetId, $x, $y, $b);
		mysql_free_result($result);
	}
	$info .= '{}]';
	return $info;
}

function getDetectorsInfo()
{
	$result = mysql_query('SELECT * FROM Detectors ORDER BY DetectorID') or die();
	
	$info = '[';
	while ($row = mysql_fetch_array($result, MYSQL_ASSOC))
		$info .= sprintf('{i:%d,x:%0.1f,y:%0.1f,b:%d},', $row['DetectorID'], $row['X'], $row['Y'], 0);
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
$connection = mysql_connect(DBURL, DBUsername, DBPassword) or die();
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
			printResponse(rsInvalidArgument, "'Invalid username'");
		else
		{
			$userData = mysql_fetch_array($result, MYSQL_ASSOC);
			$givenPassword = hash('sha256', $userData['salt'] . hash('sha256', $password));
			
			if($givenPassword != $userData['password']) // incorrect password
				printResponse(rsInvalidArgument, "'Invalid password'");
			else
			{
				session_regenerate_id (); // this is a security measure
				$_SESSION['loggedIn'] = true;
				setcookie('username', $username);
				printResponse(rsOK, 0);
			}
		}
		break;
		
	case 'logout':
		invalidateUser();
		break;
		
	case 'get-tags':
		printResponse(rsOK, getTagsInfo());
		break;
		
	case 'get-detectors':
		printResponse(rsOK, getDetectorsInfo());
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
						// Returns the info of the added tag.
						$timestamp = date('Y-m-d H:i:s');
						printResponse(rsOK, "{s:'$timestamp',i:$tagId,a:$assetId,x:-1,y:-1,b:0}");
					}
					else
						printResponse(rsInvalidArgument, "'Tag ID must be a positive integer: $tagIdStr'");
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
							// Returns the info of the added detector.
							printResponse(rsOK, "{i:$detectorId,x:$x,y:$y,b:0}");
						}
						else
							printResponse(rsInvalidArgument, "'Invalid location: ($x, $y)'");
					}
					else
						printResponse(rsInvalidArgument, "'Detector ID must be a positive integer: $detectorId'");
					break;
					
				case 'del-detector':
					$detectorId = $_POST['detector-id'];
					mysql_query("DELETE FROM Detectors WHERE DetectorID = $detectorId");
					printResponse(rsOK, $detectorId);
			}
		}
		else
		{
			invalidateUser();
			printResponse(rsSessionEnd, "'Your session has expired. Please log in again.'");
		}
}

mysql_close($connection);
?>
