<?php

$user="root";
$password="";
$database="eternity";

$p_hostname = mysql_real_escape_string($_POST["hostname"]);
$p_score    = mysql_real_escape_string($_POST["score"]);
$p_grid     = mysql_real_escape_string($_POST["grid"]);

if ( strlen($p_hostname) > 3 ) {
	mysql_connect(localhost,$user,$password);
	mysql_select_db($database);

	mysql_query("begin");

	$query = "replace into eter_host values ('$p_hostname', now(), $p_score);";
	mysql_query($query);

	$query = "select count(*) from eter_grid where grid = '$p_grid';";
	$result = mysql_query($query);
	$row = mysql_fetch_row($result);

	if ( $row[0] == 0 ) {
		$query = "insert into eter_grid values (null, '$p_hostname', $p_score, '$p_grid', now());";
		mysql_query($query);
	}

	mysql_query("commit");
	mysql_close();
}
?>
