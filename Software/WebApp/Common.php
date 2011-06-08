<?php
session_start();

const DBURL		 	= 'db.cecs.pdx.edu';
const DBUsername	= 'hoangman';
const DBPassword	= 'c@p2011$#tT';
const DBName		= 'hoangman';

/* Response Status */
const rsOK					= 0;
const rsSessionEnd			= 1;
const rsInvalidArgument		= 2;
const rsSqlError			= 3;

const UpdateIntervalSecs	= 3;

const MapFileName			= 'FloorPlan.jpg';
const MapResolutionFileName = 'MapResolution.txt';

function validateUser($username)
{
	session_regenerate_id();
	$_SESSION['loggedIn'] = true;
	setcookie('username', $username);
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
?>
