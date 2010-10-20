<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<html>
<head>
	<title>Sample Partner Application - JOSSO</title>
	<meta name="description" content="Java Open Single Signon">
</head>

<body>

    <h1>This is a simple PHP JOSSO partner application.</h1>

    <h2>You are accessing an ignored resource</h2>

    <p>This page will be ignored by JOSSO, no identity information will be available here.
    Take a look at JOSSO PHP Agent configuration file for details : <b>josso-cfg.inc</b></p>
<?php
    $user = $josso_agent->getUserInSession();
    if (isset($user)) {
        // Never should be here!
        echo 'Username : ' . $user->getName() . '<br><br>';
    }
?>


</body>
</html>