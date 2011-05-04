<?php
session_start();

const DBURL		 	= 'db.cecs.pdx.edu';
const DBUsername	= 'hoangman';
const DBPassword	= 'c@p2011$#tT';
const DBName		= 'hoangman';

const OK			= 'OK';

/* Creates a 3 character sequence */
function createSalt()
{
	$s = md5(uniqid(rand(), true));
	return substr($s, 0, 3);
}

$request = $_POST['q'];

if (!$request)
	return;

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
		$query = "SELECT password, salt FROM Users WHERE username = '$username';";
		$result = mysql_query($query);
		
		if(mysql_num_rows($result) < 1) // no such user exists
			echo 'Invalid username';
		else
		{
			$userData = mysql_fetch_array($result, MYSQL_ASSOC);
			$givenPassword = hash('sha256', $userData['salt'] . hash('sha256', $password));
			
			if($givenPassword != $userData['password']) // incorrect password
				echo 'Invalid password';
			else
			{
				session_regenerate_id (); // this is a security measure
				$_SESSION['loggedIn'] = true;
				echo OK;
			}
		}
		break;
		
	case 'logout':
		$_SESSION = array();
		session_destroy();
		break;
		
	case 'get_asset':
		$result = mysql_query('SELECT * FROM Tags') or die();
		$assets = array();
		
		while ($row = mysql_fetch_row($result))
			$assets[$row[0]] = $row[1];	// row[0] = TagID, row[1] = AssetName
		$assetCount = count($assets);

		mysql_free_result($result);
		
		echo '[';
		foreach ($assets as $tagId => $assetId)
		{
			$result = mysql_query('SELECT * FROM TagInfo WHERE TagID = ' . $tagId . ' ORDER BY `Timestamp` DESC LIMIT 1') or die(mysql_error());
			if ($row = mysql_fetch_array($result, MYSQL_ASSOC))
				printf('{s:"%s",t:%d,a:"%s",x:%0.1f,y:%0.1f,b:%d},', $row['Timestamp'], $row['TagID'], $AssetId, $row['X'], $row['Y'], $row['Battery']);
			mysql_free_result($result);
		}
		echo ',{}]';
		break;
		
	case 'get_detector':
		
		break;
		
	default:
		if ($_SESSION['loggedIn'])
		{
			switch ($request)
			{
				case 'add_asset':
					//TODO: Validate tag id and asset id
					$tagId	 = $_POST['t'];
					$assetId = $_POST['a'];
					mysql_query("INSERT INTO Tags VALUE ($tagId, $assetId) ON DUPLICATE KEY UPDATE AssetID = $assetId");
					echo OK;
					break;
					
				case 'del_asset':
					$tagId	 = $_POST['t'];
					mysql_query("DELETE FROM Tags WHERE TagID = $tagId");
					mysql_query("DELETE FROM TagInfo WHERE TagID = $tagId");
					echo $tagId;	// Returns tag id so that the client can delete the right row in the table.
					break;
					
				case 'add_detector':
					$detectorId = $_POST['d'];
					$x = $_POST['x'];
					$y = $_POST['y'];
					//TODO: Validate detector id, x and y
					mysql_query("INSERT INTO Detectors VALUE ($detectorId, $x, $y) ON DUPLICATE KEY UPDATE X = $x, Y = $y");
					echo OK;
					break;
					
				case 'del_detector':
					$detectorId = $_POST['d'];
					mysql_query("DELETE FROM Detectors WHERE DetectorID = $detectorId");
					echo $detectorId;
			}
		}
}

mysql_close($connection);
?>
