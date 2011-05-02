<?php

session_start();

if (!$_SESSION['Valid'])
	$_SESSION['Valid'] = 1;

	const DBURL = 'db.cecs.pdx.edu';
	const DBUsername = 'hoangman';
	const DBPassword = 'c@p2011$#tT';
	const DBName = 'hoangman';

	/*
	global $connection;

	function open($savePath, $sessionName)
	{
		if ($connection == null)
		{
			$connection = mysql_connect(DBURL, DBUsername, DBPassword) or die('Could not connect: ' . mysql_error());
			mysql_select_db(DBName) or die('Could not select database');
		}
	  return true;
	}

	function close()
	{
	  return(true);
	}

	function read($id)
	{
		global $sess_save_path;

		$sess_file = "$sess_save_path/sess_$id";
		return (string) @file_get_contents($sess_file);
	}

	function write($id, $sess_data)
	{
		global $sess_save_path;

		$sess_file = "$sess_save_path/sess_$id";
		if ($fp = @fopen($sess_file, "w"))
		{
			$return = fwrite($fp, $sess_data);
			fclose($fp);
			return $return;
		}
		else
			return(false);
	}

	function destroy($id)
	{
		global $sess_save_path;

		$sess_file = "$sess_save_path/sess_$id";
		return(@unlink($sess_file));
	}

	function gc($maxlifetime)
	{
		global $sess_save_path;

		foreach (glob("$sess_save_path/sess_*") as $filename)
			if (filemtime($filename) + $maxlifetime < time())
				@unlink($filename);
	  return true;
	}

	session_set_save_handler("open", "close", "read", "write", "destroy", "gc");

	session_start();
	*/

	/*
	1.	Load configuration from database
	
	2.	If not fetched or database changed then
			Refetch tag table
	*/
	$assetTags = array();
	
	// Connect and select the database
	$connection = mysql_connect(DBURL, DBUsername, DBPassword) or die(mysql_error());
	mysql_select_db(DBName) or die('Could not select database');

	$assetId = $_GET['id'];

	$result = mysql_query('SELECT * FROM Tags') or die(mysql_error());

	while ($row = mysql_fetch_row($result))
		$assetTags[$row[0]] = $row[1];	// row[0] = TagID, row[1] = AssetName
	$assetCount = count($assetTags);

	mysql_free_result($result);

	/***************************************************************/
	
	/* Insert fake data into the database.
	
	mysql_query('set @FloorWidth = 12');
	mysql_query('set @FloorHeight = 7');
	mysql_query('insert into TagInfo values
		(1, now(), rand() * @FloorWidth, rand() * @FloorHeight, 100),
		(2, now(), rand() * @FloorWidth, rand() * @FloorHeight, 100),
		(3, now(), rand() * @FloorWidth, rand() * @FloorHeight, 100),
		(4, now(), rand() * @FloorWidth, rand() * @FloorHeight, 100),
		(5, now(), rand() * @FloorWidth, rand() * @FloorHeight, 100),
		(6, now(), rand() * @FloorWidth, rand() * @FloorHeight, 100),
		(7, now(), rand() * @FloorWidth, rand() * @FloorHeight, 100),
		(8, now(), rand() * @FloorWidth, rand() * @FloorHeight, 100),
		(9, now(), rand() * @FloorWidth, rand() * @FloorHeight, 100),
		(10, now(), rand() * @FloorWidth, rand() * @FloorHeight, 100)') or die(mysql_error());
	*/
	
	echo '[';
	if ($assetId == '')
	{
		foreach ($assetTags as $tagId => $assetName)
		{
			$result = mysql_query('SELECT * FROM TagInfo WHERE TagID = ' . $tagId . ' ORDER BY `Timestamp` DESC LIMIT 1') or die(mysql_error());
			if ($row = mysql_fetch_row($result))
				printf('{id:"%s",iL:[{t:"%s",x:%0.1f,y:%0.1f,b:%d}]},', $assetName, $row[1], $row[2], $row[3], $row[4]);
			mysql_free_result($result);
		}
	}
	echo '{' . session_id() . '}]';	// Attach a dummy element
	
	mysql_free_result($result);

	// Closing connection
	mysql_close($connection);
?>
