<?php

	const URL = 'db.cecs.pdx.edu';
	const User = 'hoangman';
	const Password = 'c@p2011$#tT';
	const Database = 'hoangman';


	/*
	global $connection;

	function open($savePath, $sessionName)
	{
		if ($connection == null)
		{
			$connection = mysql_connect(URL, User, Password) or die('Could not connect: ' . mysql_error());
			mysql_select_db(Database) or die('Could not select database');
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
	$assets = array();
	
	// Connect and select the database
	$connection = mysql_connect(URL, User, Password) or die(mysql_error());
	mysql_select_db(Database) or die('Could not select database');

	$assetId = $_GET['id'];
	// Perform SQL query
	$result = mysql_query('SELECT * FROM Tags') or die(mysql_error());

	// Print results in JSON
	while ($row = mysql_fetch_row($result))
		$assets[$row[0]] = $row[1];	// row[0] = TagID, row[1] = AssetName
	$assetCount = count($assets);

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
	
	if ($assetId == '')
		$result = mysql_query('SELECT * FROM TagInfo ORDER BY `Timestamp` DESC LIMIT ' . $assetCount) or die(mysql_error());
	
	echo '[';
	$i = 0;
	while ($row = mysql_fetch_row($result))
	{
		printf('{id:"%s",iL:[{t:"%s",x:%0.1f,y:%0.1f,b:%d}]},', $assets[$row[0]], $row[1], $row[2], $row[3], $row[4]);
		$i++;
	}
	echo '{}]';	// Attach a dummy element
	
	mysql_free_result($result);

	// Closing connection
	mysql_close($connection);
?>
